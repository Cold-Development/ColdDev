package dev.padrewin.colddev.compatibility.handler;

import dev.padrewin.colddev.compatibility.wrapper.CurrentWrappedKeyed;
import dev.padrewin.colddev.compatibility.wrapper.WrappedKeyed;
import org.bukkit.entity.Villager;

public class CurrentOldEnumHandler implements OldEnumHandler {

    @Override
    public WrappedKeyed getProfession(Villager villager) {
        return new CurrentWrappedKeyed(villager.getProfession());
    }

}
