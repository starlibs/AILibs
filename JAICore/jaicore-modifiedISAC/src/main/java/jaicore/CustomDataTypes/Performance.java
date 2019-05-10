package jaicore.CustomDataTypes;

/**
 * @author Helen Beierling
 *
 * @param <P> stands for the used measure of performance.
 */
public class Performance<P> {
	
	private P performance;
	
	public Performance() {}
	public Performance(P perform) {
		this.performance = perform;
	}
	
	public P getdirctPerformance() {
		return performance;
	}
	public void setPerformance(P newperformance) {
		this.performance = newperformance;
	}
	public boolean isEmpty() {
		return performance != null;
	}
}
