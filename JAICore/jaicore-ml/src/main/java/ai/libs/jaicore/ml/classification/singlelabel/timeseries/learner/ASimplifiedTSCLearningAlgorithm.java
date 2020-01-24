package ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.api4.java.algorithm.events.IAlgorithmEvent;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;

public abstract class ASimplifiedTSCLearningAlgorithm<T, C extends ASimplifiedTSClassifier<T>> extends AAlgorithm<TimeSeriesDataset2, C> {
	protected ASimplifiedTSCLearningAlgorithm(final IOwnerBasedAlgorithmConfig config, final C classifier, final TimeSeriesDataset2 input) {
		super(config, input);
		this.classifier = classifier; // this is the classifier that is being trained (and outputted in the end)
	}

	/**
	 * The model which is maintained during algorithm calls
	 */
	private final C classifier;

	public C getClassifier() {
		return this.classifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerListener(final Object listener) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAlgorithmEvent nextWithException() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<IAlgorithmEvent> iterator() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAlgorithmEvent next() {
		throw new NoSuchElementException("Cannot enumerate on this algorithm");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() {
		throw new UnsupportedOperationException();
	}
}
