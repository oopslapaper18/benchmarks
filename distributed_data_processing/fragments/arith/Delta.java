package original.arith;

import java.util.List;

public class Delta {
    public static void main(String[] args) { deltaList(null); }
    
    public static int deltaList(List data) {
        labeled_0:
        {
            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;
            int i = 0;
            while (i < ((List) data).size()) {
                int var = ((Integer) ((List) data).get(i)).intValue();
                max = Math.max(var, max);
                min = Math.min(var, min);
                i++;
            }
        }
        return max - min;
    }
    
    public Delta() { super(); }
}
