package autofe.db.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import autofe.db.model.AggregationFunction;
import autofe.db.model.Attribute;
import autofe.db.model.BackwardAggregateOperation;
import autofe.db.model.BackwardRelationship;
import autofe.db.model.Database;
import autofe.db.model.ForwardRelationship;
import autofe.db.model.Table;

public class DBUtils {

	public static Table getTargetTable(Database db) {
		return null;
	}

	public static Set<ForwardRelationship> getForwardsFor(Table table, Database db) {
		Set<ForwardRelationship> toReturn = new HashSet<>();
		for (ForwardRelationship forwardRelationship : db.getForwards()) {
			if (forwardRelationship.getFrom().equals(table)) {
				toReturn.add(forwardRelationship);
			}
		}
		return toReturn;
	}

	public static Set<BackwardRelationship> getBackwardsFor(Table table, Database db) {
		Set<BackwardRelationship> toReturn = new HashSet<>();
		for (BackwardRelationship backwardRelationship : db.getBackwards()) {
			if (backwardRelationship.getFrom().equals(table)) {
				toReturn.add(backwardRelationship);
			}
		}
		return toReturn;
	}

	public static Set<BackwardAggregateOperation> getBackwardAggregateOperations(Table from, Database db) {
		Set<BackwardAggregateOperation> operations = new HashSet<>();
		// Start recursion
		for (BackwardRelationship backwardRelationship : getBackwardsFor(from, db)) {
			getOperationsForBackwardRelation(backwardRelationship, operations,db);
		}
		return operations;
	}

	private static void getOperationsForBackwardRelation(BackwardRelationship backwardRelationship,
			Set<BackwardAggregateOperation> operations, Database db) {
		// Add operations for all attributes in 'to' table
		Table to = backwardRelationship.getFrom();
		for (Attribute attribute : to.getColumns()) {
			if (attribute.isAggregable()) {
				for (AggregationFunction af : AggregationFunction.values()) {
					BackwardAggregateOperation op = new BackwardAggregateOperation(backwardRelationship, af, attribute);
					operations.add(op);
				}
			}
		}

		// Recursion
		for (BackwardRelationship br : getBackwardsFor(to, db)) {
			getOperationsForBackwardRelation(br, operations,db);
		}
	}
	
	public static void serialize(Database db, String path) { 
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
	
	public static Database deserialize(String path) { 
		Database db = null;
		Gson gson = new Gson();
		try {
			db = gson.fromJson(new FileReader(path), Database.class);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR!");
		}
		return db;
		
	}

}
