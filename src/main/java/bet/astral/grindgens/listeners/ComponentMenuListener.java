package bet.astral.grindgens.listeners;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.component.UpgradeableComponent;
import bet.astral.grindgens.models.inventory.UpgradeMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ComponentMenuListener implements Listener {
	private final GrindGens grindGens;

	@Contract(pure = true)
	public ComponentMenuListener(GrindGens grindGens) {
		this.grindGens = grindGens;
	}

	@EventHandler
	public void onComponentUpgradeHandler(@NotNull PlayerInteractEvent event){
		if (event.getClickedBlock() != null){
			Block block = event.getClickedBlock();
			if (block == null){
				return;
			}
			Player player = event.getPlayer();
			if (!player.isSneaking()){
				return;
			}
			if (event.getAction() == Action.LEFT_CLICK_BLOCK){
				return;
			}


			Location location = block.getLocation();
			GlobalComponentManager componentManager = grindGens.globalComponentManager();
			UpgradeableComponent<?> component = componentManager.get(location, UpgradeableComponent.class);
			if (component == null){
				return;
			}
			event.setCancelled(true);
			component.open(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(@NotNull InventoryClickEvent event){
		if (event.getInventory().getHolder() instanceof UpgradeMenu<?> menu) {
			event.setCancelled(true);
//			if (menu instanceof UpgradeMenuGenerator generatorMenu){}
		}
	}
}
