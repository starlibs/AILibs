package ai.libs.jaicore.ml.core.dataset.weka;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.api4.java.ai.ml.dataset.attribute.IAttributeType;
import org.api4.java.ai.ml.dataset.attribute.nominal.INominalAttributeType;
import org.api4.java.ai.ml.dataset.supervised.INumericFeatureSupervisedDataset;
import org.api4.java.ai.ml.dataset.supervised.INumericFeatureSupervisedInstance;

import ai.libs.jaicore.ml.core.dataset.attribute.nominal.NominalAttributeType;
import ai.libs.jaicore.ml.core.dataset.attribute.numeric.NumericAttributeType;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.UnsupportedAttributeTypeException;

public class WekaInstancesUtil {

	private WekaInstancesUtil() {
		/* Intentionally blank, hiding standard constructor for this util class. */
	}

	public static Instances datasetToWekaInstances(final INumericFeatureSupervisedDataset<Double, INumericFeatureSupervisedInstance<Double>> dataset) throws UnsupportedAttributeTypeException {
		List<Attribute> attributes = new LinkedList<>();

		for (int i = 0; i < dataset.getNumFeatures(); i++) {
			IAttributeType attType = dataset.getFeatureTypes().get(i);
			if (attType instanceof NumericAttributeType) {
				attributes.add(new Attribute("att" + i));
			} else if (attType instanceof NominalAttributeType) {
				attributes.add(new Attribute("att" + i, ((NominalAttributeType) attType).getValues()));
			} else {
				throw new UnsupportedAttributeTypeException("The class attribute has an unsupported attribute type.");
			}
		}

		List<IAttributeType> classTypeList = dataset.getLabelTypes();
		if (classTypeList.size() > 1) {
			throw new IllegalArgumentException("Cannot handle more than one label type");
		}

		IAttributeType classType = classTypeList.get(0);
		Attribute classAttribute;
		if (classType instanceof NumericAttributeType) {
			classAttribute = new Attribute("class");
		} else if (classType instanceof NominalAttributeType) {
			classAttribute = new Attribute("class", ((NominalAttributeType) classType).getValues());
		} else {
			throw new UnsupportedAttributeTypeException("The class attribute has an unsupported attribute type.");
		}

		ArrayList<Attribute> attributeList = new ArrayList<>(attributes);
		attributeList.add(classAttribute);

		Instances wekaInstances = new Instances("weka-instances", attributeList, 0);
		wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);

		for (INumericFeatureSupervisedInstance<Double> inst : dataset) {
			DenseInstance iNew = new DenseInstance(attributeList.size());
			iNew.setDataset(wekaInstances);

			for (int i = 0; i < dataset.getNumFeatures(); i++) {
				iNew.setValue(i, inst.get(i));
			}

			if (classType instanceof INominalAttributeType) {
				iNew.setClassValue(((INominalAttributeType) classType).decodeToString(inst.getLabel()));
			} else {
				iNew.setClassValue(inst.getLabel());
			}

			wekaInstances.add(iNew);
		}
		return wekaInstances;
	}

	public static IAttributeType transformWEKAAttributeToAttributeType(final Attribute att) {
		String attributeName = att.name();
		if (att.isNumeric()) {
			return new NumericAttributeType(attributeName);
		} else if (att.isNominal()) {
			List<String> domain = new LinkedList<>();
			for (int i = 0; i < att.numValues(); i++) {
				domain.add(att.value(i));
			}
			return new NominalAttributeType(attributeName, domain);
		}
		throw new IllegalArgumentException("Can only transform numeric or categorical attributes");
	}

}
