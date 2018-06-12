package wilos.
  business.
  services.
  misc.
  concreterole;

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
 *
 The
 services
 associated
 to
 the
 Role
 * 
 */
@org.
  springframework.
  transaction.
  annotation.
  Transactional(readOnly=false, propagation=org.springframework.transaction.
                                              annotation.Propagation.REQUIRED) 
public class ConcreteRoleAffectationService {
    private wilos.hibernate.misc.wilosuser.ParticipantDao participantDao;
    private wilos.business.services.misc.concreterole.
      ConcreteRoleDescriptorService concreteRoleDescriptorService;
    protected final org.apache.commons.logging.Log logger = null;
    
    /**
     * Allows to get the list of all concreteRolesDescriptors for an activity
     * 
     * @param _activityId
     * @param _projectId
     * @return the list of all concreteRolesDescriptors for an activity
     */
    public java.
      util.
      List<wilos.
      model.
      misc.
      concreterole.
      ConcreteRoleDescriptor> getAllConcreteRolesDescriptorsForActivity(java.
                                                                          lang.
                                                                          String _activityId,
                                                                        java.
                                                                          lang.
                                                                          String _projectId) {
        labeled_2 :
        {
            java.util.List<wilos.model.misc.concreterole.ConcreteRoleDescriptor>
                    concreteRDList =
                    new java.util.ArrayList<wilos.model.misc.concreterole.
                            ConcreteRoleDescriptor>();
            java.util.List<wilos.model.misc.concreterole.ConcreteRoleDescriptor>
                    globalCRD =
                    this.concreteRoleDescriptorService.
                            getAllConcreteRoleDescriptorsForProject(_projectId);
            java.util.Iterator extfor$iter$1 = globalCRD.iterator();
            while (extfor$iter$1.hasNext())
            {
                wilos.model.misc.concreterole.ConcreteRoleDescriptor concreteRD =
                        (wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                                extfor$iter$1.next();
                concreteRD =
                        this.concreteRoleDescriptorService.getConcreteRoleDescriptor(
                                concreteRD.getId());
                java.util.List<wilos.model.misc.concreteactivity.ConcreteActivity>
                        globalCA =
                        new java.util.ArrayList<wilos.model.misc.concreteactivity.
                                ConcreteActivity>();
                globalCA.addAll(concreteRD.getSuperConcreteActivities());
                java.util.Iterator extfor$iter = globalCA.iterator();
                while (extfor$iter.hasNext())
                {
                    wilos.
                            model.
                            misc.
                            concretebreakdownelement.
                            ConcreteBreakdownElement
                            concreteBreakdownElement =
                            (wilos.model.misc.concretebreakdownelement.
                                    ConcreteBreakdownElement) extfor$iter.next();
                    if (concreteBreakdownElement.getId().equals(_activityId))
                    {
                        concreteRDList.add(concreteRD);
                    }
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
    public wilos.hibernate.misc.wilosuser.ParticipantDao getParticipantDao() {
        return participantDao;
    }
    
    /**
     * Setter of participantDao.
     * 
     * @param participantDao
     *            The participantDao to set.
     */
    public void setParticipantDao(wilos.hibernate.misc.wilosuser.
                                    ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }
    
    /**
     * Save roles affectation for a participant.
     * 
     * @return the page name where navigation has to be redirected to un peu ce
     *         retour qui craint
     */
    public java.lang.
      String saveParticipantConcreteRoles(java.util.HashMap<java.lang.String,
                                          java.lang.Object> rolesParticipant,
                                          java.lang.String _wilosUserId) {
        wilos.model.misc.wilosuser.Participant currentParticipant =
          this.participantDao.getParticipantById(_wilosUserId);
        wilos.model.misc.concreterole.ConcreteRoleDescriptor
          concreteRoleDescriptor =
          this.concreteRoleDescriptorService.
          getConcreteRoleDescriptor((java.lang.String)
                                      rolesParticipant.get("concreteId"));
        if (!((java.lang.Boolean) rolesParticipant.get("not_allowed"))) {
            if ((java.lang.Boolean) rolesParticipant.get("affected")) {
                currentParticipant.addConcreteRoleDescriptor(
                                     concreteRoleDescriptor);
            }
            else {
                currentParticipant.removeConcreteRoleDescriptor(
                                     concreteRoleDescriptor);
            }
            this.concreteRoleDescriptorService.getConcreteRoleDescriptorDao().
              saveOrUpdateConcreteRoleDescriptor(concreteRoleDescriptor);
        }
        return "";
    }
    
    /**
     * Getter of concreteRoleDescriptorService.
     * 
     * @return the concreteRoleDescriptorService.
     */
    public wilos.business.services.misc.concreterole.
      ConcreteRoleDescriptorService getConcreteRoleDescriptorService() {
        return this.concreteRoleDescriptorService;
    }
    
    /**
     * Setter of concreteRoleDescriptorService.
     * 
     * @param _concreteRoleDescriptorService
     *            The concreteRoleDescriptorService to set.
     */
    public void setConcreteRoleDescriptorService(wilos.business.services.misc.
                                                   concreterole.
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
    public java.
      util.
      HashMap<java.
      lang.
      String,
    java.
      lang.
      Boolean> getParticipantAffectationForConcreteRoleDescriptor(java.lang.
                                                                    String _wilosUserId,
                                                                  java.lang.
                                                                    String _concreteId) {
        java.util.HashMap<java.lang.String,
        java.lang.Boolean> roleStatus = new java.util.HashMap<java.lang.String,
        java.lang.Boolean>();
        wilos.model.misc.concreterole.ConcreteRoleDescriptor crd =
          this.concreteRoleDescriptorService.getConcreteRoleDescriptor(
                                               _concreteId);
        if (crd.getParticipant() != null) {
            roleStatus.put("affected", new java.lang.Boolean(true));
            if (crd.getParticipant().getId().equals(_wilosUserId)) {
                roleStatus.put("not_allowed", new java.lang.Boolean(false));
            } else {
                roleStatus.put("not_allowed", new java.lang.Boolean(true));
            }
        } else {
            roleStatus.put("affected", new java.lang.Boolean(false));
            roleStatus.put("not_allowed", new java.lang.Boolean(false));
        }
        return roleStatus;
    }
    
    public ConcreteRoleAffectationService() { super(); }
}
