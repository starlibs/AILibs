package jaicore.ml.classification.multiclass.reduction.splitters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.WekaUtil;
import weka.core.Instances;

public class RandomSplitter implements ISplitter {

	private static final Logger logger = LoggerFactory.getLogger(RandomSplitter.class);
	private final Random rand;

	public RandomSplitter(Random rand) {
		super();
		this.rand = rand;
	}

	@Override
	public Collection<Collection<String>> split(Instances data) throws Exception {
		Collection<Collection<String>> split = new ArrayList<>();
		Collection<String> classes = WekaUtil.getClassesActuallyContainedInDataset(data);
		if (classes.size() == 1) {
			split.add(classes);
			return split;
		}
		List<String> copy = new ArrayList<>(classes);
		Collections.shuffle(copy, rand);
		int splitIndex = (int)Math.ceil(Math.random()*(classes.size() - 1));
		Collection<String> s1 = copy.subList(0, splitIndex);
		Collection<String> s2 = copy.subList(splitIndex, copy.size());
		split.add(s1);
		split.add(s2);
		return split;
	}
}