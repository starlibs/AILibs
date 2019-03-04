package de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multiclass.wekamlplan.ClassifierFactory;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import jaicore.basic.ILoggingCustomizable;
import jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import weka.classifiers.Classifier;

/**
 * The SKLearnClassifierFactory takes a ground component instance and parses it into a <code>ScikitLearnWrapper</code> as defined in the project jaicore-ml.
 * This factory may be used in the context of HASCO, especially for ML-Plan.
 *
 * @author wever
 */
public class SKLearnClassifierFactory implements ClassifierFactory, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(SKLearnClassifierFactoryTest.class);
	private String loggerName;

	@Override
	public Classifier getComponentInstantiation(final ComponentInstance groundComponent) throws ComponentInstantiationFailedException {
		this.logger.info("Parse ground component instance {} to ScikitLearnWrapper object.", groundComponent);
		System.err.println("Juhu " + groundComponent);

		StringBuilder constructInstruction = new StringBuilder();
		constructInstruction.append(this.extractSKLearnConstructInstruction(groundComponent));
		StringBuilder imports = new StringBuilder();

		System.exit(0);
		try {
			return new ScikitLearnWrapper(constructInstruction.toString(), imports.toString());
		} catch (IOException e) {
			this.logger.error("Could not create sklearn wrapper for construction {} and imports {}.", constructInstruction, imports);
			return null;
		}
	}

	public String extractSKLearnConstructInstruction(final ComponentInstance groundComponent) {
		return null;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger.debug("Switching logger name to {}", name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.debug("Switched SKLearnClassifierFactory logger to {}", name);
	}

}
