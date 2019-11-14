package ai.libs.jaicore.experiments;

import java.util.List;

import org.api4.java.common.attributedobjects.GetPropertyFailedException;
import org.api4.java.common.attributedobjects.IGetter;

public interface IExperimentRunController<O> extends IGetter<Experiment, O> {
	public O parseResultMap(Experiment map);

	public List<IEventBasedResultUpdater> getResultUpdaterComputer(final Experiment experiment);

	public List<IExperimentTerminationCriterion> getTerminationCriteria(final Experiment experiment);

	@Override
	default O getPropertyOf(final Experiment obj) throws InterruptedException, GetPropertyFailedException {
		return this.parseResultMap(obj);
	}
}
