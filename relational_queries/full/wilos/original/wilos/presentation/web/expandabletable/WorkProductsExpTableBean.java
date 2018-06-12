/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
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

    public static final String EXPAND_TABLE_ARROW = "images/expandableTable/expand.gif";

    public static final String CONTRACT_TABLE_ARROW = "images/expandableTable/contract.gif";

    public static final String TABLE_LEAF = "images/expandableTable/leaf.gif";

    public static final String INDENTATION_STRING = "|- - - ";

    private List<HashMap<String, Object>> expTableContentWorkProduct = new ArrayList<HashMap<String, Object>>();;

    protected HashMap<String, Boolean> isExpandedTableWorkProduct = new HashMap<String, Boolean>();

    private HashMap<String, String> indentationContentWorkProduct = new HashMap<String, String>();;

    private ProcessService processService;

    private ProjectService projectService;

    private ActivityService activityService;

    private ConcreteWorkProductDescriptorService concreteWorkProductDescriptorService;

    private WorkProductDescriptorService workProductDescriptorService;

    private ConcreteTaskDescriptorService concreteTaskDescriptorService;

    private boolean isVisibleWorkProductInstanciationPanel = false;

    private boolean allConcreteActivitiesAreFinishedWorkProduct = false;

    private String viewedProcessId = "";

    private String selectedProcessId = "default";

    private boolean isVisibleNewWorkProductPanel = false;
    
    private String newWorkProductName;
    
    private String selectedRoleDescriptorId = "default";
    
    private String selectedConcreteActivityId = "default";
    
    private String newWorkProductDescription;
    
    private boolean addWorkProductRendered = false;
    
    private ConcreteActivityService concreteActivityService;
    
    private RoleDescriptorService roleDescriptorService;
    
    private boolean visibleRoleComboBox = false;
    
    private String activityEntryState = "";
    
    private String activityExitState = "";

    private ArrayList<HashMap<String, Object>> producerConcreteTasksSelectable = new ArrayList<HashMap<String, Object>>();
    private ArrayList<HashMap<String, Object>> producerConcreteTasksSelected = new ArrayList<HashMap<String, Object>>();

    private ArrayList<HashMap<String, Object>> optionalUserConcreteTasksSelectable = new ArrayList<HashMap<String, Object>>();
    private ArrayList<HashMap<String, Object>> optionalUserConcreteTasksSelected = new ArrayList<HashMap<String, Object>>();

    private ArrayList<HashMap<String, Object>> mandatoryUserConcreteTasksSelectable = new ArrayList<HashMap<String, Object>>();
    private ArrayList<HashMap<String, Object>> mandatoryUserConcreteTasksSelected = new ArrayList<HashMap<String, Object>>();

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
    public void saveWorkProductInstanciation(ActionEvent _event) {

	String projectId = (String) WebSessionService
		.getAttribute(WebSessionService.PROJECT_ID);

	if ((projectId != null) && (!this.selectedProcessId.equals("default"))) {
	    Project project = projectService.getProject(projectId);
	    if (project != null) {
		Process process = processService.getProcessDao().getProcess(
			selectedProcessId);
		if (process != null) {
		    this.workProductDescriptorService
			    .workProductsInstanciation(project, process,
				    expTableContentWorkProduct);
		}
	    }
	    this.expTableContentWorkProduct.clear();

	    /* Displays info message */
	    WebCommonService.addInfoMessage(LocaleBean
		    .getText("component.instanciation.instanciatedMessage"));
	    TreeBean tb = (TreeBean) WebCommonService.getBean("TreeBean");
	    tb.rebuildProjectTree();
	}
    }

    /**
     * getter of expTableContent HashMap List attribute
     * 
     * @return the expTableContent
     */
    public List<HashMap<String, Object>> getExpTableContentWorkProduct() {
	if (!this.selectedProcessId.equals("default")) {
	    Process process = this.processService
		    .getProcess(this.selectedProcessId);
	    if (!this.viewedProcessId.equals(process.getId())
		    || this.expTableContentWorkProduct.isEmpty()) {
		this.viewedProcessId = process.getId();
		this.isExpandedTableWorkProduct.clear();
		this.expTableContentWorkProduct.clear();
		this.indentationContentWorkProduct.clear();

		List<HashMap<String, Object>> lines = this
			.getExpTableLineContent(process);
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
    private List<HashMap<String, Object>> getExpTableLineContent(
	    Process _process) {

	List<HashMap<String, Object>> lines = new ArrayList<HashMap<String, Object>>();
	// String indentationString = "";

	Project project = this.projectService
		.getProject((String) WebSessionService
			.getAttribute(WebSessionService.PROJECT_ID));
	for (WorkProductDescriptor wpd : this.processService
		.getWorkProductDescriptorsFromProcess(_process)) {

	    HashMap<String, Object> hm = new HashMap<String, Object>();
	    int nb = this.processService
		    .getWorkProductDescriptorsWithTheSameNameNumberInProcess(
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
	    int nbcwpd = this.projectService
		    .getConcreteWorkProductDescriptorsFromProject(project)
		    .size();
	    if (nbcwpd > 0) {
		hm.put("nbOccurences", new Integer(0));
	    } else {
		hm.put("nbOccurences", new Integer(1));
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

	FacesContext context = FacesContext.getCurrentInstance();
	Map map = context.getExternalContext().getRequestParameterMap();
	String workProductId = (String) map.get("workProductId");
	String workProductName = (String) map.get("workProductName");

	// this.needIndentation = true;
	Project project = this.projectService
		.getProject((String) WebSessionService
			.getAttribute(WebSessionService.PROJECT_ID));

	Process process = this.projectService.getProcessFromProject(project);

	ArrayList<HashMap<String, Object>> tmp = new ArrayList<HashMap<String, Object>>();
	tmp.addAll(this.expTableContentWorkProduct);
	int index;

	for (HashMap<String, Object> hashMap : tmp) {
	    if (hashMap.get("id").equals(workProductId)) {
		if (hashMap.get("nodeType").equals("node")) {
		    hashMap.put("expansionImage", EXPAND_TABLE_ARROW);
		    hashMap.put("isDisabled", true);
		    index = this.expTableContentWorkProduct.indexOf(hashMap);
		    this.expTableContentWorkProduct.addAll(index + 1, this.projectService
			    .getDifferentPathsOfWorkProductDescriptorInProcess(
				    process, workProductName));
		    return;
		}
	    }
	}
    }

    /**
     * Utility method to remove all child nodes from the parent dataTable list.
     */
    private void contractNodeActionWorkProduct() {
	FacesContext context = FacesContext.getCurrentInstance();
	Map map = context.getExternalContext().getRequestParameterMap();
	String workProductId = (String) map.get("workProductId");
	String workProductName = (String) map.get("workProductName");

	ArrayList<HashMap<String, Object>> parentList = new ArrayList<HashMap<String, Object>>();
	parentList.addAll(this.expTableContentWorkProduct);

	/* Removes element which we want to contract from the parent list */
	for (HashMap<String, Object> currentElement : this.expTableContentWorkProduct) {

	    if (currentElement.get("id").equals(workProductId)
		    && currentElement.get("nodeType").equals("node")) {
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
    private void deleteChildrenWorkProduct(String _parentName,
	    ArrayList<HashMap<String, Object>> _parentList) {
	for (HashMap<String, Object> child : _parentList) {
	    if (child.get("parentId").equals(_parentName)) {
		this.expTableContentWorkProduct.remove(child);
		deleteChildrenWorkProduct((String) child.get("id"), _parentList);
	    }
	    if (child.get("id").equals(_parentName)) {
		child.put("expansionImage", CONTRACT_TABLE_ARROW);
		this.isExpandedTableWorkProduct.put((String) child.get("id"), false);
	    }
	}
    }

    /**
     * Toggles the expanded state of this ConcreteBreakDownElement.
     * 
     * @param event
     */
    public void toggleSubGroupActionWorkProduct(ActionEvent event) {
	FacesContext context = FacesContext.getCurrentInstance();
	Map map = context.getExternalContext().getRequestParameterMap();
	String workProductId = (String) map.get("workProductId");

	// toggle expanded state
	Boolean b = this.isExpandedTableWorkProduct.get(workProductId);
	if (b == null) {
	    this.isExpandedTableWorkProduct.put(workProductId, true);
	    b = this.isExpandedTableWorkProduct.get(workProductId);
	} else {
	    if (b) {
		b = false;
	    } else {
		b = true;
	    }
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
    public void setExpTableContentWorkProduct(
	    ArrayList<HashMap<String, Object>> _expTableContent) {
	this.expTableContentWorkProduct = _expTableContent;
    }

    /**
     * getter of isExpanded HashMap attribute
     * 
     * @return the isExpanded
     */
    public HashMap<String, Boolean> getIsExpandedTableWorkProduct() {
	return this.isExpandedTableWorkProduct;
    }

    /**
     * setter of isExpanded HashMap attribute
     * 
     * @param _isExpanded
     *                the isExpanded to set
     */
    public void setIsExpandedTableWorkProduct(HashMap<String, Boolean> _isExpanded) {
	this.isExpandedTableWorkProduct = _isExpanded;
    }

    /**
     * this method allows to return the current instance of ActivityService
     * 
     * @return the activityService
     */
    public ActivityService getActivityService() {
	return this.activityService;
    }

    /**
     * this method allows to set the current instance of ActivityService
     * 
     * @param _activityService
     *                the activityService to set
     */
    public void setActivityService(ActivityService _activityService) {
	this.activityService = _activityService;
    }

    /**
     * this method allows to return the current instance of ProcessService
     * 
     * @return the processService
     */
    public ProcessService getProcessService() {
	return this.processService;
    }

    /**
     * this method allows to set the current instance of ProcessService
     * 
     * @param _processService
     *                the processService to set
     */
    public void setProcessService(ProcessService _processService) {
	this.processService = _processService;
    }

    /**
     * this method allows to return the current instance of ProjectService
     * 
     * @return the projectService
     */
    public ProjectService getProjectService() {
	return this.projectService;
    }

    /**
     * this method allows to set the current instance of ProjectService
     * 
     * @param _projectService
     *                the projectService to set
     */
    public void setProjectService(ProjectService _projectService) {
	this.projectService = _projectService;
    }

    /**
     * getter of selectedProcessId String attribute
     * 
     * @return the selectedProcessId
     */
    public String getSelectedProcessId() {
	return this.selectedProcessId;
    }

    /**
     * setter of selectedProcessId String attribute
     * 
     * @param _selectedProcessGuid
     *                the selectedProcessId to set
     */
    public void setSelectedProcessId(String _selectedProcessGuid) {
	this.selectedProcessId = _selectedProcessGuid;
    }

    /**
     * getter of indentationContent HashMap attribute
     * 
     * @return the indentationContent
     */
    public HashMap<String, String> getIndentationContentWorkProduct() {
	return this.indentationContentWorkProduct;
    }

    /**
     * setter of indentationContent HashMap attribute
     * 
     * @param _indentationContent
     *                the indentationContent to set
     */
    public void setIndentationContentWorkProduct(
	    HashMap<String, String> _indentationContent) {
	this.indentationContentWorkProduct = _indentationContent;
    }

    /**
     * this method allows to return the current instance of
     * WorkProductDescriptorService
     * 
     * @return the workProductDescriptorService
     */
    public WorkProductDescriptorService getWorkProductDescriptorService() {
	return this.workProductDescriptorService;
    }

    /**
     * this method allows to set the current instance of
     * WorkProductDescriptorService
     * 
     * @param _workProductDescriptorService
     *                the workProductDescriptorService to set
     */
    public void setWorkProductDescriptorService(
	    WorkProductDescriptorService _workProductDescriptorService) {
	this.workProductDescriptorService = _workProductDescriptorService;
    }

    /**
     * getter of isVisibleWorkProductInstanciationPanel boolean attribute
     * 
     * @return the isVisibleWorkProductInstanciationPanel
     */
    public boolean getIsVisibleWorkProductInstanciationPanel() {

	Project project = this.projectService
		.getProject((String) WebSessionService
			.getAttribute(WebSessionService.PROJECT_ID));
	Process process = this.projectService.getProcessFromProject(project);
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
    public void setVisibleWorkProductInstanciationPanel(
	    boolean _isVisibleWorkProductInstanciationPanel) {
	this.isVisibleWorkProductInstanciationPanel = _isVisibleWorkProductInstanciationPanel;
    }

    /**
     * getter of isVisibleNewWorkProductPanel boolean attribute
     * 
     * @return the isVisibleNewWorkProductPanel
     */
    public boolean getIsVisibleNewWorkProductPanel() {
	Project project = this.projectService
		.getProject((String) WebSessionService
			.getAttribute(WebSessionService.PROJECT_ID));
	Process process = this.projectService.getProcessFromProject(project);
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
    public String getNewWorkProductName() {
	return this.newWorkProductName;
    }

    /**
     * setter of newWorkProductName String attribute
     * 
     * @param _newWorkProductName
     *                the newWorkProductName to set
     */
    public void setNewWorkProductName(String _newWorkProductName) {
	this.newWorkProductName = _newWorkProductName;
    }

    /**
     * ChangeListener on the Combobox including the roles
     * 
     * @param evt
     *                an ValueChangeEvent
     */
    public void changeRolesListener(ValueChangeEvent evt) {
	this.selectedRoleDescriptorId = (String) evt.getNewValue();
	this.addWorkProductRendered = !(this.selectedRoleDescriptorId
		.equals("default"))
		&& !(this.selectedConcreteActivityId.equals("default"));
    }

    /**
     * Give all the roles save in the database for the given process
     * 
     * @return the List<SelectItem> of roleDescriptor
     */
    public List<SelectItem> getRoles() {

		List<SelectItem> rolesList = new ArrayList<SelectItem>();

		rolesList
				.add(new SelectItem(
						"default",
						LocaleBean
								.getText("component.project.workproductsinstanciation.roleComboBoxDefaultChoice")));

		Project project = this.projectService
				.getProject((String) WebSessionService
						.getAttribute(WebSessionService.PROJECT_ID));

		if (project != null) {
			Process process = project.getProcess();
			if (process != null) {

				ConcreteActivity cact = this.concreteActivityService
						.getConcreteActivity(this.selectedConcreteActivityId);
				if (this.selectedConcreteActivityId.equals(project.getId())) {
					SortedSet<BreakdownElement> bdes = this.processService
							.getAllBreakdownElements(process);
					for (BreakdownElement bde : bdes) {
						if (bde instanceof RoleDescriptor) {
							RoleDescriptor rd = (RoleDescriptor) bde;
							rolesList.add(new SelectItem(rd.getId(), rd
									.getPresentationName()));
						}
					}
				}

				if (!this.selectedConcreteActivityId.equals("default")
						&& !this.selectedConcreteActivityId.equals(project
								.getId())) {
					SortedSet<BreakdownElement> bdEs = this.activityService
							.getAllBreakdownElements(cact.getActivity());
					for (BreakdownElement bde : bdEs) {
						if (bde instanceof RoleDescriptor) {
							rolesList.add(new SelectItem(bde.getId(), bde
									.getPresentationName()));
						}
					}
				}
				rolesList
						.add(new SelectItem(
								"null",
								LocaleBean
										.getText("component.project.workproductsinstanciation.noRole")));
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
    public void changeConcreteActivitiesListener(ValueChangeEvent evt) {

	this.clearProducerConcreteTasksSelectable = true;
	this.clearProducerConcreteTasksSelected = true;

	this.clearOptionalUserConcreteTasksSelectable = true;
	this.clearOptionalUserConcreteTasksSelected = true;

	this.clearMandatoryUserConcreteTasksSelectable = true;
	this.clearMandatoryUserConcreteTasksSelected = true;

	this.selectedConcreteActivityId = (String) evt.getNewValue();
	this.addWorkProductRendered = !(this.selectedRoleDescriptorId
		.equals("default"))
		&& !(this.selectedConcreteActivityId.equals("default"));
	this.visibleRoleComboBox = !(this.selectedConcreteActivityId
		.equals("default"));

    }

    /**
     * getter of selectedRoleDescriptorId String attribute
     * 
     * @return the selectedRoleDescriptorId
     */
    public String getSelectedRoleDescriptorId() {
	return selectedRoleDescriptorId;
    }

    /**
     * setter of selectedRoleDescriptorId String attribute
     * 
     * @param _selectedRoleDescriptorId
     *                the selectedRoleDescriptorId to set
     */
    public void setSelectedRoleDescriptorId(String _selectedRoleDescriptorId) {
	selectedRoleDescriptorId = _selectedRoleDescriptorId;
    }

    /**
     * getter of selectedConcreteActivityId String attribute
     * 
     * @return the selectedConcreteActivityId
     */
    public String getSelectedConcreteActivityId() {
	return selectedConcreteActivityId;
    }

    /**
     * setter of selectedConcreteActivityId String attribute
     * 
     * @param _selectedConcreteActivityId
     *                the selectedConcreteActivityId to set
     */
    public void setSelectedConcreteActivityId(String _selectedConcreteActivityId) {
	selectedConcreteActivityId = _selectedConcreteActivityId;
    }

    /**
     * getter of newWorkProductDescription String attribute
     * 
     * @return the newWorkProductDescription
     */
    public String getNewWorkProductDescription() {
	return newWorkProductDescription;
    }

    /**
     * setter of newWorkProductDescription String attribute
     * 
     * @param _newWorkProductDescription
     *                the newWorkProductDescription to set
     */
    public void setNewWorkProductDescription(String _newWorkProductDescription) {
	newWorkProductDescription = _newWorkProductDescription;
    }

    /**
     * getter of addWorkProductRendered boolean attribute
     * 
     * @return the addWorkProductRendered
     */
    public boolean isAddWorkProductRendered() {
	return addWorkProductRendered;
    }

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
    public void setVisibleNewWorkProductPanel(
	    boolean _isVisibleNewWorkProductPanel) {
	isVisibleNewWorkProductPanel = _isVisibleNewWorkProductPanel;
    }

    /**
     * this method returns all the not finishing concreteActivities for the
     * selected Project
     * 
     * @return List<SelectItem> of concreteActivities
     */
    public List<SelectItem> getConcreteActivities() {

	List<SelectItem> activityList = new ArrayList<SelectItem>();

	activityList
		.add(new SelectItem(
			"default",
			LocaleBean
				.getText("component.project.workproductsinstanciation.actComboBoxDefaultChoice")));

	Project project = this.projectService
		.getProject((String) WebSessionService
			.getAttribute(WebSessionService.PROJECT_ID));

	if (project != null) {
			for (ConcreteActivity cact : this.concreteActivityService
					.getConcreteActivitiesFromProject(project)) {

				if (!(cact.getState().equals(State.FINISHED))) {
					activityList.add(new SelectItem(cact.getId(), cact
							.getConcreteName()));
				}
			}
			if (!(project.getState().equals(State.FINISHED))) {
				activityList.add(new SelectItem(project.getId(), project
						.getConcreteName()));
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
    public String createOutOfProcessWorkProductActionListener() {
		if (this.newWorkProductName == "") {
			WebCommonService
					.addErrorMessage(LocaleBean
							.getText("component.project.workproductsinstanciation.noNameError"));
		} else if (this.selectedConcreteActivityId.equals("default")) {
			WebCommonService
					.addErrorMessage(LocaleBean
							.getText("component.project.workproductsinstanciation.noActivityDescriptorSelected"));
		} else if (this.newWorkProductDescription == "") {
			WebCommonService
					.addErrorMessage(LocaleBean
							.getText("component.project.workproductsinstanciation.noDescriptionError"));
		} else if (this.selectedRoleDescriptorId.equals("default")) {
			WebCommonService
					.addErrorMessage(LocaleBean
							.getText("component.project.workproductsinstanciation.noRoleDescriptorSelected"));
		} else {
			Project project = this.projectService
					.getProject((String) WebSessionService
							.getAttribute(WebSessionService.PROJECT_ID));

			if (!this.selectedRoleDescriptorId.equals("default")
					&& !this.selectedConcreteActivityId.equals("default")) {
				//TODO
				System.out.println("******** "+this.selectedRoleDescriptorId);
				RoleDescriptor rd = this.roleDescriptorService
						.getRoleDescriptor(this.selectedRoleDescriptorId);
				ConcreteActivity cact = this.concreteActivityService
						.getConcreteActivity(this.selectedConcreteActivityId);

				// if the selected activity is the project
				if (project.getId().equals(cact.getId())) {
					cact = null;
				}

				ArrayList<String> inputConcreteTasksIDs = new ArrayList<String>();
				ArrayList<String> inputOptionnalConcreteTasksIDs = new ArrayList<String>();
				ArrayList<String> outputConcreteTasksIDs = new ArrayList<String>();
				
				for (HashMap hm : this.mandatoryUserConcreteTasksSelectable){
					if (! hm.get("in").equals(hm.get("flag_in"))){
						// maj ctd en entree pour cwpd;
						inputConcreteTasksIDs.add((String) hm.get("ID"));
					}
					if (! hm.get("out").equals(hm.get("flag_out"))){
						// maj ctd en sortie pour cwpd;
						outputConcreteTasksIDs.add((String)hm.get("ID"));
					}
					if (! hm.get("inOptionnal").equals(hm.get("flag_inOptionnal"))){
						// maj ctd en entree otionnelle pour cwpd;
						inputOptionnalConcreteTasksIDs.add((String)hm.get("ID"));
					}
				}

				if (this.workProductDescriptorService.createWorkProduct(
						this.newWorkProductName,
						this.newWorkProductDescription, project, rd, cact,
						this.activityEntryState, this.activityExitState,
						outputConcreteTasksIDs, inputOptionnalConcreteTasksIDs,
						inputConcreteTasksIDs)) {
					WebCommonService
							.addInfoMessage(LocaleBean
									.getText("component.project.workproductsinstanciation.creationValidation"));
					this.setSelectTaskToAffectToProduct(false);
				} else {
					WebCommonService
							.addInfoMessage(LocaleBean
									.getText("component.project.workproductsinstanciation.creationError"));
				}

				TreeBean treeBean = (TreeBean) WebCommonService
						.getBean("TreeBean");

				treeBean.rebuildProjectTree();
				ProjectAdvancementBean pab = (ProjectAdvancementBean) WebCommonService
						.getBean("ProjectAdvancementBean");
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
    public ConcreteActivityService getConcreteActivityService() {
	return concreteActivityService;
    }

    /**
     * this method allows to set the current instance of ConcreteActivityService
     * 
     * @param _concreteActivityService
     *                the concreteActivityService to set
     */
    public void setConcreteActivityService(
	    ConcreteActivityService _concreteActivityService) {
	concreteActivityService = _concreteActivityService;
    }

    /**
     * this method allows to return the current instance of
     * RoleDescriptorService
     * 
     * @return the roleDescriptorService
     */
    public RoleDescriptorService getRoleDescriptorService() {
	return roleDescriptorService;
    }

    /**
     * this method allows to set the current instance of RoleDescriptorService
     * 
     * @param _roleDescriptorService
     *                the roleDescriptorService to set
     */
    public void setRoleDescriptorService(
	    RoleDescriptorService _roleDescriptorService) {
	roleDescriptorService = _roleDescriptorService;
    }

    /**
     * getter of visibleRoleComboBox boolean attribute
     * 
     * @return the visibleRoleComboBox
     */
    public boolean isVisibleRoleComboBox() {
	return visibleRoleComboBox;
    }

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
	Project project = this.projectService
		.getProject((String) WebSessionService
			.getAttribute(WebSessionService.PROJECT_ID));
	
	if(this.concreteActivityService
		.getConcreteActivitiesFromProject(project).size() != 0) {
        
        	for (ConcreteActivity cact : this.concreteActivityService
        		.getConcreteActivitiesFromProject(project)) {
        	    if (cact.getState().equals(State.FINISHED)) {
        		numberOfFinishedActivity++;
        	    }
        	}
        	if (numberOfFinishedActivity == this.concreteActivityService
        		.getConcreteActivitiesFromProject(project).size()) {
        	    this.allConcreteActivitiesAreFinishedWorkProduct = true;
        	} else {
        	    this.allConcreteActivitiesAreFinishedWorkProduct = false;
        	}
	}
	return this.allConcreteActivitiesAreFinishedWorkProduct;
    }

    /**
     * setter of allConcreteActivitiesAreFinished boolean attribute
     * 
     * @param _allConcreteActivitiesAreFinished
     */
    public void setAllConcreteActivitiesAreFinished(
	    boolean _allConcreteActivitiesAreFinished) {
	this.allConcreteActivitiesAreFinishedWorkProduct = _allConcreteActivitiesAreFinished;
    }

    /**
     * this method allows to control the value of producerConcreteTasksSelected
     * if the row corresponding on the event is already in the List, it will be
     * remove. else it will be added
     * 
     * @param event
     *                an RowSelectorEvent
     */
    public void producerConcreteTaskRowSelectionAction(RowSelectorEvent event) {
	HashMap<String, Object> row = this.producerConcreteTasksSelectable
		.get(event.getRow());
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
    public void optionalUserConcreteTaskRowSelectionAction(
	    RowSelectorEvent event) {
	HashMap<String, Object> row = this.optionalUserConcreteTasksSelectable
		.get(event.getRow());
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
    public void mandatoryUserConcreteTaskRowSelectionAction(
	    RowSelectorEvent event) {
	HashMap<String, Object> row = this.mandatoryUserConcreteTasksSelectable
		.get(event.getRow());
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
    public ConcreteTaskDescriptorService getConcreteTaskDescriptorService() {
	return this.concreteTaskDescriptorService;
    }

    /**
     * this method allows to set the current instance of
     * ConcreteTaskDescriptorService
     * 
     * @param _concreteTaskDescriptorService
     */
    public void setConcreteTaskDescriptorService(
	    ConcreteTaskDescriptorService _concreteTaskDescriptorService) {
	this.concreteTaskDescriptorService = _concreteTaskDescriptorService;
    }

    /**
     * getter of activitySelected boolean attribute
     * 
     * @return boolean
     */
    public boolean getActivitySelected() {
	this.activitySelected = (!this.selectedConcreteActivityId
		.equals("default"));
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
    public String getActivityEntryState() {
	return this.activityEntryState;
    }

    /**
     * setter of activityEntryState String attribute
     * 
     * @param _activityEntryState
     */
    public void setActivityEntryState(String _activityEntryState) {
	this.activityEntryState = _activityEntryState;
    }

    /**
     * getter of activityExitState String attribute
     * 
     * @return String
     */
    public String getActivityExitState() {
	return this.activityExitState;
    }

    /**
     * setter of activityExitState String attribute
     * 
     * @param _activityExitState
     */
    public void setActivityExitState(String _activityExitState) {
	this.activityExitState = _activityExitState;
    }

    /**
     * getter of producerConcreteTasksSelectable ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public ArrayList<HashMap<String, Object>> getProducerConcreteTasksSelectable() {
	if (this.clearProducerConcreteTasksSelectable) {
	    this.producerConcreteTasksSelectable.clear();
	    if (!(this.selectedConcreteActivityId.equals("default"))) {
		ConcreteActivity ca = this.concreteActivityService
			.getConcreteActivity(selectedConcreteActivityId);
		SortedSet<ConcreteBreakdownElement> cbdes = this.concreteActivityService
			.getConcreteBreakdownElements(ca);
		for (ConcreteBreakdownElement cbde : cbdes) {
		    if (cbde instanceof ConcreteTaskDescriptor) {
			ConcreteTaskDescriptor ctd = (ConcreteTaskDescriptor) cbde;
			HashMap<String, Object> hm = new HashMap<String, Object>();
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
    public void setProducerConcreteTasksSelectable(
	    ArrayList<HashMap<String, Object>> _producerConcreteTasksSelectable) {
	this.producerConcreteTasksSelectable = _producerConcreteTasksSelectable;
    }

    /**
     * getter of producerConcreteTasksSelected ArrayList<HashMap> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public ArrayList<HashMap<String, Object>> getProducerConcreteTasksSelected() {
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
    public void setProducerConcreteTasksSelected(
	    ArrayList<HashMap<String, Object>> _producerConcreteTasksSelected) {
	this.producerConcreteTasksSelected = _producerConcreteTasksSelected;
    }

    /**
     * getter of optionalUserConcreteTasksSelectable ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public ArrayList<HashMap<String, Object>> getOptionalUserConcreteTasksSelectable() {
	if (this.clearOptionalUserConcreteTasksSelectable) {
	    this.optionalUserConcreteTasksSelectable.clear();
	    if (!(this.selectedConcreteActivityId.equals("default"))) {
		ConcreteActivity ca = this.concreteActivityService
			.getConcreteActivity(selectedConcreteActivityId);
		SortedSet<ConcreteBreakdownElement> cbdes = this.concreteActivityService
			.getConcreteBreakdownElements(ca);
		for (ConcreteBreakdownElement cbde : cbdes) {
		    if (cbde instanceof ConcreteTaskDescriptor) {
			ConcreteTaskDescriptor ctd = (ConcreteTaskDescriptor) cbde;
			HashMap<String, Object> hm = new HashMap<String, Object>();
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
    public void setOptionalUserConcreteTasksSeletacble(
	    ArrayList<HashMap<String, Object>> _optionalUserConcreteTasksSeletacble) {
	this.optionalUserConcreteTasksSelectable = _optionalUserConcreteTasksSeletacble;
    }

    /**
     * getter of optionalUserConcreteTasksSelected ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public ArrayList<HashMap<String, Object>> getOptionalUserConcreteTasksSelected() {
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
    public void setOptionalUserConcreteTasksSelected(
	    ArrayList<HashMap<String, Object>> _optionalUserConcreteTasksSelected) {
	this.optionalUserConcreteTasksSelected = _optionalUserConcreteTasksSelected;
    }

    /**
     * setter of mandatoryUserConcreteTasksSelectable ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public ArrayList<HashMap<String, Object>> getMandatoryUserConcreteTasksSelectable() {
		if (this.clearMandatoryUserConcreteTasksSelectable) {
			this.mandatoryUserConcreteTasksSelectable.clear();
			if (!(this.selectedConcreteActivityId.equals("default"))) {
				ConcreteActivity ca = this.concreteActivityService
						.getConcreteActivity(selectedConcreteActivityId);
				SortedSet<ConcreteBreakdownElement> cbdes = this.concreteActivityService
						.getConcreteBreakdownElements(ca);
				for (ConcreteBreakdownElement cbde : cbdes) {
					if (cbde instanceof ConcreteTaskDescriptor) {
						ConcreteTaskDescriptor ctd = (ConcreteTaskDescriptor) cbde;
						HashMap<String, Object> hm = new HashMap<String, Object>();
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
    public void setMandatoryUserConcreteTasksSelectable(
	    ArrayList<HashMap<String, Object>> _mandatoryUserConcreteTasksSelectable) {
	this.mandatoryUserConcreteTasksSelectable = _mandatoryUserConcreteTasksSelectable;
    }

    /**
     * getter of mandatoryUserConcreteTasksSelected ArrayList<HashMap<String,
     * Object>> attribute
     * 
     * @return ArrayList<HashMap<String, Object>>
     */
    public ArrayList<HashMap<String, Object>> getMandatoryUserConcreteTasksSelected() {
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
    public void setMandatoryUserConcreteTasksSelected(
	    ArrayList<HashMap<String, Object>> _mandatoryUserConcreteTasksSelected) {
	this.mandatoryUserConcreteTasksSelected = _mandatoryUserConcreteTasksSelected;
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
    public void setSelectTaskToAffectToProduct(
	    boolean _selectTaskToAffectToProduct) {

	if (!(_selectTaskToAffectToProduct)) {
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
    public ConcreteWorkProductDescriptorService getConcreteWorkProductDescriptorService() {
	return this.concreteWorkProductDescriptorService;
    }

    /**
     * this method allows to set the current instance of
     * ConcreteWorkProductDescriptorService
     * 
     * @param _concreteWorkProductDescriptorService
     */
    public void setConcreteWorkProductDescriptorService(
	    ConcreteWorkProductDescriptorService _concreteWorkProductDescriptorService) {
	this.concreteWorkProductDescriptorService = _concreteWorkProductDescriptorService;
    }
}
