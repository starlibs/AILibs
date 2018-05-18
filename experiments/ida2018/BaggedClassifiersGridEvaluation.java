package ida2018;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import jaicore.basic.MySQLAdapter;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.MulticlassEvaluator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.meta.Bagging;
import weka.core.Instances;

/**
 * This determines reduction stumps that have not been evaluated and evaluates them
 * 
 * @author fmohr
 *
 */
public class BaggedClassifiersGridEvaluation {

	public static void main(String[] args) throws Exception {
		File folder = new File(args[0]);

		/* setup the experiment dimensions */
		int numSeeds = 10;
		List<Integer> seeds = new ArrayList<>();
		for (int seed = 1; seed <= numSeeds; seed++)
			seeds.add(seed);
		Collections.shuffle(seeds);
		List<File> datasetFiles = WekaUtil.getDatasetsInFolder(folder);
		Collections.shuffle(datasetFiles);

		int k = 10;

		/* conduct next experiments */
		MySQLAdapter adapter = new MySQLAdapter("isys-db.cs.upb.de", "ida2018", "WsFg33sE6aghabMr", "results_reduction");

		/* launch threads for execution */
		for (int seed : seeds) {
			System.out.println("Considering seed " + seed);
			for (File dataFile : datasetFiles) {
				System.out.println("\tConsidering data file " + dataFile.getAbsolutePath());
				for (String learner : WekaUtil.getBasicLearners()) {

					/* wait until all problems have been solved */
					System.out.println("\t\t" + learner + " on " + dataFile.getName());

					/* create constants that describe the experiment */
					final int fixedSeed = seed;
					final File fixedFile = new File(dataFile.getAbsolutePath());

					Map<String, Object> values = new HashMap<>();
					values.put("dataset", fixedFile.getName());
					values.put("classifier", learner);
					values.put("iterations", k);
					values.put("seed", seed);

					try {
						int evalId = adapter.insert("baggedclassifiers", values);

						try {
							Bagging c = new Bagging();
							c.setClassifier(AbstractClassifier.forName(learner, null));
							c.setSeed(seed);
							c.setBagSizePercent(70);
							c.setNumIterations(k);
							Instances data = new Instances(new BufferedReader(new FileReader(dataFile)));
							data.setClassIndex(data.numAttributes() - 1);
							List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(seed), .7f);
							long start = System.currentTimeMillis();
							c.buildClassifier(split.get(0));
							int buildTime = (int) (System.currentTimeMillis() - start);
							MulticlassEvaluator eval = new MulticlassEvaluator(new Random(seed));
							start = System.currentTimeMillis();
							double loss = eval.loss(c, split.get(1));
							int valTime = (int) (System.currentTimeMillis() - start);
							List<Object> update = new ArrayList<>();
							update.add(loss);
							update.add(buildTime);
							update.add(valTime);
							update.add(evalId);
							adapter.update("UPDATE baggedclassifiers SET errorRate = ?, buildTime = ?, valTime = ? WHERE eval_id = ?", update);
							System.out.println("\t\t\tAchieved loss: " + loss);
						} catch (Throwable e) {
							List<Object> update = new ArrayList<>();
							update.add(evalId);
							adapter.update("UPDATE baggedclassifiers SET errorRate = -1 WHERE eval_id = ?", update);
						}
					} catch (MySQLIntegrityConstraintViolationException e) {
						if (e.getMessage().startsWith("Duplicate entry"))
							System.out.println("Ignoring existing experiment ...");
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
