package dev.padrewin.colddev.manager;

import dev.padrewin.colddev.ColdPlugin;

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