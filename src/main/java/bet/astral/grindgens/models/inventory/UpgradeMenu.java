package bet.astral.grindgens.models.inventory;

import bet.astral.grindgens.models.component.Component;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public interface UpgradeMenu<T extends Component> extends InventoryHolder, Cloneable, Lockable {
	@NotNull
	T component();
	@NotNull
	UpgradeMenu<T> clone();
}
