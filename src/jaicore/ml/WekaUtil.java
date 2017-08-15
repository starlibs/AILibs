package jaicore.ml;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jaicore.ml.core.SimpleInstanceImpl;
import jaicore.ml.core.SimpleInstancesImpl;
import jaicore.ml.core.SimpleLabeledInstanceImpl;
import jaicore.ml.core.WekaCompatibleInstancesImpl;
import jaicore.ml.interfaces.LabeledInstance;
import weka.attributeSelection.AttributeSelection;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.json.JSONInstances;
import weka.core.json.JSONNode;

public class WekaUtil {

	public static <L> Instances fromJAICoreInstances(final WekaCompatibleInstancesImpl instances) {

		/* create basic attribute entries */
		ArrayList<Attribute> attributes = new ArrayList<>();
		int numAttributes = instances.getNumberOfColumns();
		for (int i = 1; i <= numAttributes; i++) {
			attributes.add(new Attribute("a" + i));
		}

		/*
		 * if the instances object is labeled, create the label entry and create a list of all the possible labels
		 */
		Map<Object, Double> labelMap = new HashMap<>();
		int c = 0;
		boolean isNominal = false;
		for (Object o : instances.getDeclaredClasses()) {
			labelMap.put(o, (double) (c++));
			if (!Double.class.isInstance(o)) {
				isNominal = true;
			}
		}

		/* if the feature is */
		if (isNominal) {
			attributes.add(new Attribute("label", instances.getDeclaredClasses()));
		} else {
			attributes.add(new Attribute("label"));
		}
		
		/* create instances object and insert the data points */
		Instances wekaInstances = new Instances("JAICore-extracted dataset", attributes, instances.size());
		wekaInstances.setClassIndex(numAttributes);
		Attribute classAttribute = wekaInstances.classAttribute();
		for (jaicore.ml.interfaces.Instance instance : instances) {
			Instance wekaInstance = new DenseInstance(numAttributes + 1);
			wekaInstance.setDataset(wekaInstances);
			int att = 0;
			for (Double val : instance) {
				wekaInstance.setValue(att++, val);
			}
			wekaInstance.setClassValue(labelMap.get(((LabeledInstance<?>) instance).getLabel()));
			wekaInstances.add(wekaInstance);
		}
		return wekaInstances;
	}

	public static Instances fromJAICoreInstances(final jaicore.ml.interfaces.Instances instances) {

		/* create basic attribute entries */
		ArrayList<Attribute> attributes = new ArrayList<>();
		int numAttributes = instances.getNumberOfColumns();
		for (int i = 1; i <= numAttributes; i++) {
			attributes.add(new Attribute("a" + i));
		}

		/* create instances object and insert the data points */
		Instances wekaInstances = new Instances("JAICore-extracted dataset", attributes, 0);
		for (jaicore.ml.interfaces.Instance instance : instances) {
			wekaInstances.add(fromJAICoreInstance(instance));
		}
		return wekaInstances;
	}

	public static Instances getEmptyDatasetForJAICoreInstance(final jaicore.ml.interfaces.Instance instance) {
		ArrayList<Attribute> attributes = new ArrayList<>();
		int numAttributes = instance.getNumberOfColumns();
		for (int i = 1; i <= numAttributes; i++) {
			attributes.add(new Attribute("a" + i));
		}
		return new Instances("JAICore-extracted dataset", attributes, 0);
	}

	public static Instance fromJAICoreInstance(final jaicore.ml.interfaces.Instance instance) {
		Instances emptyDataset = getEmptyDatasetForJAICoreInstance(instance);
		Instance wekaInstance = new DenseInstance(instance.getNumberOfColumns());
		int att = 0;
		for (Double val : instance) {
			wekaInstance.setValue(att++, val);
		}
		emptyDataset.add(wekaInstance);
		return emptyDataset.iterator().next();
	}

