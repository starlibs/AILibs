package de.upb.crc901.automl.hascoscikitlearnml;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hasco.model.ComponentInstance;
import hasco.query.Factory;
import weka.classifiers.Classifier;

public class ScikitLearnCompositionFactory implements Factory<Classifier> {

	@Override
	public ScikitLearnComposition getComponentInstantiation(final ComponentInstance groundComponent) {
		Map<String, String> pipelineSourceCodeMap = this.generatePythonCodeFromComponentInstance(groundComponent);
		try {
			return new ScikitLearnComposition(pipelineSourceCodeMap, groundComponent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Map<String, String> generatePythonCodeFromComponentInstance(final ComponentInstance ci) {
		Map<String, String> parsedCodeMap = new HashMap<>();
		StringBuilder pythonCodeBuilder = new StringBuilder();

		String[] componentNameSplit = ci.getComponent().getName().split("\\.");
		String singleName = componentNameSplit[componentNameSplit.length - 1];

		String importStatement = "";
		if (!singleName.equals("make_forward")) {
			importStatement = "from ";
			for (int i = 0; i < componentNameSplit.length - 1; i++) {
				importStatement += ((i > 0) ? "." : "") + componentNameSplit[i];
			}
			importStatement += " import " + singleName + "\n";
		}

		switch (ci.getComponent().getName()) {
		case "sklearn.pipeline.make_pipeline": {
			pythonCodeBuilder.append(singleName);
			pythonCodeBuilder.append("(");

			ComponentInstance[] comps = { ci.getSatisfactionOfRequiredInterfaces().get("preprocessor"), ci.getSatisfactionOfRequiredInterfaces().get("classifier") };
			boolean first = true;
			for (ComponentInstance sc : comps) {
				Map<String, String> subComponentMap = this.generatePythonCodeFromComponentInstance(sc);
				importStatement += subComponentMap.get("import");
				if (first) {
					first = false;
				} else {
					pythonCodeBuilder.append(",");
				}
				pythonCodeBuilder.append(subComponentMap.get("pipeline"));
			}
			pythonCodeBuilder.append(")");
			break;
		}
		case "sklearn.pipeline.make_union": {
			pythonCodeBuilder.append(singleName);
			pythonCodeBuilder.append("(");

			ComponentInstance[] comps = { ci.getSatisfactionOfRequiredInterfaces().get("p1"), ci.getSatisfactionOfRequiredInterfaces().get("p2") };
			boolean first = true;
			for (ComponentInstance sc : comps) {
				Map<String, String> subComponentMap = this.generatePythonCodeFromComponentInstance(sc);
				importStatement += subComponentMap.get("import");
				if (first) {
					first = false;
				} else {
					pythonCodeBuilder.append(",");
				}
				pythonCodeBuilder.append(subComponentMap.get("pipeline"));
			}
			pythonCodeBuilder.append(")");
			break;
		}
		case "make_forward": {
			Map<String, String> pre = this.generatePythonCodeFromComponentInstance(ci.getSatisfactionOfRequiredInterfaces().get("source"));
			pythonCodeBuilder.append(pre.get("pipeline"));
			importStatement += pre.get("import");
			pythonCodeBuilder.append(",");
			Map<String, String> pp = this.generatePythonCodeFromComponentInstance(ci.getSatisfactionOfRequiredInterfaces().get("base"));
			pythonCodeBuilder.append(pp.get("pipeline"));
			importStatement += pp.get("import");
			break;
		}
		default: {
			pythonCodeBuilder.append(singleName);
			pythonCodeBuilder.append("(");

			boolean first = true;
			for (String paramName : ci.getParameterValues().keySet()) {
				if (first) {
					first = false;
				} else {
					pythonCodeBuilder.append(",");
				}
				pythonCodeBuilder.append(paramName + "=");
				String paramValue = ci.getParameterValues().get(paramName);
				try {
					Double.valueOf(paramValue);
					pythonCodeBuilder.append(ci.getParameterValues().get(paramName));
				} catch (Exception e) {
					pythonCodeBuilder.append("\"" + ci.getParameterValues().get(paramName) + "\"");
				}
			}
			if (ci.getSatisfactionOfRequiredInterfaces().containsKey("estimator")) {
				Map<String, String> parameterComponent = this.generatePythonCodeFromComponentInstance(ci.getSatisfactionOfRequiredInterfaces().get("estimator"));
				if (!first) {
					pythonCodeBuilder.append(",");
				}
				pythonCodeBuilder.append("estimator=" + parameterComponent.get("pipeline"));
				importStatement += parameterComponent.get("import");
			}
			pythonCodeBuilder.append(")");
			break;
		}
		}

		parsedCodeMap.put("import", importStatement);
		parsedCodeMap.put("pipeline", pythonCodeBuilder.toString());

		return parsedCodeMap;
	}

}
