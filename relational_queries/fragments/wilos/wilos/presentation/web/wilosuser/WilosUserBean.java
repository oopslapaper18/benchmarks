package wilos.
  presentation.
  web.
  wilosuser;

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

 *
 This
 Bean
 represent
 wilos user

 */
public class WilosUserBean {
    /**
     The
     user */
    private wilos.model.misc.wilosuser.WilosUser user;
    /**
     The
     user's
     list */
    private java.util.List<wilos.model.misc.wilosuser.WilosUser> userList;
    /**
     The
     service
     for
     wilos
     User */
    private wilos.business.services.misc.wilosuser.WilosUserService
      wilosUserService;
    /** The service of role */
    private wilos.business.services.misc.wilosuser.RoleService roleService;
    /** The service of login */
    private wilos.business.services.misc.wilosuser.LoginService loginService;
    /** User, save old value of the user before modify it */
    private wilos.model.misc.wilosuser.WilosUser userold;
    /**
    
     * The service of AffectedTo
    
     */
    private wilos.business.services.misc.project.AffectedtoService
      affectedtoService;
    /** The default choice in the filter box = ALL */
    private java.lang.String selectItemFilter = "99";
    /** view of participant */
    private java.lang.String wilosUserView = "participantView_null";
    /** test delete user */
    private java.lang.String testDelete = "Rien";
    /** popup visible or not */
    private boolean visiblePopup = false;
    /** Selected role for a user */
    private java.lang.String selectRole;
    /** The list of role in database */
    private java.util.List<javax.faces.model.SelectItem> roleItem;
    /** The list of role in database + the item "ALL" */
    private java.util.List<javax.faces.model.SelectItem> roleListFilter;
    /** Clear the current user*/
    private java.lang.String cleanUser = "";
    /** for know if a user is in a session **/
    private java.lang.String isSetUserFromSession;
    /** passeword **/
    private java.lang.String currentPassword;
    /** Print the pannel pass **/
    private java.lang.String selectedPanel = "default";
    /** change the password*/
    private java.lang.String passwordConfirmation;
    private java.lang.String newpassword;
    private java.lang.String cleanBean;
    private wilos.business.services.misc.wilosuser.ParticipantService
      participantService;
    
    public wilos.business.services.misc.wilosuser.
      ParticipantService getParticipantService() { return participantService; }
    
    public void setParticipantService(wilos.business.services.misc.wilosuser.
                                        ParticipantService participantService) {
        this.participantService = participantService;
    }
    
    public java.lang.String getNewpassword() { return newpassword; }
    
    public void setNewpassword(java.lang.String newpassword) {
        this.newpassword = newpassword;
    }
    
    public java.lang.String getPasswordConfirmation() {
        return passwordConfirmation;
    }
    
    public void setPasswordConfirmation(java.lang.String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }
    
    public java.lang.String getCurrentPassword() { return currentPassword; }
    
    public void setCurrentPassword(java.lang.String currentPassword) {
        this.currentPassword = currentPassword;
    }
    
    public void setIsSetUserFromSession(java.lang.String isSetUserFromSession) {
        this.isSetUserFromSession = isSetUserFromSession;
    }
    
    /**
    
     * Constructor of WilosUserBean
    
     */
    public WilosUserBean() {
        super();
        this.user = new wilos.model.misc.wilosuser.WilosUser();
    }
    
