package IJ_NLMeans.src.main.java.de.biomedical_imaging.ij.nlMeansPlugin;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pascal
 */
public class NLMeansDenoising_ implements PlugInFilter {
    private final int weightPrecision = 1000;
    private ImagePlus imp;
    private int width;
    private int height;
    private int[][] pixels;
    private int widthE;
    private int heightE;
    private int w;
    private int n;
    private double sigma2;
    private double h2;
    private int distConst;
    private int dim;
    private int nextdx;
    private int nextdy;
    private long[][] uL;
    private long[] wMaxArrL;
    private long[] wSumArrL;
    private boolean autoEstimate = false;
    private int constantSigma = 15;
    private int smoothingFactor = 1;
    private int[] usedSigmas;
    
    public int setup(String arg, ImagePlus imp) {
        if (((String) arg).equals("final")) {
            labeled_0:
            {
                String sigmas = (String) ("" + usedSigmas[0]);
                int i = 1;
                while (i < usedSigmas.length) {
                    sigmas += (String) ("," + usedSigmas[i]);
                    i++;
                }
            }
            Prefs.set("nlmeans.sigma", (String) sigmas);
            Prefs.set("nlmeans.smoothingfactor", smoothingFactor);
            return DONE;
        }
        this.imp = (ImagePlus) imp;
        GenericDialog gd = new GenericDialog("Non-Local Means");
        ((GenericDialog) gd).addNumericField("Sigma", 15, 0);
        ((GenericDialog) gd).addNumericField("Smoothing_Factor", 1, 0);
        ((GenericDialog) gd).addCheckbox("Auto estimate sigma", false);
        ((GenericDialog) gd).addHelp("http://fiji.sc/Non_Local_Means_Denoise");
        ((GenericDialog) gd).showDialog();
        if (((GenericDialog) gd).wasCanceled()) { return -1; }
        constantSigma = (int) ((GenericDialog) gd).getNextNumber();
        smoothingFactor = (int) ((GenericDialog) gd).getNextNumber();
        autoEstimate = ((GenericDialog) gd).getNextBoolean();
        usedSigmas = (int[])
                       (new int[((ImagePlus) imp).getNSlices() *
                                  ((ImagePlus) imp).getNFrames()]);
        return IJ.setupDialog((ImagePlus) imp, DOES_ALL + FINAL_PROCESSING);
    }
    
    public void run(ImageProcessor ip) {
        int sigma = constantSigma;
        if (autoEstimate) {
            sigma = (int) getGlobalNoiseLevel((ImageProcessor) ip);
        }
        usedSigmas[((ImageProcessor) ip).getSliceNumber() - 1] = sigma;
        sigma = smoothingFactor * sigma;
        applyNonLocalMeans((ImageProcessor) ip, sigma);
    }
    
    /**
     * 
     * @param ip Image which should be denoised
     * @param sigma Estimated standard deviation of noise
     */
    public void applyNonLocalMeans(ImageProcessor ip, int sigma) {
        initSettings(sigma, (ImageProcessor) ip);
        try {
            int width = 512;
            int height = 512;
            double[][] result = NLMeansDenoising((ImageProcessor) ip, width,
                                                 height);
            createPicture((double[][]) result, (ImageProcessor) ip);
        }
        catch (InterruptedException e) { ((Throwable) e).printStackTrace(); }
    }
    
    private double[][] NLMeansDenoising(ImageProcessor ip, int windowWidth,
                                        int windowHeight)
          throws InterruptedException {
        double[][] result = (double[][])
                          (new double[dim][((ImageProcessor)
                                              ip).getWidth() *
                                             ((ImageProcessor)
                                                ip).getHeight()]);
        int ys = 0;
        while (ys < ((ImageProcessor) ip).getHeight()) {
            int xs = 0;
            while (xs < ((ImageProcessor) ip).getWidth()) {
                int imagePartWidth = windowWidth + xs >
                  ((ImageProcessor) ip).getWidth()
                  ? windowWidth -
                  (windowWidth + xs - ((ImageProcessor) ip).getWidth())
                  : windowWidth;
                int imagePartHeight = windowHeight + ys >
                  ((ImageProcessor) ip).getHeight()
                  ? windowHeight -
                  (windowHeight + ys - ((ImageProcessor) ip).getHeight())
                  : windowHeight;
                int[][] imagePartE =
                  expandImage(pixels, xs, ys, imagePartWidth, imagePartHeight,
                              ((ImageProcessor) ip).getWidth(),
                              ((ImageProcessor) ip).getHeight(), false);
                double[][] partResult =
                  NLMeansMultithreadInstance(
                    (int[][]) imagePartE,
                    Runtime.getRuntime().availableProcessors(), imagePartWidth,
                    imagePartHeight);
                nextdx = -w;
                nextdy = -w;
                int ystart = ys;
                int xstart = xs;
                int y = ystart;
                while (y < ystart + imagePartHeight) {
                    int x = xstart;
                    while (x < xstart + imagePartWidth) {
                        int d = 0;
                        while (d < dim) {
                            ((double[])
                               ((double[][]) result)[d])[y *
                                                           ((ImageProcessor)
                                                              ip).getWidth() +
                                                           x] =
                              ((double[])
                                 ((double[][])
                                    partResult)[d])[(y - ystart) *
                                                      imagePartWidth + x -
                                                      xstart];
                            d++;
                        }
                        x++;
                    }
                    y++;
                }
                xs += windowWidth;
            }
            ys += windowHeight;

        }

        return (double[][]) result;
    }
    
