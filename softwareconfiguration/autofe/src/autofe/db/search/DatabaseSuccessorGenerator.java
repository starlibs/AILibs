package autofe.db.search;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DatabaseSuccessorGenerator implements SuccessorGenerator<DatabaseNode, String> {

	private static Logger LOG = LoggerFactory.getLogger(DatabaseSuccessorGenerator.class);

	@Override
	public Collection<NodeExpansionDescription<DatabaseNode, String>> generateSuccessors(DatabaseNode node) {
		Collection<NodeExpansionDescription<DatabaseNode, String>> toReturn = new ArrayList<>();

		//TODO: Implement me
		
		return null;
	}

}
