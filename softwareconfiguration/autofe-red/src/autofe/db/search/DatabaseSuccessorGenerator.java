package autofe.db.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import autofe.db.model.database.Path;
import autofe.db.model.database.Table;
import autofe.db.model.relation.AbstractRelationship;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;
import autofe.db.util.DBUtils;
import autofe.db.util.Tuple;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DatabaseSuccessorGenerator implements SuccessorGenerator<DatabaseNode, String> {

	private static Logger LOG = LoggerFactory.getLogger(DatabaseSuccessorGenerator.class);

	private Database db;

	public DatabaseSuccessorGenerator(Database db) {
		super();
		this.db = db;
	}

	@Override
	public List<NodeExpansionDescription<DatabaseNode, String>> generateSuccessors(DatabaseNode node) {
		// Check whether node contains intermediate feature
		BackwardFeature intermediateFeature = getIntermediateFeature(node.getSelectedFeatures());

		if (intermediateFeature == null) {
			return computeForNonIntermediateNode(node);
		} else {
			return computeForIntermediateNode(node);
		}
	}

	private List<NodeExpansionDescription<DatabaseNode, String>> computeForNonIntermediateNode(DatabaseNode node) {
		// Finished nodes do not have successors
		if (node.isFinished()) {
			return Collections.emptyList();
		}

		List<NodeExpansionDescription<DatabaseNode, String>> toReturn = new ArrayList<>();

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
			// Do not select target attribute
			if (att.isTarget()) {
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
			BackwardFeature candidate = new BackwardFeature(att);

			// Check whether all variants of the backward feature are already chosen
			Set<Path> allPaths = getAllPathsFrom(candidate.getPath(), candidate);
			// Generate feature for each path
			List<BackwardFeature> allFeatures = new ArrayList<>();
			for (Path path : allPaths) {
				BackwardFeature bf = new BackwardFeature(att, path);
				allFeatures.add(bf);
			}
			if (node.getSelectedFeatures().containsAll(allFeatures)) {
				LOG.debug("Node already contains all possible features => Skip successor");
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

	private List<NodeExpansionDescription<DatabaseNode, String>> computeForIntermediateNode(DatabaseNode node) {
		List<NodeExpansionDescription<DatabaseNode, String>> toReturn = new ArrayList<>();

		BackwardFeature intermediateFeature = getIntermediateFeature(node.getSelectedFeatures());

		// Get last table
		Table lastTable = DBUtils.getTableByName(intermediateFeature.getPath().getLastTableName(), db);
		if (lastTable == null) {
			lastTable = DBUtils.getAttributeTable(intermediateFeature.getParent(), db);
		}

		// Compute possible next path elements
		List<Tuple<AbstractRelationship, AggregationFunction>> nextPathElements = nextIntermediatePathElements(
				lastTable);

		List<Tuple<AbstractRelationship, AggregationFunction>> validNextPathElements = new ArrayList<>();

		// Check whether the candidates are duplicates
		for (Tuple<AbstractRelationship, AggregationFunction> nextPathElement : nextPathElements) {
			// Compute all possible path from there
			AbstractRelationship ar = nextPathElement.getT();
			ar.setContext(db);
			Path prefix = new Path(intermediateFeature.getPath());
			prefix.addPathElement(nextPathElement);
			Set<Path> allPaths = getAllPathsFrom(prefix, intermediateFeature);

			// Generate feature for each path
			List<BackwardFeature> allFeatures = new ArrayList<>();
			for (Path path : allPaths) {
				BackwardFeature bf = new BackwardFeature(intermediateFeature);
				bf.setPath(path);
				allFeatures.add(bf);
			}

			if (node.getSelectedFeatures().containsAll(allFeatures)) {
				LOG.info("Node already contains all possible features => Skip successor");
			} else {
				validNextPathElements.add(nextPathElement);
			}

		}

		for (Tuple<AbstractRelationship, AggregationFunction> nextPathElement : validNextPathElements) {
			List<AbstractFeature> extendedFeatures = cloneFeatureList(node.getSelectedFeatures());
			BackwardFeature extendedintermediateFeature = getIntermediateFeature(extendedFeatures);
			if (extendedintermediateFeature == null) {
				throw new IllegalStateException("The intermediate feature must not be null in the current state!");
			}
			Path extendedPath = new Path(intermediateFeature.getPath());
			extendedPath.addPathElement(nextPathElement);
			extendedintermediateFeature.setPath(extendedPath);
			DatabaseNode extendedNode = new DatabaseNode(extendedFeatures, false);
			AbstractRelationship ar = nextPathElement.getT();
			String description = String.format("Intermediate: <[%s -> %s], %s>", ar.getFrom().getName(),
					ar.getTo().getName(), nextPathElement.getU());
			toReturn.add(
					new NodeExpansionDescription<DatabaseNode, String>(node, extendedNode, description, NodeType.OR));
		}

		return toReturn;

	}

	private List<Tuple<AbstractRelationship, AggregationFunction>> nextIntermediatePathElements(Table lastTable) {
		List<Tuple<AbstractRelationship, AggregationFunction>> toReturn = new ArrayList<>();

		// Find all possible next tables
		Set<BackwardRelationship> backwards = DBUtils.getBackwardsTo(lastTable, db);
		Set<ForwardRelationship> forwards = DBUtils.getForwardsTo(lastTable, db);

		// TODO: Add constraint that distance to target must not increase

		for (BackwardRelationship br : backwards) {
			for (AggregationFunction af : AggregationFunction.values()) {
				Tuple<AbstractRelationship, AggregationFunction> toAdd = new Tuple<AbstractRelationship, AggregationFunction>(
						br, af);
				toReturn.add(toAdd);
			}
		}

		for (ForwardRelationship fr : forwards) {
			Tuple<AbstractRelationship, AggregationFunction> toAdd = new Tuple<AbstractRelationship, AggregationFunction>(
					fr, null);
			toReturn.add(toAdd);
		}

		return toReturn;
	}

	private BackwardFeature getIntermediateFeature(List<AbstractFeature> features) {
		for (AbstractFeature feature : features) {
			if (feature instanceof BackwardFeature
					&& DBUtils.isIntermediate(((BackwardFeature) feature).getPath(), db)) {
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

	public Set<Path> getAllPathsFrom(Path prefix, BackwardFeature feature) {
		Set<Path> allPaths = new HashSet<>();

		// Start recursion
		addPaths(prefix, feature, allPaths);

		return allPaths;
	}

	private void addPaths(Path prefix, BackwardFeature feature, Set<Path> allPaths) {
		if (!DBUtils.isIntermediate(prefix, db)) {
			allPaths.add(prefix);
			return;
		}
		Table from = DBUtils.getTableByName(prefix.getLastTableName(), db);
		if (from == null) {
			from = DBUtils.getAttributeTable(feature.getParent(), db);
		}
		List<Tuple<AbstractRelationship, AggregationFunction>> nextElements = nextIntermediatePathElements(from);
		for (Tuple<AbstractRelationship, AggregationFunction> nextElement : nextElements) {
			Path extended = new Path(prefix);
			extended.addPathElement(nextElement);
			// Found complete path
			if (!DBUtils.isIntermediate(extended, db)) {
				allPaths.add(extended);
			} else {
				addPaths(extended, feature, allPaths);
			}
		}
	}

}
