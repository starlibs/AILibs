package ai.libs.jaicore.basic.algorithm.reduction;

import java.util.NoSuchElementException;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.IAlgorithmFactory;
import org.api4.java.algorithm.ISolutionCandidateIterator;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.basic.algorithm.ASolutionCandidateIterator;

public class AReducingSolutionIterator<I1, O1, I2, O2> extends ASolutionCandidateIterator<I1, O1> {
	private Logger logger = LoggerFactory.getLogger(AReducingSolutionIterator.class);
	private String loggerName;

	/* algorithm inputs */
	private final AlgorithmicProblemReduction<I1, O1, I2, O2> problemTransformer;
	private final ISolutionCandidateIterator<I2, O2> baseAlgorithm;

	public AReducingSolutionIterator(final I1 problem, final AlgorithmicProblemReduction<I1, O1, I2, O2> problemTransformer, final IAlgorithmFactory<I2, O2, ?> baseFactory) {
		super(problem);
		this.problemTransformer = problemTransformer;
		this.baseAlgorithm = (ISolutionCandidateIterator<I2, O2>)baseFactory.getAlgorithm(problemTransformer.encodeProblem(problem));
	}

	@Override
	public final void cancel() {
		super.cancel();
		this.baseAlgorithm.cancel();
	}

	public void runPreCreationHook() {
		/* by default, this is an empty hook */
	}

	protected ISolutionCandidateFoundEvent<O1> getSolutionEvent(final O1 solution) {
		return new ASolutionCandidateFoundEvent<>(this, solution);
	}

	@Override
	public final IAlgorithmEvent nextWithException() throws AlgorithmExecutionCanceledException, InterruptedException, AlgorithmTimeoutedException, AlgorithmException {
		if (this.isCanceled()) {
			throw new IllegalStateException("The algorithm has already been canceled. Cannot conduct futŕther steps.");
		}

		switch (this.getState()) {
		case CREATED:
			this.runPreCreationHook();

			/* set timeout on base algorithm */
			Timeout to = this.getTimeout();
			this.logger.debug("Setting timeout of search to {}", to);
			this.baseAlgorithm.setTimeout(to);
			return this.activate();

		case ACTIVE:
			this.logger.info("Starting/continuing search for next plan.");
			try {
				O2 solution = this.baseAlgorithm.nextSolutionCandidate();
				if (solution == null) {
					this.logger.info("No more solutions will be found. Terminating algorithm.");
					return this.terminate();
				}
				this.logger.info("Next solution found.");
				O1 solutionToOriginalProlem = this.problemTransformer.decodeSolution(solution);
				ISolutionCandidateFoundEvent<O1> event = this.getSolutionEvent(solutionToOriginalProlem);
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
		if (this.baseAlgorithm instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of search to {}.base", name);
			((ILoggingCustomizable) this.baseAlgorithm).setLoggerName(name + ".base");
		}
		super.setLoggerName(this.loggerName + "._algorithm");
	}

	protected Logger getLogger() {
		return this.logger;
	}

	public AlgorithmicProblemReduction<I1, O1, I2, O2> getProblemTransformer() {
		return this.problemTransformer;
	}

	public IAlgorithm<I2, O2> getBaseAlgorithm() {
		return this.baseAlgorithm;
	}
}