	public static Instance fromJAICoreInstance(final LabeledInstance<String> instance) {

		/* create basic attribute entries */
		ArrayList<Attribute> attributes = new ArrayList<>();
		int numAttributes = instance.getNumberOfColumns();
		for (int i = 1; i <= numAttributes; i++) {
			attributes.add(new Attribute("a" + i));
		}
		List<String> possibleValues = new ArrayList<>();
		possibleValues.add(instance.getLabel());
		Attribute classAttribute = new Attribute("label", possibleValues);
		attributes.add(classAttribute);
		Instances instances = new Instances("JAICore-extracted dataset", attributes, 1);
		instances.setClassIndex(numAttributes);

		Instance inst = new DenseInstance(attributes.size());
		for (int i = 0; i < attributes.size() - 1; i++) {
			inst.setValue(i, instance.get(i));
		}
		instances.add(inst);
		Instance addedInstance = instances.iterator().next();
		addedInstance.setClassValue(instance.getLabel());
		return addedInstance;
	}

	public static WekaCompatibleInstancesImpl toJAICoreLabeledInstances(final Instances wekaInstances) {
		WekaCompatibleInstancesImpl labeledInstances = new WekaCompatibleInstancesImpl(getClassesDeclaredInDataset(wekaInstances));
		for (Instance inst : wekaInstances) {
			labeledInstances.add(toJAICoreLabeledInstance(inst));
		}
		return labeledInstances;
	}

	public static jaicore.ml.interfaces.LabeledInstance<String> toJAICoreLabeledInstance(final Instance wekaInst) {
		jaicore.ml.interfaces.LabeledInstance<String> inst = new SimpleLabeledInstanceImpl();
		for (int att = 0; att < wekaInst.numAttributes(); att++) {
			if (att == wekaInst.classIndex()) {
				continue;
			}
			inst.add(wekaInst.value(att));
		}
		inst.setLabel(wekaInst.classAttribute().value((int)wekaInst.classValue()));
		return inst;
	}

	public static jaicore.ml.interfaces.Instances toJAICoreInstances(final Instances wekaInstances) {
		jaicore.ml.interfaces.Instances instances = new SimpleInstancesImpl();
		for (Instance inst : wekaInstances) {
			instances.add(toJAICoreInstance(inst));
		}
		return instances;
	}

	public static Collection<String> getPossibleClassValues(Instance instance) {
		Collection<String> labels = new ArrayList<>();
		Attribute classAttr = instance.classAttribute();
		for (int i = 0; i < classAttr.numValues(); i++)
			labels.add(classAttr.value(i));
		return labels;
	}

	public static jaicore.ml.interfaces.Instance toJAICoreInstance(final Instance wekaInst) {
		jaicore.ml.interfaces.Instance inst = new SimpleInstanceImpl();
		for (int att = 0; att < wekaInst.numAttributes(); att++) {
			inst.add(wekaInst.value(att));
		}
		return inst;
	}

	public static String getClassifierDescriptor(final Classifier c) {
		StringBuilder sb = new StringBuilder();
		sb.append(c.getClass().getName());
		if (c instanceof OptionHandler) {
			sb.append("- [");
			int i = 0;
			for (String s : ((OptionHandler) c).getOptions()) {
				if (i++ > 0) {
					sb.append(", ");
				}
				sb.append(s);
			}
			sb.append("]");
		}
		return sb.toString();
	}

	public static List<String> getClassNames(final Instance instance) {
		List<String> names = new ArrayList<>();
		Enumeration<Object> namesEnumration = instance.classAttribute().enumerateValues();
		while (namesEnumration.hasMoreElements()) {
			names.add((String) namesEnumration.nextElement());
		}
		return names;
	}

	public static Map<String, Integer> getClassNameToIDMap(final Instance instance) {
		Map<String, Integer> map = new HashMap<>();
		List<String> classNames = getClassNames(instance);
		for (int i = 0; i < classNames.size(); i++) {
			map.put(classNames.get(i), i);
		}
		return map;
	}

	public static int getIntValOfClassName(final Instance instance, final String className) {
		Map<String, Integer> map = getClassNameToIDMap(instance);
		return map.containsKey(className) ? map.get(className) : -1;
	}

