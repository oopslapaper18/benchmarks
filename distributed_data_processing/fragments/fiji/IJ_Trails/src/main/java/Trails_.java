package IJ_Trails.src.main.java;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**

 * Trail/average intensities over a time window for an imp sequence.

 *

 * @author graemeball@googlemail.com

 */
public class Trails_ implements PlugIn {
    ImagePlus imp;
    int width;
    int height;
    int nc;
    int nz;
    int nt;
    public int twh = 2;

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (showDialog()) {
            if (((ImagePlus) imp).getNFrames() > 2 * twh + 1) {
                ImagePlus imResult = exec((ImagePlus) imp);
                ((ImagePlus) imResult).show();
            } else {
                IJ.showMessage((String) ("Insufficient time points, " + nt));
            }
        }
    }
    
    boolean showDialog() {
        GenericDialog gd = new GenericDialog("Trails");
        ((GenericDialog) gd).addNumericField("time_window half-width", twh, 0);
        ((GenericDialog) gd).showDialog();
        if (((GenericDialog) gd).wasCanceled()) return false;
        twh = (int) ((GenericDialog) gd).getNextNumber();
        return true;
    }
    
    /**
    
     * Execute time-averaging, returning trailed ImagePlus.
    
     * Builds array of pixel arrays for sliding window of time frames.
    
     *
    
     * @param imp (multi-dimensional, i.e. multiple frames)
    
     */
    public ImagePlus exec(ImagePlus imp) {
        labeled_0:
        {
            this.nt = ((ImagePlus) imp).getNFrames();
            this.nz = ((ImagePlus) imp).getNSlices();
            this.nc = ((ImagePlus) imp).getNChannels();
            this.width = ((ImagePlus) imp).getWidth();
            this.height = ((ImagePlus) imp).getHeight();
            ImageStack inStack = ((ImagePlus) imp).getStack();
            int size = ((ImageStack) inStack).getSize();
            ImageStack outStack = new ImageStack(width, height, size);
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
                        ((float[][]) tWinPix)[t - 1] = getfPixels((ImageStack)
                                inStack, index);
                        t++;
                    }
                    t = 1;
                    while (t <= nt) {
                        float[] fgPix = trail((float[][]) tWinPix, wcurr, wmin,
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
                            ((float[][]) tWinPix)[wmax] = getfPixels((ImageStack)
                                            inStack,
                                    newPixIndex);
                        } else {
                            wmax -= 1;
                        }
                        t++;
                    }
                    z++;
                }
                c++;
            }
        }
        ImagePlus result = new ImagePlus((String)
                                           ("Trail" +
                                            Integer.toString(2 * twh + 1) +
                                            "_" + ((ImagePlus) imp).getTitle()),
                                         (ImageStack) outStack);
        ((ImagePlus) result).setDimensions(nc, nz, nt);
        ((ImagePlus) result).setOpenAsHyperStack(true);
        return (ImagePlus) result;
    }
    
    /**
    
     * Return a float array of pixels for a given stack slice.
    
     */
    final float[] getfPixels(ImageStack stack, int index) {
        ImageProcessor ip = ((ImageStack) stack).getProcessor(index);
        FloatProcessor fp = (FloatProcessor)
                              (FloatProcessor)
                                ((ImageProcessor) ip).convertToFloat();
        float[] pix = (float[]) (float[]) ((FloatProcessor) fp).getPixels();
        return (float[]) pix;
    }
    
    /** Trail tCurr pixels using tWinPix time window. */
    final float[] trail(float[][] tWinPix, int wcurr, int wmin, int wmax) {
        labeled_1:
        {
            int numPix = width * height;
            float[] tPix = (float[]) (new float[numPix]);
            int v = 0;
            while (v < numPix) {
                float[] tvec = getTvec((float[][]) tWinPix, v, wmin, wmax);
                ((float[]) tPix)[v] = mean((float[]) tvec);
                v++;
            }
        }
        return (float[]) tPix;
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
    
    /** Calculate mean of array of floats. */
    final float mean(float[] tvec) {
        labeled_3:
        {
            float mean = 0;
            int t = 0;
            while (t < ((float[]) tvec).length) {
                mean += ((float[]) tvec)[t];
                t++;
            }
        }
        return mean / ((float[]) tvec).length;
    }
    
    /** Remove first array of pixels and shift the others to the left. */
    final float[][] rmFirst(float[][] tWinPix, int wmax) {
        labeled_4:
        {
            int i = 0;
            while (i < wmax) {
                ((float[][]) tWinPix)[i] = (float[]) ((float[][]) tWinPix)[i + 1];
                i++;
            }
        }
        return (float[][]) tWinPix;
    }
    
    public void showAbout() {
        IJ.showMessage("Trails",
                       "Trail/average intensities over a given time window.");
    }
    
    /** Main method for testing. */
    public static void main(String[] args) {
        Class clazz = Trails_.class;
        new ImageJ();
        ImagePlus image =
          IJ.openImage("http://fiji.sc/tinevez/TrackMate/FakeTracks.tif");
        ((ImagePlus) image).show();
        IJ.runPlugIn(((Class) clazz).getName(), "");
    }
    
    public Trails_() { super(); }
}