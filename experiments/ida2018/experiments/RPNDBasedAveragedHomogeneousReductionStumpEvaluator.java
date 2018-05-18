package ida2018.experiments;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import de.upb.crc901.reduction.Util;
import de.upb.crc901.reduction.single.MySQLReductionExperiment;
import de.upb.crc901.reduction.single.heterogeneous.simplerpnd.MySQLExperimentRunner;
import jaicore.ml.WekaUtil;

/**
 * This determines reduction stumps that have not been evaluated and evaluates them
 * 
 * @author fmohr
 *
 */
public class RPNDBasedAveragedHomogeneousReductionStumpEvaluator {

	public static void main(String[] args) throws Exception {
		File folder = new File(args[0]);

		/* setup the experiment dimensions */
		int numSeeds = 25;
		List<Integer> seeds = new ArrayList<>();
		for (int seed = 1; seed <= numSeeds; seed++)
			seeds.add(seed);
		Collections.shuffle(seeds);
		List<File> datasetFiles = WekaUtil.getDatasetsInFolder(folder);
		Collections.shuffle(datasetFiles);
		
		int maxNumberOfExperiments = Integer.MAX_VALUE;
		int maxNumberOfSuccessfulExperiments = maxNumberOfExperiments;

		/* compute total number of experiments */

		/* conduct next experiments */
		MySQLExperimentRunner runner = new MySQLExperimentRunner("isys-db.cs.upb.de", "ida2018", "WsFg33sE6aghabMr", "results_reduction");
//		ExecutorService pool = Executors.newFixedThreadPool(nCPUs);
		AtomicInteger cnt = new AtomicInteger();
		AtomicInteger cntSuccessful = new AtomicInteger();

		/* launch threads for execution */
		for (int seed : seeds) {
			for (File dataFile : datasetFiles) {
				for (String learner : WekaUtil.getBasicLearners()) {

					/* wait until all problems have been solved */
					if (cnt.incrementAndGet() > maxNumberOfExperiments) {
						// System.out.println("Shutting down thread pool");
						// pool.shutdown();
						return;
					}
					System.out.println(learner + " on " + dataFile.getName());

					/* create constants that describe the experiment */
					final int fixedSeed = seed;
					final File fixedFile = new File(dataFile.getAbsolutePath());
					final String leftClassifier = learner;
					final String innerClassifier = learner;
					final String rightClassifier = learner;

					// pool.submit(new Runnable() {
					// @Override
					// public void run() {
					try {
						/* now conduct the experiment */
						MySQLReductionExperiment experiment = runner.createAndGetExperimentIfNotConducted(fixedSeed, fixedFile, leftClassifier, innerClassifier,
								rightClassifier);
						try {
							if (experiment == null)
								continue;

							runner.conductExperiment(experiment);
							if (cntSuccessful.incrementAndGet() >= maxNumberOfSuccessfulExperiments) {
								System.out.println("Maximum number has reached, ignoring further executions in thread " + Thread.currentThread().getName());
								return;
							}
						} catch (RuntimeException e) {
							String classifier = null;
							if (e.getMessage().contains("RPND"))
								classifier = "rpnd";
							else if (e.getMessage().contains("inner"))
								classifier = "inner";
							else if (e.getMessage().contains("#1"))
								classifier = "left";
							else if (e.getMessage().contains("#2"))
								classifier = "right";
							else
								e.printStackTrace();
							runner.associateExperimentWithException(experiment, classifier, e.getCause());
						} catch (Throwable e) {
							e.printStackTrace();
//							 runner.associateExperimentWithException(experiment, e);
						}

					} catch (Throwable e) {
						e.printStackTrace();
					}

				}
				// }
				// }
			}
		}
	}
}
