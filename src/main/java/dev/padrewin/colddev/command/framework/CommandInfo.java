package dev.padrewin.colddev.command.framework;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandInfo {

    public final String name;
    public final List<String> aliases;
    public final String permission;
    public final boolean playerOnly;
    public final String descriptionKey;
    public final ArgumentsDefinition arguments;

    public CommandInfo(String name,
                       List<String> aliases,
                       String permission,
                       boolean playerOnly,
                       String descriptionKey,
                       ArgumentsDefinition arguments) {
        this.name = name;
        this.aliases = aliases;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.descriptionKey = descriptionKey;
        this.arguments = arguments;
    }

    public String name() {
        return this.name;
    }

    public List<String> aliases() {
        return this.aliases;
    }

    public String permission() {
        return this.permission;
    }

    public boolean playerOnly() {
        return this.playerOnly;
    }

    public String descriptionKey() {
        return this.descriptionKey;
    }

    public ArgumentsDefinition arguments() {
        return this.arguments;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static CommandInfo named(String name) {
        return CommandInfo.builder(name).build();
    }

    public static class Builder {

        private final String name;
        private List<String> aliases;
        private String permission;
        private boolean playerOnly;
        private String descriptionKey;
        private ArgumentsDefinition arguments;

        private Builder(String name) {
            this.name = name;
            this.aliases = Collections.emptyList();
            this.permission = null;
            this.playerOnly = false;
            this.descriptionKey = null;
            this.arguments = ArgumentsDefinition.empty();
        }

        public Builder aliases(List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public Builder aliases(String... aliases) {
            this.aliases = Arrays.asList(aliases);
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder playerOnly(boolean playerOnly) {
            this.playerOnly = playerOnly;
            return this;
        }

        public Builder playerOnly() {
            this.playerOnly = true;
            return this;
        }

        public Builder descriptionKey(String descriptionKey) {
            this.descriptionKey = descriptionKey;
            return this;
        }

        public Builder arguments(ArgumentsDefinition arguments) {
            this.arguments = arguments;
            return this;
        }

        public CommandInfo build() {
            return new CommandInfo(
                    this.name,
                    this.aliases,
                    this.permission,
                    this.playerOnly,
                    this.descriptionKey,
                    this.arguments
            );
        }

    }

}
