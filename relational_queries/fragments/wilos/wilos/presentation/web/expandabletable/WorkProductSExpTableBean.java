package wilos.presentation.web.expandabletable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import com.icesoft.faces.component.ext.RowSelectorEvent;
import wilos.business.services.misc.concreteactivity.ConcreteActivityService;
import wilos.business.services.misc.concretetask.ConcreteTaskDescriptorService;
import wilos.business.services.misc.concreteworkproduct.ConcreteWorkProductDescriptorService;
import wilos.business.services.misc.project.ProjectService;
import wilos.business.services.spem2.activity.ActivityService;
import wilos.business.services.spem2.process.ProcessService;
import wilos.business.services.spem2.role.RoleDescriptorService;
import wilos.business.services.spem2.workproduct.WorkProductDescriptorService;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement;
import wilos.model.misc.concretetask.ConcreteTaskDescriptor;
import wilos.model.misc.project.Project;
import wilos.model.spem2.breakdownelement.BreakdownElement;
import wilos.model.spem2.process.Process;
import wilos.model.spem2.role.RoleDescriptor;
import wilos.model.spem2.workproduct.WorkProductDescriptor;
import wilos.presentation.web.project.ProjectAdvancementBean;
import wilos.presentation.web.tree.TreeBean;
import wilos.presentation.web.utils.WebCommonService;
import wilos.presentation.web.utils.WebSessionService;
import wilos.resources.LocaleBean;
import wilos.utils.Constantes.State;

public class WorkProductsExpTableBean {
    public static final java.lang.String EXPAND_TABLE_ARROW =
      "images/expandableTable/expand.gif";
    public static final java.lang.String CONTRACT_TABLE_ARROW =
      "images/expandableTable/contract.gif";
    public static final java.lang.String TABLE_LEAF =
      "images/expandableTable/leaf.gif";
    public static final java.lang.String INDENTATION_STRING = "|- - - ";
    private java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> expTableContentWorkProduct =
      new java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>>();
    protected java.util.HashMap<java.lang.String,
    java.lang.Boolean> isExpandedTableWorkProduct =
      new java.util.HashMap<java.lang.String, java.lang.Boolean>();
    private java.util.HashMap<java.lang.String,
    java.lang.String> indentationContentWorkProduct =
      new java.util.HashMap<java.lang.String, java.lang.String>();
    private wilos.business.services.spem2.process.ProcessService processService;
    private wilos.business.services.misc.project.ProjectService projectService;
    private wilos.business.services.spem2.activity.ActivityService activityService;
    private wilos.business.services.misc.concreteworkproduct.ConcreteWorkProductDescriptorService concreteWorkProductDescriptorService;
    private wilos.business.services.spem2.workproduct.WorkProductDescriptorService workProductDescriptorService;
    private wilos.business.services.misc.concretetask.ConcreteTaskDescriptorService concreteTaskDescriptorService;
    private boolean isVisibleWorkProductInstanciationPanel = false;
    private boolean allConcreteActivitiesAreFinishedWorkProduct = false;
    private java.lang.String viewedProcessId = "";
    private java.lang.String selectedProcessId = "default";
    private boolean isVisibleNewWorkProductPanel = false;
    private java.lang.String newWorkProductName;
    private java.lang.String selectedRoleDescriptorId = "default";
    private java.lang.String selectedConcreteActivityId = "default";
    private java.lang.String newWorkProductDescription;
    private boolean addWorkProductRendered = false;
    private wilos.business.services.misc.concreteactivity.ConcreteActivityService concreteActivityService;
    private wilos.business.services.spem2.role.RoleDescriptorService roleDescriptorService;
    private boolean visibleRoleComboBox = false;
    private java.lang.String activityEntryState = "";
    private java.lang.String activityExitState = "";
    private java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> producerConcreteTasksSelectable =
      new java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>>();
    private java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> producerConcreteTasksSelected =
      new java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>>();
    private java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> optionalUserConcreteTasksSelectable =
      new java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>>();
    private java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> optionalUserConcreteTasksSelected =
      new java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>>();
    private java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> mandatoryUserConcreteTasksSelectable =
      new java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>>();
    private java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> mandatoryUserConcreteTasksSelected =
      new java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>>();
    private boolean clearProducerConcreteTasksSelectable = false;
    private boolean clearProducerConcreteTasksSelected = false;
    private boolean clearOptionalUserConcreteTasksSelectable = false;
    private boolean clearOptionalUserConcreteTasksSelected = false;
    private boolean clearMandatoryUserConcreteTasksSelectable = false;
    private boolean clearMandatoryUserConcreteTasksSelected = false;
    private boolean selectTaskToAffectToProduct = false;
    private boolean activitySelected;
    
