package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.FeatureAgglomeration;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        linkage : {"ward", "complete", "average"}, optional, default "ward"
        Which linkage criterion to use. The linkage criterion determines which
        distance to use between sets of features. The algorithm will merge
        the pairs of cluster that minimize this criterion.

        - ward minimizes the variance of the clusters being merged.
        - average uses the average of the distances of each feature of
          the two sets.
        - complete or maximum linkage uses the maximum distances between
          all features of the two sets.


    */
    public class OptionsFor_FeatureAgglomeration_linkage extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
