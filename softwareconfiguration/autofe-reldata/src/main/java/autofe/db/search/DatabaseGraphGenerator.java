package autofe.db.search;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IGraphGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SingleRootGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.Database;

public class DatabaseGraphGenerator implements IGraphGenerator<DatabaseNode, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseGraphGenerator.class);
	private Database database;

	public DatabaseGraphGenerator(final Database database) {
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
		return new DatabaseSuccessorGenerator(this.database);
	}

	@Override
	public NodeGoalTester<DatabaseNode, String> getGoalTester() {
		return node -> {
			try {
				return node.isFinished();
			} catch (Exception e) {
				LOGGER.error("Could not determine whether the node is finished.", e);
				return false;
			}
		};
	}

	public Database getDatabase() {
		return this.database;
	}

}
