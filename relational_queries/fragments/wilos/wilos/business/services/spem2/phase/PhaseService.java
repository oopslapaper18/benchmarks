package wilos.business.services.spem2.phase;


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
import wilos.business.services.spem2.iteration.IterationService;
import wilos.business.services.spem2.task.TaskDescriptorService;
import wilos.hibernate.misc.concretephase.ConcretePhaseDao;
import wilos.hibernate.misc.project.ProjectDao;
import wilos.hibernate.spem2.phase.PhaseDao;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement;
import wilos.model.misc.concretephase.ConcretePhase;
import wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement;
import wilos.model.misc.project.Project;
import wilos.model.spem2.activity.Activity;
import wilos.model.spem2.breakdownelement.BreakdownElement;
import wilos.model.spem2.iteration.Iteration;
import wilos.model.spem2.phase.Phase;
import wilos.model.spem2.task.TaskDescriptor;
import wilos.utils.Constantes;
import wilos.utils.Constantes.State;

/**
 * PhaseManager is a transactional class, that manages operations about phase
 * 
 */
@org.springframework.transaction.annotation.Transactional(readOnly=false, propagation=org.springframework.transaction.annotation.Propagation.REQUIRED) 
public class PhaseService {
    private wilos.hibernate.misc.concretephase.ConcretePhaseDao concretePhaseDao;
    private wilos.hibernate.spem2.phase.PhaseDao phaseDao;
    private wilos.hibernate.misc.project.ProjectDao projectDao;
    private wilos.business.services.spem2.iteration.IterationService iterationService;
    private wilos.business.services.spem2.activity.ActivityService activityService;
    private wilos.business.services.misc.concreteactivity.ConcreteActivityService concreteActivityService;
    private wilos.business.services.spem2.task.TaskDescriptorService taskDescriptorService;
    private wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService concreteWorkOrderService;
    
