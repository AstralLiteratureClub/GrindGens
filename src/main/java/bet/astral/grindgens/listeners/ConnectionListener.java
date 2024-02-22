/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.listeners;

import bet.astral.grindgens.GrindGens;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {
	private final GrindGens grindGens;

	public ConnectionListener(GrindGens grindGens) {
		this.grindGens = grindGens;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		Bukkit.getServer().getScheduler().runTaskLater(grindGens, ()->this.grindGens.playerManager().load(event.getPlayer()), 5);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		this.grindGens.playerManager().unload(event.getPlayer());
	}
}
