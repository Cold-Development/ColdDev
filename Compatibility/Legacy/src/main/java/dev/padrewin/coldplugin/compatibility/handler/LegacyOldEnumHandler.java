package dev.padrewin.coldplugin.compatibility.handler;

import dev.padrewin.coldplugin.compatibility.wrapper.LegacyWrappedKeyed;
import dev.padrewin.coldplugin.compatibility.wrapper.WrappedKeyed;
import org.bukkit.entity.Villager;

public class LegacyOldEnumHandler implements OldEnumHandler {

    @Override
    public WrappedKeyed getProfession(Villager villager) {
        return new LegacyWrappedKeyed(villager.getProfession());
    }

}
