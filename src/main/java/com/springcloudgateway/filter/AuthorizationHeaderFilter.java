package com.springcloudgateway.filter;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final Environment env;

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
                return onError(exchange, "No Auth Header", HttpStatus.UNAUTHORIZED);

            String token = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0)
                    .replace("Bearer ", "");

            if (!isJwtValid(token))
                return onError(exchange, "Jwt Token Not Valid", HttpStatus.UNAUTHORIZED);

            return chain.filter(exchange);
        });
    }

    private boolean isJwtValid(String token) {
        boolean tokenValid = true;

        String subject = null;

        try {
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJws(token).getBody()
                    .getSubject();
        } catch (Exception e) {
            tokenValid = false;
        }

        if (subject == null || subject.isEmpty())
            tokenValid = false;

        return tokenValid;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(errorMessage);
        return response.setComplete();
    }

    public static class Config {
    }
}
