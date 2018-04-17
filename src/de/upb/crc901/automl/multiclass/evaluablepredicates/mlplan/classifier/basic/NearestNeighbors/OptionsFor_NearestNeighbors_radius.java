
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.NearestNeighbors;
    /*
        radius : float, optional (default = 1.0)
        Range of parameter space to use by default for :meth:`radius_neighbors`
        queries.


    */

    import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

    public class OptionsFor_NearestNeighbors_radius extends OptionsPredicate {
        
        private static List<Float> validValues = Arrays.asList(new Float[]{.5f, 1f, 1.5f, 2f, 5f});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
