package dev.padrewin.coldplugin.compatibility.handler;

import dev.padrewin.coldplugin.compatibility.wrapper.WrappedKeyed;
import org.bukkit.entity.Villager;

public interface OldEnumHandler {

    WrappedKeyed getProfession(Villager villager);

}
