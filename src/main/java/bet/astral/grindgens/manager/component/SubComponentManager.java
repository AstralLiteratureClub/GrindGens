package bet.astral.grindgens.manager.component;

import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.internals.PlayerLoader;
import bet.astral.grindgens.models.internals.Ticked;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SubComponentManager<G extends Component> extends Ticked, PlayerLoader {
	ComponentManager superManager();
	void loadInternal(@NotNull G component);
	void saveInternal(@NotNull G component);
	void unloadInternal(@NotNull G component);
	void deleteInternal(@NotNull G component);
	@Nullable
	<C extends Component> C getInternal(@NotNull Location location, Class<?> type);
	@Nullable
	<C extends Component> List<@NotNull C> getInternal(@NotNull Chunk chunk, Class<C> type);

	default void tickIfCan(@NotNull G component) {
		if (!canTick()) {
			return;
		}
		component.tick();
	}

	default void loadInternalDef(Component component) {
		//noinspection unchecked
		loadInternal((G) component);
	}
	default void unloadInternalDef(Component component) {
		//noinspection unchecked
		unloadInternal((G) component);
	}
}
