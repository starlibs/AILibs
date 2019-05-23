package autofe.db.search;

import autofe.db.model.database.Database;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DatabaseGraphGenerator implements GraphGenerator<DatabaseNode, String> {

	private Database database;

	public DatabaseGraphGenerator(Database database) {
		super();
		this.database = database;
	}

	@Override
	public SingleRootGenerator<DatabaseNode> getRootGenerator() {
		return () -> new DatabaseNode();
	}

	@Override
	public SuccessorGenerator<DatabaseNode, String> getSuccessorGenerator() {
		return new DatabaseSuccessorGenerator(database);
	}

	@Override
	public NodeGoalTester<DatabaseNode> getGoalTester() {
		return node -> node.isFinished();
	}

	@Override
	public boolean isSelfContained() {
		return false;
	}

	@Override
	public void setNodeNumbering(boolean nodenumbering) {
		throw new UnsupportedOperationException("This operation is not implemented yet.");
	}

	public Database getDatabase() {
		return database;
	}

}
