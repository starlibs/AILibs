package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.CategoricalAttributeValue;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttributeValue;
import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;
import ai.libs.jaicore.ml.core.olddataset.simple.SimpleDataset;
import ai.libs.jaicore.ml.core.olddataset.simple.SimpleInstance;

public class AttributeBasedStratiAmountSelectorAndAssignerTester {

	@Test
	public void test_number_allAttributes_categorical_serial() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(6, computedNumber);
	}

	@Test
	public void test_number_allAttributes_categorical_parallel() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(6, computedNumber);
	}

	@Test
	public void test_number_onlyTargetAttribute_categorical_serial() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(3, computedNumber);
	}

	@Test
	public void test_number_onlyTargetAttribute_categorical_parallel() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(3, computedNumber);
	}

	@Test
	public void test_assignment_onlyTargetAttribute_categorical_serial() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(1);
		selectorAndAssigner.init(dataset);
		Map<IClusterableInstance, Integer> stratiAssignment = new HashMap<>();
		for (IClusterableInstance i : dataset) {
			stratiAssignment.put(i, selectorAndAssigner.assignToStrati(i));
		}
		// Number of strati must be 3
		assertEquals(3, new HashSet<>(stratiAssignment.values()).size());

//		assertTrue("Instances 1 and 3 need to be in the same stratum", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(2)));
//
//		assertFalse("Instances 1 and 2 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(1)));
//
//		assertFalse("Instances 1 and 4 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(3)));
//
//		assertFalse("Instances 2 and 3 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(1)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(2)));
//
//		assertFalse("Instances 2 and 4 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(1)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(3)));
//
//		assertFalse("Instances 3 and 4 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(2)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(3)));
	}

	@Test
	public void test_assignment_onlyTargetAttribute_categorical_parallel() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(4);
		selectorAndAssigner.init(dataset);
		Map<IClusterableInstance, Integer> stratiAssignment = new HashMap<>();
		for (IClusterableInstance i : dataset) {
			stratiAssignment.put(i, selectorAndAssigner.assignToStrati(i));
		}
		// Number of strati must be 3
		assertEquals(3, new HashSet<>(stratiAssignment.values()).size());

//		assertTrue("Instances 1 and 3 need to be in the same stratum", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(2)));
//
//		assertFalse("Instances 1 and 2 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(1)));
//
//		assertFalse("Instances 1 and 4 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(3)));
//
//		assertFalse("Instances 2 and 3 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(1)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(2)));
//
//		assertFalse("Instances 2 and 4 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(1)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(3)));
//
//		assertFalse("Instances 3 and 4 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(2)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(3)));
	}

	@Test
	public void test_number_allAttributes_mixed_equalLength_serial() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_LENGTH, 2);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_number_allAttributes_mixed_equalLength_parallel() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_LENGTH, 2);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_number_allAttributes_mixed_equalSize_serial() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_number_allAttributes_mixed_equalSize_parallel() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_assignment_onlyTargetAttribute_mixed_serial() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(1);
		selectorAndAssigner.init(dataset);
		Map<IClusterableInstance, Integer> stratiAssignment = new HashMap<>();
		for (IClusterableInstance i : dataset) {
			stratiAssignment.put(i, selectorAndAssigner.assignToStrati(i));
		}
		// Number of strati must be 2
		assertEquals(2, new HashSet<>(stratiAssignment.values()).size());