	public static void printClassSplitAssignments(final List<Instances> split) {
		int sum = 0;
		StringBuilder sb = new StringBuilder();
		Map<String, Instances> firstSet = getInstancesPerClass(split.get(0));
		for (String cl : firstSet.keySet()) {
			sb.append(cl);
			sb.append(": ");
			int i = 0;
			for (Instances set : split) {
				Map<String, Instances> map = getInstancesPerClass(set);
				sb.append(map.containsKey(cl) ? map.get(cl).size() : 0);
				sum += (map.containsKey(cl) ? map.get(cl).size() : 0);
				if (i < split.size() - 1) {
					sb.append("/");
					i++;
				}
			}
			sb.append("\n");
		}

		System.out.println(sb.toString());
		System.out.println("Total: " + sum);
	}

	public static Instances getInstancesOfClass(final Instances data, final Collection<String> classNames) {
		Instances newInstances = new Instances(data);
		newInstances.removeIf(i -> !classNames.contains(WekaUtil.getClassName(i)));
		return newInstances;
	}

	public static Instances getInstancesOfClass(final Instances data, final String className) {
		Instances newInstances = new Instances(data);
		newInstances.removeIf(i -> !WekaUtil.getClassName(i).equals(className));
		return newInstances;
	}

	public static String getClassName(final Instance instance) {
		return getClassNames(instance).get((int) instance.classValue());
	}

	public static Map<String, Instances> getInstancesPerClass(final Instances data) {
		Instances emptyInstances = new Instances(data);
		emptyInstances.clear();
		Map<String, Instances> classWiseSeparation = new HashMap<>();
		for (Instance i : data) {
			String assignedClass = data.classAttribute().value((int) i.classValue());
			if (!classWiseSeparation.containsKey(assignedClass)) {
				Instances inst = new Instances(emptyInstances);
				classWiseSeparation.put(assignedClass, inst);
			}
			classWiseSeparation.get(assignedClass).add(i);
		}
		return classWiseSeparation;
	}

	public static Map<String, Integer> getNumberOfInstancesPerClass(final Instances data) {
		Map<String, Instances> instancesPerClass = getInstancesPerClass(data);
		Map<String, Integer> counter = new HashMap<>();
		for (String key : instancesPerClass.keySet()) {
			counter.put(key, instancesPerClass.get(key).size());
		}
		return counter;
	}

	public static int getNumberOfInstancesFromClass(final Instances data, final String c) {
		return getInstancesOfClass(data, c).size();
	}

	public static int getNumberOfInstancesFromClass(final Instances data, final Collection<String> cs) {
		Map<String, Integer> map = getNumberOfInstancesPerClass(data);
		int sum = 0;
		for (String c : cs) {
			if (map.containsKey(c)) {
				sum += map.get(c);
			}
		}
		return sum;
	}

	public static double getRelativeNumberOfInstancesFromClass(final Instances data, final String c) {
		if (data.size() == 0) {
			return 0;
		}
		return getNumberOfInstancesFromClass(data, c) / (1f * data.size());
	}

	public static double getRelativeNumberOfInstancesFromClass(final Instances data, final Collection<String> cs) {
		return getNumberOfInstancesFromClass(data, cs) / (1f * data.size());
	}

