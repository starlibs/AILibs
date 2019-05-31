package ai.libs.jaicore.ml.ranking.clusterbased;

import java.util.List;

import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.Group;
import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;

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
	List<Group<C, I>> buildGroup(List<ProblemInstance<I>> allInstances);
}
