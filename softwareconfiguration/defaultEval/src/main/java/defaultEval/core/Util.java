package defaultEval.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import de.upb.crc901.automl.hascocombinedml.MLServicePipelineFactory;
import de.upb.crc901.automl.pipeline.service.MLServicePipeline;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import scala.util.parsing.combinator.testing.Str;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Util {
	
	/** Suffix Types for unique params */
	enum ParamType {
		searcher, evaluator, classifier
	}

	public static void loadClassifierComponents(ComponentLoader cl, String envPath) throws IOException {
		cl.loadComponents(new File(envPath + "/models/weka-classifiers.json"));
	}

	public static void loadPreprocessorComponents(ComponentLoader cl, String envPath) throws IOException {
		cl.loadComponents(new File(envPath + "/models/weka-preprocessors.json"));
	}

	public static ComponentInstance createPipeline(Component searcher, Map<String, String> searcherParameter,
			Component evaluator, Map<String, String> evaluatorParameter, Component classifier,
			Map<String, String> classifierParameter) {

		if (searcher != null) {
			ComponentInstance searcherInstance = new ComponentInstance(searcher, searcherParameter, new HashMap<>());
			ComponentInstance evaluatorInstance = new ComponentInstance(evaluator, evaluatorParameter, new HashMap<>());
			ComponentInstance classifierInstance = new ComponentInstance(classifier, classifierParameter,
					new HashMap<>()); // TODO req.!

			Map<String, ComponentInstance> satisfactionOfRequiredInterfacesPreprocessor = new HashMap<>();
			satisfactionOfRequiredInterfacesPreprocessor.put("eval", evaluatorInstance);
			satisfactionOfRequiredInterfacesPreprocessor.put("search", searcherInstance);

			Map<String, ComponentInstance> satisfactionOfRequiredInterfacesPipeline = new HashMap<>();
			satisfactionOfRequiredInterfacesPipeline.put("preprocessor", new ComponentInstance(
					new Component("preprocessor"), new HashMap<>(), satisfactionOfRequiredInterfacesPreprocessor));
			satisfactionOfRequiredInterfacesPipeline.put("classifier", classifierInstance);

			return new ComponentInstance(new Component("pipeline"), new HashMap<>(),
					satisfactionOfRequiredInterfacesPipeline);

		} else {
			return new ComponentInstance(classifier, classifierParameter, new HashMap<>()); // TODO req.!
		}

	}

	public static ComponentInstance createPipeline(ComponentInstance searcherInstance,
			ComponentInstance evaluatorInstance, ComponentInstance classifierInstance) {
		if (searcherInstance != null) {
			Map<String, ComponentInstance> satisfactionOfRequiredInterfacesPreprocessor = new HashMap<>();
			satisfactionOfRequiredInterfacesPreprocessor.put("eval", evaluatorInstance);
			satisfactionOfRequiredInterfacesPreprocessor.put("search", searcherInstance);

			Map<String, ComponentInstance> satisfactionOfRequiredInterfacesPipeline = new HashMap<>();
			satisfactionOfRequiredInterfacesPipeline.put("preprocessor", new ComponentInstance(
					new Component("preprocessor"), new HashMap<>(), satisfactionOfRequiredInterfacesPreprocessor));
			satisfactionOfRequiredInterfacesPipeline.put("classifier", classifierInstance);

			return new ComponentInstance(new Component("pipeline"), new HashMap<>(),
					satisfactionOfRequiredInterfacesPipeline);

		} else {
			return classifierInstance;
		}
	}

	/**
	 * Method to evaluate the pipelines of defaultEval to be sure the Settings are
	 * all the same
	 */
	public static double evaluate(Instances instances, Classifier classifier, int seed) {
		double pctIncorrect = 0;

		try {
			Evaluation eval = new Evaluation(instances);

			eval.crossValidateModel(classifier, instances, 10, new Random(seed));

			pctIncorrect = eval.pctIncorrect();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return pctIncorrect;
	}

	public static Instances loadInstances(String path, String name) {
		DataSource ds;
		Instances instances = null;
		try {
			ds = new DataSource(path + "/" + name + ".arff");
			instances = new Instances(ds.getDataSet());
			instances.setClassIndex(instances.numAttributes() - 1); // last one as class
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instances;
	}

	public static String convertToUniqueParamName(String name, ParamType type) {
		switch (type) {
		case searcher:
			return String.format("%s_%s", name, "s");
		case evaluator:
			return String.format("%s_%s", name, "e");

		case classifier:
			return String.format("%s_%s", name, "c");

		default:
			return name;
		}
	}
	
	public static String revertFromUniqueParamName(String name) {
		if(name.length() <= 2) {
			throw new IllegalArgumentException("Name is to short to be a unique name");
		}
		return name.substring(0, name.length()-2);
	}
	

}
