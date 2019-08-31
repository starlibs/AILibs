package ai.libs.jaicore.ml.weka.dataset;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.INumericFeatureSupervisedDataset;
import org.api4.java.ai.ml.core.dataset.supervised.INumericFeatureSupervisedInstance;

import ai.libs.jaicore.ml.core.tabular.dataset.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.tabular.dataset.attribute.NumericAttribute;
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

		for (int i = 0; i < dataset.getNumAttributes(); i++) {
			IAttribute attType = dataset.getFeatureTypes().getAttributeValue(i);
			if (attType instanceof NumericAttribute) {
				attributes.add(new Attribute("att" + i));
			} else if (attType instanceof CategoricalAttribute) {
				attributes.add(new Attribute("att" + i, ((CategoricalAttribute) attType).getValues()));
			} else {
				throw new UnsupportedAttributeTypeException("The class attribute has an unsupported attribute type.");
			}
		}

		List<IAttribute> classTypeList = dataset.getLabelTypes();
		if (classTypeList.size() > 1) {
			throw new IllegalArgumentException("Cannot handle more than one label type");
		}

		IAttribute classType = classTypeList.get(0);
		Attribute classAttribute;
		if (classType instanceof NumericAttribute) {
			classAttribute = new Attribute("class");
		} else if (classType instanceof CategoricalAttribute) {
			classAttribute = new Attribute("class", ((CategoricalAttribute) classType).getValues());
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

			for (int i = 0; i < dataset.getNumAttributes(); i++) {
				iNew.setValue(i, inst.getAttributeValue(i));
			}

			if (classType instanceof ICategoricalAttribute) {
				iNew.setClassValue(((ICategoricalAttribute) classType).decodeToString(inst.getLabel()));
			} else {
				iNew.setClassValue(inst.getLabel());
			}

			wekaInstances.add(iNew);
		}
		return wekaInstances;
	}

	public static IAttribute transformWEKAAttributeToAttributeType(final Attribute att) {
		String attributeName = att.name();
		if (att.isNumeric()) {
			return new NumericAttribute(attributeName);
		} else if (att.isNominal()) {
			List<String> domain = new LinkedList<>();
			for (int i = 0; i < att.numValues(); i++) {
				domain.add(att.value(i));
			}
			return new CategoricalAttribute(attributeName, domain);
		}
		throw new IllegalArgumentException("Can only transform numeric or categorical attributes");
	}

}
