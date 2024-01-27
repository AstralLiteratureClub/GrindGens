package bet.astral.grindgens.models;

import bet.astral.grindgens.models.generators.Generator;
import bet.astral.grindgens.models.hopper.ChunkHopper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GenPlayer {
	private final UUID uuid;
	private final List<Generator> generators;
	private final List<ChunkHopper> chunkHoppers;
	private int maxGenerators = 25;

	public GenPlayer(Player player) {
		this.uuid = player.getUniqueId();
		this.generators = new LinkedList<>();
		this.chunkHoppers = new LinkedList<>();
	}

	@ApiStatus.Internal
	public void addGenerator(@NotNull Generator generator){
		this.generators.add(generator);
	}

	@ApiStatus.Internal
	public void addGenerators(@NotNull Generator... generators){
		this.generators.addAll(List.of(generators));
	}

	@ApiStatus.Internal
	public void addChunkHopper(@NotNull ChunkHopper chunkHopper){
		chunkHoppers.add(chunkHopper);
	}
	@ApiStatus.Internal
	public void addChunkHoppers(@NotNull ChunkHopper... chunkHoppers){
		for (ChunkHopper chunkHopper : chunkHoppers){
			addChunkHopper(chunkHopper);
		}
	}

	public List<Generator> generators() {
		return generators;
	}

	public UUID uuid() {
		return uuid;
	}

	public List<ChunkHopper> chunkHoppers() {
		return chunkHoppers;
	}

	public int maxGenerators() {
		return maxGenerators;
	}

	public GenPlayer setMaxGenerators(int maxGenerators) {
		this.maxGenerators = maxGenerators;
		return this;
	}
}
