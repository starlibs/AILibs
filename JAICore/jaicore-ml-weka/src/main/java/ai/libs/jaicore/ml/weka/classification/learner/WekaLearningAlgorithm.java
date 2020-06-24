package ai.libs.jaicore.ml.weka.classification.learner;

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
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;

public class WekaLearningAlgorithm extends AAlgorithm<ILabeledDataset<?>, IWekaClassifier> implements IWekaLearningAlgorithm {

	private final Class<?> classifierClass;
	private IWekaClassifier builtModel;
	private FutureTask<?> futureTask;

	public WekaLearningAlgorithm(final Class<?> classifierClass, final ILabeledDataset<?> input) {
		super(input);
		this.classifierClass = classifierClass;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		ExecutorService executor = null;
		try {
			switch (this.getState()) {
			case CREATED:
				this.builtModel = new WekaClassifier(this.classifierClass.getName(), new String[] {});
				return this.activate();
			case ACTIVE:
				IWekaInstances dataset = new WekaInstances(this.getInput());
				this.futureTask = new FutureTask<>(() -> {
					this.builtModel.fit(dataset);
					return null;
				});
				executor = Executors.newSingleThreadExecutor();
				executor.submit(this.futureTask);
				this.futureTask.get(this.getRemainingTimeToDeadline().milliseconds() - 100, TimeUnit.MILLISECONDS);
				return this.terminate();
			default:
				throw new IllegalStateException("Don't know what to do in state " + this.getState() + ".");
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
		catch (Exception e) {
			throw new AlgorithmException("Could not build classifier.", e);
		}
		finally {
			if (executor != null) {
				executor.shutdownNow();
			}
		}
	}

	@Override
	public IWekaClassifier call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.builtModel;
	}

	@Override
	public void cancel() {
		this.futureTask.cancel(true);
		super.cancel();
	}
}