    /**
     * 
     * Return all the concrete phases
     * 
     * @param _phase
     * @return Set<ConcretePhase>
     */
    public java.util.Set<wilos.model.misc.concretephase.ConcretePhase> getAllConcretePhases(wilos.model.spem2.phase.Phase _phase) {
        java.util.Set<wilos.model.misc.concretephase.ConcretePhase> tmp =
          new java.util.HashSet<wilos.model.misc.concretephase.ConcretePhase>();
        this.phaseDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                _phase);
        java.util.Iterator extfor$iter =
          _phase.getConcretePhases().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concretephase.ConcretePhase bde =
              (wilos.model.misc.concretephase.ConcretePhase) extfor$iter.next();
            tmp.add(bde);
        }
        return tmp;
    }
    
    /**
     * 
     * Return all the concrete activities of a project
     * 
     * @param _phase
     * @param _project
     * @return Set<ConcretePhase>
     */
    public java.util.Set<wilos.model.misc.concretephase.ConcretePhase> getAllConcretePhasesForAProject(wilos.model.spem2.phase.Phase _phase,
                                                                                                       wilos.model.misc.project.Project _project) {
        labeled_1 :
        {
            java.util.Set<wilos.model.misc.concretephase.ConcretePhase> tmp =
                    new java.util.HashSet<wilos.model.misc.concretephase.ConcretePhase>();
            this.phaseDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                    _phase);
            this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                    _project);
            java.util.Iterator extfor$iter =
                    _phase.getConcretePhases().iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.concretephase.ConcretePhase cph =
                        (wilos.model.misc.concretephase.ConcretePhase) extfor$iter.next();
                if (cph.getProject().getId().equals(_project.getId())) tmp.add(cph);
            }
        }
        return tmp;
    }
    
    /**
     * Exploit all elements of the breakdown element list
     * Instanciate elements
     * @param _bdes
     * @param _occ
     * @param _isInstanciated
     * @param _project
     * @param _phase
     * @param _list
     * @param _cp
     */
    private void peruseBreakdownElementList(java.util.Set<wilos.model.spem2.breakdownelement.BreakdownElement> _bdes,
                                            int _occ, boolean _isInstanciated,
                                            wilos.model.misc.project.Project _project,
                                            wilos.model.spem2.phase.Phase _phase,
                                            java.util.List<java.util.HashMap<java.lang.String,
                                            java.lang.Object>> _list,
                                            wilos.model.misc.concretephase.ConcretePhase _cp) {
        int dispOrd = 0;
        java.util.Iterator extfor$iter = _bdes.iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.breakdownelement.BreakdownElement bde =
              (wilos.model.spem2.breakdownelement.BreakdownElement)
                extfor$iter.next();
            ++dispOrd;
            if (bde instanceof wilos.model.spem2.phase.Phase) {
                wilos.model.spem2.phase.Phase ph =
                  (wilos.model.spem2.phase.Phase) bde;
                int occ = this.giveNbOccurences(ph.getId(), _list, false);
                this.phaseInstanciation(_project, ph, _project, _list, occ,
                                        _isInstanciated, dispOrd);
            }
            else
                if (bde instanceof wilos.model.spem2.iteration.Iteration) {
                    wilos.model.spem2.iteration.Iteration it =
                      (wilos.model.spem2.iteration.Iteration) bde;
                    int occ = this.giveNbOccurences(it.getId(), _list, false);
                    this.iterationService.iterationInstanciation(
                                            _project, it, _cp, _list, occ,
                                            _isInstanciated, dispOrd);
                }
                else
                    if (bde instanceof wilos.model.spem2.activity.Activity) {
                        wilos.model.spem2.activity.Activity act =
                          (wilos.model.spem2.activity.Activity) bde;
                        int occ = this.giveNbOccurences(act.getId(), _list,
                                                        false);
                        this.activityService.activityInstanciation(
                                               _project, act, _cp,
                                               _list, occ, _isInstanciated,
                                               dispOrd);
                    } else
                        if (bde instanceof wilos.model.spem2.task.TaskDescriptor) {
                            wilos.model.spem2.task.TaskDescriptor td = (wilos.model.spem2.task.TaskDescriptor)
                                                                         bde;
                            int occ = this.giveNbOccurences(td.getId(), _list,
                                                            false);
                            this.taskDescriptorService.taskDescriptorInstanciation(_project,
                                                                                   td,
                                                                                   _cp,
                                                                                   occ,
                                                                                   _isInstanciated,
                                                                                   dispOrd);
                        }
        }
    }
    
    /**
     * process a phase for a project
     * 
     * @param _project
     *                project for which the Phase shall be processed
     * @param _phase
     *                phase to instances
     */
    public void phaseInstanciation(wilos.model.misc.project.Project _project,
                                   wilos.model.spem2.phase.Phase _phase,
                                   wilos.model.misc.concreteactivity.ConcreteActivity _cact,
                                   java.util.List<java.util.HashMap<java.lang.String,
                                   java.lang.Object>> _list, int _occ,
                                   boolean _isInstanciated, int _dispOrd) {
        if (_occ > 0) {
            this.concreteActivityService.getConcreteActivityDao(
                                           ).getSessionFactory(
                                               ).getCurrentSession(
                                                   ).saveOrUpdate(_cact);
            java.util.ArrayList<wilos.model.misc.concretephase.ConcretePhase> concretePhasesSisters =
              new java.util.ArrayList<wilos.model.misc.concretephase.ConcretePhase>(
              );
            int nbExistingConcretePhaseChildren = 0;
            java.util.Iterator extfor$iter =
              _cact.getConcreteBreakdownElements().iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement tmp =
                  (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                    extfor$iter.next();
                if (tmp instanceof wilos.model.misc.concretephase.ConcretePhase) {
                    if (((wilos.model.misc.concretephase.ConcretePhase)
                           tmp).getPhase().getId().equals(_phase.getId())) {
                        nbExistingConcretePhaseChildren++;
                        concretePhasesSisters.add(
                                                (wilos.model.misc.concretephase.ConcretePhase)
                                                  tmp);
                    }
                }
            }
            int nbConcretePhaseSisters = nbExistingConcretePhaseChildren;
            int i = nbExistingConcretePhaseChildren + 1;
            while (i <= nbExistingConcretePhaseChildren + _occ) {
                wilos.model.misc.concretephase.ConcretePhase cp =
                  new wilos.model.misc.concretephase.ConcretePhase();
                java.util.Set<wilos.model.spem2.breakdownelement.BreakdownElement> bdes =
                  new java.util.HashSet<wilos.model.spem2.breakdownelement.BreakdownElement>(
                  );
                bdes.addAll(
                       this.activityService.getAllBreakdownElements(_phase));
                if (_occ != 1 || nbExistingConcretePhaseChildren != 0) {
                    if (_phase.getPresentationName().equals(""))
                        cp.setConcreteName(_phase.getName() + "#" + i);
                    else
                        cp.setConcreteName(_phase.getPresentationName() + "#" +
                                             i);
                } else {
                    if (_phase.getPresentationName().equals(""))
                        cp.setConcreteName(_phase.getName()); else
                        cp.setConcreteName(_phase.getPresentationName());
                }
                cp.addPhase(_phase);
                cp.setProject(_project);
                cp.setBreakdownElement(_phase);
                cp.setInstanciationOrder(i);
                cp.setWorkBreakdownElement(_phase);
                cp.setActivity(_phase);
                _cact.setConcreteBreakdownElements(
                        this.concreteActivityService.getConcreteBreakdownElements(
                                                       _cact));
                cp.addSuperConcreteActivity(_cact);
                cp.setDisplayOrder(
                     cp.getSuperConcreteActivity().getDisplayOrder() +
                       java.lang.Integer.toString(_dispOrd + i));
                this.concretePhaseDao.saveOrUpdateConcretePhase(cp);
                this.peruseBreakdownElementList(bdes, _occ, _isInstanciated,
                                                _project, _phase, _list, cp);
                this.concretePhaseDao.saveOrUpdateConcretePhase(cp);
                if (nbConcretePhaseSisters != 0) {
                    wilos.model.misc.concretephase.ConcretePhase lastConcretePhase =
                      null;
                    java.util.Iterator extfor$iter$1 =
                      concretePhasesSisters.iterator();
                    while (extfor$iter$1.hasNext()) {
                        wilos.model.misc.concretephase.ConcretePhase tmp =
                          (wilos.model.misc.concretephase.ConcretePhase)
                            extfor$iter$1.next();
                        if (lastConcretePhase == null ||
                              tmp.getInstanciationOrder() >
                              lastConcretePhase.getInstanciationOrder()) {
                            lastConcretePhase = tmp;
                        }
                    }
                    this.concreteWorkOrderService.saveConcreteWorkOrder(
                                                    lastConcretePhase.getId(),
                                                    cp.getId(),
                                                    wilos.utils.Constantes.WorkOrderType.FINISH_TO_START,
                                                    _project.getId());
                }
                ++nbConcretePhaseSisters;
                concretePhasesSisters.add(cp);
                i++;
            }
        }
    }
    
    private void UpdateElementOfBreakdownElementList(java.util.Set<wilos.model.spem2.breakdownelement.BreakdownElement> _bdes,
                                                     wilos.model.misc.project.Project _project,
                                                     wilos.model.spem2.phase.Phase _phase,
                                                     java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> _cacts,
                                                     java.util.List<java.util.HashMap<java.lang.String,
                                                     java.lang.Object>> _list) {
        java.util.Iterator extfor$iter = _bdes.iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.breakdownelement.BreakdownElement bde =
              (wilos.model.spem2.breakdownelement.BreakdownElement)
                extfor$iter.next();
            if (bde instanceof wilos.model.spem2.phase.Phase) {
                wilos.model.spem2.phase.Phase ph =
                  (wilos.model.spem2.phase.Phase) bde;
                int occ = this.giveNbOccurences(ph.getId(), _list, true);
                this.phaseUpdate(_project, ph, _cacts, _list, occ);
            }
            else
                if (bde instanceof wilos.model.spem2.iteration.Iteration) {
                    wilos.model.spem2.iteration.Iteration it =
                      (wilos.model.spem2.iteration.Iteration) bde;
                    int occ = this.giveNbOccurences(it.getId(), _list, true);
                    this.iterationService.iterationUpdate(_project, it, _cacts,
                                                          _list, occ);
                }
                else
                    if (bde instanceof wilos.model.spem2.activity.Activity) {
                        wilos.model.spem2.activity.Activity act =
                          (wilos.model.spem2.activity.Activity) bde;
                        int occ = this.giveNbOccurences(act.getId(), _list,
                                                        true);
                        this.activityService.activityUpdate(_project, act,
                                                            _cacts, _list, occ);
                    } else
                        if (bde instanceof wilos.model.spem2.task.TaskDescriptor) {
                            wilos.model.spem2.task.TaskDescriptor td = (wilos.model.spem2.task.TaskDescriptor)
                                                                         bde;
                            int occ = this.giveNbOccurences(td.getId(), _list,
                                                            true);
                            this.taskDescriptorService.taskDescriptorUpdate(_project,
                                                                            td,
                                                                            _cacts,
                                                                            occ);
                        }
        }
    }
    
    /**
     * 
     * Update and actualize a phase
     * 
     * @param _project
     * @param _phase
     * @param _cacts
     * @param _list
     * @param _occ
     */
    public void phaseUpdate(wilos.model.misc.project.Project _project,
                            wilos.model.spem2.phase.Phase _phase,
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
                this.phaseInstanciation(_project, _phase, tmp, _list, _occ,
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
                    }
            }
        } else {
            java.util.Set<wilos.model.spem2.breakdownelement.BreakdownElement> bdes = new java.util.HashSet<wilos.model.spem2.breakdownelement.BreakdownElement>();
            bdes.addAll(this.activityService.getAllBreakdownElements(_phase));
            java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> cacts = new java.util.HashSet<wilos.model.misc.concreteactivity.ConcreteActivity>();
            cacts.addAll(this.getAllConcretePhasesForAProject(_phase,
                                                              _project));
            this.UpdateElementOfBreakdownElementList(bdes, _project, _phase,
                                                     cacts, _list);
        }
    }
    
    /**
     * 
     * Give (return) the number of occurrences
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
     * Getter of concretePhaseDao
     * 
     * @return the concretePhaseDao
     */
    public wilos.hibernate.misc.concretephase.ConcretePhaseDao getConcretePhaseDao() {
        return concretePhaseDao;
    }
    
    /**
     * Setter of concretePhaseDao
     * 
     * @param concretePhaseDao
     *                the concretePhaseDao to set
     */
    public void setConcretePhaseDao(wilos.hibernate.misc.concretephase.ConcretePhaseDao concretePhaseDao) {
        this.concretePhaseDao = concretePhaseDao;
    }
    
    /**
     * 
     * Getter of IterationService
     * 
     * @return the iterationService
     */
    public wilos.business.services.spem2.iteration.IterationService getIterationService() {
        return iterationService;
    }
    
    /**
     * 
     * Setter of IterationService
     * 
     * @param iterationService
     *                the iterationService to set
     */
    public void setIterationService(wilos.business.services.spem2.iteration.IterationService iterationService) {
        this.iterationService = iterationService;
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
     * Getter of PhaseDao
     * 
     * @return the phaseDao
     */
    public wilos.hibernate.spem2.phase.PhaseDao getPhaseDao() {
        return phaseDao;
    }
    
    /**
     * 
     * Setter of PhaseDao
     * 
     * @param phaseDao
     *                the phaseDao to set
     */
    public void setPhaseDao(wilos.hibernate.spem2.phase.PhaseDao phaseDao) {
        this.phaseDao = phaseDao;
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
     *                the concreteWorkOrderService to set
     */
    public void setConcreteWorkOrderService(wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService _concreteWorkOrderService) {
        this.concreteWorkOrderService = _concreteWorkOrderService;
    }
    
    public PhaseService() { super(); }
}

