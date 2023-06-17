package com.axcelerate.homehub.controller;


import com.axcelerate.homehub.dto.ApplianceRegisterResponse;
import com.axcelerate.homehub.dto.RemoteOperationResponse;
import com.axcelerate.homehub.dto.RemoteSlotBindingResponse;

import com.axcelerate.homehub.service.HomeHubService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.SortedSet;
import java.util.TreeSet;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeHubController.class)
public class HomeHubControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeHubService service;

    @Test
    public void testBindRemoteSlotToAppliance_Success() throws Exception {
        // Mock the service response
        RemoteSlotBindingResponse response = new RemoteSlotBindingResponse(HttpStatus.CREATED, "Binding successful");
        Mockito.when(service.bindRemoteSlotToAppliance(Mockito.anyString(), Mockito.anyString())).thenReturn(response);

        // Perform the POST request and assert the response
        mockMvc.perform(post("/home-hub/remote/slot1/appliance/device1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bindingResponse").value("Binding successful"));
    }

    @Test
    public void testBindRemoteSlotToAppliance_BadRequest() throws Exception {
        String errorMessage = "Slot is not bound to any appliance. Please bind the slot first";
        RemoteSlotBindingResponse response = new RemoteSlotBindingResponse(HttpStatus.BAD_REQUEST, errorMessage);
        Mockito.when(service.bindRemoteSlotToAppliance(Mockito.anyString(), Mockito.anyString())).thenReturn(response);

        mockMvc.perform(post("/home-hub/remote/{slotId}/appliance/{applianceName}", "slot1", "appliance1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.bindingStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.bindingResponse").value(errorMessage));
    }

    @Test
    public void testOperateAppliance_Success() throws Exception {
        String slotId = "slot1";
        int operation = 1;
        RemoteOperationResponse response = new RemoteOperationResponse(HttpStatus.OK, "Appliance turned ON");
        Mockito.when(service.operateAppliance(slotId, operation)).thenReturn(response);

        mockMvc.perform(post("/home-hub/remote/{slotId}/{operation}", slotId, operation)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationStatus").value("OK"))
                .andExpect(jsonPath("$.operationResponse").value("Appliance turned ON"));
    }

    @Test
    public void testOperateAppliance_BadRequest() throws Exception {
        String slotId = "slot1";
        int operation = 2;
        RemoteOperationResponse response = new RemoteOperationResponse(HttpStatus.BAD_REQUEST, "Invalid operation");
        Mockito.when(service.operateAppliance(slotId, operation)).thenReturn(response);

        mockMvc.perform(post("/home-hub/remote/{slotId}/{operation}", slotId, operation)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.operationStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.operationResponse").value("Invalid operation"));
    }

    @Test
    public void testUndo_Success() throws Exception {
        RemoteOperationResponse response = new RemoteOperationResponse(HttpStatus.OK, "Previous operation undone");
        Mockito.when(service.undoOperation()).thenReturn(response);

        mockMvc.perform(post("/home-hub/remote/undo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationStatus").value("OK"))
                .andExpect(jsonPath("$.operationResponse").value("Previous operation undone"));
    }

    @Test
    public void testUndo_BadRequest() throws Exception {
        RemoteOperationResponse response = new RemoteOperationResponse(HttpStatus.BAD_REQUEST, "No previous action found");
        Mockito.when(service.undoOperation()).thenReturn(response);

        mockMvc.perform(post("/home-hub/remote/undo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.operationStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.operationResponse").value("No previous action found"));
    }

    @Test
    public void testRegisterAppliance_Success() throws Exception {
        String applianceName = "MyAppliance";
        ApplianceRegisterResponse response = new ApplianceRegisterResponse(HttpStatus.OK, applianceName, "Appliance registered successfully");
        Mockito.when(service.registerAppliance(applianceName)).thenReturn(response);

        mockMvc.perform(post("/home-hub/appliances/{applianceName}", applianceName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value("OK"))
                .andExpect(jsonPath("$.responseMessage").value("Appliance registered successfully"));
    }

    @Test
    public void testRegisterAppliance_BadRequest() throws Exception {
        String applianceName = "ExistingAppliance";

        ApplianceRegisterResponse response = new ApplianceRegisterResponse(HttpStatus.BAD_REQUEST, applianceName, "Appliance already registered");
        Mockito.when(service.registerAppliance(applianceName)).thenReturn(response);

        mockMvc.perform(post("/home-hub/appliances/{applianceName}", applianceName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.responseMessage").value("Appliance already registered"));
    }

    @Test
    public void testGetUsedSlots() throws Exception {

        // Define the sorted set to be returned
        SortedSet<String> sortedSet = new TreeSet<>();
        sortedSet.add("Slot1");
        sortedSet.add("Slot2");

        Mockito.when(service.listAllSlots()).thenReturn(sortedSet);

        // Perform the GET request and assert the response
        mockMvc.perform(MockMvcRequestBuilders.get("/home-hub/remote/slots"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Slot1,Slot2"));
    }

}
