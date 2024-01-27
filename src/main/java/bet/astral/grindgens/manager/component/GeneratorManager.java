package bet.astral.grindgens.manager.component;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.generators.Generator;
import org.bukkit.Location;

import java.util.LinkedList;
import java.util.List;

public class GeneratorManager<G extends Generator> extends AbstractComponentManager<G>{
	public GeneratorManager(GrindGens grindGens, ComponentManager componentManager) {
		super(grindGens, componentManager);
	}

	@Override
	public boolean canTick() {
		return !components.isEmpty();
	}

	@Override
	public void tick() throws IllegalStateException {
		if (!canTick()){
			throw new IllegalStateException("GeneratorManager is not able to tick currently!");
		}
		List<Location> locations = new LinkedList<>();
		components.forEach((location, component)->{
			if (!component.isLoaded()){
				locations.add(location);
			} else {
				component.tick();
			}
		});
		for (Location location : locations) {
			this.components.remove(location);
		}
	}

	@Override
	public void load(GenPlayer genPlayer) {

	}

	@Override
	public void unload(GenPlayer genPlayer) {

	}
}

