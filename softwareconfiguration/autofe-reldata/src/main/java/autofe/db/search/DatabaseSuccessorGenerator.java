package autofe.db.search;

import java.util.ArrayList;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSuccessorGenerator.class);

	private Database db;

	public DatabaseSuccessorGenerator(final Database db) {
		super();
		this.db = db;
	}

	@Override
	public List<NodeExpansionDescription<DatabaseNode, String>> generateSuccessors(final DatabaseNode node) {
		// Check whether node contains intermediate feature
		BackwardFeature intermediateFeature = this.getIntermediateFeature(node.getSelectedFeatures());

		if (intermediateFeature == null) {
			return this.computeForNonIntermediateNode(node);
		} else {
			return this.computeForIntermediateNode(node);
		}
	}

	private List<NodeExpansionDescription<DatabaseNode, String>> computeForNonIntermediateNode(final DatabaseNode node) {
		// Finished nodes do not have successors
		if (node.isFinished()) {
			return Collections.emptyList();
		}

		List<NodeExpansionDescription<DatabaseNode, String>> toReturn = new ArrayList<>();

		Set<Attribute> forwardAttributes = this.db.getForwardAttributes();

		Set<Attribute> backwardAttributes = this.db.getBackwardAttributes();

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
			// Do not select target attribute
			if (node.containsAttribute(att) || att.isTarget()) {
				continue;
			}
			if ((addOnlyLargerFeatures && att.compareTo(maxForwardAttribute) > 0) || !addOnlyLargerFeatures) {
				List<AbstractFeature> extended = this.cloneFeatureList(currentFeatures);
				extended.add(new ForwardFeature(att));
				DatabaseNode to = new DatabaseNode(extended, false);
				toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, to, "Forward: " + att.getName(), NodeType.OR));
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
			Set<Path> allPaths = this.getAllPathsFrom(candidate.getPath(), candidate);
			// Generate feature for each path
			List<BackwardFeature> allFeatures = new ArrayList<>();
			for (Path path : allPaths) {
				BackwardFeature bf = new BackwardFeature(att, path);
				allFeatures.add(bf);
			}
			if (node.getSelectedFeatures().containsAll(allFeatures)) {
				LOGGER.debug("Node already contains all possible features => Skip successor");
				continue;
			}

			List<AbstractFeature> extended = this.cloneFeatureList(currentFeatures);
			extended.add(new BackwardFeature(att));
			DatabaseNode to = new DatabaseNode(extended, false);
			toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, to, "Backward: " + att.getName(), NodeType.OR));
		}

		// Exit edge
		DatabaseNode exitNode = new DatabaseNode(new ArrayList<>(currentFeatures), true);
		toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, exitNode, "Exit", NodeType.OR));

		return toReturn;
	}

	private List<NodeExpansionDescription<DatabaseNode, String>> computeForIntermediateNode(final DatabaseNode node) {

		BackwardFeature intermediateFeature = this.getIntermediateFeature(node.getSelectedFeatures());
		if (intermediateFeature == null) {
			throw new IllegalArgumentException("Intermediate feature must not be null.");
		}

		// Get last table
		Table lastTable = DBUtils.getTableByName(intermediateFeature.getPath().getLastTableName(), this.db);
		if (lastTable == null) {
			lastTable = DBUtils.getAttributeTable(intermediateFeature.getParent(), this.db);
		}

		// Compute possible next path elements
		List<Tuple<AbstractRelationship, AggregationFunction>> nextPathElements = this.nextIntermediatePathElements(lastTable);

		List<Tuple<AbstractRelationship, AggregationFunction>> validNextPathElements = new ArrayList<>();

		// Check whether the candidates are duplicates
		for (Tuple<AbstractRelationship, AggregationFunction> nextPathElement : nextPathElements) {
			// Compute all possible path from there
			AbstractRelationship ar = nextPathElement.getT();
			ar.setContext(this.db);
			Path prefix = new Path(intermediateFeature.getPath());
			prefix.addPathElement(nextPathElement);
			Set<Path> allPaths = this.getAllPathsFrom(prefix, intermediateFeature);

			// Generate feature for each path
			List<BackwardFeature> allFeatures = new ArrayList<>();
			for (Path path : allPaths) {
				BackwardFeature bf = new BackwardFeature(intermediateFeature);
				bf.setPath(path);
				allFeatures.add(bf);
			}

			if (node.getSelectedFeatures().containsAll(allFeatures)) {
				LOGGER.info("Node already contains all possible features => Skip successor");
			} else {
				validNextPathElements.add(nextPathElement);
			}

		}

		List<NodeExpansionDescription<DatabaseNode, String>> toReturn = new ArrayList<>();
		for (Tuple<AbstractRelationship, AggregationFunction> nextPathElement : validNextPathElements) {
			List<AbstractFeature> extendedFeatures = this.cloneFeatureList(node.getSelectedFeatures());
			BackwardFeature extendedintermediateFeature = this.getIntermediateFeature(extendedFeatures);
			if (extendedintermediateFeature == null) {
				throw new IllegalStateException("The intermediate feature must not be null in the current state!");
			}
			Path extendedPath = new Path(intermediateFeature.getPath());
			extendedPath.addPathElement(nextPathElement);
			extendedintermediateFeature.setPath(extendedPath);
			DatabaseNode extendedNode = new DatabaseNode(extendedFeatures, false);
			AbstractRelationship ar = nextPathElement.getT();
			String description = String.format("Intermediate: <[%s -> %s], %s>", ar.getFrom().getName(), ar.getTo().getName(), nextPathElement.getU());
			toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, extendedNode, description, NodeType.OR));
		}

		return toReturn;

	}

	private List<Tuple<AbstractRelationship, AggregationFunction>> nextIntermediatePathElements(final Table lastTable) {
		List<Tuple<AbstractRelationship, AggregationFunction>> toReturn = new ArrayList<>();

		// Find all possible next tables
		Set<BackwardRelationship> backwards = DBUtils.getBackwardsTo(lastTable, this.db);
		Set<ForwardRelationship> forwards = DBUtils.getForwardsTo(lastTable, this.db);

		for (BackwardRelationship br : backwards) {
			for (AggregationFunction af : AggregationFunction.values()) {
				Tuple<AbstractRelationship, AggregationFunction> toAdd = new Tuple<>(br, af);
				toReturn.add(toAdd);
			}
		}

		for (ForwardRelationship fr : forwards) {
			Tuple<AbstractRelationship, AggregationFunction> toAdd = new Tuple<>(fr, null);
			toReturn.add(toAdd);
		}

		return toReturn;
	}

	private BackwardFeature getIntermediateFeature(final List<AbstractFeature> features) {
		for (AbstractFeature feature : features) {
			if (feature instanceof BackwardFeature && DBUtils.isIntermediate(((BackwardFeature) feature).getPath(), this.db)) {
				return (BackwardFeature) feature;
			}
		}
		return null;
	}

	private List<AbstractFeature> cloneFeatureList(final List<AbstractFeature> featureList) {
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

	public Set<Path> getAllPathsFrom(final Path prefix, final BackwardFeature feature) {
		Set<Path> allPaths = new HashSet<>();

		// Start recursion
		this.addPaths(prefix, feature, allPaths);

		return allPaths;
	}

	private void addPaths(final Path prefix, final BackwardFeature feature, final Set<Path> allPaths) {
		if (!DBUtils.isIntermediate(prefix, this.db)) {
			allPaths.add(prefix);
			return;
		}
		Table from = DBUtils.getTableByName(prefix.getLastTableName(), this.db);
		if (from == null) {
			from = DBUtils.getAttributeTable(feature.getParent(), this.db);
		}
		List<Tuple<AbstractRelationship, AggregationFunction>> nextElements = this.nextIntermediatePathElements(from);
		for (Tuple<AbstractRelationship, AggregationFunction> nextElement : nextElements) {
			Path extended = new Path(prefix);
			extended.addPathElement(nextElement);
			// Found complete path
			if (!DBUtils.isIntermediate(extended, this.db)) {
				allPaths.add(extended);
			} else {
				this.addPaths(extended, feature, allPaths);
			}
		}
	}

}
