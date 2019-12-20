package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.IntBasedCategoricalAttribute;

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

	public static ILabeledDataset<?> convertToClassificationDataset(final ILabeledDataset<?> dataset) {
		IAttribute currentLabelAttribute = dataset.getLabelAttribute();
		if (currentLabelAttribute instanceof ICategoricalAttribute) {
			return dataset;
		}
		Set<String> values = new HashSet<>();
		for (ILabeledInstance i : dataset) {
			values.add(i.getLabel().toString());
		}
		IntBasedCategoricalAttribute attr = new IntBasedCategoricalAttribute(currentLabelAttribute.getName(), new ArrayList<>(values));

		/* copy attribute list and exchange this attribute */
		List<IAttribute> attList = new ArrayList<>(dataset.getInstanceSchema().getAttributeList());

		/* get new scheme */
		LabeledInstanceSchema scheme = new LabeledInstanceSchema(dataset.getRelationName(), attList, attr);
		Dataset datasetModified = new Dataset(scheme);

		/* now copy all the instances*/
		int numAttributes = dataset.getNumAttributes();
		for (ILabeledInstance i : dataset) {
			ILabeledInstance ci;
			if (i instanceof DenseInstance) {
				ci = new DenseInstance(i.getAttributes(), attr.getIdOfLabel(i.getLabel().toString()));
			}
			else if (i instanceof SparseInstance) {
				ci = new SparseInstance(numAttributes, ((SparseInstance)i).getAttributeMap(), attr.getIdOfLabel(i.getLabel().toString()));
			}
			else {
				throw new UnsupportedOperationException();
			}
			if (!datasetModified.getLabelAttribute().isValidValue(ci.getLabel())) {
				throw new IllegalStateException("Value " + ci.getLabel() + " is not a valid label value for label attribute " + datasetModified.getLabelAttribute());
			}
			datasetModified.add(ci);
		}

		return datasetModified;
	}
}
