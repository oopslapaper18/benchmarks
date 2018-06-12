package wilos.
  hibernate.
  misc.
  concreteactivity;

import java.util.ArrayList;
import java.util.List;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import wilos.model.misc.concreteactivity.ConcreteActivity;

/**
 *
 ConcreteActivityDao
 manage
 requests
 from
 the
 system
 to
 store
 Concrete
 *
 Activities
 to
 the
 database
 *
 
 *
 @author
 garwind
 *
 @author deder
 */
public class ConcreteActivityDao extends org.
  springframework.
  orm.
  hibernate3.
  support.
  HibernateDaoSupport {
    /**
     *
     Saves
     or
     updates
     a
     ConcreteActivity
     *
     
     *
     @param
     _concreteactivity
     The
     ConcreteActivity
     to
     be
     saved
     or updated
     */
    public java.
      lang.
      String saveOrUpdateConcreteActivity(wilos.model.misc.concreteactivity.
                                            ConcreteActivity _concreteactivity) {
        if (_concreteactivity != null) {
            this.getHibernateTemplate().saveOrUpdate(_concreteactivity);
            return _concreteactivity.getId();
        }
        return "";
    }
    
    /**
     * Returns a list of all the ConcreteActivities
     * 
     * @return A list of all the ConcreteActivities
     */
    public java.util.List<wilos.model.misc.concreteactivity.
      ConcreteActivity> getAllConcreteActivities() {
        java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity>
          concreteActivities =
          new java.util.ArrayList<wilos.model.misc.concreteactivity.
          ConcreteActivity>();
        java.util.Iterator extfor$iter =
          this.getHibernateTemplate().loadAll(null).iterator();
        while (extfor$iter.hasNext()) {
            java.lang.Object obj = (java.lang.Object) extfor$iter.next();
            wilos.model.misc.concreteactivity.ConcreteActivity ca =
              (wilos.model.misc.concreteactivity.ConcreteActivity) obj;
            concreteActivities.add(ca);
        }
        return concreteActivities;
    }
    
    /**
     * Tests if there are one or more existing ConcreteActivities that have the
     given ID
     * 
     * @param _id The wanted ID
     * @return True or false
     */
    public boolean existsConcreteActivity(java.lang.String _id) {
        java.util.List concreteactvities =
          this.getHibernateTemplate().find(
                                        "from ConcreteActivity a where a.id=?",
                                        _id);
        return concreteactvities.size() > 0;
    }
    
    /**
     * Returns the ConcreteActivities which has the specified ID
     * 
     * @param _id The id of the wanted ConcreteActivity
     * @return The wanted ConcreteActivity
     */
    public wilos.model.misc.concreteactivity.
      ConcreteActivity getConcreteActivity(java.lang.String _id) {
        if (_id == null) return null;
        if (!_id.equals("")) return null;
        return null;
    }
    
    /**
     * Returns the ConcreteActivity which has the given prefix
     * If there are many ConcreteActivities with the same prefix, it returns the
     first of them
     * 
     * @param _prefix The wanted prefix
     * @return The wanted ConcreteActivity
     */
    public wilos.model.misc.concreteactivity.
      ConcreteActivity getConcreteActivityFromPrefix(java.lang.String _prefix) {
        java.util.List concreteactvities =
          this.getHibernateTemplate().
          find("from ConcreteActivity a where a.prefix=?", _prefix);
        if (concreteactvities.size() > 0)
            return (wilos.model.misc.concreteactivity.ConcreteActivity)
                     concreteactvities.get(0); else return null;
    }
    
    /**
     * Deletes the ConcreteActivity
     * 
     * @param _concreteactivity The ConcreteActivity to be deleted
     */
    public void deleteConcreteActivity(wilos.model.misc.concreteactivity.
                                         ConcreteActivity _concreteactivity) {
        this.getHibernateTemplate().delete(_concreteactivity);
    }
    
    /**
     * Returns the ConcreteActivity which has the given name
     * If there are many ConcreteActivities with the same name, it returns the
     first of them
     * 
     * @param _name The wanted name
     * @return The wanted ConcreteActivity
     */
    public wilos.model.misc.concreteactivity.
      ConcreteActivity getConcreteActivityByName(java.lang.String _name) {
        if (!_name.equals("")) {
            java.util.List activities =
              this.getHibernateTemplate().find("from Activity a where a.name=?",
                                               _name);
            if (activities.size() > 0)
                return (wilos.model.misc.concreteactivity.ConcreteActivity)
                         activities.get(0);
        }
        return null;
    }
    
    /**
     * Returns the maximum display order for the given concreteActivity
     * 
     * @param _cact the wanted concreteActivity
     * @return the highest displayOrder for the given activity
     */
    public java.
      lang.
      String getMaxDisplayOrder(wilos.model.misc.concreteactivity.
                                  ConcreteActivity _cact) {
        labeled_1 :
        {
            String r;
            r = "";
            java.util.List cacts =
                    this.getHibernateTemplate().
                            find(("from ConcreteActivity ca where ca.id=? order by displayOrder" +
                                    " DESC"), _cact.getId());
            java.lang.String res =
                    ((wilos.model.misc.concreteactivity.ConcreteActivity) cacts.get(0)).
                            getDisplayOrder();
            if (res.length() > 0)
            {
                r = res.substring(res.length() - 1);
            } else r = "0";
        }
        return r;
    }
    
    public ConcreteActivityDao() { super(); }
}

