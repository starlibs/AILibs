package autofe.algorithm.hasco;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.core.exception.PredictionException;
import ai.libs.jaicore.ml.core.exception.TrainingException;
import ai.libs.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class AutoFEWekaPipeline implements IFEMLClassifier, Serializable, Cloneable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoFEWekaPipeline.class);

    private static final String EXCEPTION_MESSAGE = "Could not classify instances due to an exception.";

    /**
     *
     */
    private static final long serialVersionUID = 1847043351230983666L;

    private FilterPipeline filterPipeline;
    private transient Classifier mlPipeline;

    public AutoFEWekaPipeline(final FilterPipeline filterPipeline, final Classifier mlPipeline) {
        this.filterPipeline = filterPipeline;
        this.mlPipeline = mlPipeline;
    }

    @Override
    public void buildClassifier(final Instances data) throws Exception {
        this.mlPipeline.buildClassifier(data);
    }

    @Override
    public void buildClassifier(final DataSet data) throws TrainingException {
        try {
            this.mlPipeline.buildClassifier(this.transformData(data));
        } catch (Exception e) {
            throw new TrainingException(EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public double classifyInstance(final Instance instance) throws PredictionException {
        try {
            return this.mlPipeline.classifyInstance(instance);
        } catch (Exception e) {
            throw new PredictionException(EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public double classifyInstance(final INDArray instance, final Instances refInstances) throws PredictionException {
        try {
            return this.classifyInstance(this.transformData(instance, refInstances));
        } catch (Exception e) {
            throw new PredictionException(EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public double[] distributionForInstance(final Instance instance) throws PredictionException {
        try {
            return this.mlPipeline.distributionForInstance(instance);
        } catch (Exception e) {
            throw new PredictionException(EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public double[] distributionForInstance(final INDArray instance, final Instances refInstances) throws PredictionException {
        try {
            return this.mlPipeline.distributionForInstance(this.transformData(instance, refInstances));
        } catch (Exception e) {
            throw new PredictionException(EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public Capabilities getCapabilities() {
        return this.mlPipeline.getCapabilities();
    }

    @Override
    public Instances transformData(final DataSet data) throws InterruptedException {
        DataSet intermediateData = data;
        if (this.filterPipeline != null && this.filterPipeline.getFilters() != null
                && !this.filterPipeline.getFilters().isEmpty()) {
            intermediateData = this.filterPipeline.applyFilter(intermediateData, true);
        }
        return DataSetUtils.matricesToInstances(intermediateData);
    }

    @Override
    public Instance transformData(final INDArray instance, final Instances refInstances) throws InterruptedException {
        List<INDArray> data = new LinkedList<>();
        data.add(instance);
        Instances wekaData = this.transformData(new DataSet(refInstances, data));
        return wekaData.get(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.filterPipeline);
        sb.append("=>");

        if (this.mlPipeline instanceof MLPipeline) {
            sb.append(this.mlPipeline);
        } else {
            sb.append(WekaUtil.getClassifierDescriptor(this.mlPipeline));
        }

        return sb.toString();
    }

    public FilterPipeline getFilterPipeline() {
        return this.filterPipeline;
    }

    public Classifier getMLPipeline() {
        return this.mlPipeline;
    }

    @Override
    public AutoFEWekaPipeline clone() throws CloneNotSupportedException {
        super.clone();
        try {
            return new AutoFEWekaPipeline(this.filterPipeline.clone(), WekaUtil.cloneClassifier(this.mlPipeline));
        } catch (Exception e) {
            LOGGER.error("Could not clone AutoFEWekaPipeline due to '" + e.getMessage()
                    + "'. Returning null pipeline instead.");
            return new AutoFEWekaPipeline(null, null);
        }
    }
}
