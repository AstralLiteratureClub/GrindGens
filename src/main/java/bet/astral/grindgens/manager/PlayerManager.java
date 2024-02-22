/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.manager;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.database.PlayerDatabase;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.generators.GeneratorType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerManager {
	public static GenPlayer emptyPlayer;
	private static final HashMap<UUID, GenPlayer> playerManager = new HashMap<>();
	public Set<GenPlayer> requestedSaves = new HashSet<>();
	private final GrindGens grindGens;

	public PlayerManager(GrindGens grindGens) {
		this.grindGens = grindGens;
		emptyPlayer = new GenPlayer(grindGens);
		grindGens.getServer().getScheduler().runTaskTimerAsynchronously(grindGens, ()->{
			Set<GenPlayer> saved = new HashSet<>(requestedSaves);
			requestedSaves.clear();
			for (GenPlayer player : saved){
				grindGens.playerDatabase().save(player);
			}
		}, 20, 20);
	}

	public void load(Player player){
		player.sendRichMessage("<red>Loading your profile...");
		PlayerDatabase playerDatabase = grindGens.playerDatabase();
		playerDatabase.load(player).thenAcceptAsync(genPlayer-> {
					if (genPlayer == null) {
						genPlayer = new GenPlayer(grindGens, player);
						genPlayer.setMaxGenerators(grindGens.config.maxPlayerGenerators);
						genPlayer.setBitcoin(0.0D);
						GlobalComponentManager globalComponentManager = grindGens.globalComponentManager();
						ItemCreator itemCreator = grindGens.itemCreator();
						for (GeneratorType type : globalComponentManager.generatorTypes()){
							if (type.amountGiven()>=1){
								ItemStack generator = itemCreator.createGenerator(type, 0, 0);
								generator.setAmount(type.amountGiven());
								player.getInventory().addItem(generator);
							}
						}
						genPlayer.setNew(true);
					}
					playerManager.put(genPlayer.uuid(), genPlayer);
					player.sendRichMessage("<green>Loaded your generator profile.");
			}
		);
	}

	public void unload(Player player){
		grindGens.playerDatabase().save(playerManager.get(player.getUniqueId()), genPlayer->{
			for (Component component : genPlayer.components()){
				grindGens.globalComponentManager().unload(component);
			}
			// Everything is saved to the database when action is dealt
			playerManager.remove(player.getUniqueId());
		});
	}

	public GenPlayer asGenPlayer(Player player){
		return playerManager.get(player.getUniqueId());
	}

	public GenPlayer emptyGenPlayer() {
		return emptyPlayer;
	}
}
