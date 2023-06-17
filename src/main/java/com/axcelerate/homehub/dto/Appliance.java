package com.axcelerate.homehub.dto;

import com.axcelerate.homehub.enums.ApplianceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appliance {
    String name;
    ApplianceStatus status;

}
