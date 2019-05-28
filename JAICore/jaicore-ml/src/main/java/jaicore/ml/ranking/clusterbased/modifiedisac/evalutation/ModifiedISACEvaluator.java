package jaicore.ml.ranking.clusterbased.modifiedisac.evalutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.ranking.clusterbased.customdatatypes.RankingForGroup;
import jaicore.ml.ranking.clusterbased.modifiedisac.ModifiedISAC;
import weka.core.Instance;
import weka.core.Instances;

public class ModifiedISACEvaluator {

	private ModifiedISACEvaluator() {
		/* do nothing */
	}

	private static final String CN_RANDOMFOREST = "weka.classifiers.trees.RandomForest";
	private static final Logger logger = LoggerFactory.getLogger(ModifiedISACEvaluator.class);

	private static double[] platz1my;
	private static double[] top3mymethod;
	private static double[] platz1overall;
	private static double[] top3overall;
	private static double[] untochedmy;
	private static double[] untocedoverall;
	private static double[] platz1ml;
	private static double[] top3ml;
	private static double[] untouchedml;
	private static double[] stepdifference;
	private static double[] stepdifferenceML;
	private static double[] kendallforML;
	private static double[] times;
	private static double randomForestplatz1;
	private static double[] randomForest;
	private static double naivebaismulti;
	private static double naivebais;

	public static double getNaivebais() {
		return naivebais;
	}

	public static double getNaivebaismulti() {
		return naivebaismulti;
	}

	public static double[] getRandomForest() {
		return randomForest;
	}

	public static double getRandomForestplatz1() {
		return randomForestplatz1;
	}

	public static double[] getTimes() {
		return times;
	}

	public static double[] getKendallforML() {
		return kendallforML;
	}

	public static double[] getStepdifferenceML() {
		return stepdifferenceML;
	}

	public static double[] getStepdifference() {
		return stepdifference;
	}

	public static double[] getPlatz1ml() {
		return platz1ml;
	}

	public static double[] getTop3ml() {
		return top3ml;
	}

	public static double[] getUntouchedml() {
		return untouchedml;
	}

	public static double[] getUntochedmy() {
		return untochedmy;
	}

	public static double[] getUntocedoverall() {
		return untocedoverall;
	}

	public static double[] getPlatz1my() {
		return platz1my;
	}

	public static double[] getPlatz1overall() {
		return platz1overall;
	}

	public static double[] gettop3mymethod() {
		return top3mymethod;
	}

	public static double[] getTop3overall() {
		return top3overall;
	}

