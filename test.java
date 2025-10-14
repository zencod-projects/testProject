// SSL skipping logic
    @Override
    protected Resource resolveResource(String filename) {
        Resource resource = super.resolveResource(filename);
        
        // If it's an HTTPS URL, wrap it with SSL skipping
        if (resource instanceof UrlResource) {
            URL url = ((UrlResource) resource).getURL();
            if ("https".equals(url.getProtocol())) {
                return new NoSSLUrlResource(url);
            }
        }
        
        return resource;
    }
    
    // Custom Resource implementation that skips SSL
    private static class NoSSLUrlResource extends UrlResource {
        
        private static final SSLSocketFactory SSL_SOCKET_FACTORY;
        private static final HostnameVerifier HOSTNAME_VERIFIER;
        
        static {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { 
                            return null; 
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
                };
                
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                SSL_SOCKET_FACTORY = sc.getSocketFactory();
                HOSTNAME_VERIFIER = (hostname, session) -> true;
                
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        public NoSSLUrlResource(URL url) {
            super(url);
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            URL url = getURL();
            if ("https".equals(url.getProtocol())) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setSSLSocketFactory(SSL_SOCKET_FACTORY);
                connection.setHostnameVerifier(HOSTNAME_VERIFIER);
                return connection.getInputStream();
            }
            return super.getInputStream();
        }
    }
