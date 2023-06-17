package com.axcelerate.homehub.repository;

import com.axcelerate.homehub.dto.Appliance;
import com.axcelerate.homehub.enums.ApplianceStatus;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class HomeHubRepository {
    private final Set<String> applianceRegistry = new HashSet<>();
    private final Map<String, Appliance> bindings = new HashMap<>();
    private static String lastOperatedSlot=null;
    public boolean isApplianceRegistered(String  applianceName) {
        return applianceRegistry.contains(applianceName);
    }
    public void registerAppliance(String applianceName) {
        applianceRegistry.add(applianceName);
    }

    public boolean isSlotAvailable(String slotId) {
        return !bindings.containsKey(slotId);
    }

    public boolean isApplianceAlreadyBound(String applianceName) {
        return bindings.containsValue(applianceName);

    }

    public void bindSlot(String slotId, String applianceName) {
        bindings.put(slotId, new Appliance(applianceName, ApplianceStatus.OFF));
    }
    public Appliance updateApplianceStatus(String slotId, int operation) {
        lastOperatedSlot = slotId;
        bindings.get(slotId).setStatus(ApplianceStatus.values()[operation]);
        return bindings.get(slotId);

    }

    public Set<String> getUsedSlots() {
        return bindings.keySet();
    }

    public Appliance  undoPreviousAction() {

        var lastOperatedAppliance = bindings.get(lastOperatedSlot);
        return updateApplianceStatus(lastOperatedSlot, lastOperatedAppliance.getStatus().ordinal() == 0 ? 1 : 0);
    }
    public static String getLastOperatedSlot() {
        return lastOperatedSlot;
    }
}
