package ai.libs.jaicore.ml.weka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.attribute.IAttributeType;
import org.api4.java.ai.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import org.api4.java.ai.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import org.api4.java.ai.ml.core.dataset.attribute.primitive.BooleanAttributeType;
import org.api4.java.ai.ml.core.dataset.attribute.primitive.BooleanAttributeValue;
import org.api4.java.ai.ml.core.dataset.attribute.primitive.NumericAttributeType;
import org.api4.java.ai.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstance;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstances;
import weka.core.Instances;

@RunWith(Parameterized.class)
public class WekaInstancesTester {

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<File[]> data() throws IOException, Exception {
		List<File> datasets = new ArrayList<>();
		datasets.add(new File("testrsc/ml/orig/amazon-subseteval.arff"));
		datasets.add(new File("testrsc/ml/orig/vowel.arff"));
		datasets.add(new File("testrsc/ml/orig/letter.arff"));
		File[][] data = new File[datasets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = datasets.get(i);
		}
		return Arrays.asList(data);
	}

	@Parameter(0)
	public File dataset;

	@Test
	public void testConversionToWekaInstances() throws Exception {
		Instances data = new Instances(new FileReader(this.dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances<Object> wrapped = new WekaInstances<>(data);
		int n = data.size();

		/* check that attribute types coincide */
		int numAttributes = data.numAttributes() - 1;
		assertEquals(numAttributes, wrapped.getNumberOfAttributes());
		assertEquals(numAttributes, wrapped.getAttributeTypes().size());
		for (int i = 0; i < numAttributes; i++) {
			IAttributeType<?> type = wrapped.getAttributeTypes().get(i);
			assertEquals(data.attribute(i).isNumeric(), type instanceof NumericAttributeType);
			if (data.attribute(i).isNominal()) {
				boolean isBinary = data.attribute(i).numValues() == 2;
				assertEquals("Nominal attribute \"" + data.attribute(i).name() + "\" is binary but is of type " + type.getClass().getName() + " in the wrapped model (should be " + BooleanAttributeType.class.getName() + ")", isBinary, type instanceof BooleanAttributeType);
				assertEquals("Nominal attribute \"" + data.attribute(i).name() + "\" is not binary but is of type " + type.getClass().getName() + " in the wrapped model (should be " + CategoricalAttributeType.class.getName() + ")", !isBinary, type instanceof CategoricalAttributeType);
			}
		}


		/* check that data is transferred correctly */
		assertEquals(n, wrapped.size());
		for (int i = 0; i < n; i++) {
			WekaInstance<Object> inst = wrapped.get(i);
			assertNotNull(inst.getElement());
			assertEquals(inst.getElement(), data.get(i)); // instance has not changed

			/* check for each value that the contained information is correct */
			for (int j = 0; j <= numAttributes; j++) {
				if (j < numAttributes) {
					Object value = inst.getAttributeValueAtPosition(j, Object.class);
					if (value instanceof NumericAttributeValue) {
						assertEquals("Attribute \"" + data.get(i).attribute(j).name() + "\" has value " + inst.getAttributeValueAtPosition(j, Object.class) + " but should have " + data.get(i).value(j), data.get(i).value(j), ((NumericAttributeValue) value).getValue(), 0.0);
					}
					else if (value instanceof BooleanAttributeValue) {
						assertEquals("Attribute \"" + data.get(i).attribute(j).name() + "\" has value " + inst.getAttributeValueAtPosition(j, Object.class) + " but should have " + (data.get(i).value(j) == 1.0), data.get(i).value(j) == 1.0, ((BooleanAttributeValue) value).getValue());
					}
					else if (value instanceof CategoricalAttributeValue) {
						assertEquals("Attribute \"" + data.get(i).attribute(j).name() + "\" has value " + inst.getAttributeValueAtPosition(j, Object.class) + " but should have " + data.get(i).stringValue(j), data.get(i).stringValue(j), ((CategoricalAttributeValue) value).getValue());
					} else {
						fail("Unsupported attribute value type " + value.getClass());
					}
				} else {
					if (inst.getTargetValue() instanceof String) {
						assertEquals(WekaUtil.getClassName(data.get(i)), inst.getTargetValue());
					}
					else {
						assertEquals(data.get(i).classValue(), inst.getTargetValue());
					}
				}
			}
			assertNotNull(i + "-th instance has target value null!", inst.getTargetValue());
		}
	}

	@Test
	public void testCreateEmpty() throws Exception {
		Instances data = new Instances(new FileReader(this.dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances<Object> wrapped = new WekaInstances<>(data);
		int size = wrapped.size();
		WekaInstances<Object> emptyCopy = wrapped.createEmpty();

		/* check that the empty copy indeed IS empty and that the original list is unchanged */
		assertTrue(emptyCopy.isEmpty());
		assertEquals(size, wrapped.size());

		/* check that attribute types coincide */
		List<IAttributeType<?>> attributeTypesOfDataset = wrapped.getAttributeTypes();
		List<IAttributeType<?>> attributeTypesOfEmptyCopy = emptyCopy.getAttributeTypes();
		int n = attributeTypesOfDataset.size();
		assertEquals(n, attributeTypesOfEmptyCopy.size());
		for (int i = 0; i < n; i++) {
			assertEquals((i + 1) + "-th attribute should be of type " + attributeTypesOfDataset.get(i) + " but is " + attributeTypesOfEmptyCopy.get(i), attributeTypesOfDataset.get(i).getClass(), attributeTypesOfEmptyCopy.get(i).getClass());
		}

		/* check that target type is the same */
		assertEquals(wrapped.getTargetType().getClass(), emptyCopy.getTargetType().getClass());
	}

	@Test
	public void testIterability() throws Exception {
		Instances data = new Instances(new FileReader(this.dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances<Object> wrapped = new WekaInstances<>(data);
		for (WekaInstance<Object> wi : wrapped) {
			assertTrue(data.contains(wi.getElement()));
		}
	}

	@Test
	public void testEqualnessOfTwoCopiesOfSameDataset() throws Exception {
		Instances ds1 = new Instances(new FileReader(this.dataset));
		ds1.setClassIndex(ds1.numAttributes() - 1);
		WekaInstances<Object> wrapped1 = new WekaInstances<>(ds1);

		Instances ds2 = new Instances(new FileReader(this.dataset));
		ds2.setClassIndex(ds2.numAttributes() - 1);
		WekaInstances<Object> wrapped2 = new WekaInstances<>(ds2);

		/* first conduct an instance-wise comparison and a mutual containment check */
		int n = ds1.size();
		assertEquals("Copy of dataset has different length than the original.", n, ds2.size());
		for (int i = 0; i < n; i++) {
			WekaInstance<Object> i1 = wrapped1.get(i);
			WekaInstance<Object> i2 = wrapped2.get(i);
			assertEquals("Hash codes of single instance don't match!", i1.hashCode(), i2.hashCode());
			assertEquals("Comparing the instances with equals yields false.", i1, i2);
			assertTrue("The second dataset does not contain " + i1 + ", which is contained in the first.", wrapped2.contains(i1));
			assertTrue("The first dataset does not contain " + i2 + ", which is contained in the second.", wrapped1.contains(i2));
		}

		/* now compare the entire dataset */
		assertEquals("Hash codes of entire dataset don't match!", wrapped1.hashCode(), wrapped2.hashCode());
		assertEquals("Comparing the datasets with equals yields false.", wrapped1, wrapped2);
	}

	@Test
	public void testContainsPredicate() throws Exception {
		Instances data = new Instances(new FileReader(this.dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances<Object> wrapped = new WekaInstances<>(data);

		for (WekaInstance<Object> i : wrapped) {
			assertTrue(wrapped.contains(i));
		}
	}

	@Test
	public void testSelfEqualness() throws Exception {
		Instances data = new Instances(new FileReader(this.dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances<Object> wrapped = new WekaInstances<>(data);

		for (WekaInstance<Object> i : wrapped) {
			assertTrue(i.equals(i));
		}
		assertEquals(wrapped, wrapped);
	}

	@Test
	public void testThatEveryInstanceOccursOnlyOnce() throws Exception {
		Instances data = new Instances(new FileReader(this.dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances<Object> wrapped = new WekaInstances<>(data);
		int n = wrapped.size();

		for (int i = 0; i < n; i++) {
			WekaInstance<Object> x = wrapped.get(i);
			for (int j = 0; j < n; j++) {
				if (i != j) {
					WekaInstance<Object> y = wrapped.get(j);
					assertFalse("Instance " + i + " and " + j + " are identical:\n\t" + Arrays.toString(x.getAsDoubleVector()) + " with label " + x.getTargetValue() + "\n\t" + Arrays.toString(y.getAsDoubleVector()) + " with label " + y.getTargetValue(), x.equals(y));
				}
			}
		}
	}

	@Test
	public void testArraysCorrespondToListViaEquals() throws Exception {
		Instances data = new Instances(new FileReader(this.dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances<Object> wrapped = new WekaInstances<>(data);

		/* check object array */
		Object[] dataAsArray = wrapped.toArray();
		int n = dataAsArray.length;
		assertEquals(wrapped.size(), n);
		for (int i = 0; i < n; i++) {
			assertTrue(wrapped.get(i).equals(dataAsArray[i]));
		}

		/* check Instance array */
		WekaInstance<Object>[] dataAsSpecificArray = wrapped.toArray(new WekaInstance[0]);
		n = dataAsSpecificArray.length;
		assertEquals(wrapped.size(), n);
		for (int i = 0; i < n; i++) {
			assertTrue(wrapped.get(i).equals(dataAsSpecificArray[i]));
		}
	}
}
