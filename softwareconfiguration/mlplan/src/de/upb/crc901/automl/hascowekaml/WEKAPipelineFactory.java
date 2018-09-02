package de.upb.crc901.automl.hascowekaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import hasco.model.ComponentInstance;
import hasco.query.Factory;
import jaicore.basic.ListHelper;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import jaicore.ml.multioptionsingleenhancer.*;

public class WEKAPipelineFactory implements Factory<MLPipeline> {

	@Override
	public MLPipeline getComponentInstantiation(final ComponentInstance groundComponent) throws Exception {

		ComponentInstance preprocessorCI = null;
		String ppName = "";
		ComponentInstance classifierCI = null;
		
		/**Only used to check the class of the classifier of the ComponentIntance */
		Classifier instanciatedClassifier = null;

		switch (groundComponent.getComponent().getName()) {
		case "pipeline": {
			/* Retrieve component instances of pipeline */
			preprocessorCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("preprocessor");
			ppName = preprocessorCI.getComponent().getName();

			classifierCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier");
			break;
		}
		default: {
			classifierCI = groundComponent;
			break;
		}
		}

		ASEvaluation eval = null;
		ASSearch search = null;
		if (ppName.startsWith("weka")) {
			ComponentInstance evaluatorCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("eval");
			ComponentInstance searcherCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("search");

			eval = ASEvaluation.forName(evaluatorCI.getComponent().getName(),
					this.getParameterList(evaluatorCI).toArray(new String[] {}));
			search = ASSearch.forName(searcherCI.getComponent().getName(),
					this.getParameterList(searcherCI).toArray(new String[] {}));
		}
		
		/**Instantiate the classifier of the ComponentInstance if there is one specified.
		 * This is needed due to the fact, that we have to check later, if the classifier is a MultiClassifiersCombiner,
		 * because in this case we run the subroutine of the MetaBuildingBlock System
		 */
		if(!(groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier") == null))
		{
			instanciatedClassifier = AbstractClassifier.forName(groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier")
					.toString().substring(groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier").toString()
					.indexOf(":") + 1, groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier").toString()
					.indexOf("(")), null);
			
		}
		
		classifierCI.getParameterValues();
		List<String> parameters = this.getParameterList(classifierCI);
		
		/**Check if the class that is represented by the ComponentInstance is a MultiClassifiersCombiner to run the subroutine of
		 * the MetaBuildingBlock System, to calculate the parameters for the base classifiers
		 */
		if(instanciatedClassifier instanceof weka.classifiers.MultipleClassifiersCombiner) {
			List<String> subparameters = this.convertMetaBuildingBlocksToOptions(classifierCI);
			parameters = subparameters;
		}

		Classifier c = AbstractClassifier.forName(classifierCI.getComponent().getName(),
				parameters.toArray(new String[] {}));
		
		return new MLPipeline(search, eval, c);
	}
	
	/**
	 * Takes a ComponentInstance object and calculates for a meta classifier the specifications of its base classifiers
	 * 
	 * Note that this method should only be called for ComponentInstance that represent classes that inherit from weka.classifiers.MultipleClassifiersCombiner,
	 * due to the fact that the option -B for base classifiers has to exists. Furthermore the ComponentInstance needs to support the MetaBuildingBlock System,
	 * otherwise this method is likely to just terminate with an Exception
	 * 
	 * @param classifierCI ComponentInstance that represents am MultiClassifiersCombiner
	 * @return List of parameters that specify the base classifiers and their specifications of a MultiClassifiersCombiner
	 */
	private List<String> convertMetaBuildingBlocksToOptions(ComponentInstance classifierCI) throws Exception{
		
		ComponentInstance currentMetaBuildingBlockToAnalyze = classifierCI;
		ComponentInstance currentBaseClassifierToAnalyze;
		List<String> subparameters = new ArrayList<String>();
		
		currentMetaBuildingBlockToAnalyze.getParameterValues();
		List<String> metaParameters = new ArrayList<String>();
		metaParameters.addAll(this.getParameterList(currentMetaBuildingBlockToAnalyze));
		
		for(int i = 0; i < metaParameters.size(); i++) {
			if(metaParameters.get(i).equals("-MetaBuildingBlock") || metaParameters.get(i).equals("-MultiCombinerCompatibleClassifier")) {
				for(int j = metaParameters.size() - 1; j >= i; j--) {
					System.out.println(metaParameters.get(j));
					metaParameters.remove(j);
				}
				break;
			}
		}
		
		subparameters.addAll(metaParameters);
		
		while(currentMetaBuildingBlockToAnalyze != null && currentMetaBuildingBlockToAnalyze.getSatisfactionOfRequiredInterfaces()
				.containsKey("MultiCombinerCompatibleClassifier")) {
			
			currentBaseClassifierToAnalyze = currentMetaBuildingBlockToAnalyze.getSatisfactionOfRequiredInterfaces().get("MultiCombinerCompatibleClassifier");
			currentBaseClassifierToAnalyze.getParameterValues(); //ggf not neccessary
			List<String> baseClassifierParameters = this.getParameterList(currentBaseClassifierToAnalyze);
			String baseClassifierParameter = "";
			
			for(int i = 0; i < baseClassifierParameters.size(); i++) {
				baseClassifierParameter = baseClassifierParameter + baseClassifierParameters.get(i) + " ";
			}
			
			baseClassifierParameter = currentBaseClassifierToAnalyze.getComponent().getName() + " " + baseClassifierParameter;
			
			subparameters.add("-B");
			subparameters.add(baseClassifierParameter);
			
			currentMetaBuildingBlockToAnalyze = currentMetaBuildingBlockToAnalyze
					.getSatisfactionOfRequiredInterfaces().get("MetaBuildingBlock");
		}
		
		return subparameters;
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

}
