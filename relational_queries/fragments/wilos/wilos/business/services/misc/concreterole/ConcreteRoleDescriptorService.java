package wilos.
  business.
  services.
  misc.
  concreterole;

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

@org.
  springframework.
  transaction.
  annotation.
  Transactional(propagation=org.springframework.transaction.annotation.
                              Propagation.REQUIRED) 
public class ConcreteRoleDescriptorService {
    private wilos.hibernate.misc.concreterole.ConcreteRoleDescriptorDao
      concreteRoleDescriptorDao;
    private wilos.business.services.misc.concreteactivity.
      ConcreteActivityService concreteActivityService;
    private wilos.business.services.spem2.role.RoleDescriptorService
      roleDescriptorService;
    private wilos.hibernate.misc.wilosuser.ParticipantDao participantDao;
    private wilos.hibernate.misc.concretetask.ConcreteTaskDescriptorDao
      concreteTaskDescriptorDao;
    
    /**
     * Allows to get the participant for a concreteRoleDescriptor
     * 
     * @param _concreteRoleDescriptor
     * @return the participant
     */
    public wilos.
      model.
      misc.
      wilosuser.
      Participant getParticipant(wilos.model.misc.concreterole.
                                   ConcreteRoleDescriptor _concreteRoleDescriptor) {
        this.getConcreteRoleDescriptorDao().getSessionFactory().
          getCurrentSession().saveOrUpdate(_concreteRoleDescriptor);
        return _concreteRoleDescriptor.getParticipant();
    }
    
    /**
     * Allows to save the concreteRoleDescriptor
     * 
     * @param _concreteRoleDescriptor
     */
    public void saveConcreteRoleDescriptor(wilos.model.misc.concreterole.
                                             ConcreteRoleDescriptor _concreteRoleDescriptor) {
        this.concreteRoleDescriptorDao.saveOrUpdateConcreteRoleDescriptor(
                                         _concreteRoleDescriptor);
    }
    
    /**
     * Affecte un participant a un role
     * 
     * @param _concreteRoleDescriptor
     * @param _participant
     * @return
     */
    public wilos.
      model.
      misc.
      concreterole.
      ConcreteRoleDescriptor addPartiConcreteRoleDescriptor(wilos.model.misc.
                                                              concreterole.
                                                              ConcreteRoleDescriptor _concreteRoleDescriptor,
                                                            wilos.model.misc.
                                                              wilosuser.
                                                              Participant _participant) {
        _concreteRoleDescriptor =
          this.getConcreteRoleDescriptor(_concreteRoleDescriptor.getId());
        if (_concreteRoleDescriptor != null) {
            wilos.model.misc.wilosuser.Participant user =
              _concreteRoleDescriptor.getParticipant();
            if (user == null) {
                _concreteRoleDescriptor.setParticipant(_participant);
                this.saveConcreteRoleDescriptor(_concreteRoleDescriptor);
                return _concreteRoleDescriptor;
            } else {
                _concreteRoleDescriptor.getParticipant().getName();
                return _concreteRoleDescriptor;
            }
        } else {
            return null;
        }
    }
    
    /**
     * Allows to get the list of all concreteRoleDecriptors
     * 
     * @return the list of all concreteRoleDecriptors
     */
    public java.util.List<wilos.model.misc.concreterole.
      ConcreteRoleDescriptor> getAllConcreteRoleDescriptors() {
        return this.getConcreteRoleDescriptorDao().
          getAllConcreteRoleDescriptors();
    }
    
    /**
     * Allows to get the list of primary concreteTaskDescriptors for a
     * concreteRole id
     * 
     * @param _concreteRoleId
     * @return the list of primary concreteTaskDescriptors
     */
    public java.
      util.
      List<wilos.
      model.
      misc.
      concretetask.
      ConcreteTaskDescriptor> getPrimaryConcreteTaskDescriptors(java.lang.
                                                                  String _concreteRoleId) {
        return this.concreteRoleDescriptorDao.
          getMainConcreteTaskDescriptorsForConcreteRoleDescriptor(
            _concreteRoleId);
    }
    
