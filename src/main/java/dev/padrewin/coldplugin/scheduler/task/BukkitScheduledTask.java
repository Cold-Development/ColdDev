package dev.padrewin.coldplugin.scheduler.task;

import dev.padrewin.coldplugin.ColdPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class BukkitScheduledTask implements ScheduledTask {

    private final BukkitTask bukkitTask;
    private final boolean repeating;

    public BukkitScheduledTask(BukkitTask bukkitTask, boolean repeating) {
        this.bukkitTask = bukkitTask;
        this.repeating = repeating;
    }

    @Override
    public void cancel() {
        this.bukkitTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return this.bukkitTask.isCancelled();
    }

    @Override
    public ColdPlugin getOwningPlugin() {
        return (ColdPlugin) this.bukkitTask.getOwner();
    }

    @Override
    public boolean isRunning() {
        return Bukkit.getScheduler().isCurrentlyRunning(this.bukkitTask.getTaskId());
    }

    @Override
    public boolean isRepeating() {
        return this.repeating;
    }

}