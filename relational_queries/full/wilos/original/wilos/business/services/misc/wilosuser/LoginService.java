/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) 2007 Sebastien BALARD <sbalard@wilos-project.org>
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

package wilos.business.services.misc.wilosuser;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import wilos.hibernate.misc.wilosuser.WilosUserDao;
import wilos.model.misc.wilosuser.Participant;
import wilos.model.misc.wilosuser.WilosUser;

/**
 * The services used by any WilosUser to log into the application
 * 
 */
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class LoginService {

	private WilosUserDao wilosUserDao;

	protected final Log logger = null; //LogFactory.getLog(this.getClass());

	/**
	 * Allows to get the wilosUserDao
	 * 
	 * @return the wilosUserDao.
	 */
	public WilosUserDao getWilosUserDao() {
		return this.wilosUserDao;
	}

	/**
	 * Allows to set the wilosUserDao
	 * 
	 * @param _wilosUserDao
	 * 
	 */
	public void setWilosUserDao(WilosUserDao _wilosUserDao) {
		this.wilosUserDao = _wilosUserDao;
	}

	/**
	 * Allows to check if the couple user/password is present
	 * 
	 * @param _login
	 * @param _password
	 * 
	 * @return The WilosUser if the login and the password matches, else null
	 */
	public WilosUser getAuthentifiedUser(String _login, String _password) {
		WilosUser wilosUsers = this.wilosUserDao.getUserByLogin(_login);
		if (wilosUsers.getPassword().equals(_password)) {
			return wilosUsers;
		}
		return null;
	}

	/**
	 * Allows to check if the login exists
	 * 
	 * @param _login
	 * @return True is the login is already present
	 */
	public boolean loginExist(String _login) {
		boolean found = false;
		String userLogin;
		List<WilosUser> wilosUsers = this.wilosUserDao.getAllWilosUsers();
		for (WilosUser user : wilosUsers) {
			if (user.getLogin() != null) {
				userLogin = user.getLogin().toUpperCase();
				if (userLogin.equals(_login.toUpperCase()))
					found = true;
			}
		}
		return found;
	}

	/**
	 * Allows to check if the login exists
	 * 
	 * @param _login
	 * @return True is the login is already present
	 */
	public boolean loginExist(String _login, String _login_old) {
		boolean found = false;
		String userLogin;
		List<WilosUser> wilosUsers = this.wilosUserDao.getAllWilosUsers();
		for (WilosUser user : wilosUsers) {
			if (user.getLogin() != null) {
				userLogin = user.getLogin().toUpperCase();
				if(!userLogin.equalsIgnoreCase(_login_old)){
					if (userLogin.equals(_login.toUpperCase())){
						found = true;
					}
				}
				}
			}
		return found;
	}
	/**
	 * 
	 * Allows to check if the user is a participant
	 * 
	 * @param wilosuser
	 * @return true if the parameter is a Participant
	 */
	public boolean isParticipant(WilosUser wilosuser) {
		if (wilosuser instanceof Participant)
			return true;
		else
			return false;
	}


}
