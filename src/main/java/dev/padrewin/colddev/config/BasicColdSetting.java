package dev.padrewin.colddev.config;

import java.util.Objects;
import java.util.function.Supplier;

/* package */ class BasicColdSetting<T> implements ColdSetting<T> {

    private final String key;
    private final ColdSettingSerializer<T> serializer;
    private final Supplier<T> defaultValueSupplier;
    private final String[] comments;

    public BasicColdSetting(String key, ColdSettingSerializer<T> serializer, T defaultValue, String... comments) {
        this(key, serializer, () -> defaultValue, comments);
    }

    public BasicColdSetting(String key, ColdSettingSerializer<T> serializer, Supplier<T> defaultValueSupplier, String... comments) {
        this.key = key;
        this.defaultValueSupplier = defaultValueSupplier;
        this.comments = comments != null ? comments : new String[0];
        this.serializer = serializer;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public ColdSettingSerializer<T> getSerializer() {
        return this.serializer;
    }

    @Override
    public T getDefaultValue() {
        return this.defaultValueSupplier.get();
    }

    @Override
    public String[] getComments() {
        return this.comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicColdSetting)) return false;
        BasicColdSetting<?> that = (BasicColdSetting<?>) o;
        return Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key);
    }

}
