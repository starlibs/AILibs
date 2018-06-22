package autofe.algorithm.hasco.filter.image;

import java.util.ArrayList;
import java.util.List;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Texture.BinaryPattern.LocalBinaryPattern;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;

public class LocalBinaryPatternFilter implements IFilter<FastBitmap> {

	private LocalBinaryPattern lbp = new LocalBinaryPattern();

	@Override
	public DataSet<FastBitmap> applyFilter(final DataSet<FastBitmap> inputData, final boolean copy) {

		// TODO: Check for copy flag

		// Assume to deal with FastBitmap instances
		List<FastBitmap> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
		for (FastBitmap inst : inputData.getIntermediateInstances()) {
			transformedInstances.add(lbp.toFastBitmap(inst));
		}

		return new DataSet<FastBitmap>(inputData.getInstances(), transformedInstances);
	}

}
