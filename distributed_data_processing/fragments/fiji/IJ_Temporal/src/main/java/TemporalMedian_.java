package IJ_Temporal.src.main.java;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.Arrays;

/**

 * A "probabilistic" temporal median filter to extract a foreground

 * probability imp from a time sequence.

 *

 * @author graemeball@googlemail.com

 */
public class TemporalMedian_ implements PlugIn {
    int width;
    int height;
    int nc;
    int nz;
    int nt;
    public int twh = 5;
    public double nsd = 2.0;
    
    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (showDialog()) {
            if (((ImagePlus) imp).getNFrames() > 2 * twh + 1) {
                ImagePlus result = exec((ImagePlus) imp);
                ((ImagePlus) result).show();
            } else {
                IJ.showMessage((String) ("Insufficient time points, " + nt));
            }
        }
    }
    
    boolean showDialog() {
        GenericDialog gd = new GenericDialog("Temporal Median");
        ((GenericDialog) gd).addNumericField("time_window half-width", twh, 0);
        ((GenericDialog) gd).addNumericField("foreground_stdevs over median",
                                             nsd, 1);
        ((GenericDialog) gd).showDialog();
        if (((GenericDialog) gd).wasCanceled()) {
            return false;
        } else {
            twh = (int) ((GenericDialog) gd).getNextNumber();
            nsd = (float) ((GenericDialog) gd).getNextNumber();
            return true;
        }
    }
    
    /**
    
     * Execute temporal median filter, returning new foreground ImagePlus.
    
     * Uses array of pixel arrays for sliding window of time frames.
    
     *
    
     * @param imp (multi-dimensional, i.e. multiple frames)
    
     */
    public ImagePlus exec(ImagePlus imp) {
        this.width = ((ImagePlus) imp).getWidth();
        this.height = ((ImagePlus) imp).getHeight();
        this.nc = ((ImagePlus) imp).getNChannels();
        this.nz = ((ImagePlus) imp).getNSlices();
        this.nt = ((ImagePlus) imp).getNFrames();
        ImageStack inStack = ((ImagePlus) imp).getStack();
        int size = ((ImageStack) inStack).getSize();
        ImageStack outStack = new ImageStack(width, height, size);
        int progressCtr = 0;
        IJ.showStatus("Finding Foreground...");
        int c = 1;
        while (c <= nc) {
            int z = 1;
            while (z <= nz) {
                float[][] tWinPix = (float[][])
                                      (new float[2 * twh + 1][width * height]);
                int wmin = 0;
                int wcurr = 0;
                int wmax = twh;
                int t = 1;
                while (t <= wmax + 1) {
                    int index = ((ImagePlus) imp).getStackIndex(c, z, t);
                    ((float[][]) tWinPix)[t - 1] = vsPixels((ImageStack)
                                                              inStack, index);
                    t++;
                }
                t = 1;
                while (t <= nt) {
                    float[] fgPix = calcFg((float[][]) tWinPix, wcurr, wmin,
                                           wmax);
                    FloatProcessor fp2 = new FloatProcessor(width, height,
                                                            (float[]) fgPix);
                    int index = ((ImagePlus) imp).getStackIndex(c, z, t);
                    ((ImageStack) outStack).addSlice((String) ("" + index),
                                                     (ImageProcessor)
                                                       (ImageProcessor) fp2,
                                                     index);
                    ((ImageStack) outStack).deleteSlice(index);
                    if (t > twh) {
                        tWinPix = rmFirst((float[][]) tWinPix, wmax);
                    } else {
                        wcurr += 1;
                        wmax += 1;
                    }
                    if (t < nt - twh) {
                        int newPixIndex = ((ImagePlus)
                                             imp).getStackIndex(c, z,
                                                                t + twh + 1);
                        ((float[][]) tWinPix)[wmax] = vsPixels((ImageStack)
                                                                 inStack,
                                                               newPixIndex);
                    } else {
                        wmax -= 1;
                    }
                    IJ.showProgress(progressCtr++, nc * nz * nt);
                    t++;
                }
                z++;
            }
            c++;
        }
        ImagePlus result = new ImagePlus((String)
                                           ("FG_" +
                                            ((ImagePlus) imp).getTitle()),
                                         (ImageStack) outStack);
        ((ImagePlus) result).setDimensions(nc, nz, nt);
        ((ImagePlus) result).setOpenAsHyperStack(true);
        return (ImagePlus) result;
    }
    
    /**
    
     * Return a float array of variance-stabilized pixels for a given
    
     * stack slice - applies Anscombe transform.
    
     */
    final float[] vsPixels(ImageStack stack, int index) {
        ImageProcessor ip = ((ImageStack) stack).getProcessor(index);
        FloatProcessor fp = (FloatProcessor)
                              (FloatProcessor)
                                ((ImageProcessor) ip).convertToFloat();
        labeled_0:
        {
            float[] pix = (float[]) (float[]) ((FloatProcessor) fp).getPixels();
            int i = 0;
            while (i < ((float[]) pix).length) {
                double raw = (double) ((float[]) pix)[i];
                double transf = 2 * Math.sqrt(raw + 3 / 8);
                ((float[]) pix)[i] = (float) transf;
                i++;
            }
        }
        return (float[]) pix;
    }
    
    /** Calculate foreground pixel array using for tCurr using tWinPix. */
    final float[] calcFg(float[][] tWinPix, int wcurr, int wmin, int wmax) {
        labeled_1:
        {
            float sd = estimStdev((float[][]) tWinPix, wmin, wmax);
            int numPix = width * height;
            float[] fgPix = (float[]) (new float[numPix]);
            int v = 0;
            while (v < numPix) {
                float[] tvec = getTvec((float[][]) tWinPix, v, wmin, wmax);
                float median = median((float[]) tvec);
                float currPix = ((float[]) ((float[][]) tWinPix)[wcurr])[v];
                ((float[]) fgPix)[v] = calcFgProb(currPix, median, sd);
                v++;
            }
        }
        return (float[]) fgPix;
    }
    
    /** Build time vector for this pixel for  given window. */
    final float[] getTvec(float[][] tWinPix, int v, int wmin, int wmax) {
        labeled_2:
        {
            float[] tvec = (float[]) (new float[wmax - wmin + 1]);
            int w = wmin;
            while (w <= wmax) {
                ((float[]) tvec)[w] = ((float[]) ((float[][]) tWinPix)[w])[v];
                w++;
            }
        }
        return (float[]) tvec;
    }
    
    /** Calculate median of an array of floats. */
    final float median(float[] m) {
        Arrays.sort((float[]) m);
        int middle = ((float[]) m).length / 2;
        if (((float[]) m).length % 2 == 1) {
            return ((float[]) m)[middle];
        } else {
            return (((float[]) m)[middle - 1] + ((float[]) m)[middle]) / 2.0F;
        }
    }
    
    /** Remove first array of pixels and shift the others to the left. */
    final float[][] rmFirst(float[][] tWinPix, int wmax) {
        labeled_3:
        {
            int i = 0;
            while (i < wmax) {
                ((float[][]) tWinPix)[i] = (float[]) ((float[][]) tWinPix)[i + 1];
                i++;
            }
        }
        return (float[][]) tWinPix;
    }
    
    /**
    
     * Estimate Stdev for this time window using random 0.1% of tvecs.
    
     * Returns the average (mean) stdev of a sample of random tvecs.
    
     */
    final float estimStdev(float[][] tWinPix, int wmin, int wmax) {
        float sd = 0;
        int pixArrayLen = ((float[]) ((float[][]) tWinPix)[0]).length;
        int samples = ((float[][]) tWinPix).length * pixArrayLen / 1000;
        int n = 0;
        while (n < samples) {
            int randPix = (int) Math.floor(Math.random() * pixArrayLen);
            float[] tvec = getTvec((float[][]) tWinPix, randPix, wmin, wmax);
            sd += calcSD((float[]) tvec) / samples;
            n++;
        }
        return sd;
    }
    
    /** Standard deviation of a vector of float values. */
    final float calcSD(float[] vec) {
        labeled_4:
        {
            float sd = 0;
            float mean = 0;
            float variance = 0;
            {
                float[] extfor$arr = (float[]) vec;
                int extfor$iter$1 = 0;
                while (extfor$iter$1 < ((float[]) extfor$arr).length) {
                    float v = ((float[]) extfor$arr)[extfor$iter$1];
                    extfor$iter$1 = extfor$iter$1 + 1;
                    { mean += v; }
                }
            }
        }
        labeled_5:
        {
            mean /= ((float[]) vec).length;
            {
                float[] extfor$arr$2 = (float[]) vec;
                int extfor$iter$3 = 0;
                while (extfor$iter$3 < ((float[]) extfor$arr$2).length) {
                    float v = ((float[]) extfor$arr$2)[extfor$iter$3];
                    extfor$iter$3 = extfor$iter$3 + 1;
                    { variance += (mean - v) * (mean - v); }
                }
            }
        }
        variance /= ((float[]) vec).length;
        sd = (float) Math.sqrt(variance);
        return sd;
    }
    
    /**
    
     * Calculate foreground probability for a pixel using tvec median & stdev.
    
     * foreground probability, P(x,y,z,t) = Q(-v), where:
    
     * v = [I(x,y,z,t) - Ibg(x,y,z,t) - k*sigma]/sigma ;
    
     * I and Ibg are Intensity and background intensity (i.e. temporal median);
    
     * sigma is standard deviation of intensity over time ;
    
     * Q is the Q-function (see calcQ).
    
     *
    
     */
    final float calcFgProb(float currPix, float median, float sd) {
        float fgProb;
        fgProb = (currPix - median - (float) nsd * sd) / sd;
        fgProb = calcQ(-fgProb);
        return fgProb;
    }
    
    /**
    
     * Calculate Q-function, Q(v) = 0.5*(1 - erf(v/sqrt(2))) ;
    
     * where erf is the error function ;
    
     * see: see http://en.wikipedia.org/wiki/Q-function
    
     */
    static final float calcQ(float v) {
        final float root2 = 1.4142135F;
        float Q;
        Q = 0.5F * (1.0F - calcErf(v / root2));
        return Q;
    }
    
    /**
    
     * Calculate the error function. See e.g.:
    
     * http://en.wikipedia.org/wiki/Error_function
    
     */
    static final float calcErf(float v) {
        float erf;
        final float a1 = 0.278393F;
        final float a2 = 0.230389F;
        final float a3 = 9.72E-4F;
        final float a4 = 0.078108F;
        boolean neg = false;
        if (v < 0) {
            v = -v;
            neg = true;
        }
        erf = 1.0F -
                (float)
                  (1.0 /
                     Math.pow((double)
                                (1.0 + a1 * v + a2 * v * v + a3 * v * v * v +
                                   a4 * v * v * v * v), 4.0));
        if (neg) { erf = -erf; }
        return erf;
    }
    
    public void showAbout() {
        IJ.showMessage(
             "TemporalMedian",
             "A probabilistic temporal median filter, as described in " +
               "Parton et al. (2011), JCB 194 (1): 121.");
    }
    
    /** Main method for debugging - loads a test imp from Fiji wiki. */
    public static void main(String[] args) {
        Class clazz = TemporalMedian_.class;
        float i = -2;
        while (i < 2) {
            System.out.println((String) ("erf(" + i + ") = " + calcErf(i)));
            System.out.println((String) ("Q(" + i + ") = " + calcQ(i)));
            i += 0.2;
        }
        new ImageJ();
        ImagePlus image =
          IJ.openImage("http://fiji.sc/tinevez/TrackMate/FakeTracks.tif");
        ((ImagePlus) image).show();
        IJ.runPlugIn(((Class) clazz).getName(), "");
    }
    
    public TemporalMedian_() { super(); }
}
