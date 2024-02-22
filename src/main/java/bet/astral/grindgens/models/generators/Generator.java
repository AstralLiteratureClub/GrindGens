/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.generators;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.Tier;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.component.AbstractComponent;
import bet.astral.grindgens.models.component.ComponentType;
import bet.astral.grindgens.models.component.DataComponent;
import bet.astral.grindgens.models.component.MinableComponent;
import bet.astral.grindgens.models.component.UpgradeableComponent;
import bet.astral.grindgens.models.inventory.UpgradeMenu;
import bet.astral.grindgens.models.inventory.upgrade.UpgradeMenuGenerator;
import com.google.gson.*;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Generator extends AbstractComponent implements UpgradeableComponent<Generator>, MinableComponent, DataComponent {
	@ApiStatus.Internal
	public static ComponentType componentType(){
		return ComponentType.GENERATOR;
	}
	private final GeneratorType type;
	private int valueTier;
	private int dropRateTier;
	private UpgradeMenu<Generator> upgradeMenu;
	private UpgradeMenu<Generator> lockedUpgradeMenu;
	private int ticks;

	public Generator(GrindGens grindGens, GenPlayer genPlayer, GeneratorType type, Location location, int id, int dropRateTier, int valueTier) {
		super(grindGens, ComponentType.GENERATOR, genPlayer, location, type.sourceItem().getType(), id, null);
		this.type = type;
		this.dropRateTier = dropRateTier;
		this.valueTier = valueTier;
	}

	/**
	 * Used for reflections by the component database
	 */
	@ApiStatus.Internal
	protected Generator(GrindGens gens, ComponentType type, GenPlayer genPlayer, Location location, Material material, int id, String data) {
		super(gens, type, genPlayer, location, material, id, data);
		try {
			Type typeToken = new TypeToken<Map<String, Object>>() {}.getType();
			Map<String, Object> dataMap = new Gson().fromJson(data, typeToken);
			String typeStr = (String) dataMap.get("type");

			this.dropRateTier = ((Double) dataMap.get("dropRateTier")).intValue();
			this.valueTier = ((Double) dataMap.get("valueTier")).intValue();

			this.type = gens.globalComponentManager().getGeneratorType(typeStr);

			ticks = 10;
			setLoaded(true);
			tick();
		} catch (JsonSyntaxException | IllegalStateException e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Generator(GrindGens grindGens, GenPlayer genPlayer, GeneratorType type, GlobalComponentManager generatorManager, Location location){
		super(grindGens, ComponentType.GENERATOR, genPlayer, location, type.sourceItem().getType(), generatorManager.requestSecureId());
		this.type = type;
		this.dropRateTier = 1;
		this.valueTier = 1;
		this.ticks = 10;
	}


	@Override
	public boolean canTick() {
		return isLoaded();
	}

	@Override
	public void tick() throws IllegalStateException {
		if (!canTick()){
			throw new IllegalStateException("Cannot tick generator while it's not loaded! "+ id());
		}
		ticks-=1;
		if (ticks == -1){
			ticks = (int) currentTier(UpgradeMenuGenerator.Path.DROP_RATE).value();
		} else{
			return;
		}

		Tier tier = currentTier(UpgradeMenuGenerator.Path.VALUE);
		double bitcoins = genPlayer().getBitcoins();
		double value = tier.value();

		genPlayer().setBitcoin(bitcoins+value);
		genPlayer().requestSave();
	}

	public GrindGens grindGens() {
		return grindGens;
	}

	public GeneratorType generatorType() {
		return type;
	}

	@Override
	public void open(Player player) {
		if (hasUpgradePermission(player)){
			player.openInventory(upgradeMenu().getInventory());
		} else {
			player.openInventory(lockedMenu().getInventory());
		}
	}

	@Override
	public void closeMenus() {
		if (upgradeMenu == null){
			return;
		}
		upgradeMenu.getInventory().close();
		lockedUpgradeMenu.getInventory().close();
	}

	public int ticks() {
		return ticks;
	}


	@Override
	public UpgradeMenu<Generator> upgradeMenu() {
		if (upgradeMenu == null){
			upgradeMenu = new UpgradeMenuGenerator(this);
		}
		upgradeMenu.unlock();
		return upgradeMenu;
	}

	@Override
	public UpgradeMenu<Generator> lockedMenu() {
		if (this.lockedUpgradeMenu == null){
			lockedUpgradeMenu = new UpgradeMenuGenerator(this);
		}
		lockedUpgradeMenu.lock();
		return lockedUpgradeMenu;
	}

	@Override
	public boolean hasUpgradePermission(Player player) {
		return true;
	}

	@Override
	public ItemStack getBlock() {
		return grindGens.itemCreator().createGenerator(type, dropRateTier, valueTier);
	}

	public int valueTier() {
		return valueTier;
	}

	public int dropRateTier() {
		return dropRateTier;
	}

	public void addValueTier(){
		valueTier++;
	}
	public void addDropRateTier(){
		dropRateTier++;
	}

	public void addTier(@NotNull UpgradeMenuGenerator.Path path){
		switch (path){
			case VALUE -> valueTier++;
			case DROP_RATE -> dropRateTier++;
			default -> throw new RuntimeException("Unknown upgrade path!");
		}
	}

	@Nullable
	public Tier nextTier(UpgradeMenuGenerator.Path path){
		switch (path){
			case VALUE -> {
				if (valueTier+1>=type.maxValueTier()){
					return null;
				}
				return type.valueTier(valueTier+1);
			}
			case DROP_RATE -> {
				if (dropRateTier+1>=type.maxDropRateTier()){
					return null;
				}
				return type.dropRateTier(dropRateTier+1);
			}
			default -> {
				return null;
			}
		}
	}
	@Nullable
	public Tier currentTier(UpgradeMenuGenerator.Path path){
		switch (path){
			case VALUE -> {
				return type.valueTier(valueTier);
			}
			case DROP_RATE -> {
				return type.dropRateTier(dropRateTier);
			}
			default -> {
				return null;
			}
		}
	}

	@Override
	public Generator clone() {
		return new Generator(grindGens(), genPlayer(), generatorType(), location(), grindGens.globalComponentManager().requestSecureId(), dropRateTier, valueTier);
	}


	@SuppressWarnings("UnnecessaryBoxing")
	@Override
	public JsonElement dataAsJson() throws JsonParseException {
		Map<String, Object> data = new HashMap<>();
		data.put("type", generatorType().name());
		data.put("dropRateTier", Integer.valueOf(dropRateTier));
		data.put("valueTier", Integer.valueOf(valueTier));
		return new Gson().toJsonTree(data);
	}
}
