package original.arith;

import java.util.List;

public class EqualFrequency {
    public static void main(String[] args) { equalFrequency(null); }
    
    public static boolean equalFrequency(List data) {
        labeled_0:
        {
						int first = 0;
						int second = 0;
						int i = 0;
						while (i < ((List) data).size()) {
								int var = ((Integer) ((List) data).get(i)).intValue();
								if (var == 100) { first++; }
								if (var == 110) { second++; }
								i++;
						}
				}
        return first == second;
    }
    
    public EqualFrequency() { super(); }
}
