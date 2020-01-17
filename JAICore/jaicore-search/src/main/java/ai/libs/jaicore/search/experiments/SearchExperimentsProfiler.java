package ai.libs.jaicore.search.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;

import ai.libs.jaicore.experiments.Experiment;
import ai.libs.jaicore.experiments.exceptions.ExperimentDecodingException;
import ai.libs.jaicore.search.landscapeanalysis.GenericLandscapeAnalyzer;
import ai.libs.jaicore.search.landscapeanalysis.LandscapeAnalysisCompletionTechnique;

public class SearchExperimentsProfiler {

	private static final String FOLDER_LANDSCAPES = "landscapes";

	private class Toolbox<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> {
		private final ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> decoder;
		public Toolbox(final ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> decoder) {
			super();
			this.decoder = decoder;
		}

		public GenericLandscapeAnalyzer<N, A> getLandscapeAnalyzer(final Experiment experiment) {
			try {
				return new GenericLandscapeAnalyzer<>(this.decoder.getProblem(experiment));
			} catch (ExperimentDecodingException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private final Toolbox<?, ?, ?> toolbox;
	private File workingDirectory;

	public <I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> SearchExperimentsProfiler(
			final ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> decoder, final File workingDirectory) {
		this.toolbox = new Toolbox<>(decoder);
		this.workingDirectory = workingDirectory;
	}

	private File getLandscapeFolder(final Experiment experiment) {
		File folder =  new File(this.workingDirectory + File.separator + FOLDER_LANDSCAPES + File.separator + experiment.hashCode());
		folder.mkdirs();
		return folder;
	}

	public void plainLandscapeAnalysis(final Experiment experiment, final Number probeSize) throws IOException, PathEvaluationException, InterruptedException {
		try (FileWriter fw = new FileWriter(new File(this.getLandscapeFolder(experiment) + File.separator + probeSize.intValue() + ".plainlandscape"))) {
			for (double d : this.toolbox.getLandscapeAnalyzer(experiment).getValues(probeSize, LandscapeAnalysisCompletionTechnique.RANDOM)) {
				fw.write(d + "\n");
			}
		}
	}

	public void iterativeLandscapeAnalysis(final Experiment experiment, final Number probeSize) throws IOException, PathEvaluationException, InterruptedException {
		List<List<double[]>> iterativeAnalysisResults = this.toolbox.getLandscapeAnalyzer(experiment).getIterativeProbeValuesAlongRandomPath(probeSize);
		int m = iterativeAnalysisResults.size();
		for (int depth = 0; depth < m; depth++) {
			List<double[]> probesInDepth = iterativeAnalysisResults.get(depth);
			int n = probesInDepth.size();
			for (int branch = 0; branch < n; branch++) {
				try (FileWriter fw = new FileWriter(new File(this.getLandscapeFolder(experiment) + File.separator + depth + "-" + branch + ".iterativelandscape"))) {
					for (double d : probesInDepth.get(branch)) {
						fw.write(d + "\n");
					}
				}
			}
		}
	}
}
