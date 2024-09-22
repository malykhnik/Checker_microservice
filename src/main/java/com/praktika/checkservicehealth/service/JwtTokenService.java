package com.praktika.checkservicehealth.service;

import com.praktika.checkservicehealth.dto.TokenDto;

public interface JwtTokenService {
    String saveJwt(String token);
    String getJwt();
}
