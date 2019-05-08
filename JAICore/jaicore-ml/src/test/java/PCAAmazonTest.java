import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import jaicore.ml.WekaUtil;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

public class PCAAmazonTest {


	public static void main2(final String[] args) throws Exception {
		File fileIn = new File("../../../../datasets/classification/multi-class/amazon.arff");
		System.out.println(fileIn.getAbsolutePath());
		Instances data = new Instances(new FileReader(fileIn));
		data.setClassIndex(data.numAttributes() - 1);

		/* shrink down to 50% (roughly a double 70% split) */
		File fileIntermediate = new File("testrsc/ml/orig/amazon-intermediate.arff");
		data = WekaUtil.getStratifiedSplit(data, 0, .7).get(0);
		FileWriter fw = new FileWriter(fileIntermediate);
		fw.write(data.toString());
		System.out.println("Wrote reduced data to intermediate file. Now applying SubsetEval.");

		File fileOut = new File("testrsc/ml/orig/amazon-subseteval.arff");

		/* run PCA */
		int attributesBefore = data.numAttributes() - 1;
		AttributeSelection as = new AttributeSelection();
		as.setEvaluator(new CfsSubsetEval());
		as.setSearch(new GreedyStepwise());

		long start = System.currentTimeMillis();
		as.SelectAttributes(data);
		long end = System.currentTimeMillis();

		System.out.println("Ready, now shrinking the data ...");
		Instances reducedData = as.reduceDimensionality(data);
		int attributesAfter = reducedData.numAttributes() - 1;
		fw = new FileWriter(fileOut);
		fw.write(reducedData.toString());

		System.out.println(attributesBefore + " -> " + attributesAfter + " in " + Math.round((end - start) / 1000.f) + "s");
	}

	public static void main(final String[] args) throws Exception {
		//		File file = new File("testrsc/ml/orig/amazon-subseteval.arff");
		File file = new File("../../../../datasets/classification/multi-class/amazon.arff");
		Instances data = new Instances(new FileReader(file));
		data.setClassIndex(data.numAttributes() - 1);

		List<Instances> split = WekaUtil.getStratifiedSplit(WekaUtil.getStratifiedSplit(data, 0, 0.7).get(0), 0, .7);


		long start = System.currentTimeMillis();
		Classifier smo = new SMO();
		smo.buildClassifier(split.get(0));
		long end = System.currentTimeMillis();
		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(smo, split.get(1));
		System.out.println(eval.errorRate() + " in " + (end - start) + "ms");
	}
}
