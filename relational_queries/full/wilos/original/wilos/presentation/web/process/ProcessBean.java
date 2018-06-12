/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
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

package wilos.presentation.web.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wilos.business.services.misc.project.ProjectService;
import wilos.business.services.misc.wilosuser.ProcessManagerService;
import wilos.business.services.misc.wilosuser.WilosUserService;
import wilos.business.services.spem2.process.ProcessManagementService;
import wilos.business.services.spem2.process.ProcessService;
import wilos.model.misc.project.Project;
import wilos.model.misc.wilosuser.WilosUser;
import wilos.model.spem2.process.Process;
import wilos.presentation.web.expandabletable.RolesExpTableBean;
import wilos.presentation.web.expandabletable.TasksExpTableBean;
import wilos.presentation.web.expandabletable.WorkProductsExpTableBean;
import wilos.presentation.web.utils.WebCommonService;
import wilos.presentation.web.utils.WebSessionService;
import wilos.resources.LocaleBean;

public class ProcessBean {

	private ProcessService processService;

	private ProjectService projectService;

	private ProcessManagerService processManagerService;

	private List<HashMap<String, Object>> processesList;

	private String processesListView;

	private static final String VIEW_NULL = "processesManagement_null";

	private static final String VIEW_NOT_NULL = "processesManagementPanelGroup_not_null";

	protected final Log logger = null; //LogFactory.getLog(this.getClass());

	private ProcessManagementService processManagementService;

	private boolean visiblePopup = false;

	private String processId;

	/* fields for ExpandableTable management */

	private Project project;

	private String selectedProcessId = "default";

	private boolean instanciateDependenciesWithProcess = false;

	private boolean readOnly = false;

	private boolean isVisibleExpTable = false;

	private boolean isProjectManager = false;

	private WilosUserService wilosUserService;

	/* field for dependencies view management */

	// TODO utile ?
	private String instanciationDependenciesView = "view_instanciation_panelGroup";

	/* Manage the popup. */

	/**
	 * This method allow to print the right message when an user want to delete
	 * the selected process
	 * 
	 * @param event
	 */
	public void confirmDelete(ActionEvent event) {
		if (this.processManagementService.hasBeenInstanciated(this.processId)) {
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.process.management.deletionforbidden"));
		} else {
			this.processManagementService.removeProcess(this.processId);
			WebCommonService.addInfoMessage(LocaleBean
					.getText("component.process.management.deletiondone"));
		}

		this.visiblePopup = false;
	}

	/**
	 * This method fixed the visiblePopup boolean attribute to false
	 * 
	 * @param event
	 */
	public void cancelDelete(ActionEvent event) {
		this.visiblePopup = false;
	}

	/* Others for Process management */

