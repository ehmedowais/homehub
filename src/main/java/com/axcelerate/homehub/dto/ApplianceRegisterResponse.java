package com.axcelerate.homehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplianceRegisterResponse {
    private HttpStatus httpStatus;
    private String appliance;
    private String responseMessage;
}
