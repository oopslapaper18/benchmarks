/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) 2007 Sebastien BALARD <sbalard@wilos-project.org>
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
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class ActivityService {

    private ActivityDao activityDao;

    private ConcretePhaseDao concretePhaseDao;

    private ConcreteIterationDao concreteIterationDao;

    private ProjectDao projectDao;

    private ConcreteActivityService concreteActivityService;

    private TaskDescriptorService taskDescriptorService;

    private ConcreteWorkOrderService concreteWorkOrderService;

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
    private void peruseConcreteActivitiesChildren(
	    int _nbExistingConcreteActivitiesChildren, int _occ,
	    Project _project, Activity _activity, int _dispOrd,
	    ConcreteActivity _cact, List<HashMap<String, Object>> _list,
	    ArrayList<ConcreteActivity> _concreteActivitiesSisters) {

	int nbConcreteActivitiesSisters = _nbExistingConcreteActivitiesChildren;
	for (int i = _nbExistingConcreteActivitiesChildren + 1; i <= _nbExistingConcreteActivitiesChildren
		+ _occ; i++) {

	    ConcreteActivity cact = new ConcreteActivity();

	    List<BreakdownElement> bdes = new ArrayList<BreakdownElement>();
	    bdes.addAll(this.getAllBreakdownElements(_activity));

	    if (_occ != 1 || _nbExistingConcreteActivitiesChildren != 0) {
		if (_activity.getPresentationName().equals(""))
		    cact.setConcreteName(_activity.getName() + "#" + i);
		else
		    cact.setConcreteName(_activity.getPresentationName() + "#"
			    + i);
	    } else {
		if (_activity.getPresentationName().equals(""))
		    cact.setConcreteName(_activity.getName());
		else
		    cact.setConcreteName(_activity.getPresentationName());
	    }
	    cact.addActivity(_activity);
	    cact.setProject(_project);
	    cact.setBreakdownElement(_activity);
	    cact.setInstanciationOrder(i);
	    cact.setWorkBreakdownElement(_activity);
	    cact.setActivity(_activity);
	    _cact.setConcreteBreakdownElements(this.concreteActivityService
		    .getConcreteBreakdownElements(_cact));
	    cact.addSuperConcreteActivity(_cact);
	    cact.setDisplayOrder(cact.getSuperConcreteActivity()
		    .getDisplayOrder()
		    + Integer.toString(_dispOrd + i));
	  
	    this.concreteActivityService.saveConcreteActivity(cact);

	    int dispOrd = 0;
	    for (BreakdownElement bde : bdes) {
		dispOrd++;
		if (bde instanceof Activity) {
		    Activity act = (Activity) bde;
		    int occ = this.giveNbOccurences(act.getId(), _list, false);
		    if (occ == 0 && _occ > 0)
			occ = _occ;
		    
		    this.activityInstanciation(_project, act, cact, _list, occ,
			    false, dispOrd);
		} else if (bde instanceof TaskDescriptor) {
		    TaskDescriptor td = (TaskDescriptor) bde;
		    int occ = this.giveNbOccurences(td.getId(), _list, false);
		    if (occ == 0 && _occ > 0)
			occ = _occ;
		    this.taskDescriptorService.taskDescriptorInstanciation(
			    _project, td, cact, occ, false, dispOrd);
		}
	    }

	    this.concreteActivityService.saveConcreteActivity(cact);

	    // if added ConcreteActivity has sisters, we add a FtS
	    // dependency between it and its predecessor
	    if (nbConcreteActivitiesSisters != 0) {
		ConcreteActivity lastConcreteActivity = null;
		for (ConcreteActivity tmp : _concreteActivitiesSisters) {
		    if (lastConcreteActivity == null
			    || tmp.getInstanciationOrder() > lastConcreteActivity
				    .getInstanciationOrder()) {
			lastConcreteActivity = tmp;
		    }
		}
		this.concreteWorkOrderService.saveConcreteWorkOrder(
			lastConcreteActivity.getId(), cact.getId(),
			Constantes.WorkOrderType.FINISH_TO_START, _project
				.getId());
	    }

	    nbConcreteActivitiesSisters++;
	    _concreteActivitiesSisters.add(cact);
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
    public void activityInstanciation(Project _project, Activity _activity,
	    ConcreteActivity _cact, List<HashMap<String, Object>> _list,
	    int _occ, boolean _isInstanciated, int _dispOrd) {

	if (_occ > 0) {
	   
	    this.concreteActivityService.getConcreteActivityDao()
		    .getSessionFactory().getCurrentSession()
		    .saveOrUpdate(_cact);
	    ArrayList<ConcreteActivity> concreteActivitiesSisters = new ArrayList<ConcreteActivity>();
	    int nbExistingConcreteActivitiesChildren = 0;
	    for (ConcreteBreakdownElement tmp : _cact
		    .getConcreteBreakdownElements()) {
		if (tmp instanceof ConcreteActivity) {
		    if (((ConcreteActivity) tmp).getActivity().getId().equals(
			    _activity.getId())) {
			nbExistingConcreteActivitiesChildren++;
			concreteActivitiesSisters.add((ConcreteActivity) tmp);
		    }
		}
	    }

	    // Peruse all concrete activities children
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
    public void activityUpdate(Project _project, Activity _act,
	    Set<ConcreteActivity> _cacts, List<HashMap<String, Object>> _list,
	    int _occ) {

	// one concretephase at least to insert in all attached
	// concreteactivities of the parent of _phase
	if (_occ > 0) {
	    for (ConcreteActivity tmp : _cacts) {
		String strDispOrd = this.concreteActivityService
			.getMaxDisplayOrder(tmp);
		int dispOrd = Integer.parseInt(strDispOrd) + 1;
		this.activityInstanciation(_project, _act, tmp, _list, _occ,
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
		} else if (tmp instanceof ConcreteActivity) {
		    ConcreteActivity cact = (ConcreteActivity) tmp;
		    this.concreteActivityService.saveConcreteActivity(cact);
		}

		this.concreteActivityService.saveConcreteActivity(tmp);

	    }
	} else {

	    // diving in all the concreteBreakdownElements to looking for update
	    Set<BreakdownElement> bdes = new HashSet<BreakdownElement>();
	    bdes.addAll(this.getAllBreakdownElements(_act));

	    Set<ConcreteActivity> cacts = new HashSet<ConcreteActivity>();
	    cacts.addAll(this.getAllConcreteActivitiesForAProject(_act,
		    _project));

	    for (BreakdownElement bde : bdes) {
		if (bde instanceof Activity) {
		    Activity act = (Activity) bde;
		    int occ = this.giveNbOccurences(act.getId(), _list, true);
		    this.activityUpdate(_project, act, cacts, _list, occ);
		} else if (bde instanceof TaskDescriptor) {
		    TaskDescriptor td = (TaskDescriptor) bde;
		    int occ = this.giveNbOccurences(td.getId(), _list, true);
		    this.taskDescriptorService.taskDescriptorUpdate(_project,
			    td, cacts, occ);
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
     * 
     * return all the concrete activities
     * 
     * @param _act
     * @return Set<ConcreteActivity>
     */
    public Set<ConcreteActivity> getAllConcreteActivities(Activity _act) {
	Set<ConcreteActivity> tmp = new HashSet<ConcreteActivity>();
	this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_act);
	for (ConcreteActivity bde : _act.getConcreteActivities()) {
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
    public Set<ConcreteActivity> getAllConcreteActivitiesForAProject(
	    Activity _act, Project _project) {
	Set<ConcreteActivity> tmp = new HashSet<ConcreteActivity>();
	this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_act);

	for (ConcreteActivity cact : _act.getConcreteActivities()) {
	    if (cact.getProject().getId().equals(_project.getId()))
		tmp.add(cact);
	}
	return tmp;
    }

    /**
     * Get the breakdownElements collection of an activity
     * 
     * @param _act
     * @return
     */
    public SortedSet<BreakdownElement> getAllBreakdownElements(Activity _act) {

	SortedSet<BreakdownElement> tmp = new TreeSet<BreakdownElement>();

	this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_act);
	for (BreakdownElement bde : _act.getBreakdownElements()) {
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
    public Set<Guidance> getAllGuidances(Activity _act) {
	this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_act);
	Set<Guidance> tmp = new HashSet<Guidance>();
	for (Guidance g : _act.getGuidances()) {
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
    public List<ConcreteActivity> getConcreteActivityFromActivity(Activity _act) {
	this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_act);
	List<ConcreteActivity> tmp = new ArrayList<ConcreteActivity>();
	for (ConcreteActivity ca : _act.getConcreteActivities()) {
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
    public List<ConcreteActivity> getConcreteActivitiesFromActivityAndForAProject(
	    Activity _act, Project _project) {
	this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
		_act);

	List<ConcreteActivity> tmp = new ArrayList<ConcreteActivity>();
	for (ConcreteActivity cact : _act.getConcreteActivities()) {
	    if (cact.getProject().getId().equals(_project.getId()))
		tmp.add(cact);
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
    public Activity getActivity(String _id) {
	return this.activityDao.getActivity(_id);
    }

    /**
     * Return activities list
     * 
     * @return List of activities
     */
    public List<Activity> getAllActivities() {
	return this.activityDao.getAllActivities();
    }

    /**
     * 
     * return one activity from one guide
     * 
     * @param _guid
     * @return Activity
     */
    public Activity getActivityFromGuid(String _guid) {
	return this.activityDao.getActivityFromGuid(_guid);
    }

    /**
     * Save activity
     * 
     * @param _activity
     *                the activity to save
     */
    public String saveActivity(Activity _activity) {
	return this.activityDao.saveOrUpdateActivity(_activity);
    }

    /**
     * Delete an anctivity
     * 
     * @param _activity
     */
    public void deleteActivity(Activity _activity) {
	this.activityDao.deleteActivity(_activity);
    }

    /**
     * Getter of activityDao.
     * 
     * @return the activityDao.
     */
    public ActivityDao getActivityDao() {
	return this.activityDao;
    }

    /**
     * Setter of activityDao.
     * 
     * @param _activityDao
     *                The activityDao to set.
     */
    public void setActivityDao(ActivityDao _activityDao) {
	this.activityDao = _activityDao;
    }

    /**
     * 
     * Getter : return the concreteIterationDao
     * 
     * @return the concreteIterationDao
     */
    public ConcreteIterationDao getConcreteIterationDao() {
	return concreteIterationDao;
    }

    /**
     * 
     * Setter of ConcreteIterationDao
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
     * getter of ConcretePhaseDao
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
