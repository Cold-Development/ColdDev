package dev.padrewin.coldplugin.compatibility.handler;

import dev.padrewin.coldplugin.compatibility.wrapper.WrappedInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;

public interface InventoryViewHandler {

    WrappedInventoryView openInventory(Player player, Inventory inventory);

    <T extends InventoryEvent> WrappedInventoryView getView(T event);

    WrappedInventoryView getOpenInventory(Player player);

}
