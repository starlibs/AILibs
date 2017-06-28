package util.ml;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import weka.attributeSelection.AttributeSelection;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.json.JSONInstances;
import weka.core.json.JSONNode;
import weka.filters.Filter;

public class WekaUtil {

	public static List<String> getClassNames(Instance instance) {
		List<String> names = new ArrayList<>();
		Enumeration<Object> namesEnumration = instance.classAttribute().enumerateValues();
		while (namesEnumration.hasMoreElements()) {
			names.add((String) namesEnumration.nextElement());
		}
		return names;
	}

	public static Map<String, Integer> getClassNameToIDMap(Instance instance) {
		Map<String, Integer> map = new HashMap<>();
		List<String> classNames = getClassNames(instance);
		for (int i = 0; i < classNames.size(); i++)
			map.put(classNames.get(i), i);
		return map;
	}

	public static int getIntValOfClassName(Instance instance, String className) {
		Map<String, Integer> map = getClassNameToIDMap(instance);
		return map.containsKey(className) ? map.get(className) : -1;
	}

	public static void printClassSplitAssignments(List<Instances> split) {
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

	public static Instances getInstancesOfClass(Instances data, Collection<String> classNames) {
		Instances newInstances = new Instances(data);
		newInstances.removeIf(i -> !classNames.contains(WekaUtil.getClassName(i)));
		return newInstances;
	}

	public static Instances getInstancesOfClass(Instances data, String className) {
		Instances newInstances = new Instances(data);
		newInstances.removeIf(i -> !WekaUtil.getClassName(i).equals(className));
		return newInstances;
	}

	public static String getClassName(Instance instance) {
		return getClassNames(instance).get((int) instance.classValue());
	}

	public static Map<String, Instances> getInstancesPerClass(Instances data) {
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

	public static Map<String, Integer> getNumberOfInstancesPerClass(Instances data) {
		Map<String, Instances> instancesPerClass = getInstancesPerClass(data);
		Map<String, Integer> counter = new HashMap<>();
		for (String key : instancesPerClass.keySet()) {
			counter.put(key, instancesPerClass.get(key).size());
		}
		return counter;
	}

	public static List<Instances> getStratifiedSplit(Instances data, Random rand, double... portions) {

		/* check that portions sum up to s.th. smaller than 1 */
		double sum = 0;
		for (double p : portions)
			sum += p;
		if (sum > 1)
			throw new IllegalArgumentException("Portions must sum up to at most 1.");

		Instances shuffledData = new Instances(data);
		shuffledData.randomize(rand);
		List<Instances> instances = new ArrayList<>();
		Instances emptyInstances = new Instances(shuffledData);
		emptyInstances.clear();

		/* compute instances per class */
		Map<String, Instances> classWiseSeparation = getInstancesPerClass(shuffledData);

		Map<String, Integer> classCapacities = new HashMap<>();
		for (String c : classWiseSeparation.keySet())
			classCapacities.put(c, classWiseSeparation.get(c).size());

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

	public static Instance getRefactoredInstance(Instance instance) {

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

	public static Instances getEmptySetOfInstancesWithRefactoredClass(Instances instances) {
		List<Attribute> newAttributes = getAttributes(instances);
		newAttributes.add(getNewClassAttribute(instances.classAttribute()));
		Instances newData = new Instances("split", (ArrayList<Attribute>) newAttributes, 0);
		newData.setClass(newAttributes.get(newAttributes.size() - 1));
		return newData;
	}

	public static List<Attribute> getAttributes(Instances inst) {
		List<Attribute> attributes = new ArrayList<>();
		Enumeration<Attribute> e = inst.enumerateAttributes();
		while (e.hasMoreElements()) {
			attributes.add(e.nextElement());
		}
		return attributes;
	}

	public static List<Attribute> getAttributes(Instance inst) {
		List<Attribute> attributes = new ArrayList<>();
		Enumeration<Attribute> e = inst.enumerateAttributes();
		while (e.hasMoreElements()) {
			attributes.add(e.nextElement());
		}
		return attributes;
	}

	public static Attribute getNewClassAttribute(Attribute attribute) {
		List<String> vals = Arrays.asList(new String[] { "0.0", "1.0" });
		Attribute a = new Attribute(attribute.name(), vals);
		return a;
	}

	public static List<Attribute> getReplacedAttributeList(List<Attribute> attributes, Attribute classAttribute) {
		ArrayList<Attribute> newAttributes = new ArrayList<>();
		System.out.println(attributes);
		for (Attribute a : attributes) {
			if (classAttribute != a)
				newAttributes.add(a);
			else {
				newAttributes.add(getNewClassAttribute(classAttribute));
			}
		}
		System.out.println(newAttributes);
		return newAttributes;
	}

	public static Instances mergeClassesOfInstances(Instances data, Collection<String> cluster1, Collection<String> cluster2) {
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

	public static Collection<String> getClassesDeclaredInDataset(Instances data) {
		Enumeration<Object> classesExt = data.classAttribute().enumerateValues();
		List<String> classes = new ArrayList<>();
		while (classesExt.hasMoreElements()) {
			String className = (String) classesExt.nextElement();
			if (className.contains(",") || className.contains("(") || className.contains(")"))
				throw new IllegalArgumentException("Class names must not contain the following symbols: ,)(");
			classes.add(className);
		}
		return classes;
	}

	public static Collection<String> getClassesActuallyContainedInDataset(Instances data) {
		Map<String, Integer> counter = getNumberOfInstancesPerClass(data);
		return counter.keySet().stream().filter(k -> counter.get(k) != 0).collect(Collectors.toList());
	}

	public static Instance filterInstance(Instance i, AttributeSelection as) throws Exception {
		Instances data = new Instances(i.dataset());
		data.clear();
		data.add(i);

		data = as.reduceDimensionality(data);
		if (data.isEmpty())
			System.err.println("NO INSTANCE REMAINING AFTER FILTERING");
		return data.iterator().next();
	}

	public static String instancesToJsonString(Instances data) {
		StringBuilder sb = new StringBuilder();
		JSONNode json = JSONInstances.toJSON(data);
		json.getChild("header").removeFromParent();
		StringBuffer buffer = new StringBuffer();
		json.toString(buffer);
		sb.append(buffer.toString());
		sb.append("\n");
		return sb.toString();
	}
	
	public static Instances jsonStringToInstances(String json) {
		try {
			JSONNode node = JSONNode.read(new BufferedReader(new StringReader(json)));
			return JSONInstances.toInstances(node);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
