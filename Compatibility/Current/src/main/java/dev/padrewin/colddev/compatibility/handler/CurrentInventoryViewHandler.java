package dev.padrewin.colddev.compatibility.handler;

import dev.padrewin.colddev.compatibility.wrapper.CurrentWrappedInventoryView;
import dev.padrewin.colddev.compatibility.wrapper.WrappedInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public class CurrentInventoryViewHandler implements InventoryViewHandler {

    @Override
    public WrappedInventoryView openInventory(Player player, Inventory inventory) {
        return wrap(player.openInventory(inventory));
    }

    @Override
    public <T extends InventoryEvent> WrappedInventoryView getView(T event) {
        return wrap(event.getView());
    }

    @Override
    public WrappedInventoryView getOpenInventory(Player player) {
        return wrap(player.getOpenInventory());
    }

    private static WrappedInventoryView wrap(InventoryView view) {
        return new CurrentWrappedInventoryView(view);
    }

}
