/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.utils;

import bet.astral.grindgens.GrindGens;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
	private final File file;
	public FileConfiguration config;
	private final GrindGens grindGens;
	public int maxPlayerGenerators;
	public int maxChunkGenerators;

	public Config(GrindGens grindGens){
		this.grindGens = grindGens;
		file = new File(grindGens.getDataFolder(), "config.yml");
		reload();
	}
	private void setIfNotAbsent(String key, Object value, Class<?>... canBe){
		if (config.get(key) == null){
			config.set(key, value);
			return;
		}
		Object object = config.get(key);
		assert object != null;
		boolean found = false;
		for (Class<?> clazz : canBe){
			if (object.getClass().getName().equals(clazz.getName())){
				found = true;
			}
		}
		if (!found){
			config.set(key, value);
		}
	}

	public void reload(){
		config = YamlConfiguration.loadConfiguration(file);

		setIfNotAbsent("player.max-generators", 100, Integer.class);
		setIfNotAbsent("chunk.max-generators", 100, Integer.class);

		maxPlayerGenerators = config.getInt("player.max-generators", 100);
		maxChunkGenerators = config.getInt("chunk.max-generators", maxPlayerGenerators);
	}
}
