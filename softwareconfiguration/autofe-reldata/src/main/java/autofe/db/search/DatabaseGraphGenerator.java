package autofe.db.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.Database;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DatabaseGraphGenerator implements GraphGenerator<DatabaseNode, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseGraphGenerator.class);
	private Database database;

	public DatabaseGraphGenerator(Database database) {
		super();
		this.database = database;
	}

	@Override
	public SingleRootGenerator<DatabaseNode> getRootGenerator() {
		return () -> {
			try {
				return new DatabaseNode();
			} catch (Exception e) {
				LOGGER.error("Could not create new database node ", e);
				return null;
			}
		};
	}

	@Override
	public SuccessorGenerator<DatabaseNode, String> getSuccessorGenerator() {
		return new DatabaseSuccessorGenerator(database);
	}

	@Override
	public NodeGoalTester<DatabaseNode> getGoalTester() {
		return node -> {
			try {
				return node.isFinished();
			} catch (Exception e) {
				LOGGER.error("Could not determine whether the node is finished.", e);
				return false;
			}
		};
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
