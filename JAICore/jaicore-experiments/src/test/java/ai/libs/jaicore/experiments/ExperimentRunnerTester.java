package ai.libs.jaicore.experiments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.FixMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.db.DBTester;
import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExperimentRunnerTester extends DBTester implements IExperimentSetEvaluator {

	public static Stream<Arguments> getSetups() throws IOException {

		List<Arguments> args = Stream.of(Arguments.of(new File("testrsc/experiment-constrained.cfg"), (Consumer<ExperimentRunner>) (final ExperimentRunner runner) -> {
			try {
				runner.randomlyConductExperiments();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}), Arguments.of(new File("testrsc/experiment-unconstrained.cfg"), (Consumer<ExperimentRunner>) (final ExperimentRunner runner) -> {
			try {
				runner.sequentiallyConductExperiments();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}), Arguments.of(new File("testrsc/experiment-unconstrained.cfg"), (Consumer<ExperimentRunner>) (final ExperimentRunner runner) -> {
			try {
				runner.randomlyConductExperiments();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}), Arguments.of(new File("testrsc/experiment-constrained.cfg"), (Consumer<ExperimentRunner>) (final ExperimentRunner runner) -> {
			try {
				runner.sequentiallyConductExperiments();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		})).collect(Collectors.toList());

		/* build cartesian product with the database parameters */
		List<Arguments> combos = new ArrayList<>();
		for (List<Arguments> tuple : SetUtil.cartesianProduct(Arrays.asList(DBTester.getDatabaseAdapters().collect(Collectors.toList()), args))) {
			Object[] arr = new Object[3];
			arr[0] = tuple.get(0).get()[0];
			arr[1] = ConfigFactory.create(IExperimentSetConfig.class).loadPropertiesFromFile((File)tuple.get(1).get()[0]);
			arr[2] = tuple.get(1).get()[1];
			combos.add(Arguments.of(arr));
		}
		return combos.stream();
	}

	private static final String TABLE = "exptable";

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
				ObjectNode node = (ObjectNode) (new ObjectMapper().readTree(t.get(1)));
				return Math.abs(node.get("number").asInt() - Integer.valueOf(t.get(0))) == 1;
			} catch (IOException e) {
				throw new UnsupportedOperationException(e);
			}
		}
	}

	private String getTablename(final IDatabaseAdapter adapter) {
		String table = TABLE + "_" + adapter.getClass().getName().replace(".", "_");
		this.logger.info("Using table {}" , table);
		return table;
	}



	public IExperimentDatabaseHandle getHandle(final Object config) {
		IDatabaseAdapter adapter = this.reportConfigAndGetAdapter(config);
		return new ExperimenterMySQLHandle(adapter, this.getTablename(adapter));
	}

	private int getNumberOfTotalExperiments(final IExperimentSetConfig config) {
		return config.getConstraints() == null ? (3 * new Generator().getNumberOfValues()) : 4;
	}

	@ParameterizedTest(name="Experiment Creation")
	@MethodSource("getSetups")
	public void test1ThatAllExperimentsAreCreated(final Object dbConfig, final IExperimentSetConfig config, final Consumer<ExperimentRunner> experimentRunnerMethod)
			throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {

		/* check that running the experiments works */
		IExperimentDatabaseHandle handle = this.getHandle(dbConfig);
		ExperimentDatabasePreparer preparer = new ExperimentDatabasePreparer(config, handle);
		Collection<ExperimentDBEntry> experimentDBEntries = preparer.synchronizeExperiments();
		int expected = this.getNumberOfTotalExperiments(config);
		assertEquals(expected, experimentDBEntries.size());
		Collection<ExperimentDBEntry> experiments = handle.getAllExperiments();
		assertEquals(expected, experiments.size());
	}

	@ParameterizedTest(name="Experiment Creation")
	@MethodSource("getSetups")
	public void test2ThatAllExperimentsAreConductedExactlyOnceUsingParallelization(final Object dbConfig, final IExperimentSetConfig config, final Consumer<ExperimentRunner> experimentRunnerMethod) throws ExperimentDBInteractionFailedException, InterruptedException {

		/* check that running the experiments works */
		IExperimentDatabaseHandle handle = this.getHandle(dbConfig);
		ExperimentRunner runner = new ExperimentRunner(config, this, handle);
		experimentRunnerMethod.accept(runner);
		Collection<ExperimentDBEntry> conductedExperiments = handle.getConductedExperiments();
		assertEquals(this.getNumberOfTotalExperiments(config), conductedExperiments.size());

		/* check results for every experiment */
		for (ExperimentDBEntry entry : conductedExperiments) {
			assertEquals(this.computeExperimentValue(entry.getExperiment()), entry.getExperiment().getValuesOfResultFields().get("C"));
		}
	}

	@ParameterizedTest(name="Experiment Creation")
	@MethodSource("getSetups")
	public void test3ThatErasureOfSingleExperimentsWorks(final Object dbConfig, final IExperimentSetConfig config, final Consumer<ExperimentRunner> experimentRunnerMethod) throws ExperimentDBInteractionFailedException {

		/* erase all experiments */
		IExperimentDatabaseHandle handle = this.getHandle(dbConfig);
		handle.setup(config);
		int n = handle.getConductedExperiments().size();
		assertEquals(this.getNumberOfTotalExperiments(config), n); // check that all experiments are still there
		for (ExperimentDBEntry entry : handle.getConductedExperiments()) {
			handle.deleteExperiment(entry);
			assertEquals(n - (long) 1, handle.getConductedExperiments().size());
			n--;
		}
	}

	@ParameterizedTest(name="Experiment Creation")
	@MethodSource("getSetups")
	public void test4Deletion(final Object dbConfig, final IExperimentSetConfig config, final Consumer<ExperimentRunner> experimentRunnerMethod) throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException {

		IExperimentDatabaseHandle handle = this.getHandle(dbConfig);
		handle.setup(config);
		handle.deleteDatabase();
		boolean correctExceptionObserved = false;
		try {
			handle.createAndGetExperiment(new Experiment(0, 0, new HashMap<>()));
			fail("The create statement should throw an exception.");
		} catch (ExperimentDBInteractionFailedException e) {
			//			correctExceptionObserved = (e.getCause() instanceof MySQLSyntaxErrorException)
			//					&& ((MySQLSyntaxErrorException) e.getCause()).getMessage().equals("Table '" + dbConfig.getDBDatabaseName() + "." + dbConfig.getDBTableName() + "' doesn't exist");
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
