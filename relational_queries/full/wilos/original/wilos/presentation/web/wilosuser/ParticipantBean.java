/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) 2007 Clavreul Mickael <mclavreul@wilos-project.org>
 * Copyright (C) 2007 Emilien PERICO <eperico@wilos-project.org>
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

package wilos.presentation.web.wilosuser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService;
import wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementService;
import wilos.business.services.misc.project.ProjectService;
import wilos.business.services.misc.wilosuser.LoginService;
import wilos.business.services.misc.wilosuser.ParticipantService;
import wilos.model.misc.concretemilestone.ConcreteMilestone;
import wilos.model.misc.concreterole.ConcreteRoleDescriptor;
import wilos.model.misc.concretetask.ConcreteTaskDescriptor;
import wilos.model.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElement;
import wilos.model.misc.project.Project;
import wilos.model.misc.wilosuser.Participant;
import wilos.presentation.web.tree.TreeBean;
import wilos.presentation.web.utils.WebCommonService;
import wilos.presentation.web.utils.WebSessionService;
import wilos.resources.LocaleBean;
import wilos.utils.Constantes;
import wilos.utils.Security;

/**
 * Class to subscribe participant Managed-Bean link to participantSubscribe.jspx
 */
public class ParticipantBean {

	/** list of concrete role descriptor headers */
	private List<String> concreteRoleDescriptorHeaders;

	/** list of concrete role descriptors */
	private List<ConcreteRoleDescriptor> concreteRoleDescriptors;

	/** map of concrete role descriptors */
	private HashMap<String, Boolean> concreteRoleDescriptorsMap;

	/** service of participant */
	private ParticipantService participantService;

	/** service of project */
	private ProjectService projectService;

	/** service of login */
	private LoginService loginService;

	/** service of concrete role descriptor */
	private ConcreteRoleDescriptorService concreteRoleDescriptorService;

	/** participant */
	private Participant participant;

	/** confirmation of password */
	private String passwordConfirmation;

	/** current password */
	private String currentPassword;

	/** current affectation name */
	private String affectationName;

	/** list of participant */
	private List<Participant> participantsList;

	/** list of affected projects */
	private List<HashMap<String, Object>> affectedProjectsList;

	/** list of manageable projects */
	private List<HashMap<String, Object>> manageableProjectsList;

	/** the manageable project view selected */
	private String selectManageableProjectView;

	/** the affected project view selected */
	private String selectAffectedProjectView;

	/** formatter simple date format : dd/MM/yyyy */
	private SimpleDateFormat formatter;

	/** view of participant */
	private String participantView;

	/** set participant from session */
	private String isSetParticipantFromSession;

	/** clean bean */
	private String cleanBean;

	/** the logger */
	protected final Log logger = null; //LogFactory.getLog(this.getClass());

	/** popup visible or not */
	private boolean visiblePopup = false;

	/** the project id selected */
	private String selectedProjectId;

	/** display the field of password */
	private boolean displayPasswordEdition;

	/** test delete participant */
	private String testDelete = "RIEN";

	/** panel dynamic for pass */
	private String selectedPanel = "default";

	private ArrayList<SelectItem> roleItem;


	/**
	 * Constructor of participant bean
	 */
	public ParticipantBean() {
		this.participant = new Participant();
		this.affectedProjectsList = new ArrayList<HashMap<String, Object>>();
		this.manageableProjectsList = new ArrayList<HashMap<String, Object>>();
		this.selectManageableProjectView = new String();
		this.formatter = new SimpleDateFormat("dd/MM/yyyy");
		this.displayPasswordEdition = false;
	}

