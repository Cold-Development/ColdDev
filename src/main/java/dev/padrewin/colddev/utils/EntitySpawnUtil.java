package dev.padrewin.colddev.utils;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/commits/1961ead6ff7">
 *   Required to be able to compile against Spigot 1.20.2+ and still maintain compatibility with lower versions.
 * </a>
 */
@SuppressWarnings({"deprecation", "unchecked"})
public final class EntitySpawnUtil {

    private static Method method;
    static {
        try {
            method = World.class.getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
        } catch (ReflectiveOperationException ignored) { }
    }

    private EntitySpawnUtil() {

    }

    public static <T extends Entity> T spawn(Location location, Class<T> clazz, org.bukkit.util.Consumer<T> function) {
        World world = location.getWorld();
        if (method == null)
            return world.spawn(location, clazz, function);

        try {
            return (T) method.invoke(world, location, clazz, function);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}