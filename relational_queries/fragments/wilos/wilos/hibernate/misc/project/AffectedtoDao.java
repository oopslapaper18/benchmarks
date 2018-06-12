package wilos.hibernate.misc.project;

import java.util.List;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class AffectedtoDao extends org.springframework.orm.hibernate3.support.
  HibernateDaoSupport {
    /**
    
     * Returns the WilosUser which have the specified ID
    
     * 
    
     * @param _id The wanted WilosUser's ID
    
     * @return The wanted WilosUser
    
     */
    public java.lang.String getAffectedToByIdParticipant(java.lang.String _id) {
        labeled_1 :
        {
            String r;
            r = "";
            java.util.List affected =
                    this.getHibernateTemplate(
                    ).find("from Affectedto wu where wu.participant_id=?", _id);
            if (affected.size() > 0)
            {
                r = "false";
            } else
            {
                r = "true";
            }
        }
        return r;
    }
    
    public AffectedtoDao() { super(); }
}