	public static double[] evaluateModifiedISACLeaveOneOut(final Instances data) throws TrainingException {
		double[] results = new double[data.numInstances()];
		platz1my = new double[data.numInstances()];
		platz1overall = new double[data.numInstances()];
		top3mymethod = new double[data.numInstances()];
		top3overall = new double[data.numInstances()];
		untochedmy = new double[data.numInstances()];
		untocedoverall = new double[data.numInstances()];
		top3ml = new double[data.numInstances()];
		untouchedml = new double[data.numInstances()];
		platz1ml = new double[data.numInstances()];
		stepdifference = new double[data.numInstances()];
		stepdifferenceML = new double[data.numInstances()];
		kendallforML = new double[data.numInstances()];
		times = new double[data.numInstances()];
		randomForest = new double[data.numInstances()];
		naivebaismulti = 0;
		naivebais = 0;

		for (int i = 0; i < data.numInstances(); i++) {
			HashMap<String, Integer> positionInRanking = new HashMap<>();
			Instances trainingData = new Instances(data);
			Instances testprep = new Instances(data);
			Instances tester = new Instances(data);
			Instance reminder = tester.get(i);

			trainingData.delete(i);

			double[] overallranking = new double[22];
			int[] totalvalue = new int[22];

			for (Instance inst : trainingData) {
				int tmp = 0;
				for (int l = inst.numAttributes() - 1; l >= 104; l--) {
					if (!Double.isNaN(inst.value(l))) {
						overallranking[tmp] += inst.value(l);
						totalvalue[tmp]++;
					}
					tmp++;
				}
			}

			for (int o = 0; o < overallranking.length; o++) {
				overallranking[o] = overallranking[o] / totalvalue[o];
			}

			HashMap<String, Double> overall = new HashMap<>();

			int exampel = 0;
			if (i == 0) {
				exampel = 1;
			}

			for (int p = 0; p < overallranking.length; p++) {
				overall.put(data.get(exampel).attribute((data.numAttributes() - 1) - p).name(), overallranking[p]);
			}
			StopWatch watch = new StopWatch();
			watch.start();
			ModifiedISAC isac = new ModifiedISAC();
			isac.buildRanker();
			watch.stop();
			logger.info("Time: {}ms", watch.getTime());
			times[i] = watch.getTime();
			watch.reset();

			HashMap<String, Double> classAndPerfo = new HashMap<>();
			for (int p = testprep.numAttributes() - 1; p >= 104; p--) {
				classAndPerfo.put(testprep.get(i).attribute(p).name(), testprep.get(i).value(p));
				testprep.deleteAttributeAt(p);
			}
			testprep.deleteAttributeAt(0);
			Instance inst = testprep.get(i);

			RankingForGroup<double[], String> ranking = isac.getRanking(inst);
			// get the ground truth ranking as string list

			int size = 3;

			double[] rankingTruth = new double[22];
			int tmp = 0;
			ArrayList<String> top3truth = new ArrayList<>();

			while (!classAndPerfo.isEmpty()) {
				double maxPerfo = Double.MIN_VALUE;
				String myClassi = "";
				for (Entry<String, Double> classifierWithPerformance : classAndPerfo.entrySet()) {
					String classi = classifierWithPerformance.getKey();
					if (!classAndPerfo.get(classi).isNaN() && classifierWithPerformance.getValue() >= maxPerfo) {
						maxPerfo = classAndPerfo.get(classi);
						myClassi = classi;
					}
				}
				if (myClassi.isEmpty()) {
					int nans = tmp;
					for (String str : classAndPerfo.keySet()) {
						if (nans < size) {
							top3truth.add(str);
						}
						positionInRanking.put(str, nans);
						rankingTruth[nans] = nans;
						nans++;
					}
					classAndPerfo.clear();
				} else {
					if (tmp < size) {
						top3truth.add(myClassi);
						if (myClassi.equals(CN_RANDOMFOREST)) {
							randomForestplatz1++;
						}
						if (myClassi.equals("weka.classifiers.bayes.NaiveBayesMultinomial")) {
							naivebaismulti++;
						}
						if (myClassi.equals("weka.classifiers.bayes.NaiveBayes")) {
							naivebais++;
						}
					}
					rankingTruth[tmp] = tmp;
					positionInRanking.put(myClassi, tmp);
					classAndPerfo.remove(myClassi);
					tmp++;
				}

			}
			if (logger.isInfoEnabled()) {
				logger.info("Ranking truth: {} ", Arrays.toString(rankingTruth));
			}

			double[] difference3 = new double[size];

			HashMap<String, Double> loopoverall = (HashMap<String, Double>) overall.clone();
			double[] finishedoverallranking = new double[22];
			HashMap<String, Integer> rankingoverall = new HashMap<>();
			int loopcounter = 0;

			while (!loopoverall.isEmpty()) {
				double maxPerfo = Double.MIN_VALUE;
				String myClassifier = "";
				for (Entry<String, Double> cNameWithPerformance : loopoverall.entrySet()) {
					if (cNameWithPerformance.getValue() >= maxPerfo) {
						maxPerfo = cNameWithPerformance.getValue();
						myClassifier = cNameWithPerformance.getKey();
					}
				}
				if (loopcounter < size && maxPerfo != Double.MIN_VALUE) {
					difference3[loopcounter] = maxPerfo;
				}
				finishedoverallranking[positionInRanking.get(myClassifier)] = loopcounter;
				rankingoverall.put(myClassifier, loopcounter);
				loopoverall.remove(myClassifier);
				loopcounter++;
			}
			if (logger.isInfoEnabled()) {
				logger.info("baseline ranking {}", Arrays.toString(finishedoverallranking));
			}

			// get the ranking as string list
			ArrayList<String> rankingAsStringList = new ArrayList<>();
			for (String rank : ranking) {
				rankingAsStringList.add(rank);
			}

			double[] rankingFromMyMethod = new double[22];
			ArrayList<String> top3my = new ArrayList<>();
			int intermidiate = 0;
			for (String classi : rankingAsStringList) {
				rankingFromMyMethod[positionInRanking.get(classi)] = intermidiate;
				if (intermidiate < size) {
					top3my.add(classi);
				}
				intermidiate++;

			}
			if (logger.isInfoEnabled()) {
				logger.info("My ranking: {}", Arrays.toString(rankingFromMyMethod));
			}

			ArrayList<String> top3MlPlan = new ArrayList<>();
			ArrayList<String> mlPlanranking = makeStaticRanking();
			double[] mlplanranking = new double[22];
			intermidiate = 0;

			for (String str : mlPlanranking) {
				Integer perfo = positionInRanking.get(str);
				if (perfo != null) {
					if (intermidiate < size) {
						top3MlPlan.add(str);
					}
					int index = perfo;
					mlplanranking[index] = intermidiate;
					intermidiate++;
				}

			}
			if (logger.isInfoEnabled()) {
				logger.info("ML-plan ranking: {}", Arrays.toString(mlplanranking));
			}

			double stpestiloptwouldbereached = 0;
			double stepstillmlreachopt = 0;
			stpestiloptwouldbereached = rankingFromMyMethod[0] + 1;
			stepstillmlreachopt = mlplanranking[0] + 1;

			double[] difference1 = new double[size];
			double[] difference2 = new double[size];
			double[] difference4 = new double[size];

			for (int h = 0; h < size; h++) {
				String classitruth = top3truth.get(h);
				String mltruth = top3MlPlan.get(h);
				String mytruth = top3my.get(h);

				double perfotruth = 0;
				double perfomy = 0;
				double perfoml = 0;

				for (int t = 125; t >= 104; t--) {
					if (reminder.attribute(t).name().equals(classitruth)) {
						perfotruth = reminder.value(t);
					}
					if (reminder.attribute(t).name().equals(mytruth)) {
						perfomy = reminder.value(t);
					}
					if (reminder.attribute(t).name().equals(mltruth)) {
						perfoml = reminder.value(t);
					}
				}
				difference1[h] = perfotruth;
				difference2[h] = perfomy;
				difference4[h] = perfoml;
			}

			logger.info("Das betrachtete Datenset: {}", i + 1);

			logger.info("Die steps die es bräuchte um Platz eins der opt lösung in meiner zu erreichen {}", stpestiloptwouldbereached);
			stepdifference[i] = stpestiloptwouldbereached;
			stepdifferenceML[i] = stepstillmlreachopt;

			logger.info("Der Verlust zweichen Platz eins der optimal Lösung und der besten meiner Lösungen: {}", (Math.rint((1000.0 * (difference1[0] - difference2[0])))) / 1000.0);
			logger.info("Mein Platz 3: {}", difference2[size - 1]);
			logger.info("Der Verlust gegen meinen Platz 3: {}", (Math.rint((1000.0 * (difference1[0] - difference2[size - 1])))) / 1000.0);
			untochedmy[i] = difference2[0];
			untocedoverall[i] = difference3[0];
			untouchedml[i] = difference4[0];
			logger.info("{} {}", difference2[0], difference4[0]);

			logger.info("Bester in der optimalen Lösung: {}", difference1[0]);
			logger.info("Mein Platz eins: {}", difference2[0]);

			double platz1 = difference2[0];
			platz1my[i] = difference1[0] - platz1;
			platz1ml[i] = difference1[0] - difference4[0];
			logger.info("Performance von ML platz 1 {}", difference4[0]);

			logger.info("Platz eins der baseline {}", difference3[0]);
			platz1overall[i] = difference1[0] - difference3[0];

			logger.info("Das wahre ranking: {}", top3truth);
			logger.info("Mein ranking: {}", top3my);
			logger.info("ML-Plan ranking {}", top3MlPlan);

			Arrays.sort(difference2);
			logger.info("Beste Performance {}", difference2[size - 1]);

			logger.info("Ist die beste Performance Plazt 1 bei meinem ranking? {}", (platz1 == difference2[size - 1]));

			KendallsCorrelation correl = new KendallsCorrelation();
			KendallsCorrelation correlML = new KendallsCorrelation();
			results[i] = correl.correlation(rankingTruth, rankingFromMyMethod);
			kendallforML[i] = correlML.correlation(rankingTruth, mlplanranking);

			Arrays.sort(difference3);
			Arrays.sort(difference4);
			top3ml[i] = difference1[0] - difference4[difference4.length - 1];
			top3mymethod[i] = difference1[0] - difference2[difference2.length - 1];
			top3overall[i] = difference1[0] - difference3[difference3.length - 1];
			randomForest[i] = positionInRanking.get(CN_RANDOMFOREST);
		}
		return results;

	}