//		assertTrue("Instances 1 and 3 need to be in the same stratum", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(2)));
//
//		assertTrue("Instances 1 and 4 need to be in the same stratum", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(3)));
//
//		assertFalse("Instances 1 and 2 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(1)));
//
//		assertFalse("Instances 1 and 5 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(4)));
//
//		assertFalse("Instances 1 and 6 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(5)));
	}

	@Test
	public void test_assignment_onlyTargetAttribute_mixed_parallel() {
		ILabeledDataset<IClusterableInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<IClusterableInstance, ILabeledDataset<IClusterableInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(4);
		selectorAndAssigner.init(dataset);
		Map<IClusterableInstance, Integer> stratiAssignment = new HashMap<>();
		for (IClusterableInstance i : dataset) {
			stratiAssignment.put(i, selectorAndAssigner.assignToStrati(i));
		}
		// Number of strati must be 2
		assertEquals(2, new HashSet<>(stratiAssignment.values()).size());

//		assertTrue("Instances 1 and 3 need to be in the same stratum", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(2)));
//
//		assertTrue("Instances 1 and 4 need to be in the same stratum", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(3)));
//
//		assertFalse("Instances 1 and 2 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(1)));
//
//		assertFalse("Instances 1 and 5 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(4)));
//
//		assertFalse("Instances 1 and 6 need to be in the different strati", stratiAssignment.getAttributeValue(dataset.getAttributeValue(0)) == stratiAssignment.getAttributeValue(dataset.getAttributeValue(5)));
	}

	public ILabeledDataset<IClusterableInstance> createToyDatasetOnlyCategorical() {
		// Attribute 1
		String[] domain1 = { "A", "B" };
		IAttribute type1 = new CategoricalAttribute("a1", Arrays.asList(domain1));

		// Attribute 2
		String[] domain2 = { "C" };
		IAttribute type2 = new CategoricalAttribute("a2", Arrays.asList(domain2));

		// Attribute 3
		String[] domain3 = { "X", "Y", "Z" };
		IAttribute type3 = new CategoricalAttribute("a3", Arrays.asList(domain3));

		List<IAttribute> attributeTypeList = new ArrayList<>();
		attributeTypeList.add(type1);
		attributeTypeList.add(type2);

		ILabeledInstanceSchema schema = new LabeledInstanceSchema("testData", attributeTypeList, type3);

		ILabeledDataset<IClusterableInstance> simpleDataset = new SimpleDataset(schema);
		List<List<Object>> features = Arrays.asList(Arrays.asList("A", "C"), Arrays.asList("A", "C"), Arrays.asList("B", "C"), Arrays.asList("A", "C"));
		List<Object> labels = Arrays.asList("X", "Y", "X", "Z");

		// generate SimpleInstance objects and add to dataset
		IntStream.range(0, features.size()).mapToObj(x -> new SimpleInstance(schema, features.get(x), labels.get(x))).forEach(simpleDataset::add);

		return simpleDataset;
	}

	public ILabeledDataset<IClusterableInstance> createToyDatasetMixed() {
		// Attribute 1 (categorical)
		String[] domain1 = { "A", "B" };
		IAttribute type1 = new CategoricalAttribute("att0", Arrays.asList(domain1));

		// Attribute 2 (numeric)
		IAttribute type2 = new NumericAttribute("att1");

		// Attribute 3 (numeric)
		IAttribute type3 = new NumericAttribute("att2");

		List<IAttribute> attributeTypeList = new ArrayList<>();
		attributeTypeList.add(type1);
		attributeTypeList.add(type2);

		ILabeledInstanceSchema schema = new LabeledInstanceSchema(attributeTypeList, type3);

		SimpleDataset simpleDataset = new SimpleDataset(schema);

		// Instance 1
		IAttributeValue value11 = new CategoricalAttributeValue((ICategoricalAttribute) type1, "A");
		IAttributeValue value12 = new NumericAttributeValue((INumericAttribute) type2, 0.0);
		double value13 = 1.0;

		// Instance 2
		IAttributeValue value21 = new CategoricalAttributeValue((ICategoricalAttribute) type1, "B");
		IAttributeValue value22 = new NumericAttributeValue((INumericAttribute) type2, 3.0);
		double value23 = 20.0;

		// Instance 3
		IAttributeValue value31 = new CategoricalAttributeValue((ICategoricalAttribute) type1, "A");
		IAttributeValue value32 = new NumericAttributeValue((INumericAttribute) type2, 3.0);
		double value33 = -2.0;

		// Instance 4
		IAttributeValue value41 = new CategoricalAttributeValue((ICategoricalAttribute) type1, "B");
		IAttributeValue value42 = new NumericAttributeValue((INumericAttribute) type2, 2.0);
		double value43 = 1.5;

		// Instance 5
		IAttributeValue value51 = new CategoricalAttributeValue((ICategoricalAttribute) type1, "B");
		IAttributeValue value52 = new NumericAttributeValue((INumericAttribute) type2, 6.0);
		double value53 = 3.0;

		// Instance 6
		IAttributeValue value61 = new CategoricalAttributeValue((ICategoricalAttribute) type1, "A");
		IAttributeValue value62 = new NumericAttributeValue((INumericAttribute) type2, 10.0);
		double value63 = 5.0;

		ArrayList<IAttributeValue> values1 = new ArrayList<>();
		values1.add(value11);
		values1.add(value12);
		SimpleInstance i1 = new SimpleInstance(values1, value13);

		ArrayList<IAttributeValue> values2 = new ArrayList<>();
		values2.add(value21);
		values2.add(value22);
		SimpleInstance i2 = new SimpleInstance(values2, value23);

		ArrayList<IAttributeValue> values3 = new ArrayList<>();
		values3.add(value31);
		values3.add(value32);
		SimpleInstance i3 = new SimpleInstance(values3, value33);

		ArrayList<IAttributeValue> values4 = new ArrayList<>();
		values4.add(value41);
		values4.add(value42);
		SimpleInstance i4 = new SimpleInstance(values4, value43);

		ArrayList<IAttributeValue> values5 = new ArrayList<>();
		values5.add(value51);
		values5.add(value52);
		SimpleInstance i5 = new SimpleInstance(values5, value53);

		ArrayList<IAttributeValue> values6 = new ArrayList<>();
		values6.add(value61);
		values6.add(value62);
		SimpleInstance i6 = new SimpleInstance(values6, value63);

		simpleDataset.add(i1);
		simpleDataset.add(i2);
		simpleDataset.add(i3);
		simpleDataset.add(i4);
		simpleDataset.add(i5);
		simpleDataset.add(i6);

		return simpleDataset;
	}

}
