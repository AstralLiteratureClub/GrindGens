package bet.astral.grindgens.listeners;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.manager.ItemCreator;
import bet.astral.grindgens.manager.component.GlobalComponentManager;
import bet.astral.grindgens.models.generators.GeneratorType;
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
		this.grindGens.playerManager().load(event.getPlayer());
		GlobalComponentManager globalComponentManager = grindGens.globalComponentManager();
		// TODO make this only for new players
		ItemCreator itemCreator = grindGens.itemCreator();

		for (GeneratorType type : globalComponentManager.generatorTypes()){
			if (type.amountGiven()>0){
				itemCreator.createGenerator(type, 0);
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		this.grindGens.playerManager().unload(event.getPlayer());
	}
}
