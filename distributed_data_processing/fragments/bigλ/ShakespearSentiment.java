package original.big\u03bb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShakespearSentiment {
    public Map sentiment(List words) {
        labeled_0:
        {
            Map result = new HashMap();
            ((Map) result).put("love", Integer.valueOf(0));
            ((Map) result).put("hate", Integer.valueOf(0));
            {
                java.util.Iterator extfor$iter = ((List) words).iterator();
                while (((java.util.Iterator) extfor$iter).hasNext()) {
                    String word = (String)
                                    (String)
                                      ((java.util.Iterator) extfor$iter).next();
                    {
                        if (((String) word).trim().toLowerCase().equals("love")) {
                            ((Map) result).
                              put(
                                "love",
                                Integer.valueOf(
                                          ((Integer)
                                             ((Map) result).get("love")).intValue(
                                                                           ) + 1));
                        }
                        else
                            if (((String) word).trim().toLowerCase().equals(
                                                                       "hate")) {
                                ((Map) result).
                                  put(
                                    "hate",
                                    Integer.valueOf(
                                              ((Integer)
                                                 ((Map) result).get(
                                                                  "hate")).intValue(
                                                                             ) +
                                                  1));
                            }
                    }
                }
            }
        }
        return (Map) result;
    }
    
    public ShakespearSentiment() { super(); }
}
