package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.QuadraticDiscriminantAnalysis;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        store_covariance : boolean
        If True the covariance matrices are computed and stored in the
        `self.covariance_` attribute.

        .. versionadded:: 0.17


    */
    public class OptionsFor_QuadraticDiscriminantAnalysis_store_covariance extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
