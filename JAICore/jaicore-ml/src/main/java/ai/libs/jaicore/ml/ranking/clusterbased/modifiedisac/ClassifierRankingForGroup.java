package ai.libs.jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.ArrayList;

import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.GroupIdentifier;
import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.RankingForGroup;

@SuppressWarnings("serial")
public class ClassifierRankingForGroup extends RankingForGroup<double[],String> {

	/**
	 * This class saves a classifier ranking in form of their names as string for a group of problem instances. The group is identified by a group identifier here in form of
	 * of point.
	 * @param identifier
	 * @param solutionsForGroup
	 */
	ClassifierRankingForGroup(final GroupIdentifier<double[]> identifier, final ArrayList<String> solutionsForGroup) {
		super(identifier, solutionsForGroup);
	}
}