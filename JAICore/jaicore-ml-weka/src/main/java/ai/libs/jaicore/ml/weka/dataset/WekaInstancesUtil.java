package ai.libs.jaicore.ml.weka.dataset;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaInstancesUtil {

	private WekaInstancesUtil() {
		/* Intentionally blank, hiding standard constructor for this util class. */
	}

	public static ILabeledInstanceSchema extractSchema(final Instances dataset) {
		int targetIndex = dataset.classIndex();
		if (targetIndex < 0) {
			throw new IllegalArgumentException("Class index of Instances object is not set!");
		}
		List<IAttribute> attributes = IntStream.range(0, dataset.numAttributes()).mapToObj(x -> dataset.attribute(x)).map(WekaInstancesUtil::transformWEKAAttributeToAttributeType).collect(Collectors.toList());
		IAttribute labelAttribute = attributes.remove(targetIndex);
		return new LabeledInstanceSchema(dataset.relationName(), attributes, labelAttribute);
	}

	public static Instances datasetToWekaInstances(final ILabeledDataset<? extends ILabeledInstance> dataset) throws UnsupportedAttributeTypeException {
		Instances wekaInstances = createDatasetFromSchema(dataset.getInstanceSchema());
		for (ILabeledInstance inst : dataset) {
			DenseInstance iNew = new DenseInstance(wekaInstances.numAttributes());
			iNew.setDataset(wekaInstances);
			for (int i = 0; i < dataset.getNumAttributes(); i++) {
				iNew.setValue(i, (Double) inst.getAttributeValue(i));
			}

			if (dataset.getLabelAttribute() instanceof ICategoricalAttribute) {
				iNew.setClassValue(((ICategoricalAttribute) dataset.getLabelAttribute()).serializeAttributeValue(inst.getLabel()));
			} else {
				iNew.setClassValue((Double) inst.getLabel());
			}
			wekaInstances.add(iNew);
		}
		return wekaInstances;
	}

	public static Instances createDatasetFromSchema(final ILabeledInstanceSchema schema) throws UnsupportedAttributeTypeException {
		List<Attribute> attributes = new LinkedList<>();

		for (int i = 0; i < schema.getNumAttributes(); i++) {
			IAttribute attType = schema.getAttributeList().get(i);
			if (attType instanceof NumericAttribute) {
				attributes.add(new Attribute("att" + i));
			} else if (attType instanceof CategoricalAttribute) {
				attributes.add(new Attribute("att" + i, ((CategoricalAttribute) attType).getValues()));
			} else {
				throw new UnsupportedAttributeTypeException("The class attribute has an unsupported attribute type.");
			}
		}

		IAttribute classType = schema.getLabelAttribute();
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

	public static Instance transformInstanceToWekaInstance(final ILabeledInstanceSchema schema, final ILabeledInstance instance) throws UnsupportedAttributeTypeException {
		Instances dataset = createDatasetFromSchema(schema);
		Instance iNew = new DenseInstance(dataset.numAttributes());
		iNew.setDataset(dataset);
		for (int i = 0; i < instance.getNumAttributes(); i++) {
			if (schema.getAttribute(i) instanceof INumericAttribute) {
				iNew.setValue(i, ((INumericAttribute) schema.getAttribute(i)).getAsAttributeValue(instance.getAttributeValue(i)).getValue());
			} else if (schema.getAttribute(i) instanceof ICategoricalAttribute) {
				iNew.setValue(i, ((ICategoricalAttribute) schema.getAttribute(i)).getAsAttributeValue(instance.getAttributeValue(i)).getValue());
			} else {
				throw new UnsupportedAttributeTypeException("Only categorical and numeric attributes are supported!");
			}
		}

		if (schema.getLabelAttribute() instanceof INumericAttribute) {
			iNew.setValue(iNew.numAttributes() - 1, ((INumericAttribute) schema.getLabelAttribute()).getAsAttributeValue(instance.getLabel()).getValue());
		} else if (schema.getLabelAttribute() instanceof ICategoricalAttribute) {
			iNew.setValue(iNew.numAttributes() - 1, ((ICategoricalAttribute) schema.getLabelAttribute()).getAsAttributeValue(instance.getLabel()).getValue());
		} else {
			throw new UnsupportedAttributeTypeException("Only categorical and numeric attributes are supported!");
		}
		return iNew;
	}

}
