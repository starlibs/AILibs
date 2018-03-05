package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.TSNE;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        init : string or numpy array, optional (default: "random")
        Initialization of embedding. Possible options are 'random', 'pca',
        and a numpy array of shape (n_samples, n_components).
        PCA initialization cannot be used with precomputed distances and is
        usually more globally stable than random initialization.


    */
    public class OptionsFor_TSNE_init extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
