package jaicore.modifiedISAC;

import java.util.List;

import jaicore.CustomDataTypes.Group;
import jaicore.CustomDataTypes.GroupIdentifier;
import jaicore.CustomDataTypes.ProblemInstance;
import weka.core.Instance;

public class Cluster extends Group<double[], Instance> {

	/**  Saves a cluster in two components. First, a list of the elements in the cluster 
	 * 	 here in form of list of problem instnaces. Second, the identifier of the cluster
	 * 	 in form of the cluster center as a point.
	 * @param instanlist
	 * @param id
	 */
	Cluster(List<ProblemInstance<Instance>> instanlist, GroupIdentifier<double[]> id) {
		super(instanlist, id);
		// TODO Auto-generated constructor stub
	}

}
