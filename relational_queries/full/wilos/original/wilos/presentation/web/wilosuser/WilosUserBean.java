package wilos.presentation.web.wilosuser;

import java.util.ArrayList;
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
import javax.mail.MessagingException;

import wilos.business.services.misc.project.AffectedtoService;
import wilos.business.services.misc.wilosuser.LoginService;
import wilos.business.services.misc.wilosuser.ParticipantService;
import wilos.business.services.misc.wilosuser.RoleService;
import wilos.business.services.misc.wilosuser.WilosUserService;
import wilos.model.misc.wilosuser.Participant;
import wilos.model.misc.wilosuser.Role;
import wilos.model.misc.wilosuser.WilosUser;
import wilos.presentation.web.utils.SendMail;
import wilos.presentation.web.utils.WebCommonService;
import wilos.presentation.web.utils.WebSessionService;
import wilos.resources.LocaleBean;
import wilos.utils.Security;

/**
 * This Bean represent wilos user
 */
public class WilosUserBean {

	/** The user */
	private WilosUser user;

	/** The user's list */
	private List<WilosUser> userList;

	/** The service for wilos User */
	private WilosUserService wilosUserService;

	/** The service of role */
	private RoleService roleService;

	/** The service of login */

	private LoginService loginService;

	/** User, save old value of the user before modify it */
	private WilosUser userold;

	/**
	 * The service of AffectedTo
	 */
	private AffectedtoService affectedtoService;

	/** The default choice in the filter box = ALL */
	private String selectItemFilter = "99";

	/** view of participant */
	private String wilosUserView = "participantView_null";

	/** test delete user */
	private String testDelete = "Rien";

	/** popup visible or not */
	private boolean visiblePopup = false;

	/** Selected role for a user */
	private String selectRole;

	/** The list of role in database */
	private List<SelectItem> roleItem;

	/** The list of role in database + the item "ALL" */
	private List<SelectItem> roleListFilter;

	/** Clear the current user*/
	private String cleanUser = "";

	/** for know if a user is in a session **/
	private String isSetUserFromSession;

	/** passeword **/
	private String currentPassword;

	/** Print the pannel pass **/
	private String selectedPanel = "default";

	/** change the password*/
	private String passwordConfirmation;
	private String newpassword;

	private String cleanBean;

	private ParticipantService participantService;

	public ParticipantService getParticipantService() {
		return participantService;
	}

	public void setParticipantService(ParticipantService participantService) {
		this.participantService = participantService;
	}

	public String getNewpassword() {
		return newpassword;
	}

	public void setNewpassword(String newpassword) {
		this.newpassword = newpassword;
	}

	public String getPasswordConfirmation() {
		return passwordConfirmation;
	}

