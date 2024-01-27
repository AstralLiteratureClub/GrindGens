package bet.astral.grindgens.models.internals;

import bet.astral.grindgens.models.GenPlayer;

public interface PlayerLoader {
	void load(GenPlayer genPlayer);
	void unload(GenPlayer genPlayer);
}
