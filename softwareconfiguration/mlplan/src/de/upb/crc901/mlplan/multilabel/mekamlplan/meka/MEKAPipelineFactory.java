package de.upb.crc901.mlplan.multilabel.mekamlplan.meka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multilabel.mekamlplan.MultiLabelClassifierFactory;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.functions.SMO;
import weka.core.OptionHandler;

/**
 * A pipeline factory that converts a given ComponentInstance that consists of
 * components that correspond to MEKA algorithms to a MultiLabelClassifier.
 *
 */
public class MEKAPipelineFactory implements MultiLabelClassifierFactory {

	private static final Logger logger = LoggerFactory.getLogger(MEKAPipelineFactory.class);

	@Override
	public MultiLabelClassifier getComponentInstantiation(ComponentInstance groundComponent) {
		return (MultiLabelClassifier) convertGroundComponentToMLClassifier(groundComponent);
	}

	/**
	 * Converts the given ComponentInstance to a MultiLabelClassifier.
	 * 
	 * @param groundComponent
	 *            the ComponentInstance to convert
	 * @return a new MultiLabelClassifier
	 */
	private Classifier convertGroundComponentToMLClassifier(ComponentInstance groundComponent) {

		/* collect basic information about the component */
		Component component = groundComponent.getComponent();
		Map<String, String> paramValues = groundComponent.getParameterValues();
		String className = component.getName();

		/* now try to create an object of this component */
		try {
			List<String> params = new ArrayList<>();
			for (Parameter p : component.getParameters()) {
				if (paramValues.containsKey(p.getName())) {

					/* ignore activator params, which are only used to control the search */
					if (p.getName().contains("Activator"))
						continue;

					/* if this is a boolean flag and the value is false, omit it */
					String value = paramValues.get(p.getName());
					if (value.equals("false"))
						continue;

					if (className.contains("meka"))
						params.add(p.getName().replaceAll("_", "-"));
					else if (className.contains("weka"))
						params.add("-" + p.getName());

					/* if this is a boolean flag and the value is positive, just add the name */
					if (!value.equals("true"))
						params.add(value);
				}
			}
			String[] paramsAsArray = params.toArray(new String[] {});
			Classifier c = (Classifier) Class.forName(className).newInstance();
			if (c instanceof OptionHandler) {
				try {
					((OptionHandler) c).setOptions(paramsAsArray);
				} catch (Exception e) {
					logger.error("Invalid option array for classifier {}: {}. Exception: {}. Error message: {}",
							className, params, e.getClass().getName(), e.getMessage());
				}
			}

			/* if this is an enhanced classifier, set its base classifier */
			if (component.getRequiredInterfaces().size() > 1)
				throw new IllegalArgumentException(
						"This factory can currently only handle at most one required interface per component");
			if (component.getRequiredInterfaces().size() == 1) {
				if ((c instanceof SingleClassifierEnhancer)) {
					SingleClassifierEnhancer cc = (SingleClassifierEnhancer) c;
					ComponentInstance baseClassifierCI = groundComponent.getSatisfactionOfRequiredInterfaces().values()
							.iterator().next(); // there is only one required interface
					if (baseClassifierCI == null)
						throw new IllegalStateException("The required interface \"Classifier\" of component "
								+ groundComponent.getComponent().getName() + " has not been satisifed!");
					Classifier baseClassifier = convertGroundComponentToMLClassifier(baseClassifierCI);
					cc.setClassifier(baseClassifier);
				} else if (c instanceof SMO) {
					ComponentInstance kernel = groundComponent.getSatisfactionOfRequiredInterfaces().values().iterator().next(); // there is only one required interface
					System.out.println("Kernel " + kernel);
				} else
					throw new IllegalArgumentException(
							"Required interfaces are currently only supported for SingleClassifierInhancer or SMO objects (and the base classifier must be their required interface). The presented class "
									+ c.getClass().getName() + " does not satisfy this requirement.");
			}

			return c;

		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			logger.error("Could not find a class with class name {}", className);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
