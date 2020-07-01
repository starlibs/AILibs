package ai.libs.jaicore.experiments;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.*;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArtificialExperiment1 {

    private final static Logger logger = LoggerFactory.getLogger(ArtificialExperiment1.class);

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
        executorService  = Executors.newFixedThreadPool(20);
    }

    @After
    public void shutdownExecutor() throws InterruptedException {
        for (int i = 0; i < 5 && !executorService.isShutdown(); i++) {
            executorService.shutdownNow();
            Thread.sleep(100);
        }
    }

    @Test
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
                return handle.startRandomExperiment().get();
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



    @Test
    public void runExperiments() throws ExperimentDBInteractionFailedException, InterruptedException {
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
        runner.randomlyConductExperiments(100);
    }

    @Test
    public void testStart10RandomExperiments() throws Exception {
        ExperimenterMySQLHandle handle = localHandle.get();
        for (int i = 0; i < 10; i++) {
            Optional<ExperimentDBEntry> experimentDBEntry = handle.startRandomExperiment();
            Assert.assertTrue("There should be a started experiment.", experimentDBEntry.isPresent());
        }
    }

    @Test
    public void testStartRandomExperimntsInParallel() throws Exception {
        // create a new handle for each thread:
        List<Future> allJobs = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            // enqueue a pull request job.
            // THe job may fail if no random experiment could be started.
            // The job may also fail if it takes to long to start a new random experiment.
            Future<?> submit = executorService.submit(() -> {
                ExperimenterMySQLHandle handle = localHandle.get();
                Optional<ExperimentDBEntry> experimentDBEntry = null;
                long startedTime = System.currentTimeMillis();
                try {
                    experimentDBEntry = handle.startRandomExperiment();

                } catch (ExperimentDBInteractionFailedException e) {
                    logger.error("Error trying to get a random experiment.", e);
                    throw new RuntimeException(e);
                }
                long runtime = System.currentTimeMillis() - startedTime;
                Assert.assertTrue(String.format("Starting a random experiment should should take less than 500 ms but took %d",
                        runtime),
                        runtime < 500);
                Assert.assertTrue(experimentDBEntry.isPresent());
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
            Assert.fail("Jobs failed: \n\t" + unsuccessfulJobErros.stream()
                    .map(ex -> {
                        Throwable e = (Throwable) ex;
                        while(e.getCause() != null && e.getCause() != e) {
                            e = e.getCause();
                        }
                        return e.getMessage();
                    })
                    .collect(Collectors.joining("\n\t")));
        }
    }

}
