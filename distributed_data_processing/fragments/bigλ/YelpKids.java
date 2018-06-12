package original.big\u03bb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YelpKids {
    class Record {
        public String state;
        public String city;
        public String comment;
        public int score;
        public boolean goodForKids;
        
        public Record() { super(); }
    }
    
    public Map reviewCount(List data) {
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
                        if (!((Map) result).containsKey(((Record) record).city)) {
                            ((Map) result).put(((Record) record).city,
                                               Integer.valueOf(0));
                        }
                        if (((Record) record).goodForKids) {
                            ((Map) result).
                              put(
                                ((Record) record).city,
                                Integer.
                                    valueOf(
                                      ((Integer)
                                         ((Map) result).
                                         get(((Record) record).city)).intValue() +
                                          1));
                        }
                    }
                }
            }
        }
        return (Map) result;
    }
    
    public YelpKids() { super(); }
}
