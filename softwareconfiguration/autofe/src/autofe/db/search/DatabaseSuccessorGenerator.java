package autofe.db.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.Attribute;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardFeature;
import autofe.db.model.database.Table;
import autofe.db.model.relation.AbstractRelationship;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;
import autofe.db.util.DBUtils;
import autofe.db.util.Tuple;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DatabaseSuccessorGenerator implements SuccessorGenerator<DatabaseNode, String> {

	private static Logger LOG = LoggerFactory.getLogger(DatabaseSuccessorGenerator.class);

	private Database db;

	public DatabaseSuccessorGenerator(Database db) {
		super();
		this.db = db;
	}

	@Override
	public Collection<NodeExpansionDescription<DatabaseNode, String>> generateSuccessors(DatabaseNode node) {
		// Check whether node contains intermediate feature
		BackwardFeature intermediateFeature = getIntermediateFeature(node.getSelectedFeatures());

		if (intermediateFeature == null) {
			return computeForNonIntermediateNode(node);
		} else {
			return computeForIntermediateNode(node);
		}
	}

	private Collection<NodeExpansionDescription<DatabaseNode, String>> computeForNonIntermediateNode(
			DatabaseNode node) {
		Collection<NodeExpansionDescription<DatabaseNode, String>> toReturn = new ArrayList<>();

		Set<Attribute> forwardAttributes = db.getForwardAttributes();

		Set<Attribute> backwardAttributes = db.getBackwardAttributes();

		List<AbstractFeature> currentFeatures = node.getSelectedFeatures();
		List<ForwardFeature> currentForwardFeatures = new ArrayList<>();
		for (AbstractFeature feature : currentFeatures) {
			if (feature instanceof ForwardFeature) {
				currentForwardFeatures.add((ForwardFeature) feature);
			}
		}

		// Lexicographic order
		boolean addOnlyLargerFeatures = !currentForwardFeatures.isEmpty();
		Attribute maxForwardAttribute = null;
		if (addOnlyLargerFeatures) {
			maxForwardAttribute = Collections.max(currentForwardFeatures).getParent();
		}

		// One successor node for each forward feature
		for (Attribute att : forwardAttributes) {
			if (node.containsAttribute(att)) {
				continue;
			}
			if ((addOnlyLargerFeatures && att.compareTo(maxForwardAttribute) > 0) || !addOnlyLargerFeatures) {
				List<AbstractFeature> extended = cloneFeatureList(currentFeatures);
				extended.add(new ForwardFeature(att));
				DatabaseNode to = new DatabaseNode(extended, false);
				toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, to, "Forward: " + att.getName(),
						NodeType.OR));
			}
		}

		// One successor node for each backward feature
		for (Attribute att : backwardAttributes) {
			// Do not consider non-aggregable attributes
			if (!att.isAggregable()) {
				continue;
			}
			List<AbstractFeature> extended = cloneFeatureList(currentFeatures);
			extended.add(new BackwardFeature(att));
			DatabaseNode to = new DatabaseNode(extended, false);
			toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, to, "Backward: " + att.getName(),
					NodeType.OR));
		}

		// Exit edge
		DatabaseNode exitNode = new DatabaseNode(new ArrayList<>(currentFeatures), true);
		toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, exitNode, "Exit", NodeType.OR));

		return toReturn;
	}

	private Collection<NodeExpansionDescription<DatabaseNode, String>> computeForIntermediateNode(DatabaseNode node) {
		Collection<NodeExpansionDescription<DatabaseNode, String>> toReturn = new ArrayList<>();

		BackwardFeature intermediateFeature = getIntermediateFeature(node.getSelectedFeatures());

		// Get last table
		Table lastTable = null;
		List<Tuple<AbstractRelationship, AggregationFunction>> path = intermediateFeature.getPath();
		if (path.isEmpty()) {
			lastTable = DBUtils.getAttributeTable(intermediateFeature.getParent(), db);
		} else {
			AbstractRelationship lastRelationsihp = path.get(path.size() - 1).getT();
			lastTable = lastRelationsihp.getFrom();
		}

		// Find all possible next tables
		Set<BackwardRelationship> backwards = DBUtils.getBackwardsTo(lastTable, db);
		Set<ForwardRelationship> forwards = DBUtils.getForwardsTo(lastTable, db);

		// TODO: Add constraint that distance to target must not increase

		for (BackwardRelationship br : backwards) {
			for (AggregationFunction af : AggregationFunction.values()) {
				List<AbstractFeature> extendedFeatures = cloneFeatureList(node.getSelectedFeatures());
				BackwardFeature extendedintermediateFeature = getIntermediateFeature(extendedFeatures);
				if (extendedintermediateFeature == null) {
					throw new IllegalStateException("The intermediate feature must not be null in the current state!");
				}
				List<Tuple<AbstractRelationship, AggregationFunction>> extendedPath = new ArrayList<>(path);
				Tuple<AbstractRelationship, AggregationFunction> toAdd = new Tuple<AbstractRelationship, AggregationFunction>(
						br, af);
				extendedPath.add(toAdd);
				extendedintermediateFeature.setPath(extendedPath);
				DatabaseNode extendedNode = new DatabaseNode(extendedFeatures, false);
				String description = String.format("Intermediate: <[%s -> %s], %s>", br.getFrom().getName(),
						br.getTo().getName(), af.name());
				toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, extendedNode, description,
						NodeType.OR));
			}

		}

		for (ForwardRelationship fr : forwards) {
			List<AbstractFeature> extendedFeatures = new ArrayList<>(node.getSelectedFeatures());
			BackwardFeature extendedintermediateFeature = getIntermediateFeature(extendedFeatures);
			if (extendedintermediateFeature == null) {
				throw new IllegalStateException("The intermediate feature must not be null in the current state!");
			}
			List<Tuple<AbstractRelationship, AggregationFunction>> extendedPath = new ArrayList<>(path);
			Tuple<AbstractRelationship, AggregationFunction> toAdd = new Tuple<AbstractRelationship, AggregationFunction>(
					fr, null);
			extendedPath.add(toAdd);
			extendedintermediateFeature.setPath(extendedPath);
			DatabaseNode extendedNode = new DatabaseNode(extendedFeatures, false);
			String description = String.format("Intermediate: <[%s -> %s], null>", fr.getFrom().getName(),
					fr.getTo().getName());
			toReturn.add(
					new NodeExpansionDescription<DatabaseNode, String>(node, extendedNode, description, NodeType.OR));
		}

		return toReturn;

	}

	private BackwardFeature getIntermediateFeature(List<AbstractFeature> features) {
		for (AbstractFeature feature : features) {
			if (feature instanceof BackwardFeature && DBUtils.isIntermediate((BackwardFeature) feature, db)) {
				return (BackwardFeature) feature;
			}
		}
		return null;
	}

	private List<AbstractFeature> cloneFeatureList(List<AbstractFeature> featureList) {
		List<AbstractFeature> toReturn = new ArrayList<>();
		for (AbstractFeature feature : featureList) {
			if (feature instanceof ForwardFeature) {
				toReturn.add(new ForwardFeature((ForwardFeature) feature));
			} else if (feature instanceof BackwardFeature) {
				toReturn.add(new BackwardFeature((BackwardFeature) feature));
			}
		}
		return toReturn;
	}

}
