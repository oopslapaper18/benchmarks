package original.arith;

import java.util.List;

public class Max {
    public static void main(String[] args) { maxList(null); }
    
    public static int maxList(List data) {
        labeled_0:
        {
            int max = Integer.MIN_VALUE;
            int i = 0;
            while (i < ((List) data).size()) {
                int var = ((Integer) ((List) data).get(i)).intValue();
                max = Math.max(var, max);
                i++;
            }
        }
        return max;
    }
    
    public Max() { super(); }
}
