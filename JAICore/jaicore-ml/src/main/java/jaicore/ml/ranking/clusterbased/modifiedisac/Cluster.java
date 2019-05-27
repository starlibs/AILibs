package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.List;

import jaicore.ml.ranking.clusterbased.customdatatypes.Group;
import jaicore.ml.ranking.clusterbased.customdatatypes.GroupIdentifier;
import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import weka.core.Instance;

public class Cluster extends Group<double[], Instance> {

	/**  Saves a cluster in two components. First, a list of the elements in the cluster
	 * 	 here in form of list of problem instnaces. Second, the identifier of the cluster
	 * 	 in form of the cluster center as a point.
	 * @param instanlist
	 * @param id
	 */
	Cluster(final List<ProblemInstance<Instance>> instanlist, final GroupIdentifier<double[]> id) {
		super(instanlist, id);
	}
}
