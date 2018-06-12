package wilos.business.services.misc.wilosuser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService;
import wilos.business.services.misc.concreteworkproduct.ConcreteWorkProductDescriptorService;
import wilos.business.services.misc.project.ProjectService;
import wilos.business.webservices.transfertobject.ParticipantTO;
import wilos.hibernate.misc.wilosuser.ParticipantDao;
import wilos.model.misc.concreterole.ConcreteRoleDescriptor;
import wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor;
import wilos.model.misc.project.Project;
import wilos.model.misc.wilosuser.Participant;
import wilos.utils.Security;

/**
 * The services associated to the Participant
 * 
 */
@org.springframework.transaction.annotation.Transactional(readOnly=false, propagation=org.springframework.transaction.annotation.Propagation.REQUIRED) 
public class ParticipantService {
    private wilos.hibernate.misc.wilosuser.ParticipantDao participantDao;
    private wilos.business.services.misc.project.ProjectService projectService;
    private wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService concreteRoleDescriptorService;
    private wilos.business.services.misc.concreteworkproduct.ConcreteWorkProductDescriptorService concreteWorkProductDescriptorService;
    
    /**
     * Allows to get the concreteRoleDescriptorService
     * 
     * @return the concreteRoleDescriptorService
     */
    public wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService getConcreteRoleDescriptorService() {
        return this.concreteRoleDescriptorService;
    }
    
    /**
     * Allows to set the concreteRoleDescriptorService
     * 
     * @param _concreteRoleDescriptorService
     */
    public void setConcreteRoleDescriptorService(wilos.business.services.misc.concreterole.ConcreteRoleDescriptorService _concreteRoleDescriptorService) {
        this.concreteRoleDescriptorService = _concreteRoleDescriptorService;
    }
    
    /**
     * Allows to get the participantDao
     * 
     * @return the ParticipantDao
     */
    public wilos.hibernate.misc.wilosuser.ParticipantDao getParticipantDao() {
        return this.participantDao;
    }
    
    /**
     * Allows to set the participantDao
     * 
     * @param _participantDao
     */
    public void setParticipantDao(wilos.hibernate.misc.wilosuser.ParticipantDao _participantDao) {
        this.participantDao = _participantDao;
    }
    
    /**
     * Allows to get the projectService
     * 
     * @return the projectService
     */
    public wilos.business.services.misc.project.ProjectService getProjectService() {
        return this.projectService;
    }
    
    /**
     * Allows to get the projectService
     * 
     * @param _projectService
     * 
     */
    public void setProjectService(wilos.business.services.misc.project.ProjectService _projectService) {
        this.projectService = _projectService;
    }
    
