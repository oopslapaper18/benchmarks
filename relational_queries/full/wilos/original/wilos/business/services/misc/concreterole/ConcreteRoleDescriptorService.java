/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) Sebastien BALARD <sbalard@wilos-project.org>
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import wilos.business.services.misc.concreteactivity.ConcreteActivityService;
import wilos.business.services.spem2.role.RoleDescriptorService;
import wilos.hibernate.misc.concreterole.ConcreteRoleDescriptorDao;
import wilos.hibernate.misc.concretetask.ConcreteTaskDescriptorDao;
import wilos.hibernate.misc.wilosuser.ParticipantDao;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement;
import wilos.model.misc.concreterole.ConcreteRoleDescriptor;
import wilos.model.misc.concretetask.ConcreteTaskDescriptor;
import wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor;
import wilos.model.misc.wilosuser.Participant;
import wilos.model.spem2.breakdownelement.BreakdownElement;
import wilos.model.spem2.role.RoleDescriptor;
import wilos.presentation.web.utils.WebSessionService;

@Transactional(propagation = Propagation.REQUIRED)
public class ConcreteRoleDescriptorService {

	private ConcreteRoleDescriptorDao concreteRoleDescriptorDao;

	private ConcreteActivityService concreteActivityService;

	private RoleDescriptorService roleDescriptorService;

	private ParticipantDao participantDao;

	private ConcreteTaskDescriptorDao concreteTaskDescriptorDao;

	/**
	 * Allows to get the participant for a concreteRoleDescriptor
	 * 
	 * @param _concreteRoleDescriptor
	 * @return the participant
	 */
	public Participant getParticipant(
			ConcreteRoleDescriptor _concreteRoleDescriptor) {
		this.getConcreteRoleDescriptorDao().getSessionFactory()
				.getCurrentSession().saveOrUpdate(_concreteRoleDescriptor);
		return _concreteRoleDescriptor.getParticipant();
	}

	/**
	 * Allows to save the concreteRoleDescriptor
	 * 
	 * @param _concreteRoleDescriptor
	 */
	public void saveConcreteRoleDescriptor(
			ConcreteRoleDescriptor _concreteRoleDescriptor) {
		this.concreteRoleDescriptorDao
				.saveOrUpdateConcreteRoleDescriptor(_concreteRoleDescriptor);
	}

	/**
	 * Affecte un participant a un role
	 * 
	 * @param _concreteRoleDescriptor
	 * @param _participant
	 * @return
	 */
	public ConcreteRoleDescriptor addPartiConcreteRoleDescriptor(
			ConcreteRoleDescriptor _concreteRoleDescriptor,
			Participant _participant) {
		_concreteRoleDescriptor = this
				.getConcreteRoleDescriptor(_concreteRoleDescriptor.getId());
		if (_concreteRoleDescriptor != null) {
			Participant user = _concreteRoleDescriptor.getParticipant();
			if (user == null) {
				// on ajoute le participant et on enregistre
				_concreteRoleDescriptor.setParticipant(_participant);
				this.saveConcreteRoleDescriptor(_concreteRoleDescriptor);
				// on retourne la nouvelle instance
				return _concreteRoleDescriptor;
			} else {
				_concreteRoleDescriptor.getParticipant().getName();
				return _concreteRoleDescriptor;
			}
		} else {
			// si supprimer on retourne null
			return null;
		}
	}

	/**
	 * Allows to get the list of all concreteRoleDecriptors
	 * 
	 * @return the list of all concreteRoleDecriptors
	 */
	public List<ConcreteRoleDescriptor> getAllConcreteRoleDescriptors() {
		return this.getConcreteRoleDescriptorDao()
				.getAllConcreteRoleDescriptors();
	}

	/**
	 * Allows to get the list of primary concreteTaskDescriptors for a
	 * concreteRole id
	 * 
	 * @param _concreteRoleId
	 * @return the list of primary concreteTaskDescriptors
	 */
	public List<ConcreteTaskDescriptor> getPrimaryConcreteTaskDescriptors(
			String _concreteRoleId) {
		return this.concreteRoleDescriptorDao
				.getMainConcreteTaskDescriptorsForConcreteRoleDescriptor(_concreteRoleId);
	}

