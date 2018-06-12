package original.arith;

import java.util.List;

public class ConditionalSum {
    public static void main(String[] args) { sumList(null); }
    
    public static int sumList(List data) {
        labeled_0:
        {
						int sum = 0;
						int i = 0;
						while (i < ((List) data).size()) {
								int var = ((Integer) ((List) data).get(i)).intValue();
								if (var < 100) { sum += var; }
								i++;
						}
	    	}
        return sum;
    }
    
    public ConditionalSum() { super(); }
}
