package autofe.db.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.AbstractAttribute;
import autofe.db.model.database.Database;
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

		Set<AbstractAttribute> forwardAttributes = db.getForwardAttributes();
		forwardAttributes.removeAll(node.getSelectedAttributes());

		Set<AbstractAttribute> backwardAttributes = db.getBackwardAttributes();
		backwardAttributes.removeAll(node.getSelectedAttributes());

		List<AbstractAttribute> currentAttributes = node.getSelectedAttributes();

		// One successor node for each forward feature
		for (AbstractAttribute att : forwardAttributes) {
			List<AbstractAttribute> extended = new ArrayList<>(currentAttributes);
			extended.add(att);
			DatabaseNode to = new DatabaseNode(extended, false);
			toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, to, "Forward: " + att.getName(),
					NodeType.OR));
		}

		// One successor node for each backward feature
		for (AbstractAttribute att : backwardAttributes) {
			List<AbstractAttribute> extended = new ArrayList<>(currentAttributes);
			extended.add(att);
			DatabaseNode to = new DatabaseNode(extended, false);
			toReturn.add(new NodeExpansionDescription<DatabaseNode, String>(node, to, "Backward: " + att.getName(),
					NodeType.OR));
		}

		return toReturn;
	}

}
