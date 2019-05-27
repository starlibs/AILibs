package jaicore.ml.ranking.clusterbased;

import java.util.List;

import jaicore.ml.ranking.clusterbased.CustomDataTypes.Group;
import jaicore.ml.ranking.clusterbased.CustomDataTypes.ProblemInstance;

/**
 * IGroupBuilder discribes the act of building groups out of probleminstances
 * 
 * @author Helen Beierling
 *
 * @param <C>
 *            Centers of the found groups
 * @param <I>
 *            Probleminstaces to group and grouped instances
 */
public interface IGroupBuilder<C, I> {
	List<? extends Group<C, I>> buildGroup(List<ProblemInstance<I>> allInstances);
}
