package dev.padrewin.colddev.scheduler;

import org.bukkit.plugin.Plugin;
import dev.padrewin.colddev.scheduler.task.ScheduledTask;
import dev.padrewin.colddev.scheduler.wrapper.BukkitSchedulerWrapper;
import dev.padrewin.colddev.scheduler.wrapper.FoliaSchedulerWrapper;
import dev.padrewin.colddev.scheduler.wrapper.SchedulerWrapper;
import dev.padrewin.colddev.utils.NMSUtil;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class ColdScheduler implements SchedulerWrapper {

    private static ColdScheduler instance;

    private final AtomicInteger runningTasks;
    private final SchedulerWrapper scheduler;

    private ColdScheduler(Plugin coldPlugin) {
        if (instance != null)
            throw new IllegalStateException("An instance of ColdScheduler already exists");

        instance = this;
        this.runningTasks = new AtomicInteger();

        if (NMSUtil.isFolia()) {
            this.scheduler = new FoliaSchedulerWrapper(coldPlugin);
        } else {
            this.scheduler = new BukkitSchedulerWrapper(coldPlugin);
        }
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
    public ScheduledTask runTaskAtEntity(Entity entity, Runnable runnable, Runnable retired) {
        return this.scheduler.runTaskAtEntity(entity, this.wrap(runnable), retired == null ? null : this.wrap(retired));
    }

    @Override
    public ScheduledTask runTaskTimerAtEntity(Entity entity, Runnable runnable, Runnable retired, long delay, long period) {
        return this.scheduler.runTaskTimerAtEntity(entity, this.wrap(runnable), retired == null ? null : this.wrap(retired), delay, period);
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

    /**
     * Runs the task immediately if already on the main thread (Bukkit/Paper) or the global region thread (Folia),
     * otherwise schedules it there. Use it for work that is not tied to a place, such as console commands.
     *
     * @param runnable The task to run
     */
    public void executeGlobal(Runnable runnable) {
        boolean onGlobalThread = NMSUtil.isFolia() ? Bukkit.isGlobalTickThread() : Bukkit.isPrimaryThread();
        if (onGlobalThread) {
            runnable.run();
        } else {
            this.runTask(runnable);
        }
    }

    /**
     * Runs the task immediately if the current thread owns the entity, otherwise schedules it on the entity's thread.
     * On Bukkit/Paper this runs inline when called from the main thread; on Folia it runs on the entity's region thread.
     *
     * @param entity The entity whose thread should run the task
     * @param runnable The task to run
     */
    public void executeAtEntity(Entity entity, Runnable runnable) {
        if (this.isEntityThread(entity)) {
            runnable.run();
        } else {
            this.runTaskAtEntity(entity, runnable);
        }
    }

    /**
     * Runs the task immediately if the current thread owns the location, otherwise schedules it on the location's thread.
     * On Bukkit/Paper this runs inline when called from the main thread; on Folia it runs on the location's region thread.
     *
     * @param location The location whose thread should run the task
     * @param runnable The task to run
     */
    public void executeAtLocation(Location location, Runnable runnable) {
        if (this.isLocationThread(location)) {
            runnable.run();
        } else {
            this.runTaskAtLocation(location, runnable);
        }
    }

    /**
     * Teleports an entity in a way that works on both Folia and Bukkit/Paper.
     * Uses Paper's async teleport when available (required on Folia), otherwise a regular teleport.
     *
     * @param entity The entity to teleport
     * @param location The destination
     * @return A future completed with whether the teleport succeeded
     */
    public CompletableFuture<Boolean> teleport(Entity entity, Location location) {
        if (NMSUtil.hasAsyncTeleport())
            return entity.teleportAsync(location);
        return CompletableFuture.completedFuture(entity.teleport(location));
    }

    /**
     * Gets the scheduler for the given plugin. Works for any plugin, not only {@link dev.padrewin.colddev.ColdPlugin}s.
     *
     * @param coldPlugin The plugin that owns the scheduled tasks
     * @return The scheduler instance
     */
    public static ColdScheduler getInstance(Plugin coldPlugin) {
        if (instance == null)
            instance = new ColdScheduler(coldPlugin);
        return instance;
    }

}
