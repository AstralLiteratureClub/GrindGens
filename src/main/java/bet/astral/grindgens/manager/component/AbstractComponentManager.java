package bet.astral.grindgens.manager.component;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.internals.ComponentLoader;
import bet.astral.grindgens.models.internals.SecureId;
import bet.astral.grindgens.models.internals.Ticked;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbstractComponentManager<G extends Component> implements ComponentLoader<G>, SubComponentManager<G>, SecureId, Ticked {
	private final GrindGens grindGens;
	private final ComponentManager componentManager;
	protected final Map<Location, G> components = new HashMap<>();

	public AbstractComponentManager(GrindGens grindGens, ComponentManager componentManager) {
		this.grindGens = grindGens;
		this.componentManager = componentManager;
	}

	public Random random() {
		return componentManager.random();
	}

	public GrindGens grindGens() {
		return grindGens;
	}

	public Map<Location, G> components() {
		return components;
	}

	@Override
	public ComponentManager superManager() {
		return componentManager;
	}

	@Override
	public void loadInternal(@NotNull G component) {
		components.put(component.location(), component);
		// TODO
	}

	@Override
	public void saveInternal(@NotNull G component) {
		loadInternal(component);
		// TODO
	}

	@Override
	public void unloadInternal(@NotNull G component) {
		components.remove(component.location());
		Bukkit.broadcastMessage("Removing component: " + component.type().name() + " from " + component.location().x() + "x " + component.location().y() + "y " + component.location().z() + "z " + component.location().getWorld().getName());
	}

	@Override
	public void deleteInternal(@NotNull G component) {
		unloadInternal(component);
		// TODO
	}

	@Override
	public <C extends Component> C getInternal(@NotNull Location location, Class<?> type) {
		G component = components.get(location);
		if (component == null){
			return null;
		}
		if (type.isInstance(component)){
			//noinspection unchecked
			return (C) component;
		}
		return null;
	}

	@Override
	public <C extends Component> List<@NotNull C> getInternal(@NotNull Chunk chunk, Class<C> type) {
		if (components.isEmpty()){
			return null;
		}
		List<C> chunkLocation = new LinkedList<>();
		for (G component : components.values()){
			if (!component.location().getChunk().equals(chunk)){
				continue;
			}
			if (type.isInstance(component)){
				//noinspection unchecked
				chunkLocation.add((C) component);
			}
		}
		return chunkLocation;
	}

	@Override
	public void load(G component) {
		loadInternal(component);
	}

	@Override
	public int requestSecureId() {
		return this.componentManager.requestSecureId();
	}

}