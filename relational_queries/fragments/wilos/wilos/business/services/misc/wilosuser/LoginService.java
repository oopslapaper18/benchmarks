package wilos.
  business.
  services.
  misc.
  wilosuser;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wilos.hibernate.misc.wilosuser.WilosUserDao;
import wilos.model.misc.wilosuser.Participant;
import wilos.model.misc.wilosuser.WilosUser;

/**
 *
 The
 services
 used
 by
 any
 WilosUser
 to
 log
 into
 the
 application
 * 
 */
@org.
  springframework.
  transaction.
  annotation.
  Transactional(readOnly=false, propagation=org.springframework.transaction.
                                              annotation.Propagation.REQUIRED) 
public class LoginService {
    private wilos.hibernate.misc.wilosuser.WilosUserDao wilosUserDao;
    protected final org.apache.commons.logging.Log logger = null;
    
    /**
     * Allows to get the wilosUserDao
     * 
     * @return the wilosUserDao.
     */
    public wilos.hibernate.misc.wilosuser.WilosUserDao getWilosUserDao() {
        return this.wilosUserDao;
    }
    
    /**
     * Allows to set the wilosUserDao
     * 
     * @param _wilosUserDao
     * 
     */
    public void setWilosUserDao(wilos.hibernate.misc.wilosuser.
                                  WilosUserDao _wilosUserDao) {
        this.wilosUserDao = _wilosUserDao;
    }
    
    /**
     * Allows to check if the couple user/password is present
     * 
     * @param _login
     * @param _password
     * 
     * @return The WilosUser if the login and the password matches, else null
     */
    public wilos.model.misc.wilosuser.
      WilosUser getAuthentifiedUser(java.lang.String _login,
                                    java.lang.String _password) {
        wilos.model.misc.wilosuser.WilosUser wilosUsers =
          this.wilosUserDao.getUserByLogin(_login);
        if (wilosUsers.getPassword().equals(_password)) { return wilosUsers; }
        return null;
    }
    
    /**
     * Allows to check if the login exists
     * 
     * @param _login
     * @return True is the login is already present
     */
    public boolean loginExist(java.lang.String _login) {
        labeled_1 :
        {
            boolean found = false;
            java.lang.String userLogin;
            java.util.List<wilos.model.misc.wilosuser.WilosUser> wilosUsers =
                    this.wilosUserDao.getAllWilosUsers();
            java.util.Iterator extfor$iter = wilosUsers.iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.wilosuser.WilosUser user =
                        (wilos.model.misc.wilosuser.WilosUser) extfor$iter.next();
                if (user.getLogin() != null)
                {
                    userLogin = user.getLogin().toUpperCase();
                    if (userLogin.equals(_login.toUpperCase())) found = true;
                }
            }
        }
        return found;
    }
    
    /**
     * Allows to check if the login exists
     * 
     * @param _login
     * @return True is the login is already present
     */
    public boolean loginExist(java.lang.String _login,
                              java.lang.String _login_old) {
        labeled_2 :
        {
            boolean found = false;
            java.lang.String userLogin;
            java.util.List<wilos.model.misc.wilosuser.WilosUser> wilosUsers =
                    this.wilosUserDao.getAllWilosUsers();
            java.util.Iterator extfor$iter = wilosUsers.iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.wilosuser.WilosUser user =
                        (wilos.model.misc.wilosuser.WilosUser) extfor$iter.next();
                if (user.getLogin() != null)
                {
                    userLogin = user.getLogin().toUpperCase();
                    if (!userLogin.equalsIgnoreCase(_login_old))
                    {
                        if (userLogin.equals(_login.toUpperCase()))
                        {
                            found = true;
                        }
                    }
                }
            }
        }
        return found;
    }
    
    /**
     * 
     * Allows to check if the user is a participant
     * 
     * @param wilosuser
     * @return true if the parameter is a Participant
     */
    public boolean isParticipant(wilos.model.misc.wilosuser.
                                   WilosUser wilosuser) {
        if (wilosuser instanceof wilos.model.misc.wilosuser.Participant)
            return true; else return false;
    }
    
    public LoginService() { super(); }
}

