package dev.padrewin.colddev.compatibility.handler;

import dev.padrewin.colddev.compatibility.wrapper.WrappedKeyed;
import org.bukkit.entity.Villager;

public interface OldEnumHandler {

    WrappedKeyed getProfession(Villager villager);

}
