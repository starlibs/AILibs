package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.LocallyLinearEmbedding;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        neighbors_algorithm : string ['auto'|'brute'|'kd_tree'|'ball_tree']
        algorithm to use for nearest neighbors search,
        passed to neighbors.NearestNeighbors instance


    */
    public class OptionsFor_LocallyLinearEmbedding_neighbors_algorithm extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
