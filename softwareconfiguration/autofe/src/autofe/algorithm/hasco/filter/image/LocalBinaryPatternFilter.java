package autofe.algorithm.hasco.filter.image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Texture.BinaryPattern.LocalBinaryPattern;
import autofe.algorithm.hasco.filter.meta.IFilter;

public class LocalBinaryPatternFilter implements IFilter {

	private LocalBinaryPattern lbp = new LocalBinaryPattern();

	@SuppressWarnings("unchecked")
	@Override
	public Collection<?> applyFilter(final Collection<?> inputData) {

		// Assume to deal with FastBitmap instances
		List<FastBitmap> transformedInstances = new ArrayList<>(inputData.size());
		for (FastBitmap inst : (Collection<FastBitmap>) inputData) {
			transformedInstances.add(lbp.toFastBitmap(inst));
		}

		return transformedInstances;
	}

}
