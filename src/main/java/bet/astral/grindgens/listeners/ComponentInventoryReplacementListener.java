package bet.astral.grindgens.listeners;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.component.MenuReplacementComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class ComponentInventoryReplacementListener implements Listener {
	private final GrindGens grindGens;

	public ComponentInventoryReplacementListener(GrindGens grindGens) {
		this.grindGens = grindGens;
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event){
		if (true)
			return;
		if (event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			Location location = block.getLocation();
			GlobalComponentManager globalComponentManager = grindGens.globalComponentManager();
			MenuReplacementComponent component = globalComponentManager.get(location, MenuReplacementComponent.class);
			if (component == null) {
				return;
			}
			event.setCancelled(true);

			Inventory inventory = component.getInventory();
			Player player = event.getPlayer();
			player.openInventory(inventory);
		}
	}
}
