package autofe.test;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import autofe.algorithm.hasco.AutoFEWekaPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.OneR;
import weka.core.Instances;

public class ImageAutoMLHASCOTest {

	private static final String DATA_FOLDER = "testrsc/kit/";

	public static void main(final String[] args) throws Exception {
		System.out.println("Load dataset...");
		final DataSet parsedDataSet = DataSetUtils.loadDatasetFromImageFolder(new File(DATA_FOLDER));

		System.out.println("Split dataset into train and test set...");
		final List<DataSet> stratifiedSplit = DataSetUtils.getStratifiedSplit(parsedDataSet, new Random(0), .7);
		System.out.println(stratifiedSplit.get(0).getIntermediateInstances().size() + " " + stratifiedSplit.get(1).getIntermediateInstances().size());

		System.out.println("Instantiate AutoFEWekaPipeline...");
		FilterPipelineFactory factory = new FilterPipelineFactory(parsedDataSet.getIntermediateInstances().get(0).shape());

		ComponentLoader cl = new ComponentLoader(new File("model/MLPlanFEWeka.json"));
		List<Component> componentList = new LinkedList<>(cl.getComponents());
		Map<String, Component> componentMap = new HashMap<>();
		for (Component c : componentList) {
			componentMap.put(c.getName(), c);
		}

		Map<String, String> filterParameterValues = new HashMap<>();
		filterParameterValues.put("catFilter", "SobelEdgeDetector ");
		filterParameterValues.put("catFilter", "GaussianBlur");
		ComponentInstance filter = new ComponentInstance(componentMap.get("autofe.algorithm.hasco.filter.image.CatalanoWrapperFilter"), filterParameterValues, new HashMap<>());

		ComponentInstance extractor = new ComponentInstance(componentMap.get("autofe.algorithm.hasco.filter.image.LocalBinaryPatternFilter"), new HashMap<>(), new HashMap<>());

		Map<String, ComponentInstance> abstractPipeSubComponents = new HashMap<>();
		abstractPipeSubComponents.put("extractor", extractor);
		abstractPipeSubComponents.put("preprocessors", filter);
		ComponentInstance abstractPipe = new ComponentInstance(componentMap.get("AbstractPipe"), new HashMap<>(), abstractPipeSubComponents);
		System.out.println(abstractPipe);

		Map<String, ComponentInstance> filterPipelineSubComponents = new HashMap<>();
		filterPipelineSubComponents.put("pipe", abstractPipe);
		ComponentInstance filterPipeline = new ComponentInstance(componentMap.get("pipeline"), new HashMap<>(), filterPipelineSubComponents);

		System.out.println("ComponentInstance of filterPipeline: " + filterPipeline);

		FilterPipeline filterPipe = factory.getComponentInstantiation(filterPipeline);

		MLPipeline mlPipe = new MLPipeline(new LinkedList<>(), new OneR());
		final AutoFEWekaPipeline pipe = new AutoFEWekaPipeline(filterPipe, mlPipe);

		System.out.println("Build AutoFEWekaPipeline...");
		pipe.buildClassifier(stratifiedSplit.get(0));

		System.out.println("Transform DataSet into Instances via AutoFEWekaPipeline...");
		Instances transformedData = pipe.transformData(stratifiedSplit.get(1));

		System.out.println("Evaluate AutoFEWekaPipeline with transformed data...");
		Evaluation eval = new Evaluation(transformedData);
		eval.evaluateModel(pipe, transformedData, new Object[] {});

		System.out.println("Error rate: " + eval.errorRate());

	}

}
