package ida2018.experiments;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.upb.crc901.reduction.single.MySQLReductionExperiment;
import de.upb.crc901.reduction.single.homogeneous.bestofkatrandom.MySQLReductionExperimentRunnerWrapper;
import ida2018.IDA2018Util;
import jaicore.ml.WekaUtil;

/**
 * This determines reduction stumps that have not been evaluated and evaluates them
 * 
 * @author fmohr
 *
 */
public class RandomBestOfKHomogeneousReductionStumpGridEvaluator {

	public static void main(String[] args) throws Exception {
		File folder = new File(args[0]);

		/* setup the experiment dimensions */
		int numSeeds = 5;
		List<Integer> seeds = new ArrayList<>();
		for (int seed = 1; seed <= numSeeds; seed++)
			seeds.add(seed);
		Collections.shuffle(seeds);
		List<File> datasetFiles = WekaUtil.getDatasetsInFolder(folder);
		Collections.shuffle(datasetFiles);

		int k = 10;
		int mccvRepeats = 20;

		/* conduct next experiments */
		MySQLReductionExperimentRunnerWrapper runner = new MySQLReductionExperimentRunnerWrapper("isys-db.cs.upb.de", "ida2018", "WsFg33sE6aghabMr", "results_reduction", k,
				mccvRepeats);

		/* launch threads for execution */
		for (int seed : seeds) {
			System.out.println("Considering seed " + seed);
			for (File dataFile : datasetFiles) {
				if (!IDA2018Util.getConsideredDatasets().contains(dataFile.getName()) || !dataFile.exists()) {
					System.out.println("Skipping " + dataFile.getName() + " because it is not considered (anymore)");
					continue;
				}
				System.out.println("\tConsidering data file " + dataFile.getAbsolutePath());
				for (String learner : IDA2018Util.getConsideredLearners()){

					/* wait until all problems have been solved */
					System.out.println("\t\t" + learner + " on " + dataFile.getName());

					/* create constants that describe the experiment */
					final int fixedSeed = seed;
					final File fixedFile = new File(dataFile.getAbsolutePath());

					try {

						/* now conduct the experiment */
						MySQLReductionExperiment experiment = runner.createAndGetExperimentIfNotConducted(fixedSeed, fixedFile, learner);
						try {
							if (experiment == null)
								continue;
							runner.conductExperiment(experiment);
						} catch (Throwable e) {
							runner.associateExperimentWithException(experiment, e);
							if (!(e instanceof RuntimeException))
								e.printStackTrace();
						}

					} catch (Throwable e) {
						e.printStackTrace();
					}

				}
			}
		}
	}
}
