package jaicore.search.algorithms.standard.lds;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class NodeOrderList extends ArrayList<Integer> implements Comparable<NodeOrderList> {

	@Override
	public int compareTo(NodeOrderList o) {
//		System.out.println("Comparing " + this + " to " + o + ".");
		if (o == null)
			return 1;
		
		/* first just count the number of deviations */
		int thisDeviations = this.stream().mapToInt(x -> x).sum();
		int otherDeviations = o.stream().mapToInt(x -> x).sum();
		if (thisDeviations != otherDeviations)
			return thisDeviations - otherDeviations;
		
		/* if we come here, the number of deviations for the two nodes is the same. Then take the one with the highest unique deviation */
		int nThis = this.size();
		for (int i = 0; i < nThis; i++) {
			if (this.get(i) != o.get(i))
				return o.get(i).compareTo(this.get(i));
		}
//		int nThis = this.size();
//		int nOther = o.size();
//		int nMax = Math.max(nThis, nOther);
//		int offsetThis = nMax - nThis;
//		int offsetOther = nMax - nOther;
//		
//		
//		for (int i = 0; i < nMax; i++) {
//			int valThis = (i >= offsetThis) ? this.get(i - offsetThis) : 0;
//			int valOther = (i >= offsetOther) ? o.get(i - offsetOther) : 0;
//			
//			if (valThis != valOther) {
//				int order = nMax - i - 1;
//				int factor = (int)Math.pow(10, order);
//				return Integer.compare(valThis, valOther) * factor;
//			}
//		}
//		System.out.println("Comparing " + this + " to " + o + ": 0");
		return 0;
	}

}