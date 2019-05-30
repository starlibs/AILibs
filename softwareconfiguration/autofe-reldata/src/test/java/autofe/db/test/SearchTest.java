package autofe.db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.Attribute;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardFeature;
import autofe.db.model.database.Path;
import autofe.db.model.database.Table;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.search.DatabaseGraphGenerator;
import autofe.db.search.DatabaseNode;
import autofe.db.search.DatabaseNodeEvaluator;
import autofe.db.util.DBUtils;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class SearchTest {

	private static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";

	// @Test
	public void testSearch() throws Exception {
		Database initialDatabase = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(initialDatabase);
		DatabaseNodeEvaluator evaluator = new DatabaseNodeEvaluator(generator);

		GraphSearchWithSubpathEvaluationsInput<DatabaseNode, String, Double> tree = new GraphSearchWithSubpathEvaluationsInput<>(generator, evaluator);

		BestFirst<GraphSearchWithSubpathEvaluationsInput<DatabaseNode, String, Double>, DatabaseNode, String, Double> search = new BestFirst<>(tree);
		search.setTimeoutForComputationOfF(60000, node -> 100.0);

		SearchGraphPath<DatabaseNode, String> solution = null;
		while ((solution = search.nextSolutionCandidate()) != null) {
			System.out.println(solution.getNodes().get(solution.getNodes().size() - 1));
		}
	}

	@Test
	public void testLexicographicOrderEmptyFeatureList() throws Exception {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(db);
		SuccessorGenerator<DatabaseNode, String> sg = generator.getSuccessorGenerator();
		DatabaseNode node = new DatabaseNode();
		Collection<NodeExpansionDescription<DatabaseNode, String>> successors = sg.generateSuccessors(node);
		assertEquals(6, successors.size());

		String descriptions = this.concatExpansionDescriptions(successors);

		// Forward features
		assertTrue(descriptions.contains("Forward: Balance"));
		assertTrue(descriptions.contains("Forward: BankName"));
		// Target
		assertFalse(descriptions.contains("Forward: Credible"));
		assertTrue(descriptions.contains("Forward: FirstName"));
		assertTrue(descriptions.contains("Forward: TransactionCounter"));

		// Backward features
		assertTrue(descriptions.contains("Backward: Price"));
		assertFalse(descriptions.contains("Backward: OrderDate"));
		assertFalse(descriptions.contains("Backward: ProductName"));

		// Exit edge
		assertTrue(descriptions.contains("Exit"));
	}

	@Test
	public void testLexicographicOrderForwardFeatureList() throws Exception {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(db);
		SuccessorGenerator<DatabaseNode, String> sg = generator.getSuccessorGenerator();

		Table bankAccount = DBUtils.getTableByName("BankAccount", db);
		Attribute bankName = DBUtils.getAttributeByName("BankName", bankAccount);
		ForwardFeature ff = new ForwardFeature(bankName);

		DatabaseNode node = new DatabaseNode(Collections.singletonList(ff), false);
		Collection<NodeExpansionDescription<DatabaseNode, String>> successors = sg.generateSuccessors(node);
		assertEquals(4, successors.size());

		String descriptions = this.concatExpansionDescriptions(successors);

		// Forward features
		assertFalse(descriptions.contains("Forward: Balance"));
		assertFalse(descriptions.contains("Forward: BankName"));
		// Target
		assertFalse(descriptions.contains("Forward: Credible"));
		assertTrue(descriptions.contains("Forward: FirstName"));
		assertTrue(descriptions.contains("Forward: TransactionCounter"));

		// Backward features
		assertTrue(descriptions.contains("Backward: Price"));
		assertFalse(descriptions.contains("Backward: OrderDate"));
		assertFalse(descriptions.contains("Backward: ProductName"));

		// Exit edge
		assertTrue(descriptions.contains("Exit"));

	}

	@Test
	public void testLexicographicOrderBackwardFeatureList() throws Exception {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(db);
		SuccessorGenerator<DatabaseNode, String> sg = generator.getSuccessorGenerator();

		Table product = DBUtils.getTableByName("Product", db);
		Attribute price = DBUtils.getAttributeByName("Price", product);
		BackwardFeature bf = new BackwardFeature(price);
		Path path = bf.getPath();
		// Complete path
		path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.MAX);
		path.addPathElement(new BackwardRelationship("Customer", "Orders", "CustomerId"), AggregationFunction.AVG);

		DatabaseNode node = new DatabaseNode(Collections.singletonList(bf), false);
		Collection<NodeExpansionDescription<DatabaseNode, String>> successors = sg.generateSuccessors(node);
		assertEquals(6, successors.size());

		String descriptions = this.concatExpansionDescriptions(successors);

		// Forward features
		assertTrue(descriptions.contains("Forward: Balance"));
		assertTrue(descriptions.contains("Forward: BankName"));
		// Target
		assertFalse(descriptions.contains("Forward: Credible"));
		assertTrue(descriptions.contains("Forward: FirstName"));
		assertTrue(descriptions.contains("Forward: TransactionCounter"));

		// Backward features
		assertTrue(descriptions.contains("Backward: Price"));
		assertFalse(descriptions.contains("Backward: OrderDate"));
		assertFalse(descriptions.contains("Backward: ProductName"));

		// Exit edge
		assertTrue(descriptions.contains("Exit"));

	}

	private String concatExpansionDescriptions(final Collection<NodeExpansionDescription<DatabaseNode, String>> successors) {
		StringBuilder sb = new StringBuilder();
		for (NodeExpansionDescription<DatabaseNode, String> description : successors) {
			sb.append(description.getAction());
			sb.append("|");
		}
		return sb.toString();
	}

	@Test
	public void testIntermediateNode() throws Exception {
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

		String descriptions = this.concatExpansionDescriptions(successors);
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], SUM>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], MIN>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], MAX>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], AVG>"));

	}

	@Test
	public void testIntermediateNodeDuplicateCheck2Path() throws Exception {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(db);
		SuccessorGenerator<DatabaseNode, String> sg = generator.getSuccessorGenerator();
		Table product = DBUtils.getTableByName("Product", db);
		Attribute price = DBUtils.getAttributeByName("Price", product);

		// Create complete backward feature
		BackwardFeature completeBf = new BackwardFeature(price);
		Path path = completeBf.getPath();
		path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.MAX);
		path.addPathElement(new BackwardRelationship("Customer", "Orders", "CustomerId"), AggregationFunction.AVG);

		// Create intermediate backward feature
		BackwardFeature intermediateBf = new BackwardFeature(price);

		// Create node containing the features
		List<AbstractFeature> features = new ArrayList<>();
		features.add(completeBf);
		features.add(intermediateBf);
		DatabaseNode node = new DatabaseNode(features, false);

		Collection<NodeExpansionDescription<DatabaseNode, String>> successors = sg.generateSuccessors(node);
		assertEquals(4, successors.size());

		String descriptions = this.concatExpansionDescriptions(successors);
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], SUM>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], MIN>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], MAX>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], AVG>"));

		// Get successor node for which the MAX aggregation function has been used
		DatabaseNode nextNode = null;
		for (NodeExpansionDescription<DatabaseNode, String> successor : successors) {
			if (successor.getAction().equals("Intermediate: <[Orders -> Product], MAX>")) {
				nextNode = successor.getTo();
			}
		}

		// Expand node
		successors = sg.generateSuccessors(nextNode);

		assertEquals(3, successors.size());
		descriptions = this.concatExpansionDescriptions(successors);
		assertTrue(descriptions.contains("Intermediate: <[Customer -> Orders], SUM>"));
		assertTrue(descriptions.contains("Intermediate: <[Customer -> Orders], MIN>"));
		assertTrue(descriptions.contains("Intermediate: <[Customer -> Orders], MAX>"));
		assertFalse(descriptions.contains("Intermediate: <[Customer -> Orders], AVG>"));
	}

	@Test
	public void testIntermediateNodeDuplicateCheck1Path() throws Exception {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(db);
		SuccessorGenerator<DatabaseNode, String> sg = generator.getSuccessorGenerator();
		Table product = DBUtils.getTableByName("Product", db);
		Attribute price = DBUtils.getAttributeByName("Price", product);

		// Create all backward feature starting with aggregation function MAX
		List<AbstractFeature> features = new ArrayList<>();
		for (AggregationFunction af : AggregationFunction.values()) {
			BackwardFeature completeBf = new BackwardFeature(price);
			Path path = completeBf.getPath();
			path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.MAX);
			path.addPathElement(new BackwardRelationship("Customer", "Orders", "CustomerId"), af);
			features.add(completeBf);
		}

		// Create intermediate backward feature
		BackwardFeature intermediateBf = new BackwardFeature(price);

		// Create node containing the features
		features.add(intermediateBf);
		DatabaseNode node = new DatabaseNode(features, false);

		Collection<NodeExpansionDescription<DatabaseNode, String>> successors = sg.generateSuccessors(node);
		assertEquals(3, successors.size());

		String descriptions = this.concatExpansionDescriptions(successors);
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], SUM>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], MIN>"));
		assertFalse(descriptions.contains("Intermediate: <[Orders -> Product], MAX>"));
		assertTrue(descriptions.contains("Intermediate: <[Orders -> Product], AVG>"));
	}

	@Test
	public void testStandardNodeDuplicateCheck() throws Exception {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(db);
		SuccessorGenerator<DatabaseNode, String> sg = generator.getSuccessorGenerator();
		Table product = DBUtils.getTableByName("Product", db);
		Attribute price = DBUtils.getAttributeByName("Price", product);

		List<AbstractFeature> features = new ArrayList<>();
		// Create all backward feature starting with aggregation function MAX
		for (AggregationFunction af : AggregationFunction.values()) {
			BackwardFeature completeBf = new BackwardFeature(price);
			Path path = completeBf.getPath();
			path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.MAX);
			path.addPathElement(new BackwardRelationship("Customer", "Orders", "CustomerId"), af);
			features.add(completeBf);
		}

		// Create all backward feature starting with aggregation function MIN
		for (AggregationFunction af : AggregationFunction.values()) {
			BackwardFeature completeBf = new BackwardFeature(price);
			Path path = completeBf.getPath();
			path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.MIN);
			path.addPathElement(new BackwardRelationship("Customer", "Orders", "CustomerId"), af);
			features.add(completeBf);
		}

		// Create all backward feature starting with aggregation function AVG
		for (AggregationFunction af : AggregationFunction.values()) {
			BackwardFeature completeBf = new BackwardFeature(price);
			Path path = completeBf.getPath();
			path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.AVG);
			path.addPathElement(new BackwardRelationship("Customer", "Orders", "CustomerId"), af);
			features.add(completeBf);
		}

		// Create all backward feature starting with aggregation function SUM
		for (AggregationFunction af : AggregationFunction.values()) {
			BackwardFeature completeBf = new BackwardFeature(price);
			Path path = completeBf.getPath();
			path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.SUM);
			path.addPathElement(new BackwardRelationship("Customer", "Orders", "CustomerId"), af);
			features.add(completeBf);
		}

		DatabaseNode node = new DatabaseNode(features, false);

		Collection<NodeExpansionDescription<DatabaseNode, String>> successors = sg.generateSuccessors(node);
		assertEquals(5, successors.size());

		String descriptions = this.concatExpansionDescriptions(successors);

		// Forward features
		assertTrue(descriptions.contains("Forward: Balance"));
		assertTrue(descriptions.contains("Forward: BankName"));
		// Target
		assertFalse(descriptions.contains("Forward: Credible"));
		assertTrue(descriptions.contains("Forward: FirstName"));
		assertTrue(descriptions.contains("Forward: TransactionCounter"));

		// Backward features
		assertFalse(descriptions.contains("Backward: Price"));
		assertFalse(descriptions.contains("Backward: OrderDate"));
		assertFalse(descriptions.contains("Backward: ProductName"));

		// Exit edge
		assertTrue(descriptions.contains("Exit"));

	}

}
