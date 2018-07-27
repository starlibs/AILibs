package autofe.db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import autofe.db.model.database.Attribute;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardFeature;
import autofe.db.model.database.Table;
import autofe.db.search.DatabaseGraphGenerator;
import autofe.db.search.DatabaseNode;
import autofe.db.search.DatabaseNodeEvaluator;
import autofe.db.util.DBUtils;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class SearchTest {

	private static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";

	// @Test
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

	@Test
	public void testLexicographicOrderEmptyFeatureList() {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(db);
		SuccessorGenerator<DatabaseNode, String> sg = generator.getSuccessorGenerator();
		DatabaseNode node = new DatabaseNode();
		Collection<NodeExpansionDescription<DatabaseNode, String>> successors = sg.generateSuccessors(node);
		assertEquals(9, successors.size());
		
		String descriptions = concatExpansionDescriptions(successors);
		
		//Forward features
		assertTrue(descriptions.contains("Forward: Balance"));
		assertTrue(descriptions.contains("Forward: BankName"));
		assertTrue(descriptions.contains("Forward: Credible"));
		assertTrue(descriptions.contains("Forward: FirstName"));
		assertTrue(descriptions.contains("Forward: TransactionCounter"));

		// Backward features
		assertTrue(descriptions.contains("Backward: Price"));
		assertTrue(descriptions.contains("Backward: OrderDate"));
		assertTrue(descriptions.contains("Backward: ProductName"));

		// Exit edge
		assertTrue(descriptions.contains("Exit"));
	}

	@Test
	public void testLexicographicOrderForwardFeatureList() {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(db);
		SuccessorGenerator<DatabaseNode, String> sg = generator.getSuccessorGenerator();

		Table bankAccount = DBUtils.getTableByName("BankAccount", db);
		Attribute credible = DBUtils.getAttributeByName("Credible", bankAccount);
		ForwardFeature ff = new ForwardFeature(credible);

		DatabaseNode node = new DatabaseNode(Collections.singletonList(ff), false);
		Collection<NodeExpansionDescription<DatabaseNode, String>> successors = sg.generateSuccessors(node);
		assertEquals(6, successors.size());

		String descriptions = concatExpansionDescriptions(successors);

		// Forward features
		assertFalse(descriptions.contains("Forward: Balance"));
		assertFalse(descriptions.contains("Forward: BankName"));
		assertFalse(descriptions.contains("Forward: Credible"));
		assertTrue(descriptions.contains("Forward: FirstName"));
		assertTrue(descriptions.contains("Forward: TransactionCounter"));

		// Backward features
		assertTrue(descriptions.contains("Backward: Price"));
		assertTrue(descriptions.contains("Backward: OrderDate"));
		assertTrue(descriptions.contains("Backward: ProductName"));

		// Exit edge
		assertTrue(descriptions.contains("Exit"));

	}

	// TODO: Add test case for a feature list containing a backward feature

	private String concatExpansionDescriptions(Collection<NodeExpansionDescription<DatabaseNode, String>> successors) {
		StringBuilder sb = new StringBuilder();
		for (NodeExpansionDescription<DatabaseNode, String> description : successors) {
			sb.append(description.getAction());
			sb.append("|");
		}
		return sb.toString();
	}

	@Test
	public void testIntermediateNode() {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(db);
		SuccessorGenerator<DatabaseNode, String> sg = generator.getSuccessorGenerator();
		Table product = DBUtils.getTableByName("Product", db);
		Attribute price = DBUtils.getAttributeByName("Price", product);

		// Create intermediate feature
		BackwardFeature bf = new BackwardFeature(price);
		DatabaseNode node = new DatabaseNode(Collections.singletonList(bf), false);

		Collection<NodeExpansionDescription<DatabaseNode, String>> successors = sg.generateSuccessors(node);
		assertEquals(4, successors.size());

		String descriptions = concatExpansionDescriptions(successors);
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], SUM>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], MIN>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], MAX>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], AVG>"));

	}

}
