package ai.libs.mlplan.multiclass.wekamlplan.weka;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IParameterDomain;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline;
import ai.libs.mlplan.core.ILearnerFactory;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.meta.Stacking;
import weka.core.OptionHandler;

public class WekaPipelineFactory implements ILearnerFactory<IWekaClassifier> {

	private Logger logger = LoggerFactory.getLogger(WekaPipelineFactory.class);

	private static final String L_CLASSIFIER = "classifier";

	@Override
	public IWekaClassifier getComponentInstantiation(final IComponentInstance groundComponent) throws ComponentInstantiationFailedException {
		this.logger.debug("Instantiate weka classifier from component instance {}.", groundComponent);
		try {
			if (groundComponent.getComponent().getName().equals("pipeline")) {
				IComponentInstance preprocessorCI = null;
				/* Retrieve component instances of pipeline */
				preprocessorCI = groundComponent.getSatisfactionOfRequiredInterface("preprocessor").iterator().next();
				IComponentInstance evaluatorCI = preprocessorCI.getSatisfactionOfRequiredInterface("eval").iterator().next();
				IComponentInstance searcherCI = preprocessorCI.getSatisfactionOfRequiredInterface("search").iterator().next();

				ASEvaluation eval = ASEvaluation.forName(evaluatorCI.getComponent().getName(), this.getParameterList(evaluatorCI).toArray(new String[0]));
				ASSearch search = ASSearch.forName(searcherCI.getComponent().getName(), this.getParameterList(searcherCI).toArray(new String[0]));

				IWekaClassifier c = this.getComponentInstantiation(groundComponent.getSatisfactionOfRequiredInterface(L_CLASSIFIER).iterator().next());
				this.logger.debug("Returning a MLPipeline object (aseval: {}, assearch: {}, classifier: {})", eval != null, search != null, c != null);
				return new WekaClassifier(new MLPipeline(search, eval, c.getClassifier()));

			} else {
				Classifier c = AbstractClassifier.forName(groundComponent.getComponent().getName(), this.getParameterList(groundComponent).toArray(new String[0]));
				List<String> options = this.getParameterList(groundComponent);
				options.add("-do-not-check-capabilities");
				if (c instanceof OptionHandler) {
					((OptionHandler) c).setOptions(options.toArray(new String[0]));
				}

				for (Entry<String, List<IComponentInstance>> reqI : groundComponent.getSatisfactionOfRequiredInterfaces().entrySet()) {
					switch (reqI.getKey()) {
					case "W":
						if (c instanceof SingleClassifierEnhancer) { // suppose that this defines a base classifier
							((SingleClassifierEnhancer) c).setClassifier(this.getComponentInstantiation(reqI.getValue().iterator().next()).getClassifier());
						} else {
							this.logger.error("Got required interface W but classifier {} is not single classifier enhancer", c.getClass().getName());
						}
						break;
					case "K":
						if (c instanceof SMO) {
							IComponentInstance kernel = reqI.getValue().iterator().next();
							Kernel k = (Kernel) Class.forName(kernel.getComponent().getName()).newInstance();
							k.setOptions(this.getParameterList(kernel).toArray(new String[0]));
							((SMO) c).setKernel(k);
						} else {
							this.logger.error("Got required interface K but classifier {} is not SMO", c.getClass().getName());
						}
						break;
					case "B": // suppose that this defines a base classifier
						Classifier baseClassifier = this.getComponentInstantiation(reqI.getValue().iterator().next()).getClassifier();
						if (c instanceof Stacking) {
							((Stacking) c).setClassifiers(new Classifier[] { baseClassifier });
						} else {
							this.logger.error("Unsupported option B for classifier {}", c.getClass().getName());
						}
						break;
					default:
						this.logger.error("Got required interface {} for classifier {}. Dont know what to do with it...", reqI.getKey(), c.getClass().getName());
						break;
					}
				}
				return new WekaClassifier(c);
			}
		} catch (Exception e) {
			throw new ComponentInstantiationFailedException(e, "Could not instantiate component.");
		}
	}

	private List<IWekaClassifier> getListOfBaseLearners(final IComponentInstance ci) throws ComponentInstantiationFailedException {
		List<IWekaClassifier> baseLearnerList = new LinkedList<>();

		if (ci.getComponent().getName().equals("MultipleBaseLearnerListElement")) {
			baseLearnerList.add(this.getComponentInstantiation(ci.getSatisfactionOfRequiredInterface(L_CLASSIFIER).iterator().next()));
		} else if (ci.getComponent().getName().equals("MultipleBaseLearnerListChain")) {
			baseLearnerList.add(this.getComponentInstantiation(ci.getSatisfactionOfRequiredInterface(L_CLASSIFIER).iterator().next()));
			baseLearnerList.addAll(this.getListOfBaseLearners(ci.getSatisfactionOfRequiredInterface("chain").iterator().next()));
		}

		return baseLearnerList;
	}

	private List<String> getParameterList(final IComponentInstance ci) {
		List<String> parameters = new LinkedList<>();

		for (Entry<String, String> parameterValues : ci.getParameterValues().entrySet()) {

			IParameter param = ci.getComponent().getParameter(parameterValues.getKey());
			boolean isDefault = param.isDefaultValue(parameterValues.getValue());

			if (parameterValues.getKey().toLowerCase().endsWith("activator") || parameterValues.getValue().equals("REMOVED") || isDefault) {
				continue;
			}

			if (!parameterValues.getValue().equals("false")) {
				parameters.add("-" + parameterValues.getKey());
			}

			IParameterDomain domain = ci.getComponent().getParameter(parameterValues.getKey()).getDefaultDomain();
			if (parameterValues.getValue() != null && !parameterValues.getValue().equals("") && !parameterValues.getValue().equals("true") && !parameterValues.getValue().equals("false")) {
				if (domain instanceof NumericParameterDomain && ((NumericParameterDomain) domain).isInteger()) {
					parameters.add((int) Double.parseDouble(parameterValues.getValue()) + "");
				} else {
					parameters.add(parameterValues.getValue());
				}
			}
		}

		return parameters;
	}
}
