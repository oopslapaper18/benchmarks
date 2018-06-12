package original.arith;

import java.util.List;

public class Min {
    public static void main(String[] args) { minList(null); }
    
    public static int minList(List data) {
        labeled_0:
        {
						int min = Integer.MAX_VALUE;
						int i = 0;
						while (i < ((List) data).size()) {
								int var = ((Integer) ((List) data).get(i)).intValue();
								min = Math.min(var, min);
								i++;
						}
				}
        return min;
    }
    
    public Min() { super(); }
}
