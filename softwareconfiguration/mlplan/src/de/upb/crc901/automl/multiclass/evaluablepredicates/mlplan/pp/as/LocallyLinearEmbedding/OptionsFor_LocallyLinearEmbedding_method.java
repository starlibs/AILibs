package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.LocallyLinearEmbedding;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        method : string ('standard', 'hessian', 'modified' or 'ltsa')
        standard : use the standard locally linear embedding algorithm.  see
                   reference [1]
        hessian  : use the Hessian eigenmap method. This method requires
                   ``n_neighbors > n_components * (1 + (n_components + 1) / 2``
                   see reference [2]
        modified : use the modified locally linear embedding algorithm.
                   see reference [3]
        ltsa     : use local tangent space alignment algorithm
                   see reference [4]


    */
    public class OptionsFor_LocallyLinearEmbedding_method extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
