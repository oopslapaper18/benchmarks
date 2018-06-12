package original.big\u03bb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitterCounts {
    public Map pairs(List tweets) {
        labeled_0:
        {
            Map result = new HashMap();
            {
                java.util.Iterator extfor$iter$2 = ((List) tweets).iterator();
                while (((java.util.Iterator) extfor$iter$2).hasNext()) {
                    String tweet = (String)
                                     (String)
                                       ((java.util.Iterator) extfor$iter$2).next();
                    {
                        {
                            String[] extfor$arr = ((String) tweet).split("\\s+");
                            int extfor$iter$1 = 0;
                            while (extfor$iter$1 < ((String[]) extfor$arr).length) {
                                String word = (String)
                                                ((String[])
                                                   extfor$arr)[extfor$iter$1];
                                extfor$iter$1 = extfor$iter$1 + 1;
                                {
                                    if (((String) word).charAt(0) == '#') {
                                        if (!((Map) result).containsKey(word)) {
                                            ((Map) result).put((String) word,
                                                               Integer.valueOf(0));
                                        }
                                        ((Map) result).
                                          put(
                                            (String) word,
                                            Integer.
                                                valueOf(
                                                  ((Integer)
                                                     ((Map) result).
                                                     get(word)).intValue() + 1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return (Map) result;
    }
    
    public TwitterCounts() { super(); }
}
