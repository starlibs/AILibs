package jaicore.ml.core.dataset.sampling.inmemory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import jaicore.ml.core.dataset.InstanceSchema;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.dataset.attribute.categorical.ICategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import jaicore.ml.core.dataset.standard.SimpleInstance;

public class AttributeBasedStratiAmountSelectorAndAssignerTester {

	@Test
	public void test_number_allAttributes_categorical_serial() {
		SimpleDataset dataset = createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(6, computedNumber);
	}

	@Test
	public void test_number_allAttributes_categorical_parallel() {
		SimpleDataset dataset = createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(6, computedNumber);
	}

	@Test
	public void test_number_onlyTargetAttribute_categorical_serial() {
		SimpleDataset dataset = createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(3, computedNumber);
	}

	@Test
	public void test_number_onlyTargetAttribute_categorical_parallel() {
		SimpleDataset dataset = createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(3, computedNumber);
	}

	@Test
	public void test_assignment_onlyTargetAttribute_categorical_serial() {
		SimpleDataset dataset = createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(1);
		selectorAndAssigner.init(dataset);
		Map<SimpleInstance, Integer> stratiAssignment = new HashMap<>();
		for (SimpleInstance i : dataset) {
			stratiAssignment.put(i, selectorAndAssigner.assignToStrati(i));
		}
		// Number of strati must be 3
		assertEquals(3, new HashSet<>(stratiAssignment.values()).size());

		assertTrue("Instances 1 and 3 need to be in the same stratum",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(2)));

		assertFalse("Instances 1 and 2 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(1)));

		assertFalse("Instances 1 and 4 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(3)));

		assertFalse("Instances 2 and 3 need to be in the different strati",
				stratiAssignment.get(dataset.get(1)) == stratiAssignment.get(dataset.get(2)));

		assertFalse("Instances 2 and 4 need to be in the different strati",
				stratiAssignment.get(dataset.get(1)) == stratiAssignment.get(dataset.get(3)));

		assertFalse("Instances 3 and 4 need to be in the different strati",
				stratiAssignment.get(dataset.get(2)) == stratiAssignment.get(dataset.get(3)));
	}

	@Test
	public void test_assignment_onlyTargetAttribute_categorical_parallel() {
		SimpleDataset dataset = createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(4);
		selectorAndAssigner.init(dataset);
		Map<SimpleInstance, Integer> stratiAssignment = new HashMap<>();
		for (SimpleInstance i : dataset) {
			stratiAssignment.put(i, selectorAndAssigner.assignToStrati(i));
		}
		// Number of strati must be 3
		assertEquals(3, new HashSet<>(stratiAssignment.values()).size());

		assertTrue("Instances 1 and 3 need to be in the same stratum",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(2)));

		assertFalse("Instances 1 and 2 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(1)));

		assertFalse("Instances 1 and 4 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(3)));

		assertFalse("Instances 2 and 3 need to be in the different strati",
				stratiAssignment.get(dataset.get(1)) == stratiAssignment.get(dataset.get(2)));

		assertFalse("Instances 2 and 4 need to be in the different strati",
				stratiAssignment.get(dataset.get(1)) == stratiAssignment.get(dataset.get(3)));

		assertFalse("Instances 3 and 4 need to be in the different strati",
				stratiAssignment.get(dataset.get(2)) == stratiAssignment.get(dataset.get(3)));
	}

	@Test
	public void test_number_allAttributes_mixed_equalLength_serial() {
		SimpleDataset dataset = createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices), DiscretizationStrategy.EQUAL_LENGTH, 2);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_number_allAttributes_mixed_equalLength_parallel() {
		SimpleDataset dataset = createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices), DiscretizationStrategy.EQUAL_LENGTH, 2);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_number_allAttributes_mixed_equalSize_serial() {
		SimpleDataset dataset = createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices), DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_number_allAttributes_mixed_equalSize_parallel() {
		SimpleDataset dataset = createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices), DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_assignment_onlyTargetAttribute_mixed_serial() {
		SimpleDataset dataset = createToyDatasetMixed();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices), DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(1);
		selectorAndAssigner.init(dataset);
		Map<SimpleInstance, Integer> stratiAssignment = new HashMap<>();
		for (SimpleInstance i : dataset) {
			stratiAssignment.put(i, selectorAndAssigner.assignToStrati(i));
		}
		// Number of strati must be 2
		assertEquals(2, new HashSet<>(stratiAssignment.values()).size());

