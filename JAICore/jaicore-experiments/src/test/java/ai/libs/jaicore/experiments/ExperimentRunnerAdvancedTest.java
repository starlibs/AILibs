package ai.libs.jaicore.experiments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aeonbits.owner.ConfigFactory;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.logging.LoggerUtil;

public class ExperimentRunnerAdvancedTest extends AExperimentTester {

	private final static String EXECUTOR_INFO = "Testexecutor"; // for the executor field in the database

	private final static IExperimentSetConfig EX_CONFIG = (IExperimentSetConfig) ConfigFactory.create(IExperimentSetConfig.class).loadPropertiesFromFile(new File("testrsc/artificial/experiment1.cfg"));

	private ExecutorService executorService;

	@BeforeEach
	public void prepareExecutor() {
		this.executorService = Executors.newFixedThreadPool(100);
	}

	@AfterEach
	public void shutdownExecutor() throws InterruptedException {
		this.executorService.shutdownNow();
		this.executorService.awaitTermination(1, TimeUnit.MINUTES);
	}

	private void prepareTable(final Object dbConfig) throws Exception {
		this.prepareTable(this.getHandle(dbConfig));
	}

	private void prepareTable(final IExperimentDatabaseHandle handle) throws Exception {
		this.logger.info("Preparing table.");

		/* delete database */
		try {
			handle.deleteDatabase();
			this.logger.info("Deleted entries of database.");
		} catch (ExperimentDBInteractionFailedException exception) {
			if (exception.getCause() instanceof MySQLSyntaxErrorException) {
				// The table is not found. No previous round
			}
		}

		/* create database from scratch */
		ExperimentDatabasePreparer preparer = new ExperimentDatabasePreparer(EX_CONFIG, handle);
		preparer.synchronizeExperiments();

		/* start some experiments */
		int numExperimentsToStart = 10;
		List<ExperimentDBEntry> startedExperiments = IntStream.range(0, numExperimentsToStart).boxed().map(i -> {
			try {
				this.logger.debug("Starting experiment {}", i);
				return handle.startNextExperiment(EXECUTOR_INFO).get();
			} catch (ExperimentDBInteractionFailedException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
		assertEquals(numExperimentsToStart, handle.getRunningExperiments().size());

		/* mark some of the started experiments as finished */
		int numExperimentsToFinish = 5;
		assertTrue(numExperimentsToFinish <= numExperimentsToStart);
		this.logger.info("Now mark {} experiments as finished.", numExperimentsToFinish);
		startedExperiments.stream().limit(numExperimentsToFinish).forEach(exp -> {
			try {
				handle.finishExperiment(exp);
				this.logger.debug("Experiment {} marked as finished.", exp.getId());
			} catch (ExperimentDBInteractionFailedException e) {
				throw new RuntimeException(e);
			}
		});
		assertEquals(numExperimentsToStart - numExperimentsToFinish, handle.getRunningExperiments().size());
		this.logger.info("Table preparation ready.");
	}

	public void eraseTable(final Object dbConfig) throws SQLException, IOException {
		IDatabaseAdapter adapter = this.reportConfigAndGetAdapter(dbConfig);
		String tablename = this.getTablename(adapter);
		String query = "DROP TABLE `" + tablename + "`";
		this.logger.info("Erazing table via query {}.", query);
		adapter.update(query);
		this.logger.info("Table erazed.");
		assertFalse(adapter.doesTableExist(tablename));
	}

	public void runExperiments(final String methodName, final Object dbConfig, final BiConsumer<ExperimentRunner, Integer> runMethod) throws Exception {
		IExperimentDatabaseHandle handle = this.getHandle(dbConfig);
		this.prepareTable(dbConfig);

		/* This is a dummy evaluator that just writes the string "result" into the column with name R */
		IExperimentSetEvaluator evaluator = (final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) -> {
			Map<String, Object> result = new HashMap<>();
			this.logger.info("Writing result for experiment {}", experimentEntry.getId());
			result.put("R", "result");
			processor.processResults(result);
		};

		/* conduct experiments */
		ExperimentRunner runner = new ExperimentRunner(EX_CONFIG, evaluator, handle);
		runner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		long startTime = System.currentTimeMillis();
		int threadCount = 2;
		int experimentsPerThread = 2;
		List<Future> allJobs = new ArrayList<>();
		this.logger.info("Enqueuing for {} thread {} jobs (each). That is, we will conduct {} experiments in total.", threadCount, experimentsPerThread, threadCount * experimentsPerThread);
		for (int i = 0; i < threadCount; i++) {
			Future<?> job = this.executorService.submit(() -> {
				runMethod.accept(runner, experimentsPerThread);
			});
			allJobs.add(job);
		}
		this.logger.info("Now waiting for jobs to finish.");
		allJobs.forEach(future -> {
			try {
				future.get();
				this.logger.debug("Observed result.");
			} catch (InterruptedException | ExecutionException e) {
				this.logger.error("Error executing experiments in {} mode.", methodName, e);
			}
		});
		long runtimeDuration = System.currentTimeMillis() - startTime;
		this.logger.info("It took {} seconds to run {} experiments in {} mode.", TimeUnit.MILLISECONDS.toSeconds(runtimeDuration), threadCount * experimentsPerThread, methodName);
		this.eraseTable(dbConfig);
	}

	@ParameterizedTest(name = "Random Batch")
	@MethodSource("getDatabaseConfigs")
	public void testRandomBatchExperimentRuntime(final Object dbConfig) throws Exception {
		this.runExperiments("random batch", dbConfig, (runner, count) -> {
			try {
				runner.randomlyConductExperiments(count);
			} catch (ExperimentDBInteractionFailedException | InterruptedException e) {
				this.logger.error("Error random batch: ", e);
				Assert.fail();
			}
		});
	}

	@ParameterizedTest(name = "Sequential")
	@MethodSource("getDatabaseConfigs")
	public void testSequentialExperimentRuntime(final Object dbConfig) throws Exception {
		this.runExperiments("sequential", dbConfig, (runner, count) -> {
			try {
				runner.sequentiallyConductExperiments(count);
			} catch (ExperimentDBInteractionFailedException | InterruptedException e) {
				this.logger.error("Error sequentiall batch: ", e);
				Assert.fail();
			}
		});
	}

	@ParameterizedTest(name = "Start 10")
	@MethodSource("getDatabaseConfigs")
	public void testStart10Experiments(final Object dbConfig) throws Exception {
		this.prepareTable(dbConfig);
		IExperimentDatabaseHandle handle = this.getHandle(dbConfig);
		handle.setup(EX_CONFIG);
		for (int i = 0; i < 10; i++) {
			Optional<ExperimentDBEntry> experimentDBEntry = handle.startNextExperiment(EXECUTOR_INFO);
			Assert.assertTrue("There should be a started experiment.", experimentDBEntry.isPresent());
		}
		this.eraseTable(dbConfig);
	}

	@ParameterizedTest(name = "Run In Parallel")
	@MethodSource("getDatabaseConfigs")
	public void testStartExperimntsInParallel(final Object dbConfig) throws Exception {

		// create a new handle for each thread:
		this.prepareTable(dbConfig);
		List<Future> allJobs = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			// enqueue a pull request job.
			// THe job may fail if no random experiment could be started.
			// The job may also fail if it takes to long to start a new random experiment.
			Future<?> submit = this.executorService.submit(() -> {
				try {
					IExperimentDatabaseHandle handle = this.getHandle(dbConfig);
					handle.setup(EX_CONFIG);
					Optional<ExperimentDBEntry> experimentDBEntry = null;
					long startedTime = System.currentTimeMillis();
					experimentDBEntry = handle.startNextExperiment(EXECUTOR_INFO);

					long runtime = System.currentTimeMillis() - startedTime;
					Assert.assertTrue(String.format("Starting a random experiment should should take less than 5s but took %f", runtime / 1000.0), runtime < 5000);
					Assert.assertTrue(experimentDBEntry.isPresent());

				} catch (Exception e) {
					this.logger.error("Error trying to get a random experiment.", e);
					throw new RuntimeException(e);
				}
				Awaitility.await().atLeast(Duration.ofMillis(new Random().nextInt(10)));
			});
			allJobs.add(submit);
		}
		List<?> unsuccessfulJobErros = allJobs.stream().map(job -> {
			try {
				job.get();
			} catch (ExecutionException e) {
				return Optional.of(e);
			} catch (InterruptedException e) {
				return Optional.empty();
			}
			return Optional.empty();
		}).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		if (!unsuccessfulJobErros.isEmpty()) {
			for (Object unsuccessfulJobErro : unsuccessfulJobErros) {
				this.logger.error("Job failed:", (Exception) unsuccessfulJobErro);
			}
			Assert.fail("Jobs failed: \n\t" + unsuccessfulJobErros.size());
		}
		this.eraseTable(dbConfig);
	}

}
