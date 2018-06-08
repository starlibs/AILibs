package jaicore.ml.evaluation;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import jaicore.logging.LoggerUtil;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

@SuppressWarnings("serial")
public class MulticlassEvaluator implements BasicMLEvaluator, Serializable {

	private final static Logger logger = LoggerFactory.getLogger(MulticlassEvaluator.class);

	private final Random rand;
	private boolean canceled;
	private final EventBus measurementEventBus = new EventBus();

	public MulticlassEvaluator(Random r) {
		super();
		this.rand = r;
	}

	public double getErrorRateForRandomSplit(Classifier c, Instances data, double splitSize) throws Exception {
		if (c == null)
			throw new IllegalArgumentException("Cannnot get error rate for classifier NULL");
		if (data == null)
			throw new IllegalArgumentException("Cannnot get error rate for data NULL");
		List<Instances> split = WekaUtil.getStratifiedSplit(data, rand, splitSize);
		Instances train = split.get(0);
		Instances test = split.get(1);
		return getErrorRateForSplit(c, train, test);
	}

	public double getErrorRateForSplit(Classifier c, Instances train, Instances test) throws Exception {
		logger.info("Split size is {}/{}", train.size(), test.size());
		try {
			Classifier cCopy = WekaUtil.cloneClassifier(c);
			cCopy.buildClassifier(train);
			return loss(cCopy, test);
		} catch (InterruptedException e) {
			logger.info("Evaluation of classifier was interrupted.");
			throw e;
		}
		catch (Exception e) {
			logger.error("Problems with evaluation of classifier. Details:\n{}", LoggerUtil.getExceptionInfo(e));
			measurementEventBus.post(new ClassifierMeasurementEvent<Double>(c, null, e));
			throw e;
		}
	}

	public double loss(Classifier c, Instances test) throws Exception {
		int mistakes = 0;
		try {
			if (c instanceof IInstancesClassifier) {
				IInstancesClassifier cc = (IInstancesClassifier) c;
				double[] predictions = cc.classifyInstances(test);
				for (int i = 0; i < predictions.length; i++) {
					if (predictions[i] != test.get(i).classValue())
						mistakes++;
				}
			} else {
				for (Instance i : test) {
					if (i.classValue() != c.classifyInstance(i))
						mistakes++;
				}
			}
			double error = mistakes * 1f / test.size();
			measurementEventBus.post(new ClassifierMeasurementEvent<Double>(c, error, null));
			return error;
		} catch (Exception e) {
			logger.error("Problems with evaluation of classifier. Details:\n{}", LoggerUtil.getExceptionInfo(e));
			measurementEventBus.post(new ClassifierMeasurementEvent<Double>(c, null, e));
			throw e;
		}
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public EventBus getMeasurementEventBus() {
		return measurementEventBus;
	}
}
