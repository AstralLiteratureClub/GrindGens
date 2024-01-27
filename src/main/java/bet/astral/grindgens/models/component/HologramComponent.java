package bet.astral.grindgens.models.component;

import bet.astral.grindgens.GrindGens;
import eu.decentsoftware.holograms.api.DecentHolograms;
import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramManager;
import org.jetbrains.annotations.ApiStatus;

public interface HologramComponent extends Component {
	DecentHolograms hologramAPI = DecentHologramsAPI.get();

	@ApiStatus.Internal
	default void hologramRegister() {
		if (!DecentHologramsAPI.isRunning()){
			GrindGens.getPlugin(GrindGens.class).getLogger().warning("DecentHolograms is not running! Try to fix the issue by restarting!");
			return;
		}
		Hologram hologram = hologram();
		hologram.setDefaultVisibleState(true);
		hologram.setAlwaysFacePlayer(true);

		HologramManager hologramManager = hologramAPI.getHologramManager();
		hologramManager.registerHologram(hologram);

		hologram.showAll();
		hologram.updateAll();
		hologram.setUpdateRange(150);
	}
	@ApiStatus.Internal
	default void hologramUnregister() {
		if (!DecentHologramsAPI.isRunning()){
			GrindGens.getPlugin(GrindGens.class).getLogger().warning("DecentHolograms is not running! Try to fix the issue by restarting!");
			return;
		}
		String name = hologramName();
		DecentHolograms decentHolograms = DecentHologramsAPI.get();
		HologramManager hologramManager = decentHolograms.getHologramManager();
		hologramManager.removeHologram(name);
	}

	Hologram hologram();
	String hologramName();
}
