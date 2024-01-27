package bet.astral.grindgens.models.internals;

public interface Ticked {
	/**
	 * Returns true if this class can tick
	 * @return can tick
	 */
	boolean canTick();

	/**
	 * Ticks this class
	 * @throws IllegalStateException if the class cannot tick
	 */
	void tick() throws IllegalStateException;

}
