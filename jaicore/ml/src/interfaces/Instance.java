package jaicore.ml.interfaces;

import java.util.List;

public interface Instance extends List<Double> {
	public String toJson();
	public int getNumberOfColumns();
}
