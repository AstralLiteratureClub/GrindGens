package bet.astral.grindgens.models.component;

import bet.astral.grindgens.models.GenPlayer;
import org.bukkit.Location;
import org.bukkit.Material;

public interface Component extends TickedComponent {
	Location hologramLocation();
	ComponentType type();
	Location location();
	GenPlayer genPlayer();
	int id();
	Material blockMaterial();
}
