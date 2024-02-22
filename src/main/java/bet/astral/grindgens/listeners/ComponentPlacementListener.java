/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.listeners;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.component.ComponentType;
import bet.astral.grindgens.models.component.MinableComponent;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.generators.GeneratorType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ComponentPlacementListener implements Listener {
	private final GrindGens grindGens;

	public ComponentPlacementListener(GrindGens grindGens) {
		this.grindGens = grindGens;
	}

	@EventHandler(priority =EventPriority.LOWEST, ignoreCancelled = true)
	public void onGeneratorPlace(BlockPlaceEvent event){
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemInHand();
		ItemMeta meta = itemStack.getItemMeta();
		PersistentDataContainer pdc = meta.getPersistentDataContainer();

		if (pdc.has(grindGens.generatorItemTypeKey()) && pdc.has(grindGens.generatorItemDropTierKey())){
			String generatorStringType = pdc.get(grindGens.generatorItemTypeKey(), PersistentDataType.STRING);
			Integer tierDrop = pdc.get(grindGens.generatorItemDropTierKey(), PersistentDataType.INTEGER);
			Integer tierValue = pdc.get(grindGens.generatorItemValueTierKey(), PersistentDataType.INTEGER);

			if (tierDrop == null || tierValue == null){
				return;
			}

			GlobalComponentManager globalComponentManager = grindGens.globalComponentManager();
			GenPlayer genPlayer = grindGens.playerManager().asGenPlayer(event.getPlayer());


			Location location = event.getBlock().getLocation();
			int id = grindGens.globalComponentManager().requestSecureId();

			GeneratorType type = globalComponentManager.getGeneratorType(generatorStringType);

			if (genPlayer.generators().size()>=genPlayer.maxGenerators()){
				player.sendRichMessage("<red>You've already reached the maximum amount of generators!");
				event.setCancelled(true);
				event.setBuild(false);
				return;
			}

			Chunk chunk = location.getChunk();
			if (globalComponentManager.get(chunk) != null){
				Set<Component> components = new HashSet<>(Objects.requireNonNull(globalComponentManager.get(chunk)));
				components.removeIf(component -> component.type()!= ComponentType.GENERATOR);
				if (components.size()>=grindGens.config.maxChunkGenerators){
					player.sendRichMessage("<red>You've reached the maximum amount of generators in this chunk!");
					event.setCancelled(true);
					event.setBuild(false);
					return;
				}
			}


			Generator generator = new Generator(grindGens, genPlayer, type, location, id, tierDrop, tierValue);

			grindGens.globalComponentManager().save(generator);
			generator.setLoaded(true);
			generator.setNew(true);
//			player.sendRichMessage("<green>Placed down a generator.");
//			player.sendMessage("You placed down a generator! "+generator.id());
		}
	}
	@EventHandler(priority =EventPriority.LOWEST, ignoreCancelled = true)
	public void onComponentBreak(BlockBreakEvent event) {
		Location location = event.getBlock().getLocation();
		GlobalComponentManager componentManager = grindGens.globalComponentManager();
		Component component = componentManager.get(location);
		if (!(component instanceof MinableComponent minableComponent)) {
			return;
		}
		if (!component.genPlayer().uuid().toString().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())){
			event.setCancelled(true);
			event.setDropItems(false);
			return;
		}
		ItemStack itemStack = minableComponent.getBlock();

		componentManager.delete(minableComponent);
		if (event.isDropItems())
			location.getWorld().dropItemNaturally(location, itemStack);
		for (Player p : Bukkit.getOnlinePlayers()) {
			try {
				grindGens.glowingBlocks().unsetGlowing(location, p);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		event.setDropItems(false);
	}
}
