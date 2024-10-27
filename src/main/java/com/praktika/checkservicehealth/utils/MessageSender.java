package com.praktika.checkservicehealth.utils;

import com.praktika.checkservicehealth.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class MessageSender {
    private static final RestClient restClient = RestClient.create();
    public static void sendToTG(MessageDto messageDto) {
        restClient.post()
                .uri("http://notification-microservice:8083/api/v1/sendToTG")
                .body(messageDto)
                .retrieve()
                .body(String.class);
    }
    public static void sendToMail(MessageDto messageDto) {
        restClient.post()
                .uri("http://notification-microservice:8083/api/v1/sendToMail")
                .body(messageDto)
                .retrieve()
                .body(String.class);
    }
}
