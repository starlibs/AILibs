package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.SpectralCoclustering;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        mini_batch : bool, optional, default: False
        Whether to use mini-batch k-means, which is faster but may get
        different results.


    */
    public class OptionsFor_SpectralCoclustering_mini_batch extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