    /**
    
     * Set the participant of the participantBean to the participant which is
    
     * stored into the session
    
     * @return ok or null if set participant from session it s ok or not
    
     */
    public java.lang.String getIsSetUserFromSession() {
        wilos.model.misc.wilosuser.WilosUser user = this.getUserFromSession();
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
    private wilos.model.misc.wilosuser.WilosUser getUserFromSession() {
        java.
          lang.
          String
          userId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.WILOS_USER_ID);
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
        try { this.emailValidation(null, null, this.user.getEmailAddress()); }
        catch (javax.faces.validator.ValidatorException ve) {
            error = true;
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText(
                      "component.projectdirectorcreate.err.emailNotValid"));
        }
        if (error == false) {
            if (this.loginService.loginExist(this.user.getLogin().trim(),
                                             this.userold.getLogin())) {
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addErrorMessage(
                    wilos.resources.LocaleBean.
                        getText(
                          "component.projectdirectorcreate.err.loginalreadyexist"));
            }
            else {
                if (this.user.getRole_id().equalsIgnoreCase("0") &&
                      this.participantService.getParticipant(
                                                user.getId()) == null) {
                    wilos.model.misc.wilosuser.Participant p =
                      new wilos.model.misc.wilosuser.Participant();
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
                }
                else
                    if (!this.user.getRole_id().equalsIgnoreCase("0") &&
                          this.participantService.getParticipant(
                                                    user.getId()) != null) {
                        wilos.model.misc.wilosuser.WilosUser p =
                          new wilos.model.misc.wilosuser.WilosUser();
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
                    }
                    else {
                        wilos.model.misc.wilosuser.WilosUser p =
                          new wilos.model.misc.wilosuser.WilosUser();
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
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addInfoMessage(
                    wilos.resources.LocaleBean.
                        getText("component.projectdirectorcreate.success"));
                this.userList = null;
                this.user = new wilos.model.misc.wilosuser.WilosUser();
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
        this.user.setPassword(
                    wilos.utils.Security.encode(this.user.getPassword()));
        if (this.user.getPassword().equalsIgnoreCase(this.currentPassword)) {
            if (this.newpassword != null && this.passwordConfirmation != null) {
                error = this.updatePasswordAction();
                if (error == false && this.newpassword.trim().length() != 0) {
                    this.user.setPassword(
                                wilos.utils.Security.encode(this.newpassword));
                }
            }
            try {
                this.emailValidation(null, null, this.user.getEmailAddress());
            }
            catch (javax.faces.validator.ValidatorException ve) {
                error = true;
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addErrorMessage(
                    wilos.resources.LocaleBean.
                        getText("component.forminscription.err.badpassword"));
            }
            if (error == false) {
                this.wilosUserService.saveWilosUser(this.user);
                wilos.
                  presentation.
                  web.
                  wilosuser.
                  LoginBean
                  lb =
                  (wilos.presentation.web.wilosuser.LoginBean)
                    wilos.presentation.web.utils.WebCommonService.
                    getBean("LoginBean");
                lb.setUser(this.user);
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addInfoMessage(
                    wilos.resources.LocaleBean.
                        getText("component.projectdirectorcreate.success"));
            }
        }
        else {
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText("component.forminscription.err.badpassword"));
        }
    }
    
    /**
    
     * Return the list of user in function of the role
    
     * 
    
     * @param role_id
    
     *                the role's id
    
     * @return the list of user
    
     */
    public java.util.List<wilos.model.misc.wilosuser.
      WilosUser> getUserByRole(java.lang.String role_id) {
        this.userList =
          new java.util.ArrayList<wilos.model.misc.wilosuser.WilosUser>();
        this.userList.
          addAll(
            this.affectedtoService.
                affected(
                  this.roleService.getRoleUser(
                                     this.wilosUserService.getUserByRole(
                                                             role_id))));
        return this.userList;
    }
    
    /**
    
     * Change the list in function of the filter
    
     * 
    
     * @param evt
    
     */
    public void changeListenerFilter(javax.faces.event.ValueChangeEvent evt) {
        java.lang.String choix = (java.lang.String) evt.getNewValue();
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
    public java.util.List<wilos.model.misc.wilosuser.WilosUser> getUserList() {
        if (this.userList == null || this.userList.size() == 0) {
            buildUserList();
        }
        return this.userList;
    }
    
    /**
    
     * Return all user
    
     */
    public void buildUserList() {
        this.userList =
          new java.util.ArrayList<wilos.model.misc.wilosuser.WilosUser>();
        this.userList.
          addAll(
            this.affectedtoService.
                affected(
                  this.roleService.getRoleUser(
                                     this.wilosUserService.getUser())));
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
    public java.util.List<wilos.model.misc.wilosuser.
      WilosUser> getOneUser(java.lang.String id) {
        this.user = this.wilosUserService.getSimpleUser(id);
        return this.userList;
    }
    
    /**
    
     * Modify selected user
    
     */
    public void modifyUser() {
        javax.faces.context.FacesContext context =
          javax.faces.context.FacesContext.getCurrentInstance();
        java.util.Map map =
          context.getExternalContext().getRequestParameterMap();
        java.lang.String idUser = (java.lang.String) map.get("idUser");
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
    public void emailValidation(javax.faces.context.FacesContext _context,
                                javax.faces.component.UIComponent _toValidate,
                                java.lang.Object _value)
          throws javax.faces.validator.ValidatorException {
        java.lang.String enteredEmail = (java.lang.String) _value;
        java.util.regex.Pattern p =
          java.util.regex.Pattern.compile(".+@.+\\.[a-z]+");
        java.util.regex.Matcher m = p.matcher(enteredEmail);
        boolean matchFound = m.matches();
        if (!matchFound) {
            javax.faces.application.FacesMessage message =
              new javax.faces.application.FacesMessage();
            message.
              setSummary(
                wilos.resources.LocaleBean.
                    getText("component.forminscription.err.invalidemail"));
            message.setSeverity(
                      javax.faces.application.FacesMessage.SEVERITY_ERROR);
            throw new javax.faces.validator.ValidatorException(message);
        }
    }
    
    /**
    
     * Delete selected user
    
     */
    public void deleteWilosUser(javax.faces.event.ActionEvent _evt) {
        javax.faces.context.FacesContext context =
          javax.faces.context.FacesContext.getCurrentInstance();
        java.util.Map map =
          context.getExternalContext().getRequestParameterMap();
        this.setTestDelete((java.lang.String) map.get("idUser"));
        this.visiblePopup = true;
    }
    
    /**
    
     * This method allow to print the right message when an user want to delete
    
     * the selected user
    
     * 
    
     * @param event
    
     */
    public void confirmDelete(javax.faces.event.ActionEvent event) {
        boolean suppression =
          this.wilosUserService.deleteWilosuser(this.testDelete);
        if (suppression) {
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addInfoMessage(
                wilos.resources.LocaleBean.
                    getText("component.participantList.deleteparti.success"));
        }
        else {
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText("component.participantList.deleteparti.failed"));
        }
        this.visiblePopup = false;
    }
    
    /**
    
     * This method fixed the visiblePopup boolean attribute to false
    
     * 
    
     * @param event
    
     */
    public void cancel(javax.faces.event.ActionEvent event) {
        this.visiblePopup = false;
    }
    
    /**
    
     * Test for delete participant
    
     * 
    
     * @return value of the user
    
     */
    public java.lang.String getTestDelete() { return testDelete; }
    
    /**
    
     * Test for delete participant
    
     * 
    
     * @return value of the user
    
     */
    public void setTestDelete(java.lang.String deleteUser) {
        this.testDelete = deleteUser;
    }
    
    public void changeListener(javax.faces.event.ValueChangeEvent evt) {
        this.selectRole = (java.lang.String) evt.getNewValue();
    }
    
    /**
    
     * Give all the processes save in the database
    
     * 
    
     * @return the processes list
    
     */
    public java.util.List<javax.faces.model.SelectItem> getRoleItem() {
        this.roleItem = new java.util.ArrayList<javax.faces.model.SelectItem>();
        java.util.List<wilos.model.misc.wilosuser.Role> roles =
          this.roleService.getRoleDao().getRole();
        java.util.Iterator extfor$iter = roles.iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.wilosuser.Role r =
              (wilos.model.misc.wilosuser.Role) extfor$iter.next();
            if (!r.getRole_id().equalsIgnoreCase(userold.getRole_id()))
                this.roleItem.add(
                                new javax.faces.model.SelectItem(r.getRole_id(),
                                                                 r.getName()));
            else
                this.roleItem.add(
                                0,
                                new javax.faces.model.SelectItem(r.getRole_id(),
                                                                 r.getName()));
        }
        return this.roleItem;
    }
    
    /**
    
     * List of role for the filter combo box
    
     * 
    
     * @return the list of item
    
     */
    public java.util.List<javax.faces.model.SelectItem> getRoleListFilter() {
        this.roleListFilter =
          new java.util.ArrayList<javax.faces.model.SelectItem>();
        java.util.List<wilos.model.misc.wilosuser.Role> roles =
          this.roleService.getRoleDao().getRole();
        java.util.Iterator extfor$iter = roles.iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.wilosuser.Role r =
              (wilos.model.misc.wilosuser.Role) extfor$iter.next();
            this.roleListFilter.add(
                                  new javax.faces.model.SelectItem(
                                      r.getRole_id(), r.getName()));
        }
        wilos.model.misc.wilosuser.Role r = new wilos.model.misc.wilosuser.Role(
          );
        r.setName(
            wilos.resources.LocaleBean.getText(
                                         "component.participantlist.all"));
        r.setRole_id("99");
        this.roleListFilter.add(0,
                                new javax.faces.model.SelectItem(r.getRole_id(),
                                                                 r.getName()));
        return this.roleListFilter;
    }
    
    /**
    
     * Get a new password
    
     */
    public void sendNewPassword() {
        this.userold = this.user;
        this.user = this.wilosUserService.getUserByLogin(this.user.getLogin());
        if (this.userold.getKeyPassword().equals(this.user.getKeyPassword())) {
            this.user.setNewPassword(this.userold.getNewPassword());
            this.user.setKeyPassword("");
            this.wilosUserService.saveWilosUser(this.user);
            wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                            "wilos");
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addInfoMessage(
                wilos.resources.LocaleBean.
                    getText("component.forminscription.passwordsuccess"));
        }
        else {
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText("component.formforgetpassword.code.invalid"));
        }
    }
    
    /**
    
     * Get code for change the forgotten password
    
     */
    public void sendKey() {
        this.user =
          this.wilosUserService.getUserByEmail(this.user.getEmailAddress());
        if (user != null) {
            this.user.setKeyPassword(this.user.generateNewPassword());
            java.lang.String message =
              wilos.resources.LocaleBean.
              getText("component.formforgetpassword.mail.header.name") + " " +
            this.user.getFirstname() + " " + this.user.getName() +
            ",</br></br>";
            message +=
              wilos.resources.LocaleBean.
                getText("component.formforgetpassword.mail.body") +
              " : <b>" +
              this.user.getKeyPassword() +
              " </b> </br>" +
              wilos.resources.LocaleBean.
                getText("component.formforgetpassword.mail.end");
            ;
            java.lang.String[] recipient = new java.lang.String[1];
            recipient[0] = this.user.getEmailAddress();
            java.lang.String subject =
              wilos.resources.LocaleBean.
              getText("component.formForgottenPassword.title");
            try {
                wilos.presentation.web.utils.SendMail.postMail(
                                                        recipient, subject,
                                                        message,
                                                        "wilos.be@gmail.com");
                wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                                "wilos");
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addInfoMessage(
                    wilos.resources.LocaleBean.
                        getText("component.formforgetpassword.mail.sended"));
                this.wilosUserService.saveWilosUser(this.user);
            }
            catch (javax.mail.MessagingException e) {
                wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                                "wilos");
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addInfoMessage(
                    wilos.resources.LocaleBean.
                        getText(
                          "component.formforgetpassword.mail.not.sended"));
            }
        }
        else {
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText("component.forminscription.err.invalidemail"));
        }
    }
    
    public void redirectToModifyPassword() {
        wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                        "forgottenPassword");
        wilos.presentation.web.utils.WebSessionService.
          setAttribute(
            wilos.presentation.web.utils.WebSessionService.USER_GUIDE,
            "guide.forgotten.password");
    }
    
    /**
    
     * Change the list of role for the filtered list
    
     * 
    
     * @param roleListFilter
    
     */
    public void setRoleListFilter(java.util.List<javax.faces.model.
                                    SelectItem> roleListFilter) {
        this.roleListFilter = roleListFilter;
    }
    
    /**
    
     * Get the user
    
     * 
    
     * @return the user
    
     */
    public wilos.model.misc.wilosuser.WilosUser getUser() { return user; }
    
    /**
    
     * Set the user
    
     * 
    
     * @param user
    
     *                the new user
    
     */
    public void setUser(wilos.model.misc.wilosuser.WilosUser user) {
        this.user = user;
    }
    
    /**
    
     * Get the service of wilos user
    
     * 
    
     * @return the WilosUserService
    
     */
    public wilos.business.services.misc.wilosuser.
      WilosUserService getWilosUserService() { return wilosUserService; }
    
    /**
    
     * Change the wilosUser service
    
     * 
    
     * @param wilosUserService
    
     *                the new service
    
     */
    public void setWilosUserService(wilos.business.services.misc.wilosuser.
                                      WilosUserService wilosUserService) {
        this.wilosUserService = wilosUserService;
    }
    
    /**
    
     * Change the list of user
    
     * 
    
     * @param userList
    
     */
    public void setUserList(java.util.List<wilos.model.misc.wilosuser.
                              WilosUser> userList) { this.userList = userList; }
    
    /**
    
     * Get the service of role
    
     * 
    
     * @return the RoleService
    
     */
    public wilos.business.services.misc.wilosuser.RoleService getRoleService() {
        return roleService;
    }
    
    /**
    
     * Change the role service
    
     * 
    
     * @param roleService
    
     *                the new service for role
    
     */
    public void setRoleService(wilos.business.services.misc.wilosuser.
                                 RoleService roleService) {
        this.roleService = roleService;
    }
    
    /**
    
     * Get the login service
    
     * 
    
     * @return the loginService
    
     */
    public wilos.business.services.misc.wilosuser.
      LoginService getLoginService() { return loginService; }
    
    /**
    
     * Change the login service
    
     * 
    
     * @param loginService
    
     *                the new loginService
    
     */
    public void setLoginService(wilos.business.services.misc.wilosuser.
                                  LoginService loginService) {
        this.loginService = loginService;
    }
    
    /**
    
     * Get the old user
    
     * 
    
     * @return the old user
    
     */
    public wilos.model.misc.wilosuser.WilosUser getUserold() { return userold; }
    
    /**
    
     * Change the old user
    
     * 
    
     * @param userold
    
     *                the new old user
    
     */
    public void setUserold(wilos.model.misc.wilosuser.WilosUser userold) {
        this.userold = userold;
    }
    
    /**
    
     * Test the visibilty of the popup
    
     * 
    
     * @return true if the popup is visible else false
    
     */
    public boolean isVisiblePopup() { return visiblePopup; }
    
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
    public java.lang.String getWilosUserView() {
        labeled_1 :
        {
            java.util.List<wilos.model.misc.wilosuser.WilosUser> l =
                    this.getUserByRole(this.selectItemFilter);
            if (l.size() > 0)
            {
                this.setWilosUserView("participantView_not_null");
            } else
            {
                this.setWilosUserView("participantView_null");
            }
        }
        return wilosUserView;
    }
    
    /**
    
     * Set the visible panel
    
     * 
    
     * @param wilosUserView
    
     */
    public void setWilosUserView(java.lang.String wilosUserView) {
        this.wilosUserView = wilosUserView;
    }
    
    /**
    
     * Get the selected role
    
     * 
    
     * @return the role
    
     */
    public java.lang.String getSelectRole() { return selectRole; }
    
    /**
    
     * Change the selected role
    
     * 
    
     * @param selectRole
    
     *                the new selected role
    
     */
    public void setSelectRole(java.lang.String selectRole) {
        this.selectRole = selectRole;
    }
    
    /**
    
     * Change the list of role
    
     * 
    
     * @param roleItem
    
     *                the new list of role
    
     */
    public void setRoleItem(java.util.List<javax.faces.model.
                              SelectItem> roleItem) {
        this.roleItem = roleItem;
    }
    
    /**
    
     * Get the AffectedToService
    
     * 
    
     * @return the AffectedToService
    
     */
    public wilos.business.services.misc.project.
      AffectedtoService getAffectedtoService() { return affectedtoService; }
    
    /**
    
     * Change the AffectedToService
    
     * 
    
     * @param affectedtoService
    
     *                the new service
    
     */
    public void setAffectedtoService(wilos.business.services.misc.project.
                                       AffectedtoService affectedtoService) {
        this.affectedtoService = affectedtoService;
    }
    
    /**
    
     * Get the current selected item in the filter
    
     * 
    
     * @return the current selected item
    
     */
    public java.lang.String getSelectItemFilter() { return selectItemFilter; }
    
    /**
    
     * Change the current selected item in the filter
    
     * 
    
     * @param selectItemFilter
    
     *                the new selected item
    
     */
    public void setSelectItemFilter(java.lang.String selectItemFilter) {
        this.selectItemFilter = selectItemFilter;
    }
    
    /**
    
     * Cancel the new participant subscription and return to the home page
    
     */
    public void cancelSubscription() {
        wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                        "wilos");
        wilos.
          presentation.
          web.
          utils.
          WebCommonService.
          addInfoMessage(
            wilos.resources.LocaleBean.
                getText("component.formforgetpassword.cancel"));
        wilos.presentation.web.utils.WebSessionService.
          setAttribute(
            wilos.presentation.web.utils.WebSessionService.USER_GUIDE,
            "guide.accueil");
    }
    
    /**
    
     * Cancel the new participant subscription and return to the home page
    
     */
    public void cancelSubscriptions() {
        wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                        "wilos");
        wilos.presentation.web.utils.WebCommonService.
          addInfoMessage(
            wilos.resources.LocaleBean.getText(
                                         "component.forminscription.cancel"));
    }
    
    /**
    
     * Cancel the ask of a code for change the password
    
     */
    public void cancelAskKey() {
        wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                        "wilos");
        wilos.
          presentation.
          web.
          utils.
          WebCommonService.
          addInfoMessage(
            wilos.resources.LocaleBean.
                getText("component.formforgetpassword.cancel.key"));
    }
    
    /**
    
     * Clean the user
    
     * @return ""
    
     */
    public java.lang.String getCleanUser() {
        this.user = new wilos.model.misc.wilosuser.WilosUser();
        return cleanUser;
    }
    
    public void setCleanUser(java.lang.String cleanUser) {
        this.cleanUser = cleanUser;
    }
    
    /**
    
     * Change the user password with verification
    
     */
    public boolean updatePasswordAction() {
        boolean error = false;
        if (this.user.getPassword().trim().length() == 0) {
            error = true;
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText("component.forminscription.err.passwordRequired"));
        }
        else
            if (this.newpassword == null) {
                
            }
            else
                if (this.newpassword.trim().length() ==
                      0 &&
                      selectedPanel.equalsIgnoreCase("pass")) {
                    error = true;
                    wilos.
                      presentation.
                      web.
                      utils.
                      WebCommonService.
                      addErrorMessage(
                        wilos.resources.LocaleBean.
                            getText(
                              "component.forminscription.err.newpasswordRequired"));
                }
                else
                    if (this.passwordConfirmation.trim().length() ==
                          0 &&
                          selectedPanel.equalsIgnoreCase("pass")) {
                        error = true;
                        wilos.
                          presentation.
                          web.
                          utils.
                          WebCommonService.
                          addErrorMessage(
                            wilos.resources.LocaleBean.
                                getText(
                                  "component.forminscription.err.confirmpasswordRequired"));
                    }
        if (!error && selectedPanel.equalsIgnoreCase("pass")) {
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addInfoMessage(
                wilos.resources.LocaleBean.
                    getText("component.forminscription.passwordsuccess"));
        }
        return error;
    }
    
    /**
    
     * Set password edited print
    
     * @return null
    
     */
    public java.lang.String doEditPassword() {
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
    public java.lang.String getSelectedPanel() { return selectedPanel; }
    
    /**
    
     * Method who compare if the password and the confirm password are the same
    
     * @param _context the faces context
    
     * @param _toValidate validate uicomponent
    
     * @param _value password value
    
     * @throws ValidatorException exception of validator
    
     */
    public void passwordEqualValidation(javax.faces.context.
                                          FacesContext _context,
                                        javax.faces.component.
                                          UIComponent _toValidate,
                                        java.lang.Object _value)
          throws javax.faces.validator.ValidatorException {
        javax.faces.component.UIComponent passcomponent =
          _toValidate.findComponent("equal1");
        java.lang.String passValue =
          (java.lang.String) passcomponent.getAttributes().get("value");
        if (!_value.equals(passValue)) {
            javax.faces.application.FacesMessage message =
              new javax.faces.application.FacesMessage();
            message.
              setSummary(
                wilos.resources.LocaleBean.
                    getText("component.forminscription.err.passwordnotequals"));
            message.setSeverity(
                      javax.faces.application.FacesMessage.SEVERITY_ERROR);
            throw new javax.faces.validator.ValidatorException(message);
        }
    }
    
    /**
    
     * Method for saving participant data from form
    
     */
    public void saveUserAction() {
        boolean error = true;
        try {
            wilos.model.misc.wilosuser.WilosUser userExist =
              this.wilosUserService.getUserByLogin(this.user.getLogin().trim());
            if (userExist.getLogin() != null)
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addErrorMessage(
                    wilos.resources.LocaleBean.
                        getText(
                          "component.forminscription.err.loginalreadyexist"));
        }
        catch (java.lang.Exception e) { error = false; }
        if (!error) {
            if (this.user.getName().length() == 0) {
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addErrorMessage(
                    wilos.resources.LocaleBean.
                        getText(
                          "component.forminscription.err.lastnameRequired"));
            }
            else
                if (this.user.getFirstname().length() == 0) {
                    wilos.
                      presentation.
                      web.
                      utils.
                      WebCommonService.
                      addErrorMessage(
                        wilos.resources.LocaleBean.
                            getText(
                              "component.forminscription.err.firstnameRequired"));
                }
                else
                    if (this.user.getEmailAddress().length() == 0) {
                        wilos.
                          presentation.
                          web.
                          utils.
                          WebCommonService.
                          addErrorMessage(
                            wilos.resources.LocaleBean.
                                getText(
                                  "component.forminscription.err.emailRequired"));
                    }
                    else
                        if (this.user.getLogin().length() == 0) {
                            wilos.
                              presentation.
                              web.
                              utils.
                              WebCommonService.
                              addErrorMessage(
                                wilos.resources.LocaleBean.
                                    getText(
                                      "component.forminscription.err.loginRequired"));
                        }
                        else
                            if (this.user.getPassword().length() == 0) {
                                wilos.
                                  presentation.
                                  web.
                                  utils.
                                  WebCommonService.
                                  addErrorMessage(
                                    wilos.resources.LocaleBean.
                                        getText(
                                          "component.forminscription.err.passwordRequired"));
                            }
                            else
                                if (this.user.getPassword().length() < 6) {
                                    java.lang.System.out.
                                      println(this.user.getPassword().length() +
                                                "***");
                                    wilos.
                                      presentation.
                                      web.
                                      utils.
                                      WebCommonService.
                                      addErrorMessage(
                                        wilos.resources.LocaleBean.
                                            getText(
                                              "component.forminscription.err.passwordRequiredSixChar"));
                                }
                                else
                                    if (this.passwordConfirmation.length() ==
                                          0) {
                                        wilos.
                                          presentation.
                                          web.
                                          utils.
                                          WebCommonService.
                                          addErrorMessage(
                                            wilos.resources.LocaleBean.
                                                getText(
                                                  "component.forminscription.err.confirmpasswordRequired"));
                                    }
                                    else {
                                        this.user.
                                          setPassword(
                                            wilos.utils.Security.
                                                encode(
                                                  this.user.getPassword()));
                                        wilos.model.misc.wilosuser.Participant
                                          p =
                                          new wilos.model.misc.wilosuser.
                                          Participant();
                                        p.setId(this.user.getId());
                                        p.setLogin(this.user.getLogin());
                                        this.participantService.
                                          saveParticipantWithoutEncryption(p);
                                        wilos.model.misc.wilosuser.Participant
                                          Pa =
                                          this.participantService.
                                          getParticipantDao().getParticipant(
                                                                p.getLogin());
                                        this.user.setId(Pa.getId());
                                        this.wilosUserService.saveWilosUser(
                                                                this.user);
                                        wilos.
                                          presentation.
                                          web.
                                          utils.
                                          WebCommonService.
                                          addInfoMessage(
                                            wilos.resources.LocaleBean.
                                                getText(
                                                  "component.forminscription.success"));
                                        wilos.presentation.web.utils.
                                          WebCommonService.changeContentPage(
                                                             "wilos");
                                        wilos.
                                          presentation.
                                          web.
                                          utils.
                                          WebSessionService.
                                          setAttribute(
                                            wilos.presentation.web.utils.
                                              WebSessionService.USER_GUIDE,
                                            "guide.end.inscription");
                                    }
        }
        this.user = new wilos.model.misc.wilosuser.WilosUser();
    }
    
    /**
    
     * Getter of cleanBean.
    
     * @return the cleanBean.
    
     */
    public java.lang.String getCleanBean() {
        this.user = new wilos.model.misc.wilosuser.WilosUser();
        this.cleanBean = "ok";
        return this.cleanBean;
    }
    
    /**
    
     * Setter of cleanBean.
    
     * @param _cleanBean
    
     *            The cleanBean to set.
    
     */
    public void setCleanBean(java.lang.String _cleanBean) {
        this.cleanBean = _cleanBean;
    }
    
    public void participantPanelDisplayed() {  }
}

