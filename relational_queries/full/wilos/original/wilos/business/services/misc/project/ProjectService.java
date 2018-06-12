/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) 2007 Sebastien BALARD <sbalard@wilos-project.org>
 * Copyright (C) 2007 Emilien PERICO <eperico@wilos-project.org>
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
package wilos.business.services.misc.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import wilos.business.services.misc.concreteactivity.ConcreteActivityService;
import wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementService;
import wilos.business.services.misc.stateservice.StateService;
import wilos.business.services.spem2.process.ProcessService;
import wilos.hibernate.misc.concreteactivity.ConcreteActivityDao;
import wilos.hibernate.misc.concreterole.ConcreteRoleDescriptorDao;
import wilos.hibernate.misc.concretetask.ConcreteTaskDescriptorDao;
import wilos.hibernate.misc.project.ProjectDao;
import wilos.hibernate.misc.wilosuser.ParticipantDao;
import wilos.hibernate.spem2.activity.ActivityDao;
import wilos.hibernate.spem2.role.RoleDescriptorDao;
import wilos.hibernate.spem2.task.TaskDescriptorDao;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement;
import wilos.model.misc.concretemilestone.ConcreteMilestone;
import wilos.model.misc.concreterole.ConcreteRoleDescriptor;
import wilos.model.misc.concretetask.ConcreteTaskDescriptor;
import wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor;
import wilos.model.misc.project.Project;
import wilos.model.misc.wilosuser.Participant;
import wilos.model.spem2.activity.Activity;
import wilos.model.spem2.breakdownelement.BreakdownElement;
import wilos.model.spem2.process.Process;
import wilos.model.spem2.role.RoleDescriptor;
import wilos.model.spem2.task.TaskDescriptor;
import wilos.model.spem2.workproduct.WorkProductDescriptor;
import wilos.presentation.web.utils.WebSessionService;
import wilos.utils.Constantes.State;

