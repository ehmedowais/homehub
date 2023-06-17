package com.axcelerate.homehub.controller;

import com.axcelerate.homehub.dto.ApplianceRegisterResponse;
import com.axcelerate.homehub.dto.RemoteOperationResponse;
import com.axcelerate.homehub.dto.RemoteSlotBindingResponse;
import com.axcelerate.homehub.service.HomeHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/home-hub")
public class HomeHubController {
    @Autowired
    HomeHubService service;

    @GetMapping("remote/slots")
    @Operation(summary = "This endpoint is used to list bound slots of a remote")

    public ResponseEntity<String> getUsedSlots() {
        return new ResponseEntity<>(service.listAllSlots().stream().collect(Collectors.joining(",")), HttpStatus.OK);
    }

    @PostMapping("/remote/{slotId}/appliance/{applianceName}")
    @Operation(summary = "This endpoint is used to bind an appliance with a remote slot. Be noted prior to binding the appliance please register it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appliance turned ON/OFF",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RemoteSlotBindingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Slot is not bound to any appliance. Please bind the slot first",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RemoteSlotBindingResponse.class)))

    })
    public ResponseEntity<RemoteSlotBindingResponse> bindRemoteSlotToAppliance(
            @PathVariable("slotId") @NotBlank() @Size(min = 1, message = "To bind appliance with remote slot the slotID is required with at least 1 character") String slotId,
            @PathVariable("applianceName") @NotBlank() @Size(min = 1, message = "To bind appliance with remote slot the appliance name is required with at least 1 character") String applianceName
    ) {
        var response = service.bindRemoteSlotToAppliance(slotId, applianceName);
        return new ResponseEntity<>(response, response.getBindingStatus());
    }

    @PostMapping("/remote/{slotId}/{operation}")
    @Operation(summary = "This endpoint is used to operate an appliance. SlotId is slot from remote and operation is 0/1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appliance turned ON/OFF",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RemoteOperationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Slot is not bound to any appliance. Please bind the slot first",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RemoteOperationResponse.class)))

    })
    public ResponseEntity<RemoteOperationResponse> operateAppliance(@PathVariable("slotId")  String slotId,
                                                   @PathVariable("operation") @Min(0) @Max(1)  int operation) {
        var response = service.operateAppliance(slotId, operation);
        return new ResponseEntity<>(response, response.getOperationStatus());
    }

    @PostMapping("/remote/undo")
    @Operation(summary = "This endpoint is used to undo a previous operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appliance turned ON/OFF",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RemoteOperationResponse.class))),
            @ApiResponse(responseCode = "400", description = "No previous action found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RemoteOperationResponse.class)))

    })
    public ResponseEntity<RemoteOperationResponse> undo() {
        var response = service.undoOperation();
        return new ResponseEntity<>(response, response.getOperationStatus());
    }


    @PostMapping("/appliances/{applianceName}")
    @Operation(summary = "This endpoint is used to register a device/appliance with Home Hub server. Once a device is registered we can\n" +
            " Bind the device with a slot from remote and use it")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Appliance has been registered with Home Hub please bind remote slot",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplianceRegisterResponse.class))),
    @ApiResponse(responseCode = "400", description = "Appliance already registered with Home Hub please use a difference name",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplianceRegisterResponse.class)))

    })
    public ResponseEntity<ApplianceRegisterResponse> registerAppliance(@PathVariable("applianceName") String applianceName) {
        var response = service.registerAppliance(applianceName);
        return new ResponseEntity(response, response.getHttpStatus());
    }
}
