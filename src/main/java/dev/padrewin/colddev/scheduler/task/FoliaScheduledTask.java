package dev.padrewin.colddev.scheduler.task;

import dev.padrewin.colddev.ColdPlugin;

public class FoliaScheduledTask implements ScheduledTask {

    private final io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask;

    public FoliaScheduledTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask) {
        this.foliaTask = foliaTask;
    }

    @Override
    public void cancel() {
        this.foliaTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return this.foliaTask.isCancelled();
    }

    @Override
    public ColdPlugin getOwningPlugin() {
        return (ColdPlugin) this.foliaTask.getOwningPlugin();
    }

    @Override
    public boolean isRunning() {
        io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState state = this.foliaTask.getExecutionState();
        return state == io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState.RUNNING
                || state == io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState.CANCELLED_RUNNING;
    }

    @Override
    public boolean isRepeating() {
        return this.foliaTask.isRepeatingTask();
    }

}