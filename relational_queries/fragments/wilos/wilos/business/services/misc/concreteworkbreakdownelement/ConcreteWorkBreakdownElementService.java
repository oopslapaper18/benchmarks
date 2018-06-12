package wilos.business.services.misc.concreteworkbreakdownelement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wilos.business.services.spem2.workbreakdownelement.WorkBreakdownElementService;
import wilos.hibernate.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementDao;
import wilos.hibernate.misc.project.ProjectDao;
import wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement;
import wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkOrder;
import wilos.model.misc.project.Project;
import wilos.model.spem2.workbreakdownelement.WorkBreakdownElement;
import wilos.model.spem2.workbreakdownelement.WorkOrder;

/**
 * @author Sebastien
 * 
 */
@org.springframework.transaction.annotation.Transactional(readOnly=false, propagation=org.springframework.transaction.annotation.Propagation.REQUIRED) 
public class ConcreteWorkBreakdownElementService {
    private wilos.hibernate.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementDao concreteWorkBreakdownElementDao;
    private wilos.business.services.spem2.workbreakdownelement.WorkBreakdownElementService workBreakdownElementService;
    private wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService concreteWorkOrderService;
    private wilos.hibernate.misc.project.ProjectDao projectDao;
    
    /**
     * Get the ConcreteWorkBreakdownElements list having at least one successor
     * 
     * @return List<ConcreteWorkBreakdownElement>
     */
    public java.util.List<wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement> getAllConcreteWorkBreakdownElementsWithAtLeastOneSuccessor(wilos.model.misc.project.Project _project) {
        this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                  _project);
        labeled_1 :
        {
            java.util.List<wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement> tmp =
                    new java.util.ArrayList<wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement>(
                    );
            java.util.Iterator extfor$iter =
                    this.concreteWorkBreakdownElementDao.getAllConcreteWorkBreakdownElements(
                    ).iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement cwbde =
                        (wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement)
                                extfor$iter.next();
                wilos.model.spem2.workbreakdownelement.WorkBreakdownElement wbde =
                        cwbde.getWorkBreakdownElement();
                if (!(cwbde instanceof wilos.model.misc.project.Project) && wbde !=
                        null)
                {
                    java.lang.String id = cwbde.getProject().getId();
                    if (id.equals(_project.getId()) &&
                            wbde.getSuccessors().size() != 0)
                    {
                        tmp.add(cwbde);
                    }
                }
            }
        }
        return tmp;
    }
    
    /**
     * Allows to get the list of all concreteBreakdownElements for a project
     * 
     * @param _project
     * @return the list of all concreteBreakdownElements
     */
    public java.util.List<wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement> getAllConcreteWorkBreakdownElements(wilos.model.misc.project.Project _project) {
        this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                  _project);
        java.util.List<wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement> tmp =
          new java.util.ArrayList<wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement>(
          );
        java.util.Iterator extfor$iter =
          this.concreteWorkBreakdownElementDao.getAllConcreteWorkBreakdownElements(
                                                 ).iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement cwbde =
              (wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement)
                extfor$iter.next();
            wilos.model.spem2.workbreakdownelement.WorkBreakdownElement wbde =
              cwbde.getWorkBreakdownElement();
            if (!(cwbde instanceof wilos.model.misc.project.Project) && wbde !=
                  null) {
                java.lang.String id = cwbde.getProject().getId();
                if (id.equals(_project.getId())) { tmp.add(cwbde); }
            }
        }
        return tmp;
    }
    
    /**
     * Allows to get the list of super concretActivities from a
     * concreteWorkBreakdownElement
     * 
     * @param _cwbde
     * @return the list of super concretActivities
     */
    public java.util.List<wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement> getSuperConcreteActivitiesFromConcreteWorkBreakdownElement(wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement _cwbde) {
        this.concreteWorkBreakdownElementDao.getSessionFactory(
                                               ).getCurrentSession(
                                                   ).saveOrUpdate(_cwbde);
        java.util.List<wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement> tmp =
          new java.util.ArrayList<wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement>(
          );
        java.util.Iterator extfor$iter =
          _cwbde.getSuperConcreteActivities().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement cwbde =
              (wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement)
                extfor$iter.next();
            tmp.add(cwbde);
        }
        return tmp;
    }
    
    /**
     * Allows to check if a concreteWorkBreakdownElement's predecessor is
     * instanciable
     * 
     * @param _cwbde
     * @return true if the predecessor is instanciable, false in the other case
     */
    public boolean isInstanciablePredecessor(wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement _cwbde) {
        boolean instanciable = false;
        this.concreteWorkBreakdownElementDao.getSessionFactory(
                                               ).getCurrentSession(
                                                   ).saveOrUpdate(_cwbde);
        wilos.model.spem2.workbreakdownelement.WorkBreakdownElement pred =
          _cwbde.getWorkBreakdownElement();
        int s = 0;
        java.util.Iterator extfor$iter = pred.getSuccessors().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.workbreakdownelement.WorkOrder wo =
              (wilos.model.spem2.workbreakdownelement.WorkOrder)
                extfor$iter.next();
            wilos.model.spem2.workbreakdownelement.WorkBreakdownElement succ =
              wo.getSuccessor();
            int p = _cwbde.getConcreteSuccessors().size();
            s +=
              this.workBreakdownElementService.getAllConcreteWorkBreakdownElementsFromWorkBreakdownElement(
                                                 succ).size();
            if (p < s) { instanciable = true; }
        }
        return instanciable;
    }
    
    /**
     * Allows to get a concreteWorkBrreakdownElement with its id
     * 
     * @param _id
     * @return the concreteWorkBrreakdownElement
     */
    public wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement getConcreteWorkBreakdownElement(java.lang.String _id) {
        return this.concreteWorkBreakdownElementDao.getConcreteWorkBreakdownElement(
                                                      _id);
    }
    
    /**
     * Allows to get the concreteWorkBreakdownElementDao
     * 
     * @return the concreteWorkBreakdownElementDao
     */
    public wilos.hibernate.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementDao getConcreteWorkBreakdownElementDao() {
        return this.concreteWorkBreakdownElementDao;
    }
    
    /**
     * Allows to set the concreteWorkBreakdownElementDao
     * 
     * @param _concreteWorkBreakdownElementDao
     */
    public void setConcreteWorkBreakdownElementDao(wilos.hibernate.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementDao _concreteWorkBreakdownElementDao) {
        this.concreteWorkBreakdownElementDao = _concreteWorkBreakdownElementDao;
    }
    
    /**
     * Allows to get the projectDao
     * 
     * @return the projectDao
     */
    public wilos.hibernate.misc.project.ProjectDao getProjectDao() {
        return this.projectDao;
    }
    
    /**
     * Allows to set the projectDao
     * 
     * @param _projectDao
     */
    public void setProjectDao(wilos.hibernate.misc.project.ProjectDao _projectDao) {
        this.projectDao = _projectDao;
    }
    
    /**
     * Allows to get the workBreakdownElementService
     * 
     * @return the workBreakdownElementService
     */
    public wilos.business.services.spem2.workbreakdownelement.WorkBreakdownElementService getWorkBreakdownElementService() {
        return this.workBreakdownElementService;
    }
    
    /**
     * Allows to set the workBreakdownElementService
     * 
     * @param _workBreakdownElementService
     */
    public void setWorkBreakdownElementService(wilos.business.services.spem2.workbreakdownelement.WorkBreakdownElementService _workBreakdownElementService) {
        this.workBreakdownElementService = _workBreakdownElementService;
    }
    
    /**
     * Allows to get the concreteWorkOrderService
     * 
     * @return the concreteWorkOrderService
     */
    public wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService getConcreteWorkOrderService() {
        return this.concreteWorkOrderService;
    }
    
    /**
     * Allows to set the concreteWorkOrderService
     * 
     * @param _concreteWorkOrderService
     * 
     */
    public void setConcreteWorkOrderService(wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkOrderService _concreteWorkOrderService) {
        this.concreteWorkOrderService = _concreteWorkOrderService;
    }
    
    /**
     * Allows to get the list of concretePredecessor by hashMap
     * 
     * @param _cwbde
     * @return the list of concretePredecessor by hashMap
     */
    public java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> getConcretePredecessorHashMap(wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement _cwbde) {
        java.util.List<java.util.HashMap<java.lang.String,
        java.lang.Object>> predecessorHashMap =
          new java.util.ArrayList<java.util.HashMap<java.lang.String,
        java.lang.Object>>();
        if (_cwbde != null) {
            _cwbde = this.getConcreteWorkBreakdownElement(_cwbde.getId());
            if (_cwbde != null) {
                java.util.Iterator extfor$iter =
                  _cwbde.getConcretePredecessors().iterator();
                while (extfor$iter.hasNext()) {
                    wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkOrder cwo =
                      (wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkOrder)
                        extfor$iter.next();
                    java.util.HashMap<java.lang.String,
                    java.lang.Object> hm =
                      new java.util.HashMap<java.lang.String,
                    java.lang.Object>();
                    wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement cPred =
                      this.getConcreteWorkBreakdownElementDao(
                             ).getConcreteWorkBreakdownElement(
                                 cwo.getConcreteWorkOrderId(
                                       ).getConcretePredecessorId());
                    hm.put("pred", cPred.getConcreteName());
                    hm.put("plannedStartingDate",
                           cPred.getPlannedStartingDate());
                    hm.put("plannedFinishingDate",
                           cPred.getPlannedFinishingDate());
                    hm.put("linkType", cwo.getConcreteLinkType());
                    predecessorHashMap.add(hm);
                }
            }
        }
        return predecessorHashMap;
    }
    
    /**
     * Allows to get the list of concreteSuccessors by hashMap
     * 
     * @param _cwbde
     * @return the list of concreteSuccessors by hashMap
     */
    public java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> getConcreteSuccessorHashMap(wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement _cwbde) {
        java.util.List<java.util.HashMap<java.lang.String,
        java.lang.Object>> successorHashMap =
          new java.util.ArrayList<java.util.HashMap<java.lang.String,
        java.lang.Object>>();
        if (_cwbde != null) {
            _cwbde = this.getConcreteWorkBreakdownElement(_cwbde.getId());
            if (_cwbde != null) {
                java.util.Iterator extfor$iter =
                  _cwbde.getConcreteSuccessors().iterator();
                while (extfor$iter.hasNext()) {
                    wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkOrder cwo =
                      (wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkOrder)
                        extfor$iter.next();
                    java.util.HashMap<java.lang.String,
                    java.lang.Object> hm =
                      new java.util.HashMap<java.lang.String,
                    java.lang.Object>();
                    wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement cSucc =
                      this.getConcreteWorkBreakdownElementDao(
                             ).getConcreteWorkBreakdownElement(
                                 cwo.getConcreteWorkOrderId(
                                       ).getConcreteSuccessorId());
                    hm.put("succ", cSucc.getConcreteName());
                    hm.put("plannedStartingDate",
                           cSucc.getPlannedStartingDate());
                    hm.put("plannedFinishingDate",
                           cSucc.getPlannedFinishingDate());
                    hm.put("linkType", cwo.getConcreteLinkType());
                    successorHashMap.add(hm);
                }
            }
        }
        return successorHashMap;
    }
    
    /**
     * Return the project associated to the id
     * 
     * @param _id
     * @return the project
     */
    public wilos.model.misc.project.Project getProject(java.lang.String _id) {
        return this.projectDao.getProject(_id);
    }
    
    public ConcreteWorkBreakdownElementService() { super(); }
}

