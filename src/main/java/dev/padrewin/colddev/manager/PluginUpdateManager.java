package dev.padrewin.colddev.manager;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.config.CommentedFileConfiguration;
import dev.padrewin.colddev.scheduler.task.ScheduledTask;
import dev.padrewin.colddev.utils.ColdDevUtils;
import dev.padrewin.colddev.utils.HexUtils;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Checks the plugin's latest GitHub release and tells the console and staff when it's newer than the
 * running version, with the release's message for server owners when it has one (see {@link UpdateNotice}).
 * <p>
 * The check runs when the server starts and then every {@code check-updates-interval-hours} (ColdDev's
 * config.yml), so a message added to a release later still reaches running servers. Reloading the plugin
 * doesn't check again before the interval has passed.
 */
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

    private static final long DEFAULT_INTERVAL_HOURS = 6;
    private static final int TIMEOUT_MILLIS = 10_000;
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024;

    private volatile String updateVersion;
    private volatile List<String> updateMessage = Collections.emptyList();

    // Kept across reloads: the manager instance survives them (disable + reload)
    private volatile long lastCheckMillis;
    private volatile String announced;
    private boolean snapshotWarned;
    private ScheduledTask checkTask;

    public PluginUpdateManager(ColdPlugin coldPlugin) {
        super(coldPlugin);
        Bukkit.getPluginManager().registerEvents(this, this.coldPlugin);
    }

    @Override
    public void reload() {
        if (this.coldPlugin.getGithubOwner() == null || this.coldPlugin.getGithubRepo() == null)
            return;

        String currentVersion = this.coldPlugin.getDescription().getVersion();
        if (currentVersion.contains("-SNAPSHOT")) {
            if (!this.snapshotWarned) {
                for (String line : SNAPSHOT_HEADER) {
                    this.coldPlugin.getLogger().warning(line);
                }
                this.snapshotWarned = true;
            }
            return;
        }

        File configFile = new File(this.coldPlugin.getColdDevDataFolder(), "config.yml");
        CommentedFileConfiguration configuration = CommentedFileConfiguration.loadConfiguration(configFile);
        boolean firstLoad = false;
        boolean changed = false;
        if (!configuration.contains("check-updates")) {
            configuration.set("check-updates", true, "Should all plugins running ColdDev check for updates?", "ColdDev is a core library created by Cold Development");
            firstLoad = true;
            changed = true;
        }
        if (!configuration.contains("check-updates-interval-hours")) {
            configuration.set("check-updates-interval-hours", DEFAULT_INTERVAL_HOURS,
                    "How often running servers check again for an update, in hours.",
                    "0 only checks when the server starts.");
            changed = true;
        }
        if (changed)
            configuration.save(configFile);

        if (firstLoad || !configuration.getBoolean("check-updates"))
            return;

        long intervalHours = Math.max(0, configuration.getLong("check-updates-interval-hours", DEFAULT_INTERVAL_HOURS));
        long intervalMillis = TimeUnit.HOURS.toMillis(intervalHours);
        long sinceLastCheck = System.currentTimeMillis() - this.lastCheckMillis;

        if (intervalHours == 0) {
            if (this.lastCheckMillis == 0)
                this.checkTask = this.coldPlugin.getScheduler().runTaskAsync(() -> this.checkForUpdate(currentVersion));
            return;
        }

        // After a reload, carry on where the previous schedule was instead of checking right away
        long firstDelay = this.lastCheckMillis == 0 ? 0 : Math.max(0, intervalMillis - sinceLastCheck);
        this.checkTask = this.coldPlugin.getScheduler().runTaskTimerAsync(() -> this.checkForUpdate(currentVersion),
                TimeUnit.MILLISECONDS.toSeconds(firstDelay), TimeUnit.MILLISECONDS.toSeconds(intervalMillis), TimeUnit.SECONDS);
    }

    private void checkForUpdate(String currentVersion) {
        this.lastCheckMillis = System.currentTimeMillis();
        try {
            UpdateNotice.Release release = this.fetchLatestRelease();
            if (release.tag() == null)
                throw new IOException("Could not parse version from GitHub API response");

            if (!ColdDevUtils.isUpdateAvailable(release.tag(), currentVersion)) {
                this.updateVersion = null;
                this.updateMessage = Collections.emptyList();
                return;
            }

            List<String> message = release.message();
            this.updateMessage = message;
            this.updateVersion = release.tag();

            // The console hears about each version (and each change of its message) once
            String key = release.tag() + '\n' + String.join("\n", message);
            if (key.equals(this.announced))
                return;
            this.announced = key;

            ColdDevUtils.getLogger().info(ANSI_RED + "An update for " + ANSI_PURPLE_CHINESE + this.coldPlugin.getName() + ANSI_RED + ANSI_BOLD
                    + " (" + release.tag() + ")" + ANSI_RESET + ANSI_RED + " is available! You are running "
                    + ANSI_BOLD + ANSI_RED + "v" + currentVersion + "." + ANSI_RESET);
            for (String line : message) {
                ColdDevUtils.getLogger().info("  " + ChatColor.stripColor(HexUtils.colorify(line)));
            }
        } catch (Exception e) {
            ColdDevUtils.getLogger().warning("An error occurred checking for an update. There is either no established internet connection or the GitHub API is down.");
        }
    }

    @Override
    public void disable() {
        if (this.checkTask != null) {
            this.checkTask.cancel();
            this.checkTask = null;
        }
    }

    /**
     * Gets the latest release of the plugin from the GitHub API
     *
     * @return its tag and notes
     * @throws IOException if a network error occurs
     */
    private UpdateNotice.Release fetchLatestRelease() throws IOException {
        String owner = this.coldPlugin.getGithubOwner();
        String repo = this.coldPlugin.getGithubRepo();
        URL github = new URL("https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest");

        HttpURLConnection connection = (HttpURLConnection) github.openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("User-Agent", "ColdDev-UpdateChecker");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK)
                throw new IOException("Failed to fetch latest version. Response code: " + responseCode);

            try (InputStream input = connection.getInputStream()) {
                // Release notes can be long, but never this long: don't let a bad response fill the memory
                byte[] body = input.readNBytes(MAX_RESPONSE_BYTES + 1);
                if (body.length > MAX_RESPONSE_BYTES)
                    throw new IOException("The GitHub API response is too large");
                return UpdateNotice.parseRelease(new String(body, StandardCharsets.UTF_8));
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * @return the version of the latest update of this plugin, or null if there is none
     */
    public String getUpdateVersion() {
        return this.updateVersion;
    }

    /**
     * @return the message for server owners of the latest update (colour codes not applied yet);
     * empty if there is no update or it has no message
     */
    public List<String> getUpdateMessage() {
        return this.updateMessage;
    }

    /**
     * Called when a player joins and notifies them if an update is available, when they have
     * {@code <plugin>.updates} (this plugin) or {@code colddev.updates} (every ColdDev plugin).
     * <p>
     * A permission rather than isOp(): servers give staff everything with LuckPerms ({@code *})
     * instead of /op, and that grants these too. Undeclared permissions default to OPs, so OPs
     * still get the message without any setup.
     *
     * @param event The join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (this.updateVersion == null) {
            return;
        }

        if (!player.hasPermission(this.coldPlugin.getName().toLowerCase() + ".updates")
                && !player.hasPermission("colddev.updates")) {
            return;
        }

        this.coldPlugin.getScheduler().runTaskAtEntityLater(player, () -> {
            String version = this.updateVersion;
            if (version == null || !player.isOnline()) {
                return;
            }

            String website = coldPlugin.getDescription().getWebsite();
            String updateMessage = "&cAn update for " + ColdDevUtils.GRADIENT +
                    coldPlugin.getName() + " &c(&4%new%&c) is available! You are running &4v%current%&c.";

            StringPlaceholders placeholders = StringPlaceholders.of("new", version, "current", coldPlugin.getDescription().getVersion());

            ColdDevUtils.sendMessage(player, updateMessage, placeholders);
            for (String line : this.updateMessage) {
                player.sendMessage(HexUtils.colorify("&7" + line));
            }

            if (website != null) {
                Component clickHereComponent = Component.text("Click here to update")
                        .color(NamedTextColor.GREEN)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(website))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open the plugin page.")));

                player.sendMessage(clickHereComponent);
                player.sendMessage(Component.empty());
            }
        }, 150L);
    }
}
