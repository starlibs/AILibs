package ai.libs.jaicore.ml.weka.dataset;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.dataset.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.attribute.NumericAttribute;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.UnsupportedAttributeTypeException;

public class WekaInstancesUtil {

	private WekaInstancesUtil() {
		/* Intentionally blank, hiding standard constructor for this util class. */
	}

	public static Instances datasetToWekaInstances(final ILabeledDataset<ILabeledInstance> dataset) throws UnsupportedAttributeTypeException {
		List<Attribute> attributes = new LinkedList<>();

		for (int i = 0; i < dataset.getNumAttributes(); i++) {
			IAttribute attType = dataset.getListOfAttributes().get(i);
			if (attType instanceof NumericAttribute) {
				attributes.add(new Attribute("att" + i));
			} else if (attType instanceof CategoricalAttribute) {
				attributes.add(new Attribute("att" + i, ((CategoricalAttribute) attType).getValues()));
			} else {
				throw new UnsupportedAttributeTypeException("The class attribute has an unsupported attribute type.");
			}
		}

		IAttribute classType = dataset.getLabelAttribute();
		Attribute classAttribute;

		if (classType instanceof INumericAttribute) {
			classAttribute = new Attribute("class");
		} else if (classType instanceof ICategoricalAttribute) {
			classAttribute = new Attribute("class", ((CategoricalAttribute) classType).getValues());
		} else {
			throw new UnsupportedAttributeTypeException("The class attribute has an unsupported attribute type.");
		}

		ArrayList<Attribute> attributeList = new ArrayList<>(attributes);
		attributeList.add(classAttribute);

		Instances wekaInstances = new Instances("weka-instances", attributeList, 0);
		wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);

		for (ILabeledInstance inst : dataset) {
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
