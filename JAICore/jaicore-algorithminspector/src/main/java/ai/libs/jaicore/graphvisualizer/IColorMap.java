package ai.libs.jaicore.graphvisualizer;

import java.awt.Color;

public interface IColorMap {

	public Color get(double min, double max, double val);
}
