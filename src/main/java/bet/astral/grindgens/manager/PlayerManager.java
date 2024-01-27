package bet.astral.grindgens.manager;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {
	private static final HashMap<UUID, GenPlayer> playerManager = new HashMap<>();
	private final GrindGens grindGens;

	public PlayerManager(GrindGens grindGens) {
		this.grindGens = grindGens;
	}

	public void load(Player player){
		GenPlayer genPlayer = new GenPlayer(player);
		playerManager.put(player.getUniqueId(), genPlayer);
	}

	public void unload(Player player){
		playerManager.remove(player.getUniqueId());
	}

	public GenPlayer asGenPlayer(Player player){
		return playerManager.get(player.getUniqueId());
	}
}
