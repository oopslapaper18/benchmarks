package original.big\u03bb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikiPageCount {
    class Record {
        public String name;
        public int views;
        public int something;
        
        public Record() { super(); }
    }
    
    public Map pageCount(List data) {
        labeled_0:
        {
            Map result = new HashMap();
            {
                java.util.Iterator extfor$iter = ((List) data).iterator();
                while (((java.util.Iterator) extfor$iter).hasNext()) {
                    Record record = (Record)
                                      (Record)
                                        ((java.util.Iterator) extfor$iter).next();
                    {
                        if (!((Map) result).containsKey(((Record) record).name)) {
                            ((Map) result).put(((Record) record).name,
                                               Integer.valueOf(0));
                        }
                        ((Map) result).
                          put(
                            ((Record) record).name,
                            Integer.valueOf(
                                      ((Integer)
                                         ((Map)
                                            result).get(((Record)
                                                           record).name)).intValue(
                                                                            ) +
                                          ((Record) record).views));
                    }
                }
            }
        }
        return (Map) result;
    }
    
    public WikiPageCount() { super(); }
}
