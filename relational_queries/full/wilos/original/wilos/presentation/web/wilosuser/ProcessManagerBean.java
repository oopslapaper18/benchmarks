/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
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
 * Managed-Bean link to processmanager_create.jsp
 * 
 */
public class ProcessManagerBean {

    	/** service of process manager*/
	private ProcessManagerService processManagerService;

	/** the process manager*/
	private WilosUser processManager;

	/** service of login*/
	private LoginService loginService;

	/** the field of password confirmation*/
	private String passwordConfirmation;

	/** list of process manager*/
	private List<WilosUser> processManagerList;

	/** the view of process manager*/
	private String processManagerView;

	/** the log*/
	protected final Log logger = null; //LogFactory.getLog(this.getClass());

	/**
	 * Constructor of ProcessManagerBean
	 * 
	 */
	public ProcessManagerBean() {
		this.processManager = new WilosUser();
	}

	/**
	 * Method for saving processManager data from form
	 */
	public void saveProcessManagerAction() {
		boolean error = false;
		// test if the fields are correctly completed
		if (this.processManager.getName().trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean.getText("component.processmanagercreate.err.lastnameRequired"));
		}
		if (this.processManager.getFirstname().trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean.getText("component.processmanagercreate.err.firstnameRequired"));
		}
		if (this.processManager.getLogin().trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean.getText("component.processmanagercreate.err.loginRequired"));
		}
		if (this.processManager.getPassword().trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean.getText("component.processmanagercreate.err.passwordRequired"));
		}
		if (this.passwordConfirmation.trim().length() == 0) {
			error = true;
			WebCommonService
					.addErrorMessage(LocaleBean.getText("component.processmanagercreate.err.confirmpasswordRequired"));
		}

		if (!error) {
			if (this.loginService.loginExist(this.processManager.getLogin()
					.trim())) {
				WebCommonService
						.addErrorMessage(LocaleBean.getText("component.processmanagercreate.err.loginalreadyexist"));
			} else {
				this.processManagerService
						.saveProcessManager(this.processManager);
				WebCommonService
						.addInfoMessage(LocaleBean.getText("component.processmanagercreate.success"));
			}
		}
		this.processManager = new WilosUser();
		this.passwordConfirmation = new String();
	}

	/**
	 * 
	 * Method for control if the password and the confirme password are the same
	 * @param _context the contexte
	 * @param _toValidate validate
	 * @param _value value of password
	 * @throws ValidatorException exception validator
	 */
	public void passwordEqualValidation(FacesContext _context,
			UIComponent _toValidate, Object _value) throws ValidatorException {

		UIComponent passcomponent = _toValidate.findComponent("equal1PM");
		String passValue = (String) passcomponent.getAttributes().get("value");

		if (!_value.equals(passValue)) {
			FacesMessage message = new FacesMessage();
			message
					.setSummary(LocaleBean.getText("component.processmanagercreate.err.passwordnotequals"));
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
	}

	/**
	 * Getter of processManager.
	 * 
	 * @return the processManager.
	 */
	public WilosUser getProcessManager() {
		return this.processManager;
	}

	/**
	 * Setter of processManager.
	 * 
	 * @param _processManager
	 *            The processManager to set.
	 */
	public void setProcessManager(WilosUser _processManager) {
		this.processManager = _processManager;
	}

	/**
	 * Getter of processManagerService.
	 * 
	 * @return the processManagerService.
	 */
	public ProcessManagerService getProcessManagerService() {
		return this.processManagerService;
	}

	/**
	 * Setter of processManagerService.
	 * 
	 * @param _processManagerService
	 *            The processManagerService to set.
	 */
	public void setProcessManagerService(
			ProcessManagerService _processManagerService) {
		this.processManagerService = _processManagerService;
	}

	/**
	 * Getter of the password confirmation
	 * @return the password confirmation
	 */
	public String getPasswordConfirmation() {
		return passwordConfirmation;
	}

	/**
	 * Setter of password confirmation
	 * @param passwordConfirmation the new password confirmation
	 */
	public void setPasswordConfirmation(String passwordConfirmation) {
		this.passwordConfirmation = passwordConfirmation;
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
	 *            The loginService to set.
	 */
	public void setLoginService(LoginService _loginService) {
		this.loginService = _loginService;
	}

	/**
	 * Getter of ProcessManagerList.
	 * 
	 * @return the ProcessManagerList.
	 */
	public List<WilosUser> getProcessManagerList() {
		this.processManagerList = new ArrayList<WilosUser>();
		processManagerList.addAll(this.processManagerService
				.getProcessManagers());
		return this.processManagerList;
	}

	/**
	 * Setter of ProcessManagerList.
	 * 
	 * @param _ProcessManagerList
	 *            The ProcessManagerList to set.
	 */
	public void setProcessManagerList(List<WilosUser> _processManagerList) {
		this.processManagerList = _processManagerList;
	}

	/**
	 * Getter of processManagerView.
	 * 
	 * @return the processManagerView.
	 */
	public String getProcessManagerView() {
		if (this.getProcessManagerList().size() == 0) {
			this.processManagerView = "processManagerView_null";
		} else {
			this.processManagerView = "processManagerView_not_null";
		}
		return this.processManagerView;
	}

	/**
	 * Setter of processManagerView.
	 * 
	 * @param _processManagerView
	 *            The processManagerView to set.
	 */
	public void setProcessManagerView(String _processManagerView) {
		this.processManagerView = _processManagerView;
	}
}
