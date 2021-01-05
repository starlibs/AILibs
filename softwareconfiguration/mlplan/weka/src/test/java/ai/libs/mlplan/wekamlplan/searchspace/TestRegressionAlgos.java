package ai.libs.mlplan.wekamlplan.searchspace;

import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.regression.loss.ERegressionPerformanceMeasure;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.weka.weka.WekaRegressorFactory;

public class TestRegressionAlgos {

	public static void main(final String[] args) throws Exception {

		List<ILabeledDataset<ILabeledInstance>> dataset = OpenMLDatasetReader.loadTaskFold(3005, 0);
		IComponentRepository repo = new ComponentSerialization().deserializeRepository(new ResourceFile("automl/searchmodels/weka/weka-full-regression.json"));
		Collection<IComponentInstance> asChoices = ComponentUtil.getAllAlgorithmSelectionInstances("MetaRegressor", repo);

		WekaRegressorFactory factory = new WekaRegressorFactory();
		for (IComponentInstance ci : asChoices) {
			try {
				IWekaClassifier learner = factory.getComponentInstantiation(ci);
				System.out.println("Test " + ComponentInstanceUtil.getComponentInstanceAsComponentNames(ci));
				learner.fit(dataset.get(0));
				ILearnerRunReport report = new SupervisedLearnerExecutor().execute(learner, dataset.get(1));
				System.out.println("RMSE: " + ERegressionPerformanceMeasure.RMSE.loss(report.getPredictionDiffList().getCastedView(Double.class, IRegressionPrediction.class)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
