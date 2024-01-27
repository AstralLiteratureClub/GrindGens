package bet.astral.grindgens.models;

import bet.astral.grindgens.models.internals.Ticked;

public class Drop implements Ticked {
	private final int generatedBy;
	private double value;


	public Drop(int generatedBy) {
		this.generatedBy = generatedBy;
	}


	public Drop setValue(double value) {
		this.value = value;
		return this;
	}


	public double value() {
		return value;
	}

	public int generatedBy() {
		return generatedBy;
	}

	@Override
	public boolean canTick() {
		return false;
	}

	@Override
	public void tick() throws IllegalStateException {
	}
}
