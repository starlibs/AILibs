package autofe.db.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.Attribute;
import autofe.db.model.database.Database;
import autofe.db.model.database.Path;
import autofe.db.model.database.Table;
import autofe.db.model.relation.AbstractRelationship;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;

public class DBUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBUtils.class);

	private DBUtils() {
		// prevent instantiation of this util class.
	}

	public static Table getTargetTable(Database db) {
		for (Table t : db.getTables()) {
			if (t.isTarget()) {
				return t;
			}
		}
		return null;
	}

	public static Set<ForwardRelationship> getForwardsFrom(Table table, Database db) {
		Set<ForwardRelationship> toReturn = new HashSet<>();
		for (ForwardRelationship forwardRelationship : db.getForwards()) {
			forwardRelationship.setContext(db);
			if (forwardRelationship.getFrom().equals(table)) {
				toReturn.add(forwardRelationship);
			}
		}
		return toReturn;
	}

	public static Set<BackwardRelationship> getBackwardsFrom(Table table, Database db) {
		Set<BackwardRelationship> toReturn = new HashSet<>();
		for (BackwardRelationship backwardRelationship : db.getBackwards()) {
			backwardRelationship.setContext(db);
			if (backwardRelationship.getFrom().equals(table)) {
				toReturn.add(backwardRelationship);
			}
		}
		return toReturn;
	}

	public static Set<ForwardRelationship> getForwardsTo(Table table, Database db) {
		Set<ForwardRelationship> toReturn = new HashSet<>();
		for (ForwardRelationship forwardRelationship : db.getForwards()) {
			forwardRelationship.setContext(db);
			if (forwardRelationship.getTo().equals(table)) {
				toReturn.add(forwardRelationship);
			}
		}
		return toReturn;
	}

	public static Set<BackwardRelationship> getBackwardsTo(Table table, Database db) {
		Set<BackwardRelationship> toReturn = new HashSet<>();
		for (BackwardRelationship backwardRelationship : db.getBackwards()) {
			backwardRelationship.setContext(db);
			if (backwardRelationship.getTo().equals(table)) {
				toReturn.add(backwardRelationship);
			}
		}
		return toReturn;
	}

	public static Set<Table> getForwardReachableTables(Table from, Database db) {
		Set<Table> tables = new HashSet<>();

		addForwardTables(from, db, tables);
		return tables;
	}

	private static void addForwardTables(Table from, Database db, Set<Table> tables) {
		tables.add(from);
		for (ForwardRelationship fr : getForwardsFrom(from, db)) {
			addForwardTables(fr.getTo(), db, tables);
		}
	}

	public static Set<Table> getBackwardReachableTables(Table from, Database db) {
		Set<Table> tables = new HashSet<>();
		Set<Table> forwardTables = getForwardReachableTables(from, db);
		for (Table t : forwardTables) {
			addBackwardTables(t, db, tables);
		}
		return tables;
	}

	private static void addBackwardTables(Table from, Database db, Set<Table> tables) {
		for (BackwardRelationship br : getBackwardsFrom(from, db)) {
			tables.add(br.getTo());
			addBackwardTables(br.getTo(), db, tables);
		}
	}

	public static void serializeToFile(Database db, String path) {
		Gson gson = initGson();
		try {
			FileWriter fw = new FileWriter(path);
			gson.toJson(db, fw);
			fw.flush();
			fw.close();
		} catch (JsonIOException | IOException e) {
			LOGGER.error("An error occured while serializing the database to a file", e);
		}
	}

	public static Database deserializeFromFile(String path) {
		Database db = null;
		Gson gson = initGson();
		try {
			db = gson.fromJson(new FileReader(path), Database.class);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			LOGGER.error("An error occured while deserializing the database from a file", e);
		}
		return db;
	}

	public static String serializeToString(Database db) {
		Gson gson = initGson();
		try {
			return gson.toJson(db);
		} catch (JsonIOException e) {
			LOGGER.error("An error occured while serializing the database to a String", e);
		}
		return null;
	}

	public static Database deserializeFromString(String serialized) {
		Database db = null;
		Gson gson = initGson();
		try {
			db = gson.fromJson(serialized, Database.class);
		} catch (JsonSyntaxException | JsonIOException e) {
			LOGGER.error("An error occured while deserializing the database from a string", e);
		}
		return db;
	}

	private static Gson initGson() {
		return new GsonBuilder().registerTypeAdapter(AbstractRelationship.class, new InterfaceAdapter<>()).create();
	}

	public static Database clone(Database db) {
		String serialized = serializeToString(db);
		return deserializeFromString(serialized);
	}

	public static Table getTableByName(String name, Database db) {
		for (Table t : db.getTables()) {
			if (t.getName().equals(name)) {
				return t;
			}
		}
		return null;
	}

	public static Attribute getAttributeByName(String name, Table table) {
		for (Attribute a : table.getColumns()) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}

	public static Table getAttributeTable(Attribute attribute, Database db) {
		for (Table t : db.getTables()) {
			if (t.getColumns().contains(attribute)) {
				return t;
			}
		}
		return null;
	}

	public static boolean isIntermediate(Path path, Database db) {
		List<Tuple<AbstractRelationship, AggregationFunction>> pathElements = path.getPathElements();

		if (pathElements.isEmpty()) {
			return true;
		}

		// Check whether last edge goes to the target or is forward reachable
		AbstractRelationship relationship = pathElements.get(pathElements.size() - 1).getT();
		relationship.setContext(db);
		Table lastTable = relationship.getFrom();
		Set<Table> forwardReachable = getForwardReachableTables(getTargetTable(db), db);
		return ((!lastTable.isTarget()) && !(forwardReachable.contains(lastTable)));
	}

	public static Attribute getPrimaryKey(Table table) {
		for (Attribute attribute : table.getColumns()) {
			if (attribute.isPrimaryKey()) {
				return attribute;
			}
		}
		return null;
	}

	public static Attribute getTargetAttribute(Database db) {
		Table targetTable = getTargetTable(db);
		if (targetTable == null) {
			throw new IllegalArgumentException("The target table must not be null");
		}

		for (Attribute attribute : targetTable.getColumns()) {
			if (attribute.isTarget()) {
				return attribute;
			}
		}
		return null;
	}

	public static List<ForwardRelationship> getJoinTables(Table from, Table to, Database db) {
		Map<Table, List<ForwardRelationship>> paths = new HashMap<>();
		for (ForwardRelationship fr : getForwardsFrom(from, db)) {
			addJoinTable(new ArrayList<>(), fr, db, paths);
		}
		return paths.get(to);
	}

	private static void addJoinTable(List<ForwardRelationship> currentPath, ForwardRelationship currentRelationship, Database db, Map<Table, List<ForwardRelationship>> paths) {
		currentRelationship.setContext(db);
		List<ForwardRelationship> extendedPath = new ArrayList<>(currentPath);
		extendedPath.add(currentRelationship);
		paths.put(currentRelationship.getTo(), extendedPath);
		for (ForwardRelationship fr : getForwardsFrom(currentRelationship.getTo(), db)) {
			addJoinTable(extendedPath, fr, db, paths);
		}
	}

}
