package original.arith;

import java.util.List;

public class MaxAbs {
    public static void main(String[] args) { maxAbsList(null); }
    
    public static int maxAbsList(List data) {
        labeled_0:
        {
            int maxAbs = Integer.MIN_VALUE;
            int i = 0;
            while (i < ((List) data).size()) {
                int var = ((Integer) ((List) data).get(i)).intValue();
                maxAbs = Math.max(Math.abs(var), maxAbs);
                i++;
            }
        }
        return maxAbs;
    }
    
    public MaxAbs() { super(); }
}
