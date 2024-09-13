package dev.padrewin.coldplugin.manager;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.config.CommentedFileConfiguration;
import dev.padrewin.coldplugin.utils.NMSUtil;
import dev.padrewin.coldplugin.utils.ColdDevUtils;
import dev.padrewin.coldplugin.utils.StringPlaceholders;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PluginUpdateManager extends Manager implements Listener {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_PURPLE_CHINESE = "\u001B[38;5;93m";

    private static final String[] SNAPSHOT_HEADER = {
            "================================================",
            " You are currently running a DEVELOPMENT BUILD!",
            " These types of builds are not meant to be run",
            " on a production server, and are not supported.",
            "================================================"
    };

    private boolean displayedSnapshotHeader;
    private String updateVersion;

    public PluginUpdateManager(ColdPlugin coldPlugin) {
        super(coldPlugin);
        Bukkit.getPluginManager().registerEvents(this, this.coldPlugin);
    }

    @Override
    public void reload() {
        if (this.coldPlugin.getSpigotId() == -1 || this.updateVersion != null)
            return;

        File configFile = new File(this.coldPlugin.getColdDevDataFolder(), "config.yml");

        String currentVersion = this.coldPlugin.getDescription().getVersion();
        if (currentVersion.contains("-SNAPSHOT") && !this.displayedSnapshotHeader) {
            for (String line : SNAPSHOT_HEADER) {
                this.coldPlugin.getLogger().warning(line);
            }
            this.displayedSnapshotHeader = true;
            return;
        }

        boolean firstLoad = false;
        CommentedFileConfiguration configuration = CommentedFileConfiguration.loadConfiguration(configFile);
        if (!configuration.contains("check-updates")) {
            configuration.set("check-updates", true, "Should all plugins running ColdDev check for updates?", "ColdDev is a core library created by Cold Development");
            configuration.save(configFile);
            firstLoad = true;
        }

        if (firstLoad || !configuration.getBoolean("check-updates"))
            return;

        // Check for updates
        this.coldPlugin.getScheduler().runTaskAsync(() -> this.checkForUpdate(currentVersion));
    }

    private boolean updateMessageShown = false;

    private void checkForUpdate(String currentVersion) {
        try {
            String latestVersion = this.getLatestVersion();

            if (ColdDevUtils.isUpdateAvailable(latestVersion, currentVersion)) {
                this.updateVersion = latestVersion;

                if (updateMessageShown) {
                    return;
                }

                String message = ANSI_RED + "An update for " + ANSI_PURPLE_CHINESE + this.coldPlugin.getName() + ANSI_RED + ANSI_BOLD
                        + " (" + this.updateVersion + ")" + ANSI_RESET + ANSI_RED + " is available! You are running "
                        + ANSI_RED + ANSI_BOLD + "v" + currentVersion + "." + ANSI_RESET;

                ColdDevUtils.getLogger().info(message);

                updateMessageShown = true;
            }
        } catch (Exception e) {
            ColdDevUtils.getLogger().warning("An error occurred checking for an update. There is either no established internet connection or the Spigot API is down.");
        }
    }

    @Override
    public void disable() {

    }

    /**
     * Gets the latest version of the plugin from the Spigot Web API
     *
     * @return the latest version of the plugin from Spigot
     * @throws IOException if a network error occurs
     */
    private String getLatestVersion() throws IOException {
        URL spigot = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.coldPlugin.getSpigotId());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(spigot.openStream()))) {
            return reader.readLine();
        }
    }

    /**
     * @return the version of the latest update of this plugin, or null if there is none
     */
    public String getUpdateVersion() {
        return this.updateVersion;
    }

    /**
     * Called when a player joins and notifies ops if an update is available
     *
     * @param event The join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (this.updateVersion == null) {
            return;
        }

        if (!player.isOp()) {
            return;
        }

        // Creează o sarcină cu un delay de 5 secunde
        new BukkitRunnable() {
            @Override
            public void run() {
                String website = coldPlugin.getDescription().getWebsite();
                String updateMessage = "&cAn update for " + ColdDevUtils.GRADIENT +
                        coldPlugin.getName() + " &c(&4%new%&c) is available! You are running &4%current%&c." +
                        (website != null ? " " + website : "");

                StringPlaceholders placeholders = StringPlaceholders.of("new", updateVersion, "current", coldPlugin.getDescription().getVersion());
                ColdDevUtils.sendMessage(player, updateMessage, placeholders);
            }
        }.runTaskLater(this.coldPlugin, 100L); // 100 ticks = 5 secunde
    }

}