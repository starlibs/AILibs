package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.pca.RandomizedPCA;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        whiten : bool, optional
        When True (False by default) the `components_` vectors are multiplied
        by the square root of (n_samples) and divided by the singular values to
        ensure uncorrelated outputs with unit component-wise variances.

        Whitening will remove some information from the transformed signal
        (the relative variance scales of the components) but can sometime
        improve the predictive accuracy of the downstream estimators by
        making their data respect some hard-wired assumptions.


    */
    public class OptionsFor_RandomizedPCA_whiten extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
