package ai.libs.jaicore.graphvisualizer;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class SeismicColorMap implements IColorMap {

	@Override
	public Color get(final double min, final double max, final double v) {
		final float hBlue = .75f;
		final float hRed = 0f;
		final float brightness = 1f;
		double half = (max + min) / 2.0;
		double spectrumToHalf = Math.abs(max - half);

		double vAdjusted = Math.min(Math.max(min, v), max);

		float relDistanceToHalf = (float)(Math.abs(vAdjusted - half) / spectrumToHalf);
		if (vAdjusted < half) {
			return Color.getHSBColor(hBlue, relDistanceToHalf, brightness);
		}
		else {
			return Color.getHSBColor(hRed, relDistanceToHalf, brightness);
		}
	}

	public static void main(final String[] args) {

		double min = 0;
		double max = 1;

		SeismicColorMap cm = new SeismicColorMap();
		BufferedImage image = new BufferedImage(500/*Width*/, 100/*height*/, BufferedImage.TYPE_INT_ARGB);

		for (int i = 0; i < 100; i++)  {
			double v = min + (i * 1.0 / 100) * (max - min);
			Color c = cm.get(min, max, v);
			for (int j = 0; j < 100; j++) {
				for (int k = 0; k < 5; k ++) {
					image.setRGB(i * 5 + k, j, c.getRGB());
				}
			}
		}
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setVisible(true);
	}
}
