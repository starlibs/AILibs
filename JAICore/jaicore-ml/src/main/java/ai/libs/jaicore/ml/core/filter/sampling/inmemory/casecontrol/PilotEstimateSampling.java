package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.HashMap;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;

public abstract class PilotEstimateSampling<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends CaseControlLikeSampling<I, D> {

	private Logger logger = LoggerFactory.getLogger(PilotEstimateSampling.class);

	protected int preSampleSize;
	private I chosenInstance = null;

	protected PilotEstimateSampling(final D input) {
		super(input);
	}

	public I getChosenInstance() {
		return this.chosenInstance;
	}

	public void setChosenInstance(final I chosenInstance) {
		this.chosenInstance = chosenInstance;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException {
		this.logger.info("Executing next step.");
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

	@SuppressWarnings("unchecked")
	private AlgorithmEvent doInitStep() throws AlgorithmException, InterruptedException {
		try {
			this.sample = (D) this.getInput().createEmptyCopy();
			if (this.probabilityBoundaries == null || this.chosenInstance == null) {
				ISingleLabelClassifier pilotEstimator = null;

				if (pilotEstimator == null) {
					/* this is currently all WEKA based stuff */
					throw new UnsupportedOperationException();
				}
				// set preSampleSize to |Dataset|/2 as default value, if preSampleSize would be
				// smaller than 1
				if (this.preSampleSize < 1) {
					this.preSampleSize = this.getInput().size() / 2;
				}
				D pilotEstimateSample = (D) this.getInput().createEmptyCopy();
				D sampleCopy = (D) this.getInput().createEmptyCopy();

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

				// String[] options = new String[2];
				// options[0] = "-R";
				// options[1] = "last";
				// numericToNominal.setOptions(options);
				// numericToNominal.setInputFormat(pilotEstimateSample);

				// pilotEstimateSample = Filter.useFilter(pilotEstimateSample, numericToNominal);

				ArrayList<Pair<Double, Double>> classMapping = new ArrayList<>();
				// boolean classNotInMapping;
				// for (I in : pilotEstimateInstances) {
				// classNotInMapping = true;
				// for (Pair<Double, Double> classPair : classMapping) {
				// if (in.classValue() == classPair.getX().doubleValue()) {
				// classNotInMapping = false;
				// }
				// }
				// if (classNotInMapping) {
				// classMapping.add(new Pair<Double, Double>(in.classValue(), (double) classMapping.size()));
				// }
				// }

				// pilotEstimator.fit(pilotEstimateInstances);
				// this.probabilityBoundaries = this.calculateFinalInstanceBoundaries(sampleCopy, pilotEstimator);
			}
		} catch (DatasetCreationException e1) {
			throw new AlgorithmException("Could not create a copy of the dataset.", e1);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new AlgorithmException("Unexpected error", e);
		}
		return this.activate();
	}

	abstract ArrayList<Pair<I, Double>> calculateFinalInstanceBoundaries(D instances, ISingleLabelClassifier pilotEstimator);
}
