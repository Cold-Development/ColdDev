package dev.padrewin.coldplugin.manager;

import dev.padrewin.coldplugin.ColdPlugin;

public abstract class Manager {

    protected final ColdPlugin coldPlugin;

    public Manager(ColdPlugin coldPlugin) {
        this.coldPlugin = coldPlugin;
    }

    /**
     * Reloads the Manager's settings
     */
    public abstract void reload();

    /**
     * Cleans up the Manager's resources
     */
    public abstract void disable();

}