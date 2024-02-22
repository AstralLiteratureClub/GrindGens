/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.manager.component;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.generators.GeneratorType;
import bet.astral.grindgens.models.internals.ComponentLoader;
import bet.astral.grindgens.models.internals.SecureId;
import bet.astral.grindgens.models.internals.Ticked;
import bet.astral.grindgens.utils.Configuration;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class GlobalComponentManager implements Ticked, SecureId, ComponentLoader<Component>{
	private final GrindGens grindGens;
	private final Random random = new Random((System.nanoTime()*System.nanoTime())/System.currentTimeMillis());
	private final Map<Location, Component> locationComponents = new HashMap<>();
	private final Map<Chunk, Set<Component>> chunkComponents = new HashMap<>();
	private final Set<GeneratorType> generatorTypes = new HashSet<>();
	private final Set<Component> componentsToSave = new HashSet<>();

	public GlobalComponentManager(GrindGens grindGens) {
		this.grindGens = grindGens;

		Configuration configuration = new Configuration(this);
		YamlConfiguration generatorFinder = YamlConfiguration.loadConfiguration(new File(grindGens.getDataFolder(), "/components/generator-finder.yml"));
		List<String> directions = generatorFinder.getStringList("generators");
		grindGens.getLogger().info("Preparing to load generators!");
		for (String dir : directions){
			dir = dir.replaceFirst("./", "");
			dir = dir.replaceFirst("//", "");
			dir = dir.replaceFirst("\\\\", "");
			File file = new File(grindGens.getDataFolder()+ File.separator, dir);
			grindGens.getLogger().info("Found generator directory/file: "+ dir +" | Is created: "+ file.exists() + " | Is directory: "+ file.isDirectory());
			add(generatorTypes, configuration.loadGeneratorsFolder(file));
			// This can check that if the file is a folder or not and just detect .yml files
			grindGens.getLogger().info("Loaded "+ generatorTypes.size()+" generators");
		}
		grindGens.getLogger().info("Loaded all of the generators! Found "+ generatorTypes.size() + "!");


		Runnable runnable = () -> {
			Set<Component> components = new HashSet<>(componentsToSave);
			componentsToSave.clear();
			for (Component component : components){
				if (component.deleted()) {
					continue;
				}
				grindGens.componentDatabase().saveComponent(component);
			}
		};

		grindGens.getServer().getScheduler().runTaskTimerAsynchronously(grindGens, runnable, 100, 20);
	}
	public void add(Set<GeneratorType> types, Set<GeneratorType> newTypes){
		for (GeneratorType type : types){
			newTypes.removeIf(genType->genType.name().equalsIgnoreCase(type.name()));
		}
		types.addAll(newTypes);
	}

	public GeneratorType getGeneratorType(String generatorStringType) {
		return !generatorTypes.isEmpty() ? generatorTypes.stream().filter(type->type.name().equalsIgnoreCase(generatorStringType)).findAny().orElse(null) : null;
	}
	public Set<GeneratorType> generatorTypes(){
		return ImmutableSet.copyOf(generatorTypes);
	}

	public @Nullable Component get(@NotNull Location location) {
		return locationComponents.get(location);
	}

	public @Nullable Set<@NotNull Component> get(@NotNull Chunk chunk) {
		return chunkComponents.get(chunk);
	}

	public void save(@NotNull Component component) {
		Location location = component.location();
		Block block = location.getBlock();
		if (block.getType() != component.blockMaterial()) {
			grindGens.getLogger().severe("Component of " + component.type().name() + " at " + location.x() + ", " + location.y() + ", " + location.z() + ", " + location.getWorld().getName() + " is not the correct block type of " + component.blockMaterial().name() + " but instead " + block.getType().name() + ".\nThis component will not be used and will be removed from player's components!");
			GenPlayer player = component.genPlayer();
			player.removeComponent(component);
			player.requestSave();
			return;
		}

		load(component);

//		grindGens.getLogger().info("Saving component: "+ component.type().name()+"#"+component.id());
		componentsToSave.add(component);
//		grindGens.getLogger().info("Saved component: "+ component.type().name()+"#"+component.id());

		GenPlayer genPlayer = component.genPlayer();
		genPlayer.addComponent(component);
		genPlayer.requestSave();
	}

	public void delete(@NotNull Component component) {
		unload(component);
		grindGens.getLogger().info("Removing component: "+ component.type().name());

		grindGens.componentDatabase().delete(component);
		component.setDeleted(true);

		GenPlayer genPlayer = component.genPlayer();
		genPlayer.removeComponent(component);
		genPlayer.requestSave();
		return;
	}

	public void tickIfCan(@NotNull Component component) {
		try {
			if (component.canTick()) {
				component.tick();
			}
		} catch (ClassCastException e) {
			throw new RuntimeException(e);
		}
	}
	public Random random() {
		return random;
	}

	@Override
	public void load(@NotNull Component component) {

		Location location = component.location();
		Block block = location.getBlock();
		if (block.getType() != component.blockMaterial()){
			grindGens.getLogger().severe("Component of " + component.type().name() + " at " + location.x()+ ", "+ location.y()+ ", "+ location.z() + ", "+ location.getWorld().getName()+ " is not the correct block type of " + component.blockMaterial().name() + " but instead " + block.getType().name()+".\nThis component will not be used and will be removed from player's components!");
			delete(component); // Make sure players and other components are in sync
			return;
		}

		chunkComponents.putIfAbsent(component.location().getChunk(), new HashSet<>());
		chunkComponents.get(component.location().getChunk()).add(component);
		locationComponents.put(component.location(), component);
	}

	@Override
	public void unload(Component component) {
		chunkComponents.get(component.location().getChunk()).remove(component);
		if (chunkComponents.get(component.location().getChunk()) != null && chunkComponents.get(component.location().getChunk()).isEmpty()) {
			chunkComponents.remove(component.location().getChunk());
		}
		locationComponents.remove(component.location());
	}

	@Override
	public int requestSecureId() {
		return random.nextInt(-32766, 32766);
	}

	@Override
	public boolean canTick() {
		return true;
	}

	@Override
	public void tick() throws IllegalStateException {
		for (Component component : locationComponents.values()){
			tickIfCan(component);
		}
	}

	public GrindGens grindGens() {
		return grindGens;
	}
}
