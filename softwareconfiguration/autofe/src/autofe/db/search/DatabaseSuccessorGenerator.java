package autofe.db.search;

import java.util.Collection;

import autofe.db.model.Database;
import autofe.db.model.ForwardRelationship;
import autofe.db.model.Table;
import autofe.db.util.DBUtils;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DatabaseSuccessorGenerator implements SuccessorGenerator<DatabaseNode, String>{

	@Override
	public Collection<NodeExpansionDescription<DatabaseNode, String>> generateSuccessors(DatabaseNode node) {
		return null;
	}
	
	

}
