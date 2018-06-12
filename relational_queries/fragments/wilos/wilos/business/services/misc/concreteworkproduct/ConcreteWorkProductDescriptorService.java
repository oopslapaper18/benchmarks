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

@org.springframework.transaction.annotation.Transactional(readOnly=false, propagation=org.springframework.transaction.annotation.Propagation.REQUIRED) 
public class ConcreteWorkProductDescriptorService {
    private wilos.hibernate.misc.concreteworkproduct.ConcreteWorkProductDescriptorDao concreteWorkProductDescriptorDao;
    private wilos.business.services.misc.concreteactivity.ConcreteActivityService concreteActivityService;
    private wilos.business.services.spem2.workproduct.WorkProductDescriptorService workProductDescriptorService;
    private wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService concreteRoleDescriptorService;
    private wilos.business.services.spem2.role.RoleDescriptorService roleDescriptorService;
    private wilos.business.services.misc.wilosuser.ParticipantService participantService;
    
    /**
     * Allows to get the concreteWorkProductDescriptor with its id
     * 
     * @param _concreteWorkProductDescriptorId
     * @return the concreteWorkProductDescriptor
     */
    public wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor getConcreteWorkProductDescriptor(java.lang.String _concreteWorkProductDescriptorId) {
        return this.concreteWorkProductDescriptorDao.getConcreteWorkProductDescriptor(
                                                       _concreteWorkProductDescriptorId);
    }
    
