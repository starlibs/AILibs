package ai.libs.jaicore.experiments;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.*;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.aeonbits.owner.ConfigFactory;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArtificialExperiments {

	private final static Logger logger = LoggerFactory.getLogger(ArtificialExperiments.class);

	private final static IExperimentSetConfig EX_CONFIG = (IExperimentSetConfig) ConfigFactory
			.create(IExperimentSetConfig.class)
			.loadPropertiesFromFile(new File("testrsc/artificial/experiment1.cfg"));

	private final static IDatabaseConfig DB_CONFIG = (IDatabaseConfig) ConfigFactory
			.create(IDatabaseConfig.class)
			.loadPropertiesFromFile(new File("testrsc/dbconfig.properties"));


	private final static ThreadLocal<ExperimenterMySQLHandle> localHandle = ThreadLocal.withInitial(() -> {
		ExperimenterMySQLHandle handle = new ExperimenterMySQLHandle(DB_CONFIG);
		try {
			handle.setup(EX_CONFIG);
		} catch (ExperimentDBInteractionFailedException e) {
			throw new RuntimeException(e);
		}
		return handle;
	});

	private ExecutorService executorService;

	@Before
	public void prepareExecutor() {
		executorService  = Executors.newFixedThreadPool(100);
	}

	@After
	public void shutdownExecutor() throws InterruptedException {
		for (int i = 0; i < 5 && !executorService.isShutdown(); i++) {
			executorService.shutdownNow();
			Thread.sleep(100);
		}
	}

	@Before
	public void prepareDB() throws Exception{
		ExperimenterMySQLHandle handle = new ExperimenterMySQLHandle(DB_CONFIG);
		// Delete the database:
		try {
			handle.deleteDatabase();
		} catch(ExperimentDBInteractionFailedException exception) {
			if(exception.getCause() instanceof MySQLSyntaxErrorException) {
				// The table is not found. No previous round
			}
		}
		// create the table from new:
		ExperimentDatabasePreparer preparer = new ExperimentDatabasePreparer(EX_CONFIG, handle);
		preparer.synchronizeExperiments();
		// start 100 experiments:
		List<ExperimentDBEntry> startedExperiments = IntStream.range(0, 100).boxed().map(i -> {
			try {
				return handle.startNextExperiment().get();
			} catch (ExperimentDBInteractionFailedException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
		// finish 50
		startedExperiments.stream().limit(5).forEach(exp ->
		{
			try {
				handle.finishExperiment(exp);
			} catch (ExperimentDBInteractionFailedException e) {
				throw new RuntimeException(e);
			}
		});
	}


	public void runExperiments(BiConsumer<ExperimentRunner, Integer> runMethod, String methodName) throws ExperimentDBInteractionFailedException, InterruptedException {
		ExperimenterMySQLHandle handle = new ExperimenterMySQLHandle(DB_CONFIG);
		IExperimentSetEvaluator evaluator =
				(ExperimentDBEntry experimentEntry,
				 IExperimentIntermediateResultProcessor processor) -> {
					Experiment experiment = experimentEntry.getExperiment();
					Map<String, Object> result = new HashMap<>();
					result.put("R", "result");
					processor.processResults(result);
				};

		ExperimentRunner runner = new ExperimentRunner(EX_CONFIG, evaluator, handle);
		long startTime = System.currentTimeMillis();
		int threadCount = 20;
		int experimentsPerThread = 100;
		List<Future> allJobs = new ArrayList<>();
		for (int i = 0; i < threadCount; i++) {
			Future<?> job = executorService.submit(() -> {
				runMethod.accept(runner, experimentsPerThread);
			});
			allJobs.add(job);
		}
		allJobs.forEach(future -> {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error executing experiments in {} mode.", methodName, e);
			}
		});
		long runtimeDuration = System.currentTimeMillis() - startTime;
		logger.info("It took {} seconds to run {} experiments in {} mode.", TimeUnit.MILLISECONDS.toSeconds(runtimeDuration), threadCount * experimentsPerThread, methodName);
	}

	@Test
	public void testRandomBatchExperimentRuntime() throws ExperimentDBInteractionFailedException, InterruptedException {
		runExperiments((runner, count) -> {
			try {
				runner.randomlyConductExperiments(count);
			} catch (ExperimentDBInteractionFailedException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}, "random batch");
	}

	@Test
	public void testSequentialExperimentRuntime() throws ExperimentDBInteractionFailedException, InterruptedException {
		runExperiments((runner, count) -> {
			try {
				runner.sequentiallyConductExperiments(count);
			} catch (ExperimentDBInteractionFailedException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}, "sequential");
	}

	@Test
	public void testStart10Experiments() throws Exception {
		ExperimenterMySQLHandle handle = localHandle.get();
		for (int i = 0; i < 10; i++) {
			Optional<ExperimentDBEntry> experimentDBEntry = handle.startNextExperiment();
			Assert.assertTrue("There should be a started experiment.", experimentDBEntry.isPresent());
		}
	}

	@Test
	public void testStartExperimntsInParallel() throws Exception {
		// create a new handle for each thread:
		List<Future> allJobs = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			// enqueue a pull request job.
			// THe job may fail if no random experiment could be started.
			// The job may also fail if it takes to long to start a new random experiment.
			Future<?> submit = executorService.submit(() -> {
				try {
					ExperimenterMySQLHandle handle = localHandle.get();
					Optional<ExperimentDBEntry> experimentDBEntry = null;
					long startedTime = System.currentTimeMillis();
						experimentDBEntry = handle.startNextExperiment();

					long runtime = System.currentTimeMillis() - startedTime;
					Assert.assertTrue(String.format("Starting a random experiment should should take less than 1000 ms but took %d",
							runtime),
							runtime < 1000);
					Assert.assertTrue(experimentDBEntry.isPresent());

				} catch (Exception e) {
					logger.error("Error trying to get a random experiment.", e);
					throw new RuntimeException(e);
				}
				try {
					Thread.sleep(new Random().nextInt(10));
				} catch (InterruptedException e) {
					throw new RuntimeException();
				}
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
		}).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());
		if(!unsuccessfulJobErros.isEmpty()) {
			for (Object unsuccessfulJobErro : unsuccessfulJobErros) {
				logger.error("Job failed:", (Exception) unsuccessfulJobErro);
			}
			Assert.fail("Jobs failed: \n\t" + unsuccessfulJobErros.size());
		}
	}

}
