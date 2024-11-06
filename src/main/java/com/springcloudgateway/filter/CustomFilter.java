package com.springcloudgateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        //PreFilter
        return ((exchange, chain) -> {
            //http.server.reactive
            //reactive 의존성 필요
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Custom Pre Filter : request id -> {}", request);

            //Post Filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("Custom Post Filter : response code -> {}", response.getStatusCode());
            }));
        });
    }

    public static class Config {
        // Put the Configuration Properties
    }
}
