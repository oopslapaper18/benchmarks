package original.arith;

import java.util.List;

public class Sum {
	public static void main(String[] args) { sumList(null); }
    
    public static int sumList(List data) {
        labeled_0:
        {
						int sum = 0;
						int i = 0;
						while (i < ((List) data).size()) {
								sum += ((Integer) ((List) data).get(i)).intValue();
								i++;
						}
				}
        return sum;
    }
    
    public Sum() { super(); }
}
