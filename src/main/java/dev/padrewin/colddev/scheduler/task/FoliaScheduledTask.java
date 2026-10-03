package dev.padrewin.colddev.scheduler.task;

import org.bukkit.plugin.Plugin;

public class FoliaScheduledTask implements ScheduledTask {

    private final io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask;

    /**
     * @param foliaTask The Folia task, or null when Folia refused to schedule it because the entity was already removed
     */
    public FoliaScheduledTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask) {
        this.foliaTask = foliaTask;
    }

    @Override
    public void cancel() {
        if (this.foliaTask != null)
            this.foliaTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return this.foliaTask == null || this.foliaTask.isCancelled();
    }

    @Override
    public Plugin getOwningPlugin() {
        return this.foliaTask == null ? null : this.foliaTask.getOwningPlugin();
    }

    @Override
    public boolean isRunning() {
        if (this.foliaTask == null)
            return false;
        io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState state = this.foliaTask.getExecutionState();
        return state == io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState.RUNNING
                || state == io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState.CANCELLED_RUNNING;
    }

    @Override
    public boolean isRepeating() {
        return this.foliaTask != null && this.foliaTask.isRepeatingTask();
    }

}