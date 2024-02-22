/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.database;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.generators.Generator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PlayerDatabase {
	private static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS players (uniqueId VARCHAR(36) PRIMARY KEY, bitcoin DOUBLE, generators JSON, maxGenerators INT);";
	private static final String GET_BY_ID_QUERY = "SELECT * FROM players WHERE uniqueId = ?";
	private static final String UPDATE_QUERY = "UPDATE players SET bitcoin = ?, generators = ?, maxGenerators = ? WHERE uniqueId = ?";
	private static final String INSERT_QUERY = "INSERT INTO players (uniqueId, bitcoin, generators, maxGenerators) VALUES (?, ?, ?, ?);";
	private final Connection connection;
	private final GrindGens grindGens;

	public PlayerDatabase(GrindGens grindGens) {
		this.grindGens = grindGens;
		connection = connect();
		createTable();
	}

	public void onDisable() {
		if (connection != null) {
			try {
				if (!connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}


	private void createTable(){
		try {
			PreparedStatement statement = connection.prepareStatement(CREATE_QUERY);
			// Execute as it's not queried
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't create a new database table components", e);
		}
	}

	private Connection connect(){
		File file = new File(grindGens.getDataFolder(), "players.db");
		String connectionString = "jdbc:sqlite:"+file;

		try {
			return DriverManager.getConnection(connectionString);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't connect to database " + file);
		}
	}

	@NotNull
	public CompletableFuture<@Nullable GenPlayer> load(Player player){
		return load(player.getUniqueId());
	}
	@NotNull
	public CompletableFuture<@Nullable GenPlayer> load(UUID id){
		return CompletableFuture.supplyAsync(()->{
			try {
				PreparedStatement statement = connection.prepareStatement(GET_BY_ID_QUERY);
				statement.setString(1, id.toString());
				ResultSet resultSet = statement.executeQuery();
				if (resultSet != null && resultSet.next()) {
					double bitcoin = resultSet.getDouble("bitcoin");
					int maxGenerators = resultSet.getInt("maxGenerators");
					String generators = resultSet.getString("generators");
					if (!generators.startsWith("[")){
						generators = "["+generators+"]";
					}
					JsonArray genArray = JsonParser.parseString(generators).getAsJsonArray();

					GenPlayer genPlayer = new GenPlayer(grindGens, id);
					genPlayer.setNew(false);
					genPlayer.setMaxGenerators(maxGenerators);
					genPlayer.setBitcoin(bitcoin);
					if (!genArray.isEmpty()) {
						ComponentDatabase componentDatabase = grindGens.componentDatabase();
						for (JsonElement element : genArray) {
							if (element == null || element == JsonNull.INSTANCE) {
								continue;
							}
							int idGen = element.getAsInt();
							componentDatabase.load(idGen, genPlayer).thenAcceptAsync(component -> {
								if (component == null){
									Bukkit.broadcast("Error Loading a component for "+ id, "grindgens.alerts");
								}
								//Bukkit.broadcastMessage("Loading component: "+ component.type() + " | x" + component.location().x() +" y"+component.location().y()+" z"+component.location().z());
								grindGens.globalComponentManager().load(component);
								genPlayer.addComponent(component);
//								Bukkit.broadcastMessage("Loaded component: "+ component.type() + " | x" + component.location().x() +" y"+component.location().y()+" z"+component.location().z());
							});
						}
					}
					return genPlayer;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return null;
		});
	}
	public void save(GenPlayer player){
		save(player, null);
	}
	public void save(GenPlayer player, Consumer<GenPlayer> runAfter){
		grindGens.getServer().getScheduler().runTaskAsynchronously(grindGens, ()->{

			if (player.isNew()){
				try {
				PreparedStatement insertStatement = connection.prepareStatement(INSERT_QUERY);
				insertStatement.setString(1, player.uuid().toString());
				insertStatement.setDouble(2, player.getBitcoins());
				if (player.generators().isEmpty()){
					insertStatement.setString(3, "[]");
				} else {
					JsonArray jsonElements = new JsonArray(player.generators().size());
					for (Generator generator : player.generators()) {
						jsonElements.add(generator.id());
					}
					insertStatement.setString(3, jsonElements.toString());
				}
				insertStatement.setInt(4, player.maxGenerators());

				insertStatement.executeUpdate();
				insertStatement.close();
				} catch (SQLException e) {
					Bukkit.broadcastMessage("Failed to create a new database value!");
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			} else {
				try {
				PreparedStatement updateStatement = connection.prepareStatement(UPDATE_QUERY);
				updateStatement.setDouble(1, player.getBitcoins());
				if (!player.generators().isEmpty()) {
					JsonArray jsonElements = new JsonArray(player.generators().size());
					for (Generator generator : player.generators()) {
						jsonElements.add(generator.id());
					}
					updateStatement.setString(2, jsonElements.toString());
				} else {
					updateStatement.setString(2, "[]");
				}
				updateStatement.setInt(3, player.maxGenerators());
				updateStatement.setString(4, player.uuid().toString());

				updateStatement.executeUpdate();
				 updateStatement.close();
				} catch (SQLException e) {
					Bukkit.broadcastMessage("Failed to update an old database value!");
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			if (runAfter != null){
				runAfter.accept(player);
			}
			player.setNew(false);
		});
	}

}
