package dev.padrewin.colddev.compatibility.handler;

import dev.padrewin.colddev.compatibility.wrapper.WrappedInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;

public interface InventoryViewHandler {

    WrappedInventoryView openInventory(Player player, Inventory inventory);

    <T extends InventoryEvent> WrappedInventoryView getView(T event);

    WrappedInventoryView getOpenInventory(Player player);

}
