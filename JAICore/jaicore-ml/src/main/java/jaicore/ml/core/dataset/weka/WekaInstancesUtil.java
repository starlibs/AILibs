package jaicore.ml.core.dataset.weka;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jaicore.ml.core.dataset.ILabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.InstanceSchema;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.dataset.attribute.primitive.BooleanAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import jaicore.ml.core.dataset.standard.SimpleInstance;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.UnsupportedAttributeTypeException;

public class WekaInstancesUtil {

	private WekaInstancesUtil() {
		/* Intentionally blank, hiding standard constructor for this util class. */
	}

	public static SimpleDataset wekaInstancesToDataset(final Instances data) {
		List<IAttributeType<?>> attributeTypeList = new LinkedList<>();
		for (int i = 0; i < data.numAttributes(); i++) {
			if (i != data.classIndex()) {
				attributeTypeList.add(transformWEKAAttributeToAttributeType(data.attribute(i)));
			}
		}
		IAttributeType<?> targetType = transformWEKAAttributeToAttributeType(data.classAttribute());

		InstanceSchema schema = new InstanceSchema(attributeTypeList, targetType);
		SimpleDataset dataset = new SimpleDataset(schema);

		for (Instance inst : data) {
			ArrayList<IAttributeValue<?>> attributeValuesList = new ArrayList<>();
			String targetValue = null;

			int attIx = 0;
			for (int i = 0; i < inst.numAttributes(); i++) {
				if (i != data.classIndex()) {
					IAttributeType<?> type = schema.get(attIx);
					if (type instanceof NumericAttributeType) {
						attributeValuesList.add(new NumericAttributeValue((NumericAttributeType) type, inst.value(i)));
					} else {
						attributeValuesList.add(new CategoricalAttributeValue((CategoricalAttributeType) type, inst.attribute(i).value((int) inst.value(i))));
					}
					attIx++;
				} else {
					targetValue = inst.stringValue(i);
				}
			}

			SimpleInstance instance = new SimpleInstance(attributeValuesList, targetValue);
			dataset.add(instance);
		}

		return dataset;
	}

	public static Instances datasetToWekaInstances(final IOrderedLabeledAttributeArrayDataset<?, ?> dataset) throws UnsupportedAttributeTypeException {
		List<Attribute> attributes = new LinkedList<>();

		for (int i = 0; i < dataset.getNumberOfAttributes(); i++) {
			IAttributeType<?> attType = dataset.getAttributeTypes().get(i);
			if (attType instanceof NumericAttributeType) {
				attributes.add(new Attribute("att" + i));
			} else if (attType instanceof CategoricalAttributeType) {
				attributes.add(new Attribute("att" + i, ((CategoricalAttributeType) attType).getDomain()));
			} else {
				throw new UnsupportedAttributeTypeException("The class attribute has an unsupported attribute type.");
			}
		}

		IAttributeType<?> classType = dataset.getTargetType();
		Attribute classAttribute;
		if (classType instanceof NumericAttributeType) {
			classAttribute = new Attribute("class");
		} else if (classType instanceof CategoricalAttributeType) {
			classAttribute = new Attribute("class", ((CategoricalAttributeType) classType).getDomain());
		} else {
			throw new UnsupportedAttributeTypeException("The class attribute has an unsupported attribute type.");
		}

		ArrayList<Attribute> attributeList = new ArrayList<>(attributes);
		attributeList.add(classAttribute);

		Instances wekaInstances = new Instances("weka-instances", attributeList, 0);
		wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);

		for (ILabeledAttributeArrayInstance inst : dataset) {
			DenseInstance iNew = new DenseInstance(attributeList.size());
			iNew.setDataset(wekaInstances);

			for (int i = 0; i < dataset.getNumberOfAttributes(); i++) {
				if (dataset.getAttributeTypes().get(i) instanceof NumericAttributeType) {
					IAttributeValue<Double> val = inst.getAttributeValueAtPosition(i, Double.class);
					iNew.setValue(i, val.getValue());
				} else if (dataset.getAttributeTypes().get(i) instanceof CategoricalAttributeType) {
					IAttributeValue<String> val = inst.getAttributeValueAtPosition(i, String.class);
					iNew.setValue(i, val.getValue());
				}
			}

			wekaInstances.add(iNew);
		}
		return wekaInstances;
	}

	public static IAttributeType<?> transformWEKAAttributeToAttributeType(final Attribute att) {
		if (att.isNumeric()) {
			return new NumericAttributeType();
		} else if (att.isNominal()) {
			List<String> domain = new LinkedList<>();
			for (int i = 0; i < att.numValues(); i++) {
				domain.add(att.value(i));
			}
			return att.numValues() == 2 ? new BooleanAttributeType() : new CategoricalAttributeType(domain);
		}
		throw new IllegalArgumentException("Can only transform numeric or categorical attributes");
	}

}
