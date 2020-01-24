package ai.libs.jaicore.ml.core.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public class DatasetTestUtil {
	public static void checkDatasetCoherence(final ILabeledDataset<?> ds) {

		/* check number of attributes */
		int numAttributes = ds.getNumAttributes();
		assertEquals("Number of attributes of the dataset deviate from the number of attributes defined in the schema of the dataset.", numAttributes, ds.getInstanceSchema().getNumAttributes());
		for (ILabeledInstance i : ds) {
			assertNotNull("Instance " + i + " has no label.", i.getLabel());
			assertEquals("Number of attributes of the instance deviate from the number of attributes defined in the schema.", numAttributes, i.getNumAttributes());

			/* check validity of instance values by direct access */
			for (int j = 0; j < numAttributes; j++) {
				IAttribute attribute = ds.getAttribute(j);
				assertNotNull("Attribute " + j + " when retrieved via getAttribute(j) from dataset is NULL for dataset of type " + ds.getClass().getName(), attribute);
				assertTrue(attribute.isValidValue(i.getAttributeValue(j)));
			}

			/* check validity of instance values by array access */
			double[] point = i.getPoint();
			for (int j = 0; j < numAttributes; j++) {
				IAttribute attribute = ds.getAttribute(j);
				assertNotNull("Attribute " + j + " when retrieved via getAttribute(j) from dataset is NULL for dataset of type " + ds.getClass().getName(), attribute);
				assertTrue(point[j] + " is not a valid value for attribute " + attribute.getName() + " with domain " + attribute.getStringDescriptionOfDomain() + " (" + attribute.getClass().getName() + ")", attribute.isValidValue(point[j]));
			}

			/* check equalness of values obtained via getAttributes()[j] and getAttribute(j)  */
			Object[] values = i.getAttributes();
			for (int j = 0; j < numAttributes; j++) {
				IAttribute attribute = ds.getAttribute(j);
				assertNotNull("Attribute " + j + " when retrieved via getAttribute(j) from dataset is NULL for dataset of type " + ds.getClass().getName(), attribute);
				assertEquals(values[j], i.getAttributeValue(j));
			}
		}
	}
}
