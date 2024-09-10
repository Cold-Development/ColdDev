package dev.padrewin.coldplugin.config;

import java.io.File;
import java.util.List;

public interface ColdConfig {

    List<ColdSetting<?>> getSettings();

    <T> T get(ColdSetting<T> setting);

    default <T> T get(String key, ColdSettingSerializer<T> serializer) {
        return this.get(key, serializer, null);
    }

    default <T> T get(String key, ColdSettingSerializer<T> serializer, T defaultValue) {
        return this.get(ColdSetting.of(key, serializer, defaultValue));
    }

    <T> void set(ColdSetting<T> setting, T value);

    default <T> void set(String key, T value, ColdSettingSerializer<T> serializer) {
        this.set(ColdSetting.of(key, serializer, value), value);
    }

    File getFile();

    CommentedFileConfiguration getBaseConfig();

    void reload();

    default void save() {
        this.getBaseConfig().save(this.getFile());
    }

    static Builder builder(File file) {
        return new BasicColdConfig.Builder(file);
    }

    interface Builder {

        Builder header(String... header);

        Builder settings(List<ColdSetting<?>> settings);

        Builder writeDefaultValueComments();

        ColdConfig build();

    }

}