	public static List<Instances> getStratifiedSplit(final Instances data, final Random rand, final double... portions) {

		/* check that portions sum up to s.th. smaller than 1 */
		double sum = 0;
		for (double p : portions) {
			sum += p;
		}
		if (sum > 1) {
			throw new IllegalArgumentException("Portions must sum up to at most 1.");
		}

		Instances shuffledData = new Instances(data);
		shuffledData.randomize(rand);
		List<Instances> instances = new ArrayList<>();
		Instances emptyInstances = new Instances(shuffledData);
		emptyInstances.clear();

		/* compute instances per class */
		Map<String, Instances> classWiseSeparation = getInstancesPerClass(shuffledData);

		Map<String, Integer> classCapacities = new HashMap<>();
		for (String c : classWiseSeparation.keySet()) {
			classCapacities.put(c, classWiseSeparation.get(c).size());
		}

		/* first assign one item of each class to each fold */
		for (int i = 0; i <= portions.length; i++) {
			Instances instancesForSplit = new Instances(emptyInstances);
			for (String c : classWiseSeparation.keySet()) {
				Instances availableInstances = classWiseSeparation.get(c);
				if (!availableInstances.isEmpty()) {
					instancesForSplit.add(availableInstances.get(0));
					availableInstances.remove(0);
				}
			}
			instances.add(instancesForSplit);
		}

		/* now distribute remaining instances over the folds */
		for (int i = 0; i <= portions.length; i++) {
			double portion = i < portions.length ? portions[i] : 1 - sum;
			Instances instancesForSplit = instances.get(i);
			for (String c : classWiseSeparation.keySet()) {
				Instances availableInstances = classWiseSeparation.get(c);
				int items = (int) Math.min(availableInstances.size(), Math.ceil(portion * classCapacities.get(c)));
				for (int j = 0; j < items; j++) {
					instancesForSplit.add(availableInstances.get(0));
					availableInstances.remove(0);
				}
			}
			instancesForSplit.randomize(rand);
		}
		return instances;
	}

	public static Instance getRefactoredInstance(final Instance instance) {

		/* modify instance */
		Instances dataset = WekaUtil.getEmptySetOfInstancesWithRefactoredClass(instance.dataset());
		int numAttributes = instance.numAttributes();
		int classIndex = instance.classIndex();
		Instance iNew = new DenseInstance(numAttributes);
		for (int i = 0; i < numAttributes; i++) {
			Attribute a = instance.attribute(i);
			if (i != classIndex) {
				iNew.setValue(a, instance.value(a));
			} else {
				iNew.setValue(a, 0.0); // the value does not matter since this should only be used for TESTING
			}
		}
		dataset.add(iNew);
		iNew.setDataset(dataset);
		return iNew;
	}

	public static Instance getRefactoredInstance(final Instance instance, final List<String> classes) {
		/* modify instance */
		Instances dataset = WekaUtil.getEmptySetOfInstancesWithRefactoredClass(instance.dataset(), classes);
		int numAttributes = instance.numAttributes();
		int classIndex = instance.classIndex();
		Instance iNew = new DenseInstance(numAttributes);
		for (int i = 0; i < numAttributes; i++) {
			Attribute a = instance.attribute(i);
			if (i != classIndex) {
				iNew.setValue(a, instance.value(a));
			} else {
				iNew.setValue(a, 0.0); // the value does not matter since this should only be used for TESTING
			}
		}
		dataset.add(iNew);
		iNew.setDataset(dataset);
		return iNew;
	}

	public static Instances getEmptySetOfInstancesWithRefactoredClass(final Instances instances) {
		List<Attribute> newAttributes = getAttributes(instances);
		newAttributes.add(getNewClassAttribute(instances.classAttribute()));
		Instances newData = new Instances("split", (ArrayList<Attribute>) newAttributes, 0);
		newData.setClass(newAttributes.get(newAttributes.size() - 1));
		return newData;
	}

	public static Instances getEmptySetOfInstancesWithRefactoredClass(final Instances instances, final List<String> classes) {
		List<Attribute> newAttributes = getAttributes(instances);
		newAttributes.add(getNewClassAttribute(instances.classAttribute(), classes));
		Instances newData = new Instances("split", (ArrayList<Attribute>) newAttributes, 0);
		newData.setClass(newAttributes.get(newAttributes.size() - 1));
		return newData;
	}

	public static List<Attribute> getAttributes(final Instances inst) {
		List<Attribute> attributes = new ArrayList<>();
		Enumeration<Attribute> e = inst.enumerateAttributes();
		while (e.hasMoreElements()) {
			attributes.add(e.nextElement());
		}
		return attributes;
	}

