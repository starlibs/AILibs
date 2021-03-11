package ai.libs.jaicore.ml.classification.multilabel.learner.neighbour;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationPredictionPerformanceMeasure;
import org.api4.java.common.metric.IDistanceMetric;
import org.api4.java.datastructure.kvstore.IKVStore;

import ai.libs.jaicore.basic.ArrayUtil;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ValueUtil;
import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.basic.kvstore.KVStoreCollection;
import ai.libs.jaicore.basic.kvstore.KVStoreStatisticsUtil;
import ai.libs.jaicore.basic.kvstore.KVStoreUtil;
import ai.libs.jaicore.basic.metric.ManhattanDistance;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.classification.multilabel.MultiLabelClassification;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.OWARelevanceLoss;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.owa.MoebiusTransformOWAValueFunction;
import meka.classifiers.multilabel.AbstractMultiLabelClassifier;
import meka.core.MLUtils;
import weka.core.Instance;
import weka.core.Instances;

public class MoebiusKNN extends AbstractMultiLabelClassifier {

	private int k;
	private IDistanceMetric metric;
	private IMultiLabelClassificationPredictionPerformanceMeasure internalMeasure;

	private int L;
	private double[][] data;
	private double[][] labels;

	public MoebiusKNN(final int k, final IDistanceMetric metric, final IMultiLabelClassificationPredictionPerformanceMeasure internalMeasure) {
		this.k = k;
		this.metric = metric;
		this.internalMeasure = internalMeasure;
	}

	@Override
	public void buildClassifier(final Instances trainingSet) throws Exception {
		this.L = trainingSet.classIndex();
		this.data = MLUtils.getXfromD(trainingSet);
		this.labels = MLUtils.getYfromD(trainingSet);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		double[] x = MLUtils.getxfromInstance(instance);

		List<Pair<Integer, Double>> distances = Collections.synchronizedList(new ArrayList<>(this.data.length));
		IntStream.range(0, this.data.length).parallel().forEach(i -> distances.add(new Pair<>(i, this.metric.distance(x, this.data[i]))));
		Collections.sort(distances, new Comparator<Pair<Integer, Double>>() {
			@Override
			public int compare(final Pair<Integer, Double> o1, final Pair<Integer, Double> o2) {
				return o1.getY().compareTo(o2.getY());
			}
		});

		double[][] neighbourLabels = new double[this.k][];
		for (int i = 0; i < this.k; i++) {
			neighbourLabels[i] = this.labels[distances.get(i).getX()];
		}

		return this.findOptimalLabeling(neighbourLabels);
	}

	private class Incumbent {
		IMultiLabelClassification bestLabeling = null;
		Double bestLoss = null;

		public synchronized void updateIncumbent(final IMultiLabelClassification labeling, final double loss) {
			if (this.bestLabeling == null || loss < this.bestLoss) {
				// System.out.println("New incumbent candidate: " + Arrays.toString(labeling.getPrediction()) + " --> " + loss);
				this.bestLabeling = labeling;
				this.bestLoss = loss;
			}
		}
	}

	private double[] findOptimalLabeling(final double[][] neighbourLabels) {
		int[][] gtLabels = ArrayUtil.thresholdDoubleToBinaryMatrix(neighbourLabels, 0.5);
		List<int[]> gtLabelList = Arrays.stream(gtLabels).collect(Collectors.toList());

		Incumbent inc = new Incumbent();

		Set<Integer> allZeroColumns = new HashSet<>();
		Set<Integer> allOneColumns = new HashSet<>();
		for (int i = 0; i < gtLabels[0].length; i++) {
			int sum = 0;
			for (int j = 0; j < gtLabels.length; j++) {
				sum += gtLabels[j][i];
			}
			if (sum == 0) {
				allZeroColumns.add(i);
			} else if (sum == neighbourLabels.length) {
				allOneColumns.add(i);
			}
		}
		List<IMultiLabelClassification> prunedLabelings = new ArrayList<>();

		int numLabelsToEnumerate = this.L - allZeroColumns.size() - allOneColumns.size();
		if (numLabelsToEnumerate > 0) {
			for (double[] vector : this.enumerateAllLabelings(this.L - allZeroColumns.size() - allOneColumns.size())) {
				double[] labeling = new double[this.L];
				int index = 0;
				for (int i = 0; i < this.L; i++) {
					if (allZeroColumns.contains(i)) {
						labeling[i] = 0.0;
					} else if (allOneColumns.contains(i)) {
						labeling[i] = 1.0;
					} else {
						labeling[i] = vector[index++];
					}
				}
				prunedLabelings.add(new MultiLabelClassification(labeling));
			}
		} else {
			double[] labeling = new double[this.L];
			for (int i = 0; i < this.L; i++) {
				if (allZeroColumns.contains(i)) {
					labeling[i] = 0.0;
				} else if (allOneColumns.contains(i)) {
					labeling[i] = 1.0;
				}
			}
			prunedLabelings.add(new MultiLabelClassification(labeling));
		}
		prunedLabelings.stream().parallel().forEach(pred -> {
			double sum = 0.0;
			for (int[] gt : gtLabelList) {
				sum += this.internalMeasure.loss(Arrays.asList(gt), Arrays.asList(pred));
			}
			inc.updateIncumbent(pred, sum);
		});

		return inc.bestLabeling.getPrediction();
	}

