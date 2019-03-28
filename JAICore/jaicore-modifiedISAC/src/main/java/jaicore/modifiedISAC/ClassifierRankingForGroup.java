package jaicore.modifiedISAC;

import java.util.ArrayList;

import jaicore.CustomDataTypes.GroupIdentifier;
import jaicore.CustomDataTypes.RankingForGroup;
import jaicore.CustomDataTypes.Solution;

public class ClassifierRankingForGroup extends RankingForGroup<double[],String> {

	/**
	 * This class saves a classifier ranking in form of their names as string for a group of problem instances. The group is identified by a group identifier here in form of 
	 * of point. 
	 * @param identifier
	 * @param solutionsForGroup
	 */
	ClassifierRankingForGroup(GroupIdentifier<double[]> identifier, ArrayList<Solution<String>> solutionsForGroup) {
		super(identifier, solutionsForGroup);
		// TODO Auto-generated constructor stub
	}

}
