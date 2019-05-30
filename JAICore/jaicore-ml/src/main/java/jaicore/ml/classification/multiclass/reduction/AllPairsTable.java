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

	public AllPairsTable(final Instances training, final Instances validation, final Classifier c) {
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


				if (!this.separabilities.containsKey(a)) {
					this.separabilities.put(a, new HashMap<>());
				}
				this.separabilities.get(a).put(b, eval.pctCorrect() / 100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.classCount = WekaUtil.getNumberOfInstancesPerClass(training);
		this.sum = training.size();
	}

	public double getSeparability(final String c1, final String c2) {
		if (c1.equals(c2)) {
			throw new IllegalArgumentException("Cannot separate a class from itself.");
		}
		if (c1.compareTo(c2) > 0) {
			return this.getSeparability(c2, c1);
		}
		return this.separabilities.get(c1).get(c2);
	}

	public double getUpperBoundOnSeparability(final Collection<String> classes) {
		double max = 0;
		for (Collection<String> pair : SetUtil.getAllPossibleSubsetsWithSize(classes, 2)) {
			Iterator<String> i = pair.iterator();
			String a = i.next();
			String b = i.next();
			double expectedContributionToError = (1 - this.getSeparability(a, b));
			double relativeExpectedContributionToError = expectedContributionToError * (this.classCount.get(a) + this.classCount.get(b)) / (1f * this.sum);
			max = Math.max(max, relativeExpectedContributionToError);
		}
		return 1 - max;
	}

	public double getAverageSeparability(final Collection<String> classes) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Collection<String> pair : SetUtil.getAllPossibleSubsetsWithSize(classes, 2)) {
			Iterator<String> i = pair.iterator();
			String a = i.next();
			String b = i.next();
			stats.addValue(this.getSeparability(a, b));
		}
		return stats.getMean();
	}

	public double getMultipliedSeparability(final Collection<String> classes) {
		double seperability = 1;
		for (Collection<String> pair : SetUtil.getAllPossibleSubsetsWithSize(classes, 2)) {
			Iterator<String> i = pair.iterator();
			String a = i.next();
			String b = i.next();
			seperability *= this.getSeparability(a, b);
		}
		return seperability;
	}
}
