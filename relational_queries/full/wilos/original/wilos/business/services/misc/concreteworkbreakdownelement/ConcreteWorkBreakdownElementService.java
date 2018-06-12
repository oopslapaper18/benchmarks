/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2007 Sebastien BALARD <sbalard@wilos-project.org>
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
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class ConcreteWorkBreakdownElementService {

	private ConcreteWorkBreakdownElementDao concreteWorkBreakdownElementDao;

	private WorkBreakdownElementService workBreakdownElementService;

	private ConcreteWorkOrderService concreteWorkOrderService;

	private ProjectDao projectDao;

	/**
	 * Get the ConcreteWorkBreakdownElements list having at least one successor
	 * 
	 * @return List<ConcreteWorkBreakdownElement>
	 */
	public List<ConcreteWorkBreakdownElement> getAllConcreteWorkBreakdownElementsWithAtLeastOneSuccessor(
			Project _project) {

		this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
				_project);

		List<ConcreteWorkBreakdownElement> tmp = new ArrayList<ConcreteWorkBreakdownElement>();

		for (ConcreteWorkBreakdownElement cwbde : this.concreteWorkBreakdownElementDao
				.getAllConcreteWorkBreakdownElements()) {
			WorkBreakdownElement wbde = cwbde.getWorkBreakdownElement();
			if (!(cwbde instanceof Project) && (wbde != null)) {
				String id = cwbde.getProject().getId();
				if (id.equals(_project.getId())
						&& wbde.getSuccessors().size() != 0) {
					tmp.add(cwbde);
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
	public List<ConcreteWorkBreakdownElement> getAllConcreteWorkBreakdownElements(
			Project _project) {
		this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
				_project);

		List<ConcreteWorkBreakdownElement> tmp = new ArrayList<ConcreteWorkBreakdownElement>();

		for (ConcreteWorkBreakdownElement cwbde : this.concreteWorkBreakdownElementDao
				.getAllConcreteWorkBreakdownElements()) {
			WorkBreakdownElement wbde = cwbde.getWorkBreakdownElement();
			if (!(cwbde instanceof Project) && (wbde != null)) {
				String id = cwbde.getProject().getId();
				if (id.equals(_project.getId())) {
					tmp.add(cwbde);
				}
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
	public List<ConcreteWorkBreakdownElement> getSuperConcreteActivitiesFromConcreteWorkBreakdownElement(
			ConcreteWorkBreakdownElement _cwbde) {

		this.concreteWorkBreakdownElementDao.getSessionFactory()
				.getCurrentSession().saveOrUpdate(_cwbde);

		List<ConcreteWorkBreakdownElement> tmp = new ArrayList<ConcreteWorkBreakdownElement>();

		for (ConcreteWorkBreakdownElement cwbde : _cwbde
				.getSuperConcreteActivities()) {
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
	public boolean isInstanciablePredecessor(ConcreteWorkBreakdownElement _cwbde) {

		boolean instanciable = false;

		this.concreteWorkBreakdownElementDao.getSessionFactory()
				.getCurrentSession().saveOrUpdate(_cwbde);

		// getting of spem predecessor
		WorkBreakdownElement pred = _cwbde.getWorkBreakdownElement();
		// for each spem successor
		int s = 0;
		for (WorkOrder wo : pred.getSuccessors()) {
			WorkBreakdownElement succ = wo.getSuccessor();
			// if ConcreteSuccessors number is less inferior than succesor
			// ConcreteWbdes number
			int p = _cwbde.getConcreteSuccessors().size();
			s += this.workBreakdownElementService
					.getAllConcreteWorkBreakdownElementsFromWorkBreakdownElement(
							succ).size();
			if (p < s) {
				instanciable = true;
			}
		}
		return instanciable;
	}

	/**
	 * Allows to get a concreteWorkBrreakdownElement with its id
	 * 
	 * @param _id
	 * @return the concreteWorkBrreakdownElement
	 */
	public ConcreteWorkBreakdownElement getConcreteWorkBreakdownElement(
			String _id) {
		return this.concreteWorkBreakdownElementDao
				.getConcreteWorkBreakdownElement(_id);
	}

	/**
	 * Allows to get the concreteWorkBreakdownElementDao
	 * 
	 * @return the concreteWorkBreakdownElementDao
	 */
	public ConcreteWorkBreakdownElementDao getConcreteWorkBreakdownElementDao() {
		return this.concreteWorkBreakdownElementDao;
	}

	/**
	 * Allows to set the concreteWorkBreakdownElementDao
	 * 
	 * @param _concreteWorkBreakdownElementDao
	 */
	public void setConcreteWorkBreakdownElementDao(
			ConcreteWorkBreakdownElementDao _concreteWorkBreakdownElementDao) {
		this.concreteWorkBreakdownElementDao = _concreteWorkBreakdownElementDao;
	}

	/**
	 * Allows to get the projectDao
	 * 
	 * @return the projectDao
	 */
	public ProjectDao getProjectDao() {
		return this.projectDao;
	}

	/**
	 * Allows to set the projectDao
	 * 
	 * @param _projectDao
	 */
	public void setProjectDao(ProjectDao _projectDao) {
		this.projectDao = _projectDao;
	}

	/**
	 * Allows to get the workBreakdownElementService
	 * 
	 * @return the workBreakdownElementService
	 */
	public WorkBreakdownElementService getWorkBreakdownElementService() {
		return this.workBreakdownElementService;
	}

	/**
	 * Allows to set the workBreakdownElementService
	 * 
	 * @param _workBreakdownElementService
	 */
	public void setWorkBreakdownElementService(
			WorkBreakdownElementService _workBreakdownElementService) {
		this.workBreakdownElementService = _workBreakdownElementService;
	}

	/**
	 * Allows to get the concreteWorkOrderService
	 * 
	 * @return the concreteWorkOrderService
	 */
	public ConcreteWorkOrderService getConcreteWorkOrderService() {
		return this.concreteWorkOrderService;
	}

	/**
	 * Allows to set the concreteWorkOrderService
	 * 
	 * @param _concreteWorkOrderService
	 * 
	 */
	public void setConcreteWorkOrderService(
			ConcreteWorkOrderService _concreteWorkOrderService) {
		this.concreteWorkOrderService = _concreteWorkOrderService;
	}

	/**
	 * Allows to get the list of concretePredecessor by hashMap
	 * 
	 * @param _cwbde
	 * @return the list of concretePredecessor by hashMap
	 */
	public List<HashMap<String, Object>> getConcretePredecessorHashMap(
			ConcreteWorkBreakdownElement _cwbde) {
		List<HashMap<String, Object>> predecessorHashMap = new ArrayList<HashMap<String, Object>>();
		if (_cwbde != null) {
			_cwbde = this.getConcreteWorkBreakdownElement(_cwbde.getId());
			if (_cwbde != null) {
				for (ConcreteWorkOrder cwo : _cwbde.getConcretePredecessors()) {

					HashMap<String, Object> hm = new HashMap<String, Object>();
					ConcreteWorkBreakdownElement cPred = this
							.getConcreteWorkBreakdownElementDao()
							.getConcreteWorkBreakdownElement(
									cwo.getConcreteWorkOrderId()
											.getConcretePredecessorId());

					hm.put("pred", cPred.getConcreteName());
					hm.put("plannedStartingDate", cPred
							.getPlannedStartingDate());
					hm.put("plannedFinishingDate", cPred
							.getPlannedFinishingDate());
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
	public List<HashMap<String, Object>> getConcreteSuccessorHashMap(
			ConcreteWorkBreakdownElement _cwbde) {

		List<HashMap<String, Object>> successorHashMap = new ArrayList<HashMap<String, Object>>();
		if (_cwbde != null) {
			_cwbde = this.getConcreteWorkBreakdownElement(_cwbde.getId());
			if (_cwbde != null) {
				for (ConcreteWorkOrder cwo : _cwbde.getConcreteSuccessors()) {

					HashMap<String, Object> hm = new HashMap<String, Object>();
					ConcreteWorkBreakdownElement cSucc = this
							.getConcreteWorkBreakdownElementDao()
							.getConcreteWorkBreakdownElement(
									cwo.getConcreteWorkOrderId()
											.getConcreteSuccessorId());

					hm.put("succ", cSucc.getConcreteName());
					hm.put("plannedStartingDate", cSucc
							.getPlannedStartingDate());
					hm.put("plannedFinishingDate", cSucc
							.getPlannedFinishingDate());
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
	public Project getProject(String _id) {
		return this.projectDao.getProject(_id);
	}
}