	public void setPasswordConfirmation(String passwordConfirmation) {
		this.passwordConfirmation = passwordConfirmation;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public void setIsSetUserFromSession(String isSetUserFromSession) {
		this.isSetUserFromSession = isSetUserFromSession;
	}

	/**
	 * Constructor of WilosUserBean
	 */
	public WilosUserBean() {
		this.user = new WilosUser();
	}

	/**
	 * Set the participant of the participantBean to the participant which is
	 * stored into the session
	 * @return ok or null if set participant from session it s ok or not
	 */
	public String getIsSetUserFromSession() {
		WilosUser user = this.getUserFromSession();
		if (user != null) {
			this.user = user;
			this.user.setPassword("");
			this.isSetUserFromSession = "ok";
		} else {
			this.isSetUserFromSession = "null";
		}
		return this.isSetUserFromSession;
	}

	/**
	 * Method which permits the getting of the object Participant which is
	 * stored into the session
	 * @return the participant stored into the session
	 */
	private WilosUser getUserFromSession() {
		String userId = (String) WebSessionService
		.getAttribute(WebSessionService.WILOS_USER_ID);
		this.user = this.wilosUserService.getSimpleUser(userId);
		this.currentPassword = this.user.getPassword();
		return user;
	}

	/**
	 * Validate all fields before update user by an admin
	 */
	public void validateModificationWilosUser() {
		boolean error = false;
		this.user.setRole_id(this.selectRole);
		try {
			// Verify if the email is valid
			this.emailValidation(null, null, this.user.getEmailAddress());
		} catch (ValidatorException ve) {
			error = true;
			WebCommonService
			.addErrorMessage(LocaleBean
					.getText("component.projectdirectorcreate.err.emailNotValid"));
		}

		if (error == false) {
			if (this.loginService.loginExist(this.user.getLogin().trim(),
					this.userold.getLogin())) {
				// This login already exists
				WebCommonService
				.addErrorMessage(LocaleBean
						.getText("component.projectdirectorcreate.err.loginalreadyexist"));
			} else {
				if(this.user.getRole_id().equalsIgnoreCase("0") && this.participantService.getParticipant(user.getId()) == null){
					Participant p = new Participant();
					p.setEmailAddress(this.user.getEmailAddress());
					p.setFirstname(this.user.getFirstname());
					p.setName(this.user.getName());
					p.setLogin(this.user.getLogin());
					p.setKeyPassword(this.user.getKeyPassword());
					p.setNewPassword(this.user.getNewPassword());
					p.setPassword(this.user.getPassword());
					p.setRole_id(this.user.getRole_id());
					this.wilosUserService.deleteWilosuser(this.user.getId());
					this.participantService.saveParticipantWithoutEncryption(p);

				}else if(!this.user.getRole_id().equalsIgnoreCase("0") && this.participantService.getParticipant(user.getId()) != null){
					WilosUser p = new WilosUser();
					p.setEmailAddress(this.user.getEmailAddress());
					p.setFirstname(this.user.getFirstname());
					p.setName(this.user.getName());
					p.setLogin(this.user.getLogin());
					p.setKeyPassword(this.user.getKeyPassword());
					p.setNewPassword(this.user.getNewPassword());
					p.setPassword(this.user.getPassword());
					p.setRole_id(this.user.getRole_id());
					this.participantService.deleteParticipant(user.getId());
					this.wilosUserService.saveWilosUser(p);
				}else{
					WilosUser p = new WilosUser();
					p.setId(this.user.getId());
					p.setEmailAddress(this.user.getEmailAddress());
					p.setFirstname(this.user.getFirstname());
					p.setName(this.user.getName());
					p.setLogin(this.user.getLogin());
					p.setKeyPassword(this.user.getKeyPassword());
					p.setNewPassword(this.user.getNewPassword());
					p.setPassword(this.user.getPassword());
					p.setRole_id(this.user.getRole_id());
					this.wilosUserService.saveWilosUser(p);
				}
				WebCommonService.addInfoMessage(LocaleBean
						.getText("component.projectdirectorcreate.success"));
				this.userList = null;
				this.user = new WilosUser();
				// 99 = item ALL
				if (this.selectItemFilter.equals("99")) {
					this.getUserList();
				} else {
					this.getUserByRole(this.selectItemFilter);
				}
			}
		}

	}

	/**
	 * Validate all fields before update a user by him
	 */
	public void validateModifyAWilosUser() {
		boolean error = false;
		
		this.user.setPassword(Security.encode(this.user.getPassword()));

		if (this.user.getPassword().equalsIgnoreCase(this.currentPassword)) {
			if (this.newpassword != null && this.passwordConfirmation != null) {
				error = this.updatePasswordAction();
				if(error == false && this.newpassword.trim().length() != 0){
					this.user.setPassword(Security.encode(this.newpassword));
				}
			}
			try {
				// Verify if the email is valid
				this.emailValidation(null, null, this.user.getEmailAddress());

			} catch (ValidatorException ve) {
				error = true;
				WebCommonService.addErrorMessage(LocaleBean
						.getText("component.forminscription.err.badpassword"));
			}
			if (error == false) {
			this.wilosUserService.saveWilosUser(this.user);
			LoginBean lb = (LoginBean) WebCommonService.getBean("LoginBean");
			lb.setUser(this.user);
				WebCommonService.addInfoMessage(LocaleBean
						.getText("component.projectdirectorcreate.success"));
			}
		} else {
			WebCommonService.addErrorMessage(LocaleBean

					.getText("component.forminscription.err.badpassword"));
		}
	}

	/**
	 * Return the list of user in function of the role
	 * 
	 * @param role_id
	 *                the role's id
	 * @return the list of user
	 */
	public List<WilosUser> getUserByRole(String role_id) {
		this.userList = new ArrayList<WilosUser>();
		this.userList.addAll(this.affectedtoService.affected(this.roleService
				.getRoleUser(this.wilosUserService.getUserByRole(role_id))));
		return this.userList;
	}

	/**
	 * Change the list in function of the filter
	 * 
	 * @param evt
	 */
	public void changeListenerFilter(ValueChangeEvent evt) {
		String choix = (String) evt.getNewValue();
		if (choix.equals("99")) {
			this.userList.clear();
			this.setUserList(getUserList());
		} else {
			this.setUserList(getUserByRole(choix));
		}

	}

	/**
	 * Return the list of users
	 * 
	 * @return the list of users
	 */
	public List<WilosUser> getUserList() {
		if (this.userList == null || this.userList.size() == 0) {
			buildUserList();
		}
		return this.userList;
	}

	/**
	 * Return all user
	 */
	public void buildUserList() {
		this.userList = new ArrayList<WilosUser>();
		this.userList.addAll(this.affectedtoService.affected((this.roleService
				.getRoleUser(this.wilosUserService.getUser()))));
		if (this.userList == null) {
			this.wilosUserView = "participantView_null";
		} else {
			this.wilosUserView = "participantView_not_null";
		}
	}

	/**
	 * Get a user by his id
	 * 
	 * @param id
	 * @return the list of user
	 */
	public List<WilosUser> getOneUser(String id) {
		this.user = this.wilosUserService.getSimpleUser(id);
		return this.userList;
	}

	/**
	 * Modify selected user
	 */
	public void modifyUser() {
		FacesContext context = FacesContext.getCurrentInstance();
		Map map = context.getExternalContext().getRequestParameterMap();
		String idUser = (String) map.get("idUser");
		/** Search the participant on the list */
		this.user = wilosUserService.getSimpleUser(idUser);
		this.userold = this.user;
	}

	/**
	 * Method for control if the email it's ok
	 * 
	 * @param _context
	 *                context of faces
	 * @param _toValidate
	 *                validate ui component
	 * @param _value
	 *                value of the email
	 * @throws ValidatorException
	 *                 exception of validator
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
	 * Delete selected user
	 */
	public void deleteWilosUser(ActionEvent _evt) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map map = context.getExternalContext().getRequestParameterMap();
		this.setTestDelete((String) map.get("idUser"));
		this.visiblePopup = true;
	}

