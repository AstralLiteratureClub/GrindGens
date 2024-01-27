package bet.astral.grindgens.manager.component;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.hopper.ChunkHopper;

public class ChunkHopperManager<G extends ChunkHopper> extends AbstractComponentManager<G> {
	public ChunkHopperManager(GrindGens grindGens, ComponentManager componentManager) {
		super(grindGens, componentManager);
	}

	@Override
	public boolean canTick() {
		return false;
	}

	@Override
	public void tick() throws IllegalStateException {
		// NOTHING
	}

	@Override
	public void load(GenPlayer genPlayer) {

	}

	@Override
	public void unload(GenPlayer genPlayer) {

	}
}