package original.pheonix;

import java.util.Arrays;
import java.util.List;

public class LinearRegression {
    public static class Point {
        public int x;
        public int y;
        
        public Point(int x, int y) {
            super();
            this.x = x;
            this.y = y;
        }
    }
    
    public static void main(String[] args) {
        List points = Arrays.asList((Point[])
                                      (new Point[] { new Point(100, 500),
                                       new Point(3, 1000), new Point(7, 600),
                                       new Point(300, 34) }));
        regress((List) points);
    }
    
    public static int[] regress(List points) {
        labeled_0:
        {
            int SX_ll = 0;
            int SY_ll = 0;
            int SXX_ll = 0;
            int SYY_ll = 0;
            int SXY_ll = 0;
            int i = 0;
            while (i < ((List) points).size()) {
                SX_ll += ((Point) ((List) points).get(i)).x;
                SXX_ll += ((Point) ((List) points).get(i)).x *
                            ((Point) ((List) points).get(i)).x;
                SY_ll += ((Point) ((List) points).get(i)).y;
                SYY_ll += ((Point) ((List) points).get(i)).y *
                            ((Point) ((List) points).get(i)).y;
                SXY_ll += ((Point) ((List) points).get(i)).x *
                            ((Point) ((List) points).get(i)).y;
                i++;
            }
        }
        int[] result = (int[]) (new int[5]);
        ((int[]) result)[0] = SX_ll;
        ((int[]) result)[1] = SXX_ll;
        ((int[]) result)[2] = SY_ll;
        ((int[]) result)[3] = SYY_ll;
        ((int[]) result)[4] = SXY_ll;
        return (int[]) result;
    }
    
    public LinearRegression() { super(); }
}
