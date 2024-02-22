/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.manager;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.generators.GeneratorType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ItemCreator {
	private final GrindGens grindGens;

	@Contract(pure = true)
	public ItemCreator(GrindGens grindGens) {
		this.grindGens = grindGens;
	}

	public Component generatorPlaceholder(@NotNull Component component, @NotNull GeneratorType type, int dropTier, int valueTier){
		return component.replaceText(builder-> builder.match("%tier-drop-rate%").replacement(String.valueOf(dropTier)))
				.replaceText(builder-> builder.match("%tier-value%").replacement(String.valueOf(valueTier)))
				.replaceText(builder -> builder.match("%drop-rate-seconds%").replacement(String.valueOf((type.dropRateTier(dropTier).value())*(0.05))))
				.replaceText(builder -> builder.match("%drop-rate-second%").replacement(String.valueOf((type.dropRateTier(dropTier).value()*(0.05)))))
				.replaceText(builder -> builder.match("%drop-rate-ticks%").replacement(String.valueOf((type.dropRateTier(dropTier).value()))))
				.replaceText(builder -> builder.match("%drop-rate-tick%").replacement(String.valueOf((type.dropRateTier(dropTier).value()))))
				.replaceText(builder -> builder.match("%value%").replacement(String.valueOf(((type).valueTier(valueTier).value()))))
				;
	}

	@Nullable
	public Generator asGeneratorDefaultInstance(@NotNull ItemStack itemStack){
		ItemMeta meta = itemStack.getItemMeta();
		if (meta == null){
			return null;
		}
		PersistentDataContainer data = meta.getPersistentDataContainer();
		Integer dropTier = data.get(grindGens.generatorItemDropTierKey(), PersistentDataType.INTEGER);
		Integer valueTier = data.get(grindGens.generatorItemValueTierKey(), PersistentDataType.INTEGER);
		String type = data.get(grindGens.generatorItemTypeKey(), PersistentDataType.STRING);

		if (type == null || dropTier == null || valueTier == null){
			return null;
		}
		GlobalComponentManager componentManager = grindGens.globalComponentManager();
		GeneratorType genType = componentManager.getGeneratorType(type);
		if (genType == null){
			return null;
		}

		GenPlayer genPlayer = grindGens.playerManager().emptyGenPlayer();
		Location location = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		int id = 999999999;

		return new Generator(grindGens, genPlayer, genType, location, id, dropTier, valueTier);
	}

	public ItemStack createGenerator(@NotNull GeneratorType generatorType, int dropTier, int valueTier){
		ItemStack itemStack = generatorType.sourceItem().clone();
		ItemMeta meta = itemStack.getItemMeta();


		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		pdc.set(grindGens.generatorItemTypeKey(), PersistentDataType.STRING, generatorType.name());
		pdc.set(grindGens.generatorItemDropTierKey(), PersistentDataType.INTEGER, dropTier);
		pdc.set(grindGens.generatorItemValueTierKey(), PersistentDataType.INTEGER, valueTier);

		Component displayname = meta.displayName() != null ? meta.displayName() : Component.translatable(itemStack.translationKey());
		assert displayname != null;
		int finalDropTier = dropTier+1;
		int finalValueTier = valueTier+1;
		displayname = generatorPlaceholder(displayname, generatorType, dropTier, valueTier);

		meta.displayName(displayname.decoration(TextDecoration.ITALIC, false));
		if (meta.lore() != null) {
			List<Component> lore = new LinkedList<>();
			for (Component line : Objects.requireNonNull(meta.lore())) {
				lore.add(generatorPlaceholder(line, generatorType, finalDropTier, finalValueTier).decoration(TextDecoration.ITALIC, false));
			}
			meta.lore(lore);
		}

		itemStack.setItemMeta(meta);
		return itemStack;
	}
	public ItemStack createHopper(){
		ItemStack itemStack = new ItemStack(Material.HOPPER);
		ItemMeta meta = itemStack.getItemMeta();

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		pdc.set(grindGens.chunkHopperItemKey(), PersistentDataType.BOOLEAN, true);

		meta.displayName(Component.text("Chunk Hopper", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

		List<Component> lore = new LinkedList<>();

		split(lore);
		lore.add(Component.text("Teleports all items to this", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
		lore.add(Component.text(" hopper as they are generated.", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
		split(lore);

		meta.lore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	public void split(List<Component> lore){
		Component component = Component.empty();
		boolean gray = true;
		for (int i = 0; i < 15; i++){
			gray = !gray;
			if (!gray){
				component = component.append(Component.text("-", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
			}else{
				component = component.append(Component.text("-", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
			}
		}
		lore.add(component);
	}
}
