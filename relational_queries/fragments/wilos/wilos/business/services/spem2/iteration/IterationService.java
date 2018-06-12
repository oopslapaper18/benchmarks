package wilos.business.services.spem2.iteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wilos.business.services.misc.concreteactivity.ConcreteActivityService;
import wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService;
import wilos.business.services.misc.stateservice.StateService;
import wilos.business.services.spem2.activity.ActivityService;
import wilos.business.services.spem2.breakdownelement.BreakdownElementService;
import wilos.business.services.spem2.task.TaskDescriptorService;
import wilos.hibernate.misc.concreteiteration.ConcreteIterationDao;
import wilos.hibernate.misc.concretephase.ConcretePhaseDao;
import wilos.hibernate.misc.project.ProjectDao;
import wilos.hibernate.spem2.iteration.IterationDao;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement;
import wilos.model.misc.concreteiteration.ConcreteIteration;
import wilos.model.misc.concretephase.ConcretePhase;
import wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement;
import wilos.model.misc.project.Project;
import wilos.model.spem2.activity.Activity;
import wilos.model.spem2.breakdownelement.BreakdownElement;
import wilos.model.spem2.iteration.Iteration;
import wilos.model.spem2.task.TaskDescriptor;
import wilos.utils.Constantes;
import wilos.utils.Constantes.State;

/**
 * IterationManager is a transactional class, that manages operations about
 * Iteration
 * 
 */
@org.springframework.transaction.annotation.Transactional(readOnly=false, propagation=org.springframework.transaction.annotation.Propagation.REQUIRED) 
public class IterationService {
    private wilos.hibernate.misc.concreteiteration.ConcreteIterationDao concreteIterationDao;
    private wilos.hibernate.spem2.iteration.IterationDao iterationDao;
    private wilos.hibernate.misc.concretephase.ConcretePhaseDao concretePhaseDao;
    private wilos.hibernate.misc.project.ProjectDao projectDao;
    private wilos.business.services.spem2.breakdownelement.BreakdownElementService breakdownElementService;
    private wilos.business.services.spem2.activity.ActivityService activityService;
    private wilos.business.services.misc.concreteactivity.ConcreteActivityService concreteActivityService;
    private wilos.business.services.spem2.task.TaskDescriptorService taskDescriptorService;
    private wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService concreteWorkOrderService;
    
