package com.praktika.checkservicehealth.utils;

import com.praktika.checkservicehealth.dto.MessageDto;
import com.praktika.checkservicehealth.service.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
@Component
@RequiredArgsConstructor
public class MessageSender {
    private static final RestClient restClient = RestClient.create();
    private final KafkaProducerService kafkaProducerService;

    public void sendToTG(MessageDto messageDto) {
        kafkaProducerService.sendMessage("my-topic", messageDto.getMessage());

//        restClient.post()
//                .uri("http://notification-microservice:8083/api/v1/sendToTG")
//                .body(messageDto)
//                .retrieve()
//                .body(String.class);
    }

    public void sendToMail(MessageDto messageDto) {
        kafkaProducerService.sendMessage("my-topic", messageDto.getMessage());
//        restClient.post()
//                .uri("http://notification-microservice:8083/api/v1/sendToMail")
//                .body(messageDto)
//                .retrieve()
//                .body(String.class);
    }
}
