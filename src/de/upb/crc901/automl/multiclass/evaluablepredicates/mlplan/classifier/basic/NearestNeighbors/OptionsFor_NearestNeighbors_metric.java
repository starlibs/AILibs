package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.NearestNeighbors;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        metric : string or callable, default 'minkowski'
        metric to use for distance computation. Any metric from scikit-learn
        or scipy.spatial.distance can be used.

        If metric is a callable function, it is called on each
    
    */
    public class OptionsFor_NearestNeighbors_metric extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"manhattan", "chebyshev"}); // minkowski is default and covers euclidean with p=2

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
