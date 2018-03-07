package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.Imputer;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        strategy : string, optional (default="mean")
        The imputation strategy.

        - If "mean", then replace missing values using the mean along
          the axis.
        - If "median", then replace missing values using the median along
          the axis.
        - If "most_frequent", then replace missing using the most frequent
          value along the axis.


    */
    public class OptionsFor_Imputer_strategy extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"median", "most_frequest"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
