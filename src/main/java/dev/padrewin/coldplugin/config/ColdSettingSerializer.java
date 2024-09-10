package dev.padrewin.coldplugin.config;

@FunctionalInterface
public interface ColdSettingSerializer<T> {

    T read(CommentedConfigurationSection config, String key);

    default T read(CommentedConfigurationSection config, ColdSetting<T> setting) {
        return this.read(config, setting.getKey());
    }

    default void write(CommentedConfigurationSection config, ColdSetting<T> setting, T value) {
        this.write(config, setting.getKey(), value, setting.getComments());
    }

    default void write(CommentedConfigurationSection config, String key, T value, String... comments) {
        config.set(key, value, comments);
    }

}
