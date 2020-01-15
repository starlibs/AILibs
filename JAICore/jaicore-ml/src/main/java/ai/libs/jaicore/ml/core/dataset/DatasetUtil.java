package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.reconstruction.IReconstructible;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.IntBasedCategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;

public class DatasetUtil {

	private DatasetUtil() {
		/* just to avoid instantiation */
	}

	public static Map<Object, Integer> getLabelCounts(final ILabeledDataset<?> ds) {
		Map<Object, Integer> labelCounter = new HashMap<>();
		ds.forEach(li -> {
			Object label = li.getLabel();
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

		/* add reconstruction instructions to the dataset */
		if (dataset instanceof IReconstructible) {
			((IReconstructible) dataset).getConstructionPlan().getInstructions().forEach(datasetModified::addInstruction);
			try {
				datasetModified.addInstruction(new ReconstructionInstruction(DatasetUtil.class.getMethod("convertToClassificationDataset", ILabeledDataset.class), "this"));
			} catch (NoSuchMethodException | SecurityException e) {
				throw new UnsupportedOperationException(e);
			}
		}
		return datasetModified;
	}

	public static ILabeledDataset<?> getDatasetFromMapCollection(final Collection<Map<String, Object>> datasetAsListOfMaps, final String nameOfLabelAttribute) {
		List<String> keyOrder = datasetAsListOfMaps.iterator().next().keySet().stream().sorted().collect(Collectors.toList());
		return getDatasetFromMapCollection(datasetAsListOfMaps, nameOfLabelAttribute, keyOrder);
	}

	public static ILabeledDataset<?> getDatasetFromMapCollection(final Collection<Map<String, Object>> datasetAsListOfMaps, final String nameOfLabelAttribute, final List<String> orderOfAttributes) {

		/* check that all keys are identical */
		Set<String> keys = new HashSet<>(orderOfAttributes);
		for (Map<String, Object> dataPoint : datasetAsListOfMaps) {
			if (!keys.equals(dataPoint.keySet())) {
				throw new IllegalStateException();
			}
		}

		List<IAttribute> attributeList = new ArrayList<>();
		for (String key : orderOfAttributes) {
			if (key.equals(nameOfLabelAttribute)) {
				continue;
			}
			Object val = datasetAsListOfMaps.iterator().next().get(key);
			if (val instanceof Number) {
				attributeList.add(new NumericAttribute(key));
			}
			else if (val instanceof Boolean) {
				attributeList.add(new IntBasedCategoricalAttribute(key, Arrays.asList("false", "true")));
			}
			else {
				throw new UnsupportedOperationException();
			}
		}
		LabeledInstanceSchema schema = new LabeledInstanceSchema("rel", attributeList, new NumericAttribute(nameOfLabelAttribute));
		Dataset metaDataset = new Dataset(schema);
		for (Map<String, Object> row : datasetAsListOfMaps) {
			ILabeledInstance inst = getInstanceFromMap(schema, row, nameOfLabelAttribute);
			metaDataset.add(inst);
		}
		return metaDataset;
	}

	public static ILabeledInstance getInstanceFromMap(final ILabeledInstanceSchema schema, final Map<String, Object> row, final String nameOfLabelAttribute) {
		return getInstanceFromMap(schema, row, nameOfLabelAttribute, new HashMap<>());
	}

	public static ILabeledInstance getInstanceFromMap(final ILabeledInstanceSchema schema, final Map<String, Object> row, final String nameOfLabelAttribute, final Map<IAttribute, Function<ILabeledInstance, Double>> attributeValueComputer) {
		List<Object> attributes = new ArrayList<>(schema.getNumAttributes());
		List<Integer> attributeToRecover = new ArrayList<>();
		int i = 0;
		for (IAttribute att : schema.getAttributeList()) {
			if (row.containsKey(att.getName())) {
				attributes.add(row.get(att.getName()));
			}
			else {
				attributeToRecover.add(i);
				attributes.add(null);
			}
			i++;
		}
		ILabeledInstance inst = new DenseInstance(attributes, row.get(nameOfLabelAttribute));
		if (inst.getNumAttributes() != schema.getNumAttributes()) {
			throw new IllegalStateException("Created dense instance with " + inst.getNumAttributes() + " attributes where the scheme requires " + schema.getNumAttributes());
		}

		/* post-compute the missing attribute values */
		for (int attIndex : attributeToRecover) {
			inst.setAttributeValue(attIndex, attributeValueComputer.get(schema.getAttribute(attIndex)).apply(inst));
		}
		return inst;
	}

	public static final int EXPANSION_SQUARES = 1;
	public static final int EXPANSION_LOGARITHM = 2;
	public static final int EXPANSION_PRODUCTS = 3;

	public static Pair<List<IAttribute>, Map<IAttribute, Function<ILabeledInstance, Double>>> getPairOfNewAttributesAndExpansionMap(final ILabeledDataset<?> dataset, final int...expansions) throws InterruptedException {
		List<IAttribute> attributeList = dataset.getInstanceSchema().getAttributeList();
		List<IAttribute> newAttributes = new ArrayList<>();
		boolean computeSquares = false;
		boolean computeProducts = false;
		boolean computeLogs = false;
		for (int expansion : expansions) {
			switch (expansion) {
			case EXPANSION_LOGARITHM:
				computeLogs = true;
				break;
			case EXPANSION_SQUARES:
				computeSquares = true;
				break;
			case EXPANSION_PRODUCTS:
				computeProducts = true;
				break;
			default:
				throw new UnsupportedOperationException("Unknown expansion " + expansion);
			}
		}

		/* compute new attribute objects */
		Map<IAttribute, Function<ILabeledInstance, Double>> transformations = new HashMap<>();
		for (int attId = 0; attId < dataset.getNumAttributes(); attId ++) {
			final int attIdFinal = attId;
			IAttribute att = dataset.getAttribute(attId);
			if (computeSquares && (att instanceof INumericAttribute)) {
				IAttribute dAtt = new NumericAttribute(att.getName() + "_2");
				newAttributes.add(dAtt);
				transformations.put(dAtt, i -> Math.pow(Double.parseDouble(i.getAttributeValue(attIdFinal).toString()), 2));
			}
			else if (computeLogs && (att instanceof INumericAttribute)) {
				IAttribute dAtt = new NumericAttribute(att.getName() + "_log");
				newAttributes.add(dAtt);
				transformations.put(dAtt, i -> Math.log((double)i.getAttributeValue(attIdFinal)));
			}
		}

		/* compute products */
		if (computeProducts) {

			/* compute all sub-sets of features */
			Collection<Collection<IAttribute>> featureSubSets = SetUtil.powerset(attributeList);
			for (Collection<IAttribute> subset : featureSubSets) {
				if (subset.size() > 3 || subset.size() < 2) {
					continue;
				}
				StringBuilder featureName = new StringBuilder("x");
				final List<Integer> indices = new ArrayList<>();
				for (IAttribute feature : subset.stream().sorted((a1,a2) -> a1.getName().compareTo(a2.getName())).collect(Collectors.toList())) {
					featureName.append("_" + feature.getName());
					indices.add(attributeList.indexOf(feature));
				}
				IAttribute dAtt = new NumericAttribute(featureName.toString());
				if (attributeList.contains(dAtt)) {
					throw new IllegalStateException("Dataset already has attribute " + dAtt.getName());
				}
				else if (newAttributes.contains(dAtt)) {
					throw new IllegalStateException("Already added attribute " + dAtt.getName());
				}
				newAttributes.add(dAtt);
				transformations.put(dAtt, i -> {
					double val = 1;
					for (int index : indices) {
						val *= Double.parseDouble(i.getAttributeValue(index).toString());
					}
					return val;
				});
			}
		}
		return new Pair<>(newAttributes, transformations);
	}

	public static ILabeledDataset<?> getExpansionOfDataset(final ILabeledDataset<?> dataset, final int... expansions) throws InterruptedException {
		return getExpansionOfDataset(dataset, getPairOfNewAttributesAndExpansionMap(dataset, expansions));
	}

	public static ILabeledDataset<?> getExpansionOfDataset(final ILabeledDataset<?> dataset, final Pair<List<IAttribute>, Map<IAttribute, Function<ILabeledInstance, Double>>> expansionDescription) {

		/* compute values for new attributes */
		List<IAttribute> newAttributeList = new ArrayList<>(dataset.getInstanceSchema().getAttributeList());
		newAttributeList.addAll(expansionDescription.getX());
		ILabeledInstanceSchema schema = new LabeledInstanceSchema(dataset.getRelationName() + "_expansion", newAttributeList, dataset.getLabelAttribute());
		Dataset ds = new Dataset(schema);
		for (ILabeledInstance i : dataset) {
			ds.add(getExpansionOfInstance(i, expansionDescription));
		}
		return ds;
	}

	public static ILabeledInstance getExpansionOfInstance(final ILabeledInstance i, final Pair<List<IAttribute>, Map<IAttribute, Function<ILabeledInstance, Double>>> expansionDescription)  {

		List<Object> attributes = new ArrayList<>(Arrays.asList(i.getAttributes()));
		for (IAttribute newAtt : expansionDescription.getX()) {
			attributes.add(expansionDescription.getY().get(newAtt).apply(i));
		}
		return new DenseInstance(attributes, i.getLabel());
	}
}
