package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.FastICA;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        fun : string or function, optional. Default: "logcosh"
        The functional form of the G function used in the
        approximation to neg-entropy. Could be either "logcosh", "exp",
        or "cube".
        You can also provide your own function. It should return a tuple
        containing the value of the function, and of its derivative, in the
        point. Example:

        def my_g(x):
            return x ** 3, 3 * x ** 2


    */
    public class OptionsFor_FastICA_fun extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"exp",
                 "cube"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
