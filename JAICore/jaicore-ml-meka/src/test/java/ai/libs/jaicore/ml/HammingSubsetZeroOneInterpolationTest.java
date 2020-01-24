package ai.libs.jaicore.ml;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ai.libs.jaicore.ml.classification.multilabel.MultiLabelClassification;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.OWARelevanceLoss;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.owa.MoebiusTransformOWAValueFunction;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.owa.PolynomialOWAValueFunction;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.LC;
import meka.core.MLUtils;
import meka.core.Metrics;
import meka.core.Result;
import weka.core.Instances;

public class HammingSubsetZeroOneInterpolationTest {

	private static final File DATASET_FILE = new File("../../../datasets/classification/multi-label/bibtex.arff");

	private static Instances dataset;
	private static List<IWekaInstances> datasplit;

	private static final boolean MOEBIUS_TRANSFORM = true;

	@BeforeClass
	public static void setup() throws Exception {
		dataset = new Instances(new FileReader(DATASET_FILE));
		MLUtils.prepareData(dataset);
		datasplit = WekaUtil.realizeSplit(new WekaInstances(dataset), WekaUtil.getArbitrarySplit(new WekaInstances(dataset), new Random(42), .7));
	}

	@Ignore
	@Test
	public void runLCTest() throws Exception {
		LC lc = new LC();
		lc.buildClassifier(datasplit.get(0).getInstances());
		Result res = Evaluation.testClassifier(lc, datasplit.get(1).getInstances());
		System.out.println("====");
		System.out.println("LC");
		System.out.println(Metrics.L_Hamming(res.allTrueValues(), res.allPredictions(0.5)));
		this.evaluatePolynomialRelevanceLoss(res.allTrueValues(), res.allPredictions(0.5));
		System.out.println(Metrics.L_ZeroOne(res.allTrueValues(), res.allPredictions(0.5)));
	}

	@Ignore
	@Test
	public void runBRTest() throws Exception {
		BR br = new BR();
		br.buildClassifier(datasplit.get(0).getInstances());
		Result res = Evaluation.testClassifier(br, datasplit.get(1).getInstances());
		System.out.println("====");
		System.out.println("BR");
		System.out.println(Metrics.L_Hamming(res.allTrueValues(), res.allPredictions(0.5)));
		this.evaluatePolynomialRelevanceLoss(res.allTrueValues(), res.allPredictions(0.5));
		System.out.println(Metrics.L_ZeroOne(res.allTrueValues(), res.allPredictions(0.5)));
	}

	@Test
	public void runTest() throws Exception {
		LC lc = new LC();
		lc.buildClassifier(datasplit.get(0).getInstances());
		Result res = Evaluation.testClassifier(lc, datasplit.get(1).getInstances());
		double lcHamming = Metrics.L_Hamming(res.allTrueValues(), res.allPredictions(0.5));
		double lcZeroOne = Metrics.L_ZeroOne(res.allTrueValues(), res.allPredictions(0.5));

		BR br = new BR();
		br.buildClassifier(datasplit.get(0).getInstances());
		Result res2 = Evaluation.testClassifier(br, datasplit.get(1).getInstances());
		double brHamming = Metrics.L_Hamming(res2.allTrueValues(), res2.allPredictions(0.5));
		double brZeroOne = Metrics.L_ZeroOne(res2.allTrueValues(), res2.allPredictions(0.5));

		boolean hamming = lcHamming > brHamming;
		boolean zeroOne = lcZeroOne > brZeroOne;

		System.out.println(lcHamming + " " + brHamming);
		System.out.println(lcZeroOne + " " + brZeroOne);
		boolean xor = hamming ^ zeroOne;
		System.out.println(hamming + " " + zeroOne + " " + xor);

		if (xor) {
			System.out.println("LC");
			if (!MOEBIUS_TRANSFORM) {
				this.evaluatePolynomialRelevanceLoss(res.allTrueValues(), res.allPredictions(0.5));
			} else {
				this.evaluateMoebiusTransformRelevanceLoss(res.allTrueValues(), res.allPredictions(0.5));
			}
			System.out.println("BR");
			if (!MOEBIUS_TRANSFORM) {
				this.evaluatePolynomialRelevanceLoss(res2.allTrueValues(), res2.allPredictions(0.5));
			} else {
				this.evaluateMoebiusTransformRelevanceLoss(res2.allTrueValues(), res2.allPredictions(0.5));
			}
		} else {
			System.out.println(xor);
		}

	}

	private void evaluateMoebiusTransformRelevanceLoss(final int[][] gt, final int[][] pred) {
		for (int k = 1; k <= gt[0].length; k++) {
			OWARelevanceLoss l = new OWARelevanceLoss(new MoebiusTransformOWAValueFunction(k));
			double loss = l.loss(Arrays.stream(gt).collect(Collectors.toList()), this.toClassifications(pred));
			System.out.println(new String(k + "\t" + loss).replaceAll("\\.", ","));
		}
	}

	private void evaluatePolynomialRelevanceLoss(final int[][] gt, final int[][] pred) {
		double[] scales = { 1, 1.01, 1.02, 1.03, 1.04, 1.05, 1.06, 1.07, 1.08, 1.09, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300,
				400, 500, 600, 700, 800, 900 };
		for (double scale : scales) {
			OWARelevanceLoss l = new OWARelevanceLoss(new PolynomialOWAValueFunction(scale));
			double loss = l.loss(Arrays.stream(gt).collect(Collectors.toList()), this.toClassifications(pred));
			System.out.println(new String(scale + "\t" + loss).replaceAll("\\.", ","));
		}
	}

	private List<IMultiLabelClassification> toClassifications(final int[][] values) {
		List<IMultiLabelClassification> list = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			list.add(new MultiLabelClassification(Arrays.stream(values[i]).mapToDouble(x -> x).toArray()));
		}
		return list;
	}

}
