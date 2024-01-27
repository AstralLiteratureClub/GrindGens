package bet.astral.grindgens.models.component;

import bet.astral.grindgens.models.internals.Ticked;

public interface TickedComponent extends Ticked {
	boolean isLoaded();
	void setLoaded(boolean isLoaded);
	long lastLoaded();
}