	/**
	 * Allows to get the set of primary concreteTaskDescriptors for a
	 * concreteRoleDescriptor
	 * 
	 * @param _concreteRoleDescriptor
	 * @return the set of primary concreteTaskDescriptors
	 */
	public Set<ConcreteTaskDescriptor> getPrimaryConcreteTaskDescriptors(
			ConcreteRoleDescriptor _concreteRoleDescriptor) {
		Set<ConcreteTaskDescriptor> concreteTaskDescriptors = new HashSet<ConcreteTaskDescriptor>();
		this.concreteRoleDescriptorDao.getSessionFactory().getCurrentSession()
				.saveOrUpdate(_concreteRoleDescriptor);
		this.concreteRoleDescriptorDao.getSessionFactory().getCurrentSession()
				.refresh(_concreteRoleDescriptor);
		for (ConcreteTaskDescriptor concreteTaskDescriptor : _concreteRoleDescriptor
				.getPrimaryConcreteTaskDescriptors()) {
			concreteTaskDescriptors.add(concreteTaskDescriptor);
		}
		return concreteTaskDescriptors;
	}

	/**
	 * Allows to get the list of concreteTaskDesscriptors for a
	 * concreteRoleDescriptor
	 * 
	 * @param _concreteRoleDescriptor
	 * @return the list of concreteTaskDesscriptors
	 */
	public List<ConcreteTaskDescriptor> getAllConcreteTaskDescriptorsForConcreteRoleDescriptor(
			ConcreteRoleDescriptor _concreteRoleDescriptor) {
		return this.concreteRoleDescriptorDao
				.getMainConcreteTaskDescriptorsForConcreteRoleDescriptor(_concreteRoleDescriptor
						.getId());
	}

	/**
	 * Allows to get the list of super concreteActivities for a
	 * concreteRoleDescriptor id
	 * 
	 * @param _crdid
	 * @return
	 */
	public List<ConcreteActivity> getSuperConcreteActivities(String _crdid) {
		ConcreteRoleDescriptor crd = this.getConcreteRoleDescriptor(_crdid);
		List<ConcreteActivity> listTmp = this.concreteActivityService
				.getAllConcreteActivities();
		List<ConcreteActivity> listToReturn = new ArrayList<ConcreteActivity>();

		for (ConcreteActivity ca : listTmp) {
			if (ca.getConcreteBreakdownElements().contains(crd)) {
				listToReturn.add(ca);
			}
		}

		return listToReturn;
	}

	/**
	 * 
	 * @param _conConcreteRoleDescriptor
	 * @return
	 */
	public ConcreteRoleDescriptor deleteConcreteRoleDescriptor(
			ConcreteRoleDescriptor _concreteRoleDescriptor) {
		_concreteRoleDescriptor = this
				.getConcreteRoleDescriptor(_concreteRoleDescriptor.getId());
		// role
		if (_concreteRoleDescriptor != null
				&& _concreteRoleDescriptor.getParticipant() == null) {
			Set<ConcreteActivity> lca = _concreteRoleDescriptor
					.getSuperConcreteActivities();
			for (ConcreteActivity ca : lca) {
				Set<ConcreteBreakdownElement> lcbe = ca
						.getConcreteBreakdownElements();
				for (ConcreteBreakdownElement cbe : lcbe) {
					if (cbe instanceof ConcreteWorkProductDescriptor) {
						ConcreteWorkProductDescriptor cwpd = (ConcreteWorkProductDescriptor) cbe;
						// est le meme que le role
						if (cwpd.getWorkProductDescriptor()
								.getResponsibleRoleDescriptor() != null) {
							if ((_concreteRoleDescriptor.getRoleDescriptor()
									.getId()).equals(cwpd
									.getWorkProductDescriptor()
									.getResponsibleRoleDescriptor().getId())) {
								// si oui on ne supprime pas
								return _concreteRoleDescriptor;
							}
						}
					}
					if (cbe instanceof ConcreteTaskDescriptor) {
						ConcreteTaskDescriptor ctd = (ConcreteTaskDescriptor) cbe;
						// est le meme que le role
						if (ctd.getTaskDescriptor().getMainRole() != null) {
							if ((_concreteRoleDescriptor.getRoleDescriptor()
									.getId()).equals(ctd.getTaskDescriptor()
									.getMainRole().getId())) {
								// si oui on ne supprime pas
								return _concreteRoleDescriptor;
							}
						}
					}
				}
			}
			// on supprime le role et on renvoie null
			this.removeConcreteRoleDescriptor(_concreteRoleDescriptor);
		} else if (_concreteRoleDescriptor.getParticipant() != null) {
			// bug : si on ne le fait pas plantage de l'application car il ne
			// trouve pas le nom du participant ...
			_concreteRoleDescriptor.getParticipant().getName();
			// si un participant est affecte, on ne fait rien et on renvoie la
			// nouvelle instance de role
			return _concreteRoleDescriptor;
		}
		return null;
	}

