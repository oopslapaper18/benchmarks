package original.arith;

import java.util.List;

public class Average {
    public static void main(String[] args) { avgList(null); }
    
    public static int avgList(List data) {
        labeled_0:
        {
						int sum = 0;
						int count = 0;
						int i = 0;
						while (i < ((List) data).size()) {
								sum += ((Integer) ((List) data).get(i)).intValue();
								count++;
								i++;
								i++;
						}
	    	}
        return sum / count;
    }
    
    public Average() { super(); }
}
