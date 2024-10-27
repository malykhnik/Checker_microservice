package com.praktika.checkservicehealth.service.impl;

import com.praktika.checkservicehealth.dto.*;
import com.praktika.checkservicehealth.entity.Endpoint;
import com.praktika.checkservicehealth.repository.EndpointRepo;
import com.praktika.checkservicehealth.service.EndpointService;
import com.praktika.checkservicehealth.service.JwtTokenService;
import com.praktika.checkservicehealth.utils.MessageSender;
import com.praktika.checkservicehealth.utils.WorkWithAuth;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EndpointServiceImpl implements EndpointService {
    private final Logger LOGGER = LoggerFactory.getLogger(EndpointServiceImpl.class);
    private final EndpointRepo endpointRepo;
    private final JwtTokenService jwtTokenService;
    private final EndpointWithTimeDto endpointWithTimeDto = EndpointWithTimeDto.getInstance();
    private final RestClient restClient = RestClient.create();


    @Value("${url.get_token.key}")
    String GET_TOKEN;
    @Value("${url.check_status.key}")
    String CHECK_STATUS;

    @PostConstruct
    public void postConstruct() {
        endpointWithTimeDto.init(endpointRepo);
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void checkAllEndpoints() {
        LOGGER.info("ВЫЗВАНА ФУНКЦИЯ checkAllEndpoints()");
        List<Endpoint> endpoints = endpointRepo.findAll();
        LOGGER.info(endpointWithTimeDto.getTimeObj().toString());
        LOGGER.info(endpoints.toString());

        if (endpoints.isEmpty()) return;

        endpoints.forEach(endpoint -> {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedTime = dtf.format(LocalDateTime.now());
            if (checkEndpointTimer(endpoint.getUsername())) {
                LOGGER.info("ЗАШЕЛ В условие checkEndpointTimer()");
                LoginEndpointDto loginEndpointDto = new LoginEndpointDto(endpoint.getUsername(), endpoint.getPassword());

                try {
                    LOGGER.info("ЗАШЕЛ В try");
                    TokenDto tokenDto = restClient.post()
                            .uri(endpoint.getUrl() + GET_TOKEN)
                            .header("Content-Type", "application/json")
                            .body(loginEndpointDto)
                            .retrieve()
                            .body(TokenDto.class);

                    LOGGER.info("TOKEN: " + tokenDto);

                    assert tokenDto != null;
                    String token = tokenDto.getToken();
                    AuthResponse authResponse = checkServiceAvailability(endpoint.getUrl(), token);

                    LOGGER.info("AuthResponse: " + authResponse);

                    EndpointStatusDto endpointStatusDto = new EndpointStatusDto(endpoint.getRole().getName(), endpoint.getUrl(), new ArrayList<>());

                    for (ServiceDto service : authResponse.getServices()) {
                        if ("inactive".equals(service.getStatus())) {
                            String message = String.format("Сервис %s не работает на эндпоинте %s. %s", service.getName(), endpoint.getUrl(), formattedTime);
                            sendNotifications(message);
                        }
                        endpointStatusDto.getServices().add(service);
                    }
                    EndpointWithTimeDto.getInstance().updateMap(endpoint.getUsername(), endpointStatusDto);

                } catch (Exception e) {
                    String message = String.format("Сервис на эндпоинте %s не отвечает. %s", endpoint.getUrl(), formattedTime);
                    sendNotifications(message);
                    List<ServiceDto> list = new ArrayList<>();
                    list.add(new ServiceDto("endpoint", "no connection", new CrudStatusDto(false, false, false, false)));
                    EndpointWithTimeDto.getInstance().updateMap(endpoint.getUsername(), new EndpointStatusDto(endpoint.getRole().getName(), endpoint.getUrl(), list));
                }
            } else {
                LOGGER.info("НЕ ЗАШЕЛ В if");
            }
        });
    }

    @Override
    public void checkEndpointByUrl(String url) {
        Optional<Endpoint> endpointOptional = endpointRepo.findEndpointByUrl(url);
        if (endpointOptional.isPresent()) {
            Endpoint endpoint = endpointOptional.get();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedTime = dtf.format(LocalDateTime.now());
            LoginEndpointDto loginEndpointDto = new LoginEndpointDto(endpoint.getUsername(), endpoint.getPassword());
            try {
                TokenDto tokenDto = restClient.post()
                        .uri(endpoint.getUrl() + GET_TOKEN)
                        .header("Content-Type", "application/json")
                        .body(loginEndpointDto)
                        .retrieve()
                        .body(TokenDto.class);

                assert tokenDto != null;
                String token = tokenDto.getToken();
                AuthResponse authResponse = checkServiceAvailability(endpoint.getUrl(), token);

                LOGGER.info("authResponse: {}", authResponse);
                EndpointStatusDto endpointStatusDto = new EndpointStatusDto(endpoint.getRole().getName(), endpoint.getUrl(), new ArrayList<>());

                for (ServiceDto service : authResponse.getServices()) {
                    if ("inactive".equals(service.getStatus())) {
                        String message = String.format("Сервис %s не работает на эндпоинте %s. %s", service.getName(), endpoint.getUrl(), formattedTime);
                        sendNotifications(message);
                    }
                    endpointStatusDto.getServices().add(service);
                }
                EndpointWithTimeDto.getInstance().updateMap(endpoint.getUsername(), endpointStatusDto);

            } catch (RestClientException e) {
                String message = String.format("Сервис на эндпоинте %s не отвечает. %s", endpoint.getUrl(), formattedTime);
                sendNotifications(message);
                List<ServiceDto> list = new ArrayList<>();
                list.add(new ServiceDto("endpoint", "no connection", new CrudStatusDto(false, false, false, false)));
                EndpointWithTimeDto.getInstance().updateMap(endpoint.getUsername(), new EndpointStatusDto(endpoint.getRole().getName(), endpoint.getUrl(), list));
            }

        } else {
            LOGGER.info("ЭНДПОИНТ ПУСТОЙ");
        }
    }

    private void sendNotifications(String message) {
        MessageDto messageDto = MessageDto.builder()
                .message(message)
                .build();
        MessageSender.sendToTG(messageDto);
        MessageSender.sendToMail(messageDto);
    }

    private AuthResponse checkServiceAvailability(String url, String token) {
        return restClient.get()
                .uri(url + CHECK_STATUS)
                .header("token", token)
                .retrieve()
                .body(AuthResponse.class);
    }

    private boolean checkEndpointTimer(String endpoint) {
        Map<String, TimeDto> map = EndpointWithTimeDto.getInstance().getTimeObj();
        LOGGER.info("MAP: " + map);
        if (map.get(endpoint).getLastVisit() == null) {
            Optional<Endpoint> endpointOptional = endpointRepo.findEndpointByUsername(endpoint);
            endpointOptional.ifPresent(value -> EndpointWithTimeDto.getInstance().getTimeObj().put(endpoint, new TimeDto(new EndpointStatusDto(), Instant.now(), value.getPeriod())));
        }
        if (map.get(endpoint).getLastVisit().plus(map.get(endpoint).getTimePeriod()).isBefore(Instant.now())) {
            map.get(endpoint).setLastVisit(Instant.now());
            return true;
        }
        return false;
    }

    @Override
    public List<OutputDataDto> getSavedData() {
        List<OutputDataDto> list = new ArrayList<>();
        for (Map.Entry<String, TimeDto> entry : EndpointWithTimeDto.getInstance().getTimeObj().entrySet()) {
            OutputDataDto temp = new OutputDataDto();
            temp.setServices(entry.getValue().getEndpoint().getServices());
            temp.setUrl(entry.getValue().getEndpoint().getUrl());
            temp.setRole(entry.getValue().getEndpoint().getRole());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
            String formattedTime = dtf.format(entry.getValue().getLastVisit());
            temp.setTime(formattedTime);
            list.add(temp);

            String currentRole = WorkWithAuth.getCurrentRole(jwtTokenService);

            String formattedRole = currentRole.split("_")[1];
            if (entry.getValue().getEndpoint().getServices() != null) {
                for (ServiceDto service : entry.getValue().getEndpoint().getServices()) {
                    if (formattedRole.equals("user") && service.getCrud_status() != null) {
                        service.getCrud_status().setCreate(false);
                        service.getCrud_status().setDelete(false);
                    }
                }
            }
        }
        return list;
    }

}