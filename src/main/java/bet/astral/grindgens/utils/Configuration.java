/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.utils;

import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.Tier;
import bet.astral.grindgens.models.generators.GeneratorType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class Configuration {
	private final GlobalComponentManager globalComponentManager;
	public Configuration(GlobalComponentManager globalComponentManager){
		this.globalComponentManager = globalComponentManager;
	}


	public Set<GeneratorType> loadGeneratorsFolder(File folder){
		if (folder == null || !folder.exists() || !folder.isDirectory()){
			if (folder != null && folder.exists() && folder.isDirectory()){
				return loadGenerators(folder);
			}
			return Collections.emptySet();
		}
		File[] subFiles = folder.listFiles(pathname -> !pathname.isDirectory() && !pathname.getName().startsWith("-") && (pathname.getName().endsWith(".yml") || pathname.getName().endsWith(".yaml")));
		if (subFiles == null || subFiles.length == 0){
			return Collections.emptySet();
		}
		Set<GeneratorType> types = new HashSet<>();
		for (File file : subFiles){
			globalComponentManager.add(types, loadGenerators(file));
		}
		return types;
	}

	public Set<GeneratorType> loadGenerators(File file){
		if (file == null || !file.exists()){
			return Collections.emptySet();
		}
		if (file.getName().startsWith("-")){
			return Collections.emptySet();
		}
		Set<GeneratorType> generatorTypes = new HashSet<>();
		YamlConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
		Set<String> keys = fileConfiguration.getKeys(false);
		for (String key : keys){
			ConfigurationSection section = fileConfiguration.getConfigurationSection(key);
			GeneratorType loaded = loadGenerator(section, key);
			if (loaded == null){
				continue;
			}
			generatorTypes.add(loaded);
		}
		return generatorTypes;
	}

	public GeneratorType loadGenerator(ConfigurationSection configuration, String name){
		Bukkit.broadcastMessage("Loading generator type "+ name);

		final MiniMessage mM = MiniMessage.miniMessage();
		ItemStack blockItem = loadItem(configuration, "source.");
		int giveNewPlayers = configuration.getInt("first-join.give", 0);

		Component displayname = mM.deserialize(configuration.getString("upgrade-displayname", name));
		List<Tier> dropRateTiers = new LinkedList<>();
		List<Tier> valueTiers = new LinkedList<>();

		for (int i = 1; i < 7; i++){
			ConfigurationSection dropSection = configuration.getConfigurationSection("value-tiers."+i);
			ConfigurationSection valueSection = configuration.getConfigurationSection("drop-rate-tiers."+i);
			if (dropSection != null) {
				Bukkit.broadcastMessage("Found generator drop tier: "+ i);
				Tier tierDrop = loadTier(dropSection, i);
				dropRateTiers.add(tierDrop);
			}
			if (valueSection != null) {
				Bukkit.broadcastMessage("Found generator value tier: "+ i);
				Tier tierValue = loadTier(valueSection, i);
				valueTiers.add(tierValue);
			}
		}
		double cost = configuration.getDouble("cost", 1000);
		return new GeneratorType(name, blockItem,displayname, giveNewPlayers, cost, valueTiers, dropRateTiers);
	}

	private ItemStack loadItem(ConfigurationSection configuration, String prefix){
		final MiniMessage mM = MiniMessage.miniMessage();
		try {
			String mat = configuration.getString(prefix+"material");
			Material material = Material.valueOf(mat);
			ItemStack blockItem = new ItemStack(material);
			ItemMeta meta = blockItem.getItemMeta();

			String displayString = configuration.getString(prefix+"displayname");
			Component displayname = displayString != null ? mM.deserialize(displayString) : null;
			if (displayname!=null)
				meta.displayName(displayname);

			List<String> loreStringList = configuration.getStringList(prefix+"lore");
			if (!loreStringList.isEmpty()){
				List<Component> lore = new LinkedList<>();
				for (String line : loreStringList){
					if (line != null){
						lore.add(mM.deserialize(line));
					}
				}
				if (!lore.isEmpty()){
					meta.lore(lore);
				}
			}
			int customModelData = configuration.getInt(prefix+"custom-model", 0);
			meta.setCustomModelData(customModelData);

			blockItem.setItemMeta(meta);
			return blockItem;
		} catch (IllegalArgumentException e){
			e.printStackTrace();
			return null;
		}
	}

	public Tier loadTier(ConfigurationSection configuration, int tier){
		Double value = getDouble(configuration, "value", 1.0);
		Double cost =  getDouble(configuration, "cost", null);
		if (cost == null){
			return null;
		}
		// TODO
		return new Tier(cost ,value);
	}

	public Double getDouble(ConfigurationSection section, String name, Double defaultVal){
		if (section.isDouble(name)) {
			return section.getDouble(name);
		}
		if (section.isInt (name)) {
			return (double) section.getInt(name);
		}
		return defaultVal;
	}

}
