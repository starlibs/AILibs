package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.IntBasedCategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.AttributeBasedStratifier;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;
import ai.libs.jaicore.ml.core.util.TestDatasetGenerator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class AttributeBasedStratiAmountSelectorAndAssignerTester extends ATest {

	@Test
	public void testNumberAllAttributesCategoricalSerial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices));
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.createStrati(dataset);
		assertEquals(6, computedNumber);
	}

	@Test
	public void testNumberAllAttributesCategoricalParallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices));
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.createStrati(dataset);
		assertEquals(6, computedNumber);
	}

	@Test
	public void testNumberOnlyTargetAttributeCategoricalSerial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices));
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.createStrati(dataset);
		assertEquals(3, computedNumber);
	}

	@Test
	public void testNumberOnlyTargetAttributeCategoricalParallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices));
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.createStrati(dataset);
		assertEquals(3, computedNumber);
	}

	@Test
	public void testAssignmentOnlyTargetAttributeCategoricalSerial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices));
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(1);
		selectorAndAssigner.createStrati(dataset);
		IntList stratiAssignment = new IntArrayList();
		for (ILabeledInstance i : dataset) {
			stratiAssignment.add(selectorAndAssigner.getStratum(i));
		}
		// Number of strati must be 3
		assertEquals(3, new HashSet<>(stratiAssignment).size());
		assertEquals("Instances 1 and 3 need to be in the same stratum", stratiAssignment.getInt(0), stratiAssignment.getInt(2));
		assertNotEquals("Instances 1 and 2 need to be in the different strati.",stratiAssignment.getInt(0), stratiAssignment.getInt(1));
		assertNotEquals("Instances 1 and 4 need to be in the different strati", stratiAssignment.getInt(0), stratiAssignment.getInt(3));
		assertNotEquals("Instances 2 and 3 need to be in the different strati", stratiAssignment.getInt(1), stratiAssignment.getInt(2));
		assertNotEquals("Instances 2 and 4 need to be in the different strati", stratiAssignment.getInt(1), stratiAssignment.getInt(3));
		assertNotEquals("Instances 3 and 4 need to be in the different strati", stratiAssignment.getInt(2), stratiAssignment.getInt(3));
	}

	@Test
	public void testAssignmentOnlyTargetAttributeCategoricalParallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetOnlyCategorical();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices));
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(4);
		selectorAndAssigner.createStrati(dataset);
		IntList stratiAssignment = new IntArrayList();
		for (ILabeledInstance i : dataset) {
			stratiAssignment.add(selectorAndAssigner.getStratum(i));
		}
		// Number of strati must be 3
		assertEquals(3, new HashSet<>(stratiAssignment).size());
		assertEquals("Instances 1 and 3 need to be in the same stratum", stratiAssignment.getInt(0), stratiAssignment.getInt(2));
		assertNotEquals("Instances 1 and 2 need to be in the different strati", stratiAssignment.getInt(0), stratiAssignment.getInt(1));
		assertNotEquals("Instances 1 and 4 need to be in the different strati", stratiAssignment.getInt(0), stratiAssignment.getInt(3));
		assertNotEquals("Instances 2 and 3 need to be in the different strati", stratiAssignment.getInt(1), stratiAssignment.getInt(2));
		assertNotEquals("Instances 2 and 4 need to be in the different strati", stratiAssignment.getInt(1), stratiAssignment.getInt(3));
		assertNotEquals("Instances 3 and 4 need to be in the different strati", stratiAssignment.getInt(2), stratiAssignment.getInt(3));
	}

	@Test
	public void testNumberAllAttributesMixedEqualLengthSerial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_LENGTH, 2);
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.createStrati(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void testNumberAllAttributesMixedEqualLengthParallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_LENGTH, 2);
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.createStrati(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void testNumberAllAttributesMixedEqualSizeSerial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(1);
		int computedNumber = selectorAndAssigner.createStrati(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void testNumberAllAttributesMixedEqualSizeParallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 0, 1, 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(4);
		int computedNumber = selectorAndAssigner.createStrati(dataset);
		assertEquals(8, computedNumber);
	}

	@Test
	public void testAssignmentOnlyTargetAttributeMixedSerial() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(1);
		selectorAndAssigner.createStrati(dataset);
		IntList stratiAssignment = new IntArrayList();
		for (ILabeledInstance i : dataset) {
			stratiAssignment.add(selectorAndAssigner.getStratum(i));
		}
		// Number of strati must be 2
		assertEquals(2, new HashSet<>(stratiAssignment).size());
		assertEquals("Instances 1 and 3 need to be in the same stratum", stratiAssignment.getInt(0), stratiAssignment.getInt(2));
		assertEquals("Instances 1 and 4 need to be in the same stratum", stratiAssignment.getInt(0), stratiAssignment.getInt(3));
		assertNotEquals("Instances 1 and 2 need to be in the different strati", stratiAssignment.getInt(0), stratiAssignment.getInt(1));
		assertNotEquals("Instances 1 and 5 need to be in the different strati", stratiAssignment.getInt(0), stratiAssignment.getInt(4));
		assertNotEquals("Instances 1 and 6 need to be in the different strati", stratiAssignment.getInt(0), stratiAssignment.getInt(5));
	}

	@Test
	public void testAssignmentOnlyTargetAttributeMixedParallel() {
		ILabeledDataset<ILabeledInstance> dataset = this.createToyDatasetMixed();
		Integer[] attributeIndices = { 2 };
		AttributeBasedStratifier selectorAndAssigner = new AttributeBasedStratifier(Arrays.asList(attributeIndices),
				DiscretizationStrategy.EQUAL_SIZE, 2);
		selectorAndAssigner.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		selectorAndAssigner.setNumCPUs(4);
		selectorAndAssigner.createStrati(dataset);
		IntList stratiAssignment = new IntArrayList();
		for (ILabeledInstance i : dataset) {
			stratiAssignment.add(selectorAndAssigner.getStratum(i));
		}
		// Number of strati must be 2
		assertEquals(2, new HashSet<>(stratiAssignment).size());
		assertEquals("Instances 1 and 3 need to be in the same stratum", stratiAssignment.getInt(0), stratiAssignment.getInt(2));
		assertEquals("Instances 1 and 4 need to be in the same stratum", stratiAssignment.getInt(0), stratiAssignment.getInt(3));
		assertNotEquals("Instances 1 and 2 need to be in the different strati", stratiAssignment.getInt(0), stratiAssignment.getInt(1));
		assertNotEquals("Instances 1 and 5 need to be in the different strati", stratiAssignment.getInt(0), stratiAssignment.getInt(4));
		assertNotEquals("Instances 1 and 6 need to be in the different strati", stratiAssignment.getInt(0), stratiAssignment.getInt(5));
	}

	public ILabeledDataset<ILabeledInstance> createToyDatasetOnlyCategorical() {
		// Features
		IAttribute type1 = new IntBasedCategoricalAttribute("a1", Arrays.asList("A", "B"));
		IAttribute type2 = new IntBasedCategoricalAttribute("a2", Arrays.asList("C"));
		// Label
		IAttribute type3 = new IntBasedCategoricalAttribute("a3", Arrays.asList("X", "Y", "Z"));

		ILabeledInstanceSchema schema = new LabeledInstanceSchema("testData", Arrays.asList(type1, type2), type3);

		String[] features = { "A,C", "A,C", "B,C", "A,C" };
		String[] labels = { "X", "Y", "X", "Z" };
		return TestDatasetGenerator.generateLabeledDataset(schema, features, labels);
	}

	public ILabeledDataset<ILabeledInstance> createToyDatasetMixed() {
		// Attribute 1 (categorical)
		IAttribute type1 = new IntBasedCategoricalAttribute("att0", Arrays.asList("A", "B"));
		IAttribute type2 = new NumericAttribute("att1");
		IAttribute type3 = new NumericAttribute("att2");
		ILabeledInstanceSchema schema = new LabeledInstanceSchema("mixedToyDataset", Arrays.asList(type1, type2), type3);

		String[] instances = { "A,0.0", "B,3.0", "A,3.0", "B,2.0", "B,6.0", "A,10.0" };
		String[] labels = { "1.0", "20.0", "-2.0", "1.5", "3.0", "5.0" };

		return TestDatasetGenerator.generateLabeledDataset(schema, instances, labels);
	}

}
