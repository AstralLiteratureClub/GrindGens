/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.internals;

import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.component.UpgradeableComponent;
import org.jetbrains.annotations.ApiStatus;

public interface ComponentLoader<G extends Component> {
	void load(G component);

	@ApiStatus.Internal
	default void unload(G component) {
		if (component instanceof UpgradeableComponent<?> upgradeableComponent) {
			upgradeableComponent.closeMenus();
		}
	}
}
