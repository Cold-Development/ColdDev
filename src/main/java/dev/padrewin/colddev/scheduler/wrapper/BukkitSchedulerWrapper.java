package dev.padrewin.colddev.scheduler.wrapper;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.scheduler.task.BukkitScheduledTask;
import dev.padrewin.colddev.scheduler.task.ScheduledTask;
import dev.padrewin.colddev.utils.ColdDevUtils;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class BukkitSchedulerWrapper implements SchedulerWrapper {

    private final ColdPlugin coldPlugin;
    private final BukkitScheduler scheduler;

    public BukkitSchedulerWrapper(ColdPlugin coldPlugin) {
        this.coldPlugin = coldPlugin;
        this.scheduler = Bukkit.getScheduler();
    }

    @Override
    public boolean isEntityThread(Entity entity) {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public boolean isLocationThread(Location location) {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public ScheduledTask runTask(Runnable runnable) {
        return wrap(this.scheduler.runTask(this.coldPlugin, runnable));
    }

    @Override
    public ScheduledTask runTaskAsync(Runnable runnable) {
        return wrap(this.scheduler.runTaskAsynchronously(this.coldPlugin, runnable));
    }

    @Override
    public ScheduledTask runTaskLater(Runnable runnable, long delay) {
        return wrap(this.scheduler.runTaskLater(this.coldPlugin, runnable, delay));
    }

    @Override
    public ScheduledTask runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit) {
        return wrap(this.scheduler.runTaskLater(this.coldPlugin, runnable, ColdDevUtils.timeUnitToTicks(delay, timeUnit)));
    }

    @Override
    public ScheduledTask runTaskLaterAsync(Runnable runnable, long delay) {
        return wrap(this.scheduler.runTaskLaterAsynchronously(this.coldPlugin, runnable, delay));
    }

    @Override
    public ScheduledTask runTaskLaterAsync(Runnable runnable, long delay, TimeUnit timeUnit) {
        return wrap(this.scheduler.runTaskLaterAsynchronously(this.coldPlugin, runnable, ColdDevUtils.timeUnitToTicks(delay, timeUnit)));
    }

    @Override
    public ScheduledTask runTaskTimer(Runnable runnable, long delay, long period) {
        return wrapRepeating(this.scheduler.runTaskTimer(this.coldPlugin, runnable, delay, period));
    }

    @Override
    public ScheduledTask runTaskTimer(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return wrapRepeating(this.scheduler.runTaskTimer(this.coldPlugin, runnable, ColdDevUtils.timeUnitToTicks(delay, timeUnit), ColdDevUtils.timeUnitToTicks(period, timeUnit)));
    }

    @Override
    public ScheduledTask runTaskTimerAsync(Runnable runnable, long delay, long period) {
        return wrapRepeating(this.scheduler.runTaskTimerAsynchronously(this.coldPlugin, runnable, delay, period));
    }

    @Override
    public ScheduledTask runTaskTimerAsync(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return wrapRepeating(this.scheduler.runTaskTimerAsynchronously(this.coldPlugin, runnable, ColdDevUtils.timeUnitToTicks(delay, timeUnit), ColdDevUtils.timeUnitToTicks(period, timeUnit)));
    }

    @Override
    public ScheduledTask runTaskAtLocation(Location location, Runnable runnable) {
        return this.runTask(runnable);
    }

    @Override
    public ScheduledTask runTaskAtLocationLater(Location location, Runnable runnable, long delay) {
        return this.runTaskLater(runnable, delay);
    }

    @Override
    public ScheduledTask runTaskAtLocationLater(Location location, Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.runTaskLater(runnable, delay, timeUnit);
    }

    @Override
    public ScheduledTask runTaskTimerAtLocation(Location location, Runnable runnable, long delay, long period) {
        return this.runTaskTimer(runnable, delay, period);
    }

    @Override
    public ScheduledTask runTaskTimerAtLocation(Location location, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return this.runTaskTimer(runnable, delay, period, timeUnit);
    }

    @Override
    public ScheduledTask runTaskAtEntity(Entity entity, Runnable runnable) {
        return this.runTask(runnable);
    }

    @Override
    public ScheduledTask runTaskAtEntityLater(Entity entity, Runnable runnable, long delay) {
        return this.runTaskLater(runnable, delay);
    }

    @Override
    public ScheduledTask runTaskAtEntityLater(Entity entity, Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.runTaskLater(runnable, delay, timeUnit);
    }

    @Override
    public ScheduledTask runTaskTimerAtEntity(Entity entity, Runnable runnable, long delay, long period) {
        return this.runTaskTimer(runnable, delay, period);
    }

    @Override
    public ScheduledTask runTaskTimerAtEntity(Entity entity, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return this.runTaskTimer(runnable, delay, period, timeUnit);
    }

    @Override
    public void cancelAllTasks() {
        this.scheduler.cancelTasks(this.coldPlugin);
    }

    private static ScheduledTask wrap(BukkitTask task) {
        return new BukkitScheduledTask(task, false);
    }

    private static ScheduledTask wrapRepeating(BukkitTask task) {
        return new BukkitScheduledTask(task, true);
    }

}