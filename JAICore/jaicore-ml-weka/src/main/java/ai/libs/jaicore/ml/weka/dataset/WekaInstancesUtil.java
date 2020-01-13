package ai.libs.jaicore.ml.weka.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
import ai.libs.jaicore.ml.core.dataset.schema.attribute.IntBasedCategoricalAttribute;
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
		List<IAttribute> attributes = IntStream.range(0, dataset.numAttributes()).mapToObj(dataset::attribute).map(WekaInstancesUtil::transformWEKAAttributeToAttributeType).collect(Collectors.toList());
		IAttribute labelAttribute = attributes.remove(targetIndex);
		return new LabeledInstanceSchema(dataset.relationName(), attributes, labelAttribute);
	}

	public static Instances datasetToWekaInstances(final ILabeledDataset<? extends ILabeledInstance> dataset) throws UnsupportedAttributeTypeException {
		Instances wekaInstances = createDatasetFromSchema(dataset.getInstanceSchema());
		int expectedAttributes = dataset.getInstanceSchema().getNumAttributes();
		for (ILabeledInstance inst : dataset) {
			if (inst.getNumAttributes() != expectedAttributes) {
				throw new IllegalStateException("Dataset scheme defines a number of " + expectedAttributes + " attributes, but instance has " + inst.getNumAttributes() + ". Attributes in scheme: " + dataset.getInstanceSchema().getAttributeList().stream().map(a -> "\n\t" + a.getName() + " (" + a.toString() + ")").collect(Collectors.joining()) + ". Attributes in instance: " + Arrays.stream(inst.getAttributes()).map(a -> "\n\t" + a.toString()).collect(Collectors.joining()));
			}
			double[] point = inst.getPoint();
			double[] pointWithLabel = Arrays.copyOf(point, point.length + 1);
			DenseInstance iNew = new DenseInstance(1, pointWithLabel);
			iNew.setDataset(wekaInstances);
			if (dataset.getLabelAttribute() instanceof ICategoricalAttribute) {
				iNew.setClassValue(((ICategoricalAttribute) dataset.getLabelAttribute()).getLabelOfCategory((int)inst.getLabel()));
			} else {
				iNew.setClassValue(Double.parseDouble(inst.getLabel().toString()));
			}
			wekaInstances.add(iNew); // this MUST come here AFTER having set the class value; otherwise, the class is not registered correctly in the Instances object!!
		}
		return wekaInstances;
	}

	public static Instances createDatasetFromSchema(final ILabeledInstanceSchema schema) throws UnsupportedAttributeTypeException {
		Objects.requireNonNull(schema);
		List<Attribute> attributes = new LinkedList<>();

		for (int i = 0; i < schema.getNumAttributes(); i++) {
			IAttribute attType = schema.getAttributeList().get(i);
			if (attType instanceof NumericAttribute) {
				attributes.add(new Attribute(attType.getName()));
			} else if (attType instanceof IntBasedCategoricalAttribute) {
				attributes.add(new Attribute(attType.getName(), ((IntBasedCategoricalAttribute) attType).getLabels()));
			} else {
				throw new UnsupportedAttributeTypeException("The class attribute has an unsupported attribute type " + attType.getName() + ".");
			}
		}

		IAttribute classType = schema.getLabelAttribute();
		Attribute classAttribute;

		if (classType instanceof INumericAttribute) {
			classAttribute = new Attribute(classType.getName());
		} else if (classType instanceof ICategoricalAttribute) {
			classAttribute = new Attribute(classType.getName(), ((IntBasedCategoricalAttribute) classType).getLabels());
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
			return new IntBasedCategoricalAttribute(attributeName, domain);
		}
		throw new IllegalArgumentException("Can only transform numeric or categorical attributes");
	}

	public static Instance transformInstanceToWekaInstance(final ILabeledInstanceSchema schema, final ILabeledInstance instance) throws UnsupportedAttributeTypeException {
		if (instance.getNumAttributes() != schema.getNumAttributes()) {
			throw new IllegalArgumentException("Schema and instance do not coincide. The schema defines " + schema.getNumAttributes() + " attributes but the instance has " + instance.getNumAttributes() + " attributes.");
		}
		if (instance instanceof WekaInstance) {
			return ((WekaInstance) instance).getElement();
		}
		Objects.requireNonNull(schema);
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
		if (iNew.numClasses() != dataset.numClasses()) {
			throw new IllegalStateException();
		}
		return iNew;
	}

}