	private static ArrayList<String> makeStaticRanking() {
		ArrayList<String> staticranking = new ArrayList<>();
		staticranking.add(CN_RANDOMFOREST);
		staticranking.add("weka.classifiers.bayes.NaiveBayesMultinomial");
		staticranking.add("weka.classifiers.bayes.NaiveBayes");
		staticranking.add("weka.classifiers.functions.SMO");
		staticranking.add("weka.classifiers.trees.RandomTree");
		staticranking.add("weka.classifiers.lazy.IBk");
		staticranking.add("weka.classifiers.trees.J48");
		staticranking.add("weka.classifiers.functions.VotedPerceptron");
		staticranking.add("weka.classifiers.functions.SimpleLogistic");
		staticranking.add("weka.classifiers.functions.Logistic");
		staticranking.add("weka.classifiers.functions.MultilayerPerceptron");
		staticranking.add("weka.classifiers.bayes.BayesNet");
		staticranking.add("weka.classifiers.functions.SGD");
		staticranking.add("weka.classifiers.trees.LMT");
		staticranking.add("weka.classifiers.lazy.KStar");
		staticranking.add("weka.classifiers.rules.JRip");
		staticranking.add("weka.classifiers.rules.PART");
		staticranking.add("weka.classifiers.trees.REPTree");
		staticranking.add("weka.classifiers.trees.DecisionStump");
		staticranking.add("weka.classifiers.meta.AdaBoostM1");
		staticranking.add("weka.classifiers.meta.AdditiveRegression");
		staticranking.add("weka.classifiers.meta.Bagging");
		staticranking.add("weka.classifiers.meta.ClassificationViaRegression");
		staticranking.add("weka.classifiers.meta.LogitBoost");
		staticranking.add("weka.classifiers.meta.MultiClassClassifier");
		staticranking.add("weka.classifiers.meta.RandomCommittee");
		staticranking.add("weka.classifiers.meta.RandomSubspace");
		staticranking.add("weka.classifiers.meta.Stacking");
		staticranking.add("weka.classifiers.meta.Vote");
		staticranking.add("weka.classifiers.functions.SimpleLinearRegression");
		staticranking.add("weka.classifiers.rules.M5Rules");
		staticranking.add("weka.classifiers.trees.M5P");

		return staticranking;

	}
}
