package bet.astral.grindgens.models.internals;

import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.component.HologramComponent;
import bet.astral.grindgens.models.component.UpgradeableComponent;
import org.jetbrains.annotations.ApiStatus;

public interface ComponentLoader<G extends Component> {
	void load(G component);

	@ApiStatus.Internal
	default void unload(G component) {
		if (component instanceof HologramComponent hologramComponent) {
			hologramComponent.hologramUnregister();
		}
		if (component instanceof UpgradeableComponent<?> upgradeableComponent) {
			upgradeableComponent.closeMenus();
		}
	}
}
