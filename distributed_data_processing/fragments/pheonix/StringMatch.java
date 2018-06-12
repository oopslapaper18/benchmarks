package original.pheonix;

import java.util.Arrays;
import java.util.List;

public class StringMatch {
    public static void main(String[] args) {
        List words = Arrays.asList((String[])
                                     (new String[] { "foo", "key1", "cat",
                                      "bar", "dog" }));
        matchWords((List) words);
    }
    
    public static boolean[] matchWords(List words) {
        labeled_0:
        {
            String key1 = "key1";
            String key2 = "key2";
            String key3 = "key3";
            boolean foundKey1 = false;
            boolean foundKey2 = false;
            boolean foundKey3 = false;
            int i = 0;
            while (i < ((List) words).size()) {
                if (((String) key1).equals(((List) words).get(i))) foundKey1 = true;
                if (((String) key2).equals(((List) words).get(i))) foundKey2 = true;
                if (((String) key3).equals(((List) words).get(i))) foundKey3 = true;
                i++;
            }
        }
        boolean[] res = { foundKey1, foundKey2, foundKey3 };
        return (boolean[]) res;
    }
    
    public StringMatch() { super(); }
}
