package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.List;

import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import jaicore.ml.ranking.clusterbased.customdatatypes.Table;
import jaicore.ml.ranking.clusterbased.datamanager.ITableGeneratorandCompleter;
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