	/**
	 * Method for saving participant data from form
	 */
	public void saveParticipantAction() {
		boolean error = false;

		// test if the fields are correctly completed
		if (this.participant.getName().trim().length() == 0) {
			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.lastnameRequired"));
		}
		if (this.participant.getFirstname().trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean
							.getText("component.forminscription.err.firstnameRequired"));
		}
		if (this.participant.getEmailAddress().trim().length() == 0) {
			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.emailRequired"));
		}
		if (this.participant.getLogin().trim().length() == 0) {
			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.loginRequired"));
		}
		if (this.participant.getPassword().trim().length() == 0) {
			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.passwordRequired"));
		}
		if (this.passwordConfirmation.trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean
							.getText("component.forminscription.err.confirmpasswordRequired"));
		}

		if (!error) {
			if (this.loginService
					.loginExist(this.participant.getLogin().trim())) {
				WebCommonService
						.addErrorMessage(LocaleBean
								.getText("component.forminscription.err.loginalreadyexist"));
			} else {
				this.participantService.saveParticipant(this.participant);
				WebCommonService.addInfoMessage(LocaleBean
						.getText("component.forminscription.success"));
				// confirmation
				WebCommonService.changeContentPage("wilos");
			}
		}
		this.participant = new Participant();
	}

	/**
	 * Cancel the new participant subscription and return to the home page
	 */
	public void cancelSubscription() {
		WebCommonService.changeContentPage("wilos");
		WebCommonService.addInfoMessage(LocaleBean
				.getText("component.forminscription.cancel"));
	}

	/**
	 * Method for updating participant data from form
	 */
	public void updateParticipantAction() {
		boolean error = false;
		// check authentication
		String encryptedCurrentPassword = Security.encode(this.currentPassword);

		if (this.currentPassword.length() == 0) {
			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.passwordRequired"));
		} else if (!this.getParticipantFromSession().getPassword().equals(
				encryptedCurrentPassword)) {

			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.badpassword"));
		}
		// test if the fields are correctly completed
		else if (this.participant.getName().trim().length() == 0) {

			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.lastnameRequired"));
		} else if (this.participant.getFirstname().trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean
							.getText("component.forminscription.err.firstnameRequired"));
		} else if (this.participant.getEmailAddress().trim().length() == 0) {
			// System.out.println("n7");
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.emailRequired"));
			error = true;
		}
		if (!error) {
			this.participant.setPassword(this.getParticipantFromSession()
					.getPassword());
			this.participantService
					.saveParticipantWithoutEncryption(this.participant);
			WebCommonService.addInfoMessage(LocaleBean
					.getText("component.participantUpdate.success"));
			// confirmation
			WebCommonService.changeContentPage("wilos");
		}
	}

	/**
	 * Method for updating participant data from form
	 */
	public void updatePasswordAction() {
		boolean error = false;
		// check authentication
		String encryptedCurrentPassword = Security.encode(this.currentPassword);
		if (this.currentPassword.length() == 0) {
			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.passwordRequired"));
		} else if (!this.getParticipantFromSession().getPassword().equals(
				encryptedCurrentPassword)) {
			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.badpassword"));
		} else if (this.participant.getPassword().trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean
							.getText("component.forminscription.err.newpasswordRequired"));
		} else if (this.passwordConfirmation.trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean
							.getText("component.forminscription.err.confirmpasswordRequired"));
		}

		if (!error) {
			this.participantService.saveParticipant(this.participant);
			WebCommonService.addInfoMessage(LocaleBean
					.getText("component.forminscription.passwordsuccess"));
			// confirmation
			WebCommonService.changeContentPage("wilos");
			this.displayPasswordEdition = false;
		}
		this.participant = new Participant();
	}

	/**
	 * Method for control if the email it's ok
	 * 
	 * @param _context
	 *            context of faces
	 * @param _toValidate
	 *            validate ui component
	 * @param _value
	 *            value of the email
	 * @throws ValidatorException
	 *             exception of validator
	 */
	public void emailValidation(FacesContext _context, UIComponent _toValidate,
			Object _value) throws ValidatorException {
		String enteredEmail = (String) _value;
		// Set the email pattern string
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
		// Match the given string with the pattern
		Matcher m = p.matcher(enteredEmail);
		// Check whether match is found
		boolean matchFound = m.matches();
		if (!matchFound) {
			FacesMessage message = new FacesMessage();
			message.setSummary(LocaleBean
					.getText("component.forminscription.err.invalidemail"));
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
	}

	/**
	 * Set password edited print
	 * 
	 * @return null
	 */
	public String doEditPassword() {
		if (selectedPanel.equalsIgnoreCase("pass")) {
			selectedPanel = "default";
		} else {
			selectedPanel = "pass";
		}
		return null;
	}

	/**
	 * Get password edited print
	 * 
	 * @return null
	 */
	public String getSelectedPanel() {
		return selectedPanel;
	}

	/**
	 * Set the field password undisplay
	 * 
	 * @return null
	 */
	public String cancelAction() {
		selectedPanel = "default";
		return null;
	}

	/**
	 * Send the new password
	 */
	public void sendNewPassword() {

		this.participantsList = this.getParticipantsList();
		for (Participant e : this.participantService.getParticipants()) {
			if ((e.getName()).equalsIgnoreCase(this.participant.getName())
					&& (e.getFirstname()).equalsIgnoreCase(this.participant
							.getFirstname())
					&& (e.getEmailAddress()).equalsIgnoreCase(this.participant
							.getEmailAddress())) {
				this.participant = e;
				this.currentPassword = e.generateNewPassword();
				this.participant.setPassword(this.currentPassword);
				this.participantService
						.saveParticipantWithoutEncryption(this.participant);
				this.participantService.saveParticipant(this.participant);
				this.displayPasswordEdition = false;
				this.participant = new Participant();
			}
		}
	}

	/**
	 * Getter of the list of the concrete role descriptor
	 * 
	 * @return the list of the concrete role descriptor
	 */
	public List<ConcreteRoleDescriptor> getConcreteRoleDescriptors() {
		this.concreteRoleDescriptors = new ArrayList<ConcreteRoleDescriptor>();
		// concreteRoleDescriptors.addAll(this.participantService.getConcreteRoleDescriptorsForAParticipantAndForAProject(_projectId,
		// _login);
		return this.concreteRoleDescriptors;
	}

	/**
	 * Setter of the list of the concrete role descriptor
	 * 
	 * @param _concreteRoleDescriptors
	 *            the new list of concrete role descriptor
	 */
	public void setConcreteRoleDescriptors(
			List<ConcreteRoleDescriptor> _concreteRoleDescriptors) {
		this.concreteRoleDescriptors = _concreteRoleDescriptors;
	}

	/**
	 * Method who compare if the password and the confirm password are the same
	 * 
	 * @param _context
	 *            the faces context
	 * @param _toValidate
	 *            validate uicomponent
	 * @param _value
	 *            password value
	 * @throws ValidatorException
	 *             exception of validator
	 */
	public void passwordEqualValidation(FacesContext _context,
			UIComponent _toValidate, Object _value) throws ValidatorException {
		UIComponent passcomponent = _toValidate.findComponent("equal1");
		String passValue = (String) passcomponent.getAttributes().get("value");

		if (!_value.equals(passValue)) {
			FacesMessage message = new FacesMessage();
			message
					.setSummary(LocaleBean
							.getText("component.forminscription.err.passwordnotequals"));
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
	}

	/**
	 * Getter of participant
	 * 
	 * @return the participant
	 */
	public Participant getParticipant() {
		/*
		 * userID =
		 * (String)this.webSessionService.getAttribute(this.webSessionService
		 * .WILOS_USER_ID); if (userID != null) { this.participant =
		 * this.participantService.getParticipant(userID) ; }
		 */
		return this.participant;
	}

	/**
	 * Setter of participant
	 * 
	 * @param _participant
	 *            the new participant
	 */
	public void setParticipant(Participant _participant) {
		this.participant = _participant;
	}

	/**
	 * Getter of participant service
	 * 
	 * @return the participant service
	 */
	public ParticipantService getParticipantService() {
		return this.participantService;
	}

	/**
	 * Setter of participantService
	 * 
	 * @param _participantService
	 *            the new participant service
	 */
	public void setParticipantService(ParticipantService _participantService) {
		this.participantService = _participantService;
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
	 *            the new project service
	 */
	public void setProjectService(ProjectService _projectService) {
		this.projectService = _projectService;
	}

	/**
	 * Getter of loginService.
	 * 
	 * @return the loginService.
	 */
	public LoginService getLoginService() {
		return this.loginService;
	}

	/**
	 * Setter of loginService.
	 * 
	 * @param _loginService
	 *            the new login service
	 */
	public void setLoginService(LoginService _loginService) {
		this.loginService = _loginService;
	}

	/**
	 * Getter of passwordConfirmation.
	 * 
	 * @return the passwordConfirmation.
	 */
	public String getPasswordConfirmation() {
		return this.passwordConfirmation;
	}

	/**
	 * Setter of passwordConfirmation.
	 * 
	 * @param _passwordConfirmation
	 *            the new password confirmation
	 */
	public void setPasswordConfirmation(String _passwordConfirmation) {
		this.passwordConfirmation = _passwordConfirmation;
	}

	/**
	 * Getter of participantsList.
	 * 
	 * @return the participantsList.
	 */
	public List<Participant> getParticipantsList() {
		this.participantsList = new ArrayList<Participant>();
		participantsList.addAll(this.participantService.getParticipants());
		return this.participantsList;
	}

	/**
	 * Setter of participantsList.
	 * 
	 * @param _participantsList
	 *            ths new participant list
	 */
	public void setParticipantsList(List<Participant> _participantsList) {
		this.participantsList = _participantsList;
	}

	/**
	 * Getter of the list of affected projects
	 * 
	 * @return the list of affected projects
	 */
	public List<HashMap<String, Object>> getAffectedProjectsList() {

		Participant user = getParticipantFromSession();

		if (user instanceof Participant) {
			this.affectedProjectsList = new ArrayList<HashMap<String, Object>>();
			HashMap<Project, Boolean> plist = new HashMap<Project, Boolean>();
			plist = (HashMap<Project, Boolean>) this.participantService
					.getProjectsForAParticipant(user);
			for (Project currentProject : plist.keySet()) {

				HashMap<String, Object> line = new HashMap<String, Object>();

				line.put("project_id", currentProject.getId());
				line.put("affected", plist.get(currentProject));

				// test if the user is affected to this project
				if (!plist.get(currentProject)) {
					// the participant is not affected to this project
					line.put("selectItem", currentProject.getId() + "1");
				} else {
					line.put("selectItem", currentProject.getId() + "2");

				}
				line.put("name", currentProject.getConcreteName());
				line.put("creationDate", formatter.format(currentProject
						.getCreationDate()));
				line.put("launchingDate", formatter.format(currentProject
						.getLaunchingDate()));
				line.put("description", currentProject.getDescription());
				Participant projectManager = currentProject.getProjectManager();
				String name = null;
				if (projectManager == null) {
					// no projectManager
					name = LocaleBean
							.getText("component.table1participantprojectManager.noAffectation");
					line.put("visibleAffectManager", true);
					line.put("displayOptionProjectManager", true);
				} else {
					// there is a project manager
					name = projectManager.getFirstname() + " "
							+ projectManager.getName();
					if (projectManager.getId().equals(user.getId())) {
						// the current user is the project manager
						line.put("selectItem", currentProject.getId() + "3");
						line.put("displayOptionProjectManager", true);
					} else {
						line.put("displayOptionProjectManager", false);
					}
				}
				line.put("projectManagerName", name);
				if (this.checkTasks(line)) {
					line
							.put(
									"activeTasks",
									LocaleBean
											.getText("component.tableparticipantproject.noActiveTasks"));
					line.put("disabled", false);
				} else {
					line
							.put(
									"activeTasks",
									LocaleBean
											.getText("component.tableparticipantproject.activeTasks"));
					line.put("disabled", (Boolean) line.get("affected"));
				}
				this.affectedProjectsList.add(line);
			}
		}
		return affectedProjectsList;
	}

	/**
	 * Setter of the list of affected projects
	 * 
	 * @param affectedProjectsList
	 *            the new list of affected projects
	 */
	public void setAffectedProjectsList(
			List<HashMap<String, Object>> affectedProjectsList) {
		this.affectedProjectsList = affectedProjectsList;
	}

	public Boolean checkTasks(HashMap<String, Object> line) {

		// getting the participant stored into the session
		Participant user = getParticipantFromSession();

		// Retrieving projectService from the context
		FacesContext context = FacesContext.getCurrentInstance();
		this.projectService = (ProjectService) context.getApplication()
				.getVariableResolver().resolveVariable(context,
						"ProjectService");

		Boolean allowed = true;
		String project_id = (String) line.get("project_id");

		/*
		 * Checks if current participant is assigned to any active task. If so,
		 * he is not allowed to unaffect himself as soon as tasks he has started
		 * are not complete.
		 */
		// Retrieve current project and its tasks
		List<ConcreteTaskDescriptor> concreteTasks = this.projectService
				.getProcessService().getTaskDescriptorService()
				.getConcreteTaskDescriptorDao()
				.getAllConcreteTaskDescriptorsForProject(project_id);
		// Checks for tasks state and user affectation
		for (ConcreteTaskDescriptor ctd : concreteTasks) {
			if (ctd.getState().equals(Constantes.State.STARTED)) {
				ConcreteRoleDescriptor crd = ctd
						.getMainConcreteRoleDescriptor();
				Participant participant = this.concreteRoleDescriptorService
						.getParticipant(crd);
				String login = this.participantService
						.getParticipantLogin(participant);
				if (login.equals(user.getLogin())) {
					allowed = false;
				}
			}
		}
		return allowed;
	}

	/**
	 * Method permits to save the affection of projects
	 */
	public void saveProjectsAffectation() {

		// getting the participant stored into the session
		Participant user = getParticipantFromSession();

		// creating a arraylist of hashmaps to represent the affected projects
		HashMap<String, Boolean> affectedProjects = new HashMap<String, Boolean>();

		// iterating onto the collection to create a collection of this form :
		// project_id/Boolean representing the affectation (true for
		// affectation, false for not
		for (HashMap<String, Object> ligne : this.affectedProjectsList) {

			Boolean testAffectation = (Boolean) ligne.get("affected");
			String project_id = (String) ligne.get("project_id");

			if (!testAffectation) {
				// si on se desaffecte du projet courant affich� on clean
				// l'arbre
				if (WebSessionService
						.getAttribute(WebSessionService.PROJECT_ID) != null) {
					if (WebSessionService.getAttribute(
							WebSessionService.PROJECT_ID).equals(project_id)
							&& !testAffectation) {

						// display a message to inform the participant that the
						// current project won't have of project manager.
						this.visiblePopup = true;

						// clean the tree display.
						TreeBean treeBean = (TreeBean) WebCommonService
								.getBean("TreeBean");
						treeBean.cleanTreeDisplay();
					}
				}
			}
			affectedProjects.put(project_id, testAffectation);

		}

		// saving of the new project affectation
		this.participantService.saveProjectsForAParticipant(user,
				affectedProjects);

	}

	/**
	 * Method permits to change the mode
	 * 
	 * @param evt
	 *            the event
	 */
	public void changeModeActionListener(ValueChangeEvent evt) {
		Boolean isForAssignment = (Boolean) evt.getNewValue();
		this.selectedProjectId = (String) WebSessionService
				.getAttribute(WebSessionService.PROJECT_ID);
		if (!isForAssignment) {
			// show the confirmation popup.
			this.visiblePopup = true;
		} else {
			this.participantService.saveProjectForAProjectManager(this
					.getParticipantFromSession().getId(),
					this.selectedProjectId, true);
		}
	}

	/**
	 * Assign action listener
	 * 
	 * @param event
	 *            the event
	 */
	public void assignActionListener(ActionEvent event) {
		// get the participant id into the session.
		this.selectedProjectId = (String) WebSessionService
				.getAttribute(WebSessionService.PROJECT_ID);

		// save.
		this.participantService.saveProjectForAProjectManager(this
				.getParticipantFromSession().getId(), this.selectedProjectId,
				true);
	}

	/**
	 * Method permits to look the popup
	 * 
	 * @param event
	 */
	public void disAssignActionListener(ActionEvent event) {
		this.visiblePopup = true;
	}

	/**
	 * Method permits to disaffect to projects
	 * 
	 * @param _event
	 *            the event
	 */
	public void confirmDisassignment(ActionEvent _event) {
		this.participantService.saveProjectForAProjectManager(this
				.getParticipantFromSession().getId(), this.selectedProjectId,
				false);
		this.visiblePopup = false;
	}

	/**
	 * Methods permit to cancel disassignment
	 * 
	 * @param _event
	 *            the event
	 */
	public void cancelDisassignment(ActionEvent _event) {
		this.visiblePopup = false;
	}

	/**
	 * Return the arraylist to display it into a datatable the arraylist
	 * represent the projects list affected to the participant which have no
	 * projectManager
	 * 
	 * @return the list
	 */
	public List<HashMap<String, Object>> getManageableProjectsList() {

		Participant user = getParticipantFromSession();

		if (user instanceof Participant) {
			this.manageableProjectsList = new ArrayList<HashMap<String, Object>>();
			HashMap<Project, Participant> manageableProjects = (HashMap<Project, Participant>) this.participantService
					.getManageableProjectsForAParticipant(user);

			for (Project currentProject : manageableProjects.keySet()) {

				Participant projectManager = manageableProjects
						.get(currentProject);

				HashMap<String, Object> ligne = new HashMap<String, Object>();

				ligne.put("project_id", currentProject.getId());
				ligne.put("name", currentProject.getConcreteName());
				ligne.put("creationDate", formatter.format(currentProject
						.getCreationDate()));
				ligne.put("launchingDate", formatter.format(currentProject
						.getLaunchingDate()));
				ligne.put("description", currentProject.getDescription());

				if (projectManager == null) {
					// no project manager
					ligne
							.put(
									"projectManagerName",
									LocaleBean
											.getText("component.table1participantprojectManager.noAffectation"));
					ligne.put("projectManager_id", "");
					ligne.put("affected", new Boolean(false));
					ligne.put("hasOtherManager", new Boolean(false));

					this.manageableProjectsList.add(ligne);
				} else {
					// there is a poject manager
					String projectManagerName = projectManager.getFirstname()
							.concat(" " + projectManager.getName());
					ligne.put("projectManager_id", projectManager.getId());
					ligne.put("projectManagerName", projectManagerName);
					if (projectManager.getId().equals(user.getId())) {
						ligne.put("affected", new Boolean(true));
						ligne.put("hasOtherManager", new Boolean(false));
						this.manageableProjectsList.add(ligne);
					} else {
						ligne.put("affected", new Boolean(true));
						ligne.put("hasOtherManager", new Boolean(true));
						this.manageableProjectsList.add(ligne);
					}
				}
			}
		}
		return this.manageableProjectsList;
	}

	/**
	 * Setter of manageable projects list
	 * 
	 * @param manageableProjectsList
	 *            the manageable projects list
	 */
	public void setManageableProjectsList(
			List<HashMap<String, Object>> manageableProjectsList) {
		this.manageableProjectsList = manageableProjectsList;
	}

	/**
	 * This method allows to save a project manager affectation
	 */
	public void saveProjectManagerAffectation() {
		Participant user = getParticipantFromSession();
		Map<String, Boolean> affectedManagedProjects = new HashMap<String, Boolean>();
		for (HashMap<String, Object> ligne : this.manageableProjectsList) {
			// if the current project is not already managed by an other
			// participant
			if (!((Boolean) ligne.get("hasOtherManager"))) {
				Boolean testAffectation = (Boolean) ligne.get("affected");
				String project_id = (String) ligne.get("project_id");
				affectedManagedProjects.put(project_id, testAffectation);
			}
		}
		this.participantService.saveManagedProjectsForAParticipant(user,
				affectedManagedProjects);
	}

	/**
	 * Method which permits the getting of the object Participant which is
	 * stored into the session
	 * 
	 * @return the participant stored into the session
	 */
	private Participant getParticipantFromSession() {
		String userId = (String) WebSessionService
				.getAttribute(WebSessionService.WILOS_USER_ID);

		Participant user = this.participantService.getParticipant(userId);
		return user;
	}

	/**
	 * Set the participant of the participantBean to the participant which is
	 * stored into the session
	 * 
	 * @return ok or null if set participant from session it s ok or not
	 */
	public String getIsSetParticipantFromSession() {
		Participant user = this.getParticipantFromSession();
		if (user != null) {
			this.participant = user;
			this.participant.setPassword("");
			this.isSetParticipantFromSession = "ok";
		} else {
			this.isSetParticipantFromSession = "null";
		}
		return this.isSetParticipantFromSession;
	}

	/**
	 * Setter of isSetParticipantFromSession
	 * 
	 * @param _msg
	 *            the new message
	 */
	public void setIsSetParticipantFromSession(String _msg) {
		this.isSetParticipantFromSession = _msg;
	}

	/**
	 * Getter of the selected manageable project view
	 * 
	 * @return the selected manageable project view
	 */
	public String getSelectManageableProjectView() {
		if (this.getManageableProjectsList().size() == 0) {
			this.selectManageableProjectView = "manageable_no_records_view";
		} else {
			this.selectManageableProjectView = "manageable_records_view";
		}
		return selectManageableProjectView;
	}

	/**
	 * Setter of selectManageableProjectView
	 * 
	 * @param selectManageableProjectView
	 *            the new view
	 */
	public void setSelectManageableProjectView(
			String selectManageableProjectView) {
		this.selectManageableProjectView = selectManageableProjectView;
	}

	/**
	 * Getter of formatter
	 * 
	 * @return the formatter
	 */
	public SimpleDateFormat getFormatter() {
		return formatter;
	}

	/**
	 * Setter of formatter
	 * 
	 * @param formatter
	 *            the new formatter
	 */
	public void setFormatter(SimpleDateFormat formatter) {
		this.formatter = formatter;
	}

	/**
	 * Getter of concreteRoleDescriptorHeaders.
	 * 
	 * @return the concreteRoleDescriptorHeaders.
	 */
	public List<String> getConcreteRoleDescriptorHeaders() {
		this.concreteRoleDescriptorHeaders.addAll(this
				.getConcreteRoleDescriptorsMap().keySet());
		return this.concreteRoleDescriptorHeaders;
	}

	/**
	 * Setter of concreteRoleDescriptorHeaders.
	 * 
	 * @param _concreteRoleDescriptorHeaders
	 *            The concreteRoleDescriptorHeaders to set.
	 */
	public void setConcreteRoleDescriptorHeaders(
			List<String> _concreteRoleDescriptorHeaders) {
		this.concreteRoleDescriptorHeaders = _concreteRoleDescriptorHeaders;
	}

	/**
	 * Getter of concreteRoleDescriptorsMap.
	 * 
	 * @return the concreteRoleDescriptorsMap.
	 */
	public HashMap<String, Boolean> getConcreteRoleDescriptorsMap() {
		// participantService.getConcreteRoleDescriptorsForAProject(String
		// _project_id, String
		// _participant_id);
		this.concreteRoleDescriptorsMap = new HashMap<String, Boolean>();
		List<ConcreteRoleDescriptor> concreteRoleDescriptorsForAParticipant = new ArrayList<ConcreteRoleDescriptor>();
		// concreteRoleDescriptorsForAParticipant.addAll(this.participantService.getConcreteRoleDescriptorsForAParticipant(this.getParticipantFromSession().getLogin()));
		// Ajouter les CRD ki ne sont pas affect�s au participant
		// a recup direct ds le CRDService :D
		for (ConcreteRoleDescriptor concreteRoleDescriptor : concreteRoleDescriptorsForAParticipant) {
			this.concreteRoleDescriptorsMap.put(concreteRoleDescriptor
					.getConcreteName(), true);

		}
		return this.concreteRoleDescriptorsMap;
	}

	/**
	 * Setter of concreteRoleDescriptorsMap.
	 * 
	 * @param _concreteRoleDescriptorsMap
	 *            The concreteRoleDescriptorsMap to set.
	 */
	public void setConcreteRoleDescriptorsMap(
			HashMap<String, Boolean> _concreteRoleDescriptorsMap) {
		this.concreteRoleDescriptorsMap = _concreteRoleDescriptorsMap;
	}

	/**
	 * Getter of selectAffectedProjectView.
	 * 
	 * @return the selectAffectedProjectView.
	 */
	public String getSelectAffectedProjectView() {
		if (this.getAffectedProjectsList().size() == 0) {
			this.selectAffectedProjectView = "affected_no_records_view";
		} else {
			this.selectAffectedProjectView = "affected_records_view";
		}
		return this.selectAffectedProjectView;
	}

	/**
	 * Setter of selectAffectedProjectView.
	 * 
	 * @param _selectAffectedProjectView
	 *            The selectAffectedProjectView to set.
	 */
	public void setSelectAffectedProjectView(String _selectAffectedProjectView) {
		this.selectAffectedProjectView = _selectAffectedProjectView;
	}

	/**
	 * Getter of selectAffectedProjectView.
	 * 
	 * @return the selectAffectedProjectView.
	 */
	public String getParticipantView() {
		if (this.getParticipantsList().size() == 0) {
			this.participantView = "participantView_null";
		} else {
			this.participantView = "participantView_not_null";
		}
		return this.participantView;
	}

	/**
	 * Setter of selectAffectedProjectView.
	 * 
	 * @param _ParticipantView
	 *            The selectAffectedProjectView to set.
	 */
	public void setParticipantView(String _participantView) {
		this.participantView = _participantView;
	}

	/**
	 * Getter of cleanBean.
	 * 
	 * @return the cleanBean.
	 */
	public String getCleanBean() {
		this.participant = new Participant();
		this.cleanBean = "ok";
		return this.cleanBean;
	}

	/**
	 * Setter of cleanBean.
	 * 
	 * @param _cleanBean
	 *            The cleanBean to set.
	 */
	public void setCleanBean(String _cleanBean) {
		this.cleanBean = _cleanBean;
	}

	/**
	 * Getter of the boolean if the popup is visible or not
	 * 
	 * @return true or false visiblePopup
	 */
	public boolean isVisiblePopup() {
		return visiblePopup;
	}

	/**
	 * Setter of visiblePopup
	 * 
	 * @param visiblePopup
	 *            true or false if the popup is visible or not
	 */
	public void setVisiblePopup(boolean visiblePopup) {
		this.visiblePopup = visiblePopup;
	}

	/**
	 * Getter of current password
	 * 
	 * @return the current password
	 */
	public String getCurrentPassword() {
		return currentPassword;
	}

	/**
	 * Setter of the current password
	 * 
	 * @param _currentPassword
	 *            the new current password
	 */
	public void setCurrentPassword(String _currentPassword) {
		this.currentPassword = _currentPassword;
	}

	/**
	 * Getter of isDisplayPasswordEdition
	 * 
	 * @return true if the field of password is display else false
	 */
	public boolean isDisplayPasswordEdition() {
		return displayPasswordEdition;
	}

	/**
	 * Setter displayPasswordEdition
	 * 
	 * @param _displayPasswordEdition
	 *            true or false
	 */
	public void setDisplayPasswordEdition(boolean _displayPasswordEdition) {
		this.displayPasswordEdition = _displayPasswordEdition;
	}

	/**
	 * Getter of the concreteRoleDescriptorService
	 * 
	 * @return the concrete role descriptor service
	 */
	public ConcreteRoleDescriptorService getConcreteRoleDescriptorService() {
		return concreteRoleDescriptorService;
	}

	/**
	 * Setter of concreteRoleDescriptorService
	 * 
	 * @param concreteRoleDescriptorService
	 *            the new concrete role descriptor service
	 */
	public void setConcreteRoleDescriptorService(
			ConcreteRoleDescriptorService concreteRoleDescriptorService) {
		this.concreteRoleDescriptorService = concreteRoleDescriptorService;
	}

	/**
	 * Delete selected participant
	 */
	public void deleteParticipant(ActionEvent _evt) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map map = context.getExternalContext().getRequestParameterMap();
		this.setTestDelete((String) map.get("loginParti"));
		this.visiblePopup = true;
	}

	/**
	 * Test for delete participant
	 * 
	 * @return value of the participant
	 */
	public String getTestDelete() {
		return testDelete;
	}

	/**
	 * Test for delete participant
	 * 
	 * @return value of the participant
	 */
	public void setTestDelete(String deleteParticipant) {
		this.testDelete = deleteParticipant;
	}

	/**
	 * Modify selected participant
	 */
	public void modifyParticipant() {
		FacesContext context = FacesContext.getCurrentInstance();
		Map map = context.getExternalContext().getRequestParameterMap();
		String loginParticipant = (String) map.get("loginParti");
		/** Search the participant on the list */
		for (Participant parti : this.participantsList) {
			if (parti.getLogin() != null) {
				if (parti.getLogin().equals(loginParticipant)) {
					this.participant = parti;
					break;
				}
			}
		}
	}

	/**
	 * This method allow to print the right message when an user want to delete
	 * the selected participant
	 * 
	 * @param event
	 */
	public void confirmDelete(ActionEvent event) {
		this.visiblePopup = false;
	}

	/**
	 * This method fixed the visiblePopup boolean attribute to false
	 * 
	 * @param event
	 */
	public void cancel(ActionEvent event) {
		this.visiblePopup = false;
	}

	/**
	 * Change the status of the user
	 * 
	 * @param evt
	 */
	public void changeListener(ValueChangeEvent evt) {
		String retour = evt.getNewValue().toString();
		Participant user = getParticipantFromSession();
    java.util.Iterator<HashMap<String, Object>> it;
		// Parcours de la liste des projects
		for (HashMap<String, Object> value : this.affectedProjectsList) {
			String projectId = (String) value.get("project_id");
			Project project = this.projectService.getProject(projectId);
			if (retour.contains(projectId)) {
				// R�cuperer le cas du bouton (1 = aucune affectation, 2 =
				// parti, 3 = chef de projet)
				// Le cas est le dernier caract�re de la string retourn�e,
				// le
				// reste correspond � l'id du projet modifi�
				char test = ((String) evt.getNewValue()).charAt(((String) evt
						.getNewValue()).length() - 1);
				boolean newAffectation = false;
				
        
        //switch (test) {          
				//case '1':{
				if (test == '1') {
					// no assignated
					value.put("selectItem", projectId + "1");
					// Supprimer le participant
					value.put("affected", false);
					this.saveProjectsAffectation();
					break;
        }
				//case '2': {
				else if (test == '2') {
					// as participant
					value.put("selectItem", projectId + "2");
					// Ajouter le nouvel participant
					value.put("affected", true);
					if (project.getProjectManager() != null) {
						if (project.getProjectManager().getId().equals(
								user.getId())) {
							// desaffect the project Manager
							this.participantService
									.saveProjectForAProjectManager(
											user.getId(), projectId, false);
						}
					}
					this.saveProjectsAffectation();
					break;
        }
				//case '3': {
				else if (test == '3') {
					// as project manager
					value.put("selectItem", projectId + "3");
					// Ajouter le nouvel participant
					value.put("affected", true);
					this.saveProjectsAffectation();

					newAffectation = true;
					// Refresh de la liste des projets
					this.getManageableProjectsList();

					// Sauvegarde pour l'affectation chef de projet
					// Parcours de la liste des projects
					
          it = this.manageableProjectsList.iterator();
          //for (HashMap<String, Object> value2 : this.manageableProjectsList) {
          while (it.hasNext()) {
            HashMap<String, Object> value2 = it.next();
						String projectId2 = (String) value2.get("project_id");
						if (retour.contains(projectId2)) {
							value2.put("affected", newAffectation);
							this.saveProjectManagerAffectation();
						}
					}
					break;
        }
			}

		}
	}

	/**
	 * Display a message when affectation are ok
	 * 
	 * @param _event
	 */
	public void validAffect() {
		// displaying a message to express the good validation
		WebCommonService.addInfoMessage(LocaleBean
				.getText("component.tableparticipantproject.success"));
	}

}
