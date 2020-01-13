package ai.libs.jaicore.ml.ranking.label.learner.clusterbased.modifiedisac;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.IGroupBuilder;
import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Group;
import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.ProblemInstance;
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
