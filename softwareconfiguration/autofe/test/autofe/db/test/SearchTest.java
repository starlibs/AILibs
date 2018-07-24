package autofe.db.test;

import java.util.List;

import org.junit.Test;

import autofe.db.model.database.Database;
import autofe.db.search.DatabaseGraphGenerator;
import autofe.db.search.DatabaseNode;
import autofe.db.search.DatabaseNodeEvaluator;
import autofe.db.util.DBUtils;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.Node;

public class SearchTest {

	private static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";

	@Test
	public void testSearch() {
		Database initialDatabase = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(initialDatabase);
		BestFirst<DatabaseNode, String> search = new BestFirst<>(generator, new DatabaseNodeEvaluator());
		search.setTimeoutForComputationOfF(600000, node -> 100.0);

		// Add graph visualizer
		new SimpleGraphVisualizationWindow<Node<DatabaseNode, Double>>(search).getPanel().setTooltipGenerator(n -> {
			StringBuilder sb = new StringBuilder();
			sb.append(n.getPoint().toString());
			sb.append(", score = ");
			sb.append(String.format("%.2f", n.getInternalLabel()));
			return sb.toString();
		});

		List<DatabaseNode> solutions = null;
		while ((solutions = search.nextSolution()) != null) {
			System.out.println(solutions.get(solutions.size() - 1));
		}
	}

}
