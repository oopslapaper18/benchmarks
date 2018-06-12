/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) 2007 Mathieu BENOIT <mathieu-benoit@hotmail.fr>
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

package wilos.business.services.misc.concreterole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import wilos.hibernate.misc.wilosuser.ParticipantDao;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement;
import wilos.model.misc.concreterole.ConcreteRoleDescriptor;
import wilos.model.misc.wilosuser.Participant;

/**
 * The services associated to the Role
 * 
 */
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class ConcreteRoleAffectationService {

	private ParticipantDao participantDao;

	private ConcreteRoleDescriptorService concreteRoleDescriptorService;

	protected final Log logger = null; //LogFactory.getLog(this.getClass());

	/**
	 * Allows to get the list of all concreteRolesDescriptors for an activity
	 * 
	 * @param _activityId
	 * @param _projectId
	 * @return the list of all concreteRolesDescriptors for an activity
	 */
	public List<ConcreteRoleDescriptor> getAllConcreteRolesDescriptorsForActivity(
			String _activityId, String _projectId) {
		List<ConcreteRoleDescriptor> concreteRDList = new ArrayList<ConcreteRoleDescriptor>();
		List<ConcreteRoleDescriptor> globalCRD = this.concreteRoleDescriptorService
				.getAllConcreteRoleDescriptorsForProject(_projectId);
		for (ConcreteRoleDescriptor concreteRD : globalCRD) {
			concreteRD = this.concreteRoleDescriptorService
					.getConcreteRoleDescriptor(concreteRD.getId());
			List<ConcreteActivity> globalCA = new ArrayList<ConcreteActivity>();
			globalCA.addAll(concreteRD.getSuperConcreteActivities());
			for (ConcreteBreakdownElement concreteBreakdownElement : globalCA) {
				if (concreteBreakdownElement.getId().equals(_activityId)) {
					concreteRDList.add(concreteRD);
				}
			}
		}
		return concreteRDList;
	}

	/**
	 * Getter of participantDao.
	 * 
	 * @return the participantDao.
	 */
	public ParticipantDao getParticipantDao() {
		return participantDao;
	}

	/**
	 * Setter of participantDao.
	 * 
	 * @param participantDao
	 *            The participantDao to set.
	 */
	public void setParticipantDao(ParticipantDao participantDao) {
		this.participantDao = participantDao;
	}

	/**
	 * Save roles affectation for a participant.
	 * 
	 * @return the page name where navigation has to be redirected to un peu ce
	 *         retour qui craint
	 */
	public String saveParticipantConcreteRoles(
			HashMap<String, Object> rolesParticipant, String _wilosUserId) {
		Participant currentParticipant = this.participantDao
				.getParticipantById(_wilosUserId);

		ConcreteRoleDescriptor concreteRoleDescriptor = this.concreteRoleDescriptorService
				.getConcreteRoleDescriptor((String) rolesParticipant
						.get("concreteId"));
		if (!(Boolean) rolesParticipant.get("not_allowed")) {
			if ((Boolean) rolesParticipant.get("affected")) {
				currentParticipant
						.addConcreteRoleDescriptor(concreteRoleDescriptor);
			} else {
				currentParticipant
						.removeConcreteRoleDescriptor(concreteRoleDescriptor);
			}
			this.concreteRoleDescriptorService.getConcreteRoleDescriptorDao()
					.saveOrUpdateConcreteRoleDescriptor(concreteRoleDescriptor);
		}
		return "";
	}

	/**
	 * Getter of concreteRoleDescriptorService.
	 * 
	 * @return the concreteRoleDescriptorService.
	 */
	public ConcreteRoleDescriptorService getConcreteRoleDescriptorService() {
		return this.concreteRoleDescriptorService;
	}

	/**
	 * Setter of concreteRoleDescriptorService.
	 * 
	 * @param _concreteRoleDescriptorService
	 *            The concreteRoleDescriptorService to set.
	 */
	public void setConcreteRoleDescriptorService(
			ConcreteRoleDescriptorService _concreteRoleDescriptorService) {
		this.concreteRoleDescriptorService = _concreteRoleDescriptorService;
	}

	/**
	 * Method for having the participants list with the affected role
	 * 
	 * @param _wilosUserId
	 * @param _concreteId
	 * @return a hashMap with the role and a boolean to know if the participant
	 *         is affected or not
	 */
	public HashMap<String, Boolean> getParticipantAffectationForConcreteRoleDescriptor(
			String _wilosUserId, String _concreteId) {
		HashMap<String, Boolean> roleStatus = new HashMap<String, Boolean>();
		ConcreteRoleDescriptor crd = this.concreteRoleDescriptorService
				.getConcreteRoleDescriptor(_concreteId);
		if (crd.getParticipant() != null) {
			roleStatus.put("affected", new Boolean(true));
			if (crd.getParticipant().getId().equals(_wilosUserId)) {
				roleStatus.put("not_allowed", new Boolean(false));
			} else {
				roleStatus.put("not_allowed", new Boolean(true));
			}
		} else {
			roleStatus.put("affected", new Boolean(false));
			roleStatus.put("not_allowed", new Boolean(false));
		}
		return roleStatus;
	}
}
