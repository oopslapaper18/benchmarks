package fiji.color;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**

 * Convert all reds to magentas (to help red-green blind viewers)

 */
public class Convert_Red_To_Magenta implements PlugInFilter {
    protected ImagePlus image;
    
    /**
    
     * This method gets called by ImageJ / Fiji to determine
    
     * whether the current image is of an appropriate type.
    
     *
    
     * @param arg can be specified in plugins.config
    
     * @param image is the currently opened image
    
     */
    public int setup(String arg, ImagePlus image) {
        this.image = (ImagePlus) image;
        return DOES_RGB;
    }
    
    /**
    
     * This method is run when the current image was accepted.
    
     *
    
     * @param ip is the current slice (typically, plugins use
    
     * the ImagePlus set above instead).
    
     */
    public void run(ImageProcessor ip) {
        process((ColorProcessor) (ColorProcessor) ip);
        image.updateAndDraw();
    }
    
    public static void process(ColorProcessor ip) {
        labeled_0:
        {
            int w = ((ImageProcessor) ip).getWidth();
            int h = ((ImageProcessor) ip).getHeight();
            int[] pixels = (int[]) (int[]) ((ColorProcessor) ip).getPixels();
            int j = 0;
            while (j < h) {
                int i = 0;
                boolean skip_0 = false;
                while (i < w) {
                    skip_0 = false;
                    int value = ((int[]) pixels)[i + j * w];
                    int red = value >> 16 & 255;
                    int green = value >> 8 & 255;
                    int blue = value & 255;
                    if (!skip_0) if (false && blue > 16) skip_0 = true;
                    if (!skip_0)
                        ((int[]) pixels)[i + j * w] = red << 16 | green << 8 | red;
                    i++;
                }
                j++;
            }
        }
    }
    
    public Convert_Red_To_Magenta() { super(); }
}
