package de.upb.crc901.mlplan.multiclass.weka;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.upb.crc901.automl.pipeline.ClassifierFactory;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import hasco.model.CategoricalParameterDomain;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import jaicore.basic.ListHelper;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;

public class WEKAPipelineFactory implements ClassifierFactory {

	@Override
	public Classifier getComponentInstantiation(final ComponentInstance groundComponent) throws Exception {
		if (groundComponent == null) {
			return null;
		}

		ComponentInstance preprocessorCI = null;
		String ppName = "";
		ComponentInstance classifierCI = null;
		boolean isPipeline = false;

		switch (groundComponent.getComponent().getName()) {
		case "pipeline": {
			isPipeline = true;
			/* Retrieve component instances of pipeline */
			preprocessorCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("preprocessor");

			if (preprocessorCI != null) {
				ppName = preprocessorCI.getComponent().getName();
			}
			classifierCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier");
			break;
		}
		default: {
			classifierCI = groundComponent;
			break;
		}
		}

		if (classifierCI == null) {
			return null;
		}

		Classifier c;
		try {
			List<String> parameters = this.getParameterList(classifierCI);
			c = AbstractClassifier.forName(classifierCI.getComponent().getName(), parameters.toArray(new String[] {}));
		} catch (IllegalArgumentException e) {
			return null;
		}

		if (isPipeline) {
			if (preprocessorCI == null) {
				return null;
			}
			ASEvaluation eval = null;
			ASSearch search = null;
			if (ppName.startsWith("weka")) {
				ComponentInstance evaluatorCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("eval");
				ComponentInstance searcherCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("search");

				eval = ASEvaluation.forName(evaluatorCI.getComponent().getName(), this.getParameterList(evaluatorCI).toArray(new String[] {}));
				search = ASSearch.forName(searcherCI.getComponent().getName(), this.getParameterList(searcherCI).toArray(new String[] {}));
			}
			return new MLPipeline(search, eval, c);

		} else {
			return c;
		}
	}

	private List<String> getParameterList(final ComponentInstance ci) {
		List<String> parameters = new LinkedList<>();

		for (Entry<String, String> parameterValues : ci.getParameterValues().entrySet()) {
			if (parameterValues.getKey().toLowerCase().endsWith("activator") || parameterValues.getValue().equals("REMOVED")) {
				continue;
			}

			Parameter p = ci.getComponent().getParameter(parameterValues.getKey());
			if (p.isCategorical() && !((CategoricalParameterDomain) p.getDefaultDomain()).contains(parameterValues.getValue())) {
				throw new IllegalArgumentException("Value of parameter is not in the domain");
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
			String paramValue = ci.getSatisfactionOfRequiredInterfaces().get(paramName).getComponent().getName() + " " + ListHelper.implode(subParams, " ");
			parameters.add("-" + paramName);
			parameters.add(paramValue);
		}

		return parameters;
	}

}