    /**
     * Multi Threaded Implementation of the Non-local Means Algorithm.
     * This accelerated Version is based of: Darbon, Jérôme, et al. 
     * "Fast nonlocal filtering applied to electron cryomicroscopy." 
     * Biomedical Imaging: From Nano to Macro, 2008. ISBI 2008. 
     * 5th IEEE International Symposium on. IEEE, 2008.
     * @param image The image as Integer Array. Colors are stored within first 
     * dimension of Array. Gets computed via convertImage()
     * @param threadcount Number of Threads used for Denoising
     * @throws InterruptedException 
     */
    private double[][] NLMeansMultithreadInstance(int[][] image,
                                                  int threadcount, int width,
                                                  int height)
          throws InterruptedException {
        int widthE = width + 2 * w + 2 * n;
        int heightE = height + 2 * w + 2 * n;
        long[][] u = (long[][]) (new long[dim][widthE * heightE]);
        long[] wMaxArr = (long[]) (new long[widthE * heightE]);
        long[] wSumArr = (long[]) (new long[widthE * heightE]);
        List workerList = new ArrayList(threadcount);
        int i = 0;
        while (i < threadcount) {
            Worker worker = new Worker(width, height, (int[][]) image,
                                       (long[][]) u, (long[]) wMaxArr,
                                       (long[]) wSumArr);
            ((Thread) worker).start();
            ((List) workerList).add((Worker) worker);
            i++;
        }
        {
            java.util.Iterator extfor$iter = ((List) workerList).iterator();
            while (((java.util.Iterator) extfor$iter).hasNext()) {
                Worker worker = (Worker)
                                  (Worker)
                                    ((java.util.Iterator) extfor$iter).next();
                { ((Thread) worker).join(); }
            }
        }
        return finishPicture((long[][]) u, (int[][]) image, (long[]) wMaxArr,
                             (long[]) wSumArr, width, height);
    }
    
    private synchronized void deliverImagePart(long[][] imagePart, long[][] u,
                                               int widthE, int heightE) {
        labeled_1:
        {
            int y = 0;
            while (y < heightE) {
                int offset = y * widthE;
                int x = 0;
                while (x < widthE) {
                    int d = 0;
                    while (d < dim) {
                        ((long[]) ((long[][]) u)[d])[offset + x] +=
                          ((long[]) ((long[][]) imagePart)[d])[offset + x];
                        d++;
                    }
                    x++;
                }
                y++;
            }
        }
    }
    
    /**
     * This Method is used to deliver a partial result of the Weight Sum Array.
     * The Weight Sum Array stores the sum of all Weights that are used
     * for each pixel. It is used within finishPicture(...) to properly Scale
     * each Pixel.
     * @param arr Weight Sum Array
     */
    private synchronized void deliverWSumArr(long[] arr, long[] wSumArr,
                                             int widthE, int heightE) {
        labeled_2:
        {
            int y = 0;
            while (y < heightE) {
                int offset = y * widthE;
                int x = 0;
                while (x < widthE) {
                    ((long[]) wSumArr)[offset + x] += ((long[]) arr)[offset + x];
                    x++;
                }
                y++;
            }
        }
    }
    
