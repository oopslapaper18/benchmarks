package original.pheonix;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCount {
    public static void main(String[] args) {
        List words = Arrays.asList((String[])
                                     (new String[] { "foo", "bar", "cat", "bar",
                                      "dog" }));
        countWords((List) words);
    }
    
    private static Map countWords(List words) {
        labeled_0:
        {
            Map counts = new HashMap();
            int j = 0;
            while (j < ((List) words).size()) {
                String word = (String) ((List) words).get(j);
                Integer prev = (Integer) ((Map) counts).get(word);
                if ((Integer) prev == null) prev = Integer.valueOf(0);
                ((Map) counts).put((String) word,
                                   Integer.valueOf(((Integer) prev).intValue() +
                                                       1));
                j++;
            }
        }
        return (Map) counts;
    }
    
    public WordCount() { super(); }
}
