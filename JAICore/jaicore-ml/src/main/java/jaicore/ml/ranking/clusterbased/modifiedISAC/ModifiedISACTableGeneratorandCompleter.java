package jaicore.ml.ranking.clusterbased.modifiedISAC;

import java.util.List;

import jaicore.ml.ranking.clusterbased.CustomDataTypes.ProblemInstance;
import jaicore.ml.ranking.clusterbased.CustomDataTypes.Table;
import jaicore.ml.ranking.clusterbased.DataManager.ITableGeneratorandCompleter;
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
