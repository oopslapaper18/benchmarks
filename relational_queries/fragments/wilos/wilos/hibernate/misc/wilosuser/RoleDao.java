package wilos.hibernate.misc.wilosuser;

import java.util.List;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import wilos.model.misc.wilosuser.Role;

public class RoleDao extends org.springframework.orm.hibernate3.support.
  HibernateDaoSupport {
    public java.util.List<wilos.model.misc.wilosuser.Role> getRole() {
        java.util.List role = this.getHibernateTemplate().loadAll(null);
        return role;
    }
    
    public wilos.model.misc.wilosuser.Role getARoleById(java.lang.String id) {
        labeled_1 :
        {
            wilos.model.misc.wilosuser.Role r;
            r = null;
            java.util.List<wilos.model.misc.wilosuser.Role> list =
                    this.getHibernateTemplate().find(" from Role r where r.role_id=?",
                            id);
            if (list.size() > 0)
            {
                r = (wilos.model.misc.wilosuser.Role) list.get(0);
            } else
            {
                r = null;
            }

            return r;
        }
    }
    
    public RoleDao() { super(); }
}

