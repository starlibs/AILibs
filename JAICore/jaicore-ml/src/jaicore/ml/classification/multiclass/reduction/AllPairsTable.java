package jaicore.ml.classification.multiclass.reduction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import jaicore.basic.sets.SetUtil;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class AllPairsTable {

	private final Map<String, Integer> classCount;
	private final Map<String, Map<String, Double>> separabilities = new HashMap<>();
	private final int sum;

	public AllPairsTable(Instances training, Instances validation, Classifier c) {
		Collection<String> classes = WekaUtil.getClassesActuallyContainedInDataset(training);
		for (Collection<String> set : SetUtil.getAllPossibleSubsetsWithSize(classes, 2)) {
			try {
				List<String> pair = set.stream().sorted().collect(Collectors.toList());
				String a = pair.get(0);
				String b = pair.get(1);
				Instances trainingData = WekaUtil.getInstancesOfClass(training, a);
				trainingData.addAll(WekaUtil.getInstancesOfClass(training, b));

				c.buildClassifier(trainingData);

				Instances validationData = WekaUtil.getInstancesOfClass(validation, a);
				validationData.addAll(WekaUtil.getInstancesOfClass(validation, b));
				Evaluation eval = new Evaluation(trainingData);
				eval.evaluateModel(c, validationData);
				
				
				if (!separabilities.containsKey(a))
					separabilities.put(a, new HashMap<>());
				separabilities.get(a).put(b, eval.pctCorrect() / 100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.classCount = WekaUtil.getNumberOfInstancesPerClass(training);
		sum = training.size();
		System.out.println(separabilities);
	}
	
	public double getSeparability(String c1, String c2) {
		if (c1.equals(c2))
			throw new IllegalArgumentException("Cannot separate a class from itself.");
		if (c1.compareTo(c2) > 0)
			return getSeparability(c2, c1);
		return separabilities.get(c1).get(c2);
	}
	
	public double getUpperBoundOnSeparability(Collection<String> classes) {
		double max = 0;
		for (Collection<String> pair : SetUtil.getAllPossibleSubsetsWithSize(classes, 2)) {
			Iterator<String> i = pair.iterator();
			String a = i.next();
			String b = i.next();
			double expectedContributionToError = (1 - getSeparability(a, b));
			double relativeExpectedContributionToError = expectedContributionToError * (classCount.get(a) + classCount.get(b)) / (1f * sum);
			max = Math.max(max, relativeExpectedContributionToError);
		}
		return 1 - max;
	}

	public double getAverageSeparability(Collection<String> classes) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Collection<String> pair : SetUtil.getAllPossibleSubsetsWithSize(classes, 2)) {
			Iterator<String> i = pair.iterator();
			String a = i.next();
			String b = i.next();
			stats.addValue(getSeparability(a, b));
		}
		return stats.getMean();
	}
	
	public double getMultipliedSeparability(Collection<String> classes) {
		double seperability = 1;
		for (Collection<String> pair : SetUtil.getAllPossibleSubsetsWithSize(classes, 2)) {
			Iterator<String> i = pair.iterator();
			String a = i.next();
			String b = i.next();
			seperability *= getSeparability(a, b);
		}
		return seperability;
	}
}
