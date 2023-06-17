package com.axcelerate.homehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteSlotBindingResponse {
    private HttpStatus bindingStatus;
    private String bindingResponse;
}
