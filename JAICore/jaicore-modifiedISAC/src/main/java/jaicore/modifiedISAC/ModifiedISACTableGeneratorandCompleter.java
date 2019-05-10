package jaicore.modifiedISAC;

import java.util.List;

import DataManager.ITableGeneratorandCompleter;
import jaicore.CustomDataTypes.ProblemInstance;
import jaicore.CustomDataTypes.Table;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class ModifiedISACTableGeneratorandCompleter implements ITableGeneratorandCompleter<Instance, String,Double> {

	@Override
	public Table<Instance, String, Double> getInforamtionforRanking(
			List<ProblemInstance<Instance>> InstancesToRank) {
		// TODO Auto-generated method stub
		return null;
	}

}