	private List<double[]> enumerateAllLabelings(final int L) {
		Queue<List<Double>> workingQueue = new LinkedList<>();
		workingQueue.add(new ArrayList<>());
		List<double[]> res = new ArrayList<>();
		while (!workingQueue.isEmpty()) {
			List<Double> head = workingQueue.remove();
			for (double bit : Arrays.asList(0.0, 1.0)) {
				List<Double> copy = new ArrayList<>(head);
				copy.add(bit);

				if (copy.size() == L) {
					res.add(copy.stream().mapToDouble(x -> x).toArray());
				} else {
					workingQueue.add(copy);
				}
			}
		}
		return res;
	}

	private static void runExperiments() throws Exception {

		File dir = new File("../../../datasets/classification/multi-label/");
		dir = new File("../../../ecmlpkdd/datasets/");
		List<String> datasets;
		// datasets = Arrays.asList("emotions", "scene", "yeast", "flags", "birds", "enron-f", "genbase", "llog-f", "medical"); // all datasets
		datasets = Arrays.asList("emotions", "scene", "flags", "yeast"); // smallish datasets
		datasets = Arrays.asList("birds");

		int numNeighbours = 10;
		for (String datasetName : datasets) {
			KVStoreCollection col = new KVStoreCollection();
			col.setCollectionID("MoebiusKNN");

			KVStoreCollection tbl = new KVStoreCollection();

			Instances dataset = new Instances(new FileReader(new File(dir, datasetName + "_42_" + 0 + "_test.arff")));
			MLUtils.prepareData(dataset);

			for (int i = 1; i <= dataset.classIndex(); i++) {
				System.out.println("Evaluate MoebiusKNN-" + i);

				// Result res = Evaluation.cvModel(, dataset, 10, "0.5");
				List<IMultiLabelClassification> preds = new ArrayList<>();
				List<int[]> gt = new ArrayList<>();
				for (int fold = 0; fold < 10; fold++) {
					Instances train = new Instances(new FileReader(new File(dir, datasetName + "_42_" + fold + "_train.arff")));
					Instances test = new Instances(new FileReader(new File(dir, datasetName + "_42_" + fold + "_test.arff")));
					MLUtils.prepareData(train);
					MLUtils.prepareData(test);
					gt.addAll(Arrays.stream(MLUtils.getYfromD(test)).map(x -> ArrayUtil.thresholdDoubleToBinaryArray(x, 0.5)).collect(Collectors.toList()));
					MoebiusKNN learner = new MoebiusKNN(numNeighbours, new ManhattanDistance(), new OWARelevanceLoss(new MoebiusTransformOWAValueFunction(i)));
					learner.buildClassifier(train);
					test.stream().map(x -> {
						try {
							return new MultiLabelClassification(learner.distributionForInstance(x));
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					}).forEach(preds::add);
				}

				List<Double> losses = new ArrayList<>();
				// eval
				for (int k = 1; k <= dataset.classIndex(); k++) {
					OWARelevanceLoss owaloss = new OWARelevanceLoss(new MoebiusTransformOWAValueFunction(k));
					double loss = owaloss.loss(gt, preds);
					losses.add(loss);

					IKVStore s = new KVStore();
					s.put("approach", "MoebiusKNN-" + ((i < 10) ? "0" + i : i));
					s.put("k", k);
					s.put("loss", ValueUtil.round(loss, 6));
					tbl.add(s);
				}

				IKVStore ecmlStore = new KVStore();
				ecmlStore.put("size", 10);
				ecmlStore.put("moebius", SetUtil.implode(losses, ","));
				ecmlStore.put("seed", -1);
				ecmlStore.put("L", dataset.classIndex());
				ecmlStore.put("algorithm", "MoebiusKNN-" + i);
				ecmlStore.put("dataset", datasetName);
				col.add(ecmlStore);
			}

			KVStoreStatisticsUtil.best(tbl, "k", "approach", "loss", "best");
			tbl.stream().forEach(x -> {
				x.put("loss", (x.getAsBoolean("best") ? "\\textbf{" + x.getAsString("loss") + "}" : x.getAsString("loss")));
			});

			System.out.println(datasetName);
			System.out.println(KVStoreUtil.kvStoreCollectionToLaTeXTable(tbl, "k", "approach", "loss", "?").replace(";", "\t"));

			col.serializeTo(new File(datasetName + "_results_moebius" + numNeighbours + "nn.kvstore"));
		}
	}

	public static void processExperimentData() throws IOException {
		for (File f : new File("./").listFiles()) {
			if (!f.getName().endsWith(".kvstore")) {
				continue;
			}

			KVStoreCollection col = new KVStoreCollection(FileUtil.readFileAsString(f));

			double[][] moebius = new double[col.size()][];
			for (int i = 0; i < col.size(); i++) {

				moebius[i] = col.get(i).getAsDoubleList("moebius").stream().mapToDouble(x -> x).toArray();
			}

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(col.get(0).getAsString("dataset") + ".csv")))) {
				bw.write("k," + col.stream().map(x -> x.getAsString("algorithm")).collect(Collectors.joining(",")) + "\n");
				for (int j = 0; j < moebius[0].length; j++) {
					int min = 0;
					for (int i = 1; i < moebius.length; i++) {
						if (moebius[i][j] < moebius[min][j]) {
							min = i;
						}
					}

					double[] dists = new double[moebius.length];
					for (int i = 0; i < moebius.length; i++) {
						dists[i] = ValueUtil.round(moebius[i][j] - moebius[min][j], 6);
					}

					bw.write((j + 1) + "," + Arrays.stream(dists).mapToObj(x -> x + "").collect(Collectors.joining(",")) + "\n");
				}
			}
		}

	}

	public static void main(final String[] args) throws Exception {
		runExperiments();
		processExperimentData();
	}

}
