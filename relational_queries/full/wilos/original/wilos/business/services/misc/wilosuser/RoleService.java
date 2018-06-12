package wilos.business.services.misc.wilosuser;

import java.util.ArrayList;
import java.util.List;

import wilos.hibernate.misc.wilosuser.RoleDao;
import wilos.model.misc.wilosuser.Role;
import wilos.model.misc.wilosuser.WilosUser;

public class RoleService {

	private RoleDao roleDao;

	
	public List<WilosUser> getRoleUser(List<WilosUser> user){
		List<WilosUser> listUser = new ArrayList<WilosUser>();
		List<Role> role = this.roleDao.getRole();
		for(int i = 0; i < user.size(); i ++){
			for(int a = 0; a < role.size(); a++){
				if(user.get(i).getRole_id().equalsIgnoreCase(role.get(a).getRole_id())){
					user.get(i).setRole_name(role.get(a).getName());
					WilosUser userok = user.get(i);
					listUser.add(userok);
				}
			}
		}
		return listUser;
	}
	
	public String getARoleForAnUser(String id_role){
			if(id_role.equalsIgnoreCase("3")){
				return "admin";
			}
			if(id_role.equalsIgnoreCase("1")){
				return "projectDirector";
			}
			if(id_role.equalsIgnoreCase("2")){
				return "processManager";
			}
			return "participant";
		
	}
	
	public RoleDao getRoleDao() {
		return roleDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}
	
	public List<Role> getRoleList(){
		return this.roleDao.getRole();
		
	}
	
}
