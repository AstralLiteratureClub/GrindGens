package bet.astral.grindgens.listeners;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.component.MinableComponent;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.generators.GeneratorType;
import bet.astral.grindgens.models.hopper.ChunkHopper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ComponentPlacementListener implements Listener {
	private final GrindGens grindGens;

	public ComponentPlacementListener(GrindGens grindGens) {
		this.grindGens = grindGens;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onGeneratorPlace(BlockPlaceEvent event){
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemInHand();
		ItemMeta meta = itemStack.getItemMeta();
		PersistentDataContainer pdc = meta.getPersistentDataContainer();

		if (pdc.has(grindGens.generatorItemTypeKey()) && pdc.has(grindGens.generatorItemTierKey())){
			String generatorStringType = pdc.get(grindGens.generatorItemTypeKey(), PersistentDataType.STRING);
			Integer tierType = pdc.get(grindGens.generatorItemTierKey(), PersistentDataType.INTEGER);
			if (tierType == null){
				event.setBuild(false);
				event.setCancelled(true);
				return;
			}

			Location location = event.getBlock().getLocation();
			int id = grindGens.globalComponentManager().requestSecureId();

			GlobalComponentManager componentManager = grindGens.globalComponentManager();
			GeneratorType type = componentManager.getGeneratorType(generatorStringType);
			GenPlayer genPlayer = grindGens.playerManager().asGenPlayer(event.getPlayer());
			Generator generator = new Generator(grindGens, genPlayer, type, location, id, tierType);
			genPlayer.addGenerator(generator);

			grindGens.globalComponentManager().save(generator);
			generator.setLoaded(true);
			player.sendMessage("You placed down a generator! "+generator.id());
		}
	}

	@EventHandler
	public void onChunkHopperPlace(BlockPlaceEvent event){
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemInHand();
		ItemMeta meta = itemStack.getItemMeta();
		PersistentDataContainer pdc = meta.getPersistentDataContainer();

		if (pdc.has(grindGens.chunkHopperItemKey())){
			Location location = event.getBlock().getLocation();

			GlobalComponentManager globalComponentManager = grindGens.globalComponentManager();
			GenPlayer genPlayer = grindGens.playerManager().asGenPlayer(event.getPlayer());

			ChunkHopper chunkHopper = new ChunkHopper(globalComponentManager, genPlayer, location);
			globalComponentManager.save(chunkHopper);
			genPlayer.addChunkHopper(chunkHopper);

			player.sendMessage("You placed down a chunk hopper!");
		}
	}

	@EventHandler
	public void onComponentBreak(BlockBreakEvent event) {
		Location location = event.getBlock().getLocation();
		GlobalComponentManager componentManager = grindGens.globalComponentManager();
		MinableComponent minableComponent = componentManager.get(location, MinableComponent.class);
		if (minableComponent == null) {
			return;
		}
		ItemStack itemStack = minableComponent.getBlock();

		componentManager.delete(minableComponent);
		if (event.isDropItems())
			location.getWorld().dropItemNaturally(location, itemStack);
		event.setDropItems(false);
	}
}
