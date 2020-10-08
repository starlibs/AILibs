package ai.libs.jaicore.ml.weka.weka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstance;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstance;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.test.LongParameterizedTest;
import ai.libs.jaicore.test.LongTest;
import weka.core.Instances;

public class WekaInstancesTester {

	public static Stream<Arguments> getDatasets() throws IOException, Exception {
		List<File> datasets = new ArrayList<>();
		datasets.add(new File("testrsc/ml/orig/amazon-subseteval.arff"));
		datasets.add(new File("testrsc/ml/orig/vowel.arff"));
		datasets.add(new File("testrsc/ml/orig/letter.arff"));
		return datasets.stream().map(Arguments::of);
	}

	@ParameterizedTest(name="test conversion to WekaInstances on {0}")
	@MethodSource("getDatasets")
	public void testConversionToWekaInstances(final File dataset) throws Exception {
		Instances data = new Instances(new FileReader(dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances wrapped = new WekaInstances(data);
		int n = data.size();

		/* check that attribute types coincide */
		int numAttributes = data.numAttributes() - 1;
		assertEquals(numAttributes, wrapped.getNumAttributes());
		assertEquals(numAttributes, wrapped.getListOfAttributes().size());
		for (int i = 0; i < numAttributes; i++) {
			IAttribute type = wrapped.getListOfAttributes().get(i);
			assertEquals(data.attribute(i).isNumeric(), type instanceof INumericAttribute);
			if (data.attribute(i).isNominal()) {
				boolean isBinary = data.attribute(i).numValues() == 2;
				if (isBinary) {
					assertEquals("Nominal attribute \"" + data.attribute(i).name() + "\" is binary but is of type " + type.getClass().getName() + " in the wrapped model (should be " + ICategoricalAttribute.class.getName() + ")", isBinary,
							(type instanceof ICategoricalAttribute) && ((ICategoricalAttribute) type).getLabels().size() == 2);
				} else {
					assertEquals("Nominal attribute \"" + data.attribute(i).name() + "\" is not binary but is of type " + type.getClass().getName() + " in the wrapped model (should be " + ICategoricalAttribute.class.getName() + ")",
							!isBinary, type instanceof ICategoricalAttribute);
				}
			}
		}

		/* check that data is transferred correctly */
		assertEquals(n, wrapped.size());
		for (int i = 0; i < n; i++) {
			IWekaInstance inst = wrapped.get(i);
			assertNotNull(inst.getElement());
			assertEquals(inst.getElement(), data.get(i)); // instance has not changed

			/* check for each value that the contained information is correct */
			for (int j = 0; j <= numAttributes; j++) {
				if (j < numAttributes) {
					Double value = (Double) inst.getAttributeValue(j);
					if (data.attribute(j).isNumeric()) {
						assertEquals("Attribute \"" + data.get(i).attribute(j).name() + "\" has value " + value + " but should have " + data.get(i).value(j), data.get(i).value(j), value, 0.0);
					} else if (data.attribute(j).isNominal()) {
						String expectedValue = data.attribute(j).value((int) data.get(i).value(j));
						ICategoricalAttribute type = (ICategoricalAttribute) wrapped.getListOfAttributes().get(j);
						int intValue = (int) Math.round(value);
						String wrappedValue = type.serializeAttributeValue(intValue);
						assertEquals("Attribute \"" + data.get(i).attribute(j).name() + "\" has value " + wrappedValue + " but should have " + expectedValue, expectedValue, wrappedValue);
					} else {
						fail("Unsupported attribute value type " + value.getClass());
					}
				} else {
					if (wrapped.getLabelAttribute() instanceof INumericAttribute) {
						assertEquals("Class has value " + inst.getLabel() + " but should have" + data.get(i).classValue(), data.get(i).classValue(), (Double) inst.getLabel(), 0.0);
					} else {
						String expectedValue = data.attribute(j).value((int) data.get(i).value(j)); // get true label
						String wrappedValue = ((ICategoricalAttribute) wrapped.getLabelAttribute()).getLabelOfCategory((int) inst.getLabel()).toString(); // get wrapped label attribute
						assertEquals("Class has value " + wrappedValue + " but should have " + WekaUtil.getClassName(data.get(i)), expectedValue, wrappedValue);
					}
				}
			}
			assertNotNull(i + "-th instance has target value null!", inst.getLabel());
		}
	}

	@ParameterizedTest(name="test create empty for {0}")
	@MethodSource("getDatasets")
	public void testCreateEmpty(final File dataset) throws Exception {
		Instances data = new Instances(new FileReader(dataset));
		data.setClassIndex(data.numAttributes() - 1);
		IWekaInstances wrapped = new WekaInstances(data);
		int size = wrapped.size();
		IWekaInstances emptyCopy = wrapped.createEmptyCopy();

		/* check that the empty copy indeed IS empty and that the original list is unchanged */
		assertTrue(emptyCopy.isEmpty());
		assertEquals(size, wrapped.size());

		/* check that attribute types coincide */
		List<IAttribute> attributeTypesOfDataset = wrapped.getListOfAttributes();
		List<IAttribute> attributeTypesOfEmptyCopy = emptyCopy.getListOfAttributes();
		int n = attributeTypesOfDataset.size();
		assertEquals(n, attributeTypesOfEmptyCopy.size());
		for (int i = 0; i < n; i++) {
			assertEquals((i + 1) + "-th attribute should be of type " + attributeTypesOfDataset.get(i) + " but is " + attributeTypesOfEmptyCopy.get(i), attributeTypesOfDataset.get(i).getClass(), attributeTypesOfEmptyCopy.get(i).getClass());
		}

		/* check that target type is the same */
		assertEquals(wrapped.getLabelAttribute().getClass(), emptyCopy.getLabelAttribute().getClass());
	}

	@ParameterizedTest(name="test copy creation on {0}")
	@MethodSource("getDatasets")
	public void testCopy(final File dataset) throws Exception {
		Instances data = new Instances(new FileReader(dataset));
		data.setClassIndex(data.numAttributes() - 1);
		IWekaInstances wrapped = new WekaInstances(data);
		IWekaInstances copy = (IWekaInstances) wrapped.createCopy();

		/* check that the copy is equal to the original */
		assertEquals(wrapped, copy);

		/* check that the copy is not the same anymore if one element is removed */
		copy.remove(0);
		assertNotEquals(wrapped, copy);
	}

	@ParameterizedTest(name="test iterability on {0}")
	@MethodSource("getDatasets")
	public void testIterability(final File dataset) throws Exception {
		Instances data = new Instances(new FileReader(dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances wrapped = new WekaInstances(data);
		for (IWekaInstance wi : wrapped) {
			assertTrue(data.contains(wi.getElement()));
		}
	}

	@ParameterizedTest(name="test equalness of two copies of {0}")
	@LongTest
	@MethodSource("getDatasets")
	public void testEqualnessOfTwoCopiesOfSameDataset(final File dataset) throws Exception {
		Instances ds1 = new Instances(new FileReader(dataset));
		ds1.setClassIndex(ds1.numAttributes() - 1);
		WekaInstances wrapped1 = new WekaInstances(ds1);

		Instances ds2 = new Instances(new FileReader(dataset));
		ds2.setClassIndex(ds2.numAttributes() - 1);
		WekaInstances wrapped2 = new WekaInstances(ds2);

		/* first conduct an instance-wise comparison and a mutual containment check */
		int n = ds1.size();
		assertEquals("Copy of dataset has different length than the original.", n, ds2.size());
		for (int i = 0; i < n; i++) {
			IWekaInstance i1 = wrapped1.get(i);
			IWekaInstance i2 = wrapped2.get(i);
			assertEquals("Hash codes of single instance don't match!", i1.hashCode(), i2.hashCode());
			assertEquals("Comparing the instances with equals yields false.", i1, i2);
			assertTrue("The second dataset does not contain " + i1 + ", which is contained in the first.", wrapped2.contains(i1));
			assertTrue("The first dataset does not contain " + i2 + ", which is contained in the second.", wrapped1.contains(i2));
		}

		/* now compare the entire dataset */
		assertEquals("Hash codes of entire dataset don't match!", wrapped1.hashCode(), wrapped2.hashCode());
		assertEquals("Comparing the datasets with equals yields false.", wrapped1, wrapped2);
	}

	@LongParameterizedTest
	@MethodSource("getDatasets")
	public void testContainsPredicate(final File dataset) throws Exception {
		Instances data = new Instances(new FileReader(dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances wrapped = new WekaInstances(data);

		for (IWekaInstance i : wrapped) {
			assertTrue(wrapped.contains(i));
		}
	}

	@ParameterizedTest(name="test self-equalness of {0}")
	@MethodSource("getDatasets")
	public void testSelfEqualness(final File dataset) throws Exception {
		Instances data = new Instances(new FileReader(dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances wrapped = new WekaInstances(data);

		for (IWekaInstance i : wrapped) {
			assertTrue(i.equals(i));
		}
		assertEquals(wrapped, wrapped);
	}

	@ParameterizedTest(name="test array correspondence on {0}")
	@MethodSource("getDatasets")
	public void testArraysCorrespondToListViaEquals(final File dataset) throws Exception {
		Instances data = new Instances(new FileReader(dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances wrapped = new WekaInstances(data);

		/* check object array */
		Object[] dataAsArray = wrapped.toArray();
		int n = dataAsArray.length;
		assertEquals(wrapped.size(), n);
		for (int i = 0; i < n; i++) {
			assertTrue(wrapped.get(i).equals(dataAsArray[i]));
		}

		/* check Instance array */
		WekaInstance[] dataAsSpecificArray = wrapped.toArray(new WekaInstance[0]);
		n = dataAsSpecificArray.length;
		assertEquals(wrapped.size(), n);
		for (int i = 0; i < n; i++) {
			assertTrue(wrapped.get(i).equals(dataAsSpecificArray[i]));
		}
	}

	@LongTest
	@ParameterizedTest(name="test contains on {0}")
	@MethodSource("getDatasets")
	public void testThatEachElementIsFoundWithContains(final File dataset) throws FileNotFoundException, IOException {
		Instances data = new Instances(new FileReader(dataset));
		data.setClassIndex(data.numAttributes() - 1);
		WekaInstances wrapped = new WekaInstances(data);
		for (IWekaInstance wi : wrapped) {
			assertTrue(wrapped.contains(wi));
		}
	}
}
