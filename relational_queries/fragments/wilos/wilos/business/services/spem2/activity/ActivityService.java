package wilos.business.services.spem2.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wilos.business.services.misc.concreteactivity.ConcreteActivityService;
import wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService;
import wilos.business.services.misc.stateservice.StateService;
import wilos.business.services.spem2.task.TaskDescriptorService;
import wilos.hibernate.misc.concreteiteration.ConcreteIterationDao;
import wilos.hibernate.misc.concretephase.ConcretePhaseDao;
import wilos.hibernate.misc.project.ProjectDao;
import wilos.hibernate.spem2.activity.ActivityDao;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement;
import wilos.model.misc.concreteiteration.ConcreteIteration;
import wilos.model.misc.concretephase.ConcretePhase;
import wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement;
import wilos.model.misc.project.Project;
import wilos.model.spem2.activity.Activity;
import wilos.model.spem2.breakdownelement.BreakdownElement;
import wilos.model.spem2.guide.Guidance;
import wilos.model.spem2.task.TaskDescriptor;
import wilos.utils.Constantes;
import wilos.utils.Constantes.State;

/**
 * ActivityManager is a transactional class, that manages operations about
 * activity, requested by web pages (activity.jsp & activityform.jsp)
 * 
 */
@org.springframework.transaction.annotation.Transactional(readOnly=false, propagation=org.springframework.transaction.annotation.Propagation.REQUIRED) 
public class ActivityService {
    private wilos.hibernate.spem2.activity.ActivityDao activityDao;
    private wilos.hibernate.misc.concretephase.ConcretePhaseDao concretePhaseDao;
    private wilos.hibernate.misc.concreteiteration.ConcreteIterationDao concreteIterationDao;
    private wilos.hibernate.misc.project.ProjectDao projectDao;
    private wilos.business.services.misc.concreteactivity.ConcreteActivityService concreteActivityService;
    private wilos.business.services.spem2.task.TaskDescriptorService taskDescriptorService;
    private wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService concreteWorkOrderService;
    
