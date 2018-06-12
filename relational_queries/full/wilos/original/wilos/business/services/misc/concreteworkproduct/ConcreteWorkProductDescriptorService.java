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

package wilos.business.services.misc.concreteworkproduct;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import wilos.business.services.misc.concreteactivity.ConcreteActivityService;
import wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService;
import wilos.business.services.misc.wilosuser.ParticipantService;
import wilos.business.services.spem2.role.RoleDescriptorService;
import wilos.business.services.spem2.workproduct.WorkProductDescriptorService;
import wilos.hibernate.misc.concreteworkproduct.ConcreteWorkProductDescriptorDao;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concreterole.ConcreteRoleDescriptor;
import wilos.model.misc.concretetask.ConcreteTaskDescriptor;
import wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor;
import wilos.model.misc.wilosuser.Participant;
import wilos.model.spem2.role.RoleDescriptor;
import wilos.model.spem2.workproduct.WorkProductDescriptor;
import wilos.utils.Constantes.State;

@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class ConcreteWorkProductDescriptorService {

	private ConcreteWorkProductDescriptorDao concreteWorkProductDescriptorDao;

	private ConcreteActivityService concreteActivityService;

	private WorkProductDescriptorService workProductDescriptorService;

	private ConcreteRoleDescriptorService concreteRoleDescriptorService;

	private RoleDescriptorService roleDescriptorService;

	private ParticipantService participantService;

	/**
	 * Allows to get the concreteWorkProductDescriptor with its id
	 * 
	 * @param _concreteWorkProductDescriptorId
	 * @return the concreteWorkProductDescriptor
	 */
	public ConcreteWorkProductDescriptor getConcreteWorkProductDescriptor(
			String _concreteWorkProductDescriptorId) {
		return this.concreteWorkProductDescriptorDao
				.getConcreteWorkProductDescriptor(_concreteWorkProductDescriptorId);
	}
	
	/**
	 * Lance la suppression d'un produit apres verification que cela est possible
	 * 
	 * @param _concreteWorkProductDescriptor
	 * @return
	 */
	public ConcreteWorkProductDescriptor deleteConcreteWorkProductDescriptor(ConcreteWorkProductDescriptor _concreteWorkProductDescriptor){
		_concreteWorkProductDescriptor = this.getConcreteWorkProductDescriptor(_concreteWorkProductDescriptor.getId());
		if(_concreteWorkProductDescriptor != null && _concreteWorkProductDescriptor.getParticipant() == null){
			Set<ConcreteTaskDescriptor> lctd = _concreteWorkProductDescriptor.getProducerConcreteTasks();
			boolean tache_commence = false;
			for(ConcreteTaskDescriptor ctd : lctd){
				if(ctd.getState().equals(State.STARTED)){
					tache_commence = true;
				}
			}
			if(!tache_commence){
				//on supprime le produit et on renvoie null
				this.removeConcreteWorkProductDescriptor(_concreteWorkProductDescriptor);		
			}else{
				return _concreteWorkProductDescriptor;
			}
		}else if(_concreteWorkProductDescriptor.getParticipant() != null){
			getParticipant(_concreteWorkProductDescriptor).getName();
			//si un participant est affecte, on ne fait rien et on renvoie la nouvelle instance de tache
			return _concreteWorkProductDescriptor;
		}
		return null;
	}

	/**
	 * Allows to remove a concreteWorkProductDescriptor
	 * 
	 * @param _concreteWorkProductDescriptor
	 */
	public void removeConcreteWorkProductDescriptor(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
		this.concreteWorkProductDescriptorDao.getSessionFactory()
				.getCurrentSession().saveOrUpdate(
						_concreteWorkProductDescriptor);

		boolean isOutOfProcess = _concreteWorkProductDescriptor
				.getWorkProductDescriptor().getIsOutOfProcess();

		// before deleting the concreteWorkProduct, we have too delete all links
		// between tasks and it
		_concreteWorkProductDescriptor.removeAllProducerConcreteTasks();
		_concreteWorkProductDescriptor.removeAllOptionalUserConcreteTasks();
		_concreteWorkProductDescriptor.removeAllMandatoryUserConcreteTasks();

		// super activities automaintenance
		for (ConcreteActivity sca : _concreteWorkProductDescriptor
				.getSuperConcreteActivities()) {
			sca.getConcreteBreakdownElements().remove(
					_concreteWorkProductDescriptor);
			this.concreteActivityService.saveConcreteActivity(sca);
		}

		// WorkProductDescriptor automaintenance
		WorkProductDescriptor wpd = _concreteWorkProductDescriptor
				.getWorkProductDescriptor();
		// flush current cached version of object wpd
		this.workProductDescriptorService.getWorkProductDescriptorDao()
				.getSessionFactory().getCurrentSession().evict(wpd);
		// get current instance
		this.workProductDescriptorService.getWorkProductDescriptorDao()
				.getSessionFactory().getCurrentSession().saveOrUpdate(wpd);
		// refresh collections
		this.workProductDescriptorService.getWorkProductDescriptorDao()
				.getSessionFactory().getCurrentSession().refresh(wpd);

		wpd.removeConcreteWorkProductDescriptor(_concreteWorkProductDescriptor);
		this.workProductDescriptorService.getConcreteWorkProductDescriptorDao()
				.getSessionFactory().getCurrentSession().delete(
						_concreteWorkProductDescriptor);
		this.workProductDescriptorService.getWorkProductDescriptorDao()
				.getSessionFactory().getCurrentSession().saveOrUpdate(wpd);

		// if _concreteWorkProductDescriptor was an out of process product, we
		// have to delete the WorkProductDescriptor associated too
		if (isOutOfProcess) {
			this.workProductDescriptorService.getWorkProductDescriptorDao()
					.deleteWorkProductDescriptor(wpd);
		}

	}

	/**
	 * Allows to save a concreteWorkProductDescriptor
	 * 
	 * @param _concreteWorkProductDescriptor
	 */
	public void saveConcreteWorkProductDescriptor(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
		this.concreteWorkProductDescriptorDao
				.saveOrUpdateConcreteWorkProductDescriptor(_concreteWorkProductDescriptor);

	}

	/**
	 * Allows to get the concreteWorkProductDescriptorDao
	 * 
	 * @return the concreteWorkProductDescriptorDao
	 */
	public ConcreteWorkProductDescriptorDao getConcreteWorkProductDescriptorDao() {
		return concreteWorkProductDescriptorDao;
	}

	/**
	 * Allows to set the concreteWorkProductDescriptorDao
	 * 
	 * @param _concreteWorkProductDescriptorDao
	 */
	public void setConcreteWorkProductDescriptorDao(
			ConcreteWorkProductDescriptorDao _concreteWorkProductDescriptorDao) {
		concreteWorkProductDescriptorDao = _concreteWorkProductDescriptorDao;
	}

	/**
	 * Allows to get the concreteActivityService
	 * 
	 * @return the concreteActivityService
	 */
	public ConcreteActivityService getConcreteActivityService() {
		return concreteActivityService;
	}

	/**
	 * Allows to set the concreteActivityService
	 * 
	 * @param _concreteActivityService
	 */
	public void setConcreteActivityService(
			ConcreteActivityService _concreteActivityService) {
		concreteActivityService = _concreteActivityService;
	}

	/**
	 * Allows to get the workProductDescriptorService
	 * 
	 * @return the workProductDescriptorService
	 */
	public WorkProductDescriptorService getWorkProductDescriptorService() {
		return workProductDescriptorService;
	}

	/**
	 * Allows to set the workProductDescriptorService
	 * 
	 * @param _workProductDescriptorService
	 */
	public void setWorkProductDescriptorService(
			WorkProductDescriptorService _workProductDescriptorService) {
		workProductDescriptorService = _workProductDescriptorService;
	}

	/**
	 * Allows to get the list of super concreteActivities for a
	 * concreteWorkProductDescriptor
	 * 
	 * @param _cwpdid
	 * @return the list of super concreteActivities
	 */
	public List<ConcreteActivity> getSuperConcreteActivities(String _cwpdid) {
		ConcreteWorkProductDescriptor crd = this
				.getConcreteWorkProductDescriptor(_cwpdid);
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
	 * Return concreteWorkProductDescriptor for a project list
	 * 
	 * @return list of concreteWorkProductDescriptor
	 */
	public List<ConcreteWorkProductDescriptor> getAllConcreteWorkProductDescriptorsForProject(
			String _projectId) {
		return this.getConcreteWorkProductDescriptorDao()
				.getAllConcreteWorkProductDescriptorsForProject(_projectId);
	}

	/**
	 * Allows to dissociate a concreteWorkProductDescriptor
	 * 
	 * @param _concreteWorkProductDescriptor
	 */
	public void dissociateConcreteWorkProductDescriptor(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor,
			Participant _participant) {

		ConcreteRoleDescriptor rmrd = _concreteWorkProductDescriptor
				.getResponsibleConcreteRoleDescriptor();

		this.concreteWorkProductDescriptorDao.getSessionFactory()
				.getCurrentSession().saveOrUpdate(
						_concreteWorkProductDescriptor);

		if (rmrd != null) {

			rmrd
					.removeConcreteWorkProductDescriptor(_concreteWorkProductDescriptor);
		}

		_concreteWorkProductDescriptor.setParticipant(null);
		_concreteWorkProductDescriptor.setState(State.CREATED);

		this.concreteWorkProductDescriptorDao
				.saveOrUpdateConcreteWorkProductDescriptor(_concreteWorkProductDescriptor);
		this.concreteRoleDescriptorService.saveConcreteRoleDescriptor(rmrd);

		Participant currentParticipant = this.participantService
				.getParticipantDao().getParticipant(_participant.getLogin());
		currentParticipant
				.removeConcreteWorkProductDescriptor(_concreteWorkProductDescriptor);

		this.participantService.getParticipantDao().saveOrUpdateParticipant(
				currentParticipant);

	}

	/**
	 * Allows to check if a concreteWorkProductDescriptor can be affected
	 * 
	 * @param _concreteWorkProductDescriptor
	 * @param _user
	 * @return true if the concreteWorkProductDescriptor can be affected, false
	 *         in the other case
	 */
	public boolean isAffectableToConcreteWorkProductDescriptor(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor,
			Participant _user) {

		if (_concreteWorkProductDescriptor.getParticipant() == null) {
			return true;
		} else {
			return _concreteWorkProductDescriptor.getParticipant().equals(
					_user.getId());
		}
	}

	/**
	 * When the user click on the button affected.
	 * 
	 * @param _concreteWorkProductDescriptor
	 */
	public ConcreteWorkProductDescriptor affectedConcreteWorkProductDescriptor(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor,
			Participant _user) {
		_concreteWorkProductDescriptor = this.concreteWorkProductDescriptorDao
				.getConcreteWorkProductDescriptor(_concreteWorkProductDescriptor
						.getId());
		if (_concreteWorkProductDescriptor != null) {
			if (_concreteWorkProductDescriptor.getParticipant() == null) {
				_concreteWorkProductDescriptor.setParticipant(_user.getId());

				this.concreteWorkProductDescriptorDao.getSessionFactory()
						.getCurrentSession().saveOrUpdate(
								_concreteWorkProductDescriptor);

				RoleDescriptor responsibleRole = _concreteWorkProductDescriptor
						.getWorkProductDescriptor()
						.getResponsibleRoleDescriptor();

				if (responsibleRole != null) {
					this.roleDescriptorService.getRoleDescriptorDao()
							.getSessionFactory().getCurrentSession()
							.saveOrUpdate(responsibleRole);

					Set<ConcreteRoleDescriptor> listecrd = responsibleRole
							.getConcreteRoleDescriptors();

					// on parcours les deux liste afin de trouver le bon
					// concreteRoledescriptor
					for (ConcreteRoleDescriptor tmpListeRd : listecrd) {

						if (tmpListeRd.getParticipant() != null) {
							if (tmpListeRd.getParticipant().getId().equals(
									_user.getId())) {
								this.concreteRoleDescriptorService
										.getConcreteRoleDescriptorDao()
										.saveOrUpdateConcreteRoleDescriptor(
												tmpListeRd);
								_concreteWorkProductDescriptor
										.addResponsibleConcreteRoleDescriptor(tmpListeRd);
								break;
							}
						}
					}
				}

				_concreteWorkProductDescriptor.setState(State.READY);

				this.concreteWorkProductDescriptorDao.getSessionFactory()
						.getCurrentSession().saveOrUpdate(
								_concreteWorkProductDescriptor);

				this.participantService
						.saveConcreteWorkProductDescriptorForAParticipant(
								_user, _concreteWorkProductDescriptor);
				//on renvoie le produit avec l'affectation de la personne
				return _concreteWorkProductDescriptor;
			} else {
				//bug : si on ne le fait pas plantage de l'application car il ne trouve pas le role responsable ...
				_concreteWorkProductDescriptor.getWorkProductDescriptor().getResponsibleRoleDescriptor();
				return _concreteWorkProductDescriptor;
			}
		}
		return null;
	}

	/**
	 * Allows to check if the affectation is right
	 * 
	 * @param _concreteWorkProductDescriptor
	 * @param _participant
	 * @return true if the affectation is right, false in the other case
	 */
	public boolean checkAffectation(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor,
			Participant _participant) {
		boolean afficher = false;

		this.concreteWorkProductDescriptorDao.getSessionFactory()
				.getCurrentSession().saveOrUpdate(
						_concreteWorkProductDescriptor);

		WorkProductDescriptor tmp = _concreteWorkProductDescriptor
				.getWorkProductDescriptor();
		RoleDescriptor tmpRoleDescriptor;
		WorkProductDescriptor tmp2 = this.workProductDescriptorService
				.getWorkProductDescriptorById(tmp.getId());

		if (tmp2.getResponsibleRoleDescriptor() == null) {
			return false;
		}
		tmpRoleDescriptor = tmp2.getResponsibleRoleDescriptor();
		RoleDescriptor rd = this.roleDescriptorService
				.getRoleDescriptor(tmpRoleDescriptor.getId());
		// recuperation des deux listes.
		// this.roleDescriptorService.
		List<ConcreteRoleDescriptor> listeRd = this.concreteRoleDescriptorService
				.getAllConcreteRoleDescriptorForARoleDescriptor(rd.getId());

		// on parcours les deux liste afin de trouver le bon
		// concreteRoledescriptor
		for (ConcreteRoleDescriptor tmpListeRd : listeRd) {

			ConcreteRoleDescriptor crd = this.concreteRoleDescriptorService
					.getConcreteRoleDescriptor(tmpListeRd.getId());
			if (crd.getParticipant() == null) {
				afficher = false;
			} else {
				if (crd.getParticipant().getId().equals(_participant.getId())) {
					// check if the concreteRole and the concreteWorkProduct
					// form part
					// of the same concreteActivity
					for (ConcreteActivity cact1 : crd
							.getSuperConcreteActivities()) {
						for (ConcreteActivity cact2 : _concreteWorkProductDescriptor
								.getSuperConcreteActivities()) {
							if (cact1.getId().equals(cact2.getId())) {
								return true;
							}
						}
					}
				}
			}

		}

		return afficher;
	}

	/**
	 * Allows to get the concreteRoleDescriptorService
	 * 
	 * @return the concreteRoleDescriptorService
	 */
	public ConcreteRoleDescriptorService getConcreteRoleDescriptorService() {
		return concreteRoleDescriptorService;
	}

	/**
	 * Allows to set the concreteRoleDescriptorService
	 * 
	 * @param _concreteRoleDescriptorService
	 */
	public void setConcreteRoleDescriptorService(
			ConcreteRoleDescriptorService _concreteRoleDescriptorService) {
		concreteRoleDescriptorService = _concreteRoleDescriptorService;
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
	 * @param _roleDescriptorService
	 */
	public void setRoleDescriptorService(
			RoleDescriptorService _roleDescriptorService) {
		roleDescriptorService = _roleDescriptorService;
	}

	/**
	 * Allows to get the participantService
	 * 
	 * @return the participantService
	 */
	public ParticipantService getParticipantService() {
		return participantService;
	}

	/**
	 * Allows to set the participantService
	 * 
	 * @param participantService
	 */
	public void setParticipantService(ParticipantService participantService) {
		this.participantService = participantService;
	}

	/**
	 * Allows to get the participant for a concreteWorkProductDescriptor
	 * 
	 * @param _concreteWorkProductDescriptor
	 * @return the participant
	 */
	public Participant getParticipant(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
		this.concreteWorkProductDescriptorDao.getSessionFactory()
				.getCurrentSession().saveOrUpdate(
						_concreteWorkProductDescriptor);
		String partis = _concreteWorkProductDescriptor.getParticipant();
		Participant parti = this.participantService.getParticipant(partis);
		this.participantService.getParticipantDao().saveOrUpdateParticipant(
				parti);
		return (parti);
	}

	/**
	 * Allows to get the workProductDescriptor for a
	 * concreteWorkProductDescriptor
	 * 
	 * @param _concreteWorkProductDescriptor
	 * @return
	 */
	public WorkProductDescriptor getWorkProductDescriptor(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
		this.concreteWorkProductDescriptorDao.getSessionFactory()
				.getCurrentSession().saveOrUpdate(
						_concreteWorkProductDescriptor);
		WorkProductDescriptor workProductDescriptor = _concreteWorkProductDescriptor
				.getWorkProductDescriptor();

		this.workProductDescriptorService.getWorkProductDescriptorDao()
				.saveOrUpdateWorkProductDescriptor(workProductDescriptor);

		return (workProductDescriptor);

	}

	/**
	 * Allows to get the set of super concreteActivities for a
	 * concreteWorkProductDescriptor
	 * 
	 * @param _concreteWorkProductDescriptor
	 * @return the set of super concreteActivities
	 */
	public Set<ConcreteActivity> getSuperConcreteActivity(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {

		this.concreteWorkProductDescriptorDao.getSessionFactory()
				.getCurrentSession().saveOrUpdate(
						_concreteWorkProductDescriptor);

		return _concreteWorkProductDescriptor.getSuperConcreteActivities();

	}

	/**
	 * Allows to get the responsible concreteRoleDescriptor
	 * 
	 * @param _concreteWorkProductDescriptor
	 * @return the responsible concreteRoleDescriptor
	 */
	public ConcreteRoleDescriptor getResponsibleConcreteRoleDescriptor(
			ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
		this.concreteWorkProductDescriptorDao.getSessionFactory()
				.getCurrentSession().saveOrUpdate(
						_concreteWorkProductDescriptor);
		ConcreteRoleDescriptor crd = _concreteWorkProductDescriptor
				.getResponsibleConcreteRoleDescriptor();
		this.concreteRoleDescriptorService.getConcreteRoleDescriptorDao()
				.saveOrUpdateConcreteRoleDescriptor(crd);
		return (crd);
	}

}
