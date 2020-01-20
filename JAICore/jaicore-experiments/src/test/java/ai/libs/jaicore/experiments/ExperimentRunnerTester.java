package ai.libs.jaicore.experiments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExperimentRunnerTester implements IExperimentSetEvaluator {

	@Parameters(name = "config = {0}")
	public static Collection<Object[]> setData(){

		Collection<Object[]> params = new ArrayList<>();
		params.add(new Object[] { new File("testrsc/experiment-constrained.cfg") });
		params.add(new Object[] { new File("testrsc/experiment-unconstrained.cfg") });
		return params;
	}

	public static class Generator implements IExperimentJSONKeyGenerator {

		@Override
		public int getNumberOfValues() {
			return 7;
		}

		@Override
		public ObjectNode getValue(final int i) {
			ObjectMapper om = new ObjectMapper();
			ObjectNode node = om.createObjectNode();
			node.put("number", i % 2 == 0 ? i / 2 : (i - 1) / (-2)); // even numbers will be positive (halved); odd numbers will be negated
			return node;
		}

		@Override
		public boolean isValueValid(final String value) {
			ObjectMapper om = new ObjectMapper();
			JsonNode node;
			try {
				node = om.readTree(value);
			} catch (IOException e) {
				return false;
			}
			if (!(node instanceof ObjectNode)) {
				return false;
			}
			ObjectNode castedNode = (ObjectNode) node;
			return castedNode.size() == 1 && castedNode.has("number") && castedNode.get("number").asInt() <= 2;
		}
	}

	public static class Constraint implements Predicate<List<String>> {

		private static final List<String> VALID_VALUES_A = Arrays.asList("0", "2");

		@Override
		public boolean test(final List<String> t) {
			if (t.isEmpty()) {
				return true;
			}
			if (!VALID_VALUES_A.contains(t.get(0))) {
				return false;
			}
			if (t.size() < 2) {
				return true;
			}
			try {
				ObjectNode node = (ObjectNode)(new ObjectMapper().readTree(t.get(1)));
				return Math.abs(node.get("number").asInt() - Integer.valueOf(t.get(0))) == 1;
			} catch (IOException e) {
				throw new UnsupportedOperationException(e);
			}
		}
	}

	private final IDatabaseConfig conf = (IDatabaseConfig) ConfigFactory.create(IDatabaseConfig.class).loadPropertiesFromFile(new File("testrsc/dbconfig.properties"));
	private final IExperimentDatabaseHandle handle = new ExperimenterMySQLHandle(this.conf);
	private final IExperimentSetConfig config;
	private final int numberOfTotalExperiments;

	public ExperimentRunnerTester(final File configFile) {
		super();
		this.config = (IExperimentSetConfig)ConfigFactory.create(IExperimentSetConfig.class).loadPropertiesFromFile(configFile);
		this.numberOfTotalExperiments = this.config.getConstraints() == null ? (3 * new Generator().getNumberOfValues()) : 4;
	}

	@Test
	public void test1ThatAllExperimentsAreCreated() throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {

		/* check that running the experiments works */
		ExperimentDatabasePreparer preparer = new ExperimentDatabasePreparer(this.config, this.handle);
		Collection<ExperimentDBEntry> experimentDBEntries = preparer.synchronizeExperiments();
		assertEquals(this.numberOfTotalExperiments, experimentDBEntries.size());

		Collection<ExperimentDBEntry> experiments = this.handle.getAllExperiments();
		assertEquals(this.numberOfTotalExperiments, experiments.size());
	}

	@Test
	public void test2ThatAllExperimentsAreConducted() throws ExperimentDBInteractionFailedException, InterruptedException {

		/* check that running the experiments works */
		ExperimentRunner runner = new ExperimentRunner(this.config, this, this.handle);
		runner.randomlyConductExperiments();
		Collection<ExperimentDBEntry> conductedExperiments = this.handle.getConductedExperiments();
		assertEquals(this.numberOfTotalExperiments, conductedExperiments.size());

		/* check results for every experiment */
		for (ExperimentDBEntry entry : conductedExperiments) {
			assertEquals(this.computeExperimentValue(entry.getExperiment()), entry.getExperiment().getValuesOfResultFields().get("C"));
		}
	}

	@Test
	public void test3ThatErasureOfSingleExperimentsWorks() throws ExperimentDBInteractionFailedException {

		/* erase all experiments */
		this.handle.setup(this.config);
		int n = this.handle.getConductedExperiments().size();
		assertEquals(this.numberOfTotalExperiments, n); // check that all experiments are still there
		for (ExperimentDBEntry entry : this.handle.getConductedExperiments()) {
			this.handle.deleteExperiment(entry);
			assertEquals(n - (long)1, this.handle.getConductedExperiments().size());
			n--;
		}
	}

	@Test
	public void test4Deletion() throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException {
		this.handle.setup(this.config);
		this.handle.deleteDatabase();
		boolean correctExceptionObserved = false;
		try {
			this.handle.createAndGetExperiment(new Experiment(0, 0, new HashMap<>()));
			fail("The create statement should throw an exception.");
		}
		catch (ExperimentDBInteractionFailedException e) {
			correctExceptionObserved = (e.getCause() instanceof MySQLSyntaxErrorException) && ((MySQLSyntaxErrorException)e.getCause()).getMessage().equals("Table '" + this.conf.getDBDatabaseName() + "." + this.conf.getDBTableName() + "' doesn't exist");
		}
		assertTrue(correctExceptionObserved);
	}

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException {
		Map<String, Object> results = new HashMap<>();
		results.put("C", this.computeExperimentValue(experimentEntry.getExperiment()));
		processor.processResults(results);
	}

	private String computeExperimentValue(final Experiment exp) {
		return exp.getValuesOfKeyFields().get("A") + "/" + exp.getValuesOfKeyFields().get("B");
	}
}