	/**
	 * Allows to delete a concreteRoleDescriptor
	 */
	public void removeConcreteRoleDescriptor(
			ConcreteRoleDescriptor _concreteRoledescriptor) {
		this.concreteRoleDescriptorDao.getSessionFactory().getCurrentSession()
				.saveOrUpdate(_concreteRoledescriptor);
		// super activities automaintenance
		for (ConcreteActivity sca : _concreteRoledescriptor
				.getSuperConcreteActivities()) {
			sca.getConcreteBreakdownElements().remove(_concreteRoledescriptor);
			this.concreteActivityService.saveConcreteActivity(sca);
		}
		// RoleDescriptor automaintenance
		RoleDescriptor rd = _concreteRoledescriptor.getRoleDescriptor();
		// flush current cached version of object rd
		this.roleDescriptorService.getRoleDescriptorDao().getSessionFactory()
				.getCurrentSession().evict(rd);
		// get current instance
		this.roleDescriptorService.getRoleDescriptorDao().getSessionFactory()
				.getCurrentSession().saveOrUpdate(rd);
		// refresh collections
		this.roleDescriptorService.getRoleDescriptorDao().getSessionFactory()
				.getCurrentSession().refresh(rd);

		rd.removeConcreteRoleDescriptor(_concreteRoledescriptor);
		this.roleDescriptorService.getConcreteRoleDescriptorDao()
				.getSessionFactory().getCurrentSession().delete(
						_concreteRoledescriptor);
		this.roleDescriptorService.getRoleDescriptorDao().getSessionFactory()
				.getCurrentSession().saveOrUpdate(rd);

	}

	/**
	 * Return concreteRoleDescriptor for a project list
	 * 
	 * @return list of concreteRoleDescriptors
	 */
	public List<ConcreteRoleDescriptor> getAllConcreteRoleDescriptorsForProject(
			String _projectId) {
		return this.getConcreteRoleDescriptorDao()
				.getAllConcreteRoleDescriptorsForProject(_projectId);
	}

	/**
	 * Allows to get a concreteRoleDescriptor with its id
	 * 
	 * @param _id
	 * @return concreteRoleDescriptor
	 */
	public ConcreteRoleDescriptor getConcreteRoleDescriptor(String _id) {
		return this.concreteRoleDescriptorDao.getConcreteRoleDescriptor(_id);
	}

	/*
	 * public String getConcreteRoleDescriptorName(String
	 * _concreteRoleDescriptorId) { return
	 * this.concreteRoleDescriptorDao.getConcreteRoleDescriptorName
	 * (_concreteRoleDescriptorId); }
	 */

	/**
	 * Allows to get the concreteRoleDescriptorDao
	 * 
	 * @return concreteRoleDescriptorDao
	 */
	public ConcreteRoleDescriptorDao getConcreteRoleDescriptorDao() {
		return concreteRoleDescriptorDao;
	}

	/**
	 * Allows to set the concreteRoleDescriptorDao
	 * 
	 * @param _concreteRoleDescriptorDao
	 */
	public void setConcreteRoleDescriptorDao(
			ConcreteRoleDescriptorDao _concreteRoleDescriptorDao) {
		this.concreteRoleDescriptorDao = _concreteRoleDescriptorDao;
	}

