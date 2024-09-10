package dev.padrewin.coldplugin.manager;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.framework.BaseColdCommand;
import dev.padrewin.coldplugin.command.framework.ColdCommandWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCommandManager extends Manager {

    private final List<ColdCommandWrapper> commandWrappers;

    public AbstractCommandManager(ColdPlugin coldPlugin) {
        super(coldPlugin);

        this.commandWrappers = new ArrayList<>();
    }

    @Override
    public void reload() {
        this.getRootCommands().stream()
                .map(x -> x.apply(this.coldPlugin))
                .map(x -> new ColdCommandWrapper(this.coldPlugin, x))
                .forEach(this.commandWrappers::add);

        this.commandWrappers.forEach(ColdCommandWrapper::register);
    }

    @Override
    public void disable() {
        this.commandWrappers.forEach(ColdCommandWrapper::unregister);
        this.commandWrappers.clear();
    }

    @NotNull
    public abstract List<Function<ColdPlugin, BaseColdCommand>> getRootCommands();

    public List<ColdCommandWrapper> getActiveCommands() {
        return Collections.unmodifiableList(this.commandWrappers);
    }

}