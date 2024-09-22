package com.praktika.checkservicehealth.controller;

import com.praktika.checkservicehealth.dto.OutputDataDto;
import com.praktika.checkservicehealth.service.EndpointService;
import com.praktika.checkservicehealth.service.JwtTokenService;
import com.praktika.checkservicehealth.utils.WorkWithAuth;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndpointController {
    private final Logger LOGGER = LoggerFactory.getLogger(EndpointController.class);
    private final EndpointService endpointService;
    private final JwtTokenService jwtTokenService;

    @GetMapping("/check")
    public List<OutputDataDto> checkAllEndpoints() {
        List<OutputDataDto> list = endpointService.getSavedData();
        LOGGER.info("LIST: " + list.toString());

        String currentRole = WorkWithAuth.getCurrentRole(jwtTokenService);

        List<OutputDataDto> outputList = new ArrayList<>();
        String formattedRole = currentRole.split("_")[1];
        if (!formattedRole.equals("admin")) {
            for (OutputDataDto output : list) {
                if (output.getRole().equals(formattedRole)) {
                    outputList.add(output);
                }
            }
        } else {
            outputList = list;
        }

        return outputList;
    }

    @GetMapping("/checkByUrl")
    public String checkEndpointByUrl(@RequestParam String url) {
        endpointService.checkEndpointByUrl(url);
        return "redirect:/api/endpoints/check";
    }
}
