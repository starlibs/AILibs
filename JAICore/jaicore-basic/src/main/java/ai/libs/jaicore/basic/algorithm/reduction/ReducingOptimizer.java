package ai.libs.jaicore.basic.algorithm.reduction;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.ILoggingCustomizable;
import ai.libs.jaicore.basic.ScoredItem;
import ai.libs.jaicore.basic.TimeOut;
import ai.libs.jaicore.basic.algorithm.AOptimizer;
import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.IOptimizationAlgorithm;
import ai.libs.jaicore.basic.algorithm.IOptimizationAlgorithmFactory;
import ai.libs.jaicore.basic.algorithm.events.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;

public class ReducingOptimizer<I1, O1 extends ScoredItem<V>, I2, O2 extends ScoredItem<V>, V extends Comparable<V>> extends AOptimizer<I1, O1, V> {
	private Logger logger = LoggerFactory.getLogger(ReducingOptimizer.class);
	private String loggerName;

	/* algorithm inputs */
	private final AlgorithmicProblemReduction<I1, O1, I2, O2> problemTransformer;
	private final IOptimizationAlgorithm<I2, O2, V> baseOptimizer;

	public ReducingOptimizer(final I1 problem, final AlgorithmicProblemReduction<I1, O1, I2, O2> problemTransformer, final IOptimizationAlgorithmFactory<I2, O2, V> baseFactory) {
		super(problem);
		this.problemTransformer = problemTransformer;
		this.baseOptimizer = baseFactory.getAlgorithm(problemTransformer.encodeProblem(problem));
	}

	@Override
	public final void cancel() {
		super.cancel();
		this.baseOptimizer.cancel();
	}

	public void runPreCreationHook() {
		/* by default, this is an empty hook */
	}

	protected SolutionCandidateFoundEvent<O1> getSolutionEvent(final O1 solution) {
		return new ASolutionCandidateFoundEvent<>(this.getId(), solution);
	}

	@Override
	public final AlgorithmEvent nextWithException() throws AlgorithmExecutionCanceledException, InterruptedException, AlgorithmTimeoutedException, AlgorithmException {
		if (this.isCanceled()) {
			throw new IllegalStateException("The algorithm has already been canceled. Cannot conduct futŕther steps.");
		}

		switch (this.getState()) {
		case CREATED:
			this.runPreCreationHook();

			/* set timeout on base algorithm */
			TimeOut to = this.getTimeout();
			this.logger.debug("Setting timeout of search to {}", to);
			this.baseOptimizer.setTimeout(to);
			return this.activate();

		case ACTIVE:
			this.logger.info("Starting/continuing search for next plan.");
			try {
				O2 solution = this.baseOptimizer.nextSolutionCandidate();
				if (solution == null) {
					this.logger.info("No more solutions will be found. Terminating algorithm.");
					return this.terminate();
				}
				this.logger.info("Next solution found.");
				O1 solutionToOriginalProlem = this.problemTransformer.decodeSolution(solution);
				SolutionCandidateFoundEvent<O1> event = this.getSolutionEvent(solutionToOriginalProlem);
				this.post(event);
				return event;
			} catch (NoSuchElementException e) { // if no more solution exists, terminate
				return this.terminate();
			}

		default:
			throw new IllegalStateException("Don't know what to do in state " + this.getState());
		}
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		if (this.problemTransformer instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of problem transformer to {}.problemtransformer", name);
			((ILoggingCustomizable) this.problemTransformer).setLoggerName(name + ".problemtransformer");
		}
		if (this.baseOptimizer instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of search to {}.base", name);
			((ILoggingCustomizable) this.baseOptimizer).setLoggerName(name + ".base");
		}
		super.setLoggerName(this.loggerName + "._algorithm");
	}

	protected Logger getLogger() {
		return this.logger;
	}

	public AlgorithmicProblemReduction<I1, O1, I2, O2> getProblemTransformer() {
		return this.problemTransformer;
	}

	public IOptimizationAlgorithm<I2, O2, V> getBaseOptimizer() {
		return this.baseOptimizer;
	}
}