    /**
     * Allows to get the set of primary concreteTaskDescriptors for a
     * concreteRoleDescriptor
     * 
     * @param _concreteRoleDescriptor
     * @return the set of primary concreteTaskDescriptors
     */
    public java.
      util.
      Set<wilos.
      model.
      misc.
      concretetask.
      ConcreteTaskDescriptor> getPrimaryConcreteTaskDescriptors(wilos.model.
                                                                  misc.
                                                                  concreterole.
                                                                  ConcreteRoleDescriptor _concreteRoleDescriptor) {
        java.util.Set<wilos.model.misc.concretetask.ConcreteTaskDescriptor>
          concreteTaskDescriptors =
          new java.util.HashSet<wilos.model.misc.concretetask.
          ConcreteTaskDescriptor>();
        this.concreteRoleDescriptorDao.getSessionFactory().getCurrentSession().
          saveOrUpdate(_concreteRoleDescriptor);
        this.concreteRoleDescriptorDao.getSessionFactory().getCurrentSession().
          refresh(_concreteRoleDescriptor);
        java.util.Iterator extfor$iter =
          _concreteRoleDescriptor.getPrimaryConcreteTaskDescriptors().iterator(
                                                                        );
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concretetask.ConcreteTaskDescriptor
              concreteTaskDescriptor =
              (wilos.model.misc.concretetask.ConcreteTaskDescriptor)
                extfor$iter.next();
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
    public java.util.List<wilos.model.misc.concretetask.
      ConcreteTaskDescriptor> getAllConcreteTaskDescriptorsForConcreteRoleDescriptor(wilos.model.misc.concreterole.ConcreteRoleDescriptor _concreteRoleDescriptor) {
        return this.concreteRoleDescriptorDao.
          getMainConcreteTaskDescriptorsForConcreteRoleDescriptor(
            _concreteRoleDescriptor.getId());
    }
    
    /**
     * Allows to get the list of super concreteActivities for a
     * concreteRoleDescriptor id
     * 
     * @param _crdid
     * @return
     */
    public java.util.List<wilos.model.misc.concreteactivity.
      ConcreteActivity> getSuperConcreteActivities(java.lang.String _crdid) {
        labeled_1 :
        {
            wilos.model.misc.concreterole.ConcreteRoleDescriptor crd =
                    this.getConcreteRoleDescriptor(_crdid);
            java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity>
                    listTmp = this.concreteActivityService.getAllConcreteActivities();
            java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity>
                    listToReturn =
                    new java.util.ArrayList<wilos.model.misc.concreteactivity.
                            ConcreteActivity>();
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
     * 
     * @param _conConcreteRoleDescriptor
     * @return
     */
    public wilos.
      model.
      misc.
      concreterole.
      ConcreteRoleDescriptor deleteConcreteRoleDescriptor(wilos.model.misc.
                                                            concreterole.
                                                            ConcreteRoleDescriptor _concreteRoleDescriptor) {
        _concreteRoleDescriptor =
          this.getConcreteRoleDescriptor(_concreteRoleDescriptor.getId());
        if (_concreteRoleDescriptor != null &&
              _concreteRoleDescriptor.getParticipant() == null) {
            java.util.Set<wilos.model.misc.concreteactivity.ConcreteActivity>
              lca = _concreteRoleDescriptor.getSuperConcreteActivities();
            java.util.Iterator extfor$iter$1 = lca.iterator();
            while (extfor$iter$1.hasNext()) {
                wilos.model.misc.concreteactivity.ConcreteActivity ca =
                  (wilos.model.misc.concreteactivity.ConcreteActivity)
                    extfor$iter$1.next();
                java.util.Set<wilos.model.misc.concretebreakdownelement.
                  ConcreteBreakdownElement> lcbe =
                  ca.getConcreteBreakdownElements();
                java.util.Iterator extfor$iter = lcbe.iterator();
                while (extfor$iter.hasNext()) {
                    wilos.
                      model.
                      misc.
                      concretebreakdownelement.
                      ConcreteBreakdownElement
                      cbe =
                      (wilos.model.misc.concretebreakdownelement.
                        ConcreteBreakdownElement) extfor$iter.next();
                    if (cbe instanceof wilos.model.misc.concreteworkproduct.
                          ConcreteWorkProductDescriptor) {
                        wilos.
                          model.
                          misc.
                          concreteworkproduct.
                          ConcreteWorkProductDescriptor
                          cwpd =
                          (wilos.model.misc.concreteworkproduct.
                            ConcreteWorkProductDescriptor) cbe;
                        if (cwpd.getWorkProductDescriptor().
                              getResponsibleRoleDescriptor() != null) {
                            if (_concreteRoleDescriptor.getRoleDescriptor().
                                  getId().
                                  equals(
                                    cwpd.getWorkProductDescriptor(
                                           ).getResponsibleRoleDescriptor(
                                               ).getId())) {
                                return _concreteRoleDescriptor;
                            }
                        }
                    }
                    if (cbe instanceof wilos.model.misc.concretetask.
                          ConcreteTaskDescriptor) {
                        wilos.model.misc.concretetask.ConcreteTaskDescriptor
                          ctd =
                          (wilos.model.misc.concretetask.ConcreteTaskDescriptor)
                            cbe;
                        if (ctd.getTaskDescriptor().getMainRole() != null) {
                            if (_concreteRoleDescriptor.getRoleDescriptor().
                                  getId().equals(
                                            ctd.getTaskDescriptor().getMainRole(
                                                                      ).getId(
                                                                          ))) {
                                return _concreteRoleDescriptor;
                            }
                        }
                    }
                }
            }
            this.removeConcreteRoleDescriptor(_concreteRoleDescriptor);
        } else
            if (_concreteRoleDescriptor.getParticipant() != null) {
                _concreteRoleDescriptor.getParticipant().getName();
                return _concreteRoleDescriptor;
            }
        return null;
    }
    
    /**
     * Allows to delete a concreteRoleDescriptor
     */
    public void removeConcreteRoleDescriptor(wilos.model.misc.concreterole.
                                               ConcreteRoleDescriptor _concreteRoledescriptor) {
        this.concreteRoleDescriptorDao.getSessionFactory().getCurrentSession().
          saveOrUpdate(_concreteRoledescriptor);
        java.util.Iterator extfor$iter =
          _concreteRoledescriptor.getSuperConcreteActivities().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concreteactivity.ConcreteActivity sca =
              (wilos.model.misc.concreteactivity.ConcreteActivity)
                extfor$iter.next();
            sca.getConcreteBreakdownElements().remove(_concreteRoledescriptor);
            this.concreteActivityService.saveConcreteActivity(sca);
        }
        wilos.model.spem2.role.RoleDescriptor rd =
          _concreteRoledescriptor.getRoleDescriptor();
        this.roleDescriptorService.getRoleDescriptorDao().getSessionFactory().
          getCurrentSession().evict(rd);
        this.roleDescriptorService.getRoleDescriptorDao().getSessionFactory().
          getCurrentSession().saveOrUpdate(rd);
        this.roleDescriptorService.getRoleDescriptorDao().getSessionFactory().
          getCurrentSession().refresh(rd);
        rd.removeConcreteRoleDescriptor(_concreteRoledescriptor);
        this.roleDescriptorService.getConcreteRoleDescriptorDao().
          getSessionFactory().getCurrentSession().delete(
                                                    _concreteRoledescriptor);
        this.roleDescriptorService.getRoleDescriptorDao().getSessionFactory().
          getCurrentSession().saveOrUpdate(rd);
    }
    
    /**
     * Return concreteRoleDescriptor for a project list
     * 
     * @return list of concreteRoleDescriptors
     */
    public java.
      util.
      List<wilos.
      model.
      misc.
      concreterole.
      ConcreteRoleDescriptor> getAllConcreteRoleDescriptorsForProject(java.lang.
                                                                        String _projectId) {
        return this.getConcreteRoleDescriptorDao().
          getAllConcreteRoleDescriptorsForProject(_projectId);
    }
    
    /**
     * Allows to get a concreteRoleDescriptor with its id
     * 
     * @param _id
     * @return concreteRoleDescriptor
     */
    public wilos.model.misc.concreterole.
      ConcreteRoleDescriptor getConcreteRoleDescriptor(java.lang.String _id) {
        return this.concreteRoleDescriptorDao.getConcreteRoleDescriptor(_id);
    }
    
    /**
     * Allows to get the concreteRoleDescriptorDao
     * 
     * @return concreteRoleDescriptorDao
     */
    public wilos.hibernate.misc.concreterole.
      ConcreteRoleDescriptorDao getConcreteRoleDescriptorDao() {
        return concreteRoleDescriptorDao;
    }
    
    /**
     * Allows to set the concreteRoleDescriptorDao
     * 
     * @param _concreteRoleDescriptorDao
     */
    public void setConcreteRoleDescriptorDao(wilos.hibernate.misc.concreterole.
                                               ConcreteRoleDescriptorDao _concreteRoleDescriptorDao) {
        this.concreteRoleDescriptorDao = _concreteRoleDescriptorDao;
    }
    
    /**
     * Allows to get the concreteActivityService
     * 
     * @return the concretActivityService
     */
    public wilos.business.services.misc.concreteactivity.
      ConcreteActivityService getConcreteActivityService() {
        return concreteActivityService;
    }
    
    /**
     * Allows to set the concreteActivityService
     * 
     * @param concretActivityService
     */
    public void setConcreteActivityService(wilos.business.services.misc.
                                             concreteactivity.
                                             ConcreteActivityService _concreteActivityService) {
        this.concreteActivityService = _concreteActivityService;
    }
    
    /**
     * Allows to get the list of concreteRoleDescriptor for a roleDescriptor
     * 
     * @param _roleDescriptorId
     * @return the list of concreteRoleDescriptor
     */
    public java.util.List<wilos.model.misc.concreterole.
      ConcreteRoleDescriptor> getAllConcreteRoleDescriptorForARoleDescriptor(java.lang.String _roleDescriptorId) {
        return this.concreteRoleDescriptorDao.
          getAllConcreteRoleDescriptorsForARoleDescriptor(_roleDescriptorId);
    }
    
    /**
     * Allows to get the roleDescriptorService
     * 
     * @return the roleDescriptorService
     */
    public wilos.business.services.spem2.role.
      RoleDescriptorService getRoleDescriptorService() {
        return roleDescriptorService;
    }
    
    /**
     * Allows to set the roleDescriptorService
     * 
     * @param roleDescriptorService
     */
    public void setRoleDescriptorService(wilos.business.services.spem2.role.
                                           RoleDescriptorService roleDescriptorService) {
        this.roleDescriptorService = roleDescriptorService;
    }
    
    /**
     * Allows to create an out of process concrete role
     * 
     * @param _user
     *            ,_ctd
     */
    public wilos.
      model.
      spem2.
      role.
      RoleDescriptor createOutOfProcessConcreteRoleDescriptor(wilos.model.misc.
                                                                wilosuser.
                                                                Participant _user,
                                                              wilos.model.misc.
                                                                concretetask.
                                                                ConcreteTaskDescriptor _ctd) {
        wilos.model.spem2.role.RoleDescriptor rd =
          new wilos.model.spem2.role.RoleDescriptor();
        rd.setPresentationName("No Role");
        rd.setIsOptional(true);
        rd.setIsPlanned(false);
        rd.setHasMultipleOccurrences(true);
        rd.setGuid("No Role");
        rd.setDescription("outprocess role");
        this.getRoleDescriptorService().saveRoleDescriptor(rd);
        wilos.model.misc.concreterole.ConcreteRoleDescriptor crd =
          new wilos.model.misc.concreterole.ConcreteRoleDescriptor();
        crd.addRoleDescriptor(rd);
        crd.addBreakdownElement(rd);
        crd.addPrimaryConcreteTaskDescriptor(_ctd);
        crd.setConcreteName(rd.getPresentationName() + "#1");
        crd.setInstanciationOrder(1);
        java.
          lang.
          String
          projectId =
          (java.lang.String)
            wilos.presentation.web.utils.WebSessionService.
            getAttribute(
              wilos.presentation.web.utils.WebSessionService.PROJECT_ID);
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
    public wilos.hibernate.misc.wilosuser.ParticipantDao getParticipantDao() {
        return participantDao;
    }
    
    /**
     * Allows to set the participantDao
     * 
     * @param _participantDao
     */
    public void setParticipantDao(wilos.hibernate.misc.wilosuser.
                                    ParticipantDao _participantDao) {
        this.participantDao = _participantDao;
    }
    
    /**
     * Allows to get the concreteTaskDescriptorDao
     * 
     * @return the concreteTaskDescriptorDao
     */
    public wilos.hibernate.misc.concretetask.
      ConcreteTaskDescriptorDao getConcreteTaskDescriptorDao() {
        return concreteTaskDescriptorDao;
    }
    
    /**
     * Allows to set the concreteTaskDescriptorDao
     * 
     * @param _concreteTaskDescriptorDao
     */
    public void setConcreteTaskDescriptorDao(wilos.hibernate.misc.concretetask.
                                               ConcreteTaskDescriptorDao _concreteTaskDescriptorDao) {
        this.concreteTaskDescriptorDao = _concreteTaskDescriptorDao;
    }
    
    public java.
      lang.
      String getNameConcreteRoleDescriptor(wilos.model.misc.concreterole.
                                             ConcreteRoleDescriptor _crd) {
        this.concreteRoleDescriptorDao.saveOrUpdateConcreteRoleDescriptor(_crd);
        return _crd.getRoleDescriptor().getPresentationName();
    }
    
    public ConcreteRoleDescriptorService() { super(); }
}

