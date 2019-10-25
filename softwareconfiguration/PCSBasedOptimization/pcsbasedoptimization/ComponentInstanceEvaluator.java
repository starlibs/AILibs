package ai.libs.hasco.pcsbasedoptimization;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.splitter.SplitFailedException;
import ai.libs.mlplan.multiclass.wekamlplan.IClassifierFactory;
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

	private final IClassifierFactory classifierFactory;

	private String filePath;

	private EventBus eventBus;
	
	private List<Instances> split;
	
	public ComponentInstanceEvaluator(IClassifierFactory classifierFactory, String filePath, String algorithmId) {
		this.classifierFactory = classifierFactory;
		this.filePath = filePath;
		this.eventBus = new EventBus();
		this.algorithmId = algorithmId;
		Instances dataset = loadDataset(filePath);
		try {
			split = WekaUtil.getStratifiedSplit(dataset, 0, .7f);
		} catch (SplitFailedException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * Concrete compontentInstance evaluated
	 */
	@Override
	public Double evaluate(ComponentInstance componentInstance)
			throws InterruptedException, ObjectEvaluationFailedException {
		Double score = 0.0;
		try {
			Classifier classifier = classifierFactory.getComponentInstantiation(componentInstance);
			Evaluation eval = null;
			try {

				// Normalize dataset
				classifier.buildClassifier(split.get(0));
				eval = new Evaluation(split.get(0));
				eval.evaluateModel(classifier, split.get(1));
				score = eval.pctCorrect();
				System.out.println("score:" + score);
				System.out.println("comp:" + componentInstance);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} catch (ComponentInstantiationFailedException e) {
			logger.error(e.getMessage());
		}
		PCSBasedOptimizationEvent event = new PCSBasedOptimizationEvent(componentInstance, score, algorithmId);
		eventBus.post(event);
		return score;
	}

	public List<Instances> getInstances() {
		return split;
	}
	
	private Instances loadDataset(String path) {
		Instances dataset = null;
		try {
			dataset = DataSource.read(path);
			if (dataset.classIndex() == -1) {
				dataset.setClassIndex(dataset.numAttributes() - 1);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return dataset;
	}

	public void registerListener(AlgorithmEventListener listener) {
		this.eventBus.register(listener);
	}

	public void UnregisterListener(AlgorithmEventListener listener) {
		this.eventBus.unregister(listener);
	}

}
