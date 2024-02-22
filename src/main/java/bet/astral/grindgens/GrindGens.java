/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens;

import bet.astral.grindgens.commands.CommandManager;
import bet.astral.grindgens.database.ComponentDatabase;
import bet.astral.grindgens.database.PlayerDatabase;
import bet.astral.grindgens.listeners.*;
import bet.astral.grindgens.manager.ItemCreator;
import bet.astral.grindgens.manager.PlayerManager;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.internals.Ticked;
import bet.astral.grindgens.utils.Config;
import bet.astral.messagemanager.MessageManager;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static bet.astral.grindgens.utils.Resource.loadResourceAsTemp;
import static bet.astral.grindgens.utils.Resource.loadResourceToFile;

public final class GrindGens extends JavaPlugin implements Ticked  {
    private final NamespacedKey dropValueKey = new NamespacedKey("grindgens", "dropvalue");
    private final NamespacedKey dropOwnerKey = new NamespacedKey("grindgens", "dropowner");
    private final NamespacedKey generatorItemTypeKey = new NamespacedKey("grindgens", "generatorid");
    private final NamespacedKey generatorItemValueTierKey = new NamespacedKey("grindgens", "generatorvaluetier");
    private final NamespacedKey generatorItemDropTierKey = new NamespacedKey("grindgens", "generatordroptier");
    private final NamespacedKey chunkHopperItemKey = new NamespacedKey("grindgens", "chunkhopper");
    private final NamespacedKey menuActionName = new NamespacedKey("grindgens", "menu.type");
    private final NamespacedKey menuUpgradeTiers = new NamespacedKey("grindgens", "menu.upgrades");
    private final NamespacedKey replacementMenu = new NamespacedKey("grindgens", "replacement.inventory");
    private ComponentDatabase componentDatabase;
    private PlayerDatabase playerDatabase;
    private PlayerManager playerManager;
    private MessageManager<GrindGens> messageManager;
    private GlobalComponentManager componentGlobalComponentManager;
    private ItemCreator itemCreator;
    private VaultEconomy economy;
    private GlowingEntities glowingEntities;
    private GlowingBlocks glowingBlocks;
    public Config config;

    @Override
    public void onEnable() {
        uploadUploads();
        config = new Config(this);

        // Messenger
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        messageManager = new MessageManager<>(this, configuration, new HashMap<>(), "placeholders");
        // Message manager - End

        itemCreator = new ItemCreator(this);
        playerManager = new PlayerManager(this);
        componentGlobalComponentManager = new GlobalComponentManager(this);

        getServer().getScheduler().runTaskTimer(this, this::tick, 100, 1);

        try {
            new CommandManager(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        registerListener(new ComponentMenuListener(this));
        registerListener(new ComponentPlacementListener(this));
        registerListener(new ConnectionListener(this));

        economy = new VaultEconomy(this);

        playerDatabase = new PlayerDatabase(this);
        componentDatabase = new ComponentDatabase(this);

        getLogger().info("Hello! I am enabled now!");

        glowingEntities = new GlowingEntities(this);
        glowingBlocks = new GlowingBlocks(this);

    }

    @Override
    public void reloadConfig() {
        config.reload();
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return config.config;
    }

    private void uploadUploads(){
        String[] files = new String[]{
                "config|yml",
                "messages|yml",

                "cs/generator-finder|yml",

                "cs/gn/gens-1|yml",
                "cs/gn/gens-2|yml",
                "cs/gn/gens-3|yml",
                "cs/gn/-gens-4|yml",
        };
        for (String name : files){
            name = name.replace("dm/", "discord-messages/");
            name = name.replace("cs/", "components/");
            name = name.replace("gn/", "generators/");

            String[] split = name.split("\\|");
            String fileName = split[0];
            String ending = split[1];
            File fileTemp = loadResourceAsTemp("/upload/"+fileName, ending);
            File file = loadResourceToFile("/upload/"+fileName, ending, new File(getDataFolder(), fileName+"."+ending), true);
            if (ending.matches("(?i)yml") || ending.matches("(?i)yaml")){
                loadConfig(getConfig(fileTemp), getConfig(file), file);
            }
        }
    }

    private void loadConfig(FileConfiguration tempConfig, FileConfiguration config, File file){
        Set<String> keys = tempConfig.getKeys(false);
        for (String key : keys){
            addDefaults(key, tempConfig, config);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addDefaults(String key, Configuration tempConfig, Configuration config){
        List<String> comment = tempConfig.getComments(key);
        if (!comment.isEmpty() && config.getInlineComments(key).isEmpty()){
            config.setComments(key, comment);
        }
        comment = tempConfig.getInlineComments(key);
        if (!comment.isEmpty() && config.getInlineComments(key).isEmpty()){
            config.setInlineComments(key, comment);
        }
        if (tempConfig.get(key) instanceof MemorySection section){
            for (String k : section.getKeys(false)) {
                addDefaults(k, tempConfig, config);
            }
        }
    }

    private FileConfiguration getConfig(File file){
        return YamlConfiguration.loadConfiguration(file);
    }


    @Override
    public void onDisable() {
        glowingEntities.disable();

        componentDatabase.onDisable();
        playerDatabase.onDisable();
        getLogger().info("Hello! I am disabled now!");
    }

    public GlowingEntities glowingEntities() {
        return glowingEntities;
    }

    public GlowingBlocks glowingBlocks() {
        return glowingBlocks;
    }

    public void registerListener(Listener listener){
        getServer().getPluginManager().registerEvents(listener, this);
    }


    /**
     * Returns the value key for dropped items to retrieve from dropped items
     * @return value key
     */
    public NamespacedKey dropValueKey() {
        return dropValueKey;
    }

    /**
     * Returns the owner key for dropped items to retrieve the owner
     * @return owner key
     */
    public NamespacedKey dropOwnerKey() {
        return dropOwnerKey;
    }

    /**
     * Returns the key used in item stacks to store what generator it is when placed
     * @return key
     */
    public NamespacedKey generatorItemTypeKey() {
        return generatorItemTypeKey;
    }

    public NamespacedKey generatorItemValueTierKey() {
        return generatorItemValueTierKey;
    }

    public NamespacedKey generatorItemDropTierKey() {
        return generatorItemDropTierKey;
    }

    /**
     * Returns the key to handle chunk hoppers when inside an inventory
     * @return key
     */
    public NamespacedKey chunkHopperItemKey() {
        return chunkHopperItemKey;
    }

    public NamespacedKey menuActionName() {
        return menuActionName;
    }

    public NamespacedKey menuUpgradeTiers() {
        return menuUpgradeTiers;
    }

    public NamespacedKey replacementMenu() {
        return replacementMenu;
    }

    public NamespacedKey replacementMenuSlot(int slot){
        return new NamespacedKey("grindgens", String.valueOf(slot));
    }

    public PlayerManager playerManager() {
        return playerManager;
    }


    public GlobalComponentManager globalComponentManager(){
        return componentGlobalComponentManager;
    }

    public ItemCreator itemCreator() {
        return itemCreator;
    }

    public VaultEconomy economy() {
        return economy;
    }

    @Override
    public boolean canTick() {
        return true;
    }

    @Override
    public void tick() throws IllegalStateException {
        componentGlobalComponentManager.tick();
    }


    public MessageManager<GrindGens> messenger(){
        return messageManager;
    }

	public ComponentDatabase componentDatabase() {
        return componentDatabase;
	}
    public PlayerDatabase playerDatabase() {
        return playerDatabase;
    }
}
