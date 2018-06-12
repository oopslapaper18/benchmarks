package original.stats;

import java.io.PrintWriter;

/**

 *   PlotUtil.java

 *   =============

 *

 *  Copyright (C) 2013-2014  Magdalen Berns <m.berns@sms.ed.ac.uk>

 *

 *  This program is free software: you can redistribute it and/or modify

 *  it under the terms of the GNU General Public License as published by

 *  the Free Software Foundation, either version 3 of the License, or

 *  (at your option) any later version.

 *

 *  This program is distributed in the hope that it will be useful,

 *  but WITHOUT ANY WARRANTY; without even the implied warranty of

 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the

 *  GNU General Public License for more details.



 *  You should have received a copy of the GNU General Public License

 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
public class PlotUtil extends IOUtil {
    private double[] x;
    private double[] y;
    private double[][] data;
    
    public PlotUtil(double[][] data) {
        super();
        this.data = (double[][]) data;
        x = (double[]) (new double[((double[][]) data).length]);
        y = (double[]) (new double[((double[][]) data).length]);
    }
    
    /**
    
     * x
    
     *           Pull out the x column data
    
     * @return:
    
     *         The x component of a 1d array of doubles
    
     */
    public double[] x() {
        int i = 0;
        while (i < data.length) {
            x[i] = ((double[]) data[i])[0];
            i++;
        }
        return x;
    }
    
    /**
    
     * x
    
     *           Pull out the y column data
    
     * @return
    
     *           The y component of a 1d array of doubles
    
     */
    public double[] y() {
        int i = 0;
        while (i < data.length) {
            y[i] = ((double[]) data[i])[1];
            i++;
        }
        return y;
    }
    
    /**
    
     * removeOffset
    
     *              Convenience function to remove the y intecept offset value
    
     * @return
    
     *              The 2D data array of doubles with offset removed.
    
     */
    public static double[][] removeOffset(double[][] data, double offset) {
        int i = ((double[][]) data).length - 1;
        while (i >= 0) {
            ((double[]) ((double[][]) data)[i])[0] += -offset;
            i--;
        }
        return (double[][]) data;
    }
    
    public static void writeToFile(double[][] data, PrintWriter fileOut) {
        int i = 0;
        while (i < ((double[][]) data).length) {
            ((PrintWriter) fileOut).
              printf("%2.5f %2.5f",
                     (Object[])
                       (new Object[] { Double.valueOf(((double[])
                                                         ((double[][])
                                                            data)[i])[0]),
                        Double.valueOf(((double[])
                                          ((double[][]) data)[i])[1]) }));
            ((PrintWriter) fileOut).println();
            i++;
        }
    }
}
