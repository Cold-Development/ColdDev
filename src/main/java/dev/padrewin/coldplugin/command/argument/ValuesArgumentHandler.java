package dev.padrewin.coldplugin.command.argument;

import dev.padrewin.coldplugin.command.framework.Argument;
import dev.padrewin.coldplugin.command.framework.ArgumentHandler;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.InputIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValuesArgumentHandler<T> extends ArgumentHandler<T> {

    private final Map<String, T> values;

    protected ValuesArgumentHandler(Class<T> clazz, List<T> values) {
        super(clazz);
        this.values = values.stream().collect(Collectors.toMap(
                Object::toString,
                Function.identity(),
                (t, t2) -> { throw new IllegalStateException("Duplicate toString values not allowed"); },
                () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
        );
    }

    @Override
    public T handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        T value = this.values.get(input);
        if (value == null)
            throw new HandledArgumentException("argument-handler-value");
        return value;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return new ArrayList<>(this.values.keySet());
    }

}
