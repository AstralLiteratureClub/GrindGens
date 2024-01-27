package bet.astral.grindgens.models.inventory;

public interface Lockable {
	void lock();
	void unlock();
	boolean isLocked();
}
