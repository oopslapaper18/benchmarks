package wilos.hibernate.misc.wilosuser;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import wilos.model.misc.wilosuser.Role;

public class RoleDao extends HibernateDaoSupport{

	public List<Role> getRole(){
		List role = this.getHibernateTemplate().loadAll(null); //Role.class); // akc
		return role;
	}
	
	public Role getARoleById(String id){
		List<Role> list = this.getHibernateTemplate().find( " from Role r where r.role_id=?",id);
		if (list.size() > 0) {
		    return (Role) list.get(0);
		} else {
		    return null;
		}
	}
}
