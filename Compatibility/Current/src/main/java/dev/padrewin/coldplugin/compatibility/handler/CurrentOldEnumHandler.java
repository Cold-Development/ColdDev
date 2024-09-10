package dev.padrewin.coldplugin.compatibility.handler;

import dev.padrewin.coldplugin.compatibility.wrapper.CurrentWrappedKeyed;
import dev.padrewin.coldplugin.compatibility.wrapper.WrappedKeyed;
import org.bukkit.entity.Villager;

public class CurrentOldEnumHandler implements OldEnumHandler {

    @Override
    public WrappedKeyed getProfession(Villager villager) {
        return new CurrentWrappedKeyed(villager.getProfession());
    }

}
