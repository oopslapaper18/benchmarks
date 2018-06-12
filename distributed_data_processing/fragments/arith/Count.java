package original.arith;

import java.util.List;

public class Count {
    public static void main(String[] args) { countList(null); }
    
    public static int countList(List data) {
        labeled_0:
        {
            int count = 0;
            int i = 0;
            while (i < ((List) data).size()) {
                count++;
                i++;
            }
        }
        return count;
    }
    
    public Count() { super(); }
}
