package de.upb.crc901.mlplan.multilabel;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.functions.SMO;
import weka.core.OptionHandler;

/**
* A pipeline factory that converts a given ComponentInstance that consists of
* components that correspond to MEKA algorithms to a MultiLabelClassifier.
*
*/
public class MekaPipelineFactory implements IClassifierFactory {

	/* loggin */
	private static final Logger logger = LoggerFactory.getLogger(MekaPipelineFactory.class);

	@Override
	public Classifier getComponentInstantiation(final ComponentInstance groundComponent) throws ComponentInstantiationFailedException {
		return this.convertGroundComponentToMLClassifier(groundComponent);
	}

	/**
	 * Converts the given ComponentInstance to a MultiLabelClassifier.
	 *
	 * @param groundComponent
	 *            the ComponentInstance to convert
	 * @return a new MultiLabelClassifier
	 */
	private Classifier convertGroundComponentToMLClassifier(final ComponentInstance groundComponent) throws ComponentInstantiationFailedException {

		/* collect basic information about the component */
		Component component = groundComponent.getComponent();
		Map<String, String> paramValues = groundComponent.getParameterValues();
		String className = component.getName();

		/* now try to create an object of this component */
		try {
			List<String> params = new LinkedList<>();
			for (Parameter p : component.getParameters()) {
				if (paramValues.containsKey(p.getName())) {
					String value = paramValues.get(p.getName());
					/* ignore activator params, which are only used to control the search and if this is a boolean flag and the value is false, omit it */
					if (p.getName().toLowerCase().contains("activator") || value.equals("false")) {
						continue;
					}

					if (className.contains("meka")) {
						params.add(p.getName().replaceAll("_", "-"));
					} else if (className.contains("weka")) {
						params.add("-" + p.getName());
					}

					/* if this is a boolean flag and the value is positive, just add the name */
					if (!value.equals("true")) {
						params.add(value);
					}
				}
			}

			/* Form param list as array for weka. */
			String[] paramsAsArray = new String[params.size()];
			for (int i = 0; i < params.size(); i++) {
				paramsAsArray[i] = params.get(i);
			}

			Classifier c = (Classifier) Class.forName(className).newInstance();
			if (c instanceof OptionHandler) {
				try {
					((OptionHandler) c).setOptions(paramsAsArray);
				} catch (Exception e) {
					logger.error("Invalid option array for classifier {}: {}.", className, params, e);
					throw new ComponentInstantiationFailedException(e, "Invalid option array for classifier " + className + ": " + params);
				}
			}

			/* if this is an enhanced classifier, set its base classifier */
			if (component.getRequiredInterfaces().size() > 1) {
				throw new IllegalArgumentException("This factory can currently only handle at most one required interface per component");
			}
			if (component.getRequiredInterfaces().size() == 1) {
				if ((c instanceof SingleClassifierEnhancer)) {
					SingleClassifierEnhancer cc = (SingleClassifierEnhancer) c;
					ComponentInstance baseClassifierCI = groundComponent.getSatisfactionOfRequiredInterfaces().values().iterator().next(); // there is only one required interface
					if (baseClassifierCI == null) {
						throw new IllegalStateException("The required interface \"Classifier\" of component " + groundComponent.getComponent().getName() + " has not been satisifed!");
					}
					Classifier baseClassifier = this.convertGroundComponentToMLClassifier(baseClassifierCI);
					cc.setClassifier(baseClassifier);
				} else if (c instanceof SMO) {
					ComponentInstance kernel = groundComponent.getSatisfactionOfRequiredInterfaces().get("K");// there is only one required interface

					StringBuilder kernelSB = new StringBuilder();
					kernelSB.append(kernel.getComponent().getName());
					for (Entry<String, String> kernelParamValue : kernel.getParameterValues().entrySet()) {
						kernelSB.append(" -" + kernelParamValue.getKey());
						kernelSB.append(" " + kernelParamValue.getValue());
					}
					params.add("-K");
					params.add(kernelSB.toString());
					((SMO) c).setOptions(params.toArray(new String[0]));
				} else {
					throw new IllegalArgumentException("Required interfaces are currently only supported for SingleClassifierInhancer or SMO objects (and the base classifier must be their required interface). The presented class "
							+ c.getClass().getName() + " does not satisfy this requirement.");
				}
			}

			return c;

		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			logger.error("Could not find a class with class name {}", className);
			throw new ComponentInstantiationFailedException(e, "Could not find a class with class name " + className);
		} catch (Exception e) {
			throw new ComponentInstantiationFailedException(e, "Could not instantiate component instance.");
		}
	}

}