package original.pheonix;

public class PcaJava {
    static final int DEF_GRID_SIZE = 100;
    static final int DEF_NUM_ROWS = 3;
    static final int DEF_NUM_COLS = 3;
    static final int num_rows = DEF_NUM_ROWS;
    static final int num_cols = DEF_NUM_COLS;
    static final int grid_size = DEF_GRID_SIZE;
    private static class Result {
        int[] mean;
        Integer[][] cov;
        
        Result(int[] m, Integer[][] c) {
            super();
            this.mean = (int[]) m;
            this.cov = (Integer[][]) c;
        }
    }
    
    /**
    
     * dump_points() Print the values in the matrix to the screen
    
     */
    static void dump_points(Integer[][] vals, int rows, int cols) {
        int i;
        int j;
        i = 0;
        while (i < rows) {
            j = 0;
            while (j < cols) {
                printf("%5d ",
                       (Object[])
                         (new Object[] { ((Integer[])
                                            ((Integer[][]) vals)[i])[j] }));
                j++;
            }
            printf("\n", (Object[]) (new Object[] {  }));
            i++;
        }
    }
    
    /**
    
     * generate_points() Create the values in the matrix
    
     */
    static void generate_points(Integer[][] pts, int rows, int cols) {
        int i;
        int j;
        i = 0;
        while (i < rows) {
            j = 0;
            while (j < cols) {
                ((Integer[]) ((Integer[][]) pts)[i])[j] =
                  Integer.valueOf(rand() % grid_size);
                j++;
            }
            i++;
        }
    }
    
    /**
    
     * calc_cov() Calculate the covariance
    
     */
    static Result calc_cov(Integer[][] matrix, int[] mean, Integer[][] cov) {
        labeled_0:
        {
            int i;
            int j;
            int k;
            int sum = 0;
            i = 0;
            while (i < num_rows) {
                sum = 0;
                j = 0;
                while (j < num_cols) {
                    sum += ((Integer)
                              ((Integer[])
                                 ((Integer[][]) matrix)[i])[j]).intValue();
                    j++;
                }
                ((int[]) mean)[i] = sum / num_cols;
                i++;
            }
        }
        labeled_1:
        {
            i = 0;
            while (i < num_rows) {
                j = i;
                while (j < num_rows) {
                    sum = 0;
                    k = 0;
                    while (k < num_cols) {
                        sum = sum +
                                (((Integer)
                                    ((Integer[])
                                       ((Integer[][]) matrix)[i])[k]).intValue() -
                                   ((int[]) mean)[i]) *
                                (((Integer)
                                    ((Integer[])
                                       ((Integer[][]) matrix)[j])[k]).intValue() -
                                   ((int[]) mean)[j]);
                        k++;
                    }
                    int x = sum / (num_cols - 1);
                    ((Integer[]) ((Integer[][]) cov)[i])[j] = Integer.valueOf(x);
                    ((Integer[]) ((Integer[][]) cov)[i])[j] = Integer.valueOf(x);
                    j++;
                }
                i++;
            }
        }
        return new Result((int[]) mean, (Integer[][]) cov);
    }
    
    public static void main(String[] argv) {
        int i;
        Integer[][] matrix;
        Integer[][] cov;
        int[] mean;
        matrix = (Integer[][])
                   (new Integer[][] { (Integer[])
                                        (new Integer[] { Integer.valueOf(1),
                                         Integer.valueOf(5),
                                         Integer.valueOf(3) }),
                    (Integer[])
                      (new Integer[] { Integer.valueOf(3), Integer.valueOf(4),
                       Integer.valueOf(2) }),
                    (Integer[])
                      (new Integer[] { Integer.valueOf(11), Integer.valueOf(2),
                       Integer.valueOf(4) }) });
        mean = (int[]) (new int[num_rows]);
        cov = (Integer[][]) (new Integer[num_rows][]);
        i = 0;
        while (i < num_rows) {
            ((Integer[][]) cov)[i] = (Integer[]) (new Integer[num_rows]);
            i++;
        }
        calc_cov((Integer[][]) matrix, (int[]) mean, (Integer[][]) cov);
        dump_points((Integer[][]) cov, num_rows, num_rows);
        return;
    }
    
    static int rand() {
        int v = (int) (Math.random() * Integer.MAX_VALUE);
        return v;
    }
    
    static void printf(String fmt, Object[] args) {
        System.out.format((String) fmt, (Object[]) args);
    }
    
    static void exit(int c) { System.exit(c); }
    
    public PcaJava() { super(); }
}
