package jaicore.ml.classification.multiclass.reduction.splitters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil;
import jaicore.logging.LoggerUtil;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class RPNDSplitter implements ISplitter {

	private static final Logger logger = LoggerFactory.getLogger(RPNDSplitter.class);
	private final Random rand;
	private final Classifier rpndClassifier;

	public RPNDSplitter(final Random rand, final Classifier rpndClassifier) {
		super();
		this.rand = rand;
		this.rpndClassifier = rpndClassifier;
	}

	@Override
	public Collection<Collection<String>> split(final Instances data) throws Exception {

		Collection<String> classes = WekaUtil.getClassesActuallyContainedInDataset(data);

		/* 2. if we have a leaf node, abort */
		if (classes.size() == 1) {
			Collection<Collection<String>> split = new ArrayList<>();
			split.add(classes);
			return split;
		}

		/* 3a. otherwise select randomly two classes */
		List<String> copy = new ArrayList<>(classes);
		Collections.shuffle(copy, this.rand);
		String c1 = copy.get(0);
		String c2 = copy.get(1);
		Collection<String> s1 = new HashSet<>();
		s1.add(c1);
		Collection<String> s2 = new HashSet<>();
		s2.add(c2);
		return this.split(copy, s1, s2, data);
	}

	public Collection<Collection<String>> split(final Collection<String> classes, final Collection<String> s1, final Collection<String> s2, final Instances data) throws Exception {

		logger.info("Start creation of RPND split with basis {}/{} for classes {}", s1, s2, classes);

		/* 3b. and 3c. train binary classifiers for c1 vs c2 */
		Instances reducedData = WekaUtil.mergeClassesOfInstances(data, s1, s2);
		logger.debug("Building classifier for separating the two class sets {} and {}", s1, s2);
		this.rpndClassifier.buildClassifier(reducedData);

		/* 3d. insort the remaining classes */
		logger.info("Now classifying the items of the other classes");
		List<String> remainingClasses = new ArrayList<>(SetUtil.difference(SetUtil.difference(classes, s1), s2));
		for (int i = 0; i < remainingClasses.size(); i++) {
			String className = remainingClasses.get(i);
			Instances testData = WekaUtil.getInstancesOfClass(data, className);
			logger.debug("Classify {} instances of class {}", testData.size(), className);
			int o1 = 0;
			int o2 = 0;
			for (Instance inst : testData) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				try {
					double prediction = this.rpndClassifier.classifyInstance(WekaUtil.getRefactoredInstance(inst));
					if (prediction == 0) {
						o1++;
					} else {
						o2++;
					}
				} catch (Exception e) {
					logger.error(LoggerUtil.getExceptionInfo(e));
				}
			}
			if (o1 > o2) {
				s1.add(className);
			} else {
				s2.add(className);
			}
		}
		Collection<Collection<String>> split = new ArrayList<>();
		split.add(s1);
		split.add(s2);
		return split;
	}
}