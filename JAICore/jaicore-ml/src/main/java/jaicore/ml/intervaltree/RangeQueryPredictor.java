package jaicore.ml.intervaltree;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import jaicore.ml.intervaltree.util.RQPHelper;
import jaicore.ml.intervaltree.util.RQPHelper.IntervalAndHeader;
import weka.core.Instance;

/**
 *
 * @author elppa
 *
 */
public interface RangeQueryPredictor {

	default Interval predictInterval(final Instance data) {
		IntervalAndHeader intervalAndHeader = RQPHelper.mapWEKAToTree(data);
		return predictInterval(intervalAndHeader);
	}

	public Interval predictInterval(IntervalAndHeader intervalAndHeader);
}
