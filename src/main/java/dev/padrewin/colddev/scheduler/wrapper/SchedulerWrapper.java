package dev.padrewin.colddev.scheduler.wrapper;

import dev.padrewin.colddev.scheduler.task.ScheduledTask;
import java.util.concurrent.TimeUnit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface SchedulerWrapper {

    //#region Thread Checks

    /**
     * Checks if the current thread is ticking the given entity.
     *
     * @param entity The entity to check.
     * @return true if the current thread is ticking the given entity, false otherwise.
     */
    boolean isEntityThread(Entity entity);

    /**
     * Checks if the current thread is ticking at the given location.
     *
     * @param location The location to check.
     * @return true if the current thread is ticking at the given location, false otherwise.
     */
    boolean isLocationThread(Location location);

    //#endregion

    //#region Global

    /**
     * Runs the task in the next tick.
     *
     * @param runnable The task to run.
     * @return The task created.
     */
    ScheduledTask runTask(Runnable runnable);

    /**
     * Runs the task asynchronously.
     *
     * @param runnable The task to run.
     * @return The task created.
     */
    ScheduledTask runTaskAsync(Runnable runnable);

    ScheduledTask runTaskLater(Runnable runnable, long delay);
    ScheduledTask runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit);

    ScheduledTask runTaskLaterAsync(Runnable runnable, long delay);
    ScheduledTask runTaskLaterAsync(Runnable runnable, long delay, TimeUnit timeUnit);

    ScheduledTask runTaskTimer(Runnable runnable, long delay, long period);
    ScheduledTask runTaskTimer(Runnable runnable, long delay, long period, TimeUnit timeUnit);

    ScheduledTask runTaskTimerAsync(Runnable runnable, long delay, long period);
    ScheduledTask runTaskTimerAsync(Runnable runnable, long delay, long period, TimeUnit timeUnit);

    //#endregion

    //#region Location

    ScheduledTask runTaskAtLocation(Location location, Runnable runnable);

    ScheduledTask runTaskAtLocationLater(Location location, Runnable runnable, long delay);
    ScheduledTask runTaskAtLocationLater(Location location, Runnable runnable, long delay, TimeUnit timeUnit);

    ScheduledTask runTaskTimerAtLocation(Location location, Runnable runnable, long delay, long period);
    ScheduledTask runTaskTimerAtLocation(Location location, Runnable runnable, long delay, long period, TimeUnit timeUnit);

    //#endregion

    //#region Entity

    ScheduledTask runTaskAtEntity(Entity entity, Runnable runnable);

    ScheduledTask runTaskAtEntityLater(Entity entity, Runnable runnable, long delay);
    ScheduledTask runTaskAtEntityLater(Entity entity, Runnable runnable, long delay, TimeUnit timeUnit);

    ScheduledTask runTaskTimerAtEntity(Entity entity, Runnable runnable, long delay, long period);
    ScheduledTask runTaskTimerAtEntity(Entity entity, Runnable runnable, long delay, long period, TimeUnit timeUnit);

    /**
     * Same as {@link #runTaskAtEntity(Entity, Runnable)}, but runs {@code retired} if the entity is removed
     * before the task could run (Folia drops entity tasks silently when the entity leaves, e.g. a player quits).
     * On Bukkit/Paper the task always runs, so {@code retired} is never called.
     */
    ScheduledTask runTaskAtEntity(Entity entity, Runnable runnable, Runnable retired);

    /**
     * Same as {@link #runTaskTimerAtEntity(Entity, Runnable, long, long)}, but runs {@code retired} once if the
     * entity is removed while the timer is still active (Folia stops entity tasks silently in that case).
     * On Bukkit/Paper the timer keeps running, so {@code retired} is never called.
     */
    ScheduledTask runTaskTimerAtEntity(Entity entity, Runnable runnable, Runnable retired, long delay, long period);

    //#endregion

    void cancelAllTasks();

}