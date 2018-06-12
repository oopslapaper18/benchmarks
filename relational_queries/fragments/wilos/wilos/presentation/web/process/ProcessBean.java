package wilos.
  presentation.
  web.
  process;

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
    private wilos.business.services.spem2.process.ProcessService processService;
    private wilos.business.services.misc.project.ProjectService projectService;
    private wilos.business.services.misc.wilosuser.ProcessManagerService
      processManagerService;
    private java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> processesList;
    private java.lang.String processesListView;
    private static final java.lang.String VIEW_NULL =
      "processesManagement_null";
    private static final java.lang.String VIEW_NOT_NULL =
      "processesManagementPanelGroup_not_null";
    protected final org.apache.commons.logging.Log logger = null;
    private wilos.business.services.spem2.process.ProcessManagementService
      processManagementService;
    private boolean visiblePopup = false;
    private java.lang.String processId;
    private wilos.model.misc.project.Project project;
    private java.lang.String selectedProcessId = "default";
    private boolean instanciateDependenciesWithProcess = false;
    private boolean readOnly = false;
    private boolean isVisibleExpTable = false;
    private boolean isProjectManager = false;
    private wilos.business.services.misc.wilosuser.WilosUserService
      wilosUserService;
    private java.lang.String instanciationDependenciesView =
      "view_instanciation_panelGroup";
    
    /**
     * This method allow to print the right message when an user want to delete
     * the selected process
     * 
     * @param event
     */
    public void confirmDelete(javax.faces.event.ActionEvent event) {
        if (this.processManagementService.hasBeenInstanciated(this.processId)) {
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText("component.process.management.deletionforbidden"));
        }
        else {
            this.processManagementService.removeProcess(
                                            this.processId);
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addInfoMessage(
                wilos.resources.LocaleBean.
                    getText("component.process.management.deletiondone"));
        }
        this.visiblePopup = false;
    }
    
    /**
     * This method fixed the visiblePopup boolean attribute to false
     * 
     * @param event
     */
    public void cancelDelete(javax.faces.event.ActionEvent event) {
        this.visiblePopup = false;
    }
    
    /**
     * 
     * Deletes selected process from the database
     * 
     * @param e
     *            event received when a user clicks on suppress button in the
     *            datatable
     */
    public void deleteProcess(javax.faces.event.ActionEvent e) {
        javax.faces.context.FacesContext context =
          javax.faces.context.FacesContext.getCurrentInstance();
        java.util.Map map =
          context.getExternalContext().getRequestParameterMap();
        this.processId = (java.lang.String) map.get("processId");
        this.visiblePopup = true;
    }
    
    /**
     * Getter of processesList.
     * 
     * @return the processesList.
     */
    public java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> getProcessesList() {
        if (this.processesList == null || this.processesList.size() !=
              this.processService.getAllProcesses().size()) {
            this.processesList =
              new java.util.ArrayList<java.util.HashMap<java.lang.String,
              java.lang.Object>>();
            java.util.Iterator extfor$iter =
              this.processService.getAllProcesses().iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.spem2.process.Process process =
                  (wilos.model.spem2.process.Process) extfor$iter.next();
                java.util.HashMap<java.lang.String,
                java.lang.Object> processDescription =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                processDescription.put("presentationName",
                                       process.getPresentationName());
                processDescription.put("id", process.getId());
                processDescription.put("isEditable",
                                       new java.lang.Boolean(false));
                if (process.getProcessManager() != null) {
                    wilos.model.misc.wilosuser.WilosUser manager =
                      wilosUserService.getSimpleUser(
                                         process.getProcessManager());
                    process.setProcessManager(manager.getId());
                    processDescription.put("owner",
                                           manager.getFirstname() + " " +
                                             manager.getName());
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
    public void editName(javax.faces.event.ActionEvent e) {
        java.lang.String processId =
          (java.lang.String)
            javax.faces.context.FacesContext.getCurrentInstance(
                                               ).getExternalContext(
                                                   ).getRequestParameterMap(
                                                       ).get("processEditId");
        java.util.Iterator extfor$iter = this.processesList.iterator();
        while (extfor$iter.hasNext()) {
            java.util.HashMap<java.lang.String,
            java.lang.Object> processDescription =
              (java.util.HashMap<java.lang.String, java.lang.Object>)
                extfor$iter.next();
            if (((java.lang.String) processDescription.get("id")).
                  equals(processId)) {
                processDescription.put("isEditable",
                                       new java.lang.Boolean(true));
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
    public void saveName(javax.faces.event.ActionEvent e) {
        java.lang.String processId =
          (java.lang.String)
            javax.faces.context.FacesContext.getCurrentInstance(
                                               ).getExternalContext(
                                                   ).getRequestParameterMap(
                                                       ).get("processSaveId");
        wilos.model.spem2.process.Process process =
          this.processService.getProcessDao().getProcess(processId);
        java.util.Iterator extfor$iter = this.getProcessesList().iterator();
        boolean break_0 = false;
        while (extfor$iter.hasNext() && !break_0) {
            java.util.HashMap<java.lang.String,
            java.lang.Object> processDescription =
              (java.util.HashMap<java.lang.String, java.lang.Object>)
                extfor$iter.next();
            if (!break_0)
                if (((java.lang.String) processDescription.get("id")).
                      equals(processId)) {
                    java.lang.String presentationName =
                      (java.lang.String)
                        processDescription.get("presentationName");
                    if (presentationName.trim().length() == 0) {
                        processDescription.put("presentationName",
                                               process.getPresentationName());
                        wilos.
                          presentation.
                          web.
                          utils.
                          WebCommonService.
                          addInfoMessage(
                            wilos.resources.LocaleBean.
                                getText(
                                  "component.process.management.message.invalidName"));
                    }
                    else
                        if (this.presentationNameAlreadyExists(
                                   presentationName, processId)) {
                            processDescription.put(
                                                 "presentationName",
                                                 process.getPresentationName());
                            wilos.
                              presentation.
                              web.
                              utils.
                              WebCommonService.
                              addInfoMessage(
                                wilos.resources.LocaleBean.
                                    getText(
                                      "component.process.management.message.nameAlreadyExists"));
                        }
                        else {
                            process.setPresentationName(presentationName);
                            this.processService.getProcessDao().
                              saveOrUpdateProcess(process);
                            processDescription.put(
                                                 "isEditable",
                                                 new java.lang.Boolean(false));
                        }
                    break_0 = true;
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
    private boolean presentationNameAlreadyExists(java.lang.
                                                    String _presentationName,
                                                  java.lang.String _processId) {
        labeled_1 :
        {
            boolean r;
            r = false;
            java.util.Iterator extfor$iter =
                    this.processService.getAllProcesses().iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.spem2.process.Process process =
                        (wilos.model.spem2.process.Process) extfor$iter.next();
                if (process.getPresentationName().equals(_presentationName) &&
                        !_processId.equals(process.getId())) r = true;
            }
            r = false;
        }
        return r;
    }
    
    /**
     * Give all the processes save in the database
     * 
     * @return the processes list
     */
    public java.util.List<javax.faces.model.SelectItem> getProcesses() {
        java.util.List<javax.faces.model.SelectItem> processesList =
          new java.util.ArrayList<javax.faces.model.SelectItem>();
        processesList.
          add(
            new javax.
                faces.
                model.
                SelectItem(
                "default",
                wilos.resources.LocaleBean.
                    getText("component.combobox.processchoice.defaultlabel")));
        java.util.List<wilos.model.spem2.process.Process> processes =
          this.processService.getProcessDao().getAllProcesses();
        java.util.Iterator extfor$iter = processes.iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.process.Process process =
              (wilos.model.spem2.process.Process) extfor$iter.next();
            processesList.add(
                            new javax.faces.model.SelectItem(
                                process.getId(),
                                process.getPresentationName()));
        }
        return processesList;
    }
    
    /**
     * 
     * listener on the processes selection combobox
     */
    public void changeProcessSelectionListener(javax.faces.event.
                                                 ValueChangeEvent evt) {
        wilos.
          presentation.
          web.
          expandabletable.
          TasksExpTableBean
          tasksExpTableBean =
          (wilos.presentation.web.expandabletable.TasksExpTableBean)
            wilos.presentation.web.utils.WebCommonService.
            getBean("TasksExpTableBean");
        wilos.
          presentation.
          web.
          expandabletable.
          RolesExpTableBean
          rolesExpTableBean =
          (wilos.presentation.web.expandabletable.RolesExpTableBean)
            wilos.presentation.web.utils.WebCommonService.
            getBean("RolesExpTableBean");
        wilos.
          presentation.
          web.
          expandabletable.
          WorkProductsExpTableBean
          workproductsExpTableBean =
          (wilos.presentation.web.expandabletable.WorkProductsExpTableBean)
            wilos.presentation.web.utils.WebCommonService.
            getBean("WorkProductsExpTableBean");
        this.selectedProcessId = (java.lang.String) evt.getNewValue();
        tasksExpTableBean.setSelectedProcessId((java.lang.String)
                                                 evt.getNewValue());
        rolesExpTableBean.setSelectedProcessId((java.lang.String)
                                                 evt.getNewValue());
        workproductsExpTableBean.setSelectedProcessId((java.lang.String)
                                                        evt.getNewValue());
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
    public java.lang.String getSelectedProcessId() {
        return this.selectedProcessId;
    }
    
    /**
     * setter of selectedProcessId String attribute
     * 
     * @param _processGuid
     *            the processGuid to set
     */
    public void setSelectedProcessId(java.lang.String _processGuid) {
        this.selectedProcessId = _processGuid;
    }
    
    /**
     * getter of isVisibleExpTable boolean attribute
     * 
     * @return the isVisibleExpTable
     */
    public boolean getIsVisibleExpTable() { return this.isVisibleExpTable; }
    
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
        java.
          lang.
          String
          projectId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.PROJECT_ID);
        wilos.model.misc.project.Project project =
          this.projectService.getProject(projectId);
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
    public void setReadOnly(boolean _readOnly) { this.readOnly = _readOnly; }
    
    /**
     * getter of isProjectManager boolean attribute
     * 
     * @return the isProjectManager
     */
    public boolean getIsProjectManager() {
        java.
          lang.
          String
          user_id =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.WILOS_USER_ID);
        java.
          lang.
          String
          projectId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.PROJECT_ID);
        if (projectId == null) { return false; }
        this.project = this.projectService.getProject(projectId);
        if (project == null) { return false; }
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
        java.
          lang.
          String
          user_id =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.WILOS_USER_ID);
        java.
          lang.
          String
          projectId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.PROJECT_ID);
        java.
          lang.
          String
          role =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.ROLE_TYPE);
        if (projectId == null) { return false; }
        this.project = this.projectService.getProject(projectId);
        if (project == null) { return false; }
        if (this.project.getProjectManager() != null) {
            if (this.project.getProjectManager().getId().equals(user_id)) {
                ok = true;
            }
        }
        if (role.equals("projectDirector")) {
            if (project.getProjectDirector() != null) {
                if (project.getProjectDirector().equals(user_id)) { ok = true; }
            }
        }
        return ok;
    }
    
    public boolean getIsProjectDirector() {
        java.
          lang.
          String
          user_id =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.WILOS_USER_ID);
        java.
          lang.
          String
          role =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.ROLE_TYPE);
        java.
          lang.
          String
          projectId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.PROJECT_ID);
        if (projectId == null) { return false; }
        wilos.model.misc.project.Project project =
          this.projectService.getProject(projectId);
        if (project == null || project.equals("default")) { return false; }
        if (role.equals("projectDirector")) {
            if (project.getProjectDirector() != null) {
                if (project.getProjectDirector().equals(user_id)) {
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
    
    /**
     * Setter of processesList.
     * 
     * @param _processesList
     *            The processesList to set.
     */
    public void setProcessesList(java.util.List<java.util.HashMap<java.lang.
                                   String, java.lang.Object>> _processesList) {
        this.processesList = _processesList;
    }
    
    /**
     * Getter of processesListView.
     * 
     * @return the processesListView.
     */
    public java.lang.String getProcessesListView() {
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
    public void setProcessesListView(java.lang.String _processesListView) {
        this.processesListView = _processesListView;
    }
    
    /**
     * Getter of processService.
     * 
     * @return the processService.
     */
    public wilos.business.services.spem2.process.
      ProcessService getProcessService() { return this.processService; }
    
    /**
     * Setter of processService.
     * 
     * @param _processService
     *            The processService to set.
     */
    public void setProcessService(wilos.business.services.spem2.process.
                                    ProcessService _processService) {
        this.processService = _processService;
    }
    
    /**
     * Getter of projectService.
     * 
     * @return the projectService.
     */
    public wilos.business.services.misc.project.
      ProjectService getProjectService() { return this.projectService; }
    
    /**
     * Setter of projectService.
     * 
     * @param _projectService
     *            The projectService to set.
     */
    public void setProjectService(wilos.business.services.misc.project.
                                    ProjectService _projectService) {
        this.projectService = _projectService;
    }
    
    /**
     * this method allow to return the current Instance of
     * ProcessManagementService
     * 
     * @return ProcessManagementService
     */
    public wilos.business.services.spem2.process.
      ProcessManagementService getProcessManagementService() {
        return processManagementService;
    }
    
    /**
     * this method allow to set the current Instance of ProcessManagementService
     * 
     * @param processManagementService
     */
    public void setProcessManagementService(wilos.business.services.spem2.
                                              process.
                                              ProcessManagementService processManagementService) {
        this.processManagementService = processManagementService;
    }
    
    /**
     * this method allow to return the current Instance of ProcessManagerService
     * 
     * @return ProcessManagerService
     */
    public wilos.business.services.misc.wilosuser.
      ProcessManagerService getProcessManagerService() {
        return processManagerService;
    }
    
    /**
     * this method allow to set the current Instance of ProcessManagerService
     * 
     * @param processManagerService
     */
    public void setProcessManagerService(wilos.business.services.misc.wilosuser.
                                           ProcessManagerService processManagerService) {
        this.processManagerService = processManagerService;
    }
    
    /**
     * getter of visiblePopup boolean attribute
     * 
     * @return the visiblePopup
     */
    public boolean getVisiblePopup() { return this.visiblePopup; }
    
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
    public java.lang.String getInstanciationDependenciesView() {
        return this.instanciationDependenciesView;
    }
    
    /**
     * setter of instanciationDependenciesView String attribute
     * 
     * @param _instanciationDependenciesView
     *            the instanciationDependenciesView to set
     */
    public void setInstanciationDependenciesView(java.lang.
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
    public void setInstanciateDependenciesWithProcess(boolean _instanciateDependenciesWithProcess) {
        this.instanciateDependenciesWithProcess =
          _instanciateDependenciesWithProcess;
    }
    
    public wilos.business.services.misc.wilosuser.
      WilosUserService getWilosUserService() { return wilosUserService; }
    
    public void setWilosUserService(wilos.business.services.misc.wilosuser.
                                      WilosUserService wilosUserService) {
        this.wilosUserService = wilosUserService;
    }
    
    public ProcessBean() { super(); }
}

