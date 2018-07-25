package autofe.db.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.Attribute;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardFeature;
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
		Collection<NodeExpansionDescription<DatabaseNode, String>> toReturn = new ArrayList<>();

		Set<Attribute> forwardAttributes = db.getForwardAttributes();
		// forwardAttributes.removeAll(node.getSelectedAttributes());

		Set<Attribute> backwardAttributes = db.getBackwardAttributes();
		// backwardAttributes.removeAll(node.getSelectedAttributes());

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
				List<AbstractFeature> extended = new ArrayList<>(currentFeatures);
				extended.add(new ForwardFeature(att));
				DatabaseNode to = new DatabaseNode(extended, false);
				toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, to, "Forward: " + att.getName(),
						NodeType.OR));
			}
		}

		// TODO: Introduce intermediate nodes here
		// One successor node for each backward feature
		for (Attribute att : backwardAttributes) {
			List<AbstractFeature> extended = new ArrayList<>(currentFeatures);
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

}
