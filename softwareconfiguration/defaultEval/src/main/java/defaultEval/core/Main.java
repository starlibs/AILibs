package defaultEval.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import de.upb.crc901.automl.hascocombinedml.MLServicePipelineFactory;
import de.upb.crc901.automl.pipeline.service.MLServicePipeline;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.serialization.ComponentLoader;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.EvaluationUtils;
import weka.core.Instances;

public class Main {

	enum Optimizer {
		DGGA, SMAC, Hyperband
	}

	// usable Components
	private ComponentLoader classifierComponents = new ComponentLoader();
	private ComponentLoader preProcessorComponents = new ComponentLoader();

	private Instances instances;

	private ArrayList<PipelineResultPair> defaultPipelines = new ArrayList<>();
	private ArrayList<PipelineResultPair> optimizedPipelines = new ArrayList<>();

	public Main() {
		try {
			Util.loadClassifierComponents(classifierComponents);
			Util.loadPreprocessorComponents(preProcessorComponents);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private void evaluateDefaultConfigurationClassifier() {

		// create Pipelines
		for (Component classifierComponent : classifierComponents.getComponents()) {
			HashMap<String, String> classifierParameters = new HashMap<>();
			for (Parameter p : classifierComponent.getParameters()) {
				classifierParameters.put(p.getName(), p.getDefaultValue().toString());
			}

			for (Component preProcessorComponent : preProcessorComponents.getComponents()) {
				HashMap<String, String> preProcessorParameters = new HashMap<>();
				for (Parameter p : preProcessorComponent.getParameters()) {
					preProcessorParameters.put(p.getName(), p.getDefaultValue().toString());
				}

				defaultPipelines.add(new PipelineResultPair(Util.createPipeline(preProcessorComponent,
						preProcessorParameters, classifierComponent, classifierParameters)));

			}
			defaultPipelines.add(new PipelineResultPair(
					Util.createPipeline(null, new HashMap<>(), classifierComponent, classifierParameters)));
		}

		// evaluate Pipelines

		for (PipelineResultPair pipelineResultPair : defaultPipelines) {
			double pctIncorrect = 100;

			try {
				Evaluation eval = new Evaluation(instances);

				eval.crossValidateModel(pipelineResultPair.pipeline, instances, 10, new Random());

				pctIncorrect = eval.pctIncorrect();

			} catch (Exception e) {
				e.printStackTrace();
			}

			pipelineResultPair.result = pctIncorrect;
		}

		// sort Pipelines
		defaultPipelines.sort(new Comparator<PipelineResultPair>() {
			@Override
			public int compare(PipelineResultPair o1, PipelineResultPair o2) {
				return Double.compare(o1.result, o2.result);
			}
		});

	}

	private void evaluateOptimizedConfigurationClassifier() {
		// create Pipelines
		for (Component classifierComponent : classifierComponents.getComponents()) {
			HashMap<String, String> classifierParameters = new HashMap<>();
			for (Parameter p : classifierComponent.getParameters()) {
				classifierParameters.put(p.getName(), p.getDefaultValue().toString());
			}

			for (Component preProcessorComponent : preProcessorComponents.getComponents()) {
				HashMap<String, String> preProcessorParameters = new HashMap<>();
				for (Parameter p : preProcessorComponent.getParameters()) {
					preProcessorParameters.put(p.getName(), p.getDefaultValue().toString());
				}

				optimizedPipelines.add(new PipelineResultPair(
						optimizeClassifierConfiguration(preProcessorComponent, classifierComponent, Optimizer.SMAC)));
			}

			optimizedPipelines.add(new PipelineResultPair(optimizeClassifierConfiguration(null, classifierComponent, Optimizer.SMAC)));

		}

		// evaluate Pipelines

		for (PipelineResultPair pipelineResultPair : optimizedPipelines) {
			double pctIncorrect = 100;

			try {
				Evaluation eval = new Evaluation(instances);

				eval.crossValidateModel(pipelineResultPair.pipeline, instances, 10, new Random());

				pctIncorrect = eval.pctIncorrect();

			} catch (Exception e) {
				e.printStackTrace();
			}

			pipelineResultPair.result = pctIncorrect;
		}

		// sort Pipelines
		optimizedPipelines.sort(new Comparator<PipelineResultPair>() {
			@Override
			public int compare(PipelineResultPair o1, PipelineResultPair o2) {
				return Double.compare(o1.result, o2.result);
			}
		});

	}

	private MLServicePipeline optimizeClassifierConfiguration(Component preProcessorComponent,
			Component classifierComponent, Optimizer method) {

		switch (method) {
		case DGGA:

			break;
		case Hyperband:

			break;
		case SMAC:
			
			break;

		default:
			break;
		}

		return new MLServicePipelineFactory().getComponentInstantiation(
				new ComponentInstance(classifierComponent, new HashMap<>(), new HashMap<>()));
	}

}
