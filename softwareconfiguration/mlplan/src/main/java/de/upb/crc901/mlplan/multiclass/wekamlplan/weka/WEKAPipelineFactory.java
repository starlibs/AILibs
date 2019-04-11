package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import jaicore.basic.sets.SetUtil;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;

public class WEKAPipelineFactory implements IClassifierFactory {

	@Override
	public MLPipeline getComponentInstantiation(final ComponentInstance groundComponent) throws ComponentInstantiationFailedException {

		ComponentInstance preprocessorCI = null;
		String ppName = "";
		ComponentInstance classifierCI = null;

		if (groundComponent.getComponent().getName().equals("pipeline")) {
			/* Retrieve component instances of pipeline */
			preprocessorCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("preprocessor");
			ppName = preprocessorCI.getComponent().getName();

			classifierCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier");
		} else {
			classifierCI = groundComponent;
		}

		try {
			ASEvaluation eval = null;
			ASSearch search = null;
			if (preprocessorCI != null && ppName.startsWith("weka")) {
				ComponentInstance evaluatorCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("eval");
				ComponentInstance searcherCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("search");

				eval = ASEvaluation.forName(evaluatorCI.getComponent().getName(), this.getParameterList(evaluatorCI).toArray(new String[] {}));
				search = ASSearch.forName(searcherCI.getComponent().getName(), this.getParameterList(searcherCI).toArray(new String[] {}));
			}

			classifierCI.getParameterValues();
			List<String> parameters = this.getParameterList(classifierCI);
			Classifier c = AbstractClassifier.forName(classifierCI.getComponent().getName(), parameters.toArray(new String[] {}));
			return new MLPipeline(search, eval, c);
		} catch (Exception e) {
			throw new ComponentInstantiationFailedException(e, "Could not instantiate component.");
		}
	}

	private List<String> getParameterList(final ComponentInstance ci) {
		List<String> parameters = new LinkedList<>();

		for (Entry<String, String> parameterValues : ci.getParameterValues().entrySet()) {
			if (parameterValues.getKey().toLowerCase().endsWith("activator") || parameterValues.getValue().equals("REMOVED")) {
				continue;
			}

			if (!parameterValues.getValue().equals("false")) {
				parameters.add("-" + parameterValues.getKey());
			}
			if (parameterValues.getValue() != null && !parameterValues.getValue().equals("") && !parameterValues.getValue().equals("true") && !parameterValues.getValue().equals("false")) {
				parameters.add(parameterValues.getValue());
			}
		}

		for (String paramName : ci.getSatisfactionOfRequiredInterfaces().keySet()) {
			List<String> subParams = this.getParameterList(ci.getSatisfactionOfRequiredInterfaces().get(paramName));
			String paramValue = ci.getSatisfactionOfRequiredInterfaces().get(paramName).getComponent().getName() + " " + SetUtil.implode(subParams, " ");
			parameters.add("-" + paramName);
			parameters.add(paramValue);
		}

		return parameters;
	}

}
