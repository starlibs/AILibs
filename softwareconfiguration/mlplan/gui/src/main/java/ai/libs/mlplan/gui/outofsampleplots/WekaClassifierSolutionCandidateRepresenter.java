package ai.libs.mlplan.gui.outofsampleplots;

import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionCandidateRepresenter;

public class WekaClassifierSolutionCandidateRepresenter implements SolutionCandidateRepresenter {

	@Override
	public String getStringRepresentationOfSolutionCandidate(final Object solutionCandidate) {
		return solutionCandidate.toString();
	}

}
