package ai.libs.jaicore.experiments;

import java.util.List;

public interface IExperimentRunController<O> {

	public O getExperimentEncoding(Experiment map);

	public List<IEventBasedResultUpdater> getResultUpdaterComputer(final Experiment experiment);

	public List<IExperimentTerminationCriterion> getTerminationCriteria(final Experiment experiment);
}