    /**
     * Exploit all concrete activities children
     * @param _nbExistingConcreteActivitiesChildren
     * @param _occ
     * @param _project
     * @param _activity
     * @param _dispOrd
     * @param _cact
     * @param _list
     * @param _concreteActivitiesSisters
     */
    private void peruseConcreteActivitiesChildren(int _nbExistingConcreteActivitiesChildren,
                                                  int _occ,
                                                  wilos.model.misc.project.Project _project,
                                                  wilos.model.spem2.activity.Activity _activity,
                                                  int _dispOrd,
                                                  wilos.model.misc.concreteactivity.ConcreteActivity _cact,
                                                  java.util.List<java.util.HashMap<java.lang.String,
                                                  java.lang.Object>> _list,
                                                  java.util.ArrayList<wilos.model.misc.concreteactivity.ConcreteActivity> _concreteActivitiesSisters) {
        int nbConcreteActivitiesSisters = _nbExistingConcreteActivitiesChildren;
        int i = _nbExistingConcreteActivitiesChildren + 1;
        while (i <= _nbExistingConcreteActivitiesChildren + _occ) {
            wilos.model.misc.concreteactivity.ConcreteActivity cact =
              new wilos.model.misc.concreteactivity.ConcreteActivity();
            java.util.List<wilos.model.spem2.breakdownelement.BreakdownElement> bdes =
              new java.util.ArrayList<wilos.model.spem2.breakdownelement.BreakdownElement>(
              );
            bdes.addAll(this.getAllBreakdownElements(_activity));
            if (_occ != 1 || _nbExistingConcreteActivitiesChildren != 0) {
                if (_activity.getPresentationName().equals(""))
                    cact.setConcreteName(_activity.getName() + "#" + i);
                else
                    cact.setConcreteName(_activity.getPresentationName() + "#" +
                                           i);
            } else {
                if (_activity.getPresentationName().equals(""))
                    cact.setConcreteName(_activity.getName()); else
                    cact.setConcreteName(_activity.getPresentationName());
            }
            cact.addActivity(_activity);
            cact.setProject(_project);
            cact.setBreakdownElement(_activity);
            cact.setInstanciationOrder(i);
            cact.setWorkBreakdownElement(_activity);
            cact.setActivity(_activity);
            _cact.setConcreteBreakdownElements(
                    this.concreteActivityService.getConcreteBreakdownElements(
                                                   _cact));
            cact.addSuperConcreteActivity(_cact);
            cact.setDisplayOrder(
                   cact.getSuperConcreteActivity().getDisplayOrder() +
                     java.lang.Integer.toString(_dispOrd + i));
            this.concreteActivityService.saveConcreteActivity(cact);
            int dispOrd = 0;
            java.util.Iterator extfor$iter = bdes.iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.spem2.breakdownelement.BreakdownElement bde =
                  (wilos.model.spem2.breakdownelement.BreakdownElement)
                    extfor$iter.next();
                dispOrd++;
                if (bde instanceof wilos.model.spem2.activity.Activity) {
                    wilos.model.spem2.activity.Activity act =
                      (wilos.model.spem2.activity.Activity) bde;
                    int occ = this.giveNbOccurences(act.getId(), _list, false);
                    if (occ == 0 && _occ > 0) occ = _occ;
                    this.activityInstanciation(_project, act, cact, _list, occ,
                                               false, dispOrd);
                }
                else
                    if (bde instanceof wilos.model.spem2.task.TaskDescriptor) {
                        wilos.model.spem2.task.TaskDescriptor td =
                          (wilos.model.spem2.task.TaskDescriptor) bde;
                        int occ = this.giveNbOccurences(td.getId(), _list,
                                                        false);
                        if (occ == 0 && _occ > 0) occ = _occ;
                        this.taskDescriptorService.taskDescriptorInstanciation(
                                                     _project, td, cact, occ,
                                                     false, dispOrd);
                    }
            }
            this.concreteActivityService.saveConcreteActivity(cact);
            if (nbConcreteActivitiesSisters != 0) {
                wilos.model.misc.concreteactivity.ConcreteActivity lastConcreteActivity =
                  null;
                java.util.Iterator extfor$iter$1 =
                  _concreteActivitiesSisters.iterator();
                while (extfor$iter$1.hasNext()) {
                    wilos.model.misc.concreteactivity.ConcreteActivity tmp =
                      (wilos.model.misc.concreteactivity.ConcreteActivity)
                        extfor$iter$1.next();
                    if (lastConcreteActivity == null ||
                          tmp.getInstanciationOrder() >
                          lastConcreteActivity.getInstanciationOrder()) {
                        lastConcreteActivity = tmp;
                    }
                }
                this.concreteWorkOrderService.saveConcreteWorkOrder(
                                                lastConcreteActivity.getId(),
                                                cact.getId(),
                                                wilos.utils.Constantes.WorkOrderType.FINISH_TO_START,
                                                _project.getId());
            }
            nbConcreteActivitiesSisters++;
            _concreteActivitiesSisters.add(cact);
            i++;
        }
    }
    
    /**
     * Instanciates an activity for a project
     * 
     * @param _project
     *                project for which the activity shall be instanciated
     * @param _activity
     *                activity to instanciate
     */
    public void activityInstanciation(wilos.model.misc.project.Project _project,
                                      wilos.model.spem2.activity.Activity _activity,
                                      wilos.model.misc.concreteactivity.ConcreteActivity _cact,
                                      java.util.List<java.util.HashMap<java.lang.String,
                                      java.lang.Object>> _list, int _occ,
                                      boolean _isInstanciated, int _dispOrd) {
        if (_occ > 0) {
            this.concreteActivityService.getConcreteActivityDao(
                                           ).getSessionFactory(
                                               ).getCurrentSession(
                                                   ).saveOrUpdate(_cact);
            java.util.ArrayList<wilos.model.misc.concreteactivity.ConcreteActivity> concreteActivitiesSisters =
              new java.util.ArrayList<wilos.model.misc.concreteactivity.ConcreteActivity>(
              );
            int nbExistingConcreteActivitiesChildren = 0;
            java.util.Iterator extfor$iter =
              _cact.getConcreteBreakdownElements().iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement tmp =
                  (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                    extfor$iter.next();
                if (tmp instanceof wilos.model.misc.concreteactivity.ConcreteActivity) {
                    if (((wilos.model.misc.concreteactivity.ConcreteActivity)
                           tmp).getActivity().getId().equals(
                                                        _activity.getId())) {
                        nbExistingConcreteActivitiesChildren++;
                        concreteActivitiesSisters.add(
                                                    (wilos.model.misc.concreteactivity.ConcreteActivity)
                                                      tmp);
                    }
                }
            }
            this.peruseConcreteActivitiesChildren(
                   nbExistingConcreteActivitiesChildren, _occ, _project,
                   _activity, _dispOrd, _cact, _list,
                   concreteActivitiesSisters);
        }
    }
    
    /**
     * 
     * Update the activity
     * 
     * @param _project
     * @param _phase
     * @param _cact
     * @param _list
     * @param _occ
     * @param _isInstanciated
     */
    public void activityUpdate(wilos.model.misc.project.Project _project,
                               wilos.model.spem2.activity.Activity _act,
                               java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> _cacts,
                               java.util.List<java.util.HashMap<java.lang.String,
                               java.lang.Object>> _list, int _occ) {
        if (_occ > 0) {
            java.util.Iterator extfor$iter = _cacts.iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.misc.concreteactivity.ConcreteActivity tmp =
                  (wilos.model.misc.concreteactivity.ConcreteActivity)
                    extfor$iter.next();
                java.lang.String strDispOrd =
                  this.concreteActivityService.getMaxDisplayOrder(tmp);
                int dispOrd = java.lang.Integer.parseInt(strDispOrd) + 1;
                this.activityInstanciation(_project, _act, tmp, _list, _occ,
                                           true, dispOrd);
                if (tmp instanceof wilos.model.misc.project.Project) {
                    wilos.model.misc.project.Project pj =
                      (wilos.model.misc.project.Project) tmp;
                    this.projectDao.saveOrUpdateProject(pj);
                } else
                    if (tmp instanceof wilos.model.misc.concretephase.ConcretePhase) {
                        wilos.model.misc.concretephase.ConcretePhase cph = (wilos.model.misc.concretephase.ConcretePhase)
                                                                             tmp;
                        this.concretePhaseDao.saveOrUpdateConcretePhase(cph);
                    } else
                        if (tmp instanceof wilos.model.misc.concreteiteration.ConcreteIteration) {
                            wilos.model.misc.concreteiteration.ConcreteIteration cit = (wilos.model.misc.concreteiteration.ConcreteIteration)
                                                                                         tmp;
                            this.concreteIterationDao.saveOrUpdateConcreteIteration(cit);
                        } else
                            if (tmp instanceof wilos.model.misc.concreteactivity.ConcreteActivity) {
                                wilos.model.misc.concreteactivity.ConcreteActivity cact = (wilos.model.misc.concreteactivity.ConcreteActivity)
                                                                                            tmp;
                                this.concreteActivityService.saveConcreteActivity(cact);
                            }
                this.concreteActivityService.saveConcreteActivity(tmp);
            }
        } else {
            java.util.Set<wilos.model.spem2.breakdownelement.BreakdownElement> bdes = new java.util.HashSet<wilos.model.spem2.breakdownelement.BreakdownElement>();
            bdes.addAll(this.getAllBreakdownElements(_act));
            java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> cacts = new java.util.HashSet<wilos.model.misc.concreteactivity.ConcreteActivity>();
            cacts.addAll(this.getAllConcreteActivitiesForAProject(_act,
                                                                  _project));
            java.util.Iterator extfor$iter$1 = bdes.iterator();
            while (extfor$iter$1.hasNext()) {
                wilos.model.spem2.breakdownelement.BreakdownElement bde = (wilos.model.spem2.breakdownelement.BreakdownElement)
                                                                            extfor$iter$1.next();
                if (bde instanceof wilos.model.spem2.activity.Activity) {
                    wilos.model.spem2.activity.Activity act = (wilos.model.spem2.activity.Activity)
                                                                bde;
                    int occ = this.giveNbOccurences(act.getId(), _list, true);
                    this.activityUpdate(_project, act, cacts, _list, occ);
                } else
                    if (bde instanceof wilos.model.spem2.task.TaskDescriptor) {
                        wilos.model.spem2.task.TaskDescriptor td = (wilos.model.spem2.task.TaskDescriptor)
                                                                     bde;
                        int occ = this.giveNbOccurences(td.getId(), _list,
                                                        true);
                        this.taskDescriptorService.taskDescriptorUpdate(_project,
                                                                        td,
                                                                        cacts,
                                                                        occ);
                    }
            }
        }
    }
    
    /**
     * 
     * return the number of occurrences
     * 
     * @param _id
     * @param list
     * @return int
     */
    private int giveNbOccurences(java.lang.String _id,
                                 java.util.List<java.util.HashMap<java.lang.String,
                                 java.lang.Object>> list,
                                 boolean _isInstanciated) {
        int nb = 0;
        if (!_isInstanciated) nb = 1;
        java.util.Iterator extfor$iter = list.iterator();
        boolean break_0 = false;
        while (extfor$iter.hasNext() && !break_0) {
            java.util.HashMap<java.lang.String,
            java.lang.Object> hashMap = (java.util.HashMap<java.lang.String,
                                        java.lang.Object>) extfor$iter.next();
            if (!break_0)
                if (((java.lang.String) hashMap.get("id")).equals(_id)) {
                    nb = ((java.lang.Integer)
                            hashMap.get("nbOccurences")).intValue();
                    break_0 = true;
                }
        }
        return nb;
    }
    
    /**
     * 
     * return all the concrete activities
     * 
     * @param _act
     * @return Set<ConcreteActivity>
     */
    public java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> getAllConcreteActivities(wilos.model.spem2.activity.Activity _act) {
        java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> tmp =
          new java.util.HashSet<wilos.model.misc.concreteactivity.ConcreteActivity>(
          );
        this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                   _act);
        java.util.Iterator extfor$iter =
          _act.getConcreteActivities().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concreteactivity.ConcreteActivity bde =
              (wilos.model.misc.concreteactivity.ConcreteActivity)
                extfor$iter.next();
            tmp.add(bde);
        }
        return tmp;
    }
    
    /**
     * 
     * return all the concrete activities of a project
     * 
     * @param _act
     * @param _project
     * @return Set<ConcreteActivity>
     */
    public java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> getAllConcreteActivitiesForAProject(wilos.model.spem2.activity.Activity _act,
                                                                                                                 wilos.model.misc.project.Project _project) {
        labeled_1 :
        {
            java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> tmp =
                    new java.util.HashSet<wilos.model.misc.concreteactivity.ConcreteActivity>(
                    );
            this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                    _act);
            java.util.Iterator extfor$iter =
                    _act.getConcreteActivities().iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.concreteactivity.ConcreteActivity cact =
                        (wilos.model.misc.concreteactivity.ConcreteActivity)
                                extfor$iter.next();
                if (cact.getProject().getId().equals(_project.getId()))
                    tmp.add(cact);
            }
        }
        return tmp;
    }
    
    /**
     * Get the breakdownElements collection of an activity
     * 
     * @param _act
     * @return
     */
    public java.util.SortedSet<wilos.model.spem2.breakdownelement.BreakdownElement> getAllBreakdownElements(wilos.model.spem2.activity.Activity _act) {
        java.util.SortedSet<wilos.model.spem2.breakdownelement.BreakdownElement> tmp =
          new java.util.TreeSet<wilos.model.spem2.breakdownelement.BreakdownElement>(
          );
        this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                   _act);
        java.util.Iterator extfor$iter =
          _act.getBreakdownElements().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.breakdownelement.BreakdownElement bde =
              (wilos.model.spem2.breakdownelement.BreakdownElement)
                extfor$iter.next();
            tmp.add(bde);
        }
        return tmp;
    }
    
    /**
     * 
     * return all the guidance
     * 
     * @param _act
     * @return Set<Guidance>
     */
    public java.util.Set<wilos.model.spem2.guide.Guidance> getAllGuidances(wilos.model.spem2.activity.Activity _act) {
        this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                   _act);
        java.util.Set<wilos.model.spem2.guide.Guidance> tmp =
          new java.util.HashSet<wilos.model.spem2.guide.Guidance>();
        java.util.Iterator extfor$iter = _act.getGuidances().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.guide.Guidance g =
              (wilos.model.spem2.guide.Guidance) extfor$iter.next();
            tmp.add(g);
        }
        return tmp;
    }
    
    /**
     * 
     * return all the concrete activity from the activity
     * 
     * @param _act
     * @return List<ConcreteActivity>
     */
    public java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity> getConcreteActivityFromActivity(wilos.model.spem2.activity.Activity _act) {
        this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                   _act);
        java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity> tmp =
          new java.util.ArrayList<wilos.model.misc.concreteactivity.ConcreteActivity>(
          );
        java.util.Iterator extfor$iter =
          _act.getConcreteActivities().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concreteactivity.ConcreteActivity ca =
              (wilos.model.misc.concreteactivity.ConcreteActivity)
                extfor$iter.next();
            tmp.add(ca);
        }
        return tmp;
    }
    
    /**
     * 
     * return all the concrete activities of a project and the activity
     * 
     * @param _act
     * @param _project
     * @return List<ConcreteActivity>
     */
    public java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity> getConcreteActivitiesFromActivityAndForAProject(wilos.model.spem2.activity.Activity _act,
                                                                                                                              wilos.model.misc.project.Project _project) {
        this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                   _act);
        labeled_2 :
        {
            java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity> tmp =
                    new java.util.ArrayList<wilos.model.misc.concreteactivity.ConcreteActivity>(
                    );
            java.util.Iterator extfor$iter =
                    _act.getConcreteActivities().iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.concreteactivity.ConcreteActivity cact =
                        (wilos.model.misc.concreteactivity.ConcreteActivity)
                                extfor$iter.next();
                if (cact.getProject().getId().equals(_project.getId()))
                    tmp.add(cact);
            }
        }
        return tmp;
    }
    
    /**
     * 
     * Getter of project
     * 
     * @param _id
     * @return
     */
    public wilos.model.spem2.activity.Activity getActivity(java.lang.String _id) {
        return this.activityDao.getActivity(_id);
    }
    
    /**
     * Return activities list
     * 
     * @return List of activities
     */
    public java.util.List<wilos.model.spem2.activity.Activity> getAllActivities() {
        return this.activityDao.getAllActivities();
    }
    
    /**
     * 
     * return one activity from one guide
     * 
     * @param _guid
     * @return Activity
     */
    public wilos.model.spem2.activity.Activity getActivityFromGuid(java.lang.String _guid) {
        return this.activityDao.getActivityFromGuid(_guid);
    }
    
    /**
     * Save activity
     * 
     * @param _activity
     *                the activity to save
     */
    public java.lang.String saveActivity(wilos.model.spem2.activity.Activity _activity) {
        return this.activityDao.saveOrUpdateActivity(_activity);
    }
    
    /**
     * Delete an anctivity
     * 
     * @param _activity
     */
    public void deleteActivity(wilos.model.spem2.activity.Activity _activity) {
        this.activityDao.deleteActivity(_activity);
    }
    
    /**
     * Getter of activityDao.
     * 
     * @return the activityDao.
     */
    public wilos.hibernate.spem2.activity.ActivityDao getActivityDao() {
        return this.activityDao;
    }
    
    /**
     * Setter of activityDao.
     * 
     * @param _activityDao
     *                The activityDao to set.
     */
    public void setActivityDao(wilos.hibernate.spem2.activity.ActivityDao _activityDao) {
        this.activityDao = _activityDao;
    }
    
    /**
     * 
     * Getter : return the concreteIterationDao
     * 
     * @return the concreteIterationDao
     */
    public wilos.hibernate.misc.concreteiteration.ConcreteIterationDao getConcreteIterationDao() {
        return concreteIterationDao;
    }
    
    /**
     * 
     * Setter of ConcreteIterationDao
     * 
     * @param concreteIterationDao
     *                the concreteIterationDao to set
     */
    public void setConcreteIterationDao(wilos.hibernate.misc.concreteiteration.ConcreteIterationDao concreteIterationDao) {
        this.concreteIterationDao = concreteIterationDao;
    }
    
    /**
     * 
     * getter of ConcretePhaseDao
     * 
     * @return the concretePhaseDao
     */
    public wilos.hibernate.misc.concretephase.ConcretePhaseDao getConcretePhaseDao() {
        return concretePhaseDao;
    }
    
    /**
     * 
     * Setter of ConcretePhaseDao
     * 
     * @param concretePhaseDao
     *                the concretePhaseDao to set
     */
    public void setConcretePhaseDao(wilos.hibernate.misc.concretephase.ConcretePhaseDao concretePhaseDao) {
        this.concretePhaseDao = concretePhaseDao;
    }
    
    /**
     * Getter of ProjectDao
     * 
     * @return the projectDao
     */
    public wilos.hibernate.misc.project.ProjectDao getProjectDao() {
        return projectDao;
    }
    
    /**
     * 
     * Setter of ProjectDao
     * 
     * @param projectDao
     *                the projectDao to set
     */
    public void setProjectDao(wilos.hibernate.misc.project.ProjectDao projectDao) {
        this.projectDao = projectDao;
    }
    
    /**
     * 
     * Getter of TaskDescriptorService
     * 
     * @return the taskDescriptorService
     */
    public wilos.business.services.spem2.task.TaskDescriptorService getTaskDescriptorService() {
        return taskDescriptorService;
    }
    
    /**
     * 
     * Setter of TaskDescriptorService
     * 
     * @param taskDescriptorService
     *                the taskDescriptorService to set
     */
    public void setTaskDescriptorService(wilos.business.services.spem2.task.TaskDescriptorService taskDescriptorService) {
        this.taskDescriptorService = taskDescriptorService;
    }
    
    /**
     * 
     * Getter of ConcreteActivityService
     * 
     * @return the concreteActivityService
     */
    public wilos.business.services.misc.concreteactivity.ConcreteActivityService getConcreteActivityService() {
        return this.concreteActivityService;
    }
    
    /**
     * 
     * Setter of ConcreteActivityService
     * 
     * @param _concreteActivityService
     *                the concreteActivityService to set
     */
    public void setConcreteActivityService(wilos.business.services.misc.concreteactivity.ConcreteActivityService _concreteActivityService) {
        this.concreteActivityService = _concreteActivityService;
    }
    
    /**
     * 
     * Getter of ConcreteWorkOrderService
     * 
     * @return the concreteWorkOrderService
     */
    public wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService getConcreteWorkOrderService() {
        return this.concreteWorkOrderService;
    }
    
    /**
     * 
     * Setter of ConcreteWorkOrderService
     * 
     * @param _concreteWorkOrderService
     *            the concreteWorkOrderService to set
     */
    public void setConcreteWorkOrderService(wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService _concreteWorkOrderService) {
        this.concreteWorkOrderService = _concreteWorkOrderService;
    }
    
    public ActivityService() { super(); }
}

