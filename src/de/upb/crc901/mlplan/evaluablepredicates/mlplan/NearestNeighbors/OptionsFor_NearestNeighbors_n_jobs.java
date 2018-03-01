
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.NearestNeighbors;
/*
    n_jobs : int, optional (default = 1)
        The number of parallel jobs to run for neighbors search.
        If ``-1``, then the number of jobs is set to the number of CPU cores.
        Affects only :meth:`kneighbors` and :meth:`kneighbors_graph` methods.

    Examples
    --------
      >>> import numpy as np
      >>> from sklearn.neighbors import NearestNeighbors
      >>> samples = [[0, 0, 2], [1, 0, 0], [0, 0, 1]]

      >>> neigh = NearestNeighbors(2, 0.4)
      >>> neigh.fit(samples)  #doctest: +ELLIPSIS
      NearestNeighbors(...)

      >>> neigh.kneighbors([[0, 0, 1.3]], 2, return_distance=False)
      ... #doctest: +ELLIPSIS
      array([[2, 0]]...)

      >>> nbrs = neigh.radius_neighbors([[0, 0, 1.3]], 0.4, return_distance=False)
      >>> np.asarray(nbrs[0][0])
      array(2)

    See also
    --------
    KNeighborsClassifier
    RadiusNeighborsClassifier
    KNeighborsRegressor
    RadiusNeighborsRegressor
    BallTree

    Notes
    -----
    See :ref:`Nearest Neighbors <neighbors>` in the online documentation
    for a discussion of the choice of ``algorithm`` and ``leaf_size``.

    https://en.wikipedia.org/wiki/K-nearest_neighbor_algorithm
    
 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_NearestNeighbors_n_jobs extends NumericRangeOptionPredicate {
	
	@Override
	protected double getMin() {
		return 0;
	}

	@Override
	protected double getMax() {
		return 0;
	}

	@Override
	protected int getSteps() {
		return 1;
	}

	@Override
	protected boolean needsIntegers() {
		return true;
	}
}

