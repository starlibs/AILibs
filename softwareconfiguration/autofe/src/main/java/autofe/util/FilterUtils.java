package autofe.util;

import java.util.Map;

import autofe.algorithm.hasco.filter.generic.AddConstantFilter;
import autofe.algorithm.hasco.filter.generic.AddRandomFilter;
import autofe.algorithm.hasco.filter.generic.IdentityFilter;
import autofe.algorithm.hasco.filter.generic.WEKAFilter;
import autofe.algorithm.hasco.filter.image.LocalBinaryPatternFilter;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.algorithm.hasco.filter.meta.UnionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.filters.unsupervised.attribute.PrincipalComponents;

public final class FilterUtils {
    private static final Logger logger = LoggerFactory.getLogger(FilterUtils.class);
    private static final String LAYER_PARAMETER_NAME = "layer";

    private FilterUtils() {
        // Utility class
    }

    public static IFilter getFilterForName(final String name, final Map<String, String> parameters,
                                           final long[] inputShape) {
        // Image filters
        if (name.startsWith("autofe.algorithm.hasco.filter.image.CatalanoWrapperFilter")) {
            String[] filterNameSplit = name.split("_");
            return ImageUtils.getCatalanoFilterByName(filterNameSplit[1]);
        }

        if (name.startsWith("CatalanoExtractor")) {
            String[] extractorNameSplit = name.split("_");
            return ImageUtils.getCatalanoFilterByName(extractorNameSplit[1]);
        }

        if (name.startsWith("autofe.algorithm.hasco.filter.image")) {
            switch (name) {
                case "autofe.algorithm.hasco.filter.image.LocalBinaryPatternFilter":
                    return new LocalBinaryPatternFilter();
                default:
                case "autofe.algorithm.hasco.filter.image.PretrainedNN":
                    String net = parameters.get("net");
                    net = net == null ? "" : net;
                    int layer = -1;
                    if (parameters.get(LAYER_PARAMETER_NAME) != null)
                        layer = Integer.parseInt(parameters.get(LAYER_PARAMETER_NAME));
                    return ImageUtils.getPretrainedNNFilterByName(net, layer, inputShape);
            }
        }

        switch (name) {
            case "autofe.algorithm.hasco.filter.generic.AddConstantFilter":
                return new AddConstantFilter();
            case "autofe.algorithm.hasco.filter.generic.IdentityFilter":
                logger.debug("Returning default filter due to name occurence {}.", name);
                return new IdentityFilter();
            case "autofe.algorithm.hasco.filter.generic.AddRandomFilter":
                return new AddRandomFilter();
            case "autofe.MakeUnion":
                return new UnionFilter();
            case "PCA":
                return new WEKAFilter(new PrincipalComponents());
            default:
                logger.debug("Returning default filter due to name occurence {}.", name);
                return getDefaultFilter();
        }
    }

    private static IFilter getDefaultFilter() {
        return new IdentityFilter();
    }
}
