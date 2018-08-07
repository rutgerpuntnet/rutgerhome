package net.rutger.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Services {

    private final RestTemplateBuilder restTemplateBuilder;

    @Autowired
    public Services(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    public RestTemplate createRestTemplate() {
        return restTemplateBuilder.build();
    }

}
