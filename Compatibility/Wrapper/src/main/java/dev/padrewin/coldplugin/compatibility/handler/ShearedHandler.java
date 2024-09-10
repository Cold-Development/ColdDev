package dev.padrewin.coldplugin.compatibility.handler;

import org.bukkit.entity.LivingEntity;

public interface ShearedHandler {

    boolean isSheared(LivingEntity shearable);

    void setSheared(LivingEntity shearable, boolean sheared);

}
