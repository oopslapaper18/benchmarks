package wilos.
  business.
  services.
  misc.
  wilosuser;

import java.util.ArrayList;
import java.util.List;
import wilos.hibernate.misc.wilosuser.RoleDao;
import wilos.model.misc.wilosuser.Role;
import wilos.model.misc.wilosuser.WilosUser;

public class RoleService {
    private wilos.hibernate.misc.wilosuser.RoleDao roleDao;
    
    public java.
      util.
      List<wilos.
      model.
      misc.
      wilosuser.
      WilosUser> getRoleUser(java.util.List<wilos.model.misc.wilosuser.
                               WilosUser> user) {
        labeled_1 :
        {
            java.util.List<wilos.model.misc.wilosuser.WilosUser> listUser =
                    new java.util.ArrayList<wilos.model.misc.wilosuser.WilosUser>();
            java.util.List<wilos.model.misc.wilosuser.Role> role =
                    this.roleDao.getRole();
            int i = 0;
            while (i < user.size())
            {
                int a = 0;
                while (a < role.size())
                {
                    if (user.get(i).getRole_id().equalsIgnoreCase(
                            role.get(a).getRole_id()))
                    {
                        user.get(i).setRole_name(role.get(a).getName());
                        wilos.model.misc.wilosuser.WilosUser userok = user.get(i);
                        listUser.add(userok);
                    }
                    a++;
                }
                i++;
            }
        }
        return listUser;
    }
    
    public java.lang.String getARoleForAnUser(java.lang.String id_role) {
        if (id_role.equalsIgnoreCase("3")) { return "admin"; }
        if (id_role.equalsIgnoreCase("1")) { return "projectDirector"; }
        if (id_role.equalsIgnoreCase("2")) { return "processManager"; }
        return "participant";
    }
    
    public wilos.hibernate.misc.wilosuser.RoleDao getRoleDao() {
        return roleDao;
    }
    
    public void setRoleDao(wilos.hibernate.misc.wilosuser.RoleDao roleDao) {
        this.roleDao = roleDao;
    }
    
    public java.util.List<wilos.model.misc.wilosuser.Role> getRoleList() {
        return this.roleDao.getRole();
    }
    
    public RoleService() { super(); }
}

