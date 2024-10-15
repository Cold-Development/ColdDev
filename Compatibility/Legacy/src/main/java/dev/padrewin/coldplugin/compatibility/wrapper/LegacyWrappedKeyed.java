package dev.padrewin.colddev.compatibility.wrapper;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

public class LegacyWrappedKeyed implements WrappedKeyed {

    private final Keyed keyed;

    public <T extends Keyed> LegacyWrappedKeyed(T keyed) {
        this.keyed = keyed;
    }

    @Override
    public NamespacedKey getKey() {
        return this.keyed.getKey();
    }

}
