/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.models.component;

import bet.astral.grindgens.models.internals.Ticked;

public interface TickedComponent extends Ticked {
	boolean isLoaded();
	void setLoaded(boolean isLoaded);
	long lastLoaded();
}
