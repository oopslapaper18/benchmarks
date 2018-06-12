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
    private java.util.List<java.lang.String> concreteRoleDescriptorHeaders;
    /** list of concrete role descriptors */
    private java.util.List<wilos.model.misc.concreterole.ConcreteRoleDescriptor> concreteRoleDescriptors;
    /** map of concrete role descriptors */
    private java.util.HashMap<java.lang.String,
    java.lang.Boolean> concreteRoleDescriptorsMap;
    /** service of participant */
    private wilos.business.services.misc.wilosuser.ParticipantService participantService;
    /** service of project */
    private wilos.business.services.misc.project.ProjectService projectService;
    /** service of login */
    private wilos.business.services.misc.wilosuser.LoginService loginService;
    /** service of concrete role descriptor */
    private wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService concreteRoleDescriptorService;
    /** participant */
    private wilos.model.misc.wilosuser.Participant participant;
    /** confirmation of password */
    private java.lang.String passwordConfirmation;
    /** current password */
    private java.lang.String currentPassword;
    /** current affectation name */
    private java.lang.String affectationName;
    /** list of participant */
    private java.util.List<wilos.model.misc.wilosuser.Participant> participantsList;
    /** list of affected projects */
    private java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> affectedProjectsList;
    /** list of manageable projects */
    private java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> manageableProjectsList;
    /** the manageable project view selected */
    private java.lang.String selectManageableProjectView;
    /** the affected project view selected */
    private java.lang.String selectAffectedProjectView;
    /** formatter simple date format : dd/MM/yyyy */
    private java.text.SimpleDateFormat formatter;
    /** view of participant */
    private java.lang.String participantView;
    /** set participant from session */
    private java.lang.String isSetParticipantFromSession;
    /** clean bean */
    private java.lang.String cleanBean;
    /** the logger */
    protected final org.apache.commons.logging.Log logger = null;
    /** popup visible or not */
    private boolean visiblePopup = false;
    /** the project id selected */
    private java.lang.String selectedProjectId;
    /** display the field of password */
    private boolean displayPasswordEdition;
    /** test delete participant */
    private java.lang.String testDelete = "RIEN";
    /** panel dynamic for pass */
    private java.lang.String selectedPanel = "default";
    private java.util.ArrayList<javax.faces.model.SelectItem> roleItem;
    
    /**
     * Constructor of participant bean
     */
    public ParticipantBean() {
        super();
        this.participant = new wilos.model.misc.wilosuser.Participant();
        this.affectedProjectsList =
          new java.util.ArrayList<java.util.HashMap<java.lang.String,
          java.lang.Object>>();
        this.manageableProjectsList =
          new java.util.ArrayList<java.util.HashMap<java.lang.String,
          java.lang.Object>>();
        this.selectManageableProjectView = new java.lang.String();
        this.formatter = new java.text.SimpleDateFormat("dd/MM/yyyy");
        this.displayPasswordEdition = false;
    }
    
    /**
     * Method for saving participant data from form
     */
    public void saveParticipantAction() {
        boolean error = false;
        if (this.participant.getName().trim().length() == 0) {
            error = true;
            wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.forminscription.err.lastnameRequired"));
        }
        if (this.participant.getFirstname().trim().length() == 0) {
            error = true;
            wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.forminscription.err.firstnameRequired"));
        }
        if (this.participant.getEmailAddress().trim().length() == 0) {
            error = true;
            wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.forminscription.err.emailRequired"));
        }
        if (this.participant.getLogin().trim().length() == 0) {
            error = true;
            wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.forminscription.err.loginRequired"));
        }
        if (this.participant.getPassword().trim().length() == 0) {
            error = true;
            wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.forminscription.err.passwordRequired"));
        }
        if (this.passwordConfirmation.trim().length() == 0) {
            error = true;
            wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.forminscription.err.confirmpasswordRequired"));
        }
        if (!error) {
            if (this.loginService.loginExist(
                                    this.participant.getLogin().trim())) {
                wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                                wilos.resources.LocaleBean.getText(
                                                                                             "component.forminscription.err.loginalreadyexist"));
            }
            else {
                this.participantService.saveParticipant(
                                          this.participant);
                wilos.presentation.web.utils.WebCommonService.addInfoMessage(
                                                                wilos.resources.LocaleBean.getText(
                                                                                             "component.forminscription.success"));
                wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                                "wilos");
            }
        }
        this.participant = new wilos.model.misc.wilosuser.Participant();
    }
    
    /**
     * Cancel the new participant subscription and return to the home page
     */
    public void cancelSubscription() {
        wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                        "wilos");
        wilos.presentation.web.utils.WebCommonService.addInfoMessage(
                                                        wilos.resources.LocaleBean.getText(
                                                                                     "component.forminscription.cancel"));
    }
    
    /**
     * Method for updating participant data from form
     */
    public void updateParticipantAction() {
        boolean error = false;
        java.lang.String encryptedCurrentPassword =
          wilos.utils.Security.encode(this.currentPassword);
        if (this.currentPassword.length() == 0) {
            error = true;
            wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.forminscription.err.passwordRequired"));
        }
        else
            if (!this.getParticipantFromSession(
                        ).getPassword().equals(encryptedCurrentPassword)) {
                error = true;
                wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                                wilos.resources.LocaleBean.getText(
                                                                                             "component.forminscription.err.badpassword"));
            } else
                if (this.participant.getName().trim().length() == 0) {
                    error = true;
                    wilos.presentation.web.utils.WebCommonService.addErrorMessage(wilos.resources.LocaleBean.getText("component.forminscription.err.lastnameRequired"));
                } else
                    if (this.participant.getFirstname().trim().length() == 0) {
                        error = true;
                        wilos.presentation.web.utils.WebCommonService.addErrorMessage(wilos.resources.LocaleBean.getText("component.forminscription.err.firstnameRequired"));
                    } else
                        if (this.participant.getEmailAddress().trim().length() ==
                              0) {
                            wilos.presentation.web.utils.WebCommonService.addErrorMessage(wilos.resources.LocaleBean.getText("component.forminscription.err.emailRequired"));
                            error = true;
                        }
        if (!error) {
            this.participant.setPassword(
                               this.getParticipantFromSession().getPassword());
            this.participantService.saveParticipantWithoutEncryption(
                                      this.participant);
            wilos.presentation.web.utils.WebCommonService.addInfoMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.participantUpdate.success"));
            wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                            "wilos");
        }
    }
    
    /**
     * Method for updating participant data from form
     */
    public void updatePasswordAction() {
        boolean error = false;
        java.lang.String encryptedCurrentPassword =
          wilos.utils.Security.encode(this.currentPassword);
        if (this.currentPassword.length() == 0) {
            error = true;
            wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.forminscription.err.passwordRequired"));
        }
        else
            if (!this.getParticipantFromSession(
                        ).getPassword().equals(encryptedCurrentPassword)) {
                error = true;
                wilos.presentation.web.utils.WebCommonService.addErrorMessage(
                                                                wilos.resources.LocaleBean.getText(
                                                                                             "component.forminscription.err.badpassword"));
            } else
                if (this.participant.getPassword().trim().length() == 0) {
                    error = true;
                    wilos.presentation.web.utils.WebCommonService.addErrorMessage(wilos.resources.LocaleBean.getText("component.forminscription.err.newpasswordRequired"));
                } else
                    if (this.passwordConfirmation.trim().length() == 0) {
                        error = true;
                        wilos.presentation.web.utils.WebCommonService.addErrorMessage(wilos.resources.LocaleBean.getText("component.forminscription.err.confirmpasswordRequired"));
                    }
        if (!error) {
            this.participantService.saveParticipant(this.participant);
            wilos.presentation.web.utils.WebCommonService.addInfoMessage(
                                                            wilos.resources.LocaleBean.getText(
                                                                                         "component.forminscription.passwordsuccess"));
            wilos.presentation.web.utils.WebCommonService.changeContentPage(
                                                            "wilos");
            this.displayPasswordEdition = false;
        }
        this.participant = new wilos.model.misc.wilosuser.Participant();
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
            message.setSummary(
                      wilos.resources.LocaleBean.getText(
                                                   "component.forminscription.err.invalidemail"));
            message.setSeverity(
                      javax.faces.application.FacesMessage.SEVERITY_ERROR);
            throw new javax.faces.validator.ValidatorException(message);
        }
    }
    
    /**
     * Set password edited print
     * 
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
     * 
     * @return null
     */
    public java.lang.String getSelectedPanel() { return selectedPanel; }
    
    /**
     * Set the field password undisplay
     * 
     * @return null
     */
    public java.lang.String cancelAction() {
        selectedPanel = "default";
        return null;
    }
    
    /**
     * Send the new password
     */
    public void sendNewPassword() {
        this.participantsList = this.getParticipantsList();
        java.util.Iterator extfor$iter =
          this.participantService.getParticipants().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.wilosuser.Participant e =
              (wilos.model.misc.wilosuser.Participant) extfor$iter.next();
            if (e.getName().equalsIgnoreCase(this.participant.getName()) &&
                  e.getFirstname().equalsIgnoreCase(
                                     this.participant.getFirstname()) &&
                  e.getEmailAddress().equalsIgnoreCase(
                                        this.participant.getEmailAddress())) {
                this.participant = e;
                this.currentPassword = e.generateNewPassword();
                this.participant.setPassword(this.currentPassword);
                this.participantService.saveParticipantWithoutEncryption(
                                          this.participant);
                this.participantService.saveParticipant(this.participant);
                this.displayPasswordEdition = false;
                this.participant = new wilos.model.misc.wilosuser.Participant();
            }
        }
    }
    
    /**
     * Getter of the list of the concrete role descriptor
     * 
     * @return the list of the concrete role descriptor
     */
    public java.util.List<wilos.model.misc.concreterole.ConcreteRoleDescriptor> getConcreteRoleDescriptors() {
        this.concreteRoleDescriptors =
          new java.util.ArrayList<wilos.model.misc.concreterole.ConcreteRoleDescriptor>(
            );
        return this.concreteRoleDescriptors;
    }
    
    /**
     * Setter of the list of the concrete role descriptor
     * 
     * @param _concreteRoleDescriptors
     *            the new list of concrete role descriptor
     */
    public void setConcreteRoleDescriptors(java.util.List<wilos.model.misc.concreterole.ConcreteRoleDescriptor> _concreteRoleDescriptors) {
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
    public void passwordEqualValidation(javax.faces.context.FacesContext _context,
                                        javax.faces.component.UIComponent _toValidate,
                                        java.lang.Object _value)
          throws javax.faces.validator.ValidatorException {
        javax.faces.component.UIComponent passcomponent =
          _toValidate.findComponent("equal1");
        java.lang.String passValue =
          (java.lang.String) passcomponent.getAttributes().get("value");
        if (!_value.equals(passValue)) {
            javax.faces.application.FacesMessage message =
              new javax.faces.application.FacesMessage();
            message.setSummary(
                      wilos.resources.LocaleBean.getText(
                                                   "component.forminscription.err.passwordnotequals"));
            message.setSeverity(
                      javax.faces.application.FacesMessage.SEVERITY_ERROR);
            throw new javax.faces.validator.ValidatorException(message);
        }
    }
    
    /**
     * Getter of participant
     * 
     * @return the participant
     */
    public wilos.model.misc.wilosuser.Participant getParticipant() {
        return this.participant;
    }
    
    /**
     * Setter of participant
     * 
     * @param _participant
     *            the new participant
     */
    public void setParticipant(wilos.model.misc.wilosuser.Participant _participant) {
        this.participant = _participant;
    }
    
    /**
     * Getter of participant service
     * 
     * @return the participant service
     */
    public wilos.business.services.misc.wilosuser.ParticipantService getParticipantService() {
        return this.participantService;
    }
    
    /**
     * Setter of participantService
     * 
     * @param _participantService
     *            the new participant service
     */
    public void setParticipantService(wilos.business.services.misc.wilosuser.ParticipantService _participantService) {
        this.participantService = _participantService;
    }
    
    /**
     * Getter of projectService.
     * 
     * @return the projectService.
     */
    public wilos.business.services.misc.project.ProjectService getProjectService() {
        return this.projectService;
    }
    
    /**
     * Setter of projectService.
     * 
     * @param _projectService
     *            the new project service
     */
    public void setProjectService(wilos.business.services.misc.project.ProjectService _projectService) {
        this.projectService = _projectService;
    }
    
    /**
     * Getter of loginService.
     * 
     * @return the loginService.
     */
    public wilos.business.services.misc.wilosuser.LoginService getLoginService() {
        return this.loginService;
    }
    
    /**
     * Setter of loginService.
     * 
     * @param _loginService
     *            the new login service
     */
    public void setLoginService(wilos.business.services.misc.wilosuser.LoginService _loginService) {
        this.loginService = _loginService;
    }
    
    /**
     * Getter of passwordConfirmation.
     * 
     * @return the passwordConfirmation.
     */
    public java.lang.String getPasswordConfirmation() {
        return this.passwordConfirmation;
    }
    
    /**
     * Setter of passwordConfirmation.
     * 
     * @param _passwordConfirmation
     *            the new password confirmation
     */
    public void setPasswordConfirmation(java.lang.String _passwordConfirmation) {
        this.passwordConfirmation = _passwordConfirmation;
    }
    
    /**
     * Getter of participantsList.
     * 
     * @return the participantsList.
     */
    public java.util.List<wilos.model.misc.wilosuser.Participant> getParticipantsList() {
        this.participantsList =
          new java.util.ArrayList<wilos.model.misc.wilosuser.Participant>();
        participantsList.addAll(this.participantService.getParticipants());
        return this.participantsList;
    }
    
    /**
     * Setter of participantsList.
     * 
     * @param _participantsList
     *            ths new participant list
     */
    public void setParticipantsList(java.util.List<wilos.model.misc.wilosuser.Participant> _participantsList) {
        this.participantsList = _participantsList;
    }
    
    /**
     * Getter of the list of affected projects
     * 
     * @return the list of affected projects
     */
    public java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> getAffectedProjectsList() {
        wilos.model.misc.wilosuser.Participant user =
          getParticipantFromSession();
        if (user instanceof wilos.model.misc.wilosuser.Participant) {
            this.affectedProjectsList =
              new java.util.ArrayList<java.util.HashMap<java.lang.String,
              java.lang.Object>>();
            java.util.HashMap<wilos.model.misc.project.Project,
            java.lang.Boolean> plist =
              new java.util.HashMap<wilos.model.misc.project.Project,
            java.lang.Boolean>();
            plist = (java.util.HashMap<wilos.model.misc.project.Project,
                    java.lang.Boolean>)
                      this.participantService.getProjectsForAParticipant(user);
            java.util.Iterator extfor$iter = plist.keySet().iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.misc.project.Project currentProject =
                  (wilos.model.misc.project.Project) extfor$iter.next();
                java.util.HashMap<java.lang.String,
                java.lang.Object> line = new java.util.HashMap<java.lang.String,
                java.lang.Object>();
                line.put("project_id", currentProject.getId());
                line.put("affected", plist.get(currentProject));
                if (!plist.get(currentProject)) {
                    line.put("selectItem", currentProject.getId() + "1");
                } else {
                    line.put("selectItem", currentProject.getId() + "2");
                }
                line.put("name", currentProject.getConcreteName());
                line.put("creationDate",
                         formatter.format(currentProject.getCreationDate()));
                line.put("launchingDate",
                         formatter.format(currentProject.getLaunchingDate()));
                line.put("description", currentProject.getDescription());
                wilos.model.misc.wilosuser.Participant projectManager =
                  currentProject.getProjectManager();
                java.lang.String name = null;
                if (projectManager == null) {
                    name =
                      wilos.resources.LocaleBean.getText(
                                                   "component.table1participantprojectManager.noAffectation");
                    line.put("visibleAffectManager", true);
                    line.put("displayOptionProjectManager", true);
                } else {
                    name = projectManager.getFirstname() + " " +
                           projectManager.getName();
                    if (projectManager.getId().equals(user.getId())) {
                        line.put("selectItem", currentProject.getId() + "3");
                        line.put("displayOptionProjectManager", true);
                    } else {
                        line.put("displayOptionProjectManager", false);
                    }
                }
                line.put("projectManagerName", name);
                if (this.checkTasks(line)) {
                    line.put(
                           "activeTasks",
                           wilos.resources.LocaleBean.getText(
                                                        "component.tableparticipantproject.noActiveTasks"));
                    line.put("disabled", false);
                }
                else {
                    line.put(
                           "activeTasks",
                           wilos.resources.LocaleBean.getText(
                                                        "component.tableparticipantproject.activeTasks"));
                    line.put("disabled",
                             (java.lang.Boolean) line.get("affected"));
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
    public void setAffectedProjectsList(java.util.List<java.util.HashMap<java.lang.String,
                                        java.lang.Object>> affectedProjectsList) {
        this.affectedProjectsList = affectedProjectsList;
    }
    
    public java.lang.Boolean checkTasks(java.util.HashMap<java.lang.String,
                                        java.lang.Object> line) {
        wilos.model.misc.wilosuser.Participant user =
          getParticipantFromSession();
        javax.faces.context.FacesContext context =
          javax.faces.context.FacesContext.getCurrentInstance();
        this.projectService =
          (wilos.business.services.misc.project.ProjectService)
            context.getApplication().getVariableResolver().resolveVariable(
                                                             context,
                                                             "ProjectService");
        labeled_1 :
        {
            java.lang.Boolean allowed = true;
            java.lang.String project_id = (java.lang.String) line.get("project_id");
            java.util.List<wilos.model.misc.concretetask.ConcreteTaskDescriptor> concreteTasks =
                    this.projectService.getProcessService(
                    ).getTaskDescriptorService(
                    ).getConcreteTaskDescriptorDao(
                    ).getAllConcreteTaskDescriptorsForProject(
                            project_id);
            java.util.Iterator extfor$iter = concreteTasks.iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.concretetask.ConcreteTaskDescriptor ctd =
                        (wilos.model.misc.concretetask.ConcreteTaskDescriptor)
                                extfor$iter.next();
                if (ctd.getState().equals(wilos.utils.Constantes.State.STARTED))
                {
                    wilos.model.misc.concreterole.ConcreteRoleDescriptor crd =
                            ctd.getMainConcreteRoleDescriptor();
                    wilos.model.misc.wilosuser.Participant participant =
                            this.concreteRoleDescriptorService.getParticipant(crd);
                    java.lang.String login =
                            this.participantService.getParticipantLogin(participant);
                    if (login.equals(user.getLogin()))
                    {
                        allowed = false;
                    }
                }
            }
        }
        return allowed;
    }
    
    /**
     * Method permits to save the affection of projects
     */
    public void saveProjectsAffectation() {
        wilos.model.misc.wilosuser.Participant user =
          getParticipantFromSession();
        java.util.HashMap<java.lang.String,
        java.lang.Boolean> affectedProjects =
          new java.util.HashMap<java.lang.String, java.lang.Boolean>();
        java.util.Iterator extfor$iter =
          this.affectedProjectsList.iterator();
        while (extfor$iter.hasNext()) {
            java.util.HashMap<java.lang.String,
            java.lang.Object> ligne = (java.util.HashMap<java.lang.String,
                                      java.lang.Object>) extfor$iter.next();
            java.lang.Boolean testAffectation = (java.lang.Boolean)
                                                  ligne.get("affected");
            java.lang.String project_id = (java.lang.String)
                                            ligne.get("project_id");
            if (!testAffectation) {
                if (wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                     wilos.presentation.web.utils.WebSessionService.PROJECT_ID) !=
                      null) {
                    if (wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                         wilos.presentation.web.utils.WebSessionService.PROJECT_ID).equals(
                                                                                                                                      project_id) &&
                          !testAffectation) {
                        this.visiblePopup = true;
                        wilos.presentation.web.tree.TreeBean treeBean =
                          (wilos.presentation.web.tree.TreeBean)
                            wilos.presentation.web.utils.WebCommonService.getBean(
                                                                            "TreeBean");
                        treeBean.cleanTreeDisplay();
                    }
                }
            }
            affectedProjects.put(project_id, testAffectation);
        }
        this.participantService.saveProjectsForAParticipant(user,
                                                            affectedProjects);
    }
    
    /**
     * Method permits to change the mode
     * 
     * @param evt
     *            the event
     */
    public void changeModeActionListener(javax.faces.event.ValueChangeEvent evt) {
        java.lang.Boolean isForAssignment = (java.lang.Boolean)
                                              evt.getNewValue();
        this.selectedProjectId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                             wilos.presentation.web.utils.WebSessionService.PROJECT_ID);
        if (!isForAssignment) {
            this.visiblePopup = true;
        }
        else {
            this.participantService.saveProjectForAProjectManager(
                                      this.getParticipantFromSession().getId(),
                                      this.selectedProjectId, true);
        }
    }
    
    /**
     * Assign action listener
     * 
     * @param event
     *            the event
     */
    public void assignActionListener(javax.faces.event.ActionEvent event) {
        this.selectedProjectId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                             wilos.presentation.web.utils.WebSessionService.PROJECT_ID);
        this.participantService.saveProjectForAProjectManager(
                                  this.getParticipantFromSession().getId(),
                                  this.selectedProjectId, true);
    }
    
    /**
     * Method permits to look the popup
     * 
     * @param event
     */
    public void disAssignActionListener(javax.faces.event.ActionEvent event) {
        this.visiblePopup = true;
    }
    
    /**
     * Method permits to disaffect to projects
     * 
     * @param _event
     *            the event
     */
    public void confirmDisassignment(javax.faces.event.ActionEvent _event) {
        this.participantService.saveProjectForAProjectManager(
                                  this.getParticipantFromSession().getId(),
                                  this.selectedProjectId, false);
        this.visiblePopup = false;
    }
    
    /**
     * Methods permit to cancel disassignment
     * 
     * @param _event
     *            the event
     */
    public void cancelDisassignment(javax.faces.event.ActionEvent _event) {
        this.visiblePopup = false;
    }
    
    /**
     * Return the arraylist to display it into a datatable the arraylist
     * represent the projects list affected to the participant which have no
     * projectManager
     * 
     * @return the list
     */
    public java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> getManageableProjectsList() {
        wilos.model.misc.wilosuser.Participant user =
          getParticipantFromSession();
        if (user instanceof wilos.model.misc.wilosuser.Participant) {
            this.manageableProjectsList =
              new java.util.ArrayList<java.util.HashMap<java.lang.String,
              java.lang.Object>>();
            java.util.HashMap<wilos.model.misc.project.Project,
            wilos.model.misc.wilosuser.Participant> manageableProjects =
              (java.util.HashMap<wilos.model.misc.project.Project,
              wilos.model.misc.wilosuser.Participant>)
                this.participantService.getManageableProjectsForAParticipant(
                                          user);
            java.util.Iterator extfor$iter =
              manageableProjects.keySet().iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.misc.project.Project currentProject =
                  (wilos.model.misc.project.Project) extfor$iter.next();
                wilos.model.misc.wilosuser.Participant projectManager =
                  manageableProjects.get(currentProject);
                java.util.HashMap<java.lang.String,
                java.lang.Object> ligne =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                ligne.put("project_id", currentProject.getId());
                ligne.put("name", currentProject.getConcreteName());
                ligne.put("creationDate",
                          formatter.format(currentProject.getCreationDate()));
                ligne.put("launchingDate",
                          formatter.format(currentProject.getLaunchingDate()));
                ligne.put("description", currentProject.getDescription());
                if (projectManager == null) {
                    ligne.put(
                            "projectManagerName",
                            wilos.resources.LocaleBean.getText(
                                                         "component.table1participantprojectManager.noAffectation"));
                    ligne.put("projectManager_id", "");
                    ligne.put("affected", new java.lang.Boolean(false));
                    ligne.put("hasOtherManager", new java.lang.Boolean(false));
                    this.manageableProjectsList.add(ligne);
                }
                else {
                    java.lang.String projectManagerName =
                      projectManager.getFirstname().concat(
                                                      " " +
                                                        projectManager.getName(
                                                                         ));
                    ligne.put("projectManager_id", projectManager.getId());
                    ligne.put("projectManagerName", projectManagerName);
                    if (projectManager.getId().equals(user.getId())) {
                        ligne.put("affected", new java.lang.Boolean(true));
                        ligne.put("hasOtherManager",
                                  new java.lang.Boolean(false));
                        this.manageableProjectsList.add(ligne);
                    } else {
                        ligne.put("affected", new java.lang.Boolean(true));
                        ligne.put("hasOtherManager",
                                  new java.lang.Boolean(true));
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
    public void setManageableProjectsList(java.util.List<java.util.HashMap<java.lang.String,
                                          java.lang.Object>> manageableProjectsList) {
        this.manageableProjectsList = manageableProjectsList;
    }
    
    /**
     * This method allows to save a project manager affectation
     */
    public void saveProjectManagerAffectation() {
        wilos.model.misc.wilosuser.Participant user =
          getParticipantFromSession();
        java.util.Map<java.lang.String,
        java.lang.Boolean> affectedManagedProjects =
          new java.util.HashMap<java.lang.String, java.lang.Boolean>();
        java.util.Iterator extfor$iter =
          this.manageableProjectsList.iterator();
        while (extfor$iter.hasNext()) {
            java.util.HashMap<java.lang.String,
            java.lang.Object> ligne = (java.util.HashMap<java.lang.String,
                                      java.lang.Object>) extfor$iter.next();
            if (!((java.lang.Boolean) ligne.get("hasOtherManager"))) {
                java.lang.Boolean testAffectation = (java.lang.Boolean)
                                                      ligne.get("affected");
                java.lang.String project_id = (java.lang.String)
                                                ligne.get("project_id");
                affectedManagedProjects.put(project_id, testAffectation);
            }
        }
        this.participantService.saveManagedProjectsForAParticipant(
                                  user, affectedManagedProjects);
    }
    
    /**
     * Method which permits the getting of the object Participant which is
     * stored into the session
     * 
     * @return the participant stored into the session
     */
    private wilos.model.misc.wilosuser.Participant getParticipantFromSession() {
        java.lang.String userId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                             wilos.presentation.web.utils.WebSessionService.WILOS_USER_ID);
        wilos.model.misc.wilosuser.Participant user =
          this.participantService.getParticipant(userId);
        return user;
    }
    
    /**
     * Set the participant of the participantBean to the participant which is
     * stored into the session
     * 
     * @return ok or null if set participant from session it s ok or not
     */
    public java.lang.String getIsSetParticipantFromSession() {
        wilos.model.misc.wilosuser.Participant user =
          this.getParticipantFromSession();
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
    public void setIsSetParticipantFromSession(java.lang.String _msg) {
        this.isSetParticipantFromSession = _msg;
    }
    
    /**
     * Getter of the selected manageable project view
     * 
     * @return the selected manageable project view
     */
    public java.lang.String getSelectManageableProjectView() {
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
    public void setSelectManageableProjectView(java.lang.String selectManageableProjectView) {
        this.selectManageableProjectView = selectManageableProjectView;
    }
    
    /**
     * Getter of formatter
     * 
     * @return the formatter
     */
    public java.text.SimpleDateFormat getFormatter() { return formatter; }
    
    /**
     * Setter of formatter
     * 
     * @param formatter
     *            the new formatter
     */
    public void setFormatter(java.text.SimpleDateFormat formatter) {
        this.formatter = formatter;
    }
    
    /**
     * Getter of concreteRoleDescriptorHeaders.
     * 
     * @return the concreteRoleDescriptorHeaders.
     */
    public java.util.List<java.lang.String> getConcreteRoleDescriptorHeaders() {
        this.concreteRoleDescriptorHeaders.addAll(
                                             this.getConcreteRoleDescriptorsMap(
                                                    ).keySet());
        return this.concreteRoleDescriptorHeaders;
    }
    
    /**
     * Setter of concreteRoleDescriptorHeaders.
     * 
     * @param _concreteRoleDescriptorHeaders
     *            The concreteRoleDescriptorHeaders to set.
     */
    public void setConcreteRoleDescriptorHeaders(java.util.List<java.lang.String> _concreteRoleDescriptorHeaders) {
        this.concreteRoleDescriptorHeaders = _concreteRoleDescriptorHeaders;
    }
    
    /**
     * Getter of concreteRoleDescriptorsMap.
     * 
     * @return the concreteRoleDescriptorsMap.
     */
    public java.util.HashMap<java.lang.String,
    java.lang.Boolean> getConcreteRoleDescriptorsMap() {
        this.concreteRoleDescriptorsMap =
          new java.util.HashMap<java.lang.String, java.lang.Boolean>();
        java.util.List<wilos.model.misc.concreterole.ConcreteRoleDescriptor> concreteRoleDescriptorsForAParticipant =
          new java.util.ArrayList<wilos.model.misc.concreterole.ConcreteRoleDescriptor>(
          );
        java.util.Iterator extfor$iter =
          concreteRoleDescriptorsForAParticipant.iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concreterole.ConcreteRoleDescriptor concreteRoleDescriptor =
              (wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                extfor$iter.next();
            this.concreteRoleDescriptorsMap.put(
                                              concreteRoleDescriptor.getConcreteName(
                                                                       ), true);
        }
        return this.concreteRoleDescriptorsMap;
    }
    
    /**
     * Setter of concreteRoleDescriptorsMap.
     * 
     * @param _concreteRoleDescriptorsMap
     *            The concreteRoleDescriptorsMap to set.
     */
    public void setConcreteRoleDescriptorsMap(java.util.HashMap<java.lang.String,
                                              java.lang.Boolean> _concreteRoleDescriptorsMap) {
        this.concreteRoleDescriptorsMap = _concreteRoleDescriptorsMap;
    }
    
    /**
     * Getter of selectAffectedProjectView.
     * 
     * @return the selectAffectedProjectView.
     */
    public java.lang.String getSelectAffectedProjectView() {
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
    public void setSelectAffectedProjectView(java.lang.String _selectAffectedProjectView) {
        this.selectAffectedProjectView = _selectAffectedProjectView;
    }
    
    /**
     * Getter of selectAffectedProjectView.
     * 
     * @return the selectAffectedProjectView.
     */
    public java.lang.String getParticipantView() {
        labeled_2 :
        {
            if (this.getParticipantsList().size() == 0)
            {
                this.participantView = "participantView_null";
            } else
            {
                this.participantView = "participantView_not_null";
            }
        }
        return this.participantView;
    }
    
    /**
     * Setter of selectAffectedProjectView.
     * 
     * @param _ParticipantView
     *            The selectAffectedProjectView to set.
     */
    public void setParticipantView(java.lang.String _participantView) {
        this.participantView = _participantView;
    }
    
    /**
     * Getter of cleanBean.
     * 
     * @return the cleanBean.
     */
    public java.lang.String getCleanBean() {
        this.participant = new wilos.model.misc.wilosuser.Participant();
        this.cleanBean = "ok";
        return this.cleanBean;
    }
    
    /**
     * Setter of cleanBean.
     * 
     * @param _cleanBean
     *            The cleanBean to set.
     */
    public void setCleanBean(java.lang.String _cleanBean) {
        this.cleanBean = _cleanBean;
    }
    
    /**
     * Getter of the boolean if the popup is visible or not
     * 
     * @return true or false visiblePopup
     */
    public boolean isVisiblePopup() { return visiblePopup; }
    
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
    public java.lang.String getCurrentPassword() { return currentPassword; }
    
    /**
     * Setter of the current password
     * 
     * @param _currentPassword
     *            the new current password
     */
    public void setCurrentPassword(java.lang.String _currentPassword) {
        this.currentPassword = _currentPassword;
    }
    
    /**
     * Getter of isDisplayPasswordEdition
     * 
     * @return true if the field of password is display else false
     */
    public boolean isDisplayPasswordEdition() { return displayPasswordEdition; }
    
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
    public wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService getConcreteRoleDescriptorService() {
        return concreteRoleDescriptorService;
    }
    
    /**
     * Setter of concreteRoleDescriptorService
     * 
     * @param concreteRoleDescriptorService
     *            the new concrete role descriptor service
     */
    public void setConcreteRoleDescriptorService(wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService concreteRoleDescriptorService) {
        this.concreteRoleDescriptorService = concreteRoleDescriptorService;
    }
    
    /**
     * Delete selected participant
     */
    public void deleteParticipant(javax.faces.event.ActionEvent _evt) {
        javax.faces.context.FacesContext context =
          javax.faces.context.FacesContext.getCurrentInstance();
        java.util.Map map =
          context.getExternalContext().getRequestParameterMap();
        this.setTestDelete((java.lang.String) map.get("loginParti"));
        this.visiblePopup = true;
    }
    
    /**
     * Test for delete participant
     * 
     * @return value of the participant
     */
    public java.lang.String getTestDelete() { return testDelete; }
    
    /**
     * Test for delete participant
     * 
     * @return value of the participant
     */
    public void setTestDelete(java.lang.String deleteParticipant) {
        this.testDelete = deleteParticipant;
    }
    
    /**
     * Modify selected participant
     */
    public void modifyParticipant() {
        javax.faces.context.FacesContext context =
          javax.faces.context.FacesContext.getCurrentInstance();
        java.util.Map map =
          context.getExternalContext().getRequestParameterMap();
        java.lang.String loginParticipant = (java.lang.String)
                                              map.get("loginParti");
        java.util.Iterator extfor$iter = this.participantsList.iterator();
        boolean break_0 = false;
        while (extfor$iter.hasNext() && !break_0) {
            wilos.model.misc.wilosuser.Participant parti =
              (wilos.model.misc.wilosuser.Participant) extfor$iter.next();
            if (!break_0)
                if (parti.getLogin() != null) {
                    if (!break_0)
                        if (parti.getLogin().equals(loginParticipant)) {
                            this.participant = parti;
                            break_0 = true;
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
    public void confirmDelete(javax.faces.event.ActionEvent event) {
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
     * Change the status of the user
     * 
     * @param evt
     */
    public void changeListener(javax.faces.event.ValueChangeEvent evt) {
        java.lang.String retour = evt.getNewValue().toString();
        wilos.model.misc.wilosuser.Participant user =
          getParticipantFromSession();
        java.util.Iterator<java.util.HashMap<java.lang.String,
        java.lang.Object>> it = null;
        java.util.Iterator extfor$iter =
          this.affectedProjectsList.iterator();
        boolean break_1 = false;
        while (extfor$iter.hasNext() && !break_1) {
            java.util.HashMap<java.lang.String,
            java.lang.Object> value = (java.util.HashMap<java.lang.String,
                                      java.lang.Object>) extfor$iter.next();
            java.lang.String projectId = (java.lang.String)
                                           value.get("project_id");
            wilos.model.misc.project.Project project =
              this.projectService.getProject(projectId);
            if (!break_1)
                if (retour.contains(projectId)) {
                    char test =
                      ((java.lang.String)
                         evt.getNewValue()).charAt(
                                              ((java.lang.String)
                                                 evt.getNewValue()).length() -
                                                  1);
                    boolean newAffectation = false;
                    if (!break_1)
                        if (test == '1') {
                            value.put("selectItem", projectId + "1");
                            value.put("affected", false);
                            this.saveProjectsAffectation();
                            break_1 = true;
                        }
                        else
                            if (!break_1)
                                if (test == '2') {
                                    if (!break_1)
                                        value.put(
                                                "selectItem", projectId + "2");
                                    if (!break_1)
                                        value.put(
                                                "affected", true);
                                    if (!break_1)
                                        if (project.getProjectManager() !=
                                              null) {
                                            if (!break_1)
                                                if (project.getProjectManager(
                                                              ).getId(
                                                                  ).equals(
                                                                      user.getId(
                                                                             ))) {
                                                    if (!break_1)
                                                        this.participantService.saveProjectForAProjectManager(
                                                                                  user.getId(
                                                                                         ),
                                                                                  projectId,
                                                                                  false);
                                                }
                                        }
                                    if (!break_1)
                                        this.saveProjectsAffectation();
                                    break_1 = true;
                                }
                                else
                                    if (!break_1)
                                        if (test == '3') {
                                            if (!break_1)
                                                value.put(
                                                        "selectItem",
                                                        projectId + "3");
                                            if (!break_1)
                                                value.put(
                                                        "affected", true);
                                            if (!break_1)
                                                this.saveProjectsAffectation();
                                            if (!break_1)
                                                newAffectation = true;
                                            if (!break_1)
                                                this.getManageableProjectsList(
                                                       );
                                            if (!break_1)
                                                it =
                                                  this.manageableProjectsList.iterator(
                                                                                );
                                            while (it.hasNext()) {
                                                java.util.HashMap<java.lang.String,
                                                java.lang.Object> value2 =
                                                  it.next();
                                                java.lang.String projectId2 =
                                                  (java.lang.String)
                                                    value2.get("project_id");
                                                if (retour.contains(
                                                             projectId2)) {
                                                    value2.put("affected",
                                                               newAffectation);
                                                    this.saveProjectManagerAffectation(
                                                           );
                                                }
                                            }
                                            break_1 = true;
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
        wilos.presentation.web.utils.WebCommonService.addInfoMessage(
                                                        wilos.resources.LocaleBean.getText(
                                                                                     "component.tableparticipantproject.success"));
    }
}

