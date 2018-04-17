package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.LinearDiscriminantAnalysis;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        store_covariance : bool, optional
        Additionally compute class covariance matrix (default False), used
        only in 'svd' solver.

        .. versionadded:: 0.17


    */
    public class OptionsFor_LinearDiscriminantAnalysis_store_covariance extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