	public static List<Attribute> getAttributes(final Instance inst) {
		List<Attribute> attributes = new ArrayList<>();
		Enumeration<Attribute> e = inst.enumerateAttributes();
		while (e.hasMoreElements()) {
			attributes.add(e.nextElement());
		}
		return attributes;
	}

	public static Attribute getNewClassAttribute(final Attribute attribute) {
		List<String> vals = Arrays.asList(new String[] { "0.0", "1.0" });
		Attribute a = new Attribute(attribute.name(), vals);
		return a;
	}

	public static Attribute getNewClassAttribute(final Attribute attribute, final List<String> classes) {
		Attribute a = new Attribute(attribute.name(), classes);
		return a;
	}

	public static List<Attribute> getReplacedAttributeList(final List<Attribute> attributes, final Attribute classAttribute) {
		ArrayList<Attribute> newAttributes = new ArrayList<>();
		System.out.println(attributes);
		for (Attribute a : attributes) {
			if (classAttribute != a) {
				newAttributes.add(a);
			} else {
				newAttributes.add(getNewClassAttribute(classAttribute));
			}
		}
		System.out.println(newAttributes);
		return newAttributes;
	}

	public static Instances mergeClassesOfInstances(final Instances data, final Collection<String> cluster1, final Collection<String> cluster2) {
		Instances newData = WekaUtil.getEmptySetOfInstancesWithRefactoredClass(data);
		for (Instance i : data) {
			Instance iNew = (Instance) i.copy();
			String className = i.classAttribute().value((int) Math.round(i.classValue()));
			if (cluster1.contains(className)) {
				iNew.setClassValue(0.0);
				newData.add(iNew);
			} else if (cluster2.contains(className)) {
				iNew.setClassValue(1.0);
				newData.add(iNew);
			}
		}
		return newData;
	}

	public static Instances mergeClassesOfInstances(final Instances data, final List<Set<String>> instancesCluster) {
		List<String> classes = new LinkedList<>();
		IntStream.range(0, instancesCluster.size()).forEach(x -> {
			classes.add("C" + ((double) x));
		});

		Instances newData = WekaUtil.getEmptySetOfInstancesWithRefactoredClass(data, classes);
		for (Instance i : data) {
			Instance iNew = (Instance) i.copy();
			String className = i.classAttribute().value((int) Math.round(i.classValue()));
			for (Set<String> cluster : instancesCluster) {
				if (cluster.contains(className)) {
					iNew.setClassValue(instancesCluster.indexOf(cluster));
					newData.add(iNew);
				}
			}
		}
		System.out.println(data.size() + " - " + newData.size());
		return newData;
	}

	public static List<String> getClassesDeclaredInDataset(final Instances data) {
		List<String> classes = new ArrayList<>();
		Attribute classAttribute = data.classAttribute();
		for (int i = 0; i < classAttribute.numValues(); i++)
			classes.add(classAttribute.value(i));
		return classes;
	}

	public static Collection<String> getClassesActuallyContainedInDataset(final Instances data) {
		Map<String, Integer> counter = getNumberOfInstancesPerClass(data);
		return counter.keySet().stream().filter(k -> counter.get(k) != 0).collect(Collectors.toList());
	}

	public static Instance filterInstance(final Instance i, final AttributeSelection as) throws Exception {
		Instances data = new Instances(i.dataset());
		data.clear();
		data.add(i);

		data = as.reduceDimensionality(data);
		if (data.isEmpty()) {
			System.err.println("NO INSTANCE REMAINING AFTER FILTERING");
		}
		return data.iterator().next();
	}

	public static String instancesToJsonString(final Instances data) {
		StringBuilder sb = new StringBuilder();
		JSONNode json = JSONInstances.toJSON(data);
		json.getChild("header").removeFromParent();
		StringBuffer buffer = new StringBuffer();
		json.toString(buffer);
		sb.append(buffer.toString());
		sb.append("\n");
		return sb.toString();
	}

	public static Instances jsonStringToInstances(final String json) {
		try {
			JSONNode node = JSONNode.read(new BufferedReader(new StringReader(json)));
			return JSONInstances.toInstances(node);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
