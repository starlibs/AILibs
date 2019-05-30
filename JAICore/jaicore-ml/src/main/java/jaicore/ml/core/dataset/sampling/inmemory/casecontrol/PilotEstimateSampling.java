package jaicore.ml.core.dataset.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.ILabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;
import jaicore.ml.core.dataset.weka.WekaInstances;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public abstract class PilotEstimateSampling<I extends ILabeledAttributeArrayInstance<?>, D extends IDataset<I>> extends CaseControlLikeSampling<I, D> {

	private Logger logger = LoggerFactory.getLogger(PilotEstimateSampling.class);

	protected int preSampleSize;
	private I chosenInstance = null;

	protected PilotEstimateSampling(final D input) {
		super(input);
		if (!(input instanceof WekaInstances)) {
			throw new IllegalArgumentException("Pilot Estimate Sampling currently only works with WekaInstances. The signature is kept general to avoid refactoring later on.");
		}
	}

	public I getChosenInstance() {
		return this.chosenInstance;
	}

	public void setChosenInstance(final I chosenInstance) {
		this.chosenInstance = chosenInstance;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			this.doInitStep();
			break;
		case ACTIVE:
			if (this.sample.size() < this.sampleSize) {
				do {
					double r = this.rand.nextDouble();
					this.chosenInstance = null;
					for (int i = 0; i < this.probabilityBoundaries.size(); i++) {
						if (this.probabilityBoundaries.get(i).getY().doubleValue() > r) {
							this.chosenInstance = this.probabilityBoundaries.get(i).getX();
							break;
						}
					}
					if (this.chosenInstance == null) {
						this.chosenInstance = this.probabilityBoundaries.get(this.probabilityBoundaries.size() - 1).getX();
					}
				} while (this.sample.contains(this.chosenInstance));
				this.sample.add(this.chosenInstance);
				return new SampleElementAddedEvent(this.getId());
			} else {
				return this.terminate();
			}
		case INACTIVE:
			this.doInactiveStep();
			break;
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
		return null;
	}

	private AlgorithmEvent doInitStep() throws AlgorithmException {
		try {
			this.sample = (D) this.getInput().createEmpty();
			if (this.probabilityBoundaries == null || this.chosenInstance == null) {
				Classifier pilotEstimator = new Logistic();
				// set preSampleSize to |Dataset|/2 as default value, if preSampleSize would be
				// smaller than 1
				if (this.preSampleSize < 1) {
					this.preSampleSize = this.getInput().size() / 2;
				}
				D pilotEstimateSample = (D) this.getInput().createEmpty();
				D sampleCopy = (D) this.getInput().createEmpty();

				for (I instance : this.getInput()) {
					sampleCopy.add(instance);
				}

				HashMap<Object, Integer> classOccurrences = this.countClassOccurrences(sampleCopy);

				// Count number of classes
				int numberOfClasses = classOccurrences.keySet().size();

				// Calculate Boundaries that define which Instances is choose for which random
				// number
				this.probabilityBoundaries = this.calculateInstanceBoundaries(classOccurrences, numberOfClasses);

				double r;
				I choosenInstance;
				for (int i = 0; i < this.preSampleSize; i++) {
					do {
						r = this.rand.nextDouble();
						choosenInstance = null;
						for (int j = 0; j < this.probabilityBoundaries.size(); j++) {
							if (this.probabilityBoundaries.get(j).getY().doubleValue() > r) {
								choosenInstance = this.probabilityBoundaries.get(j).getX();
								break;
							}
						}
						if (choosenInstance == null) {
							choosenInstance = this.probabilityBoundaries.get(this.probabilityBoundaries.size() - 1).getX();
						}
					} while (pilotEstimateSample.contains(choosenInstance));
					pilotEstimateSample.add(choosenInstance);
				}
				Instances pilotEstimateInstances = ((WekaInstances<?>) pilotEstimateSample).getList();

				NumericToNominal numericToNominal = new NumericToNominal();
				String[] options = new String[2];
				options[0] = "-R";
				options[1] = "last";
				try {
					numericToNominal.setOptions(options);
					numericToNominal.setInputFormat(pilotEstimateInstances);
				} catch (Exception e) {
					this.logger.error("Unexpected error", e);
					this.terminate();
				}

				try {
					pilotEstimateInstances = Filter.useFilter(pilotEstimateInstances, numericToNominal);
				} catch (Exception e) {
					this.logger.error("Cannot apply filter", e);
					this.terminate();
				}

				ArrayList<Pair<Double, Double>> classMapping = new ArrayList<>();
				boolean classNotInMapping;
				for (Instance in : pilotEstimateInstances) {
					classNotInMapping = true;
					for (Pair<Double, Double> classPair : classMapping) {
						if (in.classValue() == classPair.getX().doubleValue()) {
							classNotInMapping = false;
						}
					}
					if (classNotInMapping) {
						classMapping.add(new Pair<Double, Double>(in.classValue(), (double) classMapping.size()));
					}
				}

				try {
					pilotEstimator.buildClassifier(pilotEstimateInstances);
				} catch (Exception e) {
					this.logger.error("Cannot build classifier", e);
					this.terminate();
				}
				this.probabilityBoundaries = this.calculateFinalInstanceBoundaries(sampleCopy, pilotEstimator);
			}
		} catch (DatasetCreationException e1) {
			throw new AlgorithmException(e1, "Could not create a copy of the dataset.");
		}
		return this.activate();
	}

	abstract ArrayList<Pair<I, Double>> calculateFinalInstanceBoundaries(D instances, Classifier pilotEstimator);
}
