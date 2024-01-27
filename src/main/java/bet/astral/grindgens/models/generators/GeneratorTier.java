package bet.astral.grindgens.models.generators;

public class GeneratorTier {
	private int tier;
	private double cost;
	private int dropRate;
	private double value;

	public GeneratorTier(int tier, double cost, int dropRate, double value) {
		this.tier = tier;
		this.dropRate = dropRate;
		this.value = value;
	}

	public int tier() {
		return tier;
	}

	public double cost() {
		return cost;
	}

	public double value() {
		return value;
	}

	public int dropRate() {
		return dropRate;
	}
}
