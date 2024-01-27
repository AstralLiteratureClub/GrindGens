package bet.astral.grindgens.utils;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.generators.GeneratorTier;
import bet.astral.grindgens.models.generators.GeneratorType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class Configuration {

	public Configuration(){
	}

	public Set<GeneratorType> loadGenerators(File file){
		if (file == null || !file.exists()){
			return Collections.emptySet();
		}
		Set<GeneratorType> generatorTypes = new HashSet<>();
		YamlConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
		Set<String> keys = fileConfiguration.getKeys(false);
		for (String key : keys){
			GeneratorType loaded = loadGenerator(fileConfiguration, key);
			generatorTypes.add(loaded);
		}
		return generatorTypes;
	}

	public GeneratorType loadGenerator(FileConfiguration configuration, String key){
		int give = configuration.getInt(key+".give", 0);
		double cost = configuration.isDouble(key+".price") ? configuration.getDouble(key+".price") : ((double) (configuration.isInt(key+".price") ? configuration.getInt(key+".price") : 0));
		Component displayname = MiniMessage.miniMessage().deserialize(configuration.getString(key+".displayname", key));
		Material block = Material.valueOf(configuration.getString(key+".block"));
		Material drop = Material.valueOf(configuration.getString(key+".drop"));
		List<GeneratorTier> tiers = new ArrayList<>();
		GeneratorTier defaultTier = loadTier(configuration, key+".tiers.1", 1);
		for (int i = 2; i < 7; i++){
			if (!configuration.isSet(key+".tiers."+i) || !configuration.isConfigurationSection(key+".tiers."+i)){
				break;
			}
			GeneratorTier tier = loadTier(configuration, key+".tiers."+i, i);
			tiers.add(tier);
		}
		return new GeneratorType(key, block, drop, displayname, give, cost, defaultTier, tiers.toArray(GeneratorTier[]::new));
	}

	public GeneratorTier loadTier(FileConfiguration configuration, String key, int tier){
		double cost = configuration.isDouble(key+".price") ? configuration.getDouble(key+".price") : ((double) (configuration.isInt(key+".price") ? configuration.getInt(key+".price") : 0));;
		int dropTicks = configuration.getInt(key+".drop", 30);
		double value = configuration.isDouble(key+".value") ? configuration.getDouble(key+".value") : ((double) (configuration.isInt(key+".value") ? configuration.getInt(key+".value") : 0));
		return new GeneratorTier(tier, cost, dropTicks, value);
	}
}