	/**
	 * This method allow to print the right message when an user want to delete
	 * the selected user
	 * 
	 * @param event
	 */
	public void confirmDelete(ActionEvent event) {
		boolean suppression = this.wilosUserService.deleteWilosuser(this.testDelete);
		if(suppression){
			WebCommonService.addInfoMessage(LocaleBean
					.getText("component.participantList.deleteparti.success"));
		}else{
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.participantList.deleteparti.failed"));
		}
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
	 * Test for delete participant
	 * 
	 * @return value of the user
	 */
	public String getTestDelete() {
		return testDelete;
	}

	/**
	 * Test for delete participant
	 * 
	 * @return value of the user
	 */
	public void setTestDelete(String deleteUser) {
		this.testDelete = deleteUser;
	}

	public void changeListener(ValueChangeEvent evt) {
		this.selectRole = (String) evt.getNewValue();
	}

	/**
	 * Give all the processes save in the database
	 * 
	 * @return the processes list
	 */
	public List<SelectItem> getRoleItem() {

		this.roleItem = new ArrayList<SelectItem>();
		List<Role> roles = this.roleService.getRoleDao().getRole();
		for (Role r : roles) {
			if (!r.getRole_id().equalsIgnoreCase(userold.getRole_id()))
				this.roleItem.add(new SelectItem(r.getRole_id(), r.getName()));
			else
				this.roleItem.add(0,
						new SelectItem(r.getRole_id(), r.getName()));
		}
		return this.roleItem;
	}

	/**
	 * List of role for the filter combo box
	 * 
	 * @return the list of item
	 */
	public List<SelectItem> getRoleListFilter() {
		this.roleListFilter = new ArrayList<SelectItem>();
		List<Role> roles = this.roleService.getRoleDao().getRole();
		for (Role r : roles) {
			this.roleListFilter
			.add(new SelectItem(r.getRole_id(), r.getName()));
		}
		Role r = new Role();
		r.setName(LocaleBean.getText("component.participantlist.all"));
		r.setRole_id("99");
		this.roleListFilter.add(0, new SelectItem(r.getRole_id(), r.getName()));
		return this.roleListFilter;
	}

	/**
	 * Get a new password
	 */
	public void sendNewPassword() {
		//save information
		this.userold = this.user;
		//get the user
		this.user = this.wilosUserService.getUserByLogin(this.user.getLogin());
		//verify if the code is ok
		if(this.userold.getKeyPassword().equals(this.user.getKeyPassword()))
		{
			//change the password
			this.user.setNewPassword(this.userold.getNewPassword());
			//raz keyPassword
			this.user.setKeyPassword("");
			//save the user
			this.wilosUserService.saveWilosUser(this.user);
			//inform the user that his password has modified
			WebCommonService.changeContentPage("wilos");

			WebCommonService.addInfoMessage(LocaleBean
					.getText("component.forminscription.passwordsuccess"));
		}
		else {
			//the code is not ok
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.formforgetpassword.code.invalid"));

		}

	}

	/**
	 * Get code for change the forgotten password
	 */
	public void sendKey() {
		this.user = this.wilosUserService.getUserByEmail(this.user
				.getEmailAddress());
		if (user != null) {
			this.user.setKeyPassword(this.user.generateNewPassword());
			String message = LocaleBean
			.getText("component.formforgetpassword.mail.header.name")
			+ " " + this.user.getFirstname() + " " +this.user
			.getName() + ",</br></br>";
			message += LocaleBean
			.getText("component.formforgetpassword.mail.body")
			+ " : <b>"
			+ this.user.getKeyPassword()
			+ " </b> </br>"
			+ LocaleBean
			.getText("component.formforgetpassword.mail.end");
			;
			String[] recipient = new String[1];
			recipient[0] = this.user.getEmailAddress();
			String subject = LocaleBean
			.getText("component.formForgottenPassword.title");
			try {
				SendMail.postMail(recipient, subject, message,
						"wilos.be@gmail.com");
				WebCommonService.changeContentPage("wilos");
				WebCommonService.addInfoMessage(LocaleBean
						.getText("component.formforgetpassword.mail.sended"));
				//save the user
				this.wilosUserService.saveWilosUser(this.user);
			} catch (MessagingException e) {
				WebCommonService.changeContentPage("wilos");
				WebCommonService
				.addInfoMessage(LocaleBean
						.getText("component.formforgetpassword.mail.not.sended"));
			}

		} else {
			//email address is invalid
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.invalidemail"));
		}

	}

	public void redirectToModifyPassword() {
		WebCommonService.changeContentPage("forgottenPassword");
		WebSessionService.setAttribute(WebSessionService.USER_GUIDE, "guide.forgotten.password");
	}

	/**
	 * Change the list of role for the filtered list
	 * 
	 * @param roleListFilter
	 */
	public void setRoleListFilter(List<SelectItem> roleListFilter) {
		this.roleListFilter = roleListFilter;
	}

	/**
	 * Get the user
	 * 
	 * @return the user
	 */
	public WilosUser getUser() {
		return user;
	}

	/**
	 * Set the user
	 * 
	 * @param user
	 *                the new user
	 */
	public void setUser(WilosUser user) {
		this.user = user;
	}

	/**
	 * Get the service of wilos user
	 * 
	 * @return the WilosUserService
	 */
	public WilosUserService getWilosUserService() {
		return wilosUserService;
	}

	/**
	 * Change the wilosUser service
	 * 
	 * @param wilosUserService
	 *                the new service
	 */
	public void setWilosUserService(WilosUserService wilosUserService) {
		this.wilosUserService = wilosUserService;
	}

	/**
	 * Change the list of user
	 * 
	 * @param userList
	 */
	public void setUserList(List<WilosUser> userList) {
		this.userList = userList;
	}

	/**
	 * Get the service of role
	 * 
	 * @return the RoleService
	 */
	public RoleService getRoleService() {
		return roleService;
	}

	/**
	 * Change the role service
	 * 
	 * @param roleService
	 *                the new service for role
	 */
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

	/**
	 * Get the login service
	 * 
	 * @return the loginService
	 */
	public LoginService getLoginService() {
		return loginService;
	}

	/**
	 * Change the login service
	 * 
	 * @param loginService
	 *                the new loginService
	 */
	public void setLoginService(LoginService loginService) {
		this.loginService = loginService;
	}

	/**
	 * Get the old user
	 * 
	 * @return the old user
	 */
	public WilosUser getUserold() {
		return userold;
	}

	/**
	 * Change the old user
	 * 
	 * @param userold
	 *                the new old user
	 */
	public void setUserold(WilosUser userold) {
		this.userold = userold;
	}

	/**
	 * Test the visibilty of the popup
	 * 
	 * @return true if the popup is visible else false
	 */
	public boolean isVisiblePopup() {
		return visiblePopup;
	}

	/**
	 * Change the visibilty of the popup
	 * 
	 * @param visiblePopup
	 *                true or false
	 */
	public void setVisiblePopup(boolean visiblePopup) {
		this.visiblePopup = visiblePopup;
	}

	/**
	 * Get the visible panel
	 * 
	 * @return the visible panel
	 */
	public String getWilosUserView() {
		List<WilosUser> l = this.getUserByRole(this.selectItemFilter);
		if (l.size() > 0) {
			this.setWilosUserView("participantView_not_null");
		} else {
			this.setWilosUserView("participantView_null");
		}
		return wilosUserView;
	}

	/**
	 * Set the visible panel
	 * 
	 * @param wilosUserView
	 */
	public void setWilosUserView(String wilosUserView) {
		this.wilosUserView = wilosUserView;
	}

	/**
	 * Get the selected role
	 * 
	 * @return the role
	 */
	public String getSelectRole() {
		return selectRole;
	}

	/**
	 * Change the selected role
	 * 
	 * @param selectRole
	 *                the new selected role
	 */
	public void setSelectRole(String selectRole) {
		this.selectRole = selectRole;
	}

	/**
	 * Change the list of role
	 * 
	 * @param roleItem
	 *                the new list of role
	 */
	public void setRoleItem(List<SelectItem> roleItem) {
		this.roleItem = roleItem;
	}

	/**
	 * Get the AffectedToService
	 * 
	 * @return the AffectedToService
	 */
	public AffectedtoService getAffectedtoService() {
		return affectedtoService;
	}

	/**
	 * Change the AffectedToService
	 * 
	 * @param affectedtoService
	 *                the new service
	 */
	public void setAffectedtoService(AffectedtoService affectedtoService) {
		this.affectedtoService = affectedtoService;
	}

	/**
	 * Get the current selected item in the filter
	 * 
	 * @return the current selected item
	 */
	public String getSelectItemFilter() {
		return selectItemFilter;
	}

	/**
	 * Change the current selected item in the filter
	 * 
	 * @param selectItemFilter
	 *                the new selected item
	 */
	public void setSelectItemFilter(String selectItemFilter) {
		this.selectItemFilter = selectItemFilter;
	}

	/**
	 * Cancel the new participant subscription and return to the home page
	 */
	public void cancelSubscription() {
		WebCommonService.changeContentPage("wilos");
		WebCommonService.addInfoMessage(LocaleBean
				.getText("component.formforgetpassword.cancel"));
		WebSessionService.setAttribute(WebSessionService.USER_GUIDE, "guide.accueil");
	}

	/**
	 * Cancel the new participant subscription and return to the home page
	 */
	public void cancelSubscriptions() {
		WebCommonService.changeContentPage("wilos");
		WebCommonService.addInfoMessage(LocaleBean
				.getText("component.forminscription.cancel"));
	}

	/**
	 * Cancel the ask of a code for change the password
	 */
	public void cancelAskKey() {
		WebCommonService.changeContentPage("wilos");
		WebCommonService.addInfoMessage(LocaleBean
				.getText("component.formforgetpassword.cancel.key"));
	}

	/**
	 * Clean the user
	 * @return ""
	 */
	public String getCleanUser() {
		this.user = new WilosUser();
		return cleanUser;
	}

	public void setCleanUser(String cleanUser) {
		this.cleanUser = cleanUser;
	}

	/**
	 * Change the user password with verification
	 */
	public boolean updatePasswordAction() {
		// check authentication
		boolean error = false;
		if (this.user.getPassword().trim().length() == 0) {
			error = true;
			WebCommonService.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.passwordRequired"));
		} else if (this.newpassword==null){

		} else if (this.newpassword.trim().length() == 0 && selectedPanel.equalsIgnoreCase("pass")) {
			error = true;
			WebCommonService
			.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.newpasswordRequired"));
		} else if (this.passwordConfirmation.trim().length() == 0 && selectedPanel.equalsIgnoreCase("pass")) {
			error = true;
			WebCommonService
			.addErrorMessage(LocaleBean
					.getText("component.forminscription.err.confirmpasswordRequired"));
		}
		if (!error && selectedPanel.equalsIgnoreCase("pass")) {
			WebCommonService.addInfoMessage(LocaleBean
					.getText("component.forminscription.passwordsuccess"));
		}
		return error;
	}

	/**
	 * Set password edited print
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
	 * @return null
	 */
	public String getSelectedPanel() {
		return selectedPanel;
	}

	/**
	 * Method who compare if the password and the confirm password are the same
	 * @param _context the faces context
	 * @param _toValidate validate uicomponent
	 * @param _value password value
	 * @throws ValidatorException exception of validator
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
	 * Method for saving participant data from form
	 */
	public void saveUserAction() {
		boolean error = true;
		try {
			WilosUser userExist = this.wilosUserService
			.getUserByLogin(this.user.getLogin().trim());
			if(userExist.getLogin() != null)
				WebCommonService
				.addErrorMessage(LocaleBean
						.getText("component.forminscription.err.loginalreadyexist"));
		} catch (Exception e) {
			error = false;
		}
		if (!error) {
			
			
			if(this.user.getName().length() == 0){
				WebCommonService
				.addErrorMessage(LocaleBean
						.getText("component.forminscription.err.lastnameRequired"));
			}else if(this.user.getFirstname().length() == 0){
				WebCommonService
				.addErrorMessage(LocaleBean
						.getText("component.forminscription.err.firstnameRequired"));
			}else if(this.user.getEmailAddress().length() == 0){
				WebCommonService
				.addErrorMessage(LocaleBean
						.getText("component.forminscription.err.emailRequired"));
			}else if(this.user.getLogin().length() == 0){
				WebCommonService
				.addErrorMessage(LocaleBean
						.getText("component.forminscription.err.loginRequired"));
			}else if(this.user.getPassword().length() == 0){
				WebCommonService
				.addErrorMessage(LocaleBean
						.getText("component.forminscription.err.passwordRequired"));
			}else 
				if(this.user.getPassword().length() < 6){
					System.out.println(this.user.getPassword().length()+"***");
				WebCommonService
				.addErrorMessage(LocaleBean
						.getText("component.forminscription.err.passwordRequiredSixChar"));
			}else if(this.passwordConfirmation.length() == 0){
				WebCommonService
				.addErrorMessage(LocaleBean
						.getText("component.forminscription.err.confirmpasswordRequired"));
			}else {
			this.user.setPassword(Security.encode(this.user.getPassword()));
			Participant p = new Participant();
			p.setId(this.user.getId());
			p.setLogin(this.user.getLogin());
			this.participantService.saveParticipantWithoutEncryption(p);
			Participant Pa = this.participantService.getParticipantDao().getParticipant(p.getLogin());
			this.user.setId(Pa.getId());
			this.wilosUserService.saveWilosUser(this.user);
			WebCommonService.addInfoMessage(LocaleBean
					.getText("component.forminscription.success"));
			// confirmation
			WebCommonService.changeContentPage("wilos");
			
			//display user guide
			 WebSessionService.setAttribute(WebSessionService.USER_GUIDE,
				    "guide.end.inscription");
			}

		}
		this.user = new WilosUser();
	}

	/**
	 * Getter of cleanBean.
	 * @return the cleanBean.
	 */
	public String getCleanBean() {
		this.user = new WilosUser();
		this.cleanBean = "ok";
		return this.cleanBean;
	}

	/**
	 * Setter of cleanBean.
	 * @param _cleanBean
	 *            The cleanBean to set.
	 */
	public void setCleanBean(String _cleanBean) {
		this.cleanBean = _cleanBean;
	}

	public void participantPanelDisplayed() {

	}

}
