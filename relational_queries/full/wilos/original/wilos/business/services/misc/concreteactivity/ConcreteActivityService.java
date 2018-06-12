/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) Sebastien BALARD <sbalard@wilos-project.org> 
 * Copyright (C) 2007-2008 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
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

package wilos.business.services.misc.concreteactivity;

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

@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class ConcreteActivityService {

    private ConcreteActivityDao concreteActivityDao;

    private ConcreteMilestoneService concreteMilestoneService;

     private StateService stateService;

    /**
     *Allows to get the sorted set of concreteBreakdownElements with a concreteActivity  
     * @param _cact
     * @return the sorted set of concreteBreakdownElements
     */
    public SortedSet<ConcreteBreakdownElement> getConcreteBreakdownElements(
	    ConcreteActivity _cact) {
	SortedSet<ConcreteBreakdownElement> tmp = new TreeSet<ConcreteBreakdownElement>();
	this.concreteActivityDao.getSessionFactory().getCurrentSession()
		.saveOrUpdate(_cact);
	for (ConcreteBreakdownElement cbde : _cact
		.getConcreteBreakdownElements()) {
	    tmp.add(cbde);
	}
	return tmp;
    }

    /**
     * Allows to save the concrete activity which passed in parameters
     * 
     * @param _concreteActivity
     */
    public void saveConcreteActivity(ConcreteActivity _concreteActivity) {
	this.concreteActivityDao
		.saveOrUpdateConcreteActivity(_concreteActivity);
    }

    /**
     * Allows to get the concrete activity which has the same id than the
     * parameter
     * 
     * @param _concreteActivityId
     *                the id of the concreteActivity asked
     * @return the ConcreteActivity which has the same id
     */
    public boolean existsConcreteActivity(String _concreteActivityId) {
	return this.concreteActivityDao
		.existsConcreteActivity(_concreteActivityId);
    }

    /**
     * Allows to get the concrete activity which has the same id than the
     * parameter
     * 
     * @param _concreteActivityId
     *                the id of the concreteActivity asked
     * @return the ConcreteActivity which has the same id
     */
    public ConcreteActivity getConcreteActivity(String _concreteActivityId) {
	return this.concreteActivityDao
		.getConcreteActivity(_concreteActivityId);
    }

    /**
     * Return the list of all the Concrete Activities
     * 
     * @return the list of all the concreteActivities
     */
    public List<ConcreteActivity> getAllConcreteActivities() {
	return this.concreteActivityDao.getAllConcreteActivities();
    }

    /* Getters & Setters */

    /**
     * Return the concreteActivityDao
     * 
     * @return the concreteActivityDao
     */
    public ConcreteActivityDao getConcreteActivityDao() {
	return concreteActivityDao;
    }

    /**
     * Initialize the concreteActivityDao with the value in parameter
     * 
     * @param concreteActivityDao
     *                the concreteActivityDao to set
     */
    public void setConcreteActivityDao(ConcreteActivityDao concreteActivityDao) {
	this.concreteActivityDao = concreteActivityDao;
    }

    /**
 * Allows to get the set of concreteActivities from a project
     * @param _cact
     * @return the set of concreteActivities
     */
    public Set<ConcreteActivity> getConcreteActivitiesFromProject(
	    ConcreteActivity _cact) {
	Set<ConcreteActivity> tmp = new HashSet<ConcreteActivity>();

	this.concreteActivityDao.getSessionFactory().getCurrentSession()
		.saveOrUpdate(_cact);
	for (ConcreteActivity cact : this.getAllConcreteActivities()) {
	    if ((cact.getProject() != null)
		    && (cact.getProject().equals(_cact))) {
		tmp.add(cact);
	    }
	}
	return tmp;
    }

    /**
     * Allows to get the set of concreteActivities
     * @return the set of concreteActivities
     */
    public ConcreteMilestoneService getConcreteMilestoneService() {
	return concreteMilestoneService;
    }

    /**
     * Allows to set the concreteMilestoneService
     * @param _concreteMilestoneService
     */
    public void setConcreteMilestoneService(
	    ConcreteMilestoneService _concreteMilestoneService) {
	concreteMilestoneService = _concreteMilestoneService;
    }

    /**
     *Allows to get the service's state
     * @return the stateService
     */
    public StateService getStateService() {
        return this.stateService;
    }

    /**
     * Allows to set the service's state
     * @param _stateService 
     */
    public void setStateService(StateService _stateService) {
        this.stateService = _stateService;
    }

    /**
     * Allows to get the maxDisplayOrder
     * @param _cact
     * @return the maxDisplayOrder
     */
	public String getMaxDisplayOrder(ConcreteActivity _cact) {
		return this.concreteActivityDao.getMaxDisplayOrder(_cact);
	}
}
