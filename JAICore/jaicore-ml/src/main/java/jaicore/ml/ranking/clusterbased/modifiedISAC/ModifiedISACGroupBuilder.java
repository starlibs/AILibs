package jaicore.ml.ranking.clusterbased.modifiedISAC;

import java.util.ArrayList;
import java.util.List;

import jaicore.ml.ranking.clusterbased.IGroupBuilder;
import jaicore.ml.ranking.clusterbased.CustomDataTypes.Group;
import jaicore.ml.ranking.clusterbased.CustomDataTypes.ProblemInstance;
import weka.core.Instance;

public class ModifiedISACGroupBuilder implements IGroupBuilder<double[], Instance> {
	private ArrayList<double[]> points;
	@Override
	public List<? extends Group<double[], Instance>> buildGroup(List<ProblemInstance<Instance>> allInstances) {
		ModifiedISACgMeans groupBuilder = new ModifiedISACgMeans(points, (ArrayList<ProblemInstance<Instance>>) allInstances);
		ArrayList<Cluster> foundCluster = groupBuilder.gmeanscluster();
		return foundCluster;
	}
	
	public void setPoints(ArrayList<double[]> toSetPoints) {
		points = toSetPoints;
	}

}
