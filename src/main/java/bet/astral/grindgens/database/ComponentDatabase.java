/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.database;

import bet.astral.grindgens.GrindGens;
import bet.astral.grindgens.models.GenPlayer;
import bet.astral.grindgens.models.component.Component;
import bet.astral.grindgens.models.component.ComponentType;
import bet.astral.grindgens.models.component.DataComponent;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ComponentDatabase {
	private static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS components (id INTEGER PRIMARY KEY, x DOUBLE, y DOUBLE, z DOUBLE, world VARCHAR(50), type VARCHAR(25),  data JSON);";
	private static final String UPDATE_QUERY = "UPDATE components SET x = ?, y = ?, z = ?, world = ?, type = ?, data = ? where id = ?";
	private static final String INSERT_QUERY = "INSERT INTO components (id, x, y, z, world, type, data) VALUES (?, ?, ?, ?, ?, ?, ?)" ;
	private static final String DELETE_BY_ID_QUERY = "DELETE FROM components WHERE id = ?";
	private static final String GET_BY_ID_QUERY = "SELECT * FROM components WHERE id = ?";
	private Map<String, Class<?>> foundClasses = new HashMap<>();
	private final GrindGens grindGens;
	private final Connection connection;
	public ComponentDatabase(GrindGens grindGens){
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

	public void delete(Component component){
		Bukkit.getScheduler().runTaskAsynchronously(grindGens, () -> {
			try {
				PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID_QUERY);
				statement.setInt(1, component.id());
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}

	public void saveComponent(Component component) {
		Bukkit.getScheduler().runTaskAsynchronously(grindGens, () -> {
			int id = component.id();
			Location loc = component.location();
			double x = loc.getBlockX();
			double y = loc.getBlockY();
			double z = loc.getBlockZ();
			String world = loc.getWorld().getName();
			String type = component.getClass().getName();
			String data = "{}";
			if (component instanceof DataComponent dataComponent) {
				data = ((JsonObject) dataComponent.dataAsJson()).isEmpty() ? "{}" : dataComponent.dataAsJson().toString();
			}

			PreparedStatement statement;
			if (component.isNew()) {
				try {
					statement = connection.prepareStatement(INSERT_QUERY);
					statement.setInt(1, id);
					statement.setDouble(2, x);
					statement.setDouble(3, y);
					statement.setDouble(4, z);
					statement.setString(5, world);
					statement.setString(6, type);
					statement.setString(7, data);
				} catch (SQLException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			} else {
				try {
					statement = connection.prepareStatement(UPDATE_QUERY);
					statement.setDouble(1, x);
					statement.setDouble(2, y);
					statement.setDouble(3, z);
					statement.setString(4, world);
					statement.setString(5, type);
					statement.setString(6, data);
					statement.setInt(7, id);
				} catch (SQLException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			component.setNew(false);
			try {
				statement.executeUpdate();
				close(statement);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}


	@NotNull
	public CompletableFuture<@Nullable Component> load(final int id, @NotNull GenPlayer genPlayer) throws SQLException {
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement statement;
				statement = connection.prepareStatement(GET_BY_ID_QUERY);
				statement.setInt(1, id);
				ResultSet resultSet = statement.executeQuery();
				if (resultSet != null && resultSet.next()) {
					double x = resultSet.getDouble("x");
					double y = resultSet.getDouble("y");
					double z = resultSet.getDouble("z");
					String worldStr = resultSet.getString("world");
					String typeStr = resultSet.getString("type");
					String dataStr = resultSet.getString("data");

					World world = Bukkit.getWorld(worldStr);
					if (world == null) {
						throw new RuntimeException("Couldn't load component for id " + id + " because the world is not a loaded world (" + worldStr + ")");
					}
					Location location = new Location(world, x, y, z);
					Material material = location.getBlock().getType();

					try {
						if (!foundClasses.containsKey(typeStr)){ // Less searching for classes -> less time loading
							Class<?> clazz = Class.forName(typeStr);
							foundClasses.put(typeStr, clazz);
						}
						Class<?> clazz = foundClasses.get(typeStr);
						Method componentTypeMethod = clazz.getMethod("componentType");
						ComponentType componentType = (ComponentType) componentTypeMethod.invoke(null);

						try {
							//GrindGens gens, ComponentType type, GenPlayer genPlayer, Location location, Material material, int id, String data
							Constructor<?> constructor = getConstructor(clazz,
									GrindGens.class,
									ComponentType.class,
									GenPlayer.class,
									Location.class,
									Material.class,
									int.class,
									String.class
							);
							if (constructor == null){
								return null;
							}
							try {
								Component component = (Component) constructor.newInstance(grindGens, componentType, genPlayer, location, material, id, dataStr);
								if (component == null){
									return null;
								}
								component.setNew(false);
								component.setDeleted(false);
								return component;
							} catch (InstantiationException e) {
								e.printStackTrace();
								return null;
							}

						} catch (SecurityException e) {
							e.printStackTrace();
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} finally {
						close(resultSet);
						close(statement);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	private void close(Statement autoCloseable){
		if (autoCloseable != null) {
			try {
				if (!autoCloseable.isClosed()){
					autoCloseable.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	private void close(ResultSet autoCloseable){
		if (autoCloseable != null) {
			try {
				if (!autoCloseable.isClosed()){
					autoCloseable.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	private Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
		Constructor<?> constructor;
		try {
			constructor = clazz.getConstructor(params);
		} catch (NoSuchMethodException e) {
			try {
				constructor = clazz.getDeclaredConstructor(params);
				constructor.setAccessible(true);
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return constructor;
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
		File file = new File(grindGens.getDataFolder(), "components.db");
		// jdbc:sqlite:c:/path/to/file/database.db
		// jdbc : sqlite : FILE
		String connectionString = "jdbc:sqlite:"+file;

		try {
			return DriverManager.getConnection(connectionString);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't connect to database " + file);
		}
	}
}
