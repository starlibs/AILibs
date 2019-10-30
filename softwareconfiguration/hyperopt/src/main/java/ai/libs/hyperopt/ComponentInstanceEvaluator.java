package ai.libs.hyperopt;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import ai.libs.jaicore.ml.core.timeseries.util.WekaUtil;
import ai.libs.mlplan.multiclass.wekamlplan.ILearnerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 *
 * @author kadirayk
 *
 */
public class ComponentInstanceEvaluator implements IComponentInstanceEvaluator {

	private Logger logger = LoggerFactory.getLogger(ComponentInstanceEvaluator.class);

	private String algorithmId;

	private final ILearnerFactory classifierFactory;

	private String filePath;

	private EventBus eventBus;

	private List<Instances> split;

	public ComponentInstanceEvaluator(final ILearnerFactory classifierFactory, final String filePath, final String algorithmId) {
		this.classifierFactory = classifierFactory;
		this.filePath = filePath;
		this.eventBus = new EventBus();
		this.algorithmId = algorithmId;
		Instances dataset = this.loadDataset(filePath);
		try {
			this.split = WekaUtil.getStratifiedSplit(dataset, 0, .7f);
		} catch (SplitFailedException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Concrete compontentInstance evaluated
	 */
	@Override
	public Double evaluate(final ComponentInstance componentInstance) throws InterruptedException, ObjectEvaluationFailedException {
		Double score = 0.0;
		try {
			Classifier classifier = this.classifierFactory.getComponentInstantiation(componentInstance);
			Evaluation eval = null;
			try {

				// Normalize dataset
				classifier.buildClassifier(this.split.get(0));
				eval = new Evaluation(this.split.get(0));
				eval.evaluateModel(classifier, this.split.get(1));
				score = eval.pctCorrect();
				System.out.println("score:" + score);
				System.out.println("comp:" + componentInstance);
			} catch (Exception e) {
				this.logger.error(e.getMessage());
			}
		} catch (ComponentInstantiationFailedException e) {
			this.logger.error(e.getMessage());
		}
		PCSBasedOptimizationEvent event = new PCSBasedOptimizationEvent(componentInstance, score, this.algorithmId);
		this.eventBus.post(event);
		return score;
	}

	public List<Instances> getInstances() {
		return this.split;
	}

	private Instances loadDataset(final String path) {
		Instances dataset = null;
		try {
			dataset = DataSource.read(path);
			if (dataset.classIndex() == -1) {
				dataset.setClassIndex(dataset.numAttributes() - 1);
			}
		} catch (Exception e) {
			this.logger.error(e.getMessage());
		}

		return dataset;
	}

	public void registerListener(final AlgorithmEventListener listener) {
		this.eventBus.register(listener);
	}

	public void UnregisterListener(final AlgorithmEventListener listener) {
		this.eventBus.unregister(listener);
	}

}
