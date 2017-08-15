package jaicore.ml.classification.multiclass.reduction.splitters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import jaicore.basic.SetUtil;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class RPNDSplitter implements ISplitter {

	private final Instances data;
	private final Random rand;

	public RPNDSplitter(Instances data, Random rand) {
		super();
		this.data = data;
		this.rand = rand;
	}

	@Override
	public Collection<Collection<String>> split(Collection<String> classes, Classifier c) {

		/* 2. if we have a leaf node, abort */
		if (classes.size() == 1) {
			Collection<Collection<String>> split = new ArrayList<>();
			split.add(classes);
			return split;
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
		return split(copy, s1, s2, c);
	}

	public Collection<Collection<String>> split(Collection<String> classes, Collection<String> s1, Collection<String> s2, Classifier c) {
		
		/* 3b. and 3c. train binary classifiers for c1 vs c2 */
		Instances reducedData = WekaUtil.mergeClassesOfInstances(data, s1, s2);
		try {
			c.buildClassifier(reducedData);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* 3d. insort the remaining classes */
		List<String> remainingClasses = new ArrayList<>(SetUtil.difference(SetUtil.difference(classes, s1), s2));
		for (int i = 0; i < remainingClasses.size(); i++) {
			String className = remainingClasses.get(i);
			Instances testData = WekaUtil.getInstancesOfClass(data, className);
			int o1 = 0;
			int o2 = 0;
			for (Instance inst : testData) {
				try {
					double prediction = c.classifyInstance(WekaUtil.getRefactoredInstance(inst));
					if (prediction == 0)
						o1++;
					else
						o2++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (o1 > o2)
				s1.add(className);
			else
				s2.add(className);
		}
		Collection<Collection<String>> split = new ArrayList<>();
		split.add(s1);
		split.add(s2);
		return split;
	}
}