    /**
     * 
     * Return all the Concrete iterations
     * 
     * @param _iteration
     * @return Set<ConcreteIteration>
     */
    public java.util.Set<wilos.model.misc.concreteiteration.ConcreteIteration> getAllConcreteIterations(wilos.model.spem2.iteration.Iteration _iteration) {
        java.util.Set<wilos.model.misc.concreteiteration.ConcreteIteration> tmp =
          new java.util.HashSet<wilos.model.misc.concreteiteration.ConcreteIteration>(
          );
        this.iterationDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                    _iteration);
        java.util.Iterator extfor$iter =
          _iteration.getConcreteIterations().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concreteiteration.ConcreteIteration bde =
              (wilos.model.misc.concreteiteration.ConcreteIteration)
                extfor$iter.next();
            tmp.add(bde);
        }
        return tmp;
    }
    
    /**
     * 
     * Return all the concrete activities of a project
     * 
     * @param _iteration
     * @param _project
     * @return Set<ConcreteIteration>
     */
    public java.util.Set<wilos.model.misc.concreteiteration.ConcreteIteration> getAllConcreteIterationsForAProject(wilos.model.spem2.iteration.Iteration _iteration,
                                                                                                                   wilos.model.misc.project.Project _project) {
        labeled_1 :
        {
            java.util.Set<wilos.model.misc.concreteiteration.ConcreteIteration> tmp =
                    new java.util.HashSet<wilos.model.misc.concreteiteration.ConcreteIteration>(
                    );
            this.iterationDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                    _iteration);
            this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                    _project);
            java.util.Iterator extfor$iter =
                    _iteration.getConcreteIterations().iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.concreteiteration.ConcreteIteration cit =
                        (wilos.model.misc.concreteiteration.ConcreteIteration)
                                extfor$iter.next();
                if (cit.getProject().getId().equals(_project.getId())) tmp.add(cit);
            }
        }
        return tmp;
    }
    
    /**
     * Process an iteration for a project
     * 
     * @param _project
     *                project for which the iteration shall be processed
     * @param _phase
     *                iteration to instance
     */
    public void iterationInstanciation(wilos.model.misc.project.Project _project,
                                       wilos.model.spem2.iteration.Iteration _iteration,
                                       wilos.model.misc.concreteactivity.ConcreteActivity _cact,
                                       java.util.List<java.util.HashMap<java.lang.String,
                                       java.lang.Object>> _list, int _occ,
                                       boolean _isInstanciated, int _dispOrd) {
        if (_occ > 0) {
            this.concreteActivityService.getConcreteActivityDao(
                                           ).getSessionFactory(
                                               ).getCurrentSession(
                                                   ).saveOrUpdate(_cact);
            java.util.ArrayList<wilos.model.misc.concreteiteration.ConcreteIteration> concreteIterationsSisters =
              new java.util.ArrayList<wilos.model.misc.concreteiteration.ConcreteIteration>(
              );
            int nbExistingConcreteIterationChildren = 0;
            java.util.Iterator extfor$iter =
              _cact.getConcreteBreakdownElements().iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement tmp =
                  (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                    extfor$iter.next();
                if (tmp instanceof wilos.model.misc.concreteiteration.ConcreteIteration) {
                    if (((wilos.model.misc.concreteiteration.ConcreteIteration)
                           tmp).getIteration().getId().equals(
                                                         _iteration.getId())) {
                        nbExistingConcreteIterationChildren++;
                        concreteIterationsSisters.add(
                                                    (wilos.model.misc.concreteiteration.ConcreteIteration)
                                                      tmp);
                    }
                }
            }
            int nbConcreteIterationSisters =
              nbExistingConcreteIterationChildren;
            int i = nbExistingConcreteIterationChildren + 1;
            while (i <= nbExistingConcreteIterationChildren + _occ) {
                wilos.model.misc.concreteiteration.ConcreteIteration ci =
                  new wilos.model.misc.concreteiteration.ConcreteIteration();
                java.util.List<wilos.model.spem2.breakdownelement.BreakdownElement> bdes =
                  new java.util.ArrayList<wilos.model.spem2.breakdownelement.BreakdownElement>(
                  );
                bdes.addAll(
                       this.activityService.getAllBreakdownElements(
                                              _iteration));
                if (_occ != 1 || nbExistingConcreteIterationChildren != 0) {
                    if (_iteration.getPresentationName().equals(""))
                        ci.setConcreteName(_iteration.getName() + "#" + i);
                    else
                        ci.setConcreteName(_iteration.getPresentationName() +
                                             "#" + i);
                } else
                    if (_iteration.getPresentationName().equals("")) {
                        ci.setConcreteName(_iteration.getName());
                    } else {
                        ci.setConcreteName(_iteration.getPresentationName());
                    }
                this.concreteIterationSet(ci, _iteration, _project, i, _cact,
                                          _dispOrd);
                this.concreteIterationDao.saveOrUpdateConcreteIteration(ci);
                this.instanciationElement(bdes, _occ, _list, _project,
                                          _isInstanciated, ci);
                this.concreteIterationDao.saveOrUpdateConcreteIteration(ci);
                if (nbConcreteIterationSisters != 0) {
                    wilos.model.misc.concreteiteration.ConcreteIteration lastConcreteIteration =
                      null;
                    java.util.Iterator extfor$iter$1 =
                      concreteIterationsSisters.iterator();
                    while (extfor$iter$1.hasNext()) {
                        wilos.model.misc.concreteiteration.ConcreteIteration tmp =
                          (wilos.model.misc.concreteiteration.ConcreteIteration)
                            extfor$iter$1.next();
                        if (lastConcreteIteration == null ||
                              tmp.getInstanciationOrder() >
                              lastConcreteIteration.getInstanciationOrder()) {
                            lastConcreteIteration = tmp;
                        }
                    }
                    this.concreteWorkOrderService.saveConcreteWorkOrder(
                                                    lastConcreteIteration.getId(
                                                                            ),
                                                    ci.getId(),
                                                    wilos.utils.Constantes.WorkOrderType.FINISH_TO_START,
                                                    _project.getId());
                }
                ++nbConcreteIterationSisters;
                concreteIterationsSisters.add(ci);
                i++;
            }
        }
    }
    
    /**
     * Set elements for the concrete iteration
     * @param _ci
     * @param _iteration
     * @param _project
     * @param _i
     * @param _cact
     * @param _dispOrd
     */
    private void concreteIterationSet(wilos.model.misc.concreteiteration.ConcreteIteration _ci,
                                      wilos.model.spem2.iteration.Iteration _iteration,
                                      wilos.model.misc.project.Project _project,
                                      int _i,
                                      wilos.model.misc.concreteactivity.ConcreteActivity _cact,
                                      int _dispOrd) {
        _ci.addIteration(_iteration);
        _ci.setProject(_project);
        _ci.setBreakdownElement(_iteration);
        _ci.setInstanciationOrder(_i);
        _ci.setWorkBreakdownElement(_iteration);
        _ci.setActivity(_iteration);
        _cact.setConcreteBreakdownElements(
                this.concreteActivityService.getConcreteBreakdownElements(
                                               _cact));
        _ci.addSuperConcreteActivity(_cact);
        _ci.setDisplayOrder(_ci.getSuperConcreteActivity().getDisplayOrder() +
                              java.lang.Integer.toString(_dispOrd + _i));
    }
    
    /**
     * Instanciate element
     * @param _bdes
     * @param _occ
     * @param _list
     * @param _project
     * @param _isInstanciated
     * @param _ci
     */
    private void instanciationElement(java.util.List<wilos.model.spem2.breakdownelement.BreakdownElement> _bdes,
                                      int _occ,
                                      java.util.List<java.util.HashMap<java.lang.String,
                                      java.lang.Object>> _list,
                                      wilos.model.misc.project.Project _project,
                                      boolean _isInstanciated,
                                      wilos.model.misc.concreteiteration.ConcreteIteration _ci) {
        int dispOrd = 0;
        java.util.Iterator extfor$iter = _bdes.iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.breakdownelement.BreakdownElement bde =
              (wilos.model.spem2.breakdownelement.BreakdownElement)
                extfor$iter.next();
            ++dispOrd;
            if (bde instanceof wilos.model.spem2.iteration.Iteration) {
                wilos.model.spem2.iteration.Iteration it =
                  (wilos.model.spem2.iteration.Iteration) bde;
                int occ = this.giveNbOccurences(it.getId(), _list, false);
                if (occ == 0 && _occ > 0) occ = _occ;
                this.iterationInstanciation(_project, it, _ci, _list, occ,
                                            _isInstanciated, dispOrd);
            }
            else
                if (bde instanceof wilos.model.spem2.activity.Activity) {
                    wilos.model.spem2.activity.Activity act =
                      (wilos.model.spem2.activity.Activity) bde;
                    int occ = this.giveNbOccurences(act.getId(), _list, false);
                    if (occ == 0 && _occ > 0) occ = _occ;
                    this.activityService.activityInstanciation(_project, act,
                                                               _ci, _list, occ,
                                                               _isInstanciated,
                                                               dispOrd);
                }
                else
                    if (bde instanceof wilos.model.spem2.task.TaskDescriptor) {
                        wilos.model.spem2.task.TaskDescriptor td =
                          (wilos.model.spem2.task.TaskDescriptor) bde;
                        int occ = this.giveNbOccurences(td.getId(), _list,
                                                        false);
                        if (occ == 0 && _occ > 0) occ = _occ;
                        this.taskDescriptorService.taskDescriptorInstanciation(
                                                     _project, td, _ci, occ,
                                                     _isInstanciated, dispOrd);
                    }
        }
    }
    
    /**
     * 
     * Update and actualize an iteration
     * 
     * @param _project
     * @param _it
     * @param _cacts
     * @param _list
     * @param _occ
     */
    public void iterationUpdate(wilos.model.misc.project.Project _project,
                                wilos.model.spem2.iteration.Iteration _it,
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
                this.iterationInstanciation(_project, _it, tmp, _list, _occ,
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
                        }
            }
        } else {
            java.util.Set<wilos.model.spem2.breakdownelement.BreakdownElement> bdes = new java.util.HashSet<wilos.model.spem2.breakdownelement.BreakdownElement>();
            bdes.addAll(this.activityService.getAllBreakdownElements(_it));
            java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> cacts = new java.util.HashSet<wilos.model.misc.concreteactivity.ConcreteActivity>();
            cacts.addAll(this.getAllConcreteIterationsForAProject(_it,
                                                                  _project));
            java.util.Iterator extfor$iter$1 = bdes.iterator();
            while (extfor$iter$1.hasNext()) {
                wilos.model.spem2.breakdownelement.BreakdownElement bde = (wilos.model.spem2.breakdownelement.BreakdownElement)
                                                                            extfor$iter$1.next();
                if (bde instanceof wilos.model.spem2.iteration.Iteration) {
                    wilos.model.spem2.iteration.Iteration it = (wilos.model.spem2.iteration.Iteration)
                                                                 bde;
                    int occ = this.giveNbOccurences(it.getId(), _list, true);
                    this.iterationUpdate(_project, it, cacts, _list, occ);
                } else
                    if (bde instanceof wilos.model.spem2.activity.Activity) {
                        wilos.model.spem2.activity.Activity act = (wilos.model.spem2.activity.Activity)
                                                                    bde;
                        int occ = this.giveNbOccurences(act.getId(), _list,
                                                        true);
                        this.activityService.activityUpdate(_project, act,
                                                            cacts, _list, occ);
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
     * Give(return) the number of occurrences
     * 
     * @param _id
     * @param list
     * @param _isInstanciated
     * @return int
     */
    private int giveNbOccurences(java.lang.String _id,
                                 java.util.List<java.util.HashMap<java.lang.String,
                                 java.lang.Object>> list,
                                 boolean _isInstanciated) {
        int nb;
        if (!_isInstanciated) nb = 1; else nb = 0;
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
     * Getter of concreteIterationDao
     * 
     * @return the concreteIterationDao
     */
    public wilos.hibernate.misc.concreteiteration.ConcreteIterationDao getConcreteIterationDao() {
        return concreteIterationDao;
    }
    
    /**
     * Setter of concreteIterationDao
     * 
     * @param concreteIterationDao
     *                the concreteIterationDao to set
     */
    public void setConcreteIterationDao(wilos.hibernate.misc.concreteiteration.ConcreteIterationDao concreteIterationDao) {
        this.concreteIterationDao = concreteIterationDao;
    }
    
    /**
     * 
     * Getter of BreakdownElementService
     * 
     * @return the breakdownElementService
     */
    public wilos.business.services.spem2.breakdownelement.BreakdownElementService getBreakdownElementService() {
        return breakdownElementService;
    }
    
    /**
     * 
     * Setter of BreakdownElementService
     * 
     * @param breakdownElementService
     *                the breakdownElementService to set
     */
    public void setBreakdownElementService(wilos.business.services.spem2.breakdownelement.BreakdownElementService breakdownElementService) {
        this.breakdownElementService = breakdownElementService;
    }
    
    /**
     * Getter of ActivityService
     * 
     * @return the activityService
     */
    public wilos.business.services.spem2.activity.ActivityService getActivityService() {
        return activityService;
    }
    
    /**
     * 
     * Setter of ActivityService
     * 
     * @param activityService
     *                the activityService to set
     */
    public void setActivityService(wilos.business.services.spem2.activity.ActivityService activityService) {
        this.activityService = activityService;
    }
    
    /**
     * 
     * GEtter of ConcreteActivityService
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
     * Getter of IterationDao
     * 
     * @return the iterationDao
     */
    public wilos.hibernate.spem2.iteration.IterationDao getIterationDao() {
        return iterationDao;
    }
    
    /**
     * 
     * Setter of IterationDao
     * 
     * @param iterationDao
     *                the iterationDao to set
     */
    public void setIterationDao(wilos.hibernate.spem2.iteration.IterationDao iterationDao) {
        this.iterationDao = iterationDao;
    }
    
    /**
     * Getter of ConcretePhaseDao
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
     * 
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
    
    public IterationService() { super(); }
}