	/**
	 * 
	 * Deletes selected process from the database
	 * 
	 * @param e
	 *            event received when a user clicks on suppress button in the
	 *            datatable
	 */
	public void deleteProcess(ActionEvent e) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map map = context.getExternalContext().getRequestParameterMap();
		this.processId = (String) map.get("processId");
		this.visiblePopup = true;
	}

	/**
	 * Getter of processesList.
	 * 
	 * @return the processesList.
	 */
	public List<HashMap<String, Object>> getProcessesList() {
		if (this.processesList == null
				|| this.processesList.size() != this.processService
						.getAllProcesses().size()) {

			this.processesList = new ArrayList<HashMap<String, Object>>();
			for (Process process : this.processService.getAllProcesses()) {
				HashMap<String, Object> processDescription = new HashMap<String, Object>();
				processDescription.put("presentationName", process
						.getPresentationName());
				processDescription.put("id", process.getId());
				processDescription.put("isEditable", new Boolean(false));

				if (process.getProcessManager() != null) {
					WilosUser manager = wilosUserService.getSimpleUser(process
							.getProcessManager());
					process.setProcessManager(manager.getId());
					processDescription.put("owner", manager.getFirstname()
							+ " " + manager.getName());
				}

				this.processesList.add(processDescription);
			}
			return this.processesList;
		}
		return this.processesList;
	}

	/**
	 * 
	 * Editing process name
	 * 
	 * @param e
	 *            event received when a user clicks on edit button in the
	 *            datatable
	 */
	public void editName(ActionEvent e) {
		String processId = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get(
						"processEditId");
		for (HashMap<String, Object> processDescription : this.processesList) {
			if (((String) processDescription.get("id")).equals(processId)) {
				processDescription.put("isEditable", new Boolean(true));
			}
		}
	}

	/**
	 * 
	 * Saving new process name
	 * 
	 * @param e
	 *            event received when a user clicks on save button in the
	 *            datatable
	 */
	public void saveName(ActionEvent e) {
		String processId = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get(
						"processSaveId");
		Process process = this.processService.getProcessDao().getProcess(
				processId);
		for (HashMap<String, Object> processDescription : this
				.getProcessesList()) {
			if (((String) processDescription.get("id")).equals(processId)) {
				String presentationName = (String) processDescription
						.get("presentationName");

				if (presentationName.trim().length() == 0) {
					processDescription.put("presentationName", process
							.getPresentationName());

					// Error message.
					WebCommonService
							.addInfoMessage(LocaleBean
									.getText("component.process.management.message.invalidName"));
				} else if (this.presentationNameAlreadyExists(presentationName,
						processId)) {
					processDescription.put("presentationName", process
							.getPresentationName());

					// Error message.
					WebCommonService
							.addInfoMessage(LocaleBean
									.getText("component.process.management.message.nameAlreadyExists"));
				} else {
					process.setPresentationName(presentationName);
					this.processService.getProcessDao().saveOrUpdateProcess(
							process);
					processDescription.put("isEditable", new Boolean(false));
				}
				break;
			}
		}
	}

	/**
	 * this method allow to search if the given presentationName for the given
	 * Process is already existing in the database
	 * 
	 * @param _presentationName
	 * @param _processId
	 * @return boolean
	 */
	private boolean presentationNameAlreadyExists(String _presentationName,
			String _processId) {
		for (Process process : this.processService.getAllProcesses())
			if ((process.getPresentationName().equals(_presentationName))
					&& (!_processId.equals(process.getId())))
				return true;
		return false;
	}

	/* Others for ExpandableTable management */

	/**
	 * Give all the processes save in the database
	 * 
	 * @return the processes list
	 */
	public List<SelectItem> getProcesses() {

		List<SelectItem> processesList = new ArrayList<SelectItem>();

		processesList.add(new SelectItem("default", LocaleBean
				.getText("component.combobox.processchoice.defaultlabel")));

		List<Process> processes = this.processService.getProcessDao()
				.getAllProcesses();
		for (Process process : processes) {
			processesList.add(new SelectItem(process.getId(), process
					.getPresentationName()));
		}
		return processesList;
	}

	/**
	 * 
	 * listener on the processes selection combobox
	 */
	public void changeProcessSelectionListener(ValueChangeEvent evt) {

		TasksExpTableBean tasksExpTableBean = (TasksExpTableBean) WebCommonService
				.getBean("TasksExpTableBean");
		RolesExpTableBean rolesExpTableBean = (RolesExpTableBean) WebCommonService
				.getBean("RolesExpTableBean");
		WorkProductsExpTableBean workproductsExpTableBean = (WorkProductsExpTableBean) WebCommonService
				.getBean("WorkProductsExpTableBean");

		this.selectedProcessId = (String) evt.getNewValue();
		tasksExpTableBean.setSelectedProcessId((String) evt.getNewValue());
		rolesExpTableBean.setSelectedProcessId((String) evt.getNewValue());
		workproductsExpTableBean.setSelectedProcessId((String) evt
				.getNewValue());
		if (this.selectedProcessId.equals("default")) {
			this.isVisibleExpTable = false;
		} else {
			this.isVisibleExpTable = true;
		}
	}

	/**
	 * getter of selectedProcessId String attribute
	 * 
	 * @return the processGuid
	 */
	public String getSelectedProcessId() {
		return this.selectedProcessId;
	}

	/**
	 * setter of selectedProcessId String attribute
	 * 
	 * @param _processGuid
	 *            the processGuid to set
	 */
	public void setSelectedProcessId(String _processGuid) {
		this.selectedProcessId = _processGuid;
	}

	/**
	 * getter of isVisibleExpTable boolean attribute
	 * 
	 * @return the isVisibleExpTable
	 */
	public boolean getIsVisibleExpTable() {
		return this.isVisibleExpTable;
	}

	/**
	 * setter of isVisibleExpTable boolean attribute
	 * 
	 * @param _isVisibleExpTable
	 *            the isVisibleExpTable to set
	 */
	public void setIsVisibleExpTable(boolean _isVisibleExpTable) {
		this.isVisibleExpTable = _isVisibleExpTable;
	}

	/**
	 * getter of readOnly boolean attribute
	 * 
	 * @return the readOnly
	 */
	public boolean getReadOnly() {

		String projectId = (String) WebSessionService
				.getAttribute(WebSessionService.PROJECT_ID);
		Project project = this.projectService.getProject(projectId);

		if (project.getProcess() == null) {
			this.readOnly = false;

		} else {
			this.readOnly = true;
		}

		return this.readOnly;
	}

	/**
	 * setter of readOnly boolean attribute
	 * 
	 * @param _readOnly
	 *            the readOnly to set
	 */
	public void setReadOnly(boolean _readOnly) {
		this.readOnly = _readOnly;
	}

	/**
	 * getter of isProjectManager boolean attribute
	 * 
	 * @return the isProjectManager
	 */
	public boolean getIsProjectManager() {

		String user_id = (String) WebSessionService
				.getAttribute(WebSessionService.WILOS_USER_ID);
		String projectId = (String) WebSessionService
				.getAttribute(WebSessionService.PROJECT_ID);

		if (projectId == null) {
			return false;
		}

		this.project = this.projectService.getProject(projectId);
		if (project == null) {
			return false;
		}

		if (this.project.getProjectManager() != null) {
			if (this.project.getProjectManager().getId().equals(user_id)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * getter of isProjectManager boolean attribute
	 * 
	 * @return the isProjectManager
	 */
	public boolean getIsProjectManagerOrProjectDirector() {

		boolean ok = false;

		String user_id = (String) WebSessionService
				.getAttribute(WebSessionService.WILOS_USER_ID);
		String projectId = (String) WebSessionService
				.getAttribute(WebSessionService.PROJECT_ID);
		String role = (String) WebSessionService
				.getAttribute(WebSessionService.ROLE_TYPE);
		if (projectId == null) {
			return false;
		}

		this.project = this.projectService.getProject(projectId);
		if (project == null) {
			return false;
		}

		if (this.project.getProjectManager() != null) {
			if (this.project.getProjectManager().getId().equals(user_id)) {
				ok = true;
			}
		}

		if (role.equals("projectDirector")) {
			if (project.getProjectDirector() != null) {
				if ((project.getProjectDirector().equals(user_id))) {
					ok = true;
				}
			}
		}
		return ok;

	}

	public boolean getIsProjectDirector() {

		String user_id = (String) WebSessionService
				.getAttribute(WebSessionService.WILOS_USER_ID);

		String role = (String) WebSessionService
				.getAttribute(WebSessionService.ROLE_TYPE);

		String projectId = (String) WebSessionService
				.getAttribute(WebSessionService.PROJECT_ID);

		if (projectId == null) {
			return false;
		}
		Project project = this.projectService.getProject(projectId);
		if (project == null || project.equals("default")) {
			return false;
		}
		if (role.equals("projectDirector")) {
			if (project.getProjectDirector() != null) {
				if ((project.getProjectDirector().equals(user_id))) {
					return true;
				}
			}
		}
		return false;

	}

	/**
	 * setter of isProjectManager boolean attribute
	 * 
	 * @param _isProjectManager
	 *            the isProjectManager to set
	 */
	public void setIsProjectManager(boolean _isProjectManager) {
		this.isProjectManager = _isProjectManager;
	}

	/* Getters & Setters */

	/**
	 * Setter of processesList.
	 * 
	 * @param _processesList
	 *            The processesList to set.
	 */
	public void setProcessesList(List<HashMap<String, Object>> _processesList) {
		this.processesList = _processesList;
	}

	/**
	 * Getter of processesListView.
	 * 
	 * @return the processesListView.
	 */
	public String getProcessesListView() {
		if (this.getProcessesList().size() == 0) {
			this.processesListView = VIEW_NULL;
		} else {
			this.processesListView = VIEW_NOT_NULL;
		}
		return this.processesListView;
	}

	/**
	 * Setter of processesListView.
	 * 
	 * @param _processesListView
	 *            The processesListView to set.
	 */
	public void setProcessesListView(String _processesListView) {
		this.processesListView = _processesListView;
	}

	/**
	 * Getter of processService.
	 * 
	 * @return the processService.
	 */
	public ProcessService getProcessService() {
		return this.processService;
	}

	/**
	 * Setter of processService.
	 * 
	 * @param _processService
	 *            The processService to set.
	 */
	public void setProcessService(ProcessService _processService) {
		this.processService = _processService;
	}

	/**
	 * Getter of projectService.
	 * 
	 * @return the projectService.
	 */
	public ProjectService getProjectService() {
		return this.projectService;
	}

	/**
	 * Setter of projectService.
	 * 
	 * @param _projectService
	 *            The projectService to set.
	 */
	public void setProjectService(ProjectService _projectService) {
		this.projectService = _projectService;
	}

	/**
	 * this method allow to return the current Instance of
	 * ProcessManagementService
	 * 
	 * @return ProcessManagementService
	 */

	public ProcessManagementService getProcessManagementService() {
		return processManagementService;
	}

	/**
	 * this method allow to set the current Instance of ProcessManagementService
	 * 
	 * @param processManagementService
	 */

	public void setProcessManagementService(
			ProcessManagementService processManagementService) {
		this.processManagementService = processManagementService;
	}

	/**
	 * this method allow to return the current Instance of ProcessManagerService
	 * 
	 * @return ProcessManagerService
	 */

	public ProcessManagerService getProcessManagerService() {
		return processManagerService;
	}

	/**
	 * this method allow to set the current Instance of ProcessManagerService
	 * 
	 * @param processManagerService
	 */

	public void setProcessManagerService(
			ProcessManagerService processManagerService) {
		this.processManagerService = processManagerService;
	}

	/**
	 * getter of visiblePopup boolean attribute
	 * 
	 * @return the visiblePopup
	 */
	public boolean getVisiblePopup() {
		return this.visiblePopup;
	}

	/**
	 * setter of visiblePopup boolean attribute
	 * 
	 * @param visiblePopup
	 *            the visiblePopup to set
	 */
	public void setVisiblePopup(boolean _visiblePopup) {
		this.visiblePopup = _visiblePopup;
	}

	/**
	 * getter of instanciationDependenciesView String attribute
	 * 
	 * @return the instanciationDependenciesView
	 */
	public String getInstanciationDependenciesView() {
		return this.instanciationDependenciesView;
	}

	/**
	 * setter of instanciationDependenciesView String attribute
	 * 
	 * @param _instanciationDependenciesView
	 *            the instanciationDependenciesView to set
	 */
	public void setInstanciationDependenciesView(
			String _instanciationDependenciesView) {
		this.instanciationDependenciesView = _instanciationDependenciesView;
	}

	/**
	 * getter of instanciateDependenciesWithProcess boolean attribute
	 * 
	 * @return the instanciateDependenciesWithProcess
	 */
	public boolean getInstanciateDependenciesWithProcess() {
		return instanciateDependenciesWithProcess;
	}

	/**
	 * setter of instanciateDependenciesWithProcess boolean attribute
	 * 
	 * @param instanciateDependenciesWithProcess
	 *            the instanciateDependenciesWithProcess to set
	 */
	public void setInstanciateDependenciesWithProcess(
			boolean _instanciateDependenciesWithProcess) {
		this.instanciateDependenciesWithProcess = _instanciateDependenciesWithProcess;
	}

	public WilosUserService getWilosUserService() {
		return wilosUserService;
	}

	public void setWilosUserService(WilosUserService wilosUserService) {
		this.wilosUserService = wilosUserService;
	}

}
