package ai.libs.jaicore.ml.core.util;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.dataset.Dataset;
import ai.libs.jaicore.ml.core.dataset.DenseInstance;

public class TestDatasetGenerator {

	public static ILabeledDataset<ILabeledInstance> generateLabeledDataset(final ILabeledInstanceSchema schema, final String[] instances, final String[] labels) {
		if (instances.length != labels.length) {
			throw new IllegalArgumentException("Number of instances need to match number of labels");
		}

		ILabeledDataset<ILabeledInstance> dataset = new Dataset(schema);
		for (int i = 0; i < instances.length; i++) {
			String[] features = instances[i].split(",");
			if (features.length != schema.getNumAttributes()) {
				throw new IllegalArgumentException("Instance " + i + " has not the same number of attributes as defined in the schema");
			}
			Object[] iFeat = new Object[features.length];
			for (int att = 0; att < schema.getNumAttributes(); att++) {
				iFeat[att] = schema.getAttribute(att).deserializeAttributeValue(features[att]);
			}
			Object label = schema.getLabelAttribute().deserializeAttributeValue(labels[i]);
			dataset.add(new DenseInstance(iFeat, label));
		}
		return dataset;
	}

}