		assertTrue("Instances 1 and 3 need to be in the same stratum",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(2)));

		assertTrue("Instances 1 and 4 need to be in the same stratum",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(3)));

		assertFalse("Instances 1 and 2 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(1)));

		assertFalse("Instances 1 and 5 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(4)));

		assertFalse("Instances 1 and 6 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(5)));
	}

	@Test
	public void test_assignment_onlyTargetAttribute_mixed_parallel() {
		SimpleDataset dataset = createToyDatasetMixed();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<SimpleInstance>(
				Arrays.asList(attributeIndices), DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(4);
		selectorAndAssigner.init(dataset);
		Map<SimpleInstance, Integer> stratiAssignment = new HashMap<>();
		for (SimpleInstance i : dataset) {
			stratiAssignment.put(i, selectorAndAssigner.assignToStrati(i));
		}
		// Number of strati must be 2
		assertEquals(2, new HashSet<>(stratiAssignment.values()).size());

		assertTrue("Instances 1 and 3 need to be in the same stratum",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(2)));

		assertTrue("Instances 1 and 4 need to be in the same stratum",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(3)));

		assertFalse("Instances 1 and 2 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(1)));

		assertFalse("Instances 1 and 5 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(4)));

		assertFalse("Instances 1 and 6 need to be in the different strati",
				stratiAssignment.get(dataset.get(0)) == stratiAssignment.get(dataset.get(5)));
	}

	public SimpleDataset createToyDatasetOnlyCategorical() {
		// Attribute 1
		String[] domain1 = { "A", "B" };
		IAttributeType<?> type1 = new CategoricalAttributeType(Arrays.asList(domain1));

		// Attribute 2
		String[] domain2 = { "C" };
		IAttributeType<?> type2 = new CategoricalAttributeType(Arrays.asList(domain2));

		// Attribute 3
		String[] domain3 = { "X", "Y", "Z" };
		IAttributeType<?> type3 = new CategoricalAttributeType(Arrays.asList(domain3));

		List<IAttributeType<?>> attributeTypeList = new ArrayList<>();
		attributeTypeList.add(type1);
		attributeTypeList.add(type2);

		InstanceSchema schema = new InstanceSchema(attributeTypeList, type3);

		SimpleDataset simpleDataset = new SimpleDataset(schema);

		// Instance 1
		IAttributeValue<?> value11 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "A");
		IAttributeValue<?> value12 = new CategoricalAttributeValue((ICategoricalAttributeType) type2, "C");
		IAttributeValue<?> value13 = new CategoricalAttributeValue((ICategoricalAttributeType) type3, "X");

		ArrayList<IAttributeValue<?>> values1 = new ArrayList<>();
		values1.add(value11);
		values1.add(value12);
		SimpleInstance i1 = new SimpleInstance(schema, values1, value13);

		// Instance 2
		IAttributeValue<?> value21 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "A");
		IAttributeValue<?> value22 = new CategoricalAttributeValue((ICategoricalAttributeType) type2, "C");
		IAttributeValue<?> value23 = new CategoricalAttributeValue((ICategoricalAttributeType) type3, "Y");

		ArrayList<IAttributeValue<?>> values2 = new ArrayList<>();
		values2.add(value21);
		values2.add(value22);
		SimpleInstance i2 = new SimpleInstance(schema, values2, value23);

		// Instance 3
		IAttributeValue<?> value31 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "B");
		IAttributeValue<?> value32 = new CategoricalAttributeValue((ICategoricalAttributeType) type2, "C");
		IAttributeValue<?> value33 = new CategoricalAttributeValue((ICategoricalAttributeType) type3, "X");

		ArrayList<IAttributeValue<?>> values3 = new ArrayList<>();
		values3.add(value31);
		values3.add(value32);
		SimpleInstance i3 = new SimpleInstance(schema, values3, value33);

		// Instance 4
		IAttributeValue<?> value41 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "A");
		IAttributeValue<?> value42 = new CategoricalAttributeValue((ICategoricalAttributeType) type2, "C");
		IAttributeValue<?> value43 = new CategoricalAttributeValue((ICategoricalAttributeType) type3, "Z");

		ArrayList<IAttributeValue<?>> values4 = new ArrayList<>();
		values4.add(value41);
		values4.add(value42);
		SimpleInstance i4 = new SimpleInstance(schema, values4, value43);

		simpleDataset.add(i1);
		simpleDataset.add(i2);
		simpleDataset.add(i3);
		simpleDataset.add(i4);

		return simpleDataset;
	}

	public SimpleDataset createToyDatasetMixed() {
		// Attribute 1 (categorical)
		String[] domain1 = { "A", "B" };
		IAttributeType<?> type1 = new CategoricalAttributeType(Arrays.asList(domain1));

		// Attribute 2 (numeric)
		IAttributeType<?> type2 = new NumericAttributeType();

		// Attribute 3 (numeric)
		IAttributeType<?> type3 = new NumericAttributeType();

		List<IAttributeType<?>> attributeTypeList = new ArrayList<>();
		attributeTypeList.add(type1);
		attributeTypeList.add(type2);

		InstanceSchema schema = new InstanceSchema(attributeTypeList, type3);

		SimpleDataset simpleDataset = new SimpleDataset(schema);

		// Instance 1
		IAttributeValue<?> value11 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "A");
		IAttributeValue<?> value12 = new NumericAttributeValue((NumericAttributeType) type2, 0.0);
		IAttributeValue<?> value13 = new NumericAttributeValue((NumericAttributeType) type2, 1.0);

		// Instance 2
		IAttributeValue<?> value21 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "B");
		IAttributeValue<?> value22 = new NumericAttributeValue((NumericAttributeType) type2, 3.0);
		IAttributeValue<?> value23 = new NumericAttributeValue((NumericAttributeType) type2, 20.0);

		// Instance 3
		IAttributeValue<?> value31 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "A");
		IAttributeValue<?> value32 = new NumericAttributeValue((NumericAttributeType) type2, 3.0);
		IAttributeValue<?> value33 = new NumericAttributeValue((NumericAttributeType) type2, -2.0);

		// Instance 4
		IAttributeValue<?> value41 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "B");
		IAttributeValue<?> value42 = new NumericAttributeValue((NumericAttributeType) type2, 2.0);
		IAttributeValue<?> value43 = new NumericAttributeValue((NumericAttributeType) type2, 1.5);

		// Instance 5
		IAttributeValue<?> value51 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "B");
		IAttributeValue<?> value52 = new NumericAttributeValue((NumericAttributeType) type2, 6.0);
		IAttributeValue<?> value53 = new NumericAttributeValue((NumericAttributeType) type2, 3.0);

		// Instance 6
		IAttributeValue<?> value61 = new CategoricalAttributeValue((ICategoricalAttributeType) type1, "A");
		IAttributeValue<?> value62 = new NumericAttributeValue((NumericAttributeType) type2, 10.0);
		IAttributeValue<?> value63 = new NumericAttributeValue((NumericAttributeType) type2, 5.0);

		ArrayList<IAttributeValue<?>> values1 = new ArrayList<>();
		values1.add(value11);
		values1.add(value12);
		SimpleInstance i1 = new SimpleInstance(schema, values1, value13);

		ArrayList<IAttributeValue<?>> values2 = new ArrayList<>();
		values2.add(value21);
		values2.add(value22);
		SimpleInstance i2 = new SimpleInstance(schema, values2, value23);

		ArrayList<IAttributeValue<?>> values3 = new ArrayList<>();
		values3.add(value31);
		values3.add(value32);
		SimpleInstance i3 = new SimpleInstance(schema, values3, value33);

		ArrayList<IAttributeValue<?>> values4 = new ArrayList<>();
		values4.add(value41);
		values4.add(value42);
		SimpleInstance i4 = new SimpleInstance(schema, values4, value43);

		ArrayList<IAttributeValue<?>> values5 = new ArrayList<>();
		values5.add(value51);
		values5.add(value52);
		SimpleInstance i5 = new SimpleInstance(schema, values5, value53);

		ArrayList<IAttributeValue<?>> values6 = new ArrayList<>();
		values6.add(value61);
		values6.add(value62);
		SimpleInstance i6 = new SimpleInstance(schema, values6, value63);

		simpleDataset.add(i1);
		simpleDataset.add(i2);
		simpleDataset.add(i3);
		simpleDataset.add(i4);
		simpleDataset.add(i5);
		simpleDataset.add(i6);

		return simpleDataset;
	}

}