    /**
     * Allows to get the set of concreteRoleDescriptor for a participant and a
     * project
     * 
     * @return the set of concreteRoleDescriptor
     */
    public java.util.Set<wilos.model.misc.concreterole.ConcreteRoleDescriptor> getConcreteRoleDescriptorsForAParticipantAndForAProject(java.lang.String _participantId,
                                                                                                                                       java.lang.String _projectId) {
        labeled_1 :
        {
            java.util.Set<wilos.model.misc.concreterole.ConcreteRoleDescriptor> concreteRolesList =
                    new java.util.HashSet<wilos.model.misc.concreterole.ConcreteRoleDescriptor>(
                    );
            java.util.Set<wilos.model.misc.concreterole.ConcreteRoleDescriptor> participantConcreteRolesList =
                    this.getParticipant(_participantId).getConcreteRoleDescriptors();
            java.util.List<wilos.model.misc.concreterole.ConcreteRoleDescriptor> projectConcreteRolesList =
                    this.concreteRoleDescriptorService.getAllConcreteRoleDescriptorsForProject(
                            _projectId);
            if (projectConcreteRolesList != null)
            {
                java.util.Iterator extfor$iter$1 =
                        projectConcreteRolesList.iterator();
                while (extfor$iter$1.hasNext())
                {
                    wilos.model.misc.concreterole.ConcreteRoleDescriptor projectConcreteRole =
                            (wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                                    extfor$iter$1.next();
                    java.util.Iterator extfor$iter =
                            participantConcreteRolesList.iterator();
                    while (extfor$iter.hasNext())
                    {
                        wilos.model.misc.concreterole.ConcreteRoleDescriptor concreteRoleDescriptor =
                                (wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                                        extfor$iter.next();
                        if (projectConcreteRole.getId(
                        ).equals(
                                concreteRoleDescriptor.getId(
                                )))
                            concreteRolesList.add(concreteRoleDescriptor);
                    }
                }
            }
        }
        return concreteRolesList;
    }
    
    /**
     * Allows to get the set of concreteRoleDescriptor for a participant and a
     * project
     * 
     * @return the set of concreteRoleDescriptor
     */
    public java.util.Set<java.lang.String> getNameConcreteRoleDescriptorsForAParticipantAndForAProject(java.lang.String _participantId,
                                                                                                       java.lang.String _projectId) {
        labeled_2 :
        {
            java.util.Set<java.lang.String> concreteRolesList =
                    new java.util.HashSet<java.lang.String>();
            java.util.Set<wilos.model.misc.concreterole.ConcreteRoleDescriptor> participantConcreteRolesList =
                    this.getParticipant(_participantId).getConcreteRoleDescriptors();
            java.util.List<wilos.model.misc.concreterole.ConcreteRoleDescriptor> projectConcreteRolesList =
                    this.concreteRoleDescriptorService.getAllConcreteRoleDescriptorsForProject(
                            _projectId);
            if (projectConcreteRolesList != null)
            {
                java.util.Iterator extfor$iter$1 =
                        projectConcreteRolesList.iterator();
                while (extfor$iter$1.hasNext())
                {
                    wilos.model.misc.concreterole.ConcreteRoleDescriptor projectConcreteRole =
                            (wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                                    extfor$iter$1.next();
                    java.util.Iterator extfor$iter =
                            participantConcreteRolesList.iterator();
                    while (extfor$iter.hasNext())
                    {
                        wilos.model.misc.concreterole.ConcreteRoleDescriptor concreteRoleDescriptor =
                                (wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                                        extfor$iter.next();
                        if (projectConcreteRole.getId(
                        ).equals(
                                concreteRoleDescriptor.getId(
                                )))
                            if (!concreteRoleDescriptor.getRoleDescriptor(
                            ).getPresentationName(
                            ).equals(""))
                                concreteRolesList.add(
                                        concreteRoleDescriptor.getRoleDescriptor(
                                        ).getName(
                                        ));
                    }
                }
            }
        }
        return concreteRolesList;
    }
    
    /**
     * Allows to get the list of participants
     * 
     * @return the list of participants
     */
    public java.util.List<wilos.model.misc.wilosuser.Participant> getParticipants() {
        return this.participantDao.getAllParticipants();
    }
    
    /**
     * Allows to get the participant with its id
     * 
     * @return the participant
     */
    public wilos.model.misc.wilosuser.Participant getParticipant(java.lang.String _id) {
        return this.participantDao.getParticipantById(_id);
    }
    
    /**
     * Allows to save the participant
     * 
     * @param _participant
     */
    public void saveParticipant(wilos.model.misc.wilosuser.Participant _participant) {
        _participant.setPassword(
                       wilos.utils.Security.encode(_participant.getPassword()));
        participantDao.saveOrUpdateParticipant(_participant);
    }
    
    /**
     * Allows to save the participant without encryption of the password
     * 
     * @param _participant
     */
    public void saveParticipantWithoutEncryption(wilos.model.misc.wilosuser.Participant _participant) {
        if (_participant.getNewPassword() != null &&
              !_participant.getNewPassword().trim().equalsIgnoreCase("")) {
            _participant.setPassword(
                           wilos.utils.Security.encode(
                                                  _participant.getNewPassword(
                                                                 )));
        }
        participantDao.saveOrUpdateParticipant(_participant);
    }
    
    /**
     * Allows to delete a participant
     * 
     * @param participantId
     */
    public void deleteParticipant(java.lang.String participantId) {
        wilos.model.misc.wilosuser.Participant participant =
          this.getParticipant(participantId);
        if (participant != null) {
            this.participantDao.deleteParticipant(participant);
        }
    }
    
    /**
     * 
     * Allows to get the list of projects for a participant
     * 
     * @param participant
     * @return list of projects where the participant is affected to
     */
    public java.util.HashMap<wilos.model.misc.project.Project,
    java.lang.Boolean> getProjectsForAParticipant(wilos.model.misc.wilosuser.Participant _participant) {
        this.participantDao.getSessionFactory().getCurrentSession(
                                                  ).saveOrUpdate(_participant);
        java.util.HashMap<wilos.model.misc.project.Project,
        java.lang.Boolean> affectedProjectList =
          new java.util.HashMap<wilos.model.misc.project.Project,
        java.lang.Boolean>();
        java.util.HashSet<wilos.model.misc.project.Project> allProjectList =
          new java.util.HashSet<wilos.model.misc.project.Project>();
        allProjectList = (java.util.HashSet<wilos.model.misc.project.Project>)
                           this.projectService.getUnfinishedProjects();
        java.util.Iterator extfor$iter = allProjectList.iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.project.Project p =
              (wilos.model.misc.project.Project) extfor$iter.next();
            java.util.Set<wilos.model.misc.project.Project> tmp =
              _participant.getAffectedProjectList();
            if (tmp.contains(p)) {
                affectedProjectList.put(p, true);
            } else {
                affectedProjectList.put(p, false);
            }
        }
        return affectedProjectList;
    }
    
    /**
     * 
     * Allows to get the list of project where a participant is affected to
     * 
     * @param participant
     * @return list of project where the participant is affected to
     */
    public java.util.List<wilos.model.misc.project.Project> getAllAffectedProjectsForParticipant(wilos.model.misc.wilosuser.Participant participant) {
        labeled_3 :
        {
            java.util.List<wilos.model.misc.project.Project> affectedProjectList =
                    new java.util.ArrayList<wilos.model.misc.project.Project>();
            java.util.HashSet<wilos.model.misc.project.Project> allProjectList =
                    new java.util.HashSet<wilos.model.misc.project.Project>();
            wilos.model.misc.wilosuser.Participant chargedParticipant =
                    new wilos.model.misc.wilosuser.Participant();
            java.lang.String login = participant.getLogin();
            chargedParticipant = this.participantDao.getParticipant(login);
            allProjectList = (java.util.HashSet<wilos.model.misc.project.Project>)
                    this.projectService.getUnfinishedProjects();
            java.util.Iterator extfor$iter = allProjectList.iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.project.Project p =
                        (wilos.model.misc.project.Project) extfor$iter.next();
                if (chargedParticipant.getAffectedProjectList().contains(p))
                {
                    affectedProjectList.add(p);
                }
            }
        }
        return affectedProjectList;
    }
    
    /**
     * Allows to save projects for a participant
     * 
     * @param participant
     * @param affectedProjects
     */
    public void saveProjectsForAParticipant(wilos.model.misc.wilosuser.Participant participant,
                                            java.util.Map<java.lang.String,
                                            java.lang.Boolean> affectedProjects) {
        wilos.model.misc.wilosuser.Participant currentParticipant =
          this.getParticipantDao().getParticipant(participant.getLogin());
        wilos.model.misc.project.Project currentProject;
        java.util.Iterator extfor$iter =
          affectedProjects.keySet().iterator();
        while (extfor$iter.hasNext()) {
            java.lang.String projectId = (java.lang.String) extfor$iter.next();
            currentProject = this.projectService.getProject(projectId);
            if (java.lang.Boolean.valueOf(affectedProjects.get(projectId)) ==
                  true) {
                currentParticipant.addAffectedProject(currentProject);
            }
            else {
                currentParticipant.removeAffectedProject(currentProject);
                if (currentProject.getProjectManager() != null) {
                    if (currentProject.getProjectManager(
                                         ).getId().equals(
                                                     currentParticipant.getId(
                                                                          ))) {
                        currentParticipant.removeManagedProject(currentProject);
                        this.projectService.saveProject(currentProject);
                    }
                }
            }
        }
        this.participantDao.saveOrUpdateParticipant(currentParticipant);
    }
    
    /**
     * Allows to save a project for a projectManager
     * 
     * @param _participantId
     * @param _projectId
     * @param _isForAssignment
     */
    public void saveProjectForAProjectManager(java.lang.String _participantId,
                                              java.lang.String _projectId,
                                              boolean _isForAssignment) {
        wilos.model.misc.wilosuser.Participant currentParticipant =
          this.getParticipantDao().getParticipantById(_participantId);
        wilos.model.misc.project.Project currentProject =
          this.projectService.getProject(_projectId);
        if (_isForAssignment) {
            currentParticipant.addManagedProject(currentProject);
        } else {
            currentParticipant.removeManagedProject(currentProject);
        }
        this.participantDao.saveOrUpdateParticipant(currentParticipant);
    }
    
    /**
     * 
     * Allows to get the list of projects without project manager for a
     * participant
     * 
     * @param participant
     * @return HashMap with couples of this form : (Project,ProjectManager) or
     *         (Project,null)
     */
    public java.util.HashMap<wilos.model.misc.project.Project,
    wilos.model.misc.wilosuser.Participant> getManageableProjectsForAParticipant(wilos.model.misc.wilosuser.Participant participant) {
        java.util.HashMap<wilos.model.misc.project.Project,
        java.lang.Boolean> affectedProjectList =
          new java.util.HashMap<wilos.model.misc.project.Project,
        java.lang.Boolean>();
        java.util.HashMap<wilos.model.misc.project.Project,
        wilos.model.misc.wilosuser.Participant> manageableProjectList =
          new java.util.HashMap<wilos.model.misc.project.Project,
        wilos.model.misc.wilosuser.Participant>();
        affectedProjectList = this.getProjectsForAParticipant(participant);
        java.util.Iterator extfor$iter =
          affectedProjectList.keySet().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.project.Project project =
              (wilos.model.misc.project.Project) extfor$iter.next();
            if (affectedProjectList.get(project).booleanValue() == true) {
                if (project.getProjectManager() == null) {
                    manageableProjectList.put(project, null);
                } else {
                    manageableProjectList.put(project,
                                              project.getProjectManager());
                }
            }
        }
        return manageableProjectList;
    }
    
    /**
     * Allows to save managed projects for a participant
     * 
     * @param participant
     * @param managedProjects
     */
    public void saveManagedProjectsForAParticipant(wilos.model.misc.wilosuser.Participant participant,
                                                   java.util.Map<java.lang.String,
                                                   java.lang.Boolean> managedProjects) {
        wilos.model.misc.wilosuser.Participant currentParticipant =
          this.getParticipantDao().getParticipant(participant.getLogin());
        wilos.model.misc.project.Project currentProject;
        java.util.Iterator extfor$iter = managedProjects.keySet().iterator();
        while (extfor$iter.hasNext()) {
            java.lang.String projectId = (java.lang.String) extfor$iter.next();
            currentProject = this.projectService.getProject(projectId);
            if ((java.lang.Boolean) managedProjects.get(projectId)) {
                currentParticipant.addManagedProject(currentProject);
            } else currentParticipant.removeManagedProject(currentProject);
        }
        this.participantDao.saveOrUpdateParticipant(currentParticipant);
    }
    
    /**
     * Allows to get the participantTO with the login
     * 
     * @param _login
     * @return the participantTO
     */
    public wilos.business.webservices.transfertobject.ParticipantTO getParticipantTO(java.lang.String _login) {
        return new wilos.business.webservices.transfertobject.ParticipantTO(
          participantDao.getParticipant(_login));
    }
    
    /**
     * Allows to get a participant's login
     * 
     * @param participant
     * @return the participant's login
     */
    public java.lang.String getParticipantLogin(wilos.model.misc.wilosuser.Participant participant) {
        this.participantDao.getSessionFactory().getCurrentSession(
                                                  ).saveOrUpdate(participant);
        return participant.getLogin();
    }
    
    /**
     * Allows to get the concreteWorkProductDescriptorService
     * 
     * @return the concreteWorkProductDescriptorService
     */
    public wilos.business.services.misc.concreteworkproduct.ConcreteWorkProductDescriptorService getConcreteWorkProductDescriptorService() {
        return this.concreteWorkProductDescriptorService;
    }
    
    /**
     * Allows to set the concreteWorkProductDescriptorService
     * 
     * @param _concreteWorkProductDescriptorService
     */
    public void setConcreteWorkProductDescriptorService(wilos.business.services.misc.concreteworkproduct.ConcreteWorkProductDescriptorService _concreteWorkProductDescriptorService) {
        this.concreteWorkProductDescriptorService =
          _concreteWorkProductDescriptorService;
    }
    
    /**
     * Allows to save the concreteWorkProductDescriptor for a participant
     * 
     * @param _participant
     * @param _concreteWorkProductDescriptor
     */
    public void saveConcreteWorkProductDescriptorForAParticipant(wilos.model.misc.wilosuser.Participant _participant,
                                                                 wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor _concreteWorkProductDescriptor) {
        wilos.model.misc.wilosuser.Participant currentParticipant =
          this.getParticipantDao().getParticipant(_participant.getLogin());
        currentParticipant.addConcreteWorkProductDescriptor(
                             _concreteWorkProductDescriptor);
        this.participantDao.saveOrUpdateParticipant(currentParticipant);
    }
    
    public ParticipantService() { super(); }
}

