package de.upb.crc901.automl.hascocombinedml;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.upb.crc901.automl.pipeline.service.MLPipelinePlan;
import de.upb.crc901.automl.pipeline.service.MLPipelinePlan.MLPipe;
import de.upb.crc901.automl.pipeline.service.MLServicePipeline;
import hasco.model.ComponentInstance;
import hasco.query.Factory;
import jaicore.basic.ListHelper;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;

public class MLServicePipelineFactory implements Factory<MLServicePipeline> {

	@Override
	public MLServicePipeline getComponentInstantiation(final ComponentInstance groundComponent) {

		MLPipelinePlan plan = new MLPipelinePlan();

		try {
			ComponentInstance preprocessorCI = null;
			String ppName = "";
			ComponentInstance classifierCI = null;
			String classifierName = "";

			switch (groundComponent.getComponent().getName()) {
			case "pipeline": {
				/* Retrieve component instances of pipeline */
				preprocessorCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("preprocessor");
				ppName = preprocessorCI.getComponent().getName();

				classifierCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier");
				classifierName = classifierCI.getComponent().getName();
				break;
			}
			default: {
				classifierCI = groundComponent;
				classifierName = groundComponent.getComponent().getName();
				break;
			}
			}

			if (ppName.startsWith("sklearn")) {
				this.addSKLearnPreprocessor(preprocessorCI, plan);
			} else if (ppName.startsWith("weka")) {
				this.addWEKAPreprocessor(preprocessorCI, plan);
			}

			if (classifierName.startsWith("sklearn")) {
				this.addSKLearnClassifier(classifierCI, plan);
			} else if (classifierName.startsWith("weka")) {
				this.addWEKAClassifier(classifierCI, plan);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			return new MLServicePipeline(plan);
		} catch (InterruptedException e) {
			return null;
		}
	}

	private void addWEKAPreprocessor(final ComponentInstance ci, final MLPipelinePlan plan) throws Exception {
		ComponentInstance evaluatorCI = ci.getSatisfactionOfRequiredInterfaces().get("eval");
		ComponentInstance searcherCI = ci.getSatisfactionOfRequiredInterfaces().get("search");

		ASEvaluation eval = ASEvaluation.forName(evaluatorCI.getComponent().getName(),
				this.getParameterList(evaluatorCI).toArray(new String[] {}));
		ASSearch search = ASSearch.forName(searcherCI.getComponent().getName(),
				this.getParameterList(searcherCI).toArray(new String[] {}));
		plan.addWekaAttributeSelection(search, eval);
	}

	private List<String> getParameterList(final ComponentInstance ci) {
		List<String> parameters = new LinkedList<>();

		for (Entry<String, String> parameterValues : ci.getParameterValues().entrySet()) {
			if (parameterValues.getKey().toLowerCase().endsWith("activator")
					|| parameterValues.getValue().equals("REMOVED")) {
				continue;
			}

			if (!parameterValues.getValue().equals("false")) {
				parameters.add("-" + parameterValues.getKey());
			}
			if (parameterValues.getValue() != null && !parameterValues.getValue().equals("")
					&& !parameterValues.getValue().equals("true") && !parameterValues.getValue().equals("false")) {
				parameters.add(parameterValues.getValue());
			}
		}

		for (String paramName : ci.getSatisfactionOfRequiredInterfaces().keySet()) {
			List<String> subParams = this.getParameterList(ci.getSatisfactionOfRequiredInterfaces().get(paramName));
			String paramValue = ci.getSatisfactionOfRequiredInterfaces().get(paramName).getComponent().getName() + " "
					+ ListHelper.implode(subParams, " ");
			parameters.add("-" + paramName);
			parameters.add(paramValue);
		}

		return parameters;
	}

	private void addWEKAClassifier(final ComponentInstance ci, final MLPipelinePlan plan) throws Exception {
		ci.getParameterValues();
		List<String> parameters = this.getParameterList(ci);
		plan.setClassifier(
				AbstractClassifier.forName(ci.getComponent().getName(), parameters.toArray(new String[] {})));
	}

	private void addSKLearnPreprocessor(final ComponentInstance ci, final MLPipelinePlan plan) {
		MLPipe preprocessorPipe = plan.addAttributeSelection(ci.getComponent().getName());
		this.setParameters(plan, preprocessorPipe, ci.getParameterValues());
	}

	private void addSKLearnClassifier(final ComponentInstance ci, final MLPipelinePlan plan) {
		MLPipe classifierPipe = plan.setClassifier(ci.getComponent().getName());
		this.setParameters(plan, classifierPipe, ci.getParameterValues());
	}

	private void setParameters(final MLPipelinePlan plan, final MLPipe pipe,
			final Map<String, String> parameterValues) {
		for (String parameterName : parameterValues.keySet()) {
			plan.addOptions(pipe, parameterName, parameterValues.get(parameterName));
		}
	}

}
