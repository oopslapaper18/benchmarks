package original.big\u03bb;

import java.util.ArrayList;
import java.util.List;

public class DatabaseSelect {
    class Record {
        public List columns;
        
        public Record() { super(); }
    }
    
    public List select(List table, String key) {
        labeled_0:
        {
            List result = new ArrayList();
            {
                java.util.Iterator extfor$iter = ((List) table).iterator();
                while (((java.util.Iterator) extfor$iter).hasNext()) {
                    Record record = (Record)
                                      (Record)
                                        ((java.util.Iterator) extfor$iter).next();
                    {
                        if (((String) ((Record) record).columns.get(0)).equals(
                                                                          key)) {
                            ((List) result).add((Record) record);
                        }
                    }
                }
            }
        }
        return (List) result;
    }
    
    public DatabaseSelect() { super(); }
}
