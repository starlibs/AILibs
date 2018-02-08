package de.upb.crc901.mlplan.search.evaluators;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

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
		List<Instances> split = WekaUtil.getStratifiedSplit(data, rand, splitSize);
		Instances train = split.get(0);
		Instances test = split.get(1);
		return getErrorRateForSplit(c, train, test);
	}
	
	public double getErrorRateForSplit(Classifier c, Instances train, Instances test) throws Exception {
		Classifier cCopy = WekaUtil.cloneClassifier(c);
		cCopy.buildClassifier(train);
		int mistakes = 0;
		logger.info("Split size is {}/{}", train.size(), test.size());
		
		for (Instance i : test) {
			if (i.classValue() != cCopy.classifyInstance(i))
				mistakes++;
		}
		double error = mistakes * 100f / test.size();
		Instances data = new Instances(train);
		data.addAll(test);
		measurementEventBus.post(new ClassifierMeasurementEvent<Double>(cCopy, error));
		return error;
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
