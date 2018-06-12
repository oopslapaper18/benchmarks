/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

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
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class IterationService {

    private ConcreteIterationDao concreteIterationDao;

    private IterationDao iterationDao;

    private ConcretePhaseDao concretePhaseDao;

    private ProjectDao projectDao;

    private BreakdownElementService breakdownElementService;

    private ActivityService activityService;

    private ConcreteActivityService concreteActivityService;

    private TaskDescriptorService taskDescriptorService;

    private ConcreteWorkOrderService concreteWorkOrderService;


    /**
     * 
     * Return all the Concrete iterations
     * 
     * @param _iteration
     * @return Set<ConcreteIteration>
     */
    public Set<ConcreteIteration> getAllConcreteIterations(Iteration _iteration) {
	Set<ConcreteIteration> tmp = new HashSet<ConcreteIteration>();
	this.iterationDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_iteration);
	for (ConcreteIteration bde : _iteration.getConcreteIterations()) {
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
    public Set<ConcreteIteration> getAllConcreteIterationsForAProject(
	    Iteration _iteration, Project _project) {
	Set<ConcreteIteration> tmp = new HashSet<ConcreteIteration>();
	this.iterationDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_iteration);
	this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_project);
	for (ConcreteIteration cit : _iteration.getConcreteIterations()) {
	    if (cit.getProject().getId().equals(_project.getId()))
		tmp.add(cit);
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
    public void iterationInstanciation(Project _project, Iteration _iteration,
	    ConcreteActivity _cact, List<HashMap<String, Object>> _list,
	    int _occ, boolean _isInstanciated, int _dispOrd) {

	if (_occ > 0) {
	    this.concreteActivityService.getConcreteActivityDao()
		    .getSessionFactory().getCurrentSession()
		    .saveOrUpdate(_cact);
	    ArrayList<ConcreteIteration> concreteIterationsSisters = new ArrayList<ConcreteIteration>();
	    int nbExistingConcreteIterationChildren = 0;
	    for (ConcreteBreakdownElement tmp : _cact
		    .getConcreteBreakdownElements()) {
		if (tmp instanceof ConcreteIteration) {
		    if (((ConcreteIteration) tmp).getIteration().getId()
			    .equals(_iteration.getId())) {
			nbExistingConcreteIterationChildren++;
			concreteIterationsSisters.add((ConcreteIteration) tmp);
		    }
		}
	    }
	    int nbConcreteIterationSisters = nbExistingConcreteIterationChildren;

	    for (int i = nbExistingConcreteIterationChildren + 1; i <= nbExistingConcreteIterationChildren
		    + _occ; i++) {

		ConcreteIteration ci = new ConcreteIteration();

		List<BreakdownElement> bdes = new ArrayList<BreakdownElement>();
		bdes.addAll(this.activityService
			.getAllBreakdownElements(_iteration));

		if (_occ != 1 || nbExistingConcreteIterationChildren != 0) {
		    if (_iteration.getPresentationName().equals(""))
			ci.setConcreteName(_iteration.getName() + "#" + i);
		    else
			ci.setConcreteName(_iteration.getPresentationName()
				+ "#" + i);
		} else if (_iteration.getPresentationName().equals("")) {
		    ci.setConcreteName(_iteration.getName());
		} else {
		    ci.setConcreteName(_iteration.getPresentationName());
		}

		this.concreteIterationSet(ci, _iteration, _project, i, _cact, _dispOrd);
	
		this.concreteIterationDao.saveOrUpdateConcreteIteration(ci);

		//instanciate elements
		this.instanciationElement(bdes, _occ, _list, _project, _isInstanciated, ci);

		this.concreteIterationDao.saveOrUpdateConcreteIteration(ci);

		// if added ConcreteIteration has sisters, we add a FtS
		// dependency between it and its predecessor
		if (nbConcreteIterationSisters != 0) {
		    ConcreteIteration lastConcreteIteration = null;
		    for (ConcreteIteration tmp : concreteIterationsSisters) {
			if (lastConcreteIteration == null
				|| tmp.getInstanciationOrder() > lastConcreteIteration
					.getInstanciationOrder()) {
			    lastConcreteIteration = tmp;
			}
		    }
		    this.concreteWorkOrderService.saveConcreteWorkOrder(
			    lastConcreteIteration.getId(), ci.getId(),
			    Constantes.WorkOrderType.FINISH_TO_START, _project
				    .getId());

		}

		++nbConcreteIterationSisters;
		concreteIterationsSisters.add(ci);
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
    private void concreteIterationSet(ConcreteIteration _ci, Iteration _iteration,
	    Project _project, int _i, ConcreteActivity _cact, int _dispOrd){
	_ci.addIteration(_iteration);
	_ci.setProject(_project);
	_ci.setBreakdownElement(_iteration);
	_ci.setInstanciationOrder(_i);
	_ci.setWorkBreakdownElement(_iteration);
	_ci.setActivity(_iteration);
	_cact.setConcreteBreakdownElements(this.concreteActivityService
		.getConcreteBreakdownElements(_cact));
	_ci.addSuperConcreteActivity(_cact);
	_ci.setDisplayOrder(_ci.getSuperConcreteActivity()
		.getDisplayOrder()
		+ Integer.toString(_dispOrd + _i));
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
    private void instanciationElement(List<BreakdownElement> _bdes, int _occ,
	    List<HashMap<String, Object>> _list, Project _project, boolean _isInstanciated,
	    ConcreteIteration _ci)
    {

	int dispOrd = 0;
	for (BreakdownElement bde : _bdes) {
	    ++dispOrd;
	    if (bde instanceof Iteration) {
		Iteration it = (Iteration) bde; 
		int occ = this.giveNbOccurences(it.getId(), _list,
			false);
		if (occ == 0 && _occ > 0)
		    occ = _occ;
		this.iterationInstanciation(_project, it, _ci, _list,
			occ, _isInstanciated, dispOrd);
	    } else if (bde instanceof Activity) {
		Activity act = (Activity) bde;
		int occ = this.giveNbOccurences(act.getId(), _list,
			false);
		if (occ == 0 && _occ > 0)
		    occ = _occ;
		this.activityService.activityInstanciation(_project,
			act, _ci, _list, occ, _isInstanciated, dispOrd);
	    } else if (bde instanceof TaskDescriptor) {
		TaskDescriptor td = (TaskDescriptor) bde;
		int occ = this.giveNbOccurences(td.getId(), _list,
			false);
		if (occ == 0 && _occ > 0)
		    occ = _occ;
		this.taskDescriptorService
			.taskDescriptorInstanciation(_project, td, _ci,
				occ, _isInstanciated, dispOrd);
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
    public void iterationUpdate(Project _project, Iteration _it,
	    Set<ConcreteActivity> _cacts, List<HashMap<String, Object>> _list,
	    int _occ) {

	// one concretephase at least to insert in all attached
	// concreteactivities of the parent of _phase
	if (_occ > 0) {
	    for (ConcreteActivity tmp : _cacts) {
		String strDispOrd = this.concreteActivityService
			.getMaxDisplayOrder(tmp);
		int dispOrd = Integer.parseInt(strDispOrd) + 1;
		this.iterationInstanciation(_project, _it, tmp, _list, _occ,
			true, dispOrd);

		if (tmp instanceof Project) {
		    Project pj = (Project) tmp;
		    this.projectDao.saveOrUpdateProject(pj);
		} else if (tmp instanceof ConcretePhase) {
			ConcretePhase cph = (ConcretePhase) tmp;
			this.concretePhaseDao.saveOrUpdateConcretePhase(cph);
		} else if (tmp instanceof ConcreteIteration) {
			    ConcreteIteration cit = (ConcreteIteration) tmp;
			    this.concreteIterationDao
				    .saveOrUpdateConcreteIteration(cit);	    
		}

	    }
	  
	} else {

	    // diving in all the concreteBreakdownElements to looking for update
	    Set<BreakdownElement> bdes = new HashSet<BreakdownElement>();
	    bdes.addAll(this.activityService.getAllBreakdownElements(_it));

	    Set<ConcreteActivity> cacts = new HashSet<ConcreteActivity>();
	    cacts.addAll(this
		    .getAllConcreteIterationsForAProject(_it, _project));

	    for (BreakdownElement bde : bdes) {
		if (bde instanceof Iteration) {
		    Iteration it = (Iteration) bde;
		    int occ = this.giveNbOccurences(it.getId(), _list, true);
		    this.iterationUpdate(_project, it, cacts, _list, occ);
		} else if (bde instanceof Activity) {
			Activity act = (Activity) bde;
			int occ = this.giveNbOccurences(act.getId(), _list,
				true);
			this.activityService.activityUpdate(_project, act,
				cacts, _list, occ);
		 } else if (bde instanceof TaskDescriptor) {
			    TaskDescriptor td = (TaskDescriptor) bde;
			    int occ = this.giveNbOccurences(td.getId(), _list,
				    true);
			    this.taskDescriptorService.taskDescriptorUpdate(
				    _project, td, cacts, occ);
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
    private int giveNbOccurences(String _id,
	    List<HashMap<String, Object>> list, boolean _isInstanciated) {

	int nb;
	if (!_isInstanciated)
	    nb = 1;
	else
	    nb = 0;

	for (HashMap<String, Object> hashMap : list) {
	    if (((String) hashMap.get("id")).equals(_id)) {
		nb = ((Integer) hashMap.get("nbOccurences")).intValue();
		break;
	    }
	}

	return nb;
    }

    /**
     * Getter of concreteIterationDao
     * 
     * @return the concreteIterationDao
     */
    public ConcreteIterationDao getConcreteIterationDao() {
	return concreteIterationDao;
    }

    /**
     * Setter of concreteIterationDao
     * 
     * @param concreteIterationDao
     *                the concreteIterationDao to set
     */
    public void setConcreteIterationDao(
	    ConcreteIterationDao concreteIterationDao) {
	this.concreteIterationDao = concreteIterationDao;
    }

    /**
     * 
     * Getter of BreakdownElementService
     * 
     * @return the breakdownElementService
     */
    public BreakdownElementService getBreakdownElementService() {
	return breakdownElementService;
    }

    /**
     * 
     * Setter of BreakdownElementService
     * 
     * @param breakdownElementService
     *                the breakdownElementService to set
     */
    public void setBreakdownElementService(
	    BreakdownElementService breakdownElementService) {
	this.breakdownElementService = breakdownElementService;
    }

    /**
     * Getter of ActivityService
     * 
     * @return the activityService
     */
    public ActivityService getActivityService() {
	return activityService;
    }

    /**
     * 
     * Setter of ActivityService
     * 
     * @param activityService
     *                the activityService to set
     */
    public void setActivityService(ActivityService activityService) {
	this.activityService = activityService;
    }

    /**
     * 
     * GEtter of ConcreteActivityService
     * 
     * @return the concreteActivityService
     */
    public ConcreteActivityService getConcreteActivityService() {
	return this.concreteActivityService;
    }

    /**
     * 
     * Setter of ConcreteActivityService
     * 
     * @param _concreteActivityService
     *                the concreteActivityService to set
     */
    public void setConcreteActivityService(
	    ConcreteActivityService _concreteActivityService) {
	this.concreteActivityService = _concreteActivityService;
    }

    /**
     * Getter of TaskDescriptorService
     * 
     * @return the taskDescriptorService
     */
    public TaskDescriptorService getTaskDescriptorService() {
	return taskDescriptorService;
    }

    /**
     * 
     * Setter of TaskDescriptorService
     * 
     * @param taskDescriptorService
     *                the taskDescriptorService to set
     */
    public void setTaskDescriptorService(
	    TaskDescriptorService taskDescriptorService) {
	this.taskDescriptorService = taskDescriptorService;
    }

    /**
     * 
     * Getter of IterationDao
     * 
     * @return the iterationDao
     */
    public IterationDao getIterationDao() {
	return iterationDao;
    }

    /**
     * 
     * Setter of IterationDao
     * 
     * @param iterationDao
     *                the iterationDao to set
     */
    public void setIterationDao(IterationDao iterationDao) {
	this.iterationDao = iterationDao;
    }

    /**
     * Getter of ConcretePhaseDao
     * 
     * @return the concretePhaseDao
     */
    public ConcretePhaseDao getConcretePhaseDao() {
	return concretePhaseDao;
    }

    /**
     * 
     * Setter of ConcretePhaseDao
     * 
     * @param concretePhaseDao
     *                the concretePhaseDao to set
     */
    public void setConcretePhaseDao(ConcretePhaseDao concretePhaseDao) {
	this.concretePhaseDao = concretePhaseDao;
    }

    /**
     * 
     * Getter of ProjectDao
     * 
     * @return the projectDao
     */
    public ProjectDao getProjectDao() {
	return projectDao;
    }

    /**
     * 
     * Setter of ProjectDao
     * 
     * @param projectDao
     *                the projectDao to set
     */
    public void setProjectDao(ProjectDao projectDao) {
	this.projectDao = projectDao;
    }

    /**
     * 
     * Getter of ConcreteWorkOrderService
     * 
     * @return the concreteWorkOrderService
     */
    public ConcreteWorkOrderService getConcreteWorkOrderService() {
	return this.concreteWorkOrderService;
    }

    /**
     * 
     * Setter of ConcreteWorkOrderService
     * 
     * @param _concreteWorkOrderService
     *            the concreteWorkOrderService to set
     */
    public void setConcreteWorkOrderService(
	    ConcreteWorkOrderService _concreteWorkOrderService) {
	this.concreteWorkOrderService = _concreteWorkOrderService;
    }


}
