package com.axcelerate.homehub.service;

import com.axcelerate.homehub.dto.ApplianceRegisterResponse;
import com.axcelerate.homehub.dto.RemoteOperationResponse;
import com.axcelerate.homehub.dto.RemoteSlotBindingResponse;
import com.axcelerate.homehub.enums.ApplianceStatus;
import com.axcelerate.homehub.exception.ApplianceAlreadyRegisteredException;
import com.axcelerate.homehub.exception.ApplianceNotRegisteredException;
import com.axcelerate.homehub.exception.BindException;
import com.axcelerate.homehub.exception.NoLastOperationException;
import com.axcelerate.homehub.repository.HomeHubRepository;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HomeHubService {
    private final MessageSource messageSource;

    private final HomeHubRepository repository;

    public HomeHubService(MessageSource messageSource, HomeHubRepository repository) {
        this.messageSource = messageSource;
        this.repository = repository;
    }

    public RemoteSlotBindingResponse bindRemoteSlotToAppliance(String slotId, String applianceName) {
        if(!repository.isApplianceRegistered(applianceName)) {
            var msg = messageSource.getMessage("appliance_not_registered.message", new Object[]{applianceName}, Locale.US);
            throw new ApplianceNotRegisteredException(HttpStatus.BAD_REQUEST, msg);
        }
        if(repository.isApplianceAlreadyBound(applianceName)) {
            var msg = messageSource.getMessage("appliance_already_bound.message", new Object[]{applianceName}, Locale.US);
            throw new BindException(HttpStatus.BAD_REQUEST, msg);

        }
        if(!repository.isSlotAvailable(slotId)) {
            var msg = messageSource.getMessage("slot_already_used.message", new Object[]{slotId}, Locale.US);
            throw new BindException(HttpStatus.BAD_REQUEST, msg);
        }
        repository.bindSlot(slotId, applianceName);
        var msg = messageSource.getMessage("binding_successful.message", new Object[]{slotId, applianceName}, Locale.US);
        return new RemoteSlotBindingResponse(HttpStatus.CREATED, msg);
    }
    public ApplianceRegisterResponse registerAppliance(String applianceName) {
        if (!repository.isApplianceRegistered(applianceName)) {
            repository.registerAppliance(applianceName);
            var msg = messageSource.getMessage("appliance_successfully_registered.message", new Object[]{applianceName}, Locale.US);
            return new ApplianceRegisterResponse(HttpStatus.CREATED, applianceName, msg);
        } else {
            var msg = messageSource.getMessage("appliance_already_registered.message", new Object[]{applianceName}, Locale.US);
            throw new ApplianceAlreadyRegisteredException(HttpStatus.BAD_REQUEST, msg);
        }

    }

    public RemoteOperationResponse operateAppliance(String slotId, int operation) {
        if(operation < 0 || operation > 1) {
            var msg = messageSource.getMessage("appliance_operation_not_allowed.message", new Object[]{slotId}, Locale.US);
            throw new BindException(HttpStatus.BAD_REQUEST, msg);
        }
        if(repository.isSlotAvailable(slotId)) {
            var msg = messageSource.getMessage("slot_not_bound.message", new Object[]{slotId}, Locale.US);
            throw new BindException(HttpStatus.BAD_REQUEST, msg);
        }
        var appliance = repository.updateApplianceStatus(slotId, operation);
        var op = ApplianceStatus.values()[operation].name();
        var msg = messageSource.getMessage("appliance_operation_successful.message", new Object[]{appliance.getName(),op}, Locale.US);
        return new RemoteOperationResponse(HttpStatus.OK, msg);
    }

    public Set<String> listAllSlots() {
        return repository.getUsedSlots();
    }
    public RemoteOperationResponse undoOperation() {
        if(HomeHubRepository.getLastOperatedSlot() == null) {
            var msg = messageSource.getMessage("no_action_to_undo.message", new Object[]{}, Locale.US);
            throw new NoLastOperationException(HttpStatus.BAD_REQUEST, msg);
        }
        var unDoneAppliance = repository.undoPreviousAction();
        var msg = messageSource.getMessage("appliance_operation_successful.message", new Object[]{unDoneAppliance.getName(),
                    ApplianceStatus.values()[unDoneAppliance.getStatus().ordinal()].name()}, Locale.US);
        return new RemoteOperationResponse(HttpStatus.OK, msg);
    }
}
