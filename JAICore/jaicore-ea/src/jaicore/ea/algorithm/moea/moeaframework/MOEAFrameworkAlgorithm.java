package jaicore.ea.algorithm.moea.moeaframework;

import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.moeaframework.algorithm.StandardAlgorithms;
import org.moeaframework.core.Algorithm;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.DelayedCancellationCheckException;
import jaicore.basic.algorithm.exceptions.DelayedTimeoutCheckException;
import jaicore.ea.algorithm.AEvolutionaryAlgorithm;
import jaicore.ea.algorithm.moea.moeaframework.event.MOEAFrameworkAlgorithmResultEvent;

public class MOEAFrameworkAlgorithm extends AEvolutionaryAlgorithm {

	private Algorithm algorithm;
	private int numberOfGenerationsEvolved = 0;

	public MOEAFrameworkAlgorithm(final IMOEAFrameworkAlgorithmConfig config, final IMOEAFrameworkAlgorithmInput input) {
		super(config, input);
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		try {
			this.checkTermination();
		} catch (DelayedTimeoutCheckException e) {
			e.printStackTrace();
		} catch (DelayedCancellationCheckException e) {
			throw new TimeoutException(e.getMessage());
		}

		switch (this.getState()) {
		case created:
			// initialize population
			StandardAlgorithms sa = new StandardAlgorithms();
			Properties properties = new Properties();
			this.algorithm = sa.getAlgorithm(this.getConfig().algorithmName().toString(), properties, this.getInput().getProblem());
			this.algorithm.step();
			return super.activate();
		case active:
			this.algorithm.step();
			return new MOEAFrameworkAlgorithmResultEvent(this.getCurrentResult());
		default:
		case inactive:
			throw new AlgorithmException("The current algorithm state is >inactive<.");
		}

	}

	public MOEAFrameworkAlgorithmResult getCurrentResult() {
		this.algorithm.getResult();
		return new MOEAFrameworkAlgorithmResult(this.algorithm.getResult());
	}

	public int getNumberOfGenerationsEvolved() {
		return this.numberOfGenerationsEvolved;
	}

	public int getNumberOfEvaluations() {
		if (this.algorithm == null) {
			return 0;
		} else {
			return this.algorithm.getNumberOfEvaluations();
		}
	}

	@Override
	public MOEAFrameworkAlgorithmResult call() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		while ((this.getState() == AlgorithmState.created || this.getState() == AlgorithmState.active) && (this.getConfig().numberOfGenerations() <= 0 || this.numberOfGenerationsEvolved < this.getConfig().numberOfGenerations())
				&& (this.getConfig().numberOfEvaluations() <= 0 || this.algorithm.getNumberOfEvaluations() < this.getConfig().numberOfEvaluations())) {
			this.nextWithException();
			this.numberOfGenerationsEvolved++;
		}
		return this.getCurrentResult();
	}

	@Override
	public IMOEAFrameworkAlgorithmConfig getConfig() {
		return (IMOEAFrameworkAlgorithmConfig) super.getConfig();
	}

	@Override
	public IMOEAFrameworkAlgorithmInput getInput() {
		return (IMOEAFrameworkAlgorithmInput) super.getInput();
	}

}
