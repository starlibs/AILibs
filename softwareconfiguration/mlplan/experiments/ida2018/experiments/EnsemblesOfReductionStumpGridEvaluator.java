package ida2018.experiments;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.upb.crc901.reduction.ensemble.simple.MySQLEnsembleOfSimpleOneStepReductionsExperiment;
import de.upb.crc901.reduction.ensemble.simple.MySQLEnsembleOfSimpleOneStepReductionsExperimentRunner;
import ida2018.IDA2018Util;
import jaicore.ml.WekaUtil;

/**
 * This determines reduction stumps that have not been evaluated and evaluates them
 * 
 * @author fmohr
 *
 */
public class EnsemblesOfReductionStumpGridEvaluator {

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


		/* conduct next experiments */
		MySQLEnsembleOfSimpleOneStepReductionsExperimentRunner runner = new MySQLEnsembleOfSimpleOneStepReductionsExperimentRunner("isys-db.cs.upb.de", "ida2018",
				"WsFg33sE6aghabMr", "results_reduction");

		/* launch threads for execution */
		for (int seed : seeds) {
			System.out.println("Selecting seed " + seed);
			for (File dataFile : datasetFiles) {
				if (!IDA2018Util.getConsideredDatasets().contains(dataFile.getName()) || !dataFile.exists()) {
					System.out.println("Skipping " + dataFile.getName() + " because it is not considered (anymore)");
					continue;
				}
				
				System.out.println("\tApproaching " + dataFile.getName());
				for (String baseLearner : IDA2018Util.getConsideredLearners()) {
					
					System.out.println("\t\tUsing " + baseLearner + " as the base learner.");

					/* create constants that describe the experiment */
					final int fixedSeed = seed;
					final File fixedFile = new File(dataFile.getAbsolutePath());

					try {
						/* now conduct the experiment */
						MySQLEnsembleOfSimpleOneStepReductionsExperiment experiment = runner.createAndGetExperimentIfNotConducted(fixedSeed, fixedFile, baseLearner, 10);
						try {
							if (experiment == null)
								continue;

							runner.conductExperiment(experiment);
						} catch (Throwable e) {
							e.printStackTrace();
							runner.associateExperimentWithException(experiment, baseLearner, e);
						}

					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