    /**
     * this method allows to save the workProduct instanciation for the selected
     * project
     * 
     * @param Action
     *                Event _event
     * @return nothing
     */
    public void saveWorkProductInstanciation(javax.faces.event.ActionEvent _event) {
        java.lang.String projectId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                             wilos.presentation.web.utils.WebSessionService.PROJECT_ID);
        if (projectId != null && !this.selectedProcessId.equals("default")) {
            wilos.model.misc.project.Project project =
              projectService.getProject(projectId);
            if (project != null) {
                wilos.model.spem2.process.Process process =
                  processService.getProcessDao().getProcess(selectedProcessId);
                if (process != null) {
                    this.workProductDescriptorService.workProductsInstanciation(
                                                        project, process,
                                                        expTableContentWorkProduct);
                }
            }
            this.expTableContentWorkProduct.clear();
            wilos.presentation.web.utils.WebCommonService.addInfoMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.instanciation.instanciatedMessage"));
            wilos.presentation.web.tree.TreeBean tb =
              (wilos.presentation.web.tree.TreeBean)
                wilos.presentation.web.utils.WebCommonService.getBean(
                                                                "TreeBean");
            tb.rebuildProjectTree();
        }
    }
    
    /**
     * getter of expTableContent HashMap List attribute
     * 
     * @return the expTableContent
     */
    public java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> getExpTableContentWorkProduct() {
        if (!this.selectedProcessId.equals("default")) {
            wilos.model.spem2.process.Process process =
              this.processService.getProcess(this.selectedProcessId);
            if (!this.viewedProcessId.equals(process.getId()) ||
                  this.expTableContentWorkProduct.isEmpty()) {
                this.viewedProcessId = process.getId();
                this.isExpandedTableWorkProduct.clear();
                this.expTableContentWorkProduct.clear();
                this.indentationContentWorkProduct.clear();
                java.util.List<java.util.HashMap<java.lang.String,
                java.lang.Object>> lines = this.getExpTableLineContent(process);
                this.expTableContentWorkProduct.addAll(lines);
            }
        }
        return this.expTableContentWorkProduct;
    }
    
    /**
     * this method allow to return a list of workProductDescriptor for the given
     * Process
     * 
     * @param _process
     * @return List<HashMap<String,Object>> of workProductDescriptor
     */
    private java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> getExpTableLineContent(wilos.model.spem2.process.Process _process) {
        java.util.List<java.util.HashMap<java.lang.String,
        java.lang.Object>> lines =
          new java.util.ArrayList<java.util.HashMap<java.lang.String,
        java.lang.Object>>();
        wilos.model.misc.project.Project project =
          this.projectService.getProject(
                                (java.lang.String)
                                  wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                                   wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
        java.util.Iterator extfor$iter =
          this.processService.getWorkProductDescriptorsFromProcess(
                                _process).iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.workproduct.WorkProductDescriptor wpd =
              (wilos.model.spem2.workproduct.WorkProductDescriptor)
                extfor$iter.next();
            java.util.HashMap<java.lang.String,
            java.lang.Object> hm = new java.util.HashMap<java.lang.String,
            java.lang.Object>();
            int nb =
              this.processService.getWorkProductDescriptorsWithTheSameNameNumberInProcess(
                                    _process, wpd.getPresentationName());
            if (nb == 0) {
                hm.put("nodeType", "leaf");
                hm.put("expansionImage", TABLE_LEAF);
            } else {
                hm.put("nodeType", "node");
                hm.put("expansionImage", CONTRACT_TABLE_ARROW);
            }
            hm.put("id", wpd.getId());
            hm.put("name", wpd.getPresentationName());
            hm.put("isDisabled", false);
            int nbcwpd =
              this.projectService.getConcreteWorkProductDescriptorsFromProject(
                                    project).size();
            if (nbcwpd > 0) {
                hm.put("nbOccurences", new java.lang.Integer(0));
            } else {
                hm.put("nbOccurences", new java.lang.Integer(1));
            }
            hm.put("parentId", wpd.getPresentationName());
            lines.add(hm);
        }
        return lines;
    }
    
    /**
     * Utility method to add all child nodes to the parent dataTable list.
     */
    private void expandNodeActionWorkProduct() {
        javax.faces.context.FacesContext context =
          javax.faces.context.FacesContext.getCurrentInstance();
        java.util.Map map =
          context.getExternalContext().getRequestParameterMap();
        java.lang.String workProductId = (java.lang.String)
                                           map.get("workProductId");
        java.lang.String workProductName = (java.lang.String)
                                             map.get("workProductName");
        wilos.model.misc.project.Project project =
          this.projectService.getProject(
                                (java.lang.String)
                                  wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                                   wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
        wilos.model.spem2.process.Process process =
          this.projectService.getProcessFromProject(project);
        java.util.ArrayList<java.util.HashMap<java.lang.String,
        java.lang.Object>> tmp =
          new java.util.ArrayList<java.util.HashMap<java.lang.String,
        java.lang.Object>>();
        tmp.addAll(this.expTableContentWorkProduct);
        int index;
        java.util.Iterator extfor$iter = tmp.iterator();
        while (extfor$iter.hasNext()) {
            java.util.HashMap<java.lang.String,
            java.lang.Object> hashMap = (java.util.HashMap<java.lang.String,
                                        java.lang.Object>) extfor$iter.next();
            if (hashMap.get("id").equals(workProductId)) {
                if (hashMap.get("nodeType").equals("node")) {
                    hashMap.put("expansionImage", EXPAND_TABLE_ARROW);
                    hashMap.put("isDisabled", true);
                    index = this.expTableContentWorkProduct.indexOf(hashMap);
                    this.expTableContentWorkProduct.addAll(
                                                      index + 1,
                                                      this.projectService.getDifferentPathsOfWorkProductDescriptorInProcess(
                                                                            process,
                                                                            workProductName));
                    return;
                }
            }
        }
    }
    
    /**
     * Utility method to remove all child nodes from the parent dataTable list.
     */
    private void contractNodeActionWorkProduct() {
        javax.faces.context.FacesContext context =
          javax.faces.context.FacesContext.getCurrentInstance();
        java.util.Map map =
          context.getExternalContext().getRequestParameterMap();
        java.lang.String workProductId = (java.lang.String)
                                           map.get("workProductId");
        java.lang.String workProductName = (java.lang.String)
                                             map.get("workProductName");
        java.util.ArrayList<java.util.HashMap<java.lang.String,
        java.lang.Object>> parentList =
          new java.util.ArrayList<java.util.HashMap<java.lang.String,
        java.lang.Object>>();
        parentList.addAll(this.expTableContentWorkProduct);
        java.util.Iterator extfor$iter =
          this.expTableContentWorkProduct.iterator();
        while (extfor$iter.hasNext()) {
            java.util.HashMap<java.lang.String,
            java.lang.Object> currentElement =
              (java.util.HashMap<java.lang.String, java.lang.Object>)
                extfor$iter.next();
            if (currentElement.get("id").equals(workProductId) &&
                  currentElement.get("nodeType").equals("node")) {
                currentElement.put("expansionImage", CONTRACT_TABLE_ARROW);
                currentElement.put("isDisabled", false);
                parentList.remove(currentElement);
            }
        }
        this.deleteChildrenWorkProduct(workProductName, parentList);
    }
    
    /**
     * Hides children for a specific row of the expandable table Used to
     * simulate contraction behaviour
     * 
     * @param _parentId
     *                identifier of current row
     * @param parentList
     *                parent children
     */
    private void deleteChildrenWorkProduct(java.lang.String _parentName,
                                           java.util.ArrayList<java.util.HashMap<java.lang.String,
                                           java.lang.Object>> _parentList) {
        java.util.Iterator extfor$iter = _parentList.iterator();
        while (extfor$iter.hasNext()) {
            java.util.HashMap<java.lang.String,
            java.lang.Object> child = (java.util.HashMap<java.lang.String,
                                      java.lang.Object>) extfor$iter.next();
            if (child.get("parentId").equals(_parentName)) {
                this.expTableContentWorkProduct.remove(child);
                deleteChildrenWorkProduct((java.lang.String) child.get("id"),
                                          _parentList);
            }
            if (child.get("id").equals(_parentName)) {
                child.put("expansionImage", CONTRACT_TABLE_ARROW);
                this.isExpandedTableWorkProduct.put((java.lang.String)
                                                      child.get("id"), false);
            }
        }
    }
    
    /**
     * Toggles the expanded state of this ConcreteBreakDownElement.
     * 
     * @param event
     */
    public void toggleSubGroupActionWorkProduct(javax.faces.event.ActionEvent event) {
        javax.faces.context.FacesContext context =
          javax.faces.context.FacesContext.getCurrentInstance();
        java.util.Map map =
          context.getExternalContext().getRequestParameterMap();
        java.lang.String workProductId = (java.lang.String)
                                           map.get("workProductId");
        java.lang.Boolean b =
          this.isExpandedTableWorkProduct.get(workProductId);
        if (b == null) {
            this.isExpandedTableWorkProduct.put(workProductId, true);
            b = this.isExpandedTableWorkProduct.get(workProductId);
        } else {
            if (b) { b = false; } else { b = true; }
            this.isExpandedTableWorkProduct.put(workProductId, b);
        }
        if (b) {
            expandNodeActionWorkProduct();
        } else {
            contractNodeActionWorkProduct();
        }
    }
    
    /**
     * setter of expTableContent HashMap List attribute
     * 
     * @param _expTableContent
     *                the expTableContent to set
     */
    public void setExpTableContentWorkProduct(java.util.ArrayList<java.util.HashMap<java.lang.String,
                                              java.lang.Object>> _expTableContent) {
        this.expTableContentWorkProduct = _expTableContent;
    }
    
    /**
     * getter of isExpanded HashMap attribute
     * 
     * @return the isExpanded
     */
    public java.util.HashMap<java.lang.String,
    java.lang.Boolean> getIsExpandedTableWorkProduct() {
        return this.isExpandedTableWorkProduct;
    }
    
    /**
     * setter of isExpanded HashMap attribute
     * 
     * @param _isExpanded
     *                the isExpanded to set
     */
    public void setIsExpandedTableWorkProduct(java.util.HashMap<java.lang.String,
                                              java.lang.Boolean> _isExpanded) {
        this.isExpandedTableWorkProduct = _isExpanded;
    }
    
    /**
     * this method allows to return the current instance of ActivityService
     * 
     * @return the activityService
     */
    public wilos.business.services.spem2.activity.ActivityService getActivityService() {
        return this.activityService;
    }
    
    /**
     * this method allows to set the current instance of ActivityService
     * 
     * @param _activityService
     *                the activityService to set
     */
    public void setActivityService(wilos.business.services.spem2.activity.ActivityService _activityService) {
        this.activityService = _activityService;
    }
    
    /**
     * this method allows to return the current instance of ProcessService
     * 
     * @return the processService
     */
    public wilos.business.services.spem2.process.ProcessService getProcessService() {
        return this.processService;
    }
    
    /**
     * this method allows to set the current instance of ProcessService
     * 
     * @param _processService
     *                the processService to set
     */
    public void setProcessService(wilos.business.services.spem2.process.ProcessService _processService) {
        this.processService = _processService;
    }
    
    /**
     * this method allows to return the current instance of ProjectService
     * 
     * @return the projectService
     */
    public wilos.business.services.misc.project.ProjectService getProjectService() {
        return this.projectService;
    }
    
    /**
     * this method allows to set the current instance of ProjectService
     * 
     * @param _projectService
     *                the projectService to set
     */
    public void setProjectService(wilos.business.services.misc.project.ProjectService _projectService) {
        this.projectService = _projectService;
    }
    
    /**
     * getter of selectedProcessId String attribute
     * 
     * @return the selectedProcessId
     */
    public java.lang.String getSelectedProcessId() {
        return this.selectedProcessId;
    }
    
    /**
     * setter of selectedProcessId String attribute
     * 
     * @param _selectedProcessGuid
     *                the selectedProcessId to set
     */
    public void setSelectedProcessId(java.lang.String _selectedProcessGuid) {
        this.selectedProcessId = _selectedProcessGuid;
    }
    
    /**
     * getter of indentationContent HashMap attribute
     * 
     * @return the indentationContent
     */
    public java.util.HashMap<java.lang.String,
    java.lang.String> getIndentationContentWorkProduct() {
        return this.indentationContentWorkProduct;
    }
    
    /**
     * setter of indentationContent HashMap attribute
     * 
     * @param _indentationContent
     *                the indentationContent to set
     */
    public void setIndentationContentWorkProduct(java.util.HashMap<java.lang.String,
                                                 java.lang.String> _indentationContent) {
        this.indentationContentWorkProduct = _indentationContent;
    }
    
    /**
     * this method allows to return the current instance of
     * WorkProductDescriptorService
     * 
     * @return the workProductDescriptorService
     */
    public wilos.business.services.spem2.workproduct.WorkProductDescriptorService getWorkProductDescriptorService() {
        return this.workProductDescriptorService;
    }
    
    /**
     * this method allows to set the current instance of
     * WorkProductDescriptorService
     * 
     * @param _workProductDescriptorService
     *                the workProductDescriptorService to set
     */
    public void setWorkProductDescriptorService(wilos.business.services.spem2.workproduct.WorkProductDescriptorService _workProductDescriptorService) {
        this.workProductDescriptorService = _workProductDescriptorService;
    }
    
    /**
     * getter of isVisibleWorkProductInstanciationPanel boolean attribute
     * 
     * @return the isVisibleWorkProductInstanciationPanel
     */
    public boolean getIsVisibleWorkProductInstanciationPanel() {
        wilos.model.misc.project.Project project =
          this.projectService.getProject(
                                (java.lang.String)
                                  wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                                   wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
        wilos.model.spem2.process.Process process =
          this.projectService.getProcessFromProject(project);
        if (process != null) {
            this.isVisibleWorkProductInstanciationPanel = true;
        } else {
            this.isVisibleWorkProductInstanciationPanel = false;
        }
        return this.isVisibleWorkProductInstanciationPanel;
    }
    
    /**
     * setter of isVisibleWorkProductInstanciationPanel boolean attribute
     * 
     * @param _isVisibleWorkProductInstanciationPanel
     *                the isVisibleWorkProductInstanciationPanel to set
     */
    public void setVisibleWorkProductInstanciationPanel(boolean _isVisibleWorkProductInstanciationPanel) {
        this.isVisibleWorkProductInstanciationPanel =
          _isVisibleWorkProductInstanciationPanel;
    }
    
    /**
     * getter of isVisibleNewWorkProductPanel boolean attribute
     * 
     * @return the isVisibleNewWorkProductPanel
     */
    public boolean getIsVisibleNewWorkProductPanel() {
        wilos.model.misc.project.Project project =
          this.projectService.getProject(
                                (java.lang.String)
                                  wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                                   wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
        wilos.model.spem2.process.Process process =
          this.projectService.getProcessFromProject(project);
        if (process != null) {
            this.isVisibleNewWorkProductPanel = true;
        } else {
            this.isVisibleNewWorkProductPanel = false;
        }
        return this.isVisibleNewWorkProductPanel;
    }
    
    /**
     * setter of isVisibleNewWorkProductPanel boolean attribute
     * 
     * @param _isVisibleNewWorkProductPanel
     *                the isVisibleNewWorkProductPanel to set
     */
    public void setIsVisibleNewWorkProductPanel(boolean _isVisibleNewWorkProduct) {
        this.isVisibleNewWorkProductPanel = _isVisibleNewWorkProduct;
    }
    
    /**
     * getter of newWorkProductName String attribute
     * 
     * @return the newWorkProductName
     */
    public java.lang.String getNewWorkProductName() {
        return this.newWorkProductName;
    }
    
    /**
     * setter of newWorkProductName String attribute
     * 
     * @param _newWorkProductName
     *                the newWorkProductName to set
     */
    public void setNewWorkProductName(java.lang.String _newWorkProductName) {
        this.newWorkProductName = _newWorkProductName;
    }
    
    /**
     * ChangeListener on the Combobox including the roles
     * 
     * @param evt
     *                an ValueChangeEvent
     */
    public void changeRolesListener(javax.faces.event.ValueChangeEvent evt) {
        this.selectedRoleDescriptorId = (java.lang.String) evt.getNewValue();
        this.addWorkProductRendered =
          !this.selectedRoleDescriptorId.equals("default") &&
            !this.selectedConcreteActivityId.equals("default");
    }
    
    /**
     * Give all the roles save in the database for the given process
     * 
     * @return the List<SelectItem> of roleDescriptor
     */
    public java.util.List<javax.faces.model.SelectItem> getRoles() {
        java.util.List<javax.faces.model.SelectItem> rolesList =
          new java.util.ArrayList<javax.faces.model.SelectItem>();
        rolesList.add(
                    new javax.faces.model.SelectItem(
                        "default",
                        wilos.resources.LocaleBean.getText(
                                                     ("component.project.workproductsinstanciation.roleComboBoxDefa" +
                                                      "ultChoice"))));
        wilos.model.misc.project.Project project =
          this.projectService.getProject(
                                (java.lang.String)
                                  wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                                   wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
        if (project != null) {
            wilos.model.spem2.process.Process process = project.getProcess();
            if (process != null) {
                wilos.model.misc.concreteactivity.ConcreteActivity cact =
                  this.concreteActivityService.getConcreteActivity(
                                                 this.selectedConcreteActivityId);
                if (this.selectedConcreteActivityId.equals(project.getId())) {
                    java.util.SortedSet<wilos.model.spem2.breakdownelement.BreakdownElement> bdes =
                      this.processService.getAllBreakdownElements(process);
                    java.util.Iterator extfor$iter = bdes.iterator();
                    while (extfor$iter.hasNext()) {
                        wilos.model.spem2.breakdownelement.BreakdownElement bde =
                          (wilos.model.spem2.breakdownelement.BreakdownElement)
                            extfor$iter.next();
                        if (bde instanceof wilos.model.spem2.role.RoleDescriptor) {
                            wilos.model.spem2.role.RoleDescriptor rd =
                              (wilos.model.spem2.role.RoleDescriptor) bde;
                            rolesList.add(
                                        new javax.faces.model.SelectItem(
                                            rd.getId(),
                                            rd.getPresentationName()));
                        }
                    }
                }
                if (!this.selectedConcreteActivityId.equals("default") &&
                      !this.selectedConcreteActivityId.equals(
                                                         project.getId())) {
                    java.util.SortedSet<wilos.model.spem2.breakdownelement.BreakdownElement> bdEs =
                      this.activityService.getAllBreakdownElements(
                                             cact.getActivity());
                    java.util.Iterator extfor$iter$1 = bdEs.iterator();
                    while (extfor$iter$1.hasNext()) {
                        wilos.model.spem2.breakdownelement.BreakdownElement bde =
                          (wilos.model.spem2.breakdownelement.BreakdownElement)
                            extfor$iter$1.next();
                        if (bde instanceof wilos.model.spem2.role.RoleDescriptor) {
                            rolesList.add(
                                        new javax.faces.model.SelectItem(
                                            bde.getId(),
                                            bde.getPresentationName()));
                        }
                    }
                }
                rolesList.add(
                            new javax.faces.model.SelectItem(
                                "null",
                                wilos.resources.LocaleBean.getText(
                                                             "component.project.workproductsinstanciation.noRole")));
            }
        }
        return rolesList;
    }
    
    /**
     * ChangeListener on the Combobox including the concrete activities
     * 
     * @param evt
     *                an ValueChangeEvent
     */
    public void changeConcreteActivitiesListener(javax.faces.event.ValueChangeEvent evt) {
        this.clearProducerConcreteTasksSelectable = true;
        this.clearProducerConcreteTasksSelected = true;
        this.clearOptionalUserConcreteTasksSelectable = true;
        this.clearOptionalUserConcreteTasksSelected = true;
        this.clearMandatoryUserConcreteTasksSelectable = true;
        this.clearMandatoryUserConcreteTasksSelected = true;
        this.selectedConcreteActivityId = (java.lang.String) evt.getNewValue();
        this.addWorkProductRendered =
          !this.selectedRoleDescriptorId.equals("default") &&
            !this.selectedConcreteActivityId.equals("default");
        this.visibleRoleComboBox =
          !this.selectedConcreteActivityId.equals("default");
    }
    
    /**
     * getter of selectedRoleDescriptorId String attribute
     * 
     * @return the selectedRoleDescriptorId
     */
    public java.lang.String getSelectedRoleDescriptorId() {
        return selectedRoleDescriptorId;
    }
    
    /**
     * setter of selectedRoleDescriptorId String attribute
     * 
     * @param _selectedRoleDescriptorId
     *                the selectedRoleDescriptorId to set
     */
    public void setSelectedRoleDescriptorId(java.lang.String _selectedRoleDescriptorId) {
        selectedRoleDescriptorId = _selectedRoleDescriptorId;
    }
    
    /**
     * getter of selectedConcreteActivityId String attribute
     * 
     * @return the selectedConcreteActivityId
     */
    public java.lang.String getSelectedConcreteActivityId() {
        return selectedConcreteActivityId;
    }
    
    /**
     * setter of selectedConcreteActivityId String attribute
     * 
     * @param _selectedConcreteActivityId
     *                the selectedConcreteActivityId to set
     */
    public void setSelectedConcreteActivityId(java.lang.String _selectedConcreteActivityId) {
        selectedConcreteActivityId = _selectedConcreteActivityId;
    }
    
    /**
     * getter of newWorkProductDescription String attribute
     * 
     * @return the newWorkProductDescription
     */
    public java.lang.String getNewWorkProductDescription() {
        return newWorkProductDescription;
    }
    
    /**
     * setter of newWorkProductDescription String attribute
     * 
     * @param _newWorkProductDescription
     *                the newWorkProductDescription to set
     */
    public void setNewWorkProductDescription(java.lang.String _newWorkProductDescription) {
        newWorkProductDescription = _newWorkProductDescription;
    }
    
    /**
     * getter of addWorkProductRendered boolean attribute
     * 
     * @return the addWorkProductRendered
     */
    public boolean isAddWorkProductRendered() { return addWorkProductRendered; }
    
    /**
     * setter of addWorkProductRendered boolean attribute
     * 
     * @param _addWorkProductRendered
     *                the addWorkProductRendered to set
     */
    public void setAddWorkProductRendered(boolean _addWorkProductRendered) {
        addWorkProductRendered = _addWorkProductRendered;
    }
    
    /**
     * setter of isVisibleNewWorkProductPanel boolean attribute
     * 
     * @param _isVisibleNewWorkProductPanel
     *                the isVisibleNewWorkProductPanel to set
     */
    public void setVisibleNewWorkProductPanel(boolean _isVisibleNewWorkProductPanel) {
        isVisibleNewWorkProductPanel = _isVisibleNewWorkProductPanel;
    }
    
    /**
     * this method returns all the not finishing concreteActivities for the
     * selected Project
     * 
     * @return List<SelectItem> of concreteActivities
     */
    public java.util.List<javax.faces.model.SelectItem> getConcreteActivities() {
        java.util.List<javax.faces.model.SelectItem> activityList =
          new java.util.ArrayList<javax.faces.model.SelectItem>();
        activityList.add(
                       new javax.faces.model.SelectItem(
                           "default",
                           wilos.resources.LocaleBean.getText(
                                                        ("component.project.workproductsinstanciation.actComboBoxDefau" +
                                                         "ltChoice"))));
        wilos.model.misc.project.Project project =
          this.projectService.getProject(
                                (java.lang.String)
                                  wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                                   wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
        if (project != null) {
            java.util.Iterator extfor$iter =
              this.concreteActivityService.getConcreteActivitiesFromProject(
                                             project).iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.misc.concreteactivity.ConcreteActivity cact =
                  (wilos.model.misc.concreteactivity.ConcreteActivity)
                    extfor$iter.next();
                if (!cact.getState().equals(
                                       wilos.utils.Constantes.State.FINISHED)) {
                    activityList.add(
                                   new javax.faces.model.SelectItem(
                                       cact.getId(), cact.getConcreteName()));
                }
            }
            if (!project.getState().equals(
                                      wilos.utils.Constantes.State.FINISHED)) {
                activityList.add(
                               new javax.faces.model.SelectItem(
                                   project.getId(), project.getConcreteName()));
            }
        }
        return activityList;
    }
    
    /**
     * Method to add an out of process workProduct
     * 
     * @param evt
     * @return an empty String
     */
    public java.lang.String createOutOfProcessWorkProductActionListener() {
        if (this.newWorkProductName == "") {
            wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.project.workproductsinstanciation.noNameError"));
        }
        else
            if (this.selectedConcreteActivityId.equals("default")) {
                wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                                wilos.resources.LocaleBean.getText(
                                                                                             ("component.project.workproductsinstanciation.noActivityDescri" +
                                                                                              "ptorSelected")));
            } else
                if (this.newWorkProductDescription == "") {
                    wilos.presentation.web.utils.WebCommonService.addErrorMessage(wilos.resources.LocaleBean.getText(("component.project.workproductsinstanciation.noDescriptionErr" +
                                                                                                                      "or")));
                } else
                    if (this.selectedRoleDescriptorId.equals("default")) {
                        wilos.presentation.web.utils.WebCommonService.addErrorMessage(wilos.resources.LocaleBean.getText(("component.project.workproductsinstanciation.noRoleDescriptor" +
                                                                                                                          "Selected")));
                    } else {
                        wilos.model.misc.project.Project project = this.projectService.getProject((java.lang.String)
                                                                                                    wilos.presentation.web.utils.WebSessionService.getAttribute(wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
                        if (!this.selectedRoleDescriptorId.equals("default") &&
                              !this.selectedConcreteActivityId.equals("default")) {
                            java.lang.System.out.println("******** " +
                                                           this.selectedRoleDescriptorId);
                            wilos.model.spem2.role.RoleDescriptor rd = this.roleDescriptorService.getRoleDescriptor(this.selectedRoleDescriptorId);
                            wilos.model.misc.concreteactivity.ConcreteActivity cact = this.concreteActivityService.getConcreteActivity(this.selectedConcreteActivityId);
                            if (project.getId().equals(cact.getId())) {
                                cact = null;
                            }
                            java.util.ArrayList<java.lang.String> inputConcreteTasksIDs = new java.util.ArrayList<java.lang.String>();
                            java.util.ArrayList<java.lang.String> inputOptionnalConcreteTasksIDs = new java.util.ArrayList<java.lang.String>();
                            java.util.ArrayList<java.lang.String> outputConcreteTasksIDs = new java.util.ArrayList<java.lang.String>();
                            java.util.Iterator extfor$iter = this.mandatoryUserConcreteTasksSelectable.iterator();
                            while (extfor$iter.hasNext()) {
                                java.util.HashMap hm = (java.util.HashMap)
                                                         extfor$iter.next();
                                if (!hm.get("in").equals(hm.get("flag_in"))) {
                                    inputConcreteTasksIDs.add((java.lang.String)
                                                                hm.get("ID"));
                                }
                                if (!hm.get("out").equals(hm.get("flag_out"))) {
                                    outputConcreteTasksIDs.add((java.lang.String)
                                                                 hm.get("ID"));
                                }
                                if (!hm.get("inOptionnal").equals(hm.get("flag_inOptionnal"))) {
                                    inputOptionnalConcreteTasksIDs.add((java.lang.String)
                                                                         hm.get("ID"));
                                }
                            }
                            if (this.workProductDescriptorService.createWorkProduct(this.newWorkProductName,
                                                                                    this.newWorkProductDescription,
                                                                                    project,
                                                                                    rd,
                                                                                    cact,
                                                                                    this.activityEntryState,
                                                                                    this.activityExitState,
                                                                                    outputConcreteTasksIDs,
                                                                                    inputOptionnalConcreteTasksIDs,
                                                                                    inputConcreteTasksIDs)) {
                                wilos.presentation.web.utils.WebCommonService.addInfoMessage(wilos.resources.LocaleBean.getText(("component.project.workproductsinstanciation.creationValidati" +
                                                                                                                                 "on")));
                                this.setSelectTaskToAffectToProduct(false);
                            } else {
                                wilos.presentation.web.utils.WebCommonService.addInfoMessage(wilos.resources.LocaleBean.getText("component.project.workproductsinstanciation.creationError"));
                            }
                            wilos.presentation.web.tree.TreeBean treeBean = (wilos.presentation.web.tree.TreeBean)
                                                                              wilos.presentation.web.utils.WebCommonService.getBean("TreeBean");
                            treeBean.rebuildProjectTree();
                            wilos.presentation.web.project.ProjectAdvancementBean pab = (wilos.presentation.web.project.ProjectAdvancementBean)
                                                                                          wilos.presentation.web.utils.WebCommonService.getBean("ProjectAdvancementBean");
                            pab.refreshProjectTable();
                            this.newWorkProductDescription = "";
                            this.newWorkProductName = "";
                            this.selectedRoleDescriptorId = "default";
                            this.selectedConcreteActivityId = "default";
                            this.addWorkProductRendered = false;
                            this.visibleRoleComboBox = false;
                            this.activityEntryState = "";
                            this.activityExitState = "";
                        }
                    }
        return "";
    }
    
    /**
     * this method allows to return the current instance of
     * ConcreteActivityService
     * 
     * @return the concreteActivityService
     */
    public wilos.business.services.misc.concreteactivity.ConcreteActivityService getConcreteActivityService() {
        return concreteActivityService;
    }
    
    /**
     * this method allows to set the current instance of ConcreteActivityService
     * 
     * @param _concreteActivityService
     *                the concreteActivityService to set
     */
    public void setConcreteActivityService(wilos.business.services.misc.concreteactivity.ConcreteActivityService _concreteActivityService) {
        concreteActivityService = _concreteActivityService;
    }
    
    /**
     * this method allows to return the current instance of
     * RoleDescriptorService
     * 
     * @return the roleDescriptorService
     */
    public wilos.business.services.spem2.role.RoleDescriptorService getRoleDescriptorService() {
        return roleDescriptorService;
    }
    
    /**
     * this method allows to set the current instance of RoleDescriptorService
     * 
     * @param _roleDescriptorService
     *                the roleDescriptorService to set
     */
    public void setRoleDescriptorService(wilos.business.services.spem2.role.RoleDescriptorService _roleDescriptorService) {
        roleDescriptorService = _roleDescriptorService;
    }
    
    /**
     * getter of visibleRoleComboBox boolean attribute
     * 
     * @return the visibleRoleComboBox
     */
    public boolean isVisibleRoleComboBox() { return visibleRoleComboBox; }
    
    /**
     * setter of visibleRoleComboBox boolean attribute
     * 
     * @param _visibleRoleComboBox
     *                the visibleRoleComboBox to set
     */
    public void setVisibleRoleComboBox(boolean _visibleRoleComboBox) {
        visibleRoleComboBox = _visibleRoleComboBox;
    }
    
    /**
     * this method check if all the concreteActivities for the selected Project
     * are finished.
     * 
     * @return the boolean attribute allConcreteActivitiesAreFinished with true
     *         value when all the concreteActivities are finished
     */
    public boolean getAllConcreteActivitiesAreFinished() {
        int numberOfFinishedActivity = 0;
        wilos.model.misc.project.Project project =
          this.projectService.getProject(
                                (java.lang.String)
                                  wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                                   wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
        if (this.concreteActivityService.getConcreteActivitiesFromProject(
                                           project).size() != 0) {
            labeled_1 :
            {
                java.util.Iterator extfor$iter =
                        this.concreteActivityService.getConcreteActivitiesFromProject(
                                project).iterator();
                while (extfor$iter.hasNext())
                {
                    wilos.model.misc.concreteactivity.ConcreteActivity cact =
                            (wilos.model.misc.concreteactivity.ConcreteActivity)
                                    extfor$iter.next();
                    if (cact.getState().equals(
                            wilos.utils.Constantes.State.FINISHED))
                    {
                        numberOfFinishedActivity++;
                    }
                }
            }

            labeled_2 :
            {
                if (numberOfFinishedActivity ==
                        this.concreteActivityService.getConcreteActivitiesFromProject(
                                project).size())
                {
                    this.allConcreteActivitiesAreFinishedWorkProduct = true;
                } else
                {
                    this.allConcreteActivitiesAreFinishedWorkProduct = false;
                }
            }
        }
        return this.allConcreteActivitiesAreFinishedWorkProduct;
    }
    
    /**
     * setter of allConcreteActivitiesAreFinished boolean attribute
     * 
     * @param _allConcreteActivitiesAreFinished
     */
    public void setAllConcreteActivitiesAreFinished(boolean _allConcreteActivitiesAreFinished) {
        this.allConcreteActivitiesAreFinishedWorkProduct =
          _allConcreteActivitiesAreFinished;
    }
    
    /**
     * this method allows to control the value of producerConcreteTasksSelected
     * if the row corresponding on the event is already in the List, it will be
     * remove. else it will be added
     * 
     * @param event
     *                an RowSelectorEvent
     */
    public void producerConcreteTaskRowSelectionAction(com.icesoft.faces.component.ext.RowSelectorEvent event) {
        java.util.HashMap<java.lang.String,
        java.lang.Object> row =
          this.producerConcreteTasksSelectable.get(event.getRow());
        if (this.producerConcreteTasksSelected.contains(row)) {
            this.producerConcreteTasksSelected.remove(row);
        } else {
            this.producerConcreteTasksSelected.add(row);
        }
    }
    
    /**
     * this method allows to control the value of
     * optionalUserConcreteTasksSelected if the row corresponding on the event
     * is already in the List, it will be remove. else it will be added
     * 
     * @param event
     *                an RowSelectorEvent
     */
    public void optionalUserConcreteTaskRowSelectionAction(com.icesoft.faces.component.ext.RowSelectorEvent event) {
        java.util.HashMap<java.lang.String,
        java.lang.Object> row =
          this.optionalUserConcreteTasksSelectable.get(event.getRow());
        if (this.optionalUserConcreteTasksSelected.contains(row)) {
            this.optionalUserConcreteTasksSelected.remove(row);
        } else {
            this.optionalUserConcreteTasksSelected.add(row);
        }
    }
    
    /**
     * this method allows to control the value of
     * mandatoryUserConcreteTasksSelected if the row corresponding on the event
     * is already in the List, it will be remove. else it will be added
     * 
     * @param event
     *                an RowSelectorEvent
     */
    public void mandatoryUserConcreteTaskRowSelectionAction(com.icesoft.faces.component.ext.RowSelectorEvent event) {
        java.util.HashMap<java.lang.String,
        java.lang.Object> row =
          this.mandatoryUserConcreteTasksSelectable.get(event.getRow());
        if (this.mandatoryUserConcreteTasksSelected.contains(row)) {
            this.mandatoryUserConcreteTasksSelected.remove(row);
        } else {
            this.mandatoryUserConcreteTasksSelected.add(row);
        }
    }
    
    /**
     * this method allows to return the current instance of
     * ConcreteTaskDescriptorService
     * 
     * @return ConcreteTaskDescriptorService
     */
    public wilos.business.services.misc.concretetask.ConcreteTaskDescriptorService getConcreteTaskDescriptorService() {
        return this.concreteTaskDescriptorService;
    }
    
    /**
     * this method allows to set the current instance of
     * ConcreteTaskDescriptorService
     * 
     * @param _concreteTaskDescriptorService
     */
    public void setConcreteTaskDescriptorService(wilos.business.services.misc.concretetask.ConcreteTaskDescriptorService _concreteTaskDescriptorService) {
        this.concreteTaskDescriptorService = _concreteTaskDescriptorService;
    }
    
    /**
     * getter of activitySelected boolean attribute
     * 
     * @return boolean
     */
    public boolean getActivitySelected() {
        this.activitySelected =
          !this.selectedConcreteActivityId.equals("default");
        return this.activitySelected;
    }
    
    /**
     * setter of activitySelected boolean attribute
     * 
     * @param _activitySelected
     */
    public void setActivitySelected(boolean _activitySelected) {
        this.activitySelected = _activitySelected;
    }
    
    /**
     * getter of activityEntryState String attribute
     * 
     * @return String
     */
    public java.lang.String getActivityEntryState() {
        return this.activityEntryState;
    }
    
    /**
     * setter of activityEntryState String attribute
     * 
     * @param _activityEntryState
     */
    public void setActivityEntryState(java.lang.String _activityEntryState) {
        this.activityEntryState = _activityEntryState;
    }
    
    /**
     * getter of activityExitState String attribute
     * 
     * @return String
     */
    public java.lang.String getActivityExitState() {
        return this.activityExitState;
    }
    
    /**
     * setter of activityExitState String attribute
     * 
     * @param _activityExitState
     */
    public void setActivityExitState(java.lang.String _activityExitState) {
        this.activityExitState = _activityExitState;
    }
    
    /**
     * getter of producerConcreteTasksSelectable ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> getProducerConcreteTasksSelectable() {
        if (this.clearProducerConcreteTasksSelectable) {
            this.producerConcreteTasksSelectable.clear();
            if (!this.selectedConcreteActivityId.equals("default")) {
                wilos.model.misc.concreteactivity.ConcreteActivity ca =
                  this.concreteActivityService.getConcreteActivity(
                                                 selectedConcreteActivityId);
                java.util.SortedSet<wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement> cbdes =
                  this.concreteActivityService.getConcreteBreakdownElements(ca);
                java.util.Iterator extfor$iter = cbdes.iterator();
                while (extfor$iter.hasNext()) {
                    wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement cbde =
                      (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                        extfor$iter.next();
                    if (cbde instanceof wilos.model.misc.concretetask.ConcreteTaskDescriptor) {
                        wilos.model.misc.concretetask.ConcreteTaskDescriptor ctd =
                          (wilos.model.misc.concretetask.ConcreteTaskDescriptor)
                            cbde;
                        java.util.HashMap<java.lang.String,
                        java.lang.Object> hm =
                          new java.util.HashMap<java.lang.String,
                        java.lang.Object>();
                        hm.put("ID", ctd.getId());
                        hm.put("name", ctd.getConcreteName());
                        hm.put("selected", false);
                        this.producerConcreteTasksSelectable.add(hm);
                    }
                }
                this.clearProducerConcreteTasksSelectable = false;
            }
        }
        return this.producerConcreteTasksSelectable;
    }
    
    /**
     * setter of producerConcreteTasksSelectable ArrayList<HashMap> attribute
     * 
     * @param _producerConcreteTasksSelectable
     */
    public void setProducerConcreteTasksSelectable(java.util.ArrayList<java.util.HashMap<java.lang.String,
                                                   java.lang.Object>> _producerConcreteTasksSelectable) {
        this.producerConcreteTasksSelectable = _producerConcreteTasksSelectable;
    }
    
    /**
     * getter of producerConcreteTasksSelected ArrayList<HashMap> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> getProducerConcreteTasksSelected() {
        if (this.clearProducerConcreteTasksSelected) {
            this.producerConcreteTasksSelected.clear();
            this.clearProducerConcreteTasksSelected = false;
        }
        return this.producerConcreteTasksSelected;
    }
    
    /**
     * setter of producerConcreteTasksSelected ArrayList<HashMap> attribute
     * 
     * @param _producerConcreteTasksSelected
     */
    public void setProducerConcreteTasksSelected(java.util.ArrayList<java.util.HashMap<java.lang.String,
                                                 java.lang.Object>> _producerConcreteTasksSelected) {
        this.producerConcreteTasksSelected = _producerConcreteTasksSelected;
    }
    
    /**
     * getter of optionalUserConcreteTasksSelectable ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> getOptionalUserConcreteTasksSelectable() {
        if (this.clearOptionalUserConcreteTasksSelectable) {
            this.optionalUserConcreteTasksSelectable.clear();
            if (!this.selectedConcreteActivityId.equals("default")) {
                wilos.model.misc.concreteactivity.ConcreteActivity ca =
                  this.concreteActivityService.getConcreteActivity(
                                                 selectedConcreteActivityId);
                java.util.SortedSet<wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement> cbdes =
                  this.concreteActivityService.getConcreteBreakdownElements(ca);
                java.util.Iterator extfor$iter = cbdes.iterator();
                while (extfor$iter.hasNext()) {
                    wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement cbde =
                      (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                        extfor$iter.next();
                    if (cbde instanceof wilos.model.misc.concretetask.ConcreteTaskDescriptor) {
                        wilos.model.misc.concretetask.ConcreteTaskDescriptor ctd =
                          (wilos.model.misc.concretetask.ConcreteTaskDescriptor)
                            cbde;
                        java.util.HashMap<java.lang.String,
                        java.lang.Object> hm =
                          new java.util.HashMap<java.lang.String,
                        java.lang.Object>();
                        hm.put("ID", ctd.getId());
                        hm.put("name", ctd.getConcreteName());
                        hm.put("selected", false);
                        this.optionalUserConcreteTasksSelectable.add(hm);
                    }
                }
                this.clearOptionalUserConcreteTasksSelectable = false;
            }
        }
        return this.optionalUserConcreteTasksSelectable;
    }
    
    /**
     * setter of optionalUserConcreteTasksSelectable ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @param _optionalUserConcreteTasksSeletacble
     */
    public void setOptionalUserConcreteTasksSeletacble(java.util.ArrayList<java.util.HashMap<java.lang.String,
                                                       java.lang.Object>> _optionalUserConcreteTasksSeletacble) {
        this.optionalUserConcreteTasksSelectable =
          _optionalUserConcreteTasksSeletacble;
    }
    
    /**
     * getter of optionalUserConcreteTasksSelected ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> getOptionalUserConcreteTasksSelected() {
        if (this.clearOptionalUserConcreteTasksSelected) {
            this.optionalUserConcreteTasksSelected.clear();
            this.clearOptionalUserConcreteTasksSelected = false;
        }
        return this.optionalUserConcreteTasksSelected;
    }
    
    /**
     * setter of optionalUserConcreteTasksSelected ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @param _optionalUserConcreteTasksSelected
     */
    public void setOptionalUserConcreteTasksSelected(java.util.ArrayList<java.util.HashMap<java.lang.String,
                                                     java.lang.Object>> _optionalUserConcreteTasksSelected) {
        this.optionalUserConcreteTasksSelected =
          _optionalUserConcreteTasksSelected;
    }
    
    /**
     * setter of mandatoryUserConcreteTasksSelectable ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> getMandatoryUserConcreteTasksSelectable() {
        if (this.clearMandatoryUserConcreteTasksSelectable) {
            this.mandatoryUserConcreteTasksSelectable.clear();
            if (!this.selectedConcreteActivityId.equals("default")) {
                wilos.model.misc.concreteactivity.ConcreteActivity ca =
                  this.concreteActivityService.getConcreteActivity(
                                                 selectedConcreteActivityId);
                java.util.SortedSet<wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement> cbdes =
                  this.concreteActivityService.getConcreteBreakdownElements(ca);
                java.util.Iterator extfor$iter = cbdes.iterator();
                while (extfor$iter.hasNext()) {
                    wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement cbde =
                      (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                        extfor$iter.next();
                    if (cbde instanceof wilos.model.misc.concretetask.ConcreteTaskDescriptor) {
                        wilos.model.misc.concretetask.ConcreteTaskDescriptor ctd =
                          (wilos.model.misc.concretetask.ConcreteTaskDescriptor)
                            cbde;
                        java.util.HashMap<java.lang.String,
                        java.lang.Object> hm =
                          new java.util.HashMap<java.lang.String,
                        java.lang.Object>();
                        hm.put("ID", ctd.getId());
                        hm.put("name", ctd.getConcreteName());
                        hm.put("task", ctd);
                        hm.put("in", false);
                        hm.put("inOptionnal", false);
                        hm.put("out", false);
                        hm.put("flag_in", hm.get("in"));
                        hm.put("flag_inOptionnal", hm.get("inOptionnal"));
                        hm.put("flag_out", hm.get("out"));
                        this.mandatoryUserConcreteTasksSelectable.add(hm);
                    }
                }
                this.clearMandatoryUserConcreteTasksSelectable = false;
            }
        }
        return this.mandatoryUserConcreteTasksSelectable;
    }
    
    /**
     * setter of mandatoryUserConcreteTasksSelectable ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @param _mandatoryUserConcreteTasksSelectable
     */
    public void setMandatoryUserConcreteTasksSelectable(java.util.ArrayList<java.util.HashMap<java.lang.String,
                                                        java.lang.Object>> _mandatoryUserConcreteTasksSelectable) {
        this.mandatoryUserConcreteTasksSelectable =
          _mandatoryUserConcreteTasksSelectable;
    }
    
    /**
     * getter of mandatoryUserConcreteTasksSelected ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public java.util.ArrayList<java.util.HashMap<java.lang.String,
    java.lang.Object>> getMandatoryUserConcreteTasksSelected() {
        if (this.clearMandatoryUserConcreteTasksSelected) {
            this.mandatoryUserConcreteTasksSelected.clear();
            this.clearMandatoryUserConcreteTasksSelected = false;
        }
        return this.mandatoryUserConcreteTasksSelected;
    }
    
    /**
     * setter of mandatoryUserConcreteTasksSelected ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @param _mandatoryUserConcreteTasksSelected
     */
    public void setMandatoryUserConcreteTasksSelected(java.util.ArrayList<java.util.HashMap<java.lang.String,
                                                      java.lang.Object>> _mandatoryUserConcreteTasksSelected) {
        this.mandatoryUserConcreteTasksSelected =
          _mandatoryUserConcreteTasksSelected;
    }
    
    /**
     * getter of selectTaskToAffectToProduct boolean attribute
     * 
     * @return boolean
     */
    public boolean getSelectTaskToAffectToProduct() {
        return this.selectTaskToAffectToProduct;
    }
    
    /**
     * setter of selectTaskToAffectToProduct boolean attribute
     * 
     * @param _selectTaskToAffectToProduct
     */
    public void setSelectTaskToAffectToProduct(boolean _selectTaskToAffectToProduct) {
        if (!_selectTaskToAffectToProduct) {
            this.clearMandatoryUserConcreteTasksSelectable = true;
            this.clearMandatoryUserConcreteTasksSelected = true;
            this.clearOptionalUserConcreteTasksSelectable = true;
            this.clearOptionalUserConcreteTasksSelected = true;
            this.clearProducerConcreteTasksSelectable = true;
            this.clearProducerConcreteTasksSelected = true;
        }
        this.selectTaskToAffectToProduct = _selectTaskToAffectToProduct;
    }
    
    /**
     * this method allows to return the current instance of
     * ConcreteWorkProductDescriptorService
     * 
     * @return ConcreteWorkProductDescriptorService
     */
    public wilos.business.services.misc.concreteworkproduct.ConcreteWorkProductDescriptorService getConcreteWorkProductDescriptorService() {
        return this.concreteWorkProductDescriptorService;
    }
    
    /**
     * this method allows to set the current instance of
     * ConcreteWorkProductDescriptorService
     * 
     * @param _concreteWorkProductDescriptorService
     */
    public void setConcreteWorkProductDescriptorService(wilos.business.services.misc.concreteworkproduct.ConcreteWorkProductDescriptorService _concreteWorkProductDescriptorService) {
        this.concreteWorkProductDescriptorService =
          _concreteWorkProductDescriptorService;
    }
    
    public WorkProductsExpTableBean() { super(); }
}