    /**
     * Lance la suppression d'un produit apres verification que cela est
     possible
     * 
     * @param _concreteWorkProductDescriptor
     * @return
     */
    public wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor deleteConcreteWorkProductDescriptor(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
        _concreteWorkProductDescriptor =
          this.getConcreteWorkProductDescriptor(
                 _concreteWorkProductDescriptor.getId());
        if (_concreteWorkProductDescriptor != null &&
              _concreteWorkProductDescriptor.getParticipant() == null) {
            java.util.Set<wilos.model.misc.concretetask.ConcreteTaskDescriptor> lctd =
              _concreteWorkProductDescriptor.getProducerConcreteTasks();
            boolean tache_commence = false;
            java.util.Iterator extfor$iter = lctd.iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.misc.concretetask.ConcreteTaskDescriptor ctd =
                  (wilos.model.misc.concretetask.ConcreteTaskDescriptor)
                    extfor$iter.next();
                if (ctd.getState().equals(
                                     wilos.utils.Constantes.State.STARTED)) {
                    tache_commence = true;
                }
            }
            if (!tache_commence) {
                this.removeConcreteWorkProductDescriptor(
                       _concreteWorkProductDescriptor);
            } else {
                return _concreteWorkProductDescriptor;
            }
        } else
            if (_concreteWorkProductDescriptor.getParticipant() != null) {
                getParticipant(_concreteWorkProductDescriptor).getName();
                return _concreteWorkProductDescriptor;
            }
        return null;
    }
    
    /**
     * Allows to remove a concreteWorkProductDescriptor
     * 
     * @param _concreteWorkProductDescriptor
     */
    public void removeConcreteWorkProductDescriptor(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
        this.concreteWorkProductDescriptorDao.getSessionFactory(
                                                ).getCurrentSession(
                                                    ).saveOrUpdate(
                                                        _concreteWorkProductDescriptor);
        boolean isOutOfProcess =
          _concreteWorkProductDescriptor.getWorkProductDescriptor(
                                           ).getIsOutOfProcess();
        _concreteWorkProductDescriptor.removeAllProducerConcreteTasks();
        _concreteWorkProductDescriptor.removeAllOptionalUserConcreteTasks();
        _concreteWorkProductDescriptor.removeAllMandatoryUserConcreteTasks();
        java.util.Iterator extfor$iter =
          _concreteWorkProductDescriptor.getSuperConcreteActivities().iterator(
                                                                        );
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concreteactivity.ConcreteActivity sca =
              (wilos.model.misc.concreteactivity.ConcreteActivity)
                extfor$iter.next();
            sca.getConcreteBreakdownElements(
                  ).remove(_concreteWorkProductDescriptor);
            this.concreteActivityService.saveConcreteActivity(sca);
        }
        wilos.model.spem2.workproduct.WorkProductDescriptor wpd =
          _concreteWorkProductDescriptor.getWorkProductDescriptor();
        this.workProductDescriptorService.getWorkProductDescriptorDao(
                                            ).getSessionFactory(
                                                ).getCurrentSession().evict(
                                                                        wpd);
        this.workProductDescriptorService.getWorkProductDescriptorDao(
                                            ).getSessionFactory(
                                                ).getCurrentSession(
                                                    ).saveOrUpdate(wpd);
        this.workProductDescriptorService.getWorkProductDescriptorDao(
                                            ).getSessionFactory(
                                                ).getCurrentSession().refresh(
                                                                        wpd);
        wpd.removeConcreteWorkProductDescriptor(_concreteWorkProductDescriptor);
        this.workProductDescriptorService.getConcreteWorkProductDescriptorDao(
                                            ).getSessionFactory(
                                                ).getCurrentSession(
                                                    ).delete(
                                                        _concreteWorkProductDescriptor);
        this.workProductDescriptorService.getWorkProductDescriptorDao(
                                            ).getSessionFactory(
                                                ).getCurrentSession(
                                                    ).saveOrUpdate(wpd);
        if (isOutOfProcess) {
            this.workProductDescriptorService.getWorkProductDescriptorDao(
                                                ).deleteWorkProductDescriptor(
                                                    wpd);
        }
    }
    
    /**
     * Allows to save a concreteWorkProductDescriptor
     * 
     * @param _concreteWorkProductDescriptor
     */
    public void saveConcreteWorkProductDescriptor(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
        this.concreteWorkProductDescriptorDao.saveOrUpdateConcreteWorkProductDescriptor(
                                                _concreteWorkProductDescriptor);
    }
    
    /**
     * Allows to get the concreteWorkProductDescriptorDao
     * 
     * @return the concreteWorkProductDescriptorDao
     */
    public wilos.hibernate.misc.concreteworkproduct.ConcreteWorkProductDescriptorDao getConcreteWorkProductDescriptorDao() {
        return concreteWorkProductDescriptorDao;
    }
    
    /**
     * Allows to set the concreteWorkProductDescriptorDao
     * 
     * @param _concreteWorkProductDescriptorDao
     */
    public void setConcreteWorkProductDescriptorDao(wilos.hibernate.misc.concreteworkproduct.ConcreteWorkProductDescriptorDao _concreteWorkProductDescriptorDao) {
        concreteWorkProductDescriptorDao = _concreteWorkProductDescriptorDao;
    }
    
    /**
     * Allows to get the concreteActivityService
     * 
     * @return the concreteActivityService
     */
    public wilos.business.services.misc.concreteactivity.ConcreteActivityService getConcreteActivityService() {
        return concreteActivityService;
    }
    
    /**
     * Allows to set the concreteActivityService
     * 
     * @param _concreteActivityService
     */
    public void setConcreteActivityService(wilos.business.services.misc.concreteactivity.ConcreteActivityService _concreteActivityService) {
        concreteActivityService = _concreteActivityService;
    }
    
    /**
     * Allows to get the workProductDescriptorService
     * 
     * @return the workProductDescriptorService
     */
    public wilos.business.services.spem2.workproduct.WorkProductDescriptorService getWorkProductDescriptorService() {
        return workProductDescriptorService;
    }
    
    /**
     * Allows to set the workProductDescriptorService
     * 
     * @param _workProductDescriptorService
     */
    public void setWorkProductDescriptorService(wilos.business.services.spem2.workproduct.WorkProductDescriptorService _workProductDescriptorService) {
        workProductDescriptorService = _workProductDescriptorService;
    }
    
    /**
     * Allows to get the list of super concreteActivities for a
     * concreteWorkProductDescriptor
     * 
     * @param _cwpdid
     * @return the list of super concreteActivities
     */
    public java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity> getSuperConcreteActivities(java.lang.String _cwpdid) {
        labeled_1 :
        {
            wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor crd =
                    this.getConcreteWorkProductDescriptor(_cwpdid);
            java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity> listTmp =
                    this.concreteActivityService.getAllConcreteActivities();
            java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity> listToReturn =
                    new java.util.ArrayList<wilos.model.misc.concreteactivity.ConcreteActivity>(
                    );
            java.util.Iterator extfor$iter = listTmp.iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.concreteactivity.ConcreteActivity ca =
                        (wilos.model.misc.concreteactivity.ConcreteActivity)
                                extfor$iter.next();
                if (ca.getConcreteBreakdownElements().contains(crd))
                {
                    listToReturn.add(ca);
                }
            }
        }
        return listToReturn;
    }
    
    /**
     * Return concreteWorkProductDescriptor for a project list
     * 
     * @return list of concreteWorkProductDescriptor
     */
    public java.util.List<wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor> getAllConcreteWorkProductDescriptorsForProject(java.lang.String _projectId) {
        return this.getConcreteWorkProductDescriptorDao(
                      ).getAllConcreteWorkProductDescriptorsForProject(
                          _projectId);
    }
    
    /**
     * Allows to dissociate a concreteWorkProductDescriptor
     * 
     * @param _concreteWorkProductDescriptor
     */
    public void dissociateConcreteWorkProductDescriptor(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor,
                                                        wilos.model.misc.wilosuser.Participant _participant) {
        wilos.model.misc.concreterole.ConcreteRoleDescriptor rmrd =
          _concreteWorkProductDescriptor.getResponsibleConcreteRoleDescriptor();
        this.concreteWorkProductDescriptorDao.getSessionFactory(
                                                ).getCurrentSession(
                                                    ).saveOrUpdate(
                                                        _concreteWorkProductDescriptor);
        if (rmrd != null) {
            rmrd.removeConcreteWorkProductDescriptor(
                   _concreteWorkProductDescriptor);
        }
        _concreteWorkProductDescriptor.setParticipant(null);
        _concreteWorkProductDescriptor.setState(
                                         wilos.utils.Constantes.State.CREATED);
        this.concreteWorkProductDescriptorDao.saveOrUpdateConcreteWorkProductDescriptor(
                                                _concreteWorkProductDescriptor);
        this.concreteRoleDescriptorService.saveConcreteRoleDescriptor(rmrd);
        wilos.model.misc.wilosuser.Participant currentParticipant =
          this.participantService.getParticipantDao().getParticipant(
                                                        _participant.getLogin(
                                                                       ));
        currentParticipant.removeConcreteWorkProductDescriptor(
                             _concreteWorkProductDescriptor);
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
    public boolean isAffectableToConcreteWorkProductDescriptor(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor,
                                                               wilos.model.misc.wilosuser.Participant _user) {
        if (_concreteWorkProductDescriptor.getParticipant() == null) {
            return true;
        }
        else {
            return _concreteWorkProductDescriptor.getParticipant(
                                                    ).equals(_user.getId());
        }
    }
    
    /**
     * When the user click on the button affected.
     * 
     * @param _concreteWorkProductDescriptor
     */
    public wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor affectedConcreteWorkProductDescriptor(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor,
                                                                                                                    wilos.model.misc.wilosuser.Participant _user) {
        _concreteWorkProductDescriptor =
          this.concreteWorkProductDescriptorDao.getConcreteWorkProductDescriptor(
                                                  _concreteWorkProductDescriptor.getId(
                                                                                   ));
        if (_concreteWorkProductDescriptor != null) {
            if (_concreteWorkProductDescriptor.getParticipant() == null) {
                _concreteWorkProductDescriptor.setParticipant(_user.getId());
                this.concreteWorkProductDescriptorDao.getSessionFactory(
                                                        ).getCurrentSession(
                                                            ).saveOrUpdate(
                                                                _concreteWorkProductDescriptor);
                wilos.model.spem2.role.RoleDescriptor responsibleRole =
                  _concreteWorkProductDescriptor.getWorkProductDescriptor(
                                                   ).getResponsibleRoleDescriptor(
                                                       );
                if (responsibleRole != null) {
                    this.roleDescriptorService.getRoleDescriptorDao(
                                                 ).getSessionFactory(
                                                     ).getCurrentSession(
                                                         ).saveOrUpdate(
                                                             responsibleRole);
                    java.util.Set<wilos.model.misc.concreterole.ConcreteRoleDescriptor> listecrd =
                      responsibleRole.getConcreteRoleDescriptors();
                    java.util.Iterator extfor$iter = listecrd.iterator();
                    boolean break_0 = false;
                    while (extfor$iter.hasNext() && !break_0) {
                        wilos.model.misc.concreterole.ConcreteRoleDescriptor tmpListeRd =
                          (wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                            extfor$iter.next();
                        if (!break_0)
                            if (tmpListeRd.getParticipant() != null) {
                                if (!break_0)
                                    if (tmpListeRd.getParticipant(
                                                     ).getId().equals(
                                                                 _user.getId(
                                                                         ))) {
                                        this.concreteRoleDescriptorService.getConcreteRoleDescriptorDao(
                                                                             ).saveOrUpdateConcreteRoleDescriptor(
                                                                                 tmpListeRd);
                                        _concreteWorkProductDescriptor.addResponsibleConcreteRoleDescriptor(
                                                                         tmpListeRd);
                                        break_0 = true;
                                    }
                            }
                    }
                }
                _concreteWorkProductDescriptor.setState(
                                                 wilos.utils.Constantes.State.READY);
                this.concreteWorkProductDescriptorDao.getSessionFactory(
                                                        ).getCurrentSession(
                                                            ).saveOrUpdate(
                                                                _concreteWorkProductDescriptor);
                this.participantService.saveConcreteWorkProductDescriptorForAParticipant(
                                          _user,
                                          _concreteWorkProductDescriptor);
                return _concreteWorkProductDescriptor;
            }
            else {
                _concreteWorkProductDescriptor.getWorkProductDescriptor(
                                                 ).getResponsibleRoleDescriptor(
                                                     );
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
    public boolean checkAffectation(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor,
                                    wilos.model.misc.wilosuser.Participant _participant) {
        boolean afficher = false;
        this.concreteWorkProductDescriptorDao.getSessionFactory(
                                                ).getCurrentSession(
                                                    ).saveOrUpdate(
                                                        _concreteWorkProductDescriptor);
        wilos.model.spem2.workproduct.WorkProductDescriptor tmp =
          _concreteWorkProductDescriptor.getWorkProductDescriptor();
        wilos.model.spem2.role.RoleDescriptor tmpRoleDescriptor;
        wilos.model.spem2.workproduct.WorkProductDescriptor tmp2 =
          this.workProductDescriptorService.getWorkProductDescriptorById(
                                              tmp.getId());
        if (tmp2.getResponsibleRoleDescriptor() == null) { return false; }
        tmpRoleDescriptor = tmp2.getResponsibleRoleDescriptor();
        wilos.model.spem2.role.RoleDescriptor rd =
          this.roleDescriptorService.getRoleDescriptor(
                                       tmpRoleDescriptor.getId());
        java.util.List<wilos.model.misc.concreterole.ConcreteRoleDescriptor> listeRd =
          this.concreteRoleDescriptorService.getAllConcreteRoleDescriptorForARoleDescriptor(
                                               rd.getId());
        java.util.Iterator extfor$iter$2 = listeRd.iterator();
        while (extfor$iter$2.hasNext()) {
            wilos.model.misc.concreterole.ConcreteRoleDescriptor tmpListeRd =
              (wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                extfor$iter$2.next();
            wilos.model.misc.concreterole.ConcreteRoleDescriptor crd =
              this.concreteRoleDescriptorService.getConcreteRoleDescriptor(
                                                   tmpListeRd.getId());
            if (crd.getParticipant() == null) {
                afficher = false;
            }
            else {
                if (crd.getParticipant().getId().equals(
                                                   _participant.getId())) {
                    java.util.Iterator extfor$iter$1 =
                      crd.getSuperConcreteActivities().iterator();
                    while (extfor$iter$1.hasNext()) {
                        wilos.model.misc.concreteactivity.ConcreteActivity cact1 =
                          (wilos.model.misc.concreteactivity.ConcreteActivity)
                            extfor$iter$1.next();
                        java.util.Iterator extfor$iter =
                          _concreteWorkProductDescriptor.getSuperConcreteActivities(
                                                           ).iterator();
                        while (extfor$iter.hasNext()) {
                            wilos.model.misc.concreteactivity.ConcreteActivity cact2 =
                              (wilos.model.misc.concreteactivity.ConcreteActivity)
                                extfor$iter.next();
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
    public wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService getConcreteRoleDescriptorService() {
        return concreteRoleDescriptorService;
    }
    
    /**
     * Allows to set the concreteRoleDescriptorService
     * 
     * @param _concreteRoleDescriptorService
     */
    public void setConcreteRoleDescriptorService(wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService _concreteRoleDescriptorService) {
        concreteRoleDescriptorService = _concreteRoleDescriptorService;
    }
    
    /**
     * Allows to get the roleDescriptorService
     * 
     * @return the roleDescriptorService
     */
    public wilos.business.services.spem2.role.RoleDescriptorService getRoleDescriptorService() {
        return roleDescriptorService;
    }
    
    /**
     * Allows to set the roleDescriptorService
     * 
     * @param _roleDescriptorService
     */
    public void setRoleDescriptorService(wilos.business.services.spem2.role.RoleDescriptorService _roleDescriptorService) {
        roleDescriptorService = _roleDescriptorService;
    }
    
    /**
     * Allows to get the participantService
     * 
     * @return the participantService
     */
    public wilos.business.services.misc.wilosuser.ParticipantService getParticipantService() {
        return participantService;
    }
    
    /**
     * Allows to set the participantService
     * 
     * @param participantService
     */
    public void setParticipantService(wilos.business.services.misc.wilosuser.ParticipantService participantService) {
        this.participantService = participantService;
    }
    
    /**
     * Allows to get the participant for a concreteWorkProductDescriptor
     * 
     * @param _concreteWorkProductDescriptor
     * @return the participant
     */
    public wilos.model.misc.wilosuser.Participant getParticipant(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
        this.concreteWorkProductDescriptorDao.getSessionFactory(
                                                ).getCurrentSession(
                                                    ).saveOrUpdate(
                                                        _concreteWorkProductDescriptor);
        java.lang.String partis =
          _concreteWorkProductDescriptor.getParticipant();
        wilos.model.misc.wilosuser.Participant parti =
          this.participantService.getParticipant(partis);
        this.participantService.getParticipantDao().saveOrUpdateParticipant(
                                                      parti);
        return parti;
    }
    
    /**
     * Allows to get the workProductDescriptor for a
     * concreteWorkProductDescriptor
     * 
     * @param _concreteWorkProductDescriptor
     * @return
     */
    public wilos.model.spem2.workproduct.WorkProductDescriptor getWorkProductDescriptor(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
        this.concreteWorkProductDescriptorDao.getSessionFactory(
                                                ).getCurrentSession(
                                                    ).saveOrUpdate(
                                                        _concreteWorkProductDescriptor);
        wilos.model.spem2.workproduct.WorkProductDescriptor workProductDescriptor =
          _concreteWorkProductDescriptor.getWorkProductDescriptor();
        this.workProductDescriptorService.getWorkProductDescriptorDao(
                                            ).saveOrUpdateWorkProductDescriptor(
                                                workProductDescriptor);
        return workProductDescriptor;
    }
    
    /**
     * Allows to get the set of super concreteActivities for a
     * concreteWorkProductDescriptor
     * 
     * @param _concreteWorkProductDescriptor
     * @return the set of super concreteActivities
     */
    public java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity> getSuperConcreteActivity(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
        this.concreteWorkProductDescriptorDao.getSessionFactory(
                                                ).getCurrentSession(
                                                    ).saveOrUpdate(
                                                        _concreteWorkProductDescriptor);
        return _concreteWorkProductDescriptor.getSuperConcreteActivities();
    }
    
    /**
     * Allows to get the responsible concreteRoleDescriptor
     * 
     * @param _concreteWorkProductDescriptor
     * @return the responsible concreteRoleDescriptor
     */
    public wilos.model.misc.concreterole.ConcreteRoleDescriptor getResponsibleConcreteRoleDescriptor(wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
        this.concreteWorkProductDescriptorDao.getSessionFactory(
                                                ).getCurrentSession(
                                                    ).saveOrUpdate(
                                                        _concreteWorkProductDescriptor);
        wilos.model.misc.concreterole.ConcreteRoleDescriptor crd =
          _concreteWorkProductDescriptor.getResponsibleConcreteRoleDescriptor();
        this.concreteRoleDescriptorService.getConcreteRoleDescriptorDao(
                                             ).saveOrUpdateConcreteRoleDescriptor(
                                                 crd);
        return crd;
    }
    
    public ConcreteWorkProductDescriptorService() { super(); }
}