/**
 * The services associated to the Project
 * 
 */
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class ProjectService {

	private ProjectDao projectDao;

	private ParticipantDao participantDao;

	private ActivityDao activityDao;

	private StateService stateService;

	private ConcreteWorkBreakdownElementService concreteWorkBreakdownElementService;

	private ConcreteActivityDao concreteActivityDao;

	private ConcreteActivityService concreteActivityService;

	private ConcreteTaskDescriptorDao concreteTaskDescriptorDao;

	private ConcreteRoleDescriptorDao concreteRoleDescriptorDao;

	private TaskDescriptorDao taskDescriptorDao;

	private RoleDescriptorDao roleDescriptorDao;

	private ProcessService processService;

	public final Log logger = null; //LogFactory.getLog(this.getClass());

	private Project[] projects;

	/**
	 * Allows to create a taskDescriptor
	 * 
	 * @param _presentationName
	 * @param _description
	 * @param _mainRole
	 * @param _guid
	 * @return the taskDescriptor if it's created, null in the other case
	 */
	public TaskDescriptor createTaskDescriptor(String _presentationName,
			String _description, RoleDescriptor _mainRole, String _guid) {
		TaskDescriptor taskDesc = new TaskDescriptor();
		taskDesc.setPresentationName(_presentationName);
		taskDesc.setDescription(_description);
		taskDesc.setGuid(_guid);
		taskDesc.setPrefix("");
		taskDesc.setIsPlanned(true);
		taskDesc.setHasMultipleOccurrences(false);
		taskDesc.setIsOptional(false);
		taskDesc.setIsRepeatable(true);
		taskDesc.setIsOngoing(false);
		taskDesc.setIsEvenDriven(false);
		if(_mainRole != null){
		_mainRole.addPrimaryTask(taskDesc);
		}
		this.taskDescriptorDao.saveOrUpdateTaskDescriptor(taskDesc);
		System.out.println("### TaskDescriptor sauve");
		if (taskDesc.getId() != null)
			return taskDesc;
		else
			return null;
	}

	/**
	 * Allows to create a concreteTaskDescriptor
	 * 
	 * @param _concreteName
	 * @param _project
	 * @param _td
	 * @param _cact
	 * @return true if the concreteTaskDescriptor is created, false in the other
	 *         case
	 */
	public boolean createConcreteTaskDescriptor(String _concreteName,
			Project _project, TaskDescriptor _td, ConcreteActivity _cact) {
		ConcreteTaskDescriptor concTaskDesc = new ConcreteTaskDescriptor();

		// this variable is used to generate a random number for the
		// instanciationOrder in order to correct the tree problem
		int i = Math.abs((int) System.currentTimeMillis());

		concTaskDesc.setConcreteName(_concreteName);
		concTaskDesc.setProject(_project);
		concTaskDesc.setInstanciationOrder(i);
		concTaskDesc.addSuperConcreteActivity(_cact);

		concTaskDesc.setTaskDescriptor(_td);
		concTaskDesc.setBreakdownElement(_td);
		concTaskDesc.setWorkBreakdownElement(_td);

		this.concreteTaskDescriptorDao
		.saveOrUpdateConcreteTaskDescriptor(concTaskDesc);
		
		this.concreteActivityDao.saveOrUpdateConcreteActivity(_cact);
		
		return (concTaskDesc.getId() != null);

	}
	/**
	 * Allows to create a task
	 * 
	 * @param _taskName
	 * @param _taskDescription
	 * @param _project
	 * @param _role
	 * @param _cact
	 * @param recursive
	 * @return true if the task is created, false in the other case
	 */
	public boolean createTask(String _taskName, String _taskDescription,
			Project _project, RoleDescriptor _role, ConcreteActivity _cact,
			boolean recursive) {

		if(_role != null){
			this.roleDescriptorDao.getSessionFactory().getCurrentSession()
			.saveOrUpdate(_role);
		}
		this.concreteActivityDao.getSessionFactory().getCurrentSession()
		.saveOrUpdate(_cact);
		this.concreteActivityDao.getSessionFactory().getCurrentSession()
		.saveOrUpdate(_project);

		TaskDescriptor td = this.createTaskDescriptor(_taskName,
				_taskDescription, _role, _taskName);
		if(_role == null){
			td.setMainRole(null);
		}
		if (td == null) {
			return false;
		}

		if (recursive) {
			for (ConcreteBreakdownElement cbe : _cact
					.getConcreteBreakdownElements()) {
				if (cbe instanceof ConcreteActivity) {
					if (!(this.createConcreteTaskDescriptor(_taskName,
							_project, td, (ConcreteActivity) cbe)))
						return false;
				}
			}
		}
		if (!(this.createConcreteTaskDescriptor(_taskName, _project, td, _cact)))
			return false;
		else
			return true;
	}

	/**
	 * Save processManager
	 * 
	 * @param _processmanager
	 */
	public void saveProject(Project _project) {
		this.projectDao.saveOrUpdateProject(_project);
	}

	/**
	 * Delete a participant
	 * 
	 * @param participantId
	 */
	public boolean deleteProject(String projectId) {
		boolean ok = false;
		Project project = this.getProject(projectId);
		if (project.getProcess() == null
				&& project.getParticipants().size() == 0
				&& project.getProjectManager() == null) {
			this.projectDao.deleteProject(project);
			ok = true;
		}
		return ok;

	}

	/**
	 * Check if the project already exist
	 * 
	 * @param _projectName
	 * @return True is the _projectName is already present
	 */
	public boolean projectExist(String _projectName) {
		boolean found = false;
		String projectName;
		List<Project> projects = this.projectDao.getAllProjects();
		for (Project project : projects) {
			projectName = project.getConcreteName().toUpperCase();
			if (projectName.equals(_projectName.toUpperCase())) {
				return true;
			}
		}
		return found;
	}

	/**
	 * Gets the sorted project data.
	 * 
	 * @return table of sorted project data
	 */
	public List<Project> getAllSortedProjects() {
		List<Project> projectList = new ArrayList<Project>();
		projectList = this.projectDao.getAllProjects();
		projects = projectList.toArray(new Project[projectList.size()]);
		sortProject();
		return Arrays.asList(projects);
	}

	/**
	 * Sorts the list of project data.
	 */
	@SuppressWarnings("unchecked")
	protected void sortProject() {
		Comparator comparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				Project p1 = (Project) o1;
				Project p2 = (Project) o2;
				return p1.getConcreteName().compareTo(p2.getConcreteName());
			}
		};
		Arrays.sort(projects, comparator);
	}

	// =================================== getters & setters
	// ========================================

	/**
	 * This method returns the list of the projects that aren't yet finished
	 * 
	 * @return a set of Projects
	 */
	public Set<Project> getUnfinishedProjects() {
		Set<Project> unfinishedP = new HashSet<Project>();
		List<Project> projects = this.projectDao.getAllProjects();

		for (Project project : projects) {
			if (!(project.getIsFinished())) {
				unfinishedP.add(project);
			}
		}
		return unfinishedP;
	}

	/**
	 * Allows to get the set of concreteBreakdownElements for a project
	 * 
	 * @param project
	 * @return the set of concreteBreakdownElements
	 */
	public Set<ConcreteBreakdownElement> getConcreteBreakdownElementsFromProject(
			Project _project) {
		Set<ConcreteBreakdownElement> tmp = new HashSet<ConcreteBreakdownElement>();

		this.getProjectDao().getSessionFactory().getCurrentSession()
		.saveOrUpdate(_project);
		this.getProjectDao().getSessionFactory().getCurrentSession().refresh(
				_project);

		for (ConcreteBreakdownElement element : _project
				.getConcreteBreakdownElements()) {
			tmp.add(element);
		}
		return tmp;
	}

	/**
	 * Allows to get the set of concreteRoleDescriptors from a project for a
	 * concreteActivity
	 * 
	 * @param _project
	 * @return the set of concreteRoleDescriptors
	 */
	public Set<ConcreteRoleDescriptor> getConcreteRoleDescriptorsFromProject(
			ConcreteActivity _cact) {
		Set<ConcreteRoleDescriptor> tmp = new HashSet<ConcreteRoleDescriptor>();

		this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
				_cact);
		for (ConcreteBreakdownElement element : _cact
				.getConcreteBreakdownElements()) {
			if (element instanceof ConcreteRoleDescriptor) {
				ConcreteRoleDescriptor crd = (ConcreteRoleDescriptor) element;
				tmp.add(crd);
			} else if (!(element instanceof ConcreteTaskDescriptor)
					&& !(element instanceof ConcreteWorkProductDescriptor)
					&& !(element instanceof ConcreteMilestone)) {
				ConcreteActivity cact = (ConcreteActivity) element;
				tmp.addAll(this.getConcreteRoleDescriptorsFromProject(cact));
			}
		}
		return tmp;
	}

	/**
	 * Getter of projectDao.
	 * 
	 * @return the projectDao.
	 */
	public ProjectDao getProjectDao() {
		return this.projectDao;
	}

	/**
	 * Setter of projectDao.
	 * 
	 * @param _projectDao
	 *            The projectDao to set.
	 */
	public void setProjectDao(ProjectDao _projectDao) {
		this.projectDao = _projectDao;
	}

	/**
	 * This method returns all the projects.
	 * 
	 * @return A set of Project
	 */
	public List<Project> getAllProjects() {
		List<Project> projectList = new ArrayList<Project>();
		projectList = this.projectDao.getAllProjects();
		return projectList;
	}

	/**
	 * This method returns all the projects with no process.
	 * 
	 * @return A set of Project
	 */
	public Set<Project> getAllProjectsWithNoProcess() {
		Set<Project> projectList = new HashSet<Project>();
		List<Project> tmpList = new ArrayList<Project>();
		tmpList = this.projectDao.getAllProjects();
		for (Project project : tmpList) {
			if (project.getProcess() == null)
				projectList.add(project);
		}
		return projectList;
	}

	/**
	 * Returns the projects that aren't associated to a process.
	 * 
	 * @return A set of Project
	 */
	public Set<Project> getAllProjectsWithProcess() {
		Set<Project> projectList = new HashSet<Project>();
		List<Project> tmpList = new ArrayList<Project>();
		tmpList = this.projectDao.getAllProjects();
		for (Project project : tmpList) {
			if (project.getProcess() != null)
				projectList.add(project);
		}
		return projectList;
	}

	/**
	 * 
	 * Getter of project
	 * 
	 * @param _id
	 * @return the project
	 */
	public Project getProject(String _id) {
		return this.projectDao.getProject(_id);
	}

	/*
	 * public String getProjectName(String _projectId) { return
	 * this.projectDao.getProjectName(_projectId); }
	 */

	/**
	 * Getter of participantDao.
	 * 
	 * @return the participantDao.
	 */
	public ParticipantDao getParticipantDao() {
		return this.participantDao;
	}

	/**
	 * 
	 * Getter of processService
	 * 
	 * @return processService
	 */
	public ProcessService getProcessService() {
		return this.processService;
	}

	/**
	 * 
	 * Setter of processService
	 * 
	 * @param _processService
	 */
	public void setProcessService(ProcessService _processService) {
		this.processService = _processService;
	}

	/**
	 * Setter of participantDao.
	 * 
	 * @param _participantDao
	 *            The participantDao to set.
	 */
	public void setParticipantDao(ParticipantDao _participantDao) {
		this.participantDao = _participantDao;
	}

	/**
	 * 
	 * Return the participants affected to the project
	 * 
	 * @param project
	 * @return the set of participants affected to the project parameter
	 */
	public Set<Participant> getAllParticipants(Project project) {
		return project.getParticipants();
	}

	/**
	 * Allows to get the process for a project
	 * 
	 * @param _project
	 * @return the process
	 */
	public Process getProcessFromProject(Project _project) {
		this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
				_project);
		return _project.getProcess();
	}

	/**
	 * Allows to get all projects
	 * 
	 * @return projects
	 */
	public Project[] getProjects() {
		return projects;
	}

	/**
	 * Allows to set a list of projects
	 * 
	 * @param projects
	 */
	public void setProjects(Project[] projects) {
		this.projects = projects;
	}

	/**
	 * Allows to get the activityDao
	 * 
	 * @return the activityDao
	 */
	public ActivityDao getActivityDao() {
		return this.activityDao;
	}

	/**
	 * Allows to set the activityDao
	 * 
	 * @param _activityDao
	 */
	public void setActivityDao(ActivityDao _activityDao) {
		this.activityDao = _activityDao;
	}

	/**
	 * Allows to get the list of different paths of a roleDescriptor in process
	 * 
	 * @param _process
	 * @param _roleName
	 * @return the list of different paths
	 */
	public List<HashMap<String, Object>> getDifferentPathsOfRoleDescriptorInProcess(
			Process _process, String _roleName) {
		this.processService.getProcessDao().getSessionFactory()
		.getCurrentSession().saveOrUpdate(_process);

		Project project = this.getProject((String) WebSessionService
				.getAttribute(WebSessionService.PROJECT_ID));

		List<HashMap<String, Object>> lines = new ArrayList<HashMap<String, Object>>();
		String path = _process.getPresentationName();

		return this.giveRoleDescriptorsPathName(project, _process, path,
				_roleName, lines);
	}

	/**
	 * Allows to give the list of roleDescriptors path name
	 * 
	 * @param _process
	 * @param path
	 * @return the list of roleDescriptors path name
	 */
	private List<HashMap<String, Object>> giveRoleDescriptorsPathName(
			Project _project, Activity _act, String path, String _roleName,
			List<HashMap<String, Object>> lines) {

		final String TABLE_LEAF = "images/expandableTable/leaf.gif";

		for (BreakdownElement bde : _act.getBreakdownElements()) {
			if (bde instanceof RoleDescriptor) {
				if (bde.getPresentationName().equals(_roleName)) {
					path += " / " + bde.getPresentationName();

					HashMap<String, Object> hm = new HashMap<String, Object>();
					hm.put("nodeType", "leaf");
					hm.put("expansionImage", TABLE_LEAF);
					hm.put("id", bde.getId());
					hm.put("name", path);
					int nbcrd = this.getConcreteRoleDescriptorsFromProject(
							_project).size();
					if (nbcrd > 0) {
						hm.put("nbOccurences", new Integer(0));
					} else {
						hm.put("nbOccurences", new Integer(1));
					}
					hm.put("parentId", _roleName);
					lines.add(hm);
				}
			} else if ((bde instanceof Activity)) {
				Activity act = (Activity) bde;
				String newPath = path + " / " + act.getPresentationName();
				lines = this.giveRoleDescriptorsPathName(_project, act,
						newPath, _roleName, lines);
			}
		}
		return lines;
	}

	/**
	 * Allows to get the concreteActivityDao
	 * 
	 * @return the concreteActivityDao
	 */
	public ConcreteActivityDao getConcreteActivityDao() {
		return concreteActivityDao;
	}

	/**
	 * Allows to set the concreteActivityDao
	 * 
	 * @param _concreteActivityDao
	 */
	public void setConcreteActivityDao(ConcreteActivityDao _concreteActivityDao) {
		concreteActivityDao = _concreteActivityDao;
	}

	/**
	 * Allows to get the concreteTaskDescriptorDao
	 * 
	 * @return the concreteTaskDescriptorDao
	 */
	public ConcreteTaskDescriptorDao getConcreteTaskDescriptorDao() {
		return concreteTaskDescriptorDao;
	}

	/**
	 * Allows to set the concreteTaskDescriptorDao
	 * 
	 * @param _concreteTaskDescriptorDao
	 */
	public void setConcreteTaskDescriptorDao(
			ConcreteTaskDescriptorDao _concreteTaskDescriptorDao) {
		concreteTaskDescriptorDao = _concreteTaskDescriptorDao;
	}

	/**
	 * Allows to get the set of concreteWorkProductDescriptors from a project
	 * for a concreteActivity
	 * 
	 * @param _cact
	 * @return the set of concreteWorkProductDescriptors
	 */
	public Set<ConcreteWorkProductDescriptor> getConcreteWorkProductDescriptorsFromProject(
			ConcreteActivity _cact) {

		Set<ConcreteWorkProductDescriptor> tmp = new HashSet<ConcreteWorkProductDescriptor>();

		this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
				_cact);
		for (ConcreteBreakdownElement element : _cact
				.getConcreteBreakdownElements()) {
			if (element instanceof ConcreteWorkProductDescriptor) {

				ConcreteWorkProductDescriptor cwpd = (ConcreteWorkProductDescriptor) element;
				tmp.add(cwpd);
			} else if (!(element instanceof ConcreteTaskDescriptor)
					&& !(element instanceof ConcreteRoleDescriptor)
					&& !(element instanceof ConcreteMilestone)) {
				ConcreteActivity cact = (ConcreteActivity) element;
				tmp.addAll(this
						.getConcreteWorkProductDescriptorsFromProject(cact));
			}
		}
		return tmp;
	}

	/**
	 * Allows to get the list of different paths of a workProductDescriptor in
	 * process
	 * 
	 * @param _process
	 * @param _workProductName
	 * @return the list of different paths
	 */
	public List<HashMap<String, Object>> getDifferentPathsOfWorkProductDescriptorInProcess(
			Process _process, String _workProductName) {
		this.processService.getProcessDao().getSessionFactory()
		.getCurrentSession().saveOrUpdate(_process);

		Project project = this.getProject((String) WebSessionService
				.getAttribute(WebSessionService.PROJECT_ID));

		List<HashMap<String, Object>> lines = new ArrayList<HashMap<String, Object>>();
		String path = _process.getPresentationName();

		return this.giveWorkProductDescriptorsPathName(project, _process, path,
				_workProductName, lines);
	}

	/**
	 * Allows to get the list of workProductDescriptors path name
	 * 
	 * @param _project
	 * @param _act
	 * @param _path
	 * @param _workProductName
	 * @param _lines
	 * @return the list of workProductDescriptors path name
	 */
	private List<HashMap<String, Object>> giveWorkProductDescriptorsPathName(
			Project _project, Activity _act, String _path,
			String _workProductName, List<HashMap<String, Object>> _lines) {

		final String TABLE_LEAF = "images/expandableTable/leaf.gif";

		for (BreakdownElement bde : _act.getBreakdownElements()) {
			if (bde instanceof WorkProductDescriptor) {
				if (bde.getPresentationName().equals(_workProductName)) {
					_path += " / " + bde.getPresentationName();

					HashMap<String, Object> hm = new HashMap<String, Object>();
					hm.put("nodeType", "leaf");
					hm.put("expansionImage", TABLE_LEAF);
					hm.put("id", bde.getId());
					hm.put("name", _path);
					int nbcwpd = this
					.getConcreteWorkProductDescriptorsFromProject(
							_project).size();
					if (nbcwpd > 0) {
						hm.put("nbOccurences", new Integer(0));
					} else {
						hm.put("nbOccurences", new Integer(1));
					}
					hm.put("parentId", _workProductName);
					_lines.add(hm);
				}
			} else if ((bde instanceof Activity)) {
				Activity act = (Activity) bde;
				String newPath = _path + " / " + act.getPresentationName();
				_lines = this.giveWorkProductDescriptorsPathName(_project, act,
						newPath, _workProductName, _lines);
			}
		}
		return _lines;
	}

	/**
	 * Allows to update the concreteActivity's state for a project
	 * 
	 * @param _project
	 */
	public void updateConcreteActivitiesStateFromProject(Project _project) {
	    
		this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
				_project);
		this.projectDao.getSessionFactory().getCurrentSession().refresh(
				_project);
		
		this.stateService.updateStateTo(_project, State.READY);
	}

	/**
	 * Allows to get the concreteActivityService
	 * 
	 * @return the concreteActivityService
	 */
	public ConcreteActivityService getConcreteActivityService() {
		return concreteActivityService;
	}

	/**
	 * Allows to set the concreteActivityService
	 * 
	 * @param _concreteActivityService
	 */
	public void setConcreteActivityService(
			ConcreteActivityService _concreteActivityService) {
		concreteActivityService = _concreteActivityService;
	}

	/**
	 * Allows to get the concreteRoleDescriptorDao
	 * 
	 * @return the concreteRoleDescriptorDao
	 */
	public ConcreteRoleDescriptorDao getConcreteRoleDescriptorDao() {
		return concreteRoleDescriptorDao;
	}

	/**
	 * Allows to set the concreteRoleDescriptorDao
	 * 
	 * @param concreteRoleDescriptorDao
	 */
	public void setConcreteRoleDescriptorDao(
			ConcreteRoleDescriptorDao concreteRoleDescriptorDao) {
		this.concreteRoleDescriptorDao = concreteRoleDescriptorDao;
	}

	/**
	 * Allows to get the taskDescriptorDao
	 * 
	 * @return the taskDescriptorDao
	 */
	public TaskDescriptorDao getTaskDescriptorDao() {
		return taskDescriptorDao;
	}

	/**
	 * Allows to set the taskDescriptorDao
	 * 
	 * @param taskDescriptorDao
	 */
	public void setTaskDescriptorDao(TaskDescriptorDao taskDescriptorDao) {
		this.taskDescriptorDao = taskDescriptorDao;
	}

	/**
	 * Allows to get the roleDescriptorDao
	 * 
	 * @return the roleDescriptorDao
	 */
	public RoleDescriptorDao getRoleDescriptorDao() {
		return roleDescriptorDao;
	}

	/**
	 * Allows to set the roleDescriptorDao
	 * 
	 * @param roleDescriptorDao
	 */
	public void setRoleDescriptorDao(RoleDescriptorDao roleDescriptorDao) {
		this.roleDescriptorDao = roleDescriptorDao;
	}

	/**
	 * Allows to create a role
	 * 
	 * @param _roleName
	 * @param _roleDescription
	 * @param _project
	 * @param _task
	 * @param _cact
	 * @return true if the role is created, false in the other case
	 */
	public boolean createRole(String _roleName, String _roleDescription,
			Project _project, TaskDescriptor _task, ConcreteActivity _cact) {
		if (_task != null) {
			this.taskDescriptorDao.getSessionFactory().getCurrentSession()
			.saveOrUpdate(_task);
		}
		this.concreteActivityDao.getSessionFactory().getCurrentSession()
		.saveOrUpdate(_cact);

		// create role descriptor
		RoleDescriptor rd = this.createRoleDescriptor(_roleName,
				_roleDescription, _task, _roleName);

		if (rd == null) {
			return false;
		}

		// create BreakDownElement

		for (ConcreteBreakdownElement cbe : _cact
				.getConcreteBreakdownElements()) {
			if (cbe instanceof ConcreteActivity) {
				if (!(this.createConcreteRoleDescriptor(_roleName, _project,
						rd, (ConcreteActivity) cbe)))
					return false;
			}
		}

		if (!(this.createConcreteRoleDescriptor(_roleName, _project, rd, _cact)))
			return false;
		else
			return true;
	}

	/**
	 * Allows to create a roleDescriptor
	 * 
	 * @param _presentationName
	 * @param _description
	 * @param _mainTask
	 * @param _guid
	 * @return the roleDescriptor created, null in the other case
	 */
	public RoleDescriptor createRoleDescriptor(String _presentationName,
			String _description, TaskDescriptor _mainTask, String _guid) {

		RoleDescriptor roleDesc = new RoleDescriptor();
		roleDesc.setIsOutOfProcess(true);
		roleDesc.setPresentationName(_presentationName);
		roleDesc.setDescription(_description);
		roleDesc.setGuid(_guid);
		roleDesc.setPrefix("");
		roleDesc.setIsPlanned(true);
		roleDesc.setHasMultipleOccurrences(false);
		roleDesc.setIsOptional(false);

		if (_mainTask != null) {
			_mainTask.addMainRole(roleDesc);
		}

		this.roleDescriptorDao.saveOrUpdateRoleDescriptor(roleDesc);

		if (roleDesc.getId() != null)
			return roleDesc;
		else
			return null;
	}

	/**
	 * Allows to create a concreteRoleDescriptor
	 * 
	 * @param _concreteName
	 * @param _project
	 * @param _rd
	 * @param _cact
	 * @return true if the concreteRoleDescriptor is created, false in the other
	 *         case
	 */
	public boolean createConcreteRoleDescriptor(String _concreteName,
			Project _project, RoleDescriptor _rd, ConcreteActivity _cact) {
		ConcreteRoleDescriptor concRoleDesc = new ConcreteRoleDescriptor();

		concRoleDesc.setConcreteName(_concreteName);
		concRoleDesc.setProject(_project);
		concRoleDesc.setInstanciationOrder(1);
		concRoleDesc.addSuperConcreteActivity(_cact);

		concRoleDesc.setRoleDescriptor(_rd);
		concRoleDesc.setBreakdownElement(_rd);

		this.concreteRoleDescriptorDao
		.saveOrUpdateConcreteRoleDescriptor(concRoleDesc);

		return (concRoleDesc.getId() != null);

	}

	/**
	 * Allows to get the concreteWorkBreakdownElementService
	 * 
	 * @return the concreteWorkBreakdownElementService
	 */
	public ConcreteWorkBreakdownElementService getConcreteWorkBreakdownElementService() {
		return this.concreteWorkBreakdownElementService;
	}

	/**
	 * Allows to set the concreteWorkBreakdownElementService
	 * 
	 * @param _concreteWorkBreakdownElementService
	 */
	public void setConcreteWorkBreakdownElementService(
			ConcreteWorkBreakdownElementService _concreteWorkBreakdownElementService) {
		this.concreteWorkBreakdownElementService = _concreteWorkBreakdownElementService;
	}

	/**
	 * Allows to get the service's state
	 * 
	 * @return the stateService
	 */
	public StateService getStateService() {
		return this.stateService;
	}

	/**
	 * Allows to set the service's state
	 * 
	 * @param _stateService
	 */
	public void setStateService(StateService _stateService) {
		this.stateService = _stateService;
	}
}
