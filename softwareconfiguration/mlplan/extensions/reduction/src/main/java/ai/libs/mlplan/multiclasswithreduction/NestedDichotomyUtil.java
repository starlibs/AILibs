package ai.libs.mlplan.multiclasswithreduction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.learner.reduction.splitter.RPNDSplitter;
import ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class NestedDichotomyUtil {

	private NestedDichotomyUtil() {
		/* Intentionally left blank. Only prevent instantiation. */
	}

	private static final Logger logger = LoggerFactory.getLogger(NestedDichotomyUtil.class);

	public static ClassSplit<String> createGeneralRPNDBasedSplit(final Collection<String> classes, final Random rand, final String classifierName, final Instances data) throws InterruptedException {
		if (classes.size() < 2) {
			throw new IllegalArgumentException("Cannot compute split for less than two classes!");
		}
		try {
			RPNDSplitter splitter = new RPNDSplitter(rand, new MLPipeline(new Ranker(), new InfoGainAttributeEval(), AbstractClassifier.forName(classifierName, null)));
			Collection<Collection<String>> splitAsCollection = null;
			splitAsCollection = splitter.split(data);
			Iterator<Collection<String>> it = splitAsCollection.iterator();
			return new ClassSplit<>(classes, it.next(), it.next());
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected exception occurred while creating an RPND split", e);
			return null;
		}
	}

	public static ClassSplit<String> createGeneralRPNDBasedSplit(final Collection<String> classes, final Collection<String> s1, final Collection<String> s2, final Random rand, final String classifierName, final Instances data)
			throws InterruptedException {
		try {
			RPNDSplitter splitter = new RPNDSplitter(rand, AbstractClassifier.forName(classifierName, new String[] {}));
			Collection<Collection<String>> splitAsCollection = null;

			splitAsCollection = splitter.split(classes, s1, s2, data);
			Iterator<Collection<String>> it = splitAsCollection.iterator();
			return new ClassSplit<>(classes, it.next(), it.next());
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected exception occurred while creating an RPND split", e);
		}
		return null;
	}

	public static ClassSplit<String> createUnaryRPNDBasedSplit(final Collection<String> classes, final Random rand, final String classifierName, final Instances data) {

		/* 2. if we have a leaf node, abort */
		if (classes.size() == 1) {
			return new ClassSplit<>(classes, null, null);
		}

		/* 3a. otherwise select randomly two classes */
		List<String> copy = new ArrayList<>(classes);
		Collections.shuffle(copy, rand);
		String c1 = copy.get(0);
		String c2 = copy.get(1);
		Collection<String> s1 = new HashSet<>();
		s1.add(c1);
		Collection<String> s2 = new HashSet<>();
		s2.add(c2);

		/* 3b. and 3c. train binary classifiers for c1 vs c2 */
		Instances reducedData = WekaUtil.mergeClassesOfInstances(data, s1, s2);
		Classifier c = null;
		try {
			c = AbstractClassifier.forName(classifierName, new String[] {});
		} catch (Exception e1) {
			logger.error("Could not get object of classifier with name {}", classifierName, e1);
			return null;
		}
		try {
			c.buildClassifier(reducedData);
		} catch (Exception e) {
			logger.error("Could not train classifier", e);
		}

		/* 3d. insort the remaining classes */
		List<String> remainingClasses = new ArrayList<>(SetUtil.difference(SetUtil.difference(classes, s1), s2));
		int o1 = 0;
		int o2 = 0;
		for (int i = 0; i < remainingClasses.size(); i++) {
			String className = remainingClasses.get(i);
			Instances testData = WekaUtil.getInstancesOfClass(data, className);
			for (Instance inst : testData) {
				try {
					double prediction = c.classifyInstance(WekaUtil.getRefactoredInstance(inst));
					if (prediction == 0) {
						o1++;
					} else {
						o2++;
					}
				} catch (Exception e) {
					logger.error("Could not get prediction for some instance to assign it to a meta-class", e);
				}
			}
		}
		if (o1 > o2) {
			s1.addAll(remainingClasses);
		} else {
			s2.addAll(remainingClasses);
		}
		return new ClassSplit<>(classes, s1, s2);
	}
}
