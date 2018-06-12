/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) 2007 Mathieu BENOIT <mathieu-benoit@hotmail.fr>
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

package wilos.business.services.misc.wilosuser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService;
import wilos.business.services.misc.concreteworkproduct.ConcreteWorkProductDescriptorService;
import wilos.business.services.misc.project.ProjectService;
import wilos.business.webservices.transfertobject.ParticipantTO;
import wilos.hibernate.misc.wilosuser.ParticipantDao;
import wilos.model.misc.concreterole.ConcreteRoleDescriptor;
import wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor;
import wilos.model.misc.project.Project;
import wilos.model.misc.wilosuser.Participant;
import wilos.utils.Security;

/**
 * The services associated to the Participant
 * 
 */
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class ParticipantService {

	private ParticipantDao participantDao;

	private ProjectService projectService;

	private ConcreteRoleDescriptorService concreteRoleDescriptorService;

	private ConcreteWorkProductDescriptorService concreteWorkProductDescriptorService;

	/**
	 * Allows to get the concreteRoleDescriptorService
	 * 
	 * @return the concreteRoleDescriptorService
	 */
	public ConcreteRoleDescriptorService getConcreteRoleDescriptorService() {
		return this.concreteRoleDescriptorService;
	}

	/**
	 * Allows to set the concreteRoleDescriptorService
	 * 
	 * @param _concreteRoleDescriptorService
	 */
	public void setConcreteRoleDescriptorService(
			ConcreteRoleDescriptorService _concreteRoleDescriptorService) {
		this.concreteRoleDescriptorService = _concreteRoleDescriptorService;
	}

	/**
	 * Allows to get the participantDao
	 * 
	 * @return the ParticipantDao
	 */
	public ParticipantDao getParticipantDao() {
		return this.participantDao;
	}

	/**
	 * Allows to set the participantDao
	 * 
	 * @param _participantDao
	 */
	public void setParticipantDao(ParticipantDao _participantDao) {
		this.participantDao = _participantDao;
	}

	/**
	 * Allows to get the projectService
	 * 
	 * @return the projectService
	 */
	public ProjectService getProjectService() {
		return this.projectService;
	}

	/**
	 * Allows to get the projectService
	 * 
	 * @param _projectService
	 * 
	 */
	public void setProjectService(ProjectService _projectService) {
		this.projectService = _projectService;
	}

	/**
	 * Allows to get the set of concreteRoleDescriptor for a participant and a
	 * project
	 * 
	 * @return the set of concreteRoleDescriptor
	 */
	public Set<ConcreteRoleDescriptor> getConcreteRoleDescriptorsForAParticipantAndForAProject(
			String _participantId, String _projectId) {

		Set<ConcreteRoleDescriptor> concreteRolesList = new HashSet<ConcreteRoleDescriptor>();
		Set<ConcreteRoleDescriptor> participantConcreteRolesList = this
				.getParticipant(_participantId).getConcreteRoleDescriptors();
		List<ConcreteRoleDescriptor> projectConcreteRolesList = this.concreteRoleDescriptorService
				.getAllConcreteRoleDescriptorsForProject(_projectId);
		if (projectConcreteRolesList != null) {

			for (ConcreteRoleDescriptor projectConcreteRole : projectConcreteRolesList) {
				for (ConcreteRoleDescriptor concreteRoleDescriptor : participantConcreteRolesList) {
					if (projectConcreteRole.getId().equals(
							concreteRoleDescriptor.getId()))
						concreteRolesList.add(concreteRoleDescriptor);
				}
			}
		}
		return concreteRolesList;
	}
	
	/**
	 * Allows to get the set of concreteRoleDescriptor for a participant and a
	 * project
	 * 
	 * @return the set of concreteRoleDescriptor
	 */
	public Set<String> getNameConcreteRoleDescriptorsForAParticipantAndForAProject(
			String _participantId, String _projectId) {

		Set<String> concreteRolesList = new HashSet<String>();
		Set<ConcreteRoleDescriptor> participantConcreteRolesList = this
				.getParticipant(_participantId).getConcreteRoleDescriptors();
		List<ConcreteRoleDescriptor> projectConcreteRolesList = this.concreteRoleDescriptorService
				.getAllConcreteRoleDescriptorsForProject(_projectId);
		if (projectConcreteRolesList != null) {

			for (ConcreteRoleDescriptor projectConcreteRole : projectConcreteRolesList) {
				for (ConcreteRoleDescriptor concreteRoleDescriptor : participantConcreteRolesList) {
					if (projectConcreteRole.getId().equals(
							concreteRoleDescriptor.getId()))
					    if(!concreteRoleDescriptor.getRoleDescriptor().getPresentationName().equals(""))
						concreteRolesList.add(concreteRoleDescriptor.getRoleDescriptor().getName());
				}
			}
		}
		return concreteRolesList;
	}

	/**
	 * Allows to get the list of participants
	 * 
	 * @return the list of participants
	 */
	public List<Participant> getParticipants() {
		return this.participantDao.getAllParticipants();
	}

	/**
	 * Allows to get the participant with its id
	 * 
	 * @return the participant
	 */
	public Participant getParticipant(String _id) {
		return this.participantDao.getParticipantById(_id);
	}

	/**
	 * Allows to save the participant
	 * 
	 * @param _participant
	 */
	public void saveParticipant(Participant _participant) {
		_participant.setPassword(Security.encode(_participant.getPassword()));
		participantDao.saveOrUpdateParticipant(_participant);
	}

	/**
	 * Allows to save the participant without encryption of the password
	 * 
	 * @param _participant
	 */
	public void saveParticipantWithoutEncryption(Participant _participant) {
		if( _participant.getNewPassword() != null && !_participant.getNewPassword().trim().equalsIgnoreCase("")){
			_participant.setPassword(Security.encode(_participant
					.getNewPassword()));
			}
		participantDao.saveOrUpdateParticipant(_participant);
	}

	/**
	 * Allows to delete a participant
	 * 
	 * @param participantId
	 */
	public void deleteParticipant(String participantId) {
		Participant participant = this.getParticipant(participantId);
		if (participant != null){

			this.participantDao.deleteParticipant(participant);

			
		}
		
	}

	/**
	 * 
	 * Allows to get the list of projects for a participant
	 * 
	 * @param participant
	 * @return list of projects where the participant is affected to
	 */
	public HashMap<Project, Boolean> getProjectsForAParticipant(
			Participant _participant) {

		this.participantDao.getSessionFactory().getCurrentSession()
				.saveOrUpdate(_participant);

		HashMap<Project, Boolean> affectedProjectList = new HashMap<Project, Boolean>();
		HashSet<Project> allProjectList = new HashSet<Project>();
		// Participant loadedParticipant = new Participant();

		// chargement du participant et des projets
		// String login = loadedParticipant.getLogin();
		// loadedParticipant = this.participantDao.getParticipant(login);
		allProjectList = (HashSet<Project>) this.projectService
				.getUnfinishedProjects();

		for (Project p : allProjectList) {
			Set<Project> tmp = _participant.getAffectedProjectList();
			if (tmp.contains(p)) {
				affectedProjectList.put(p, true);
			} else {
				affectedProjectList.put(p, false);
			}
		}
		return affectedProjectList;
	}

	/**
	 * 
	 * Allows to get the list of project where a participant is affected to
	 * 
	 * @param participant
	 * @return list of project where the participant is affected to
	 */
	public List<Project> getAllAffectedProjectsForParticipant(
			Participant participant) {
		List<Project> affectedProjectList = new ArrayList<Project>();
		HashSet<Project> allProjectList = new HashSet<Project>();
		Participant chargedParticipant = new Participant();

		// chargement du participant et des projets
		String login = participant.getLogin();
		chargedParticipant = this.participantDao.getParticipant(login);
		allProjectList = (HashSet<Project>) this.projectService
				.getUnfinishedProjects();

		for (Project p : allProjectList) {
			if (chargedParticipant.getAffectedProjectList().contains(p)) {
				affectedProjectList.add(p);
			}
		}
		return affectedProjectList;
	}

	/**
	 * Allows to save projects for a participant
	 * 
	 * @param participant
	 * @param affectedProjects
	 */
	public void saveProjectsForAParticipant(Participant participant,
			Map<String, Boolean> affectedProjects) {
		Participant currentParticipant = this.getParticipantDao()
				.getParticipant(participant.getLogin());
		Project currentProject;

		// for every project
		for (String projectId : affectedProjects.keySet()) {

			currentProject = this.projectService.getProject(projectId);

			// if this is an affectation
			if (Boolean.valueOf(affectedProjects.get(projectId)) == true) {
				currentParticipant.addAffectedProject(currentProject);
			}
			// if this is an unaffectation
			else {
				// removing the participant from the project
				currentParticipant.removeAffectedProject(currentProject);

				// if the project have a project manager
				if (currentProject.getProjectManager() != null) {
					// if the project manager is the current participant
					if (currentProject.getProjectManager().getId().equals(
							currentParticipant.getId())) {
						currentParticipant.removeManagedProject(currentProject);
						this.projectService.saveProject(currentProject);
					}
				}
			}
		}
		this.participantDao.saveOrUpdateParticipant(currentParticipant);
	}

	/**
	 * Allows to save a project for a projectManager
	 * 
	 * @param _participantId
	 * @param _projectId
	 * @param _isForAssignment
	 */
	public void saveProjectForAProjectManager(String _participantId,
			String _projectId, boolean _isForAssignment) {
		Participant currentParticipant = this.getParticipantDao()
				.getParticipantById(_participantId);
		Project currentProject = this.projectService.getProject(_projectId);
		if (_isForAssignment) {
			currentParticipant.addManagedProject(currentProject);
		} else {
			// removing the participant from the project.
			currentParticipant.removeManagedProject(currentProject);
		}
		this.participantDao.saveOrUpdateParticipant(currentParticipant);
	}

	/**
	 * 
	 * Allows to get the list of projects without project manager for a
	 * participant
	 * 
	 * @param participant
	 * @return HashMap with couples of this form : (Project,ProjectManager) or
	 *         (Project,null)
	 */
	public HashMap<Project, Participant> getManageableProjectsForAParticipant(
			Participant participant) {
		HashMap<Project, Boolean> affectedProjectList = new HashMap<Project, Boolean>();
		HashMap<Project, Participant> manageableProjectList = new HashMap<Project, Participant>();

		// chargement des projets
		affectedProjectList = this.getProjectsForAParticipant(participant);

		// for every project
		for (Project project : affectedProjectList.keySet()) {
			// if the project is affected to the participant
			if (affectedProjectList.get(project).booleanValue() == true) {
				// if there is no projectManager -> the project is manageable
				if (project.getProjectManager() == null) {
					manageableProjectList.put(project, null);
				}
				// if there is a projectManager -> the project is not manageable
				else {
					manageableProjectList.put(project, project
							.getProjectManager());
				}
			}
		}
		return manageableProjectList;
	}

	/**
	 * Allows to save managed projects for a participant
	 * 
	 * @param participant
	 * @param managedProjects
	 */
	public void saveManagedProjectsForAParticipant(Participant participant,
			Map<String, Boolean> managedProjects) {
		Participant currentParticipant = this.getParticipantDao()
				.getParticipant(participant.getLogin());
		Project currentProject;
		for (String projectId : managedProjects.keySet()) {
			// loading the current project from database
			currentProject = this.projectService.getProject(projectId);
			if ((Boolean) managedProjects.get(projectId)){
				currentParticipant.addManagedProject(currentProject);
			}else
				currentParticipant.removeManagedProject(currentProject);
		}
		
		this.participantDao.saveOrUpdateParticipant(currentParticipant);
	}

	/**
	 * Allows to get the participantTO with the login
	 * 
	 * @param _login
	 * @return the participantTO
	 */
	public ParticipantTO getParticipantTO(String _login) {
		return new ParticipantTO(participantDao.getParticipant(_login));
	}

	/**
	 * Allows to get a participant's login
	 * 
	 * @param participant
	 * @return the participant's login
	 */
	public String getParticipantLogin(Participant participant) {
		this.participantDao.getSessionFactory().getCurrentSession()
				.saveOrUpdate(participant);
		return participant.getLogin();
	}

	/**
	 * Allows to get the concreteWorkProductDescriptorService
	 * 
	 * @return the concreteWorkProductDescriptorService
	 */
	public ConcreteWorkProductDescriptorService getConcreteWorkProductDescriptorService() {
		return this.concreteWorkProductDescriptorService;
	}

	/**
	 * Allows to set the concreteWorkProductDescriptorService
	 * 
	 * @param _concreteWorkProductDescriptorService
	 */
	public void setConcreteWorkProductDescriptorService(
			ConcreteWorkProductDescriptorService _concreteWorkProductDescriptorService) {
		this.concreteWorkProductDescriptorService = _concreteWorkProductDescriptorService;
	}

	/**
	 * Allows to save the concreteWorkProductDescriptor for a participant
	 * 
	 * @param _participant
	 * @param _concreteWorkProductDescriptor
	 */
	public void saveConcreteWorkProductDescriptorForAParticipant(
			Participant _participant,
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {

		Participant currentParticipant = this.getParticipantDao()
				.getParticipant(_participant.getLogin());

		currentParticipant
				.addConcreteWorkProductDescriptor(_concreteWorkProductDescriptor);

		this.participantDao.saveOrUpdateParticipant(currentParticipant);

	}

}
