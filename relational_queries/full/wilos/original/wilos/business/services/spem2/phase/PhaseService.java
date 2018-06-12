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
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class PhaseService {

    private ConcretePhaseDao concretePhaseDao;

    private PhaseDao phaseDao;

    private ProjectDao projectDao;

    private IterationService iterationService;

    private ActivityService activityService;

    private ConcreteActivityService concreteActivityService;

    private TaskDescriptorService taskDescriptorService;

    private ConcreteWorkOrderService concreteWorkOrderService;

    /**
     * 
     * Return all the concrete phases
     * 
     * @param _phase
     * @return Set<ConcretePhase>
     */
    public Set<ConcretePhase> getAllConcretePhases(Phase _phase) {
	Set<ConcretePhase> tmp = new HashSet<ConcretePhase>();
	this.phaseDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_phase);
	for (ConcretePhase bde : _phase.getConcretePhases()) {
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
    public Set<ConcretePhase> getAllConcretePhasesForAProject(Phase _phase,
	    Project _project) {
	Set<ConcretePhase> tmp = new HashSet<ConcretePhase>();
	this.phaseDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_phase);
	this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_project);
	for (ConcretePhase cph : _phase.getConcretePhases()) {
	    if (cph.getProject().getId().equals(_project.getId()))
		tmp.add(cph);
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
    private void peruseBreakdownElementList(Set<BreakdownElement> _bdes,
	    int _occ, boolean _isInstanciated, Project _project, Phase _phase,
	    List<HashMap<String, Object>> _list, ConcretePhase _cp
	    ) {
	int dispOrd = 0;
	for (BreakdownElement bde : _bdes) {
	    ++dispOrd;
	    if (bde instanceof Phase) {
		Phase ph = (Phase) bde;
		int occ = this.giveNbOccurences(ph.getId(), _list, false);
		/*if (occ == 0 && _occ > 0)
		    occ = _occ;*/
		this.phaseInstanciation(_project, ph, _project, _list, occ,
			_isInstanciated, dispOrd);
	    } else if (bde instanceof Iteration) {
		Iteration it = (Iteration) bde;
		int occ = this.giveNbOccurences(it.getId(), _list, false);
		/*if (occ == 0 && _occ > 0)
		    occ = _occ;*/
		this.iterationService.iterationInstanciation(_project, it, _cp,
			_list, occ, _isInstanciated, dispOrd);
	    } else if (bde instanceof Activity) {
		Activity act = (Activity) bde;
		int occ = this.giveNbOccurences(act.getId(), _list, false);
		/*if (occ == 0 && _occ > 0)
		    occ = _occ;*/
		this.activityService.activityInstanciation(_project, act, _cp,
			_list, occ, _isInstanciated, dispOrd);
	    } else if (bde instanceof TaskDescriptor) {
		TaskDescriptor td = (TaskDescriptor) bde;
		//System.out.println(td.getId());
		int occ = this.giveNbOccurences(td.getId(), _list, false);
		//System.out.println("task : " + td.getPresentationName() + " occ :" + occ + " _occ: " + _occ);
		/*if (occ == 0 && _occ > 0)
		    occ = _occ;*/
		this.taskDescriptorService.taskDescriptorInstanciation(
			_project, td, _cp, occ, _isInstanciated, dispOrd);

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
    public void phaseInstanciation(Project _project, Phase _phase,
	    ConcreteActivity _cact, List<HashMap<String, Object>> _list,
	    int _occ, boolean _isInstanciated, int _dispOrd) {

	// if one occurence at least
	if (_occ > 0) {
	    
	    this.concreteActivityService.getConcreteActivityDao()
		    .getSessionFactory().getCurrentSession()
		    .saveOrUpdate(_cact);
	    ArrayList<ConcretePhase> concretePhasesSisters = new ArrayList<ConcretePhase>();
	    int nbExistingConcretePhaseChildren = 0;
	    for (ConcreteBreakdownElement tmp : _cact
		    .getConcreteBreakdownElements()) {
		if (tmp instanceof ConcretePhase) {
		    if (((ConcretePhase) tmp).getPhase().getId().equals(
			    _phase.getId())) {
			nbExistingConcretePhaseChildren++;
			concretePhasesSisters.add((ConcretePhase) tmp);
		    }
		}
	    }
	    int nbConcretePhaseSisters = nbExistingConcretePhaseChildren;
	    for (int i = nbExistingConcretePhaseChildren + 1; i <= nbExistingConcretePhaseChildren
		    + _occ; i++) {

		ConcretePhase cp = new ConcretePhase();

		Set<BreakdownElement> bdes = new HashSet<BreakdownElement>();
		bdes.addAll(this.activityService
			.getAllBreakdownElements(_phase));

		if (_occ != 1 || nbExistingConcretePhaseChildren != 0) {
		    if (_phase.getPresentationName().equals(""))
			cp.setConcreteName(_phase.getName() + "#" + i);
		    else
			cp.setConcreteName(_phase.getPresentationName() + "#"
				+ i);
		} else {
		    if (_phase.getPresentationName().equals(""))
			cp.setConcreteName(_phase.getName());
		    else
			cp.setConcreteName(_phase.getPresentationName());
		}
		cp.addPhase(_phase);
		cp.setProject(_project);
		cp.setBreakdownElement(_phase);
		cp.setInstanciationOrder(i);
		cp.setWorkBreakdownElement(_phase);
		cp.setActivity(_phase);
		_cact.setConcreteBreakdownElements(this.concreteActivityService
			.getConcreteBreakdownElements(_cact));
		cp.addSuperConcreteActivity(_cact);
		cp.setDisplayOrder(cp.getSuperConcreteActivity()
			.getDisplayOrder()
			+ Integer.toString(_dispOrd + i));
	
		this.concretePhaseDao.saveOrUpdateConcretePhase(cp);

		// Instanciate elements of the list
		this.peruseBreakdownElementList(bdes, _occ, _isInstanciated,
			_project, _phase, _list, cp);		
		
		//Save the concrete phase
		this.concretePhaseDao.saveOrUpdateConcretePhase(cp);

		// if added ConcretePhase has sisters, we add a
		// FtS dependency
		// between it and its predecessor
		if (nbConcretePhaseSisters != 0) {
		    ConcretePhase lastConcretePhase = null;
		    for (ConcretePhase tmp : concretePhasesSisters) {
			if (lastConcretePhase == null
				|| tmp.getInstanciationOrder() > lastConcretePhase
					.getInstanciationOrder()) {
			    lastConcretePhase = tmp;
			}
		    }
		    this.concreteWorkOrderService.saveConcreteWorkOrder(
			    lastConcretePhase.getId(), cp.getId(),
			    Constantes.WorkOrderType.FINISH_TO_START, _project
				    .getId());
		}

		++nbConcretePhaseSisters;
		concretePhasesSisters.add(cp);
	    }
	}
    }

    private void UpdateElementOfBreakdownElementList( 
	    Set<BreakdownElement> _bdes, Project _project, Phase _phase,
	    Set<ConcreteActivity> _cacts, List<HashMap<String, Object>> _list)
    {
	 for (BreakdownElement bde : _bdes) {
		if (bde instanceof Phase) {
		    Phase ph = (Phase) bde;
		    int occ = this.giveNbOccurences(ph.getId(), _list, true);
		    this.phaseUpdate(_project, ph, _cacts, _list, occ);
		} else if (bde instanceof Iteration) {
		    Iteration it = (Iteration) bde;
		    int occ = this.giveNbOccurences(it.getId(), _list, true);
		    this.iterationService.iterationUpdate(_project, it, _cacts,
			    _list, occ);
		} else if (bde instanceof Activity) {
		    Activity act = (Activity) bde;
		    int occ = this.giveNbOccurences(act.getId(), _list, true);
		    this.activityService.activityUpdate(_project, act, _cacts,
			    _list, occ);
		} else if (bde instanceof TaskDescriptor) {
		    TaskDescriptor td = (TaskDescriptor) bde;
		    int occ = this.giveNbOccurences(td.getId(), _list, true);
		    this.taskDescriptorService.taskDescriptorUpdate(_project,
			    td, _cacts, occ);
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
    public void phaseUpdate(Project _project, Phase _phase,
	    Set<ConcreteActivity> _cacts, List<HashMap<String, Object>> _list,
	    int _occ) {

	// one concretephase at least to insert in all attached
	// concreteactivities of the parent of _phase
	if (_occ > 0) {
	    for (ConcreteActivity tmp : _cacts) {
		String strDispOrd = this.concreteActivityService
			.getMaxDisplayOrder(tmp);
		
		int dispOrd = Integer.parseInt(strDispOrd) + 1;
		this.phaseInstanciation(_project, _phase, tmp, _list, _occ,
			true, dispOrd);

		if (tmp instanceof Project) {
		    Project pj = (Project) tmp;
		    this.projectDao.saveOrUpdateProject(pj);
		    
		} else if (tmp instanceof ConcretePhase) {
		    ConcretePhase cph = (ConcretePhase) tmp;
		    this.concretePhaseDao.saveOrUpdateConcretePhase(cph);
		}
		
		
	    }
	} else {

	    // diving in all the concreteBreakdownElements to
	    // looking for update
	    Set<BreakdownElement> bdes = new HashSet<BreakdownElement>();
	    bdes.addAll(this.activityService.getAllBreakdownElements(_phase));

	    Set<ConcreteActivity> cacts = new HashSet<ConcreteActivity>();
	    cacts
		    .addAll(this.getAllConcretePhasesForAProject(_phase,
			    _project));

	    //Update element of the bdes list
	    this.UpdateElementOfBreakdownElementList(bdes, _project, 
		    _phase, cacts, _list);	 
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
    private int giveNbOccurences(String _id,
	    List<HashMap<String, Object>> list, boolean _isInstanciated) {

	int nb = 0;
	if (!_isInstanciated)
	    nb = 1;

	for (HashMap<String, Object> hashMap : list) {
	    if (((String) hashMap.get("id")).equals(_id)) {
		
		nb = ((Integer) hashMap.get("nbOccurences")).intValue();	
		break;
	    }
	}
	return nb;
    }

    /**
     * Getter of concretePhaseDao
     * 
     * @return the concretePhaseDao
     */
    public ConcretePhaseDao getConcretePhaseDao() {
	return concretePhaseDao;
    }

    /**
     * Setter of concretePhaseDao
     * 
     * @param concretePhaseDao
     *                the concretePhaseDao to set
     */
    public void setConcretePhaseDao(ConcretePhaseDao concretePhaseDao) {
	this.concretePhaseDao = concretePhaseDao;
    }

    /**
     * 
     * Getter of IterationService
     * 
     * @return the iterationService
     */
    public IterationService getIterationService() {
	return iterationService;
    }

    /**
     * 
     * Setter of IterationService
     * 
     * @param iterationService
     *                the iterationService to set
     */
    public void setIterationService(IterationService iterationService) {
	this.iterationService = iterationService;
    }

    /**
     * 
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
     * Getter of ConcreteActivityService
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
     * 
     * Getter of PhaseDao
     * 
     * @return the phaseDao
     */
    public PhaseDao getPhaseDao() {
	return phaseDao;
    }

    /**
     * 
     * Setter of PhaseDao
     * 
     * @param phaseDao
     *                the phaseDao to set
     */
    public void setPhaseDao(PhaseDao phaseDao) {
	this.phaseDao = phaseDao;
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
     *                the concreteWorkOrderService to set
     */
    public void setConcreteWorkOrderService(
	    ConcreteWorkOrderService _concreteWorkOrderService) {
	this.concreteWorkOrderService = _concreteWorkOrderService;
    }

}
