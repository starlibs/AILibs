package ai.libs.jaicore.search.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.algorithm.TimeOut;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.IExperimentIntermediateResultProcessor;
import ai.libs.jaicore.experiments.IExperimentSetEvaluator;
import ai.libs.jaicore.experiments.IExperimentTerminationCriterion;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;

public class OptimalPathSearchBenchmarker implements IExperimentSetEvaluator, ILoggingCustomizable {

	private class Caps<I extends IGraphSearchWithPathEvaluationsInput<N, A, Double>, N, A> {
		private final ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> decoder;

		public Caps(
				final ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> decoder) {
			super();
			this.decoder = decoder;
		}
	}

	private final List<IExperimentTerminationCriterion> terminationCriteria;
	private final Caps<?, ?, ?> caps;

	private Logger logger = LoggerFactory.getLogger(OptimalPathSearchBenchmarker.class);

	public <I extends IGraphSearchWithPathEvaluationsInput<N, A, Double>, N, A> OptimalPathSearchBenchmarker(
			final ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> decoder) {
		this(decoder, new ArrayList<>(0));
	}

	public <I extends IGraphSearchWithPathEvaluationsInput<N, A, Double>, N, A> OptimalPathSearchBenchmarker(
			final ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> decoder, final List<IExperimentTerminationCriterion> terminationCriteria) {
		this.caps = new Caps<>(decoder);
		this.terminationCriteria = terminationCriteria;
	}

	@Override
	public final void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException, InterruptedException {

		this.logger.info("Starting evaluation of experiment entry {}", experimentEntry);

		/* get algorithm */
		IOptimalPathInORGraphSearch<?, ?, ?, ?, Double> optimizer = this.caps.decoder.getAlgorithm(experimentEntry.getExperiment());
		this.logger.debug("Created optimizer {} for problem instance {}. Configuring logger name if possible.", optimizer, optimizer.getInput());
		if (optimizer instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) optimizer).setLoggerName(this.getLoggerName() + ".optimizer");
		}
		optimizer.setTimeout(new TimeOut(1, TimeUnit.HOURS));
		final List<Double> scores = new ArrayList<>();
		ArrayNode observations = new ObjectMapper().createArrayNode();
		final long start = System.currentTimeMillis();

		optimizer.registerListener(new Object() {

			Set<IEvaluatedPath<?, ?, Double>> solutionPaths = new HashSet<>();

			@Subscribe
			public void receiveEvent(final AlgorithmEvent e) {
				OptimalPathSearchBenchmarker.this.processEvent(processor, optimizer, e);

				/* check whether one of the termination criteria is satisfied */
				if (OptimalPathSearchBenchmarker.this.terminationCriteria.stream().anyMatch(c -> c.doesTerminate(e))) {
					optimizer.cancel();
				}
			}

			@Subscribe
			public void receiveSolution(final EvaluatedSearchSolutionCandidateFoundEvent<?, ?, Double> e) {
				EvaluatedSearchGraphPath<?, ?, Double> path = e.getSolutionCandidate();
				this.solutionPaths.add(path);
				double score = e.getSolutionCandidate().getScore();
				OptimalPathSearchBenchmarker.this.logger.info("Found solution with {} for path with nodes {}, arcs {}, annotations {}, and hash code {}. Have now {} solution paths.", score, path.getNodes(), path.getArcs(), path.getAnnotations(),
						path.getNodes().hashCode(), this.solutionPaths.size());
				scores.add(score);
				ArrayNode observation = new ObjectMapper().createArrayNode();
				observation.insert(0, System.currentTimeMillis() - start); // relative time
				observation.insert(1, MathExt.round(score, 5)); // score
				observations.add(observation);

				/* run solution found hook */
				OptimalPathSearchBenchmarker.this.runSolutionFoundHook(experimentEntry, processor, optimizer, path);
			}
		});

		/* run pre-experiment hook */
		this.runPreExperimentHook(experimentEntry, processor, optimizer);

		/* run search algorithm */
		try {
			optimizer.call();
		} catch (AlgorithmExecutionCanceledException e) {
			this.logger.info("CANCEL");
			/* this may just happen */
		} catch (NoSuchElementException e) {
			this.logger.info("NO SUCH ELEMENT");
			/* this just may happen */
		} catch (Exception e) {
			throw new ExperimentEvaluationFailedException(e);
		}

		/* update database with history */
		Map<String, Object> result = new HashMap<>();
		result.put("history", observations);
		processor.processResults(result);

		/* run post-experiment hook */
		this.runPostExperimentHook(experimentEntry, processor, optimizer);
	}

	public void runPreExperimentHook(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor, final IOptimalPathInORGraphSearch<?, ?, ?, ?, Double> optimizer) {
	}

	public void runPostExperimentHook(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor, final IOptimalPathInORGraphSearch<?, ?, ?, ?, Double> optimizer) {
	}

	public void runSolutionFoundHook(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor, final IOptimalPathInORGraphSearch<?, ?, ?, ?, Double> optimizer, final IEvaluatedPath<?, ?, Double> path) {
	}

	public void processEvent(final IExperimentIntermediateResultProcessor processor, final IOptimalPathInORGraphSearch<?, ?, ?, ?, Double> optimizer, final AlgorithmEvent e) {
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
