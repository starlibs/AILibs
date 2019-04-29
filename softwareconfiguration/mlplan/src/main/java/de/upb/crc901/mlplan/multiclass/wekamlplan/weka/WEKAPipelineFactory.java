package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.MultipleClassifiersCombiner;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.core.OptionHandler;

public class WEKAPipelineFactory implements IClassifierFactory {

	@Override
	public Classifier getComponentInstantiation(final ComponentInstance groundComponent) throws ComponentInstantiationFailedException {
		try {
			if (groundComponent.getComponent().getName().equals("pipeline")) {
				ComponentInstance preprocessorCI = null;
				/* Retrieve component instances of pipeline */
				preprocessorCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("preprocessor");
				ComponentInstance evaluatorCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("eval");
				ComponentInstance searcherCI = preprocessorCI.getSatisfactionOfRequiredInterfaces().get("search");

				ASEvaluation eval = ASEvaluation.forName(evaluatorCI.getComponent().getName(), this.getParameterList(evaluatorCI).toArray(new String[0]));
				ASSearch search = ASSearch.forName(searcherCI.getComponent().getName(), this.getParameterList(searcherCI).toArray(new String[0]));

				Classifier c = this.getComponentInstantiation(groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier"));
				return new MLPipeline(search, eval, c);

			} else {
				Classifier c = AbstractClassifier.forName(groundComponent.getComponent().getName(), this.getParameterList(groundComponent).toArray(new String[0]));
				List<String> options = this.getParameterList(groundComponent);
				options.add("-do-not-check-capabilities");
				if (c instanceof OptionHandler) {
					((OptionHandler) c).setOptions(options.toArray(new String[0]));
				}

				for (Entry<String, ComponentInstance> reqI : groundComponent.getSatisfactionOfRequiredInterfaces().entrySet()) {
					switch (reqI.getKey()) {
					case "W":
						if (c instanceof SingleClassifierEnhancer) {
							((SingleClassifierEnhancer) c).setClassifier(this.getComponentInstantiation(reqI.getValue()));
						} else {
							System.err.println("Got required interface W but classifier " + c.getClass().getName() + "is not single classifier enhancer");
						}
						break;
					case "K":
						if (c instanceof SMO) {
							Kernel k = (Kernel) Class.forName(reqI.getValue().getComponent().getName()).newInstance();
							k.setOptions(this.getParameterList(reqI.getValue()).toArray(new String[0]));
							((SMO) c).setKernel(k);
						} else {
							System.err.println("Got required interface K but classifier " + c.getClass().getName() + "is not SMO");
						}
						break;
					case "B":
						List<Classifier> baseLearnerList = this.getListOfBaseLearners(reqI.getValue());
						if (c instanceof MultipleClassifiersCombiner) {
							((MultipleClassifiersCombiner) c).setClassifiers(baseLearnerList.toArray(new Classifier[0]));
						} else {
							System.err.println("Got required interface B but classifier " + c.getClass().getName() + " is not MultipleClassifiersCombiner");
						}
						break;
					default:
						System.err.println("Got required interface " + reqI.getKey() + " for classifier " + c.getClass().getName() + ". Dont know what to do with it...");
						break;
					}
				}
				return c;
			}
		} catch (Exception e) {
			throw new ComponentInstantiationFailedException(e, "Could not instantiate component.");
		}
	}

	private List<Classifier> getListOfBaseLearners(final ComponentInstance ci) throws ComponentInstantiationFailedException {
		List<Classifier> baseLearnerList = new LinkedList<>();

		if (ci.getComponent().getName().equals("MultipleBaseLearnerListElement")) {
			baseLearnerList.add(this.getComponentInstantiation(ci.getSatisfactionOfRequiredInterfaces().get("classifier")));
		} else if (ci.getComponent().getName().equals("MultipleBaseLearnerListChain")) {
			baseLearnerList.add(this.getComponentInstantiation(ci.getSatisfactionOfRequiredInterfaces().get("classifier")));
			baseLearnerList.addAll(this.getListOfBaseLearners(ci.getSatisfactionOfRequiredInterfaces().get("chain")));
		}

		return baseLearnerList;
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

		return parameters;
	}

}
