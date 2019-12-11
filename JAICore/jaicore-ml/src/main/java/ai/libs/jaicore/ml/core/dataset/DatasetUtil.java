package ai.libs.jaicore.ml.core.dataset;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.basic.sets.SetUtil;

public class DatasetUtil {

	public static Map<Object, Integer> getLabelCounts(final ILabeledDataset<?> ds) {
		Map<Object, Integer> labelCounter = new HashMap<>();
		ds.forEach(li -> {
			Object label = ((ILabeledInstance)li).getLabel();
			labelCounter.put(label, labelCounter.computeIfAbsent(label, l -> 0) + 1);
		});
		return labelCounter;
	}

	public static int getLabelCountDifference(final ILabeledDataset<?> d1, final ILabeledDataset<?> d2) {
		Map<Object, Integer> c1 = getLabelCounts(d1);
		Map<Object, Integer> c2 = getLabelCounts(d2);
		Collection<Object> labels = SetUtil.union(c1.keySet(), c2.keySet());
		int diff = 0;
		for (Object label : labels) {
			diff += Math.abs(c1.get(label) - c2.get(label));
		}
		return diff;
	}
}
