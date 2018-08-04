package autofe.db.search;

import autofe.db.model.database.Database;
import jaicore.search.structure.core.GraphGenerator;
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
		return new SingleRootGenerator<DatabaseNode>() {

			@Override
			public DatabaseNode getRoot() {
				return new DatabaseNode();
			}

		};
	}

	@Override
	public SuccessorGenerator<DatabaseNode, String> getSuccessorGenerator() {
		return new DatabaseSuccessorGenerator(database);
	}

	@Override
	public NodeGoalTester<DatabaseNode> getGoalTester() {
		return new NodeGoalTester<DatabaseNode>() {

			@Override
			public boolean isGoal(DatabaseNode node) {
				return node.isFinished();
			}
		};
	}

	@Override
	public boolean isSelfContained() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setNodeNumbering(boolean nodenumbering) {
		// TODO Auto-generated method stub

	}

	public Database getDatabase() {
		return database;
	}

}
