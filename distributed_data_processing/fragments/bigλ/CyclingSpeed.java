package original.big\u03bb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyclingSpeed {
    class Record {
        public int fst;
        public int snd;
        public int emit;
        public double speed;
        
        public Record() { super(); }
    }
    
    public Map cyclingSpeed(List data) {
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
                        int speed = (int) Math.ceil(((Record) record).speed);
                        if (!((Map) result).containsKey(Integer.valueOf(speed))) {
                            ((Map) result).put(Integer.valueOf(speed),
                                               Integer.valueOf(0));
                        }
                        ((Map) result).
                          put(
                            Integer.valueOf(speed),
                            Integer.valueOf(
                                      ((Integer)
                                         ((Map)
                                            result).get(
                                                      Integer.valueOf(
                                                                speed))).intValue(
                                                                           ) + 1));
                    }
                }
            }
        }
        return (Map) result;
    }
    
    public CyclingSpeed() { super(); }
}
