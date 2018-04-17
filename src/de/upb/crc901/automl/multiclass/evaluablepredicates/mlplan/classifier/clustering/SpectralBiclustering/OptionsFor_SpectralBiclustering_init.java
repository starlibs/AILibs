package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.SpectralBiclustering;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        init : {'k-means++', 'random' or an ndarray}
         Method for initialization of k-means algorithm; defaults to
         'k-means++'.


    */
    public class OptionsFor_SpectralBiclustering_init extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
