package jaicore.search.algorithms.standard.uncertainty;

public class UncertaintyFMeasure implements Comparable<UncertaintyFMeasure> {

	private double fValue;
	private double uncertainty;
	
	public UncertaintyFMeasure(double fValue, double uncertainty) {
		this.fValue = fValue;
		this.uncertainty = uncertainty;
	}

	public double getfValue() {
		return fValue;
	}

	public void setfValue(double fValue) {
		this.fValue = fValue;
	}

	public double getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(double uncertainty) {
		this.uncertainty = uncertainty;
	}

	@Override
	public int compareTo(UncertaintyFMeasure u) {
		double l1 = Math.sqrt(fValue * fValue + uncertainty * uncertainty);
		double l2 = Math.sqrt(u.getfValue() * u.getfValue() + u.getUncertainty() * u.getUncertainty());
		if (l1 == l2) {
			return 0;
		} else {
			if (l1 < l2) {
				return -1;
			} else {
				return 1;
			}
		}
	}
	
	@Override
	public String toString() {
		return "f: " + fValue + ", uncertainty: " + uncertainty;
	}

}
