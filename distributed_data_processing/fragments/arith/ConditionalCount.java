package original.arith;

import java.util.List;

public class ConditionalCount {
    public static void main(String[] args) { countList(null); }
    
    public static int countList(List data) {
        labeled_0:
        {
            int count = 0;
            int i = 0;
            while (i < ((List) data).size()) {
                int var = ((Integer) ((List) data).get(i)).intValue();
                if (var < 100) { count++; }
                i++;
            }
        }
        return count;
    }
    
    public ConditionalCount() { super(); }
}
