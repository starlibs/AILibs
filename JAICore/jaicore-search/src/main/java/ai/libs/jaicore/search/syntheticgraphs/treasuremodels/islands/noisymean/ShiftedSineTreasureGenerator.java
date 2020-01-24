package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean;

import java.math.BigDecimal;
import java.math.BigInteger;

import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public class ShiftedSineTreasureGenerator extends ATreasureMeanFunction {

	private final double c; //
	private final double d; //

	private final double s;

	public ShiftedSineTreasureGenerator(final IIslandModel islandModel, final long numberOfTreasures, final double c, final double d) {
		super(islandModel, numberOfTreasures);
		this.c = c;
		this.d = d;

		this.s = 1.0 / 2 * Math.PI - c / 2 - d; // this is where the rarity zones start
	}

	@Override
	public Double apply(final BigInteger t) {
		double max = this.getNumberOfTreasures() * 2 * Math.PI;
		double positionInInterval = new BigDecimal(t).multiply(BigDecimal.valueOf(max)).divide(BigDecimal.valueOf(this.getTotalNumberOfIslands().intValue())).doubleValue();
		int periodOffset = (int) Math.floor(positionInInterval / (2 * Math.PI));

		// double relativePositionWithinOptCurve = positionInInterval % (2 * Math.PI) - this.s + this.d;
		// double shift;
		// if (relativePositionWithinOptCurve <= this.c) {
		// shift = (1 + (2 * this.d) / this.c) * relativePositionWithinOptCurve - this.d;
		// } else {
		// shift = (1 - this.d / (2 * Math.PI - this.c)) * relativePositionWithinOptCurve + 2 * Math.PI * this.d / (2 * Math.PI - this.c);
		// }
		double positionInPeriod = positionInInterval % (2 * Math.PI);
		double shiftedPosition;
		if (positionInPeriod < 0.5*(Math.PI- this.c)) {
			shiftedPosition = (1 - this.d / (0.5 * (Math.PI - this.c))) * positionInPeriod;
		} else if (positionInPeriod > 0.5 * (Math.PI + this.c)) {
			shiftedPosition = (1 - this.d / (2 * Math.PI - 0.5 * (Math.PI + this.c))) * positionInPeriod + (2 * Math.PI * this.d) / (2 * Math.PI - 0.5 * (Math.PI + this.c));
		} else {
			shiftedPosition = (1 + 2 * this.d / this.c) * positionInPeriod + this.d * ((this.c - Math.PI) / this.c - 1);
		}

		shiftedPosition += Math.PI * 2 * periodOffset;
		//		System.out.println(positionInInterval + " -> " + positionInPeriod + " -> " + shiftedPosition);

		return (-1 * Math.sin(shiftedPosition) + 1) * 50; // this produces values in [0,100]
	}
}
