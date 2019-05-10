package autofe.algorithm.hasco.filter.generic;

import java.io.Serializable;
import java.util.stream.LongStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import weka.filters.Filter;

/**
 * Filter containing WEKA Filter objects.
 *
 * @author Julian Lienen
 *
 */
public class WEKAFilter implements IFilter, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7373022098678014454L;

	private static Logger logger = LoggerFactory.getLogger(WEKAFilter.class);

	private Filter wekaFilter;

	public WEKAFilter(final Filter wekaFilter) {
		this.wekaFilter = wekaFilter;
	}

	@Override
	public DataSet applyFilter(DataSet inputData, final boolean copy) {

		inputData.updateInstances();

		if (copy) {
			inputData = inputData.copy();
		}

		// Force update of instances
		inputData.updateInstances();
		long[] refShape = inputData.getIntermediateInstances().get(0).shape();

		try {
			this.wekaFilter.setInputFormat(inputData.getInstances());
			inputData.setInstances(Filter.useFilter(inputData.getInstances(), this.wekaFilter));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.warn("Could not apply weka filter. Reason: " + e.getMessage());
			e.printStackTrace();
		}

		// Update intermediate instances using reference shape if spatial structure is
		// kept
		if (inputData.getInstances().numAttributes() - 1 == LongStream.of(refShape).reduce(1, (a, b) -> a * b))
			inputData.updateIntermediateInstances(refShape);
		else
			inputData.updateIntermediateInstances(null);

		return inputData;
	}

	@Override
	public String toString() {
		if (this.wekaFilter != null) {
			return "WEKAFilter [wekaFilter=" + this.wekaFilter.getClass().getSimpleName() + "]";
		} else {
			return "WEKAFilter [wekaFilter=" + this.wekaFilter + "]";
		}
	}

	@Override
	public WEKAFilter clone() throws CloneNotSupportedException {
		try {
			return new WEKAFilter(Filter.makeCopy(this.wekaFilter));
		} catch (Exception e) {
			logger.warn("Could not copy WEKA Filter due to " + e.getMessage() + " Trying to reuse filter.");
			return new WEKAFilter(this.wekaFilter);
		}
	}
}
