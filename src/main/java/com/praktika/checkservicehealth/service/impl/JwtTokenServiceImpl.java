package com.praktika.checkservicehealth.service.impl;

import com.praktika.checkservicehealth.service.JwtTokenService;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {
    private static final String JWT_CACHE = "jwt_cache";

    @Override
    @CachePut(value = JWT_CACHE, key = "'jwt_token'")
    public String saveJwt(String token) {
        System.out.println("ВЫЗВАНА ФУНКЦИЯ saveJwt");
        return token;
    }

    @Override
    @Cacheable(value = JWT_CACHE, key = "'jwt_token'")
    public String getJwt() {
        System.out.println("ВЫЗВАНА ФУНКЦИЯ getJwt");
        return null;
    }
}
