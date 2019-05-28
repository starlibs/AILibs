package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.ArrayList;
import java.util.List;

import jaicore.ml.ranking.clusterbased.IGroupBuilder;
import jaicore.ml.ranking.clusterbased.customdatatypes.Group;
import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import weka.core.Instance;

public class ModifiedISACGroupBuilder implements IGroupBuilder<double[], Instance> {
	private List<double[]> points;
	@Override
	public List<Group<double[], Instance>> buildGroup(final List<ProblemInstance<Instance>> allInstances) {
		ModifiedISACgMeans groupBuilder = new ModifiedISACgMeans(this.points, allInstances);
		return new ArrayList<>(groupBuilder.clusterDeprecated());
	}

	public void setPoints(final List<double[]> toSetPoints) {
		this.points = toSetPoints;
	}

}
