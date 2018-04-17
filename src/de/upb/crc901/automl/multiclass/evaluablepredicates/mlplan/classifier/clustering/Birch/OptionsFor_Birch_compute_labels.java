package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.Birch;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        compute_labels : bool, default True
        Whether or not to compute labels for each fit.


    */
    public class OptionsFor_Birch_compute_labels extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
