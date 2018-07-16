package autofe.db.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.Attribute;
import autofe.db.model.database.BackwardRelationship;
import autofe.db.model.database.Database;
import autofe.db.model.database.DatabaseOperation;
import autofe.db.model.database.ForwardRelationship;
import autofe.db.model.database.Table;
import autofe.db.model.operation.BackwardAggregateOperation;
import autofe.db.model.operation.ForwardJoinOperation;

public class DBUtils {

	private static Logger LOG = LoggerFactory.getLogger(DBUtils.class);

	public static Table getTargetTable(Database db) {
		for (Table t : db.getTables()) {
			if (t.isTarget()) {
				return t;
			}
		}
		return null;
	}

	public static Set<ForwardRelationship> getForwardsFor(Table table, Database db) {
		Set<ForwardRelationship> toReturn = new HashSet<>();
		for (ForwardRelationship forwardRelationship : db.getForwards()) {
			if (forwardRelationship.getFrom().equals(table)) {
				toReturn.add(forwardRelationship);
			}
		}
		LOG.info("There are {} forward relationships from table {}", toReturn.size(), table.getName());
		return toReturn;
	}

	public static Set<BackwardRelationship> getBackwardsFor(Table table, Database db) {
		Set<BackwardRelationship> toReturn = new HashSet<>();
		for (BackwardRelationship backwardRelationship : db.getBackwards()) {
			if (backwardRelationship.getFrom().equals(table)) {
				toReturn.add(backwardRelationship);
			}
		}
		LOG.info("There are {} backward relationships from table {}", toReturn.size(), table.getName());
		return toReturn;
	}

	public static Set<BackwardAggregateOperation> getBackwardAggregateOperations(Table from, Database db) {
		Set<BackwardAggregateOperation> operations = new HashSet<>();
		// Start recursion
		for (BackwardRelationship backwardRelationship : getBackwardsFor(from, db)) {
			getOperationsForBackwardRelation(backwardRelationship, operations, db);
		}
		LOG.info("There are {} backward operations possible from {}", operations.size(), from.getName());
		return operations;
	}

	private static void getOperationsForBackwardRelation(BackwardRelationship backwardRelationship,
			Set<BackwardAggregateOperation> operations, Database db) {
		// Add operations for all attributes in 'to' table
		Table to = backwardRelationship.getTo();
		for (Attribute attribute : to.getColumns()) {
			if (attribute.isAggregable()) {
				for (AggregationFunction af : AggregationFunction.values()) {
					BackwardAggregateOperation op = new BackwardAggregateOperation(backwardRelationship, af,
							attribute.getName());
					operations.add(op);
				}
			}
		}

		// Recursion
		for (BackwardRelationship br : getBackwardsFor(to, db)) {
			getOperationsForBackwardRelation(br, operations, db);
		}
	}

	public static Set<ForwardJoinOperation> getForwardJoinOperations(Table from, Database db) {
		Set<ForwardJoinOperation> operations = new HashSet<>();
		for (ForwardRelationship fr : getForwardsFor(from, db)) {
			operations.add(new ForwardJoinOperation(fr));
		}
		LOG.info("There are {} forward operations possible from {}", operations.size(), from.getName());
		return operations;
	}

	public static void serializeToFile(Database db, String path) {
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(DatabaseOperation.class, new InterfaceAdapter<>()).create();
		try {
			FileWriter fw = new FileWriter(path);
			gson.toJson(db, fw);
			fw.flush();
			fw.close();
		} catch (JsonIOException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Database deserializeFromFile(String path) {
		Database db = null;
		Gson gson = new GsonBuilder().registerTypeAdapter(DatabaseOperation.class, new InterfaceAdapter<>()).create();
		try {
			db = gson.fromJson(new FileReader(path), Database.class);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return db;
	}

	public static String serializeToString(Database db) {
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(DatabaseOperation.class, new InterfaceAdapter<>()).create();
		try {
			return gson.toJson(db);
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Database deserializeFromString(String serialized) {
		Database db = null;
		Gson gson = new GsonBuilder().registerTypeAdapter(DatabaseOperation.class, new InterfaceAdapter<>()).create();
		try {
			db = gson.fromJson(serialized, Database.class);
		} catch (JsonSyntaxException | JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return db;
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

}
