package autofe.db.search;

import autofe.db.sql.DatabaseConnector;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public class DatabaseNodeEvaluator implements INodeEvaluator<DatabaseNode, Double>{
	
	DatabaseConnector databaseConnector;

	@Override
	public Double f(Node<DatabaseNode, ?> node) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

}
