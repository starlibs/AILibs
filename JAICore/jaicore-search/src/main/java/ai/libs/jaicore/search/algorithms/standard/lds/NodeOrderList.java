package ai.libs.jaicore.search.algorithms.standard.lds;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class NodeOrderList extends ArrayList<Integer> implements Comparable<NodeOrderList> {

	@Override
	public int compareTo(final NodeOrderList o) {
		if (o == null) {
			return 1;
		}

		/* first just count the number of deviations */
		int thisDeviations = this.stream().mapToInt(x -> x).sum();
		int otherDeviations = o.stream().mapToInt(x -> x).sum();
		if (thisDeviations != otherDeviations) {
			return thisDeviations - otherDeviations;
		}

		/* if we come here, the number of deviations for the two nodes is the same. Then take the one with the highest unique deviation */
		int nThis = this.size();
		for (int i = 0; i < nThis; i++) {
			if (!this.get(i).equals(o.get(i))) {
				return o.get(i).compareTo(this.get(i));
			}
		}
		return 0;
	}

	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}