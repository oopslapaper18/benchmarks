package original.pheonix;

import java.util.Arrays;
import java.util.List;

class Histogram {
    public static class Pixel {
        public int r;
        public int g;
        public int b;
        
        public Pixel(int r, int g, int b) {
            super();
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
    
    public static void main(String[] args) {
        List pixels = Arrays.asList((Pixel[])
                                      (new Pixel[] { new Pixel(10, 10, 10),
                                       new Pixel(120, 120, 120),
                                       new Pixel(210, 210, 210),
                                       new Pixel(10, 120, 210) }));
        int[] hR = (int[]) (new int[256]);
        int[] hG = (int[]) (new int[256]);
        int[] hB = (int[]) (new int[256]);
        histogram((List) pixels, (int[]) hR, (int[]) hG, (int[]) hB);
    }
    
    public static int[][] histogram(List image, int[] hR, int[] hG, int[] hB) {
        labeled_0:
        {
            int i = 0;
            while (i < ((List) image).size()) {
                int r = ((Pixel) ((List) image).get(i)).r;
                int g = ((Pixel) ((List) image).get(i)).g;
                int b = ((Pixel) ((List) image).get(i)).b;
                ((int[]) hR)[r]++;
                ((int[]) hG)[g]++;
                ((int[]) hB)[b]++;
                i += 1;
            }
        }
        int[][] result = (int[][]) (new int[3][]);
        ((int[][]) result)[0] = (int[]) hR;
        ((int[][]) result)[1] = (int[]) hG;
        ((int[][]) result)[2] = (int[]) hB;
        return (int[][]) result;
    }
    
    public Histogram() { super(); }
}
