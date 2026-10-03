package dev.padrewin.colddev.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class NMSUtil {

    private static final int VERSION_NUMBER;
    private static final int MINOR_VERSION_NUMBER;
    private static final boolean IS_PAPER;
    private static final boolean IS_FOLIA;
    private static final boolean HAS_ASYNC_TELEPORT;
    static {
        String bukkitVersion = Bukkit.getBukkitVersion();
        String[] parts = bukkitVersion.split("-")[0].split("\\.");
        // Old format: 1.21.4 -> parts[0]="1", parts[1]="21", parts[2]="4"
        // New format: 26.2.build.65 -> parts[0]="26", parts[1]="2", parts[2]="build"
        boolean oldFormat = parts[0].equals("1");
        VERSION_NUMBER = Integer.parseInt(oldFormat ? parts[1] : parts[0]);
        int minorIndex = oldFormat ? 2 : 1;
        MINOR_VERSION_NUMBER = parts.length > minorIndex && parts[minorIndex].matches("\\d+") ? Integer.parseInt(parts[minorIndex]) : 0;
        // PaperConfig was removed in 1.19, newer Paper builds only ship the new configuration class
        IS_PAPER = ClassUtils.checkClass("com.destroystokyo.paper.PaperConfig") || ClassUtils.checkClass("io.papermc.paper.configuration.Configuration");
        boolean asyncTeleport;
        try {
            Entity.class.getMethod("teleportAsync", Location.class);
            asyncTeleport = true;
        } catch (NoSuchMethodException e) {
            asyncTeleport = false;
        }
        HAS_ASYNC_TELEPORT = asyncTeleport;
        IS_FOLIA = ClassUtils.checkClass("io.papermc.paper.threadedregions.RegionizedServer");
    }

    private NMSUtil() {

    }

    /**
     * @return the server version major release number
     */
    public static int getVersionNumber() {
        return VERSION_NUMBER;
    }

    /**
     * @return the server version minor release number
     */
    public static int getMinorVersionNumber() {
        return MINOR_VERSION_NUMBER;
    }

    /**
     * @return true if the server is running Paper or a fork of Paper, false otherwise
     */
    public static boolean isPaper() {
        return IS_PAPER;
    }

    /**
     * @return true if the server is running Folia, false otherwise
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * @return true if the server supports Paper's Entity#teleportAsync, false otherwise
     */
    public static boolean hasAsyncTeleport() {
        return HAS_ASYNC_TELEPORT;
    }

}