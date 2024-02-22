/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.inventory.upgrade;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.VaultEconomy;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.Tier;
import bet.astral.grindgens.models.generators.GeneratorType;
import bet.astral.grindgens.models.inventory.UpgradeMenu;
import bet.astral.grindgens.utils.TriState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class UpgradeMenuGenerator extends AbstractUpgradeMenu<Generator> implements UpgradeMenu<Generator> {

	public UpgradeMenuGenerator(@NotNull Generator component) {
		super(component);
		super.inventory = createUpgradeMenu(this);
		setup();
	}
	public UpgradeMenuGenerator(@NotNull Generator component, boolean isLocked) {
		super(component, isLocked);
		super.inventory = createUpgradeMenu(this);
		setup();
	}

	private void setup(){
		Generator generator = component();
		ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = background.getItemMeta();
		meta.displayName(Component.text(""));
		meta.addItemFlags(ItemFlag.values());
		background.setItemMeta(meta);

		for (int i = 0; i < 9; i++){
			inventory.setItem(i, background);
		}
		// 0 1 2 3 4 5
		inventory.setItem(3, upgradeButton(generator.dropRateTier(), generator.dropRateTier()+1, generator, generator.generatorType().dropRateTier(generator.dropRateTier()), generator.generatorType().dropRateTier(generator.dropRateTier()+1), Path.DROP_RATE));
		inventory.setItem(5, upgradeButton(generator.valueTier(), generator.valueTier()+1, generator, generator.generatorType().valueTier(generator.valueTier()), generator.generatorType().valueTier(generator.valueTier()+1), Path.VALUE));
	}

	public enum Path {
		VALUE,
		DROP_RATE,

	}

	public ItemStack upgradeButton(int tier, int otherTier, Generator generator, Tier newTier, Tier currentTier, Path path){
		TriState state = tier == generator.valueTier() ? TriState.SAME : tier < otherTier ? TriState.LOWER : TriState.HIGHER;
		if (currentTier == null || newTier== null){
			if (currentTier == null){
				generator.grindGens().getLogger().severe("Unknown tier: "+ otherTier);
			}
			Material material = Material.GRAY_STAINED_GLASS;
			ItemStack itemStack = new ItemStack(material);
			ItemMeta meta = itemStack.getItemMeta();
			meta.displayName(Component.text("No Upgrade Found", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
			meta.addItemFlags(ItemFlag.values());
			itemStack.setItemMeta(meta);
			return itemStack;
		}
		Material material = generator.generatorType().sourceItem().getType();

		ItemStack itemStack = new ItemStack(material);
		ItemMeta meta = itemStack.getItemMeta() != null ? Bukkit.getItemFactory().getItemMeta(material) : itemStack.getItemMeta();

		itemStack.setAmount(tier+1);

		List<Component> lore = new LinkedList<>();

		String menuAction = "nothing";
		if (!currentTier.equals(newTier)) {

			lore.add(Component.empty());
			double currentValue = currentTier.value();
			double nextValue = newTier.value();
			double cost = newTier.cost();

			// TODO - Add way to see cost to upgrade from current tier to this tier
			Component costComp = Component.text(
					" Cost: ", NamedTextColor.GREEN).append(Component.text(cost, NamedTextColor.WHITE));
			Component oldComp = null;
			Component newComp = null;

			switch (path) {
				case VALUE -> {
					if (tier!=otherTier){
						oldComp = Component.text(" Currently generates ", NamedTextColor.GREEN).append(Component.text(currentValue, NamedTextColor.WHITE)).append(Component.text(" BTC", NamedTextColor.GREEN));
						newComp = Component.text(" Next generates ", NamedTextColor.GREEN).append(Component.text(nextValue, NamedTextColor.WHITE)).append(Component.text(" BTC", NamedTextColor.GREEN));
					} else {
						oldComp = Component.text(" Currently generates ", NamedTextColor.GREEN).append(Component.text(currentValue, NamedTextColor.WHITE)).append(Component.text(" BTC", NamedTextColor.GREEN));
						newComp = Component.text(" No more upgrades", NamedTextColor.YELLOW);
					}
				}
				case DROP_RATE -> {
					if (tier!=otherTier){
						oldComp = Component.text(" Currently generates every ", NamedTextColor.GREEN).append(Component.text(currentValue*0.05, NamedTextColor.WHITE)).append(Component.text(" second", NamedTextColor.GREEN));
						newComp = Component.text(" Next generates every ", NamedTextColor.GREEN).append(Component.text(nextValue*0.05, NamedTextColor.WHITE)).append(Component.text(" second", NamedTextColor.GREEN));
					} else {
						oldComp = Component.text(" Currently generates every ", NamedTextColor.GREEN).append(Component.text(currentValue*0.05, NamedTextColor.WHITE)).append(Component.text(" second", NamedTextColor.GREEN));
						newComp = Component.text(" No more upgrades", NamedTextColor.YELLOW);
					}
				}
			}
			costComp = costComp.decoration(TextDecoration.ITALIC, false);
			oldComp = oldComp.decoration(TextDecoration.ITALIC, false);
			newComp = newComp.decoration(TextDecoration.ITALIC, false);

			lore.add(costComp);
			lore.add(oldComp);
			lore.add(newComp);
			menuAction = path+"-upgrade";
		} else {
			lore.add(Component.text(" Unlocked", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
		}

		lore.add(Component.empty());


		meta.lore(lore);

		Component displayname = displayname(tier+1);

		meta.displayName(displayname.decoration(TextDecoration.ITALIC, false));
		if (state == TriState.LOWER || state == TriState.SAME)
			meta.addEnchant(Enchantment.LUCK, 1, true);

		meta.addItemFlags(ItemFlag.values());


		PersistentDataContainer pdc = meta.getPersistentDataContainer();

		GrindGens grindGens = component().grindGens();


		pdc.set(grindGens.menuActionName(), PersistentDataType.STRING, menuAction);

		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	public @NotNull UpgradeMenuGenerator clone() {
		return new UpgradeMenuGenerator(component(), isLocked());
	}

	@Override
	public void reload() {
		inventory.clear();
		setup();
	}
}
