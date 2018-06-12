package wilos.
  presentation.
  web.
  wilosuser;

import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import wilos.business.services.misc.wilosuser.LoginService;
import wilos.business.services.misc.wilosuser.ProcessManagerService;
import wilos.model.misc.wilosuser.WilosUser;
import wilos.presentation.web.utils.WebCommonService;
import wilos.resources.LocaleBean;

/**
 *
 Managed-Bean
 link
 to
 processmanager_create.jsp
 * 
 */
public class ProcessManagerBean {
    /**
     service
     of
     process manager*/
    private wilos.business.services.misc.wilosuser.ProcessManagerService
      processManagerService;
    /** the process manager*/
    private wilos.model.misc.wilosuser.WilosUser processManager;
    /** service of login*/
    private wilos.business.services.misc.wilosuser.LoginService loginService;
    /** the field of password confirmation*/
    private java.lang.String passwordConfirmation;
    /** list of process manager*/
    private java.util.List<wilos.model.misc.wilosuser.WilosUser>
      processManagerList;
    /** the view of process manager*/
    private java.lang.String processManagerView;
    /** the log*/
    protected final org.apache.commons.logging.Log logger = null;
    
    /**
     * Constructor of ProcessManagerBean
     * 
     */
    public ProcessManagerBean() {
        super();
        this.processManager = new wilos.model.misc.wilosuser.WilosUser();
    }
    
    /**
     * Method for saving processManager data from form
     */
    public void saveProcessManagerAction() {
        boolean error = false;
        if (this.processManager.getName().trim().length() == 0) {
            error = true;
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText(
                      "component.processmanagercreate.err.lastnameRequired"));
        }
        if (this.processManager.getFirstname().trim().length() == 0) {
            error = true;
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText(
                      "component.processmanagercreate.err.firstnameRequired"));
        }
        if (this.processManager.getLogin().trim().length() == 0) {
            error = true;
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText(
                      "component.processmanagercreate.err.loginRequired"));
        }
        if (this.processManager.getPassword().trim().length() == 0) {
            error = true;
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText(
                      "component.processmanagercreate.err.passwordRequired"));
        }
        if (this.passwordConfirmation.trim().length() == 0) {
            error = true;
            wilos.
              presentation.
              web.
              utils.
              WebCommonService.
              addErrorMessage(
                wilos.resources.LocaleBean.
                    getText(
                      "component.processmanagercreate.err.confirmpasswordRequired"));
        }
        if (!error) {
            if (this.loginService.loginExist(
                                    this.processManager.getLogin().trim())) {
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addErrorMessage(
                    wilos.resources.LocaleBean.
                        getText(
                          "component.processmanagercreate.err.loginalreadyexist"));
            }
            else {
                this.processManagerService.saveProcessManager(
                                             this.processManager);
                wilos.
                  presentation.
                  web.
                  utils.
                  WebCommonService.
                  addInfoMessage(
                    wilos.resources.LocaleBean.
                        getText("component.processmanagercreate.success"));
            }
        }
        this.processManager = new wilos.model.misc.wilosuser.WilosUser();
        this.passwordConfirmation = new java.lang.String();
    }
    
    /**
     * 
     * Method for control if the password and the confirme password are the same
     * @param _context the contexte
     * @param _toValidate validate
     * @param _value value of password
     * @throws ValidatorException exception validator
     */
    public void passwordEqualValidation(javax.faces.context.
                                          FacesContext _context,
                                        javax.faces.component.
                                          UIComponent _toValidate,
                                        java.lang.Object _value)
          throws javax.faces.validator.ValidatorException {
        javax.faces.component.UIComponent passcomponent =
          _toValidate.findComponent("equal1PM");
        java.lang.String passValue =
          (java.lang.String) passcomponent.getAttributes().get("value");
        if (!_value.equals(passValue)) {
            javax.faces.application.FacesMessage message =
              new javax.faces.application.FacesMessage();
            message.
              setSummary(
                wilos.resources.LocaleBean.
                    getText(
                      "component.processmanagercreate.err.passwordnotequals"));
            message.setSeverity(
                      javax.faces.application.FacesMessage.SEVERITY_ERROR);
            throw new javax.faces.validator.ValidatorException(message);
        }
    }
    
    /**
     * Getter of processManager.
     * 
     * @return the processManager.
     */
    public wilos.model.misc.wilosuser.WilosUser getProcessManager() {
        return this.processManager;
    }
    
    /**
     * Setter of processManager.
     * 
     * @param _processManager
     *            The processManager to set.
     */
    public void setProcessManager(wilos.model.misc.wilosuser.
                                    WilosUser _processManager) {
        this.processManager = _processManager;
    }
    
    /**
     * Getter of processManagerService.
     * 
     * @return the processManagerService.
     */
    public wilos.business.services.misc.wilosuser.
      ProcessManagerService getProcessManagerService() {
        return this.processManagerService;
    }
    
    /**
     * Setter of processManagerService.
     * 
     * @param _processManagerService
     *            The processManagerService to set.
     */
    public void setProcessManagerService(wilos.business.services.misc.wilosuser.
                                           ProcessManagerService _processManagerService) {
        this.processManagerService = _processManagerService;
    }
    
    /**
     * Getter of the password confirmation
     * @return the password confirmation
     */
    public java.lang.String getPasswordConfirmation() {
        return passwordConfirmation;
    }
    
    /**
     * Setter of password confirmation
     * @param passwordConfirmation the new password confirmation
     */
    public void setPasswordConfirmation(java.lang.String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }
    
    /**
     * Getter of loginService.
     * 
     * @return the loginService.
     */
    public wilos.business.services.misc.wilosuser.
      LoginService getLoginService() { return this.loginService; }
    
    /**
     * Setter of loginService.
     * 
     * @param _loginService
     *            The loginService to set.
     */
    public void setLoginService(wilos.business.services.misc.wilosuser.
                                  LoginService _loginService) {
        this.loginService = _loginService;
    }
    
    /**
     * Getter of ProcessManagerList.
     * 
     * @return the ProcessManagerList.
     */
    public java.util.List<wilos.model.misc.wilosuser.
      WilosUser> getProcessManagerList() {
        this.processManagerList =
          new java.util.ArrayList<wilos.model.misc.wilosuser.WilosUser>();
        processManagerList.addAll(
                             this.processManagerService.getProcessManagers());
        return this.processManagerList;
    }
    
    /**
     * Setter of ProcessManagerList.
     * 
     * @param _ProcessManagerList
     *            The ProcessManagerList to set.
     */
    public void setProcessManagerList(java.util.List<wilos.model.misc.wilosuser.
                                        WilosUser> _processManagerList) {
        this.processManagerList = _processManagerList;
    }
    
    /**
     * Getter of processManagerView.
     * 
     * @return the processManagerView.
     */
    public java.lang.String getProcessManagerView() {
        labeled_1 :
        {
            if (this.getProcessManagerList().size() == 0)
            {
                this.processManagerView = "processManagerView_null";
            } else
            {
                this.processManagerView = "processManagerView_not_null";
            }
        }
        return this.processManagerView;
    }
    
    /**
     * Setter of processManagerView.
     * 
     * @param _processManagerView
     *            The processManagerView to set.
     */
    public void setProcessManagerView(java.lang.String _processManagerView) {
        this.processManagerView = _processManagerView;
    }
}

