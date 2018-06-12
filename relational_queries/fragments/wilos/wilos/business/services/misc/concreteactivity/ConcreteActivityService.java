package wilos.
  business.
  services.
  misc.
  concreteactivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wilos.business.services.misc.concretemilestone.ConcreteMilestoneService;
import wilos.business.services.misc.stateservice.StateService;
import wilos.hibernate.misc.concreteactivity.ConcreteActivityDao;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement;

@org.
  springframework.
  transaction.
  annotation.
  Transactional(readOnly=false, propagation=org.springframework.transaction.
                                              annotation.Propagation.REQUIRED) 
public class ConcreteActivityService {
    private wilos.hibernate.misc.concreteactivity.ConcreteActivityDao
      concreteActivityDao;
    private wilos.business.services.misc.concretemilestone.
      ConcreteMilestoneService concreteMilestoneService;
    private wilos.business.services.misc.stateservice.StateService stateService;
    
    /**
     *Allows to get the sorted set of concreteBreakdownElements with a
     concreteActivity  
     * @param _cact
     * @return the sorted set of concreteBreakdownElements
     */
    public java.
      util.
      SortedSet<wilos.
      model.
      misc.
      concretebreakdownelement.
      ConcreteBreakdownElement> getConcreteBreakdownElements(wilos.model.misc.
                                                               concreteactivity.
                                                               ConcreteActivity _cact) {
        java.util.SortedSet<wilos.model.misc.concretebreakdownelement.
          ConcreteBreakdownElement> tmp =
          new java.util.TreeSet<wilos.model.misc.concretebreakdownelement.
          ConcreteBreakdownElement>();
        this.concreteActivityDao.getSessionFactory().getCurrentSession().
          saveOrUpdate(_cact);
        java.util.Iterator extfor$iter =
          _cact.getConcreteBreakdownElements().iterator();
        while (extfor$iter.hasNext()) {
            wilos.
              model.
              misc.
              concretebreakdownelement.
              ConcreteBreakdownElement
              cbde =
              (wilos.model.misc.concretebreakdownelement.
                ConcreteBreakdownElement) extfor$iter.next();
            tmp.add(cbde);
        }
        return tmp;
    }
    
    /**
     * Allows to save the concrete activity which passed in parameters
     * 
     * @param _concreteActivity
     */
    public void saveConcreteActivity(wilos.model.misc.concreteactivity.
                                       ConcreteActivity _concreteActivity) {
        this.concreteActivityDao.saveOrUpdateConcreteActivity(
                                   _concreteActivity);
    }
    
    /**
     * Allows to get the concrete activity which has the same id than the
     * parameter
     * 
     * @param _concreteActivityId
     *                the id of the concreteActivity asked
     * @return the ConcreteActivity which has the same id
     */
    public boolean existsConcreteActivity(java.lang.
                                            String _concreteActivityId) {
        return this.concreteActivityDao.existsConcreteActivity(
                                          _concreteActivityId);
    }
    
    /**
     * Allows to get the concrete activity which has the same id than the
     * parameter
     * 
     * @param _concreteActivityId
     *                the id of the concreteActivity asked
     * @return the ConcreteActivity which has the same id
     */
    public wilos.
      model.
      misc.
      concreteactivity.
      ConcreteActivity getConcreteActivity(java.lang.
                                             String _concreteActivityId) {
        return this.concreteActivityDao.getConcreteActivity(
                                          _concreteActivityId);
    }
    
    /**
     * Return the list of all the Concrete Activities
     * 
     * @return the list of all the concreteActivities
     */
    public java.util.List<wilos.model.misc.concreteactivity.
      ConcreteActivity> getAllConcreteActivities() {
        return this.concreteActivityDao.getAllConcreteActivities();
    }
    
    /**
     * Return the concreteActivityDao
     * 
     * @return the concreteActivityDao
     */
    public wilos.hibernate.misc.concreteactivity.
      ConcreteActivityDao getConcreteActivityDao() {
        return concreteActivityDao;
    }
    
    /**
     * Initialize the concreteActivityDao with the value in parameter
     * 
     * @param concreteActivityDao
     *                the concreteActivityDao to set
     */
    public void setConcreteActivityDao(wilos.hibernate.misc.concreteactivity.
                                         ConcreteActivityDao concreteActivityDao) {
        this.concreteActivityDao = concreteActivityDao;
    }
    
    /**
     * Allows to get the set of concreteActivities from a project
     * @param _cact
     * @return the set of concreteActivities
     */
    public java.
      util.
      Set<wilos.
      model.
      misc.
      concreteactivity.
      ConcreteActivity> getConcreteActivitiesFromProject(wilos.model.misc.
                                                           concreteactivity.
                                                           ConcreteActivity _cact) {
        labeled_1 :
        {
            java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> tmp =
                    new java.util.HashSet<wilos.model.misc.concreteactivity.
                            ConcreteActivity>();
            this.concreteActivityDao.getSessionFactory().getCurrentSession().
                    saveOrUpdate(_cact);
            java.util.Iterator extfor$iter =
                    this.getAllConcreteActivities().iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.concreteactivity.ConcreteActivity cact =
                        (wilos.model.misc.concreteactivity.ConcreteActivity)
                                extfor$iter.next();
                if (cact.getProject() != null && cact.getProject().equals(_cact))
                {
                    tmp.add(cact);
                }
            }
        }
        return tmp;
    }
    
    /**
     * Allows to get the set of concreteActivities
     * @return the set of concreteActivities
     */
    public wilos.business.services.misc.concretemilestone.
      ConcreteMilestoneService getConcreteMilestoneService() {
        return concreteMilestoneService;
    }
    
    /**
     * Allows to set the concreteMilestoneService
     * @param _concreteMilestoneService
     */
    public void setConcreteMilestoneService(wilos.business.services.misc.
                                              concretemilestone.
                                              ConcreteMilestoneService _concreteMilestoneService) {
        concreteMilestoneService = _concreteMilestoneService;
    }
    
    /**
     *Allows to get the service's state
     * @return the stateService
     */
    public wilos.business.services.misc.stateservice.
      StateService getStateService() { return this.stateService; }
    
    /**
     * Allows to set the service's state
     * @param _stateService 
     */
    public void setStateService(wilos.business.services.misc.stateservice.
                                  StateService _stateService) {
        this.stateService = _stateService;
    }
    
    /**
     * Allows to get the maxDisplayOrder
     * @param _cact
     * @return the maxDisplayOrder
     */
    public java.
      lang.
      String getMaxDisplayOrder(wilos.model.misc.concreteactivity.
                                  ConcreteActivity _cact) {
        return this.concreteActivityDao.getMaxDisplayOrder(_cact);
    }
    
    public ConcreteActivityService() { super(); }
}

