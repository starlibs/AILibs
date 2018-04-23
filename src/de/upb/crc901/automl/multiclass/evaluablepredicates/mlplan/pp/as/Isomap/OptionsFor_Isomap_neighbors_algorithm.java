package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.Isomap;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        neighbors_algorithm : string ["auto","brute","kd_tree","ball_tree"]
        Algorithm to use for nearest neighbors search,
        passed to neighbors.NearestNeighbors instance.


    */
    public class OptionsFor_Isomap_neighbors_algorithm extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"auto","brute","kd_tree","ball_tree"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
