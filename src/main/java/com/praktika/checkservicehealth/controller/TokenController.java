package com.praktika.checkservicehealth.controller;

import com.praktika.checkservicehealth.dto.TokenDto;
import com.praktika.checkservicehealth.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class TokenController {
    private final JwtTokenService jwtTokenService;

    @PostMapping("/setJwt")
    public ResponseEntity<String> setJwtToken(@RequestBody TokenDto tokenDto) {
        System.out.println("TOKEN DTO = " + tokenDto);
        if (tokenDto.getToken() == null || tokenDto.getToken().isEmpty()) {
            System.out.println("ПУСТОЙ ТОКЕН!!!!!!!");
            return ResponseEntity.badRequest().body("Token is missing");
        }

        return ResponseEntity.ok().body(jwtTokenService.saveJwt(tokenDto.getToken()));
    }
}
