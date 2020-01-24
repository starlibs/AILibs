package autofe.db.search;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseNodeGoalPredicate implements INodeGoalTester<DatabaseNode, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseNodeGoalPredicate.class);

	@Override
	public boolean isGoal(final DatabaseNode node) {
		try {
			return node.isFinished();
		} catch (Exception e) {
			LOGGER.error("Could not determine whether the node is finished.", e);
			return false;
		}
	}

}
