package autofe.algorithm.hasco.filter.image;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.Texture.BinaryPattern.IBinaryPattern;
import Catalano.Imaging.Texture.BinaryPattern.RobustLocalBinaryPattern;
import Catalano.Imaging.Texture.BinaryPattern.UniformLocalBinaryPattern;
import Catalano.Imaging.Tools.ImageHistogram;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

public class CatalanoBinaryPatternFilter extends AbstractCatalanoFilter<IBinaryPattern> {

    /**
     *
     */
    private static final long serialVersionUID = 9139886872471194592L;

    public CatalanoBinaryPatternFilter(final String name) {
        super(name);
    }

    @Override
    public DataSet applyFilter(final DataSet inputData, final boolean copy) throws InterruptedException {
        ImageUtils.checkInputData(inputData);

        // None filter
        if (this.getCatalanoFilter() == null) {
            prepareData(inputData, copy);
        }

        ColorSpace colorSpace = sampleColorSpace(inputData);

        // Assume to deal with FastBitmap instances
        List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
        for (INDArray inst : inputData.getIntermediateInstances()) {
            checkInterrupt();

            FastBitmap bitmap = ImageUtils.matrixToFastBitmap(inst, colorSpace);
            if (colorSpace != ColorSpace.Grayscale && this.isRequiresGrayscale()) {
                bitmap.toGrayscale();
            }

            ImageHistogram imageHistogram = this.getCatalanoFilter().ComputeFeatures(bitmap);

            INDArray result = ImageUtils.imageHistorgramToMatrix(imageHistogram);
            transformedInstances.add(result);
        }

        return new DataSet(inputData.getInstances(), transformedInstances);
    }

    public boolean isRequiresGrayscale() {
        switch (this.getName()) {
            /* Binary pattern */
            case "NoneExtractor":
                return false;
            case "UniformLocalBinaryPattern":
                return true;
            case "RobustLocalBinaryPattern":
                return true;
            default:
                return false;
        }
    }

    public IBinaryPattern getCatalanoFilter() {
        switch (this.getName()) {
            /* Binary pattern */
            case "NoneExtractor":
                return null;
            case "UniformLocalBinaryPattern":
                return new UniformLocalBinaryPattern();
            case "RobustLocalBinaryPattern":
                return new RobustLocalBinaryPattern();
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        if (this.getCatalanoFilter() != null) {
            return "CatalanoBinaryPatternFilter [catalanoFilter=" + this.getName() + "]";
        } else {
            return "CatalanoBinaryPatternFilter (empty)";
        }
    }

    @Override
    public CatalanoBinaryPatternFilter clone() {
        return new CatalanoBinaryPatternFilter(this.getName());
    }
}
