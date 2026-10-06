package dev.padrewin.colddev.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* package */ class BasicColdConfig implements ColdConfig {

    private final File file;
    private final List<ColdSetting<?>> settings;
    private final Map<ColdSetting<?>, Object> settingsValueCache;
    private final String[] header;
    private final boolean writeDefaultValueComments;
    private CommentedFileConfiguration fileConfiguration;

    private BasicColdConfig(File file, List<ColdSetting<?>> settings, String[] header, boolean writeDefaultValueComments) {
        this.file = file;
        this.settings = settings;
        this.settingsValueCache = new HashMap<>(Math.min(16, this.settings.size()));
        this.header = header;
        this.writeDefaultValueComments = writeDefaultValueComments;
        this.reload();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(ColdSetting<T> setting) {
        if (this.settingsValueCache.containsKey(setting))
            return (T) this.settingsValueCache.get(setting);

        try {
            T value = setting.getSerializer().read(this.getBaseConfig(), setting);
            this.settingsValueCache.put(setting, value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return setting.getDefaultValue();
        }
    }

    @Override
    public <T> void set(ColdSetting<T> setting, T value) {
        setting.getSerializer().write(this.getBaseConfig(), setting, value);
        this.settingsValueCache.put(setting, value);
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public CommentedFileConfiguration getBaseConfig() {
        if (this.fileConfiguration == null)
            this.fileConfiguration = CommentedFileConfiguration.loadConfiguration(this.file);
        return this.fileConfiguration;
    }

    @Override
    public void reload() {
        this.fileConfiguration = null;
        this.settingsValueCache.clear();
        if (this.settings.isEmpty() && this.header.length == 0)
            return;

        boolean newFile = !this.file.exists();
        CommentedFileConfiguration config = this.getBaseConfig();

        if (newFile) {
            config.addComments(this.header);
            for (ColdSetting<?> setting : this.settings)
                setting.writeDefault(config, this.writeDefaultValueComments);
            this.save();
            return;
        }

        boolean missing = this.settings.stream().anyMatch(setting -> !config.contains(setting.getKey()));
        if (!missing)
            return;

        // An existing file gets the missing settings added line by line: re-saving it through
        // CommentedFileConfiguration would lose the comments at the end of lines and reformat it
        try {
            ConfigUpdater.update(this.file, this.defaultLines());
        } catch (IOException e) {
            e.printStackTrace();
            for (ColdSetting<?> setting : this.settings)
                if (!config.contains(setting.getKey()))
                    setting.writeDefault(config, this.writeDefaultValueComments);
            this.save();
        }
        this.fileConfiguration = null;
    }

    /**
     * @return every setting with its default value and comments, as the lines of a YAML file
     */
    private List<String> defaultLines() throws IOException {
        File defaults = File.createTempFile("colddev-config", ".yml");
        try {
            CommentedFileConfiguration config = CommentedFileConfiguration.loadConfiguration(defaults);
            for (ColdSetting<?> setting : this.settings)
                setting.writeDefault(config, this.writeDefaultValueComments);
            config.save(defaults);
            return Files.readAllLines(defaults.toPath(), StandardCharsets.UTF_8);
        } finally {
            defaults.delete();
        }
    }

    @Override
    public List<ColdSetting<?>> getSettings() {
        return Collections.unmodifiableList(this.settings);
    }

    public static class Builder implements ColdConfig.Builder {

        private final File file;
        private String[] header;
        private List<ColdSetting<?>> settings;
        private boolean writeDefaultValueComments;

        public Builder(File file) {
            this.file = file;
            this.header = new String[0];
            this.settings = Collections.emptyList();
            this.writeDefaultValueComments = false;
        }

        @Override
        public ColdConfig.Builder header(String... header) {
            this.header = header;
            return this;
        }

        @Override
        public ColdConfig.Builder settings(List<ColdSetting<?>> settings) {
            this.settings = new ArrayList<>(settings);
            return this;
        }

        @Override
        public ColdConfig.Builder writeDefaultValueComments() {
            this.writeDefaultValueComments = true;
            return this;
        }

        @Override
        public ColdConfig build() {
            return new BasicColdConfig(this.file, this.settings, this.header, this.writeDefaultValueComments);
        }

    }

}
