package com.praktika.checkservicehealth.utils;

import com.praktika.checkservicehealth.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class WorkWithAuth {
    private static final RestClient restClient = RestClient.create();
    public static String getCurrentRole(JwtTokenService jwtTokenService) {
        return restClient.get()
                .uri("http://auth-microservice:8080/api/v1/getCurrentRole")
                .header("Authorization", "Bearer " + jwtTokenService.getJwt())
                .retrieve()
                .body(String.class);
    }
}
