package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

public class SampleComplementComputer {

	/**
	 * Gets the data point contained in the original data that are not part of the
	 * @return
	 * @throws DatasetCreationException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public <I extends IInstance, D extends IDataset<I>> D getComplement(final D originalData, final D sample) throws DatasetCreationException, InterruptedException {

		if (sample == null) {
			throw new IllegalStateException("Sample computation has not started yet.");
		}

		if (originalData.isEmpty()) {
			throw new IllegalArgumentException("Cannot compute complement of an empty base set!");
		}

		/* compute frequencies (necessary, because items could occur several times) */
		Map<Object, Integer> frequenciesInInput = new HashMap<>();
		Map<Object, Integer> frequenciesInSubSample = new HashMap<>();
		Map<Object, Integer> frequenciesInComplement = new HashMap<>();

		for (Object instance : originalData) {
			frequenciesInInput.put(instance, frequenciesInInput.computeIfAbsent(instance, k -> 0) + 1);
			frequenciesInComplement.put(instance, 0);
			frequenciesInSubSample.put(instance, 0);
		}
		for (Object instance : sample) {
			frequenciesInSubSample.put(instance, frequenciesInSubSample.computeIfAbsent(instance, k -> 0) + 1); // inserts 0 if, for some reason, the value has not been defined before
		}

		/* now compute complement */
		D complement = (D) originalData.createEmptyCopy();
		for (I instance : originalData) {
			int frequencyInComplement = frequenciesInComplement.get(instance);
			if (frequenciesInSubSample.get(instance) + frequencyInComplement < frequenciesInInput.get(instance)) {
				complement.add(instance);
				frequenciesInComplement.put(instance, frequencyInComplement + 1);
			}
		}

		/* check plausibility (sizes should sum up) */
		if (sample.size() + complement.size() != originalData.size()) {
			throw new IllegalStateException("The input set of size " + originalData.size() + " has been reduced to " + sample.size() + " + " + complement.size() + ". This is not plausible.");
		} else {
			for (Entry<Object, Integer> instanceWithFrequency : frequenciesInInput.entrySet()) {
				Object inst = instanceWithFrequency.getKey();
				int frequencyNow = frequenciesInSubSample.get(inst) + frequenciesInComplement.get(inst);
				if (instanceWithFrequency.getValue() != frequencyNow) {
					throw new IllegalStateException("Frequency of instance " + inst + " was " + instanceWithFrequency.getValue() + " but is now " + frequencyNow);
				}
			}
		}
		return complement;
	}
}
