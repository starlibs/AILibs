package ai.libs.jaicore.experiments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.experiments.exceptions.ExperimentDecodingException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.logging.LoggerUtil;

public class AlgorithmBenchmarker implements IExperimentSetEvaluator, ILoggingCustomizable {

	private class Caps<I, A extends IAlgorithm<? extends I, ?>> {
		private final IExperimentDecoder<I, A> decoder;

		public Caps(final IExperimentDecoder<I, A> decoder) {
			super();
			this.decoder = decoder;
		}
	}

	private final IExperimentRunController<?> controller;
	private final Caps<?, ?> caps;

	private Function<Experiment, Timeout> experimentSpecificTimeout;
	private Timeout timeout;

	private Logger logger = LoggerFactory.getLogger(AlgorithmBenchmarker.class);
	private Thread eventThread;

	public <I, A extends IAlgorithm<? extends I, ?>> AlgorithmBenchmarker(final IExperimentDecoder<I, A> decoder, final IExperimentRunController<?> controller) {
		this.caps = new Caps<>(decoder);
		this.controller = controller;
	}

	@Override
	public final void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException, InterruptedException {

		try {
			this.logger.info("Starting evaluation of experiment entry with keys {}", experimentEntry.getExperiment().getValuesOfKeyFields());

			/* get algorithm */
			IAlgorithm<?, ?> algorithm = this.caps.decoder.getAlgorithm(experimentEntry.getExperiment());
			this.logger.debug("Created algorithm {} of class {} for problem instance {}. Configuring logger name if possible.", algorithm, algorithm.getClass(), algorithm.getInput());
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName(this.getLoggerName() + ".algorithm");
			}

			/* set algorithm timeout */
			if (this.experimentSpecificTimeout != null) {
				Timeout to = this.experimentSpecificTimeout.apply(experimentEntry.getExperiment());
				algorithm.setTimeout(to);
				this.logger.info("Set algorithm timeout to experiment specific timeout: {}", to);
			}
			else if (this.timeout != null) {
				algorithm.setTimeout(this.timeout);
				this.logger.info("Set algorithm timeout to general timeout: {}", this.timeout);
			}

			final List<IEventBasedResultUpdater> resultUpdaters = this.controller.getResultUpdaterComputer(experimentEntry.getExperiment());
			resultUpdaters.forEach(ru -> ru.setAlgorithm(algorithm));
			final List<IExperimentTerminationCriterion> terminationCriteria = this.controller.getTerminationCriteria(experimentEntry.getExperiment());

			/* create thread to process events */
			BlockingQueue<IAlgorithmEvent> eventQueue = new LinkedBlockingQueue<>();
			this.eventThread = new Thread(() -> {

				while (!Thread.currentThread().isInterrupted()) {
					IAlgorithmEvent e;
					try {
						e = eventQueue.take();
					} catch (InterruptedException e1) {
						break;
					}

					/* update result */
					final Map<String, Object> results = new HashMap<>();
					for (IEventBasedResultUpdater updater : resultUpdaters) {
						updater.processEvent(e, results);
					}
					if (!results.isEmpty()) {
						processor.processResults(results);
					}

					/* check whether one of the termination criteria is satisfied */
					if (terminationCriteria.stream().anyMatch(c -> c.doesTerminate(e, algorithm))) {
						this.logger.info("Stopping algorithm execution, because termination criterion fired.");
						algorithm.cancel();
						return;
					}
				}
			}, "Experiment Event Processor");
			this.eventThread.start();

			algorithm.registerListener(new Object() {

				@Subscribe
				public void receiveEvent(final IAlgorithmEvent e) {
					eventQueue.add(e);
				}
			});

			/* run algorithm */
			Thread t = new Thread(() -> {
				try {
					this.logger.info("Running call method on {}", algorithm);
					algorithm.call();
				} catch (AlgorithmExecutionCanceledException e) {
					this.logger.info("CANCEL");
					/* this may just happen */
				} catch (Exception e) {
					this.logger.error(LoggerUtil.getExceptionInfo(e));
				}
			});
			t.start();
			t.join();

			/* finish updaters, and update ultimate results */
			final Map<String, Object> results = new HashMap<>();
			for (IEventBasedResultUpdater updater : resultUpdaters) {
				updater.finish(results);
			}
			if (!results.isEmpty()) {
				processor.processResults(results);
			}
		} catch (ExperimentDecodingException e1) {
			throw new ExperimentEvaluationFailedException(e1);
		} finally {
			if (this.eventThread != null) {
				this.eventThread.interrupt();
				this.eventThread = null;
			}
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Loggername is now {}", name);
	}

	public Timeout getTimeout() {
		return this.timeout;
	}

	public void setTimeout(final Timeout timeout) {
		this.timeout = timeout;
	}

	public Function<Experiment, Timeout> getExperimentSpecificTimeout() {
		return this.experimentSpecificTimeout;
	}

	public void setExperimentSpecificTimeout(final Function<Experiment, Timeout> experimentSpecificTimeout) {
		this.experimentSpecificTimeout = experimentSpecificTimeout;
	}
}
