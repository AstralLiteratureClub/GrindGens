package bet.astral.grindgens.models.inventory;

import bet.astral.grindgens.models.hopper.ChunkHopper;
import org.jetbrains.annotations.NotNull;

public class UpgradeMenuChunkHopper extends AbstractUpgradeMenu<ChunkHopper> implements UpgradeMenu<ChunkHopper> {
	public UpgradeMenuChunkHopper(@NotNull ChunkHopper component) {
		super(component);
	}

	public UpgradeMenuChunkHopper(@NotNull ChunkHopper component, boolean locked) {
		super(component, locked);
	}

	@Override
	public @NotNull UpgradeMenuChunkHopper clone() {
		return new UpgradeMenuChunkHopper(this.component(), this.isLocked());
	}
}
