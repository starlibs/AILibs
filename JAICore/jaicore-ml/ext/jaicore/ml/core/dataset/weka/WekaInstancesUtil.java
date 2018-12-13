package jaicore.ml.core.dataset.weka;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.InstanceSchema;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import jaicore.ml.core.dataset.standard.SimpleInstance;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class WekaInstancesUtil {

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
			IAttributeValue<?> targetValue = null;

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
					IAttributeType<?> type = schema.getTargetType();
					if (type instanceof NumericAttributeType) {
						targetValue = new NumericAttributeValue((NumericAttributeType) type, inst.value(i));
					} else if (type instanceof CategoricalAttributeType) {
						targetValue = new CategoricalAttributeValue((CategoricalAttributeType) type, inst.classAttribute().value((int) inst.value(i)));
					}
				}
			}

			SimpleInstance instance = new SimpleInstance(attributeValuesList, targetValue);
			dataset.add(instance);
		}

		return dataset;
	}

	public static IAttributeType<?> transformWEKAAttributeToAttributeType(final Attribute att) {
		if (att.isNumeric()) {
			return new NumericAttributeType();
		} else if (att.isNominal()) {
			List<String> domain = new LinkedList<>();
			for (int i = 0; i < att.numValues(); i++) {
				domain.add(att.value(i));
			}
			return new CategoricalAttributeType(domain);
		}
		throw new IllegalArgumentException("Can only transform numeric or categorical attributes");
	}

	public static void main(final String[] args) throws FileNotFoundException, IOException, ContainsNonNumericAttributesException {
		Instances data = new Instances(new FileReader(new File("../../../datasets/classification/multi-class/car.arff")));
		data.setClassIndex(data.numAttributes() - 1);
		System.out.println("Read in weka instances.");
		long timestampStart = System.currentTimeMillis();
		SimpleDataset simpleDataset = WekaInstancesUtil.wekaInstancesToDataset(data);
		long timestampStop = System.currentTimeMillis();
		System.out.println(simpleDataset);
		System.out.println("Transformation took " + (timestampStop - timestampStart) + "ms");

		System.out.println(simpleDataset.printDoubleRepresentation());

	}
}
