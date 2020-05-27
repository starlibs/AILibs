package ai.libs.jaicore.ml.weka.preprocessing;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;

public class WekaPreprocessorFitter extends AAlgorithm<ILabeledDataset<?>, IWekaPreprocessingAlgorithm> implements IWekaPreprocessingAlgorithm {

	private final ASSearch searcher;
	private final ASEvaluation evaluator;
	private final AttributeSelection as;
	private FutureTask<?> futureTask;

	public WekaPreprocessorFitter(final ILabeledDataset<?> input, final String searcher, final String evaluator) {
		super(input);
		try {
			this.searcher = ASSearch.forName(searcher, null);
			this.evaluator = ASEvaluation.forName(evaluator, null);
			this.as = new AttributeSelection();
			this.as.setSearch(this.searcher);
			this.as.setEvaluator(this.evaluator);
		} catch (Exception e) {
			throw new UnsupportedOperationException("Cannot instantiate a pre-processor with " + searcher + " and " + evaluator);
		}
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		ExecutorService executor = null;
		try {
			switch (this.getState()) {
			case CREATED:
				return this.activate();
			case ACTIVE:
				this.futureTask = new FutureTask<>(() -> {
					this.as.SelectAttributes(new WekaInstances(this.getInput()).getInstances());
					return null;
				});
				executor = Executors.newSingleThreadExecutor();
				executor.submit(this.futureTask);
				this.futureTask.get(this.getRemainingTimeToDeadline().milliseconds() - 100, TimeUnit.MILLISECONDS);
				return this.terminate();
			default:
				throw new IllegalStateException();
			}
		}
		catch (TimeoutException e) {
			throw new AlgorithmTimeoutedException(0);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (CancellationException e) {
			throw new AlgorithmExecutionCanceledException(0);
		}
		catch(Exception e) {
			throw new AlgorithmException("Could not build model.", e);
		}
		finally {
			if (executor != null) {
				executor.shutdownNow();
			}
		}
	}

	@Override
	public IWekaPreprocessingAlgorithm call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this;
	}

	public AttributeSelection getSelector() {
		return this.as;
	}

	@Override
	public void cancel() {
		this.futureTask.cancel(true);
		super.cancel();
	}
}
