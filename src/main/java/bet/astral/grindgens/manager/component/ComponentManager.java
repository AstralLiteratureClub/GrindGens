package bet.astral.grindgens.manager.component;

import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.internals.ComponentLoader;
import bet.astral.grindgens.models.internals.SecureId;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public interface ComponentManager extends ComponentLoader<Component>, SecureId {
	@Nullable
	<C extends Component> C get(@NotNull Location location, @NotNull Class<C> type);

	@Nullable
	<C extends Component> List<@NotNull C> get(@NotNull Chunk chunk, @NotNull Class<C> type);

	void save(@NotNull Component component);
	void delete(@NotNull Component component);

	void tickIfCan(@NotNull SubComponentManager<?> subComponentManager);

	Random random();
}
