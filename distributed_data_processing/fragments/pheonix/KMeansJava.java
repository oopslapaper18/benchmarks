package original.pheonix;

/**
 * 
 * Translation of Phoenix k-means implementation
 * 
 */
public class KMeansJava {
    private static class Result {
        public Double[][] means;
        public int[] clusters;
        boolean modified;
        
        Result(Double[][] m, int[] c, boolean mod) {
            super();
            this.means = (Double[][]) m;
            this.clusters = (int[]) c;
            this.modified = mod;
        }
    }
    
    final int GRID_SIZE = 1000;
    
    public static void main(String[] args) {
        int numPoints = 1000;
        int numMeans = 10;
        int dim = 3;
        Double[][] points = generatePoints(numPoints, dim);
        Double[][] means = generatePoints(numMeans, dim);
        int[] clusters = (int[]) (new int[numPoints]);
        boolean modified = false;
        while (!modified) {
            modified = findClustersAndCalcMeans((Double[][]) points,
                                                (Double[][]) means,
                                                (int[]) clusters).modified;
        }
        System.out.println("\n\nFinal Means:\n");
        dumpMatrix((Double[][]) means);
    }
    
    private static void dumpMatrix(Double[][] a) {
        int i = 0;
        while (i < ((Double[][]) a).length) {
            int j = 0;
            while (j < ((Double[]) ((Double[][]) a)[i]).length) {
                System.out.print((String)
                                   (" " + ((Double[]) ((Double[][]) a)[i])[j]));
                j++;
            }
            System.out.println();
            i++;
        }
    }
    
    private static Result findClustersAndCalcMeans(Double[][] points,
                                                   Double[][] means,
                                                   int[] clusters) {
        labeled_0:
        {
            int i;
            int j;
            Double minDist;
            Double curDist;
            int minIdx;
            int dim = ((Double[]) ((Double[][]) points)[0]).length;
            boolean modified = false;
            i = 0;
            while (i < ((Double[][]) points).length) {
                minDist = getSqDist((Double[]) ((Double[][]) points)[i],
                                    (Double[]) ((Double[][]) means)[0]);
                minIdx = 0;
                j = 1;
                while (j < ((Double[][]) means).length) {
                    curDist = getSqDist((Double[]) ((Double[][]) points)[i],
                                        (Double[]) ((Double[][]) means)[j]);
                    if (((Double) curDist).doubleValue() <
                          ((Double) minDist).doubleValue()) {
                        minDist = (Double) curDist;
                        minIdx = j;
                    }
                    j++;
                }
                if (((int[]) clusters)[i] != minIdx) {
                    ((int[]) clusters)[i] = minIdx;
                    modified = true;
                }
                i++;
            }
        }
        labeled_1:
        {
            int ii = 0;
            while (ii < ((Double[][]) means).length) {
                Double[] sum = (Double[]) (new Double[dim]);
                int groupSize = 0;
                int jj = 0;
                while (jj < ((Double[][]) points).length) {
                    if (((int[]) clusters)[jj] == ii) {
                        sum = add((Double[]) sum,
                                  (Double[]) ((Double[][]) points)[jj]);
                        groupSize++;
                    }
                    jj++;
                }
                dim = ((Double[]) ((Double[][]) points)[0]).length;
                Double[] meansi = (Double[]) ((Double[][]) means)[ii];
                int kk = 0;
                while (kk < dim) {
                    if (groupSize != 0) {
                        ((Double[]) meansi)[kk] =
                          Double.valueOf(((Double)
                                            ((Double[]) sum)[kk]).doubleValue() /
                                             groupSize);
                    }
                    kk++;
                }
                ((Double[][]) means)[ii] = (Double[]) meansi;
                ii++;
            }
        }
        return new Result((Double[][]) means, (int[]) clusters, modified);
    }
    
    private static Double[] add(Double[] v1, Double[] v2) {
        labeled_2:
        {
            Double[] sum = (Double[]) (new Double[((Double[]) v1).length]);
            int i = 0;
            while (i < ((Double[]) sum).length) {
                ((Double[]) sum)[i] =
                  Double.valueOf(((Double) ((Double[]) v1)[i]).doubleValue() +
                                     ((Double) ((Double[]) v2)[i]).doubleValue());
                i++;
            }
        }
        return (Double[]) sum;
    }
    
    private static Double getSqDist(Double[] v1, Double[] v2) {
        labeled_3:
        {
            Double dist = Double.valueOf(0.0);
            int i = 0;
            while (i < ((Double[]) v1).length) {
                dist = Double.valueOf(((Double) dist).doubleValue() +
                                          (((Double)
                                              ((Double[]) v1)[i]).doubleValue() -
                                             ((Double)
                                                ((Double[]) v2)[i]).doubleValue()) *
                                          (((Double)
                                              ((Double[]) v1)[i]).doubleValue() -
                                             ((Double)
                                                ((Double[]) v2)[i]).doubleValue()));
                i++;
            }
        }
        return (Double) dist;
    }
    
    private static Double[][] generatePoints(int numPoints, int dim) {
        Double[][] p = (Double[][]) (new Double[numPoints][dim]);
        int i = 0;
        while (i < numPoints) {
            ((Double[][]) p)[i] = (Double[]) (new Double[dim]);
            int j = 0;
            while (j < dim) {
                ((Double[]) ((Double[][]) p)[i])[j] =
                  Double.valueOf(Math.random());
                j++;
            }
            i++;
        }
        return (Double[][]) p;
    }
    
    public KMeansJava() { super(); }
}
