package autofe.db.search;

import autofe.db.model.database.Database;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DatabaseGraphGenerator implements GraphGenerator<DatabaseNode, String> {

	private Database initialDatabase;

	public DatabaseGraphGenerator(Database initialDatabase) {
		super();
		this.initialDatabase = initialDatabase;
	}

	@Override
	public SingleRootGenerator<DatabaseNode> getRootGenerator() {
		return new SingleRootGenerator<DatabaseNode>() {

			@Override
			public DatabaseNode getRoot() {
				return new DatabaseNode(initialDatabase);
			}

		};
	}

	@Override
	public SuccessorGenerator<DatabaseNode, String> getSuccessorGenerator() {
		return new DatabaseSuccessorGenerator();
	}

	@Override
	public NodeGoalTester<DatabaseNode> getGoalTester() {
		return new NodeGoalTester<DatabaseNode>() {

			@Override
			public boolean isGoal(DatabaseNode node) {
				Database db = node.getDatabase();
				if(db.getOperationHistory().size() > 1) {
					return true;
				}
				return false;
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

}
