package ai.libs.jaicore.experiments;

import java.util.List;
import java.util.Map;

import org.api4.java.common.attributedobjects.GetPropertyFailedException;
import org.api4.java.common.attributedobjects.IGetter;

public interface IExperimentRunController<O> extends IGetter<Map<String, Object>, O> {
	public O parseResultMap(Map<String, Object> map);

	public List<IEventBasedResultUpdater> getResultUpdaterComputer(final Experiment experiment);

	public List<IExperimentTerminationCriterion> getTerminationCriteria(final Experiment experiment);

	@Override
	default O getPropertyOf(final Map<String, Object> obj) throws InterruptedException, GetPropertyFailedException {
		return this.parseResultMap(obj);
	}
}
