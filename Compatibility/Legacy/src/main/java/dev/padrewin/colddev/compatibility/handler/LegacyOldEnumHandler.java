package dev.padrewin.colddev.compatibility.handler;

import dev.padrewin.colddev.compatibility.wrapper.LegacyWrappedKeyed;
import dev.padrewin.colddev.compatibility.wrapper.WrappedKeyed;
import org.bukkit.entity.Villager;

public class LegacyOldEnumHandler implements OldEnumHandler {

    @Override
    public WrappedKeyed getProfession(Villager villager) {
        return new LegacyWrappedKeyed(villager.getProfession());
    }

}
