
package com.reactive.employee.manager.domain.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
@Configuration
public class RestConfig {

    @Value("${resilience.request.connectiontimeout}")
    private int requestConnectionTimeout;

    @Value ("${resilience.request.readtimeout}")
    private int requestReadTimeout;

    @Autowired
    private ResilienceManager resilienceManager;


    public static final Logger Employee_Director_Logger = LoggerFactory.getLogger(RestConfig.class);

    @Bean("restTemplate")
    RestTemplate restTemplate()
            throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyManagementException, KeyStoreException, IOException {

        Employee_Director_Logger.info("Rest Configurations started");
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        sslContextBuilder.loadTrustMaterial(null, new TrustAllStrategy());
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                sslContextBuilder.build(), new NoopHostnameVerifier());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                socketFactory).build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpclient);
        factory.setConnectionRequestTimeout(requestConnectionTimeout);
        factory.setConnectTimeout(requestConnectionTimeout);
        factory.setReadTimeout(requestReadTimeout);
        RestTemplate restTemplate = new RestTemplate(factory);

        BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(factory);
        restTemplate.setRequestFactory(bufferingClientHttpRequestFactory);
        HttpOutgoingTrafficInterceptor httpOutgoingTrafficInterceptor = new HttpOutgoingTrafficInterceptor(resilienceManager);
        restTemplate.setInterceptors(Collections.singletonList(httpOutgoingTrafficInterceptor));

        return restTemplate;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder)
    {
        ObjectMapper objectMapper = builder.build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

}
