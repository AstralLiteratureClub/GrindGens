package bet.astral.grindgens.manager;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.Drop;
import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.generators.GeneratorTier;
import bet.astral.grindgens.models.generators.GeneratorType;
import bet.astral.grindgens.models.internals.Ticked;
import org.bukkit.entity.Item;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class DropManager implements Ticked {
	private final GrindGens grindGens;

	public DropManager(GrindGens grindGens){
		this.grindGens = grindGens;
	}

	public void create(Generator generator, Item item){
		PersistentDataContainer pdc = item.getPersistentDataContainer();
		GeneratorType type = generator.generatorType();
		GeneratorTier tier = type.getTier(generator.tier());
		if (tier == null){
			return;
		}
		double value = tier.value();

		pdc.set(grindGens.dropOwnerKey(), PersistentDataType.INTEGER, generator.id());
		pdc.set(grindGens.dropValueKey(), PersistentDataType.DOUBLE, value);
	}
	@Nullable
	public Drop asDrop(Item item){
		PersistentDataContainer pdc = item.getPersistentDataContainer();

		if (pdc.get(grindGens.dropOwnerKey(), PersistentDataType.INTEGER) == null){
			return null;
		}

		Integer id = pdc.get(grindGens.dropOwnerKey(), PersistentDataType.INTEGER);
		if (id == null){ // IDE was annoying
			return null;
		}

		if (pdc.get(grindGens.dropValueKey(), PersistentDataType.DOUBLE) == null){
			return null;
		}
		Double value = pdc.get(grindGens.dropValueKey(), PersistentDataType.DOUBLE);
		if (value == null){
			return null;
		}

		Drop drop = new Drop(id);
		drop.setValue(value);

		return drop;
	}

	public GrindGens grindGens() {
		return grindGens;
	}

	@Override
	public boolean canTick() {
		return false;
	}

	@Override
	public void tick() throws IllegalStateException {

	}
}
