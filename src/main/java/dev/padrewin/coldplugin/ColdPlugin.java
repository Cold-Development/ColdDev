package dev.padrewin.coldplugin;

import dev.padrewin.coldplugin.command.framework.ColdCommandWrapper;
import dev.padrewin.coldplugin.command.rwd.RwdCommand;
import dev.padrewin.coldplugin.config.ColdConfig;
import dev.padrewin.coldplugin.config.ColdSetting;
import dev.padrewin.coldplugin.manager.AbstractCommandManager;
import dev.padrewin.coldplugin.manager.AbstractDataManager;
import dev.padrewin.coldplugin.manager.AbstractLocaleManager;
import dev.padrewin.coldplugin.manager.Manager;
import dev.padrewin.coldplugin.manager.PluginUpdateManager;
import dev.padrewin.coldplugin.objects.ColdPluginData;
import dev.padrewin.coldplugin.scheduler.ColdScheduler;
import dev.padrewin.coldplugin.utils.ColdDevUtils;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class ColdPlugin extends JavaPlugin {

    /**
     * The ColdPlugin identifier
     */
    public static final String COLDDEV_VERSION = "@version@";

    /**
     * The plugin ID on Spigot
     */
    private final int spigotId;

    /**
     * The plugin ID on bStats
     */
    private final int bStatsId;

    /**
     * The classes that extend the abstract managers
     */
    private final Class<? extends AbstractDataManager> dataManagerClass;
    private final Class<? extends AbstractLocaleManager> localeManagerClass;
    private final Class<? extends AbstractCommandManager> commandManagerClass;

    /**
     * The plugin managers
     */
    private final Map<Class<? extends Manager>, Manager> managers;
    private final Deque<Class<? extends Manager>> managerInitializationStack;

    /**
     * The main config.yml
     */
    private ColdConfig coldConfig;

    private boolean firstInitialization = true;
    private boolean firstToRegister = false;

    public ColdPlugin(int spigotId,
                      int bStatsId,
                      Class<? extends AbstractDataManager> dataManagerClass,
                      Class<? extends AbstractLocaleManager> localeManagerClass,
                      Class<? extends AbstractCommandManager> commandManagerClass) {
        if (dataManagerClass != null && Modifier.isAbstract(dataManagerClass.getModifiers()))
            throw new IllegalArgumentException("dataManagerClass cannot be abstract");
        if (localeManagerClass != null && Modifier.isAbstract(localeManagerClass.getModifiers()))
            throw new IllegalArgumentException("localeManagerClass cannot be abstract");
        if (commandManagerClass != null && Modifier.isAbstract(commandManagerClass.getModifiers()))
            throw new IllegalArgumentException("commandManagerClass cannot be abstract");

        this.spigotId = spigotId;
        this.bStatsId = bStatsId;
        this.dataManagerClass = dataManagerClass;
        this.localeManagerClass = localeManagerClass;
        this.commandManagerClass = commandManagerClass;

        this.managers = new ConcurrentHashMap<>();
        this.managerInitializationStack = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void onLoad() {
        // Log that we are loading
        this.getLogger().info("Initializing using ColdDev v" + COLDDEV_VERSION);

        // Log severe if the library is not relocated
        if (!ColdDevUtils.isRelocated()) {
            ColdDevUtils.getLogger().severe("=====================================================");
            ColdDevUtils.getLogger().severe("DEVELOPER ERROR!!! ColdDev has not been relocated!");
            ColdDevUtils.getLogger().severe("=====================================================");
        }
    }

    @Override
    public void onEnable() {
        // bStats Metrics
        if (this.bStatsId != -1) {
            Metrics metrics = new Metrics(this, this.bStatsId);
            this.addCustomMetricsCharts(metrics);
        }

        // Inject the plugin class into the spigot services manager
        this.injectService();

        // Load the main config file
        this.getColdConfig();

        // Load managers
        this.reload();

        // Run the plugin's enable code
        this.enable();
    }

    @Override
    public void onDisable() {
        // Run the plugin's disable code
        this.disable();

        // Shut down the managers
        this.disableManagers();
    }

    /**
     * Called during {@link JavaPlugin#onEnable} after managers have been enabled
     */
    protected abstract void enable();

    /**
     * Called during {@link JavaPlugin#onDisable} before managers have been disabled
     */
    protected abstract void disable();

    /**
     * @return the order in which Managers should be loaded, excluding
     */
    @NotNull
    protected abstract List<Class<? extends Manager>> getManagerLoadPriority();

    /**
     * Checks if the database is local only.
     * Returning true will prevent the database settings from appearing in the config.yml.
     *
     * @return true if the database is local only or doesn't exist, false otherwise
     */
    public boolean isLocalDatabaseOnly() {
        return false;
    }

    /**
     * @return the settings that should be written to the config.yml
     */
    @NotNull
    protected List<ColdSetting<?>> getColdConfigSettings() {
        return Collections.emptyList();
    }

    /**
     * @return the header to place at the top of the config.yml
     */
    @NotNull
    protected String[] getColdConfigHeader() {
        return new String[0];
    }

    /**
     * Registers any custom bStats Metrics charts for the plugin
     *
     * @param metrics The Metrics instance
     */
    protected void addCustomMetricsCharts(Metrics metrics) {
        // Must be overridden for any functionality.
    }

    /**
     * Reloads the plugin's managers
     */
    public void reload() {
        this.disableManagers();

        if (this.coldConfig != null)
            this.coldConfig.reload();

        if (this.firstInitialization) {
            List<Class<? extends Manager>> managerLoadPriority = new ArrayList<>();

            if (this.hasDataManager())
                managerLoadPriority.add(this.dataManagerClass);

            if (this.hasLocaleManager())
                managerLoadPriority.add(this.localeManagerClass);

            if (this.hasCommandManager())
                managerLoadPriority.add(this.commandManagerClass);

            managerLoadPriority.addAll(this.getManagerLoadPriority());

            if (this.spigotId != -1)
                managerLoadPriority.add(PluginUpdateManager.class);

            managerLoadPriority.forEach(this::getManager);
        } else {
            List<Class<? extends Manager>> initStack = new ArrayList<>(this.managerInitializationStack);
            Collections.reverse(initStack);
            for (Class<? extends Manager> managerClass : initStack) {
                Manager manager = this.managers.get(managerClass);
                try {
                    manager.reload();
                } catch (Exception e) {
                    throw new ManagerLoadException(managerClass, e);
                }
            }
        }

        this.firstInitialization = false;
    }

    /**
     * Runs {@link Manager#disable} on all managers in the reverse order that they were loaded.
     */
    private void disableManagers() {
        for (Class<? extends Manager> managerClass : this.managerInitializationStack) {
            Manager manager = this.managers.get(managerClass);
            try {
                manager.disable();
            } catch (Exception e) {
                throw new ManagerUnloadException(managerClass, e);
            }
        }
    }

    /**
     * Gets a manager instance and loads it if this is the first call to get it.
     *
     * @param managerClass The class of the manager to get
     * @param <T> extends Manager
     * @return A new or existing instance of the given manager class
     * @throws ManagerLoadException if the manager fails to load
     * @throws ManagerInitializationException if the manager fails to initialize
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T extends Manager> T getManager(Class<T> managerClass) {
        // Get the actual class if the abstract one is requested
        Class<? extends Manager> lookupClass = this.remapAbstractManagerClasses(managerClass);

        AtomicBoolean initialized = new AtomicBoolean();
        T manager = (T) this.managers.computeIfAbsent(lookupClass, key -> {
            try {
                return lookupClass.getConstructor(ColdPlugin.class).newInstance(this);
            } catch (Exception e) {
                throw new ManagerInitializationException(lookupClass, e);
            } finally {
                initialized.set(true);
            }
        });

        if (initialized.get()) {
            try {
                manager.reload();
            } catch (Exception e) {
                throw new ManagerLoadException(lookupClass, e);
            } finally {
                this.managerInitializationStack.push(lookupClass);
            }
        }

        return manager;
    }

    protected <T extends Manager> Class<? extends Manager> remapAbstractManagerClasses(Class<T> managerClass) {
        Class<? extends Manager> lookupClass;
        if (this.hasDataManager() && managerClass == AbstractDataManager.class) {
            lookupClass = this.dataManagerClass;
        } else if (this.hasLocaleManager() && managerClass == AbstractLocaleManager.class) {
            lookupClass = this.localeManagerClass;
        } else if (this.hasCommandManager() && managerClass == AbstractCommandManager.class) {
            lookupClass = this.commandManagerClass;
        } else {
            lookupClass = managerClass;
        }
        return lookupClass;
    }

    /**
     * @return the scheduler for this plugin
     */
    public final ColdScheduler getScheduler() {
        return ColdScheduler.getInstance(this);
    }

    /**
     * @return the main config for this plugin
     */
    public final ColdConfig getColdConfig() {
        if (this.coldConfig == null) {
            List<ColdSetting<?>> settings = new ArrayList<>();

            if (this.hasLocaleManager())
                settings.addAll(AbstractLocaleManager.SettingKey.getKeys());

            settings.addAll(this.getColdConfigSettings());

            if (this.hasDataManager() && !this.isLocalDatabaseOnly())
                settings.addAll(AbstractDataManager.SettingKey.getKeys());

            File file = new File(this.getDataFolder(), "config.yml");
            this.coldConfig = ColdConfig.builder(file)
                    .header(this.getColdConfigHeader())
                    .settings(settings)
                    .writeDefaultValueComments()
                    .build();
        }
        return this.coldConfig;
    }

    /**
     * @return the ID of the plugin on Spigot, or -1 if not tracked
     */
    public final int getSpigotId() {
        return this.spigotId;
    }

    private void injectService() {
        if (this.getLoadedColdPluginsData().isEmpty()) {
            this.firstToRegister = true;
            new ColdCommandWrapper("colddev", this.getColdDevDataFolder(), this, new RwdCommand(this)).register();
        }

        // Register our service
        Bukkit.getServicesManager().register(ColdPlugin.class, this, this, ServicePriority.Normal);
    }

    /**
     * @return data of all ColdPlugins installed on the server
     */
    @NotNull
    public final List<ColdPluginData> getLoadedColdPluginsData() {
        List<ColdPluginData> data = new ArrayList<>();

        ServicesManager servicesManager = Bukkit.getServicesManager();
        for (Class<?> service : servicesManager.getKnownServices()) {
            try {
                String colddevVersion = (String) service.getField("COLDDEV_VERSION").get(null);
                Method updateVersionMethod = service.getMethod("getUpdateVersion");

                for (RegisteredServiceProvider<?> provider : servicesManager.getRegistrations(service)) {
                    Plugin plugin = provider.getPlugin();
                    String pluginName = plugin.getName();
                    String pluginVersion = plugin.getDescription().getVersion();
                    String website = plugin.getDescription().getWebsite();
                    String updateVersion = (String) updateVersionMethod.invoke(plugin);
                    data.add(new ColdPluginData(pluginName, pluginVersion, updateVersion, website, colddevVersion));
                }
            } catch (ReflectiveOperationException | ClassCastException ignored) { }
        }

        return data;
    }

    /**
     * @return the data folder for ColdDev
     */
    @NotNull
    public final File getColdDevDataFolder() {
        File configDir = new File(this.getDataFolder().getParentFile(), "ColdDev");
        if (!configDir.exists())
            configDir.mkdirs();
        return configDir;
    }

    public final boolean hasDataManager() {
        return this.dataManagerClass != null;
    }

    public final boolean hasLocaleManager() {
        return this.localeManagerClass != null;
    }

    public final boolean hasCommandManager() {
        return this.commandManagerClass != null;
    }

    /**
     * An exception thrown when a Manager fails during {@link Manager#reload()}
     */
    private static class ManagerLoadException extends RuntimeException {

        public ManagerLoadException(Class<? extends Manager> managerClass, Throwable cause) {
            super("Failed to load " + managerClass.getSimpleName(), cause);
        }

    }

    /**
     * An exception thrown when a Manager fails during {@link Manager#disable()}
     */
    private static class ManagerUnloadException extends RuntimeException {

        public ManagerUnloadException(Class<? extends Manager> managerClass, Throwable cause) {
            super("Failed to unload " + managerClass.getSimpleName(), cause);
        }

    }

    /**
     * An exception thrown when a Manager can't be initialized
     */
    private static class ManagerInitializationException extends RuntimeException {

        public ManagerInitializationException(Class<? extends Manager> managerClass, Throwable cause) {
            super("Failed to initialize " + managerClass.getSimpleName(), cause);
        }

    }

}