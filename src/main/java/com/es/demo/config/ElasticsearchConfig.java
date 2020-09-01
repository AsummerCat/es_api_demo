package com.es.demo.config;

import lombok.Data;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

@Data
@Configuration
@ConfigurationProperties(prefix = "elastic")
public class ElasticsearchConfig {

//    private String host;
//    private Long connectTimeout;
//    private Long socketTimeout;
//    //private int port;
//
//
//    @Bean
//    public RestHighLevelClient client() {
//        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//                .connectedTo(host)
//                .withConnectTimeout(connectTimeout)
//                .withSocketTimeout(socketTimeout)
//                .build();
//        return RestClients.create(clientConfiguration).rest();
//    }
}