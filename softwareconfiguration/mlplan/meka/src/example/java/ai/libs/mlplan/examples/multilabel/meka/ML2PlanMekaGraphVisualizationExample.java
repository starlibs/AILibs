package ai.libs.mlplan.examples.multilabel.meka;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.algorithm.Timeout;

import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.classification.multilabel.dataset.IMekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.dataset.MekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.meka.ML2PlanMekaBuilder;
import meka.core.MLUtils;
import weka.core.Instances;

public class ML2PlanMekaGraphVisualizationExample {
	public static void main(final String[] args) throws Exception {

		File datasetFile = new File("../../../../datasets/classification/multi-label/flags.arff");
		Instances data = new Instances(new FileReader(datasetFile));
		MLUtils.prepareData(data);
		IMekaInstances dataset = new MekaInstances(data);
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(dataset, new Random(0), .7);

		/* initialize mlplan, and let it run for 1 hour */
		MLPlan<IMekaClassifier> mlplan = new ML2PlanMekaBuilder().withNumCpus(4).withTimeOut(new Timeout(1, TimeUnit.HOURS)).withDataset(split.get(0)).build();

		/* start visualization */
		AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(mlplan);
		window.withMainPlugin(new GraphViewPlugin());
		window.withPlugin(new NodeInfoGUIPlugin(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new SearchRolloutHistogramPlugin());
		try {
			long start = System.currentTimeMillis();
			IMekaClassifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier. Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
			ILearnerRunReport report = executor.execute(optimizedClassifier, split.get(1));
			System.out.println("Error Rate of the solution produced by ML-Plan: " + EClassificationPerformanceMeasure.ERRORRATE.loss(report.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class)));
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}
}
