package ai.libs.mlplan.examples.multiclass.weka;

import java.io.FileReader;

import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLPlanWekaInstancesExample {

	public static void main(final String[] args) throws Exception {
		Instances dataset = new Instances(new FileReader("testrsc/car.arff"));
		dataset.setClassIndex(dataset.numAttributes() - 1);
		Classifier c = new MLPlanWekaBuilder().withDataset(new WekaInstances(dataset)).build().call().getClassifier();
	}

}
