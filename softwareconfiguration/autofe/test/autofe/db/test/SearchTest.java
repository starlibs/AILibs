package autofe.db.test;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.Database;
import autofe.db.search.DatabaseGraphGenerator;
import autofe.db.search.DatabaseNode;
import autofe.db.search.DatabaseNodeEvaluator;
import autofe.db.util.DBUtils;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.NodeExpansionDescription;

public class SearchTest {

	private static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";

	@Test
	public void testSearch() {
		Database initialDatabase = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(initialDatabase);
		BestFirst<DatabaseNode, String> search = new BestFirst<>(generator, new DatabaseNodeEvaluator());
		search.setTimeoutForComputationOfF(600000, node -> 100.0);
		List<DatabaseNode> solutions = null;
		while ((solutions = search.nextSolution()) != null) {

			int i = 1;
			for (DatabaseNode node : solutions) {
				DBUtils.serializeToFile(node.getDatabase(), "solutions/solution" + i);
				i++;
			}
		}
	}

}
