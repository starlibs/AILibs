package ai.libs.jaicore.ml.weka.classification.learner.reduction.splitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;

import ai.libs.jaicore.ml.weka.WekaUtil;
import weka.core.Instances;

public class RandomSplitter implements ISplitter {

	private final Random rand;

	public RandomSplitter(final Random rand) {
		super();
		this.rand = rand;
	}

	@Override
	public Collection<Collection<String>> split(final Instances data) throws SplitFailedException {
		Collection<Collection<String>> split = new ArrayList<>();
		Collection<String> classes = WekaUtil.getClassesActuallyContainedInDataset(data);
		if (classes.size() == 1) {
			split.add(classes);
			return split;
		}
		List<String> copy = new ArrayList<>(classes);
		Collections.shuffle(copy, this.rand);
		int splitIndex = this.rand.nextInt(classes.size()) + 1;
		Collection<String> s1 = copy.subList(0, splitIndex);
		Collection<String> s2 = copy.subList(splitIndex, copy.size());
		split.add(s1);
		split.add(s2);
		return split;
	}
}