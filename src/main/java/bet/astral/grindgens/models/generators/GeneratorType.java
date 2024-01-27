package bet.astral.grindgens.models.generators;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public final class GeneratorType {
	private final String name;
	private final Material groundMaterial;
	private final ItemStack dropMaterial;
	private final Component displayname;
	private final GeneratorTier defaultTier;
	private final List<GeneratorTier> generatorTiers;
	private final int amountGiven;
	private final double cost;

	public GeneratorType(String name, Material groundMaterial, Material dropMaterial, Component displayname, int newPlayerReceive, double cost, GeneratorTier defaultTier, GeneratorTier... tiers) {
		this.name = name;
		this.groundMaterial = groundMaterial;
		this.dropMaterial = new ItemStack(dropMaterial);
		this.displayname = displayname;
		this.defaultTier = defaultTier;
		this.amountGiven = newPlayerReceive;
		this.cost = cost;
		this.generatorTiers = List.of(tiers);
	}
	public String name() {
		return name;
	}

	@Contract(pure = true)
	public Material groundMaterial() {
		return groundMaterial;
	}

	@Contract(pure = true)
	public ItemStack dropMaterial() {
		return dropMaterial;
	}

	@Contract(pure = true)
	public Component displayname(){
		return displayname;
	}

	public int defaultDropRate() {
		return defaultTier.dropRate();
	}

	@Contract(pure = true)
	public int maxTier() {
		return generatorTiers.size()+1;
	}

	public double defaultValue() {
		return defaultTier.value();
	}

	@Contract(pure = true)
	public GeneratorTier defaultTier() {
		return defaultTier;
	}

	public @NotNull List<GeneratorTier> generatorTiers() {
		List<GeneratorTier> tiers = new LinkedList<>(generatorTiers);
		tiers.add(0, defaultTier);
		return tiers;
	}

	public GeneratorTier getTier(int tier){
		if (tier < 0){
			tier = 0;
		} else if (tier > maxTier()-1){
			return null;
//			tier = maxTier();
		}

		if (tier == 0){
			return defaultTier;
		}
		tier--;
		return generatorTiers.get(tier);
	}

	public int amountGiven() {
		return amountGiven;
	}

	public double cost() {
		return cost;
	}
}
