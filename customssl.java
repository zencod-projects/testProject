package com.example.templaterendering.config;

import com.example.templaterendering.message.CitiReloadableResourceBundleMessageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PostConstruct;
import org.thymeleaf.spring6.SpringTemplateEngine;

import javax.net.ssl.HttpsURLConnection;

@Configuration
public class MessageSourceConfiguration {
    
    @Value("${message.source.base.url}")
    private String messageSourceBaseUrl;
    
    @Value("${ssl.trust-store-alias}")
    private String clientAlias;
    
    @Value("${ssl.trust-store-location}")
    private String keyStorePath;
    
    @Value("${ssl.trust-store-password}")
    private String certPass;
    
    @PostConstruct
    public void configureSSLForMessageSource() {
        if (messageSourceBaseUrl.startsWith("https://")) {
            try {
                // Reuse the same SSL configuration as your Feign client
                SSLContext sslContext = SSLFactoryUtil.getSSLContext(keyStorePath, certPass, clientAlias);
                
                // Apply the same SSL configuration to HttpsURLConnection
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new NoopHostnameVerifier());
                
                System.out.println("SSL configured for MessageSource using existing SSLFactoryUtil");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure SSL for MessageSource", e);
            }
        }
    }
    
    
    
    @Bean("springTemplateEngine")
    public SpringTemplateEngine citiTemplateEngine(MessageSource messageSource) {
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.setTemplateEngineMessageSource(messageSource);
        return springTemplateEngine;
    }
}
