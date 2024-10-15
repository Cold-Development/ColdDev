package dev.padrewin.colddev.compatibility;

import dev.padrewin.colddev.compatibility.handler.CreatureSpawnerHandler;
import dev.padrewin.colddev.compatibility.handler.InventoryViewHandler;
import dev.padrewin.colddev.compatibility.handler.OldEnumHandler;
import dev.padrewin.colddev.compatibility.handler.ShearedHandler;

public final class CompatibilityAdapter {

    private static final String PACKAGE = "dev.padrewin.coldplugin.compatibility.handler.";
    private static final InventoryViewHandler INVENTORY_VIEW_HANDLER;
    private static final ShearedHandler SHEARED_HANDLER;
    private static final CreatureSpawnerHandler CREATURE_SPAWNER_HANDLER;
    private static final OldEnumHandler OLD_ENUM_HANDLER;

    static {
        try {
            INVENTORY_VIEW_HANDLER = lookupHandler(InventoryViewHandler.class, Class.forName("org.bukkit.inventory.InventoryView").isInterface());
            SHEARED_HANDLER = lookupHandler(ShearedHandler.class, classExists("org.bukkit.entity.Shearable"));
            CREATURE_SPAWNER_HANDLER = lookupHandler(CreatureSpawnerHandler.class, classExists("org.bukkit.spawner.Spawner"));
            OLD_ENUM_HANDLER = lookupHandler(OldEnumHandler.class, Class.forName("org.bukkit.entity.Villager$Profession").isInterface());
        } catch (ReflectiveOperationException e) {
            throw new CompatibilityLookupException("Failed to initialize compatibility handler", e);
        }
    }

    public static InventoryViewHandler getInventoryViewHandler() {
        return INVENTORY_VIEW_HANDLER;
    }

    public static ShearedHandler getShearedHandler() {
        return SHEARED_HANDLER;
    }

    public static CreatureSpawnerHandler getCreatureSpawnerHandler() {
        return CREATURE_SPAWNER_HANDLER;
    }

    public static OldEnumHandler getOldEnumHandler() {
        return OLD_ENUM_HANDLER;
    }

    /**
     * Looks up a handler, choosing the implementation based on if the predicate passes.
     * A handler implementation will be looked up from the classpath with the following name format:
     * {@code <Current/Legacy><handlerClass.getSimpleName()>}. For example, given
     * {@code InventoryViewHandler} the output would be {@code CurrentInventoryViewHandler} if {@code current} is true
     * and {@code LegacyInventoryViewHandler} otherwise.
     *
     * @param handlerClass The handler class that is being looked up
     * @param current True if the Current handler should be used, false for Legacy handler
     * @return The looked up handler
     * @param <T> The type of handler being looked up
     */
    private static <T> T lookupHandler(Class<T> handlerClass, boolean current) {
        try {
            String lookupClassName = PACKAGE + (current ? "Current" : "Legacy") + handlerClass.getSimpleName();
            Class<?> lookupClass = Class.forName(lookupClassName);
            return handlerClass.cast(lookupClass.getConstructor().newInstance());
        } catch (Exception e) {
            throw new CompatibilityLookupException("Failed to look up compatibility handler for " + handlerClass.getSimpleName(), e);
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static class CompatibilityLookupException extends RuntimeException {

        public CompatibilityLookupException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
