/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.listeners;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.VaultEconomy;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.component.UpgradeableComponent;
import bet.astral.grindgens.models.Tier;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.inventory.UpgradeMenu;
import bet.astral.grindgens.models.inventory.upgrade.UpgradeMenuGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ComponentMenuListener implements Listener {
	private final GrindGens grindGens;

	@Contract(pure = true)
	public ComponentMenuListener(GrindGens grindGens) {
		this.grindGens = grindGens;
	}

	@EventHandler
	public void onComponentUpgradeHandler(@NotNull PlayerInteractEvent event) {
		if (event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			if (block == null) {
				return;
			}
			Player player = event.getPlayer();
			if (!player.isSneaking()) {
				return;
			}
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				return;
			}


			Location location = block.getLocation();
			GlobalComponentManager componentManager = grindGens.globalComponentManager();
			bet.astral.grindgens.models.component.Component component = componentManager.get(location);
			if (!(component instanceof UpgradeableComponent<?> upgradeableComponent)) {
				return;
			}
			if (!component.genPlayer().uuid().toString().equalsIgnoreCase(player.getUniqueId().toString())){
				return;
			}
			event.setCancelled(true);
			upgradeableComponent.open(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(@NotNull InventoryClickEvent event) {
		if (event.getInventory().getHolder() instanceof UpgradeMenu<?> menu) {
			event.setCancelled(true);

			Player player = (Player) event.getWhoClicked();

			ItemStack itemStack = event.getCurrentItem();
			if (itemStack == null || itemStack.getType().isAir()) {
				return;
			}
			ItemMeta meta = itemStack.getItemMeta();
			PersistentDataContainer pdc = meta.getPersistentDataContainer();

			String action = pdc.get(grindGens.menuActionName(), PersistentDataType.STRING);
			if (action == null) {
				return;
			}

			if (event.getInventory().getHolder() instanceof UpgradeMenuGenerator genMenu) {
				if (genMenu.isLocked()){
					player.sendMessage("Locked!");
					return;
				}
				if (action.toLowerCase().endsWith("-upgrade")) {
					Generator generator = genMenu.component();
					UpgradeMenuGenerator.Path path = UpgradeMenuGenerator.Path.valueOf(action.split("-")[0]);

					Component notEnoughMoney = Component.text("You do not have enough money to purchase this upgrade.", NamedTextColor.RED);
					Component purchased = Component.text("Upgraded the ", NamedTextColor.GREEN).append(Component.text(path.name(), NamedTextColor.WHITE)).append(Component.text(" to the next level."));


					Tier next = null;
					switch (path) {
						case VALUE -> {
							next = generator.generatorType().valueTier(generator.valueTier()+1);
						}
						case DROP_RATE -> {
							next = generator.generatorType().dropRateTier(generator.dropRateTier()+1);
						}
						default -> {
							throw new RuntimeException("Couldn't fetch upgrade type from upgrade path!");
						}
					}

					VaultEconomy economy = grindGens.economy();

					if (economy.has(player, next.cost())){
						player.sendMessage(purchased);
						generator.addTier(path);
						generator.requestSave();
						generator.upgradeMenu().reload();
						generator.lockedMenu().reload();
						economy.withdrawPlayer(player, next.cost());
					} else {
						player.sendMessage(notEnoughMoney);
					}
 				}
			}
		}
	}
}
