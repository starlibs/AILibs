package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.NearestCentroid;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        metric : string, or callable
        The metric to use when calculating distance between instances in a
        feature array. If metric is a string or callable, it must be one of
        the options allowed by metrics.pairwise.pairwise_distances for its
        metric parameter.
        The centroids for the samples corresponding to each class is the point
        from which the sum of the distances (according to the metric) of all
        samples that belong to that particular class are minimized.
        If the "manhattan" metric is provided, this centroid is the median and
        for all other metrics, the centroid is now set to be the mean.


    */
    public class OptionsFor_NearestCentroid_metric extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
