package dev.padrewin.colddev.scheduler.task;

import org.bukkit.plugin.Plugin;

public interface ScheduledTask {

    /**
     * Cancels this task
     */
    void cancel();

    /**
     * @return true if this task is cancelled, false otherwise
     */
    boolean isCancelled();

    /**
     * @return the plugin that scheduled this task
     */
    Plugin getOwningPlugin();

    /**
     * @return true if this task is running, false otherwise
     */
    boolean isRunning();

    /**
     * @return true if this task is repeating, false otherwise
     */
    boolean isRepeating();

}