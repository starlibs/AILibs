package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.SpectralCoclustering;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        svd_method : string, optional, default: 'randomized'
        Selects the algorithm for finding singular vectors. May be
        'randomized' or 'arpack'. If 'randomized', use
        :func:`sklearn.utils.extmath.randomized_svd`, which may be faster
        for large matrices. If 'arpack', use
        :func:`scipy.sparse.linalg.svds`, which is more accurate, but
        possibly slower in some cases.


    */
    public class OptionsFor_SpectralCoclustering_svd_method extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
