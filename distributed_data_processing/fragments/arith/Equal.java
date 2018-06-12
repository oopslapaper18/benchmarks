package original.arith;

import java.util.List;

public class Equal {
    public static void main(String[] args) { equal(null); }
    
    public static boolean equal(List data) {
        labeled_0:
        {
						boolean equal = true;
						int val = ((Integer) ((List) data).get(0)).intValue();
						int i = 0;
						while (i < ((List) data).size()) {
								if (val != ((Integer) ((List) data).get(i)).intValue()) {
										equal = false;
								}
								i++;
						}
				}
        return equal;
    }
    
    public Equal() { super(); }
}