	/**
	 * Allows to get the concreteActivityService
	 * 
	 * @return the concretActivityService
	 */
	public ConcreteActivityService getConcreteActivityService() {
		return concreteActivityService;
	}

	/**
	 * Allows to set the concreteActivityService
	 * 
	 * @param concretActivityService
	 */
	public void setConcreteActivityService(
			ConcreteActivityService _concreteActivityService) {
		this.concreteActivityService = _concreteActivityService;
	}

	/**
	 * Allows to get the list of concreteRoleDescriptor for a roleDescriptor
	 * 
	 * @param _roleDescriptorId
	 * @return the list of concreteRoleDescriptor
	 */
	public List<ConcreteRoleDescriptor> getAllConcreteRoleDescriptorForARoleDescriptor(
			String _roleDescriptorId) {
		return this.concreteRoleDescriptorDao
				.getAllConcreteRoleDescriptorsForARoleDescriptor(_roleDescriptorId);
	}

	/**
	 * Allows to get the roleDescriptorService
	 * 
	 * @return the roleDescriptorService
	 */
	public RoleDescriptorService getRoleDescriptorService() {
		return roleDescriptorService;
	}

	/**
	 * Allows to set the roleDescriptorService
	 * 
	 * @param roleDescriptorService
	 */
	public void setRoleDescriptorService(
			RoleDescriptorService roleDescriptorService) {
		this.roleDescriptorService = roleDescriptorService;
	}

	/**
	 * Allows to create an out of process concrete role
	 * 
	 * @param _user
	 *            ,_ctd
	 */
	public RoleDescriptor createOutOfProcessConcreteRoleDescriptor(
			Participant _user, ConcreteTaskDescriptor _ctd) {
		RoleDescriptor rd = new RoleDescriptor();
		// rd.getRoleDefinition().getId();

		rd.setPresentationName("No Role");
		rd.setIsOptional(true);
		rd.setIsPlanned(false);
		rd.setHasMultipleOccurrences(true);

		rd.setGuid("No Role");
		rd.setDescription("outprocess role");

		this.getRoleDescriptorService().saveRoleDescriptor(rd);

		ConcreteRoleDescriptor crd = new ConcreteRoleDescriptor();
		// crd.setRoleDescriptor(rd);
		crd.addRoleDescriptor(rd);
		crd.addBreakdownElement(rd);
		crd.addPrimaryConcreteTaskDescriptor(_ctd);
		crd.setConcreteName(rd.getPresentationName() + "#1");
		crd.setInstanciationOrder(1);
		// TODO Wilos 2
		String projectId = (String) WebSessionService
				.getAttribute(WebSessionService.PROJECT_ID);

		crd.setProject(_ctd.getProject());

		crd.setParticipant(_user);

		this.saveConcreteRoleDescriptor(crd);

		return rd;

	}

	/**
	 * Allows to get the participantDao
	 * 
	 * @return the participantDao
	 */
	public ParticipantDao getParticipantDao() {
		return participantDao;
	}

	/**
	 * Allows to set the participantDao
	 * 
	 * @param _participantDao
	 */
	public void setParticipantDao(ParticipantDao _participantDao) {
		this.participantDao = _participantDao;
	}

	/**
	 * Allows to get the concreteTaskDescriptorDao
	 * 
	 * @return the concreteTaskDescriptorDao
	 */
	public ConcreteTaskDescriptorDao getConcreteTaskDescriptorDao() {
		return concreteTaskDescriptorDao;
	}

	/**
	 * Allows to set the concreteTaskDescriptorDao
	 * 
	 * @param _concreteTaskDescriptorDao
	 */
	public void setConcreteTaskDescriptorDao(
			ConcreteTaskDescriptorDao _concreteTaskDescriptorDao) {
		this.concreteTaskDescriptorDao = _concreteTaskDescriptorDao;
	}

	public String getNameConcreteRoleDescriptor(ConcreteRoleDescriptor _crd) {
		this.concreteRoleDescriptorDao.saveOrUpdateConcreteRoleDescriptor(_crd);
		return _crd.getRoleDescriptor().getPresentationName();
	}

}
