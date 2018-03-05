package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.PassiveAggressiveClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        average : bool or int, optional
        When set to True, computes the averaged SGD weights and stores the
        result in the ``coef_`` attribute. If set to an int greater than 1,
        averaging will begin once the total number of samples seen reaches
        average. So average=10 will begin averaging after seeing 10 samples.

        .. versionadded:: 0.19
           parameter *average* to use weights averaging in SGD


    */
    public class OptionsFor_PassiveAggressiveClassifier_average extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", 2, 10, 50});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
