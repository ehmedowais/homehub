package com.axcelerate.homehub.service;

import static org.junit.jupiter.api.Assertions.*;

import com.axcelerate.homehub.dto.Appliance;
import com.axcelerate.homehub.dto.ApplianceRegisterResponse;
import com.axcelerate.homehub.dto.RemoteOperationResponse;
import com.axcelerate.homehub.dto.RemoteSlotBindingResponse;
import com.axcelerate.homehub.enums.ApplianceStatus;
import com.axcelerate.homehub.exception.*;
import com.axcelerate.homehub.repository.HomeHubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import java.util.Locale;


import static org.mockito.Mockito.*;



class HomeHubServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private HomeHubRepository repository;

    @InjectMocks
    private HomeHubService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    void testBindRemoteSlotToAppliance_WithValidData() {
        String slotId = "slot1";
        String applianceName = "appliance1";

        when(repository.isApplianceRegistered(applianceName)).thenReturn(true);
        when(repository.isApplianceAlreadyBound(applianceName)).thenReturn(false);
        when(repository.isSlotAvailable(slotId)).thenReturn(true);

        String successMessage = "Binding successful.";
        when(messageSource.getMessage(eq("binding_successful.message"), any(), eq(Locale.US))).thenReturn(successMessage);

        RemoteSlotBindingResponse response = service.bindRemoteSlotToAppliance(slotId, applianceName);

        assertEquals(HttpStatus.CREATED, response.getBindingStatus());
        assertEquals(successMessage, response.getBindingResponse());
        verify(repository, times(1)).bindSlot(slotId, applianceName);
    }

    @Test
    void testBindRemoteSlotToAppliance_WithNotRegisteredAppliance() {
        String slotId = "slot1";
        String applianceName = "appliance1";

        when(repository.isApplianceRegistered(applianceName)).thenReturn(false);

        String errorMessage = "Appliance not registered.";
        when(messageSource.getMessage(eq("appliance_not_registered.message"), any(), eq(Locale.US))).thenReturn(errorMessage);

        assertThrows(ApplianceNotRegisteredException.class, () -> service.bindRemoteSlotToAppliance(slotId, applianceName));
        verify(repository, never()).bindSlot(slotId, applianceName);
    }

    @Test
    void testBindRemoteSlotToAppliance_WithAlreadyBoundAppliance() {
        String slotId = "slot1";
        String applianceName = "appliance1";

        when(repository.isApplianceRegistered(applianceName)).thenReturn(true);
        when(repository.isApplianceAlreadyBound(applianceName)).thenReturn(true);

        String errorMessage = "Appliance already bound.";
        when(messageSource.getMessage(eq("appliance_already_bound.message"), any(), eq(Locale.US))).thenReturn(errorMessage);

        assertThrows(BindException.class, () -> service.bindRemoteSlotToAppliance(slotId, applianceName));
        verify(repository, never()).bindSlot(slotId, applianceName);
    }

    @Test
    void testBindRemoteSlotToAppliance_WithSlotNotAvailable() {
        String slotId = "slot1";
        String applianceName = "appliance1";

        when(repository.isApplianceRegistered(applianceName)).thenReturn(true);
        when(repository.isApplianceAlreadyBound(applianceName)).thenReturn(false);
        when(repository.isSlotAvailable(slotId)).thenReturn(false);

        String errorMessage = "Slot already used.";
        when(messageSource.getMessage(eq("slot_already_used.message"), any(), eq(Locale.US))).thenReturn(errorMessage);

        assertThrows(BindException.class, () -> service.bindRemoteSlotToAppliance(slotId, applianceName));
        verify(repository, never()).bindSlot(slotId, applianceName);
    }

    @Test
    public void testRegisterAppliance_Success() {
        String applianceName = "MyAppliance";
        when(repository.isApplianceRegistered(applianceName)).thenReturn(false);
        when(messageSource.getMessage("appliance_successfully_registered.message", new Object[]{applianceName}, Locale.US))
                .thenReturn("Appliance successfully registered");

        ApplianceRegisterResponse response = service.registerAppliance(applianceName);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        assertEquals(applianceName, response.getAppliance());
        assertEquals("Appliance successfully registered", response.getResponseMessage());

        verify(repository).registerAppliance(applianceName);
        verify(messageSource).getMessage("appliance_successfully_registered.message", new Object[]{applianceName}, Locale.US);
    }

    @Test
    public void testRegisterAppliance_AlreadyRegistered() {
        String applianceName = "MyAppliance";
        when(repository.isApplianceRegistered(applianceName)).thenReturn(true);
        when(messageSource.getMessage("appliance_already_registered.message", new Object[]{applianceName}, Locale.US))
                .thenReturn("Appliance already registered");

        ApplianceAlreadyRegisteredException exception = assertThrows(ApplianceAlreadyRegisteredException.class,
                () -> service.registerAppliance(applianceName));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Appliance already registered", exception.getMessage());

        verify(repository, never()).registerAppliance(any());
        verify(messageSource).getMessage("appliance_already_registered.message", new Object[]{applianceName}, Locale.US);
    }
    @Test
    public void testOperateAppliance_Success() {
        String slotId = "123";
        int operation = 1;
        String applianceName = "MyAppliance";
        ApplianceStatus applianceStatus = ApplianceStatus.ON;

       when(repository.isSlotAvailable(slotId)).thenReturn(false);
       when(repository.updateApplianceStatus(slotId, operation)).thenReturn(new Appliance(applianceName, applianceStatus));
       when(messageSource.getMessage("appliance_operation_successful.message",
                        new Object[]{applianceName, applianceStatus.name()}, Locale.US))
                .thenReturn("Appliance operation successful");

        RemoteOperationResponse response = service.operateAppliance(slotId, operation);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getOperationStatus());
        assertEquals("Appliance operation successful", response.getOperationResponse());

       verify(repository).isSlotAvailable(slotId);
       verify(repository).updateApplianceStatus(slotId, operation);
       verify(messageSource).getMessage("appliance_operation_successful.message",
                new Object[]{applianceName, applianceStatus.name()}, Locale.US);
    }

    @Test
    public void testOperateAppliance_InvalidOperation() {
        String slotId = "123";
        int operation = 2;

       when(messageSource.getMessage("appliance_operation_not_allowed.message",
                        new Object[]{slotId}, Locale.US))
                .thenReturn("Invalid appliance operation");

        BindException exception = assertThrows(BindException.class,
                () -> service.operateAppliance(slotId, operation));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid appliance operation", exception.getMessage());

       verify(repository,never()).isSlotAvailable(any());
       verify(repository,never()).updateApplianceStatus(any(), anyInt());
       verify(messageSource).getMessage("appliance_operation_not_allowed.message",
                new Object[]{slotId}, Locale.US);
    }

    @Test
    public void testOperateAppliance_SlotNotBound() {
        String slotId = "123";
        int operation = 1;

       when(repository.isSlotAvailable(slotId)).thenReturn(true);
       when(messageSource.getMessage("slot_not_bound.message",
                        new Object[]{slotId}, Locale.US))
                .thenReturn("Slot not bound");

        BindException exception = assertThrows(BindException.class,
                () -> service.operateAppliance(slotId, operation));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Slot not bound", exception.getMessage());

       verify(repository).isSlotAvailable(slotId);
       verify(repository,never()).updateApplianceStatus(any(), anyInt());
       verify(messageSource).getMessage("slot_not_bound.message",
                new Object[]{slotId}, Locale.US);
    }
    @Test
    public void testUndoOperation_Success() throws Exception {
        String applianceName = "MyAppliance";
        ApplianceStatus applianceStatus = ApplianceStatus.ON;

        var mockedStatic = mockStatic(HomeHubRepository.class);
        when(HomeHubRepository.getLastOperatedSlot()).thenReturn("123");
        when(repository.undoPreviousAction()).thenReturn(new Appliance(applianceName, applianceStatus));
        when(messageSource.getMessage("appliance_operation_successful.message",
                        new Object[]{applianceName, applianceStatus.name()}, Locale.US))
                .thenReturn("Appliance operation successful");

        RemoteOperationResponse response = service.undoOperation();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getOperationStatus());
        assertEquals("Appliance operation successful", response.getOperationResponse());

        verify(repository).undoPreviousAction();
        verify(messageSource).getMessage("appliance_operation_successful.message",
                new Object[]{applianceName, applianceStatus.name()}, Locale.US);

        verify(HomeHubRepository.class);
        HomeHubRepository.getLastOperatedSlot();
        mockedStatic.close();

    }
    @Test
    public void testUndoOperation_NoLastOperation() {
        var mockedStatic = mockStatic(HomeHubRepository.class);
        when(HomeHubRepository.getLastOperatedSlot()).thenReturn(null);
        when(messageSource.getMessage("no_action_to_undo.message",
                        new Object[]{}, Locale.US))
                .thenReturn("No previous action to undo");

        NoLastOperationException exception = assertThrows(NoLastOperationException.class,
                () -> service.undoOperation());

        assertEquals(HttpStatus.BAD_REQUEST, exception.getOperationStatus());
        assertEquals("No previous action to undo", exception.getMessage());

        //verify(HomeHubRepository).getLastOperatedSlot();
        verify(repository, never()).undoPreviousAction();
        verify(messageSource).getMessage("no_action_to_undo.message",
                new Object[]{}, Locale.US);
        mockedStatic.close();
    }

}
