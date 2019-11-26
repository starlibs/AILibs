package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;
import ai.libs.jaicore.ml.core.util.TestDatasetGenerator;

public class AttributeBasedStratiAmountSelectorAndAssignerTester {

	@Test
	public void test_number_allAttributes_categorical_serial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(6, computedNumber);
	}

	@Test
	public void test_number_allAttributes_categorical_parallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(6, computedNumber);
	}

	@Test
	public void test_number_onlyTargetAttribute_categorical_serial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(3, computedNumber);
	}

	@Test
	public void test_number_onlyTargetAttribute_categorical_parallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(3, computedNumber);
	}

	@Test
	public void test_assignment_onlyTargetAttribute_categorical_serial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(1);
		selectorAndAssigner.setDataset(dataset);
		Map<ILabeledInstance, Integer> stratiAssignment = new HashMap<>();
		for (ILabeledInstance i : dataset) {
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
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices));
		selectorAndAssigner.setNumCPUs(4);
		selectorAndAssigner.setDataset(dataset);
		Map<ILabeledInstance, Integer> stratiAssignment = new HashMap<>();
		for (ILabeledInstance i : dataset) {
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
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_LENGTH, 2);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_number_allAttributes_mixed_equalLength_parallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_LENGTH, 2);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_number_allAttributes_mixed_equalSize_serial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_number_allAttributes_mixed_equalSize_parallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.selectStratiAmount(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void test_assignment_onlyTargetAttribute_mixed_serial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(1);
		selectorAndAssigner.setDataset(dataset);
		Map<ILabeledInstance, Integer> stratiAssignment = new HashMap<>();
		for (ILabeledInstance i : dataset) {
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
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setNumCPUs(4);
		selectorAndAssigner.setDataset(dataset);
		Map<ILabeledInstance, Integer> stratiAssignment = new HashMap<>();
		for (ILabeledInstance i : dataset) {
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

	public ILabeledDataset<ILabeledInstance> createToyDatasetOnlyCategorical() {
		// Features
		IAttribute type1 = new CategoricalAttribute("a1", Arrays.asList("A", "B"));
		IAttribute type2 = new CategoricalAttribute("a2", Arrays.asList("C"));
		// Label
		IAttribute type3 = new CategoricalAttribute("a3", Arrays.asList("X", "Y", "Z"));

		ILabeledInstanceSchema schema = new LabeledInstanceSchema("testData", Arrays.asList(type1, type2), type3);

		String[] features = { "A,C", "A,C", "B,C", "A,C" };
		String[] labels = { "X", "Y", "X", "Z" };
		return TestDatasetGenerator.generateLabeledDataset(schema, features, labels);
	}

	public ILabeledDataset<ILabeledInstance> createToyDatasetMixed() {
		// Attribute 1 (categorical)
		IAttribute type1 = new CategoricalAttribute("att0", Arrays.asList("A", "B"));
		IAttribute type2 = new NumericAttribute("att1");
		IAttribute type3 = new NumericAttribute("att2");
		ILabeledInstanceSchema schema = new LabeledInstanceSchema("mixedToyDataset", Arrays.asList(type1, type2), type3);

		String[] instances = { "A,0.0", "B,3.0", "A,3.0", "B,2.0", "B,6.0", "A,10.0" };
		String[] labels = { "1.0", "20.0", "-2.0", "1.5", "3.0", "5.0" };

		return TestDatasetGenerator.generateLabeledDataset(schema, instances, labels);
	}

}
