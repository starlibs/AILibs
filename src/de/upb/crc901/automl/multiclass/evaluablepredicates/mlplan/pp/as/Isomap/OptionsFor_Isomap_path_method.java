package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.Isomap;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        path_method : string ["auto","FW","D"]
        Method to use in finding shortest path.

        "auto" : attempt to choose the best algorithm automatically.

        "FW" : Floyd-Warshall algorithm.

        "D" : Dijkstra"s algorithm.


    */
    public class OptionsFor_Isomap_path_method extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"auto","FW","D"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
