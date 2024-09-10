package dev.padrewin.coldplugin.scheduler;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.scheduler.task.ScheduledTask;
import dev.padrewin.coldplugin.scheduler.wrapper.BukkitSchedulerWrapper;
import dev.padrewin.coldplugin.scheduler.wrapper.SchedulerWrapper;
import dev.padrewin.coldplugin.utils.NMSUtil;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class ColdScheduler implements SchedulerWrapper {

    private static ColdScheduler instance;

    private final AtomicInteger runningTasks;
    private final SchedulerWrapper scheduler;

    private ColdScheduler(ColdPlugin ColdPlugin) {
        if (instance != null)
            throw new IllegalStateException("An instance of ColdScheduler already exists");

        instance = this;
        this.runningTasks = new AtomicInteger();

        // Folosim doar BukkitSchedulerWrapper, fără verificare pentru Folia
        this.scheduler = new BukkitSchedulerWrapper(ColdPlugin);
    }


    @Override
    public boolean isEntityThread(Entity entity) {
        return this.scheduler.isEntityThread(entity);
    }

    @Override
    public boolean isLocationThread(Location location) {
        return this.scheduler.isLocationThread(location);
    }

    @Override
    public ScheduledTask runTask(Runnable runnable) {
        return this.scheduler.runTask(this.wrap(runnable));
    }

    @Override
    public ScheduledTask runTaskAsync(Runnable runnable) {
        return this.scheduler.runTaskAsync(this.wrap(runnable));
    }

    @Override
    public ScheduledTask runTaskLater(Runnable runnable, long delay) {
        return this.scheduler.runTaskLater(this.wrap(runnable), delay);
    }

    @Override
    public ScheduledTask runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.scheduler.runTaskLater(this.wrap(runnable), delay, timeUnit);
    }

    @Override
    public ScheduledTask runTaskLaterAsync(Runnable runnable, long delay) {
        return this.scheduler.runTaskLaterAsync(this.wrap(runnable), delay);
    }

    @Override
    public ScheduledTask runTaskLaterAsync(Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.scheduler.runTaskLaterAsync(this.wrap(runnable), delay, timeUnit);
    }

    @Override
    public ScheduledTask runTaskTimer(Runnable runnable, long delay, long period) {
        return this.scheduler.runTaskTimer(this.wrap(runnable), delay, period);
    }

    @Override
    public ScheduledTask runTaskTimer(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return this.scheduler.runTaskTimer(this.wrap(runnable), delay, period, timeUnit);
    }

    @Override
    public ScheduledTask runTaskTimerAsync(Runnable runnable, long delay, long period) {
        return this.scheduler.runTaskTimerAsync(this.wrap(runnable), delay, period);
    }

    @Override
    public ScheduledTask runTaskTimerAsync(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return this.scheduler.runTaskTimerAsync(this.wrap(runnable), delay, period, timeUnit);
    }

    @Override
    public ScheduledTask runTaskAtLocation(Location location, Runnable runnable) {
        return this.scheduler.runTaskAtLocation(location, this.wrap(runnable));
    }

    @Override
    public ScheduledTask runTaskAtLocationLater(Location location, Runnable runnable, long delay) {
        return this.scheduler.runTaskAtLocationLater(location, this.wrap(runnable), delay);
    }

    @Override
    public ScheduledTask runTaskAtLocationLater(Location location, Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.scheduler.runTaskAtLocationLater(location, this.wrap(runnable), delay, timeUnit);
    }

    @Override
    public ScheduledTask runTaskTimerAtLocation(Location location, Runnable runnable, long delay, long period) {
        return this.scheduler.runTaskTimerAtLocation(location, this.wrap(runnable), delay, period);
    }

    @Override
    public ScheduledTask runTaskTimerAtLocation(Location location, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return this.scheduler.runTaskTimerAtLocation(location, this.wrap(runnable), delay, period, timeUnit);
    }

    @Override
    public ScheduledTask runTaskAtEntity(Entity entity, Runnable runnable) {
        return this.scheduler.runTaskAtEntity(entity, this.wrap(runnable));
    }

    @Override
    public ScheduledTask runTaskAtEntityLater(Entity entity, Runnable runnable, long delay) {
        return this.scheduler.runTaskAtEntityLater(entity, this.wrap(runnable), delay);
    }

    @Override
    public ScheduledTask runTaskAtEntityLater(Entity entity, Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.scheduler.runTaskAtEntityLater(entity, this.wrap(runnable), delay, timeUnit);
    }

    @Override
    public ScheduledTask runTaskTimerAtEntity(Entity entity, Runnable runnable, long delay, long period) {
        return this.scheduler.runTaskTimerAtEntity(entity, this.wrap(runnable), delay, period);
    }

    @Override
    public ScheduledTask runTaskTimerAtEntity(Entity entity, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return this.scheduler.runTaskTimerAtEntity(entity, this.wrap(runnable), delay, period, timeUnit);
    }

    @Override
    public void cancelAllTasks() {
        this.scheduler.cancelAllTasks();
    }

    public int getRunningTaskCount() {
        return this.runningTasks.get();
    }

    private Runnable wrap(Runnable runnable) {
        return () -> {
            this.runningTasks.incrementAndGet();
            try {
                runnable.run();
            } finally {
                this.runningTasks.decrementAndGet();
            }
        };
    }

    public static ColdScheduler getInstance(ColdPlugin ColdPlugin) {
        if (instance == null)
            instance = new ColdScheduler(ColdPlugin);
        return instance;
    }

}