    /**
     * This Method is used to deliver a partial result of the Weight Max Array.
     * The Weight Max Array stores the maximum Weight that is used per Pixel.
     * This Weight is used as Weight between the Pixel and itself.
     * @param arr Maximum Weight Array
     */
    private synchronized void deliverWMaxArr(long[] arr, long[] wMaxArr,
                                             int widthE, int heightE) {
        labeled_3:
        {
            int y = 0;
            while (y < heightE) {
                int offset = y * widthE;
                int x = 0;
                while (x < widthE) {
                    if (((long[]) wMaxArr)[offset + x] <
                          ((long[]) arr)[offset + x]) {
                        ((long[]) wMaxArr)[offset + x] = ((long[]) arr)[offset + x];
                    }
                    x++;
                }
                y++;
            }
        }
    }
    
    /**
     * Finishes the Picture by dividing every Pixel with the Sum of all Weights
     * for the respective Pixel, and by performing the last denoising step.
     * As last Step, the Pixels get weighted with the maximum Weight for each
     Pixel.
     * @param picture The Denoised Picture
     * @param wMaxArr Array with highest used Weight for each Pixel
     * @param wSumArr Array with Sum of Weights for each Pixel
     * @return 
     */
    private double[][] finishPicture(long[][] picture, int[][] pixelsExpand,
                                     long[] wMaxArr, long[] wSumArr, int width,
                                     int height) {
        labeled_3:
        {
            double[][] result = (double[][]) (new double[dim][width * height]);
            int wn = w + n;
            int widthE = width + 2 * wn;
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int offset2 = (y + wn) * widthE;
                int x = 0;
                while (x < width) {
                    int k = offset + x;
                    int kwn = offset2 + x + wn;
                    int d = 0;
                    while (d < ((double[][]) result).length) {
                        ((double[]) ((double[][]) result)[d])[k] =
                          ((long[]) ((long[][]) picture)[d])[kwn];
                        if (((long[]) wMaxArr)[kwn] == 0) {
                            ((double[]) ((double[][]) result)[d])[k] +=
                              ((int[]) ((int[][]) pixelsExpand)[d])[kwn];
                        } else {
                            ((double[])
                               ((double[][])
                                  result)[d])[k] += ((int[])
                                                       ((int[][])
                                                          pixelsExpand)[d])[kwn] *
                                                      ((long[]) wMaxArr)[kwn];
                            ((long[]) wSumArr)[kwn] += ((long[]) wMaxArr)[kwn];
                            ((double[])
                               ((double[][]) result)[d])[k] /= ((long[])
                                                                  wSumArr)[kwn];
                        }
                        d++;
                    }
                    x++;
                }
                y++;
            }
        }
        return (double[][]) result;
    }
    
    private void denoise(long[][] targetArr, int[][] pixelsExpand, long[][] S,
                         long[] wMaxArr, long[] wSumArr, int widthE,
                         int heightE, int dx, int dy) {
        labeled_4:
        {
            int wn = w + n;
            int y = wn;
            while (y < heightE - wn) {
                int offset = y * widthE;
                int offsetn = (y + dy) * widthE;
                int x = wn;
                while (x < widthE - wn) {
                    int k = offset + x;
                    int kn = offsetn + x + dx;
                    int weight = computeWeight((long[][]) S, widthE, x, y,
                                               weightPrecision);
                    ((long[]) wMaxArr)[k] = Math.max(weight, ((long[]) wMaxArr)[k]);
                    ((long[]) wSumArr)[k] += weight;
                    ((long[]) wMaxArr)[kn] = Math.max(weight,
                                                      ((long[]) wMaxArr)[kn]);
                    ((long[]) wSumArr)[kn] += weight;
                    int d = 0;
                    while (d < dim) {
                        int wk = weight * ((int[]) ((int[][]) pixelsExpand)[d])[k];
                        int wkn = weight *
                          ((int[]) ((int[][]) pixelsExpand)[d])[kn];
                        ((long[]) ((long[][]) targetArr)[d])[k] += wkn;
                        ((long[]) ((long[][]) targetArr)[d])[kn] += wk;
                        d++;
                    }
                    x++;
                }
                y++;
            }
        }
    }
    
    /**
     * Computes the Weight between the Pixel x,y and the Pixel that lies
     * at x + dx, y + dy. dx and dy are implicitly given because the
     * Difference Image is based on them.
     * @param S Difference Image for a dx / dy pair
     * @param x x-Coordinate of the current Pixel
     * @param y y-Coordinate of the current Pixel
     * @param precision Precision of the Weight. Should be multiple of 10
     * @return 
     */
    private int computeWeight(long[][] S, int widthE, int x, int y,
                              int precision) {
        double distance = computeDistance((long[][]) S, widthE, x, y);
        double exp = Math.max(distance - sigma2, 0.0);
        double weight = h2 / (h2 + exp);
        return FastMathStuff.fastRound(weight * precision);
    }
    
    /**
     * Computes the Difference between the Surroundings of the Pixel x,y and the
     
     * Pixel that lies at x + dx, y + dy. dx and dy are implicitly given 
     * because the Difference Image is based on them.
     * Is used to compute the Weights. 
     * @param S Difference Image for a dx / dy pair
     * @param x x-Coordinate of the current Pixel
     * @param y y-Coordinate of the current Pixel
     * @return 
     */
    private double computeDistance(long[][] S, int widthE, int x, int y) {
        double distance = 0;
        int d = 0;
        labeled_5:
        {
            while (d < dim) {
                distance += ((long[]) ((long[][]) S)[d])[(y + n) * widthE +
                                                           (x + n)] +
                              ((long[]) ((long[][]) S)[d])[(y - n) * widthE +
                                                             (x - n)] -
                              ((long[]) ((long[][]) S)[d])[(y - n) * widthE +
                                                             (x + n)] -
                              ((long[]) ((long[][]) S)[d])[(y + n) * widthE +
                                                             (x - n)];
                d++;
            }
        }
        return distance;
    }
    
    /**
     * Computes the Difference Image for a given dx / dy Pair. As dx and dy can
     * be negative, image needs to be expanded to prevent out of bounds errors.
     * @param image Expanded Version of Original Image
     * @param targetArr Target Array in which the Difference Image gets stored
     into
     * @param dx
     * @param dy 
     */
    private void computeDifferenceImage(int[][] image, long[][] targetArr,
                                        int dx, int dy, int widthE,
                                        int heightE) {
        labeled_6:
        {
            int wn = w + n;
            long temp;
            int d = 0;
            while (d < dim) {
                temp = ((int[]) ((int[][]) image)[d])[wn * widthE + wn] -
                         ((int[]) ((int[][]) image)[d])[(wn + dy) * widthE + dx +
                                                          wn];
                ((long[]) ((long[][]) targetArr)[d])[wn * widthE + wn] = temp *
                                                                           temp;
                d++;
            }
        }
        labeled_7:
        {
            int offset = wn * widthE;
            int offsetdy = (wn + dy) * widthE;
            int x = wn + 1;
            while (x < widthE) {
                d = 0;
                while (d < dim) {
                    temp = ((int[]) ((int[][]) image)[d])[offset + x] -
                             ((int[]) ((int[][]) image)[d])[offsetdy + x + dx];
                    ((long[]) ((long[][]) targetArr)[d])[offset + x] =
                      ((long[]) ((long[][]) targetArr)[d])[offset + x - 1] + temp *
                        temp;
                    d++;
                }
                x++;
            }
        }
        labeled_8:
        {
            int y = wn + 1;
            while (y < heightE) {
                int offsety = y * widthE;
                offsetdy = (y + dy) * widthE;
                d = 0;
                while (d < dim) {
                    temp = ((int[]) ((int[][]) image)[d])[offsety + wn] -
                             ((int[]) ((int[][]) image)[d])[offsetdy + wn + dx];
                    ((long[]) ((long[][]) targetArr)[d])[offsety + wn] =
                      ((long[]) ((long[][]) targetArr)[d])[offsety - widthE + wn] +
                        temp * temp;
                    d++;
                }
                y++;
            }
        }
        labeled_9:
        {
            y = wn + 1;
            while (y < heightE) {
                offset = y * widthE;
                int offset2 = (y + dy) * widthE;
                x = wn + 1;
                while (x < widthE) {
                    d = 0;
                    while (d < dim) {
                        ((long[]) ((long[][]) targetArr)[d])[offset + x] =
                          ((long[]) ((long[][]) targetArr)[d])[offset + x - 1];
                        ((long[]) ((long[][]) targetArr)[d])[offset + x] +=
                          ((long[]) ((long[][]) targetArr)[d])[offset + x - widthE];
                        ((long[]) ((long[][]) targetArr)[d])[offset + x] -=
                          ((long[]) ((long[][]) targetArr)[d])[offset + x - 1 -
                                                                 widthE];
                        temp = ((int[]) ((int[][]) image)[d])[offset + x] -
                                 ((int[]) ((int[][]) image)[d])[offset2 + x + dx];
                        double temp2 = temp * temp;
                        ((long[]) ((long[][]) targetArr)[d])[offset + x] += temp2;
                        d++;
                    }
                    x++;
                }
                y++;
            }
        }
    }
    
    /**
     * Expands the boundaries of an image in all four directions. The new
     content
     * of the Image gets filled with the adjacent parts of the Image.
     * To view a Preview of this Image, use display = true
     * @param image Original Image
     * @param display Display Preview of generated Image
     * @return 
     */
    private int[][] expandImage(int[][] image, int xstart, int ystart,
                                int width, int height, int orgWidth,
                                int orgHeight, boolean display) {
        labeled_10:
        {
            int heightE = height + 2 * w + 2 * n;
            int widthE = width + 2 * w + 2 * n;
            int[][] result = (int[][]) (new int[dim][widthE * heightE]);
            int y = 0;
            while (y < heightE) {
                int yr = y - w - n + ystart;
                if (yr >= orgHeight) yr = yr - orgHeight;
                if (yr < 0) yr = height + yr;
                int offset = y * widthE;
                int offsetr = yr * orgWidth;
                int x = 0;
                while (x < widthE) {
                    int xr = x + (xstart - w - n);
                    if (xr >= orgWidth) xr = xr - orgWidth;
                    if (xr < 0) xr = width + xr;
                    int d = 0;
                    while (d < dim) {
                        ((int[]) ((int[][]) result)[d])[offset + x] =
                          ((int[]) ((int[][]) image)[d])[offsetr + xr];
                        d++;
                    }
                    x++;
                }
                y++;
            }
        }
        if (display) {
            labeled_11:
            {
                int[] pixelsPicture = (int[])
                                    (new int[((int[])
                                                ((int[][]) result)[0]).length]);
                y = 0;
                while (y < heightE) {
                    int offset = y * widthE;
                    int z = 0;
                    while (z < widthE) {
                        int p = offset + z;
                        int red = (int) ((int[]) ((int[][]) result)[0])[p];
                        int green = (int) ((int[]) ((int[][]) result)[1])[p];
                        int blue = (int) ((int[]) ((int[][]) result)[2])[p];
                        int pixel = ((red & 255) << 16) + ((green & 255) << 8) +
                          (blue & 255);
                        ((int[]) pixelsPicture)[p] = pixel;
                        z++;
                    }
                    y++;
                }
            }
            BufferedImage bimg = convertToImage(widthE, heightE,
                                                (int[]) pixelsPicture);
            ImagePlus imp2 = new ImagePlus("Expanded Image",
                                           (java.awt.Image) bimg);
            ((ImagePlus) imp2).show();
        }
        return (int[][]) result;
    }
    
    /**
     * Implements the gaussian noise level estimation algorithm of 
     * Immerkaer, J., 1996. Fast noise variance estimation. 
     * Computer Vision and Image Understanding, 
     * 64(2), pp.300\u2013302.
     * @return noise level
     */
    public static double getGlobalNoiseLevel(ImageProcessor ip) {
        labeled_12:
        {
            FloatProcessor fp = null;
            if (((ImageProcessor) ip).getBitDepth() == 8) {
                ByteProcessor bp = (ByteProcessor) (ByteProcessor) ip;
                fp = ((ByteProcessor) bp).duplicate().convertToFloatProcessor();
            } else
                if (((ImageProcessor) ip).getBitDepth() == 24) {
                    ColorProcessor cp = (ColorProcessor) (ColorProcessor) ip;
                    fp = ((ColorProcessor)
                            cp).duplicate().convertToFloatProcessor();
                } else
                    if (((ImageProcessor) ip).getBitDepth() == 16) {
                        ShortProcessor sp = (ShortProcessor) (ShortProcessor) ip;
                        fp = ((ShortProcessor)
                                sp).duplicate().convertToFloatProcessor();
                    } else
                        if (((ImageProcessor) ip).getBitDepth() == 32) {
                            fp = (FloatProcessor)
                                   (FloatProcessor)
                                     ((ImageProcessor) ip).duplicate();
                        }
            Convolver convolver = new Convolver();
            int k = 6;
            float[] kernel = generateKernel(k);
            ((Convolver) convolver).convolve((ImageProcessor) fp, (float[]) kernel,
                                             2 * k + 1, 2 * k + 1);
            int w = ((ImageProcessor) fp).getWidth();
            int h = ((ImageProcessor) fp).getHeight();
            double sum = 0;
            double sub = 2 * k;
            int x = 0;
            while (x < w) {
                int y = 0;
                while (y < h) {
                    sum += Math.abs(((FloatProcessor) fp).getPixelValue(x, y));
                    y++;
                }
                x++;
            }
        }
        double sigma = Math.sqrt(Math.PI / 2) * 1.0 /
          (6.0 * (w - sub) * (h - sub)) * sum;
        return sigma;
    }
    
    public static float[] generateKernel(int k) {
        int n = 2 * k + 1;
        float[] kernel = (float[]) (new float[n * n]);
        int i = 0;
        while (i < n * n) {
            ((float[]) kernel)[i] = 0;
            i++;
        }
        ((float[]) kernel)[0] = 1;
        ((float[]) kernel)[k] = -2;
        ((float[]) kernel)[2 * k] = 1;
        ((float[]) kernel)[k * n] = -2;
        ((float[]) kernel)[k * n + k] = 4;
        ((float[]) kernel)[k * n + 2 * k] = -2;
        ((float[]) kernel)[2 * k * n] = 1;
        ((float[]) kernel)[2 * k * n + k] = -2;
        ((float[]) kernel)[2 * k * n + 2 * k] = 1;
        return (float[]) kernel;
    }
    
    /**
     * Initialize needed Settings
     * @param sigma An estimate of the standard deviation of the Noise-Level 
     * within the Image
     * @param ip The Image-Processor of the original Image
     */
    private void initSettings(int sigma, ImageProcessor ip) {
        int type = new ImagePlus(null, (ImageProcessor) ip).getType();
        double hfactor;
        if (type == ImagePlus.COLOR_256 || type == ImagePlus.COLOR_RGB) {
            if (sigma > 0 && sigma <= 25) {
                n = 1;
                w = 10;
                hfactor = 0.55;
            } else
                if (sigma > 25 && sigma <= 55) {
                    n = 2;
                    w = 17;
                    hfactor = 0.4;
                } else {
                    n = 3;
                    w = 17;
                    hfactor = 0.35;
                }
        } else {
            if (sigma > 0 && sigma <= 15) {
                n = 1;
                w = 10;
                hfactor = 0.4;
            } else
                if (sigma > 15 && sigma <= 30) {
                    n = 2;
                    w = 10;
                    hfactor = 0.4;
                } else
                    if (sigma > 30 && sigma <= 45) {
                        n = 3;
                        w = 17;
                        hfactor = 0.35;
                    } else
                        if (sigma > 45 && sigma <= 75) {
                            n = 4;
                            w = 17;
                            hfactor = 0.35;
                        } else {
                            n = 5;
                            w = 17;
                            hfactor = 0.3;
                        }
        }
        width = ((ImageProcessor) ip).getWidth();
        height = ((ImageProcessor) ip).getHeight();
        widthE = width + 2 * w + 2 * n;
        heightE = height + 2 * w + 2 * n;
        convertPixels((ImageProcessor) ip, type);
        double h = hfactor * sigma;
        sigma2 = sigma * sigma * 2 * (dim * (2 * n + 1) * (2 * n + 1));
        distConst = dim * (2 * n + 1) * (2 * n + 1);
        h2 = h * h;
        nextdx = -w;
        nextdy = -w;
    }
    
    /**
     * Returns next dx / dy Pair
     * dx and dy are needed to compute a specific iteration of the Algorithm.
     * This method provides the next unused dx / dy Pair to be used in a 
     * denoising Thread.
     * @return dx and dy as int array, in this respective order
     */
    private synchronized int[] getNextDV() {
        if (nextdy > 0) return null;
        int[] result = (int[]) (new int[] { nextdx, nextdy });
        if (nextdx == w) {
            nextdy++;
            nextdx = -w;
        } else {
            nextdx++;
        }
        return (int[]) result;
    }
    
    /**
     * Converts the Image into its proper form sothat it can be used by the 
     * Algorithm
     * @param ip
     * @param type Type of the Image based on ImageJ ImageTypes
     */
    private void convertPixels(ImageProcessor ip, int type) {
        if (type == ImagePlus.COLOR_256)
            convertColor256((ImageProcessor) ip);
        else
            if (type ==
                  ImagePlus.COLOR_RGB)
                convertRGB((ImageProcessor) ip);
            else
                if (type ==
                      ImagePlus.GRAY16)
                    convertGray16((ImageProcessor) ip);
                else
                    if (type ==
                          ImagePlus.GRAY32)
                        convertGray32((ImageProcessor) ip);
                    else
                        if (type ==
                              ImagePlus.GRAY8)
                            convertGray8((ImageProcessor) ip);
    }
    
    private void convertColor256(ImageProcessor ip) {
        labeled_13:
        {
            dim = 1;
            byte[] pixelArray = (byte[]) (byte[]) ((ImageProcessor) ip).getPixels();
            pixels = (int[][]) (new int[dim][width * height]);
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int x = 0;
                while (x < width) {
                    int pos = offset + x;
                    ((int[]) pixels[0])[pos] = ((byte[]) pixelArray)[pos] & 255;
                    x++;
                }
                y++;
            }
        }
    }
    
    private void convertRGB(ImageProcessor ip) {
        labeled_14:
        {
            dim = 3;
            int[] pixelArray = (int[]) (int[]) ((ImageProcessor) ip).getPixels();
            pixels = (int[][]) (new int[dim][width * height]);
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int x = 0;
                while (x < width) {
                    int qtemp = ((int[]) pixelArray)[offset + x];
                    ((int[]) pixels[0])[offset + x] = (qtemp & 16711680) >> 16;
                    ((int[]) pixels[1])[offset + x] = (qtemp & 65280) >> 8;
                    ((int[]) pixels[2])[offset + x] = qtemp & 255;
                    x++;
                }
                y++;
            }
        }
    }
    
    private void convertGray32(ImageProcessor ip) {
        labeled_15:
        {
            dim = 1;
            float[] pixelArray = (float[])
                                   (float[]) ((ImageProcessor) ip).getPixels();
            pixels = (int[][]) (new int[dim][width * height]);
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int x = 0;
                while (x < width) {
                    int pos = offset + x;
                    ((int[]) pixels[0])[pos] = (int) ((float[]) pixelArray)[pos];
                    x++;
                }
                y++;
            }
        }
    }
    
    private void convertGray16(ImageProcessor ip) {
        labeled_16:
        {
            dim = 1;
            short[] pixelArray = (short[])
                                   (short[]) ((ImageProcessor) ip).getPixels();
            pixels = (int[][]) (new int[dim][width * height]);
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int x = 0;
                while (x < width) {
                    int pos = offset + x;
                    ((int[]) pixels[0])[pos] = (int)
                                                 (((short[]) pixelArray)[pos] &
                                                    65535);
                    x++;
                }
                y++;
            }
        }
    }
    
    private void convertGray8(ImageProcessor ip) {
        labeled_17:
        {
            dim = 1;
            byte[] pixelArray = (byte[]) (byte[]) ((ImageProcessor) ip).getPixels();
            pixels = (int[][]) (new int[dim][width * height]);
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int x = 0;
                while (x < width) {
                    int pos = offset + x;
                    ((int[]) pixels[0])[pos] = (int)
                                                 (((byte[]) pixelArray)[pos] & 255);
                    x++;
                }
                y++;
            }
        }
    }
    
    /**
     * Converts a denoised Picture back to its original Format and saves it
     * in the ImageProcessor
     * @param image
     * @param ip 
     */
    private void createPicture(double[][] image, ImageProcessor ip) {
        if (((ImageProcessor) ip).getBitDepth() == 8) {
            createPicture8Bit((double[][]) image, (ImageProcessor) ip);
        } else
            if (((ImageProcessor) ip).getBitDepth() == 24) {
                createPicture24Bit((double[][]) image, (ImageProcessor) ip);
            } else
                if (((ImageProcessor) ip).getBitDepth() == 16) {
                    createPicture16Bit((double[][]) image, (ImageProcessor) ip);
                } else
                    if (((ImageProcessor) ip).getBitDepth() == 32) {
                        createPicture32Bit((double[][]) image,
                                           (ImageProcessor) ip);
                    }
    }
    
    private void createPicture24Bit(double[][] image, ImageProcessor ip) {
        labeled_18:
        {
            int[] pixelsPicture = (int[]) (int[]) ((ImageProcessor) ip).getPixels();
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int x = 0;
                while (x < width) {
                    int p = offset + x;
                    int red = (int) ((double[]) ((double[][]) image)[0])[p];
                    int green = (int) ((double[]) ((double[][]) image)[1])[p];
                    int blue = (int) ((double[]) ((double[][]) image)[2])[p];
                    int pixel = ((red & 255) << 16) + ((green & 255) << 8) +
                      (blue & 255);
                    ((int[]) pixelsPicture)[p] = pixel;
                    x++;
                }
                y++;
            }
        }
        ((ImageProcessor) ip).setPixels(pixelsPicture);
    }
    
    private void createPicture32Bit(double[][] image, ImageProcessor ip) {
        labeled_19:
        {
            float[] pixelsPicture = (float[])
                                  (float[]) ((ImageProcessor) ip).getPixels();
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int x = 0;
                while (x < width) {
                    int pos = offset + x;
                    float pixel = (float) ((double[]) ((double[][]) image)[0])[pos];
                    ((float[]) pixelsPicture)[pos] = pixel;
                    x++;
                }
                y++;
            }
        }
        ((ImageProcessor) ip).setPixels(pixelsPicture);
    }
    
    private void createPicture16Bit(double[][] image, ImageProcessor ip) {
        labeled_20:
        {
            short[] pixelsPicture = (short[])
                                  (short[]) ((ImageProcessor) ip).getPixels();
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int x = 0;
                while (x < width) {
                    int pos = offset + x;
                    short pixel = (short) ((double[]) ((double[][]) image)[0])[pos];
                    ((short[]) pixelsPicture)[pos] = pixel;
                    x++;
                }
                y++;
            }
        }
        ((ImageProcessor) ip).setPixels(pixelsPicture);
    }
    
    private void createPicture8Bit(double[][] image, ImageProcessor ip) {
        labeled_21:
        {
            byte[] pixelsPicture = (byte[])
                                 (byte[]) ((ImageProcessor) ip).getPixels();
            int y = 0;
            while (y < height) {
                int offset = y * width;
                int x = 0;
                while (x < width) {
                    int pos = offset + x;
                    byte pixel = (byte) ((double[]) ((double[][]) image)[0])[pos];
                    ((byte[]) pixelsPicture)[pos] = pixel;
                    x++;
                }
                y++;
            }
        }
        ((ImageProcessor) ip).setPixels(pixelsPicture);
    }
    
    public static BufferedImage convertToImage(int width, int height,
                                               int[] pixels) {
        labeled_22:
        {
            int wh = width * height;
            int[] newPixels = (int[]) (new int[wh * 3]);
            int i = 0;
            while (i < wh) {
                int rgb = ((int[]) pixels)[i];
                int red = rgb >> 16 & 255;
                int green = rgb >> 8 & 255;
                int blue = rgb & 255;
                ((int[]) newPixels)[i * 3] = red;
                ((int[]) newPixels)[i * 3 + 1] = green;
                ((int[]) newPixels)[i * 3 + 2] = blue;
                i++;
            }
        }
        BufferedImage image = new BufferedImage(width, height,
                                                BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = (WritableRaster)
                                  (WritableRaster)
                                    ((BufferedImage) image).getData();
        ((WritableRaster) raster).setPixels(0, 0, width, height,
                                            (int[]) newPixels);
        ((BufferedImage) image).setData((java.awt.image.Raster) raster);
        return (BufferedImage) image;
    }
    
    class Worker extends Thread {
        private int[][] image;
        private long[][] u;
        private long[] wMaxArr;
        private long[] wSumArr;
        int width;
        int height;
        
        public Worker() { super(); }
        
        public Worker(int width, int height, int[][] image, long[][] u,
                      long[] wMaxArr, long[] wSumArr) {
            super();
            this.width = width;
            this.height = height;
            this.image = (int[][]) image;
            this.u = (long[][]) u;
            this.wMaxArr = (long[]) wMaxArr;
            this.wSumArr = (long[]) wSumArr;
        }
        
        public void run() {
            int[] vec;
            int dx;
            int dy;
            int heightE = height + 2 * w + 2 * n;
            int widthE = width + 2 * w + 2 * n;
            long[] TwMaxArr = (long[]) (new long[widthE * heightE]);
            long[] TwSumArr = (long[]) (new long[widthE * heightE]);
            long[][] TimagePart = (long[][]) (new long[dim][widthE * heightE]);
            long[][] TS = (long[][]) (new long[dim][widthE * heightE]);
            vec = getNextDV();
            boolean skip_0 = false;
            while ((int[]) vec != null) {
                skip_0 = false;
                dx = ((int[]) vec)[0];
                dy = ((int[]) vec)[1];
                if (!skip_0)
                    if ((2 * w + 1) * dy + dx >= 0) {
                        vec = getNextDV();
                        skip_0 = true;
                    }
                if (!skip_0)
                    computeDifferenceImage(image, (long[][]) TS, dx, dy, widthE,
                                           heightE);
                if (!skip_0)
                    denoise((long[][]) TimagePart, image, (long[][]) TS,
                            (long[]) TwMaxArr, (long[]) TwSumArr, widthE,
                            heightE, dx, dy);
                if (!skip_0) vec = getNextDV();
            }
            deliverImagePart((long[][]) TimagePart, u, widthE, heightE);
            deliverWMaxArr((long[]) TwMaxArr, wMaxArr, widthE, heightE);
            deliverWSumArr((long[]) TwSumArr, wSumArr, widthE, heightE);
        }
    }
    
    public NLMeansDenoising_() { super(); }
}
