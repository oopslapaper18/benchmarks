package wilos.business.services.misc.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wilos.business.services.misc.concreteactivity.ConcreteActivityService;
import wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementService;
import wilos.business.services.misc.stateservice.StateService;
import wilos.business.services.spem2.process.ProcessService;
import wilos.hibernate.misc.concreteactivity.ConcreteActivityDao;
import wilos.hibernate.misc.concreterole.ConcreteRoleDescriptorDao;
import wilos.hibernate.misc.concretetask.ConcreteTaskDescriptorDao;
import wilos.hibernate.misc.project.ProjectDao;
import wilos.hibernate.misc.wilosuser.ParticipantDao;
import wilos.hibernate.spem2.activity.ActivityDao;
import wilos.hibernate.spem2.role.RoleDescriptorDao;
import wilos.hibernate.spem2.task.TaskDescriptorDao;
import wilos.model.misc.concreteactivity.ConcreteActivity;
import wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement;
import wilos.model.misc.concretemilestone.ConcreteMilestone;
import wilos.model.misc.concreterole.ConcreteRoleDescriptor;
import wilos.model.misc.concretetask.ConcreteTaskDescriptor;
import wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor;
import wilos.model.misc.project.Project;
import wilos.model.misc.wilosuser.Participant;
import wilos.model.spem2.activity.Activity;
import wilos.model.spem2.breakdownelement.BreakdownElement;
import wilos.model.spem2.process.Process;
import wilos.model.spem2.role.RoleDescriptor;
import wilos.model.spem2.task.TaskDescriptor;
import wilos.model.spem2.workproduct.WorkProductDescriptor;
import wilos.presentation.web.utils.WebSessionService;
import wilos.utils.Constantes.State;

/**
 * The services associated to the Project
 * 
 */
@org.springframework.transaction.annotation.Transactional(readOnly=false, propagation=org.springframework.transaction.annotation.Propagation.REQUIRED) 
public class ProjectService {
    private wilos.hibernate.misc.project.ProjectDao projectDao;
    private wilos.hibernate.misc.wilosuser.ParticipantDao participantDao;
    private wilos.hibernate.spem2.activity.ActivityDao activityDao;
    private wilos.business.services.misc.stateservice.StateService stateService;
    private wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementService concreteWorkBreakdownElementService;
    private wilos.hibernate.misc.concreteactivity.ConcreteActivityDao concreteActivityDao;
    private wilos.business.services.misc.concreteactivity.ConcreteActivityService concreteActivityService;
    private wilos.hibernate.misc.concretetask.ConcreteTaskDescriptorDao concreteTaskDescriptorDao;
    private wilos.hibernate.misc.concreterole.ConcreteRoleDescriptorDao concreteRoleDescriptorDao;
    private wilos.hibernate.spem2.task.TaskDescriptorDao taskDescriptorDao;
    private wilos.hibernate.spem2.role.RoleDescriptorDao roleDescriptorDao;
    private wilos.business.services.spem2.process.ProcessService processService;
    public final org.apache.commons.logging.Log logger = null;
    private wilos.model.misc.project.Project[] projects;
    
    /**
     * Allows to create a taskDescriptor
     * 
     * @param _presentationName
     * @param _description
     * @param _mainRole
     * @param _guid
     * @return the taskDescriptor if it's created, null in the other case
     */
    public wilos.model.spem2.task.TaskDescriptor createTaskDescriptor(java.lang.String _presentationName,
                                                                      java.lang.String _description,
                                                                      wilos.model.spem2.role.RoleDescriptor _mainRole,
                                                                      java.lang.String _guid) {
        wilos.model.spem2.task.TaskDescriptor taskDesc =
          new wilos.model.spem2.task.TaskDescriptor();
        taskDesc.setPresentationName(_presentationName);
        taskDesc.setDescription(_description);
        taskDesc.setGuid(_guid);
        taskDesc.setPrefix("");
        taskDesc.setIsPlanned(true);
        taskDesc.setHasMultipleOccurrences(false);
        taskDesc.setIsOptional(false);
        taskDesc.setIsRepeatable(true);
        taskDesc.setIsOngoing(false);
        taskDesc.setIsEvenDriven(false);
        if (_mainRole != null) { _mainRole.addPrimaryTask(taskDesc); }
        this.taskDescriptorDao.saveOrUpdateTaskDescriptor(taskDesc);
        java.lang.System.out.println("### TaskDescriptor sauve");
        if (taskDesc.getId() != null) return taskDesc; else return null;
    }
    
    /**
     * Allows to create a concreteTaskDescriptor
     * 
     * @param _concreteName
     * @param _project
     * @param _td
     * @param _cact
     * @return true if the concreteTaskDescriptor is created, false in the other
     *         case
     */
    public boolean createConcreteTaskDescriptor(java.lang.String _concreteName,
                                                wilos.model.misc.project.Project _project,
                                                wilos.model.spem2.task.TaskDescriptor _td,
                                                wilos.model.misc.concreteactivity.ConcreteActivity _cact) {
        wilos.model.misc.concretetask.ConcreteTaskDescriptor concTaskDesc =
          new wilos.model.misc.concretetask.ConcreteTaskDescriptor();
        int i = java.lang.Math.abs((int) java.lang.System.currentTimeMillis());
        concTaskDesc.setConcreteName(_concreteName);
        concTaskDesc.setProject(_project);
        concTaskDesc.setInstanciationOrder(i);
        concTaskDesc.addSuperConcreteActivity(_cact);
        concTaskDesc.setTaskDescriptor(_td);
        concTaskDesc.setBreakdownElement(_td);
        concTaskDesc.setWorkBreakdownElement(_td);
        this.concreteTaskDescriptorDao.saveOrUpdateConcreteTaskDescriptor(
                                         concTaskDesc);
        this.concreteActivityDao.saveOrUpdateConcreteActivity(_cact);
        return concTaskDesc.getId() != null;
    }
    
    /**
     * Allows to create a task
     * 
     * @param _taskName
     * @param _taskDescription
     * @param _project
     * @param _role
     * @param _cact
     * @param recursive
     * @return true if the task is created, false in the other case
     */
    public boolean createTask(java.lang.String _taskName,
                              java.lang.String _taskDescription,
                              wilos.model.misc.project.Project _project,
                              wilos.model.spem2.role.RoleDescriptor _role,
                              wilos.model.misc.concreteactivity.ConcreteActivity _cact,
                              boolean recursive) {
        if (_role != null) {
            this.roleDescriptorDao.getSessionFactory().getCurrentSession(
                                                         ).saveOrUpdate(_role);
        }
        this.concreteActivityDao.getSessionFactory().getCurrentSession(
                                                       ).saveOrUpdate(_cact);
        this.concreteActivityDao.getSessionFactory().getCurrentSession(
                                                       ).saveOrUpdate(_project);
        wilos.model.spem2.task.TaskDescriptor td =
          this.createTaskDescriptor(_taskName, _taskDescription, _role,
                                    _taskName);
        if (_role == null) { td.setMainRole(null); }
        if (td == null) { return false; }
        if (recursive) {
            java.util.Iterator extfor$iter =
              _cact.getConcreteBreakdownElements().iterator();
            while (extfor$iter.hasNext()) {
                wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement cbe =
                  (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                    extfor$iter.next();
                if (cbe instanceof wilos.model.misc.concreteactivity.ConcreteActivity) {
                    if (!this.createConcreteTaskDescriptor(
                                _taskName, _project, td,
                                (wilos.model.misc.concreteactivity.ConcreteActivity) cbe))
                        return false;
                }
            }
        }
        if (!this.createConcreteTaskDescriptor(_taskName, _project, td, _cact))
            return false; else return true;
    }
    
    /**
     * Save processManager
     * 
     * @param _processmanager
     */
    public void saveProject(wilos.model.misc.project.Project _project) {
        this.projectDao.saveOrUpdateProject(_project);
    }
    
    /**
     * Delete a participant
     * 
     * @param participantId
     */
    public boolean deleteProject(java.lang.String projectId) {
        boolean ok = false;
        wilos.model.misc.project.Project project = this.getProject(projectId);
        if (project.getProcess() == null && project.getParticipants().size() ==
              0 && project.getProjectManager() == null) {
            this.projectDao.deleteProject(project);
            ok = true;
        }
        return ok;
    }
    
    /**
     * Check if the project already exist
     * 
     * @param _projectName
     * @return True is the _projectName is already present
     */
    public boolean projectExist(java.lang.String _projectName) {
        labeled_1 :
        {
            boolean found = false;
            java.lang.String projectName;
            java.util.List<wilos.model.misc.project.Project> projects =
                    this.projectDao.getAllProjects();
            java.util.Iterator extfor$iter = projects.iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.project.Project project =
                        (wilos.model.misc.project.Project) extfor$iter.next();
                projectName = project.getConcreteName().toUpperCase();
                if (projectName.equals(_projectName.toUpperCase()))
                {
                    return true;
                }
            }
        }
        return found;
    }
    
    /**
     * Gets the sorted project data.
     * 
     * @return table of sorted project data
     */
    public java.util.List<wilos.model.misc.project.Project> getAllSortedProjects() {
        labeled_2 :
        {
            java.util.List<wilos.model.misc.project.Project> projectList =
                    new java.util.ArrayList<wilos.model.misc.project.Project>();
            projectList = this.projectDao.getAllProjects();
            projects =
                    projectList.toArray(
                            new wilos.model.misc.project.Project[projectList.size(
                            )]);
            sortProject();
        }
        return java.util.Arrays.asList(projects);
    }
    
    /**
     * Sorts the list of project data.
     */
    @java.lang.SuppressWarnings("unchecked") 
    protected void sortProject() {
        java.util.Comparator comparator =
          new java.util.Comparator(
          ) {
            public int compare(java.lang.Object o1,
                               java.lang.Object o2) {
                wilos.model.misc.project.Project p1 =
                  (wilos.model.misc.project.Project) o1;
                wilos.model.misc.project.Project p2 =
                  (wilos.model.misc.project.Project) o2;
                return p1.getConcreteName().compareTo(p2.getConcreteName());
            }
        };
        java.util.Arrays.sort(projects, comparator);
    }
    
    /**
     * This method returns the list of the projects that aren't yet finished
     * 
     * @return a set of Projects
     */
    public java.util.Set<wilos.model.misc.project.Project> getUnfinishedProjects() {
        labeled_3 :
        {
            java.util.Set<wilos.model.misc.project.Project> unfinishedP =
                    new java.util.HashSet<wilos.model.misc.project.Project>();
            java.util.List<wilos.model.misc.project.Project> projects =
                    this.projectDao.getAllProjects();
            java.util.Iterator extfor$iter = projects.iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.project.Project project =
                        (wilos.model.misc.project.Project) extfor$iter.next();
                if (!project.getIsFinished())
                {
                    unfinishedP.add(project);
                }
            }
        }
        return unfinishedP;
    }
    
    /**
     * Allows to get the set of concreteBreakdownElements for a project
     * 
     * @param project
     * @return the set of concreteBreakdownElements
     */
    public java.util.Set<wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement> getConcreteBreakdownElementsFromProject(wilos.model.misc.project.Project _project) {
        java.util.Set<wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement> tmp =
          new java.util.HashSet<wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement>(
          );
        this.getProjectDao().getSessionFactory().getCurrentSession(
                                                   ).saveOrUpdate(_project);
        this.getProjectDao().getSessionFactory().getCurrentSession(
                                                   ).refresh(_project);
        java.util.Iterator extfor$iter =
          _project.getConcreteBreakdownElements().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement element =
              (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                extfor$iter.next();
            tmp.add(element);
        }
        return tmp;
    }
    
    /**
     * Allows to get the set of concreteRoleDescriptors from a project for a
     * concreteActivity
     * 
     * @param _project
     * @return the set of concreteRoleDescriptors
     */
    public java.util.Set<wilos.model.misc.concreterole.ConcreteRoleDescriptor> getConcreteRoleDescriptorsFromProject(wilos.model.misc.concreteactivity.ConcreteActivity _cact) {
        java.util.Set<wilos.model.misc.concreterole.ConcreteRoleDescriptor> tmp =
          new java.util.HashSet<wilos.model.misc.concreterole.ConcreteRoleDescriptor>(
          );
        this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                   _cact);
        labeled_4:
        {
            java.util.Iterator extfor$iter =
                    _cact.getConcreteBreakdownElements().iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement element =
                        (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                                extfor$iter.next();
                if (element instanceof wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                {
                    wilos.model.misc.concreterole.ConcreteRoleDescriptor crd =
                            (wilos.model.misc.concreterole.ConcreteRoleDescriptor)
                                    element;
                    tmp.add(crd);
                } else if (!(element instanceof wilos.model.misc.concretetask.ConcreteTaskDescriptor) &&
                        !(element instanceof wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor) &&
                        !(element instanceof wilos.model.misc.concretemilestone.ConcreteMilestone))
                {
                    wilos.model.misc.concreteactivity.ConcreteActivity cact = (wilos.model.misc.concreteactivity.ConcreteActivity)
                            element;
                    tmp.addAll(this.getConcreteRoleDescriptorsFromProject(cact));
                }
            }
        }
        return tmp;
    }
    
    /**
     * Getter of projectDao.
     * 
     * @return the projectDao.
     */
    public wilos.hibernate.misc.project.ProjectDao getProjectDao() {
        return this.projectDao;
    }
    
    /**
     * Setter of projectDao.
     * 
     * @param _projectDao
     *            The projectDao to set.
     */
    public void setProjectDao(wilos.hibernate.misc.project.ProjectDao _projectDao) {
        this.projectDao = _projectDao;
    }
    
    /**
     * This method returns all the projects.
     * 
     * @return A set of Project
     */
    public java.util.List<wilos.model.misc.project.Project> getAllProjects() {
        java.util.List<wilos.model.misc.project.Project> projectList =
          new java.util.ArrayList<wilos.model.misc.project.Project>();
        projectList = this.projectDao.getAllProjects();
        return projectList;
    }
    
    /**
     * This method returns all the projects with no process.
     * 
     * @return A set of Project
     */
    public java.util.Set<wilos.model.misc.project.Project> getAllProjectsWithNoProcess() {
        labeled_5 :
        {
            java.util.Set<wilos.model.misc.project.Project> projectList =
                    new java.util.HashSet<wilos.model.misc.project.Project>();
            java.util.List<wilos.model.misc.project.Project> tmpList =
                    new java.util.ArrayList<wilos.model.misc.project.Project>();
            tmpList = this.projectDao.getAllProjects();
            java.util.Iterator extfor$iter = tmpList.iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.project.Project project =
                        (wilos.model.misc.project.Project) extfor$iter.next();
                if (project.getProcess() == null) projectList.add(project);
            }
        }
        return projectList;
    }
    
    /**
     * Returns the projects that aren't associated to a process.
     * 
     * @return A set of Project
     */
    public java.util.Set<wilos.model.misc.project.Project> getAllProjectsWithProcess() {
        labeled_6 :
        {
            java.util.Set<wilos.model.misc.project.Project> projectList =
                    new java.util.HashSet<wilos.model.misc.project.Project>();
            java.util.List<wilos.model.misc.project.Project> tmpList =
                    new java.util.ArrayList<wilos.model.misc.project.Project>();
            tmpList = this.projectDao.getAllProjects();
            java.util.Iterator extfor$iter = tmpList.iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.misc.project.Project project =
                        (wilos.model.misc.project.Project) extfor$iter.next();
                if (project.getProcess() != null) projectList.add(project);
            }
        }
        return projectList;
    }
    
    /**
     * 
     * Getter of project
     * 
     * @param _id
     * @return the project
     */
    public wilos.model.misc.project.Project getProject(java.lang.String _id) {
        return this.projectDao.getProject(_id);
    }
    
    /**
     * Getter of participantDao.
     * 
     * @return the participantDao.
     */
    public wilos.hibernate.misc.wilosuser.ParticipantDao getParticipantDao() {
        return this.participantDao;
    }
    
    /**
     * 
     * Getter of processService
     * 
     * @return processService
     */
    public wilos.business.services.spem2.process.ProcessService getProcessService() {
        return this.processService;
    }
    
    /**
     * 
     * Setter of processService
     * 
     * @param _processService
     */
    public void setProcessService(wilos.business.services.spem2.process.ProcessService _processService) {
        this.processService = _processService;
    }
    
    /**
     * Setter of participantDao.
     * 
     * @param _participantDao
     *            The participantDao to set.
     */
    public void setParticipantDao(wilos.hibernate.misc.wilosuser.ParticipantDao _participantDao) {
        this.participantDao = _participantDao;
    }
    
    /**
     * 
     * Return the participants affected to the project
     * 
     * @param project
     * @return the set of participants affected to the project parameter
     */
    public java.util.Set<wilos.model.misc.wilosuser.Participant> getAllParticipants(wilos.model.misc.project.Project project) {
        return project.getParticipants();
    }
    
    /**
     * Allows to get the process for a project
     * 
     * @param _project
     * @return the process
     */
    public wilos.model.spem2.process.Process getProcessFromProject(wilos.model.misc.project.Project _project) {
        this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                  _project);
        return _project.getProcess();
    }
    
    /**
     * Allows to get all projects
     * 
     * @return projects
     */
    public wilos.model.misc.project.Project[] getProjects() { return projects; }
    
    /**
     * Allows to set a list of projects
     * 
     * @param projects
     */
    public void setProjects(wilos.model.misc.project.Project[] projects) {
        this.projects = projects;
    }
    
    /**
     * Allows to get the activityDao
     * 
     * @return the activityDao
     */
    public wilos.hibernate.spem2.activity.ActivityDao getActivityDao() {
        return this.activityDao;
    }
    
    /**
     * Allows to set the activityDao
     * 
     * @param _activityDao
     */
    public void setActivityDao(wilos.hibernate.spem2.activity.ActivityDao _activityDao) {
        this.activityDao = _activityDao;
    }
    
    /**
     * Allows to get the list of different paths of a roleDescriptor in process
     * 
     * @param _process
     * @param _roleName
     * @return the list of different paths
     */
    public java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> getDifferentPathsOfRoleDescriptorInProcess(wilos.model.spem2.process.Process _process,
                                                                  java.lang.String _roleName) {
        this.processService.getProcessDao().getSessionFactory(
                                              ).getCurrentSession(
                                                  ).saveOrUpdate(_process);
        wilos.model.misc.project.Project project =
          this.getProject(
                 (java.lang.String)
                   wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                    wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
        java.util.List<java.util.HashMap<java.lang.String,
        java.lang.Object>> lines =
          new java.util.ArrayList<java.util.HashMap<java.lang.String,
        java.lang.Object>>();
        java.lang.String path = _process.getPresentationName();
        return this.giveRoleDescriptorsPathName(project, _process, path,
                                                _roleName, lines);
    }
    
    /**
     * Allows to give the list of roleDescriptors path name
     * 
     * @param _process
     * @param path
     * @return the list of roleDescriptors path name
     */
    private java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> giveRoleDescriptorsPathName(wilos.model.misc.project.Project _project,
                                                   wilos.model.spem2.activity.Activity _act,
                                                   java.lang.String path,
                                                   java.lang.String _roleName,
                                                   java.util.List<java.util.HashMap<java.lang.String,
                                                   java.lang.Object>> lines) {
        final java.lang.String TABLE_LEAF = "images/expandableTable/leaf.gif";
        java.util.Iterator extfor$iter =
          _act.getBreakdownElements().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.breakdownelement.BreakdownElement bde =
              (wilos.model.spem2.breakdownelement.BreakdownElement)
                extfor$iter.next();
            if (bde instanceof wilos.model.spem2.role.RoleDescriptor) {
                if (bde.getPresentationName().equals(_roleName)) {
                    path += " / " + bde.getPresentationName();
                    java.util.HashMap<java.lang.String,
                    java.lang.Object> hm =
                      new java.util.HashMap<java.lang.String,
                    java.lang.Object>();
                    hm.put("nodeType", "leaf");
                    hm.put("expansionImage", TABLE_LEAF);
                    hm.put("id", bde.getId());
                    hm.put("name", path);
                    int nbcrd =
                      this.getConcreteRoleDescriptorsFromProject(_project).size(
                                                                             );
                    if (nbcrd > 0) {
                        hm.put("nbOccurences", new java.lang.Integer(0));
                    } else {
                        hm.put("nbOccurences", new java.lang.Integer(1));
                    }
                    hm.put("parentId", _roleName);
                    lines.add(hm);
                }
            }
            else
                if (bde instanceof wilos.model.spem2.activity.Activity) {
                    wilos.model.spem2.activity.Activity act =
                      (wilos.model.spem2.activity.Activity) bde;
                    java.lang.String newPath = path + " / " +
                    act.getPresentationName();
                    lines = this.giveRoleDescriptorsPathName(_project, act,
                                                             newPath, _roleName,
                                                             lines);
                }
        }
        return lines;
    }
    
    /**
     * Allows to get the concreteActivityDao
     * 
     * @return the concreteActivityDao
     */
    public wilos.hibernate.misc.concreteactivity.ConcreteActivityDao getConcreteActivityDao() {
        return concreteActivityDao;
    }
    
    /**
     * Allows to set the concreteActivityDao
     * 
     * @param _concreteActivityDao
     */
    public void setConcreteActivityDao(wilos.hibernate.misc.concreteactivity.ConcreteActivityDao _concreteActivityDao) {
        concreteActivityDao = _concreteActivityDao;
    }
    
    /**
     * Allows to get the concreteTaskDescriptorDao
     * 
     * @return the concreteTaskDescriptorDao
     */
    public wilos.hibernate.misc.concretetask.ConcreteTaskDescriptorDao getConcreteTaskDescriptorDao() {
        return concreteTaskDescriptorDao;
    }
    
    /**
     * Allows to set the concreteTaskDescriptorDao
     * 
     * @param _concreteTaskDescriptorDao
     */
    public void setConcreteTaskDescriptorDao(wilos.hibernate.misc.concretetask.ConcreteTaskDescriptorDao _concreteTaskDescriptorDao) {
        concreteTaskDescriptorDao = _concreteTaskDescriptorDao;
    }
    
    /**
     * Allows to get the set of concreteWorkProductDescriptors from a project
     * for a concreteActivity
     * 
     * @param _cact
     * @return the set of concreteWorkProductDescriptors
     */
    public java.util.Set<wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor> getConcreteWorkProductDescriptorsFromProject(wilos.model.misc.concreteactivity.ConcreteActivity _cact) {
        java.util.Set<wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor> tmp =
          new java.util.HashSet<wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor>(
          );
        this.activityDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                   _cact);
        java.util.Iterator extfor$iter =
          _cact.getConcreteBreakdownElements().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement element =
              (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                extfor$iter.next();
            if (element instanceof wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor) {
                wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor cwpd =
                  (wilos.model.misc.concreteworkproduct.ConcreteWorkProductDescriptor)
                    element;
                tmp.add(cwpd);
            } else
                if (!(element instanceof wilos.model.misc.concretetask.ConcreteTaskDescriptor) &&
                      !(element instanceof wilos.model.misc.concreterole.ConcreteRoleDescriptor) &&
                      !(element instanceof wilos.model.misc.concretemilestone.ConcreteMilestone)) {
                    wilos.model.misc.concreteactivity.ConcreteActivity cact = (wilos.model.misc.concreteactivity.ConcreteActivity)
                                                                                element;
                    tmp.addAll(this.getConcreteWorkProductDescriptorsFromProject(cact));
                }
        }
        return tmp;
    }
    
    /**
     * Allows to get the list of different paths of a workProductDescriptor in
     * process
     * 
     * @param _process
     * @param _workProductName
     * @return the list of different paths
     */
    public java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> getDifferentPathsOfWorkProductDescriptorInProcess(wilos.model.spem2.process.Process _process,
                                                                         java.lang.String _workProductName) {
        this.processService.getProcessDao().getSessionFactory(
                                              ).getCurrentSession(
                                                  ).saveOrUpdate(_process);
        wilos.model.misc.project.Project project =
          this.getProject(
                 (java.lang.String)
                   wilos.presentation.web.utils.WebSessionService.getAttribute(
                                                                    wilos.presentation.web.utils.WebSessionService.PROJECT_ID));
        java.util.List<java.util.HashMap<java.lang.String,
        java.lang.Object>> lines =
          new java.util.ArrayList<java.util.HashMap<java.lang.String,
        java.lang.Object>>();
        java.lang.String path = _process.getPresentationName();
        return this.giveWorkProductDescriptorsPathName(project, _process, path,
                                                       _workProductName, lines);
    }
    
    /**
     * Allows to get the list of workProductDescriptors path name
     * 
     * @param _project
     * @param _act
     * @param _path
     * @param _workProductName
     * @param _lines
     * @return the list of workProductDescriptors path name
     */
    private java.util.List<java.util.HashMap<java.lang.String,
    java.lang.Object>> giveWorkProductDescriptorsPathName(wilos.model.misc.project.Project _project,
                                                          wilos.model.spem2.activity.Activity _act,
                                                          java.lang.String _path,
                                                          java.lang.String _workProductName,
                                                          java.util.List<java.util.HashMap<java.lang.String,
                                                          java.lang.Object>> _lines) {
        final java.lang.String TABLE_LEAF = "images/expandableTable/leaf.gif";
        java.util.Iterator extfor$iter =
          _act.getBreakdownElements().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.breakdownelement.BreakdownElement bde =
              (wilos.model.spem2.breakdownelement.BreakdownElement)
                extfor$iter.next();
            if (bde instanceof wilos.model.spem2.workproduct.WorkProductDescriptor) {
                if (bde.getPresentationName().equals(_workProductName)) {
                    _path += " / " + bde.getPresentationName();
                    java.util.HashMap<java.lang.String,
                    java.lang.Object> hm =
                      new java.util.HashMap<java.lang.String,
                    java.lang.Object>();
                    hm.put("nodeType", "leaf");
                    hm.put("expansionImage", TABLE_LEAF);
                    hm.put("id", bde.getId());
                    hm.put("name", _path);
                    int nbcwpd =
                      this.getConcreteWorkProductDescriptorsFromProject(
                             _project).size();
                    if (nbcwpd > 0) {
                        hm.put("nbOccurences", new java.lang.Integer(0));
                    } else {
                        hm.put("nbOccurences", new java.lang.Integer(1));
                    }
                    hm.put("parentId", _workProductName);
                    _lines.add(hm);
                }
            }
            else
                if (bde instanceof wilos.model.spem2.activity.Activity) {
                    wilos.model.spem2.activity.Activity act =
                      (wilos.model.spem2.activity.Activity) bde;
                    java.lang.String newPath = _path + " / " +
                    act.getPresentationName();
                    _lines =
                      this.giveWorkProductDescriptorsPathName(_project, act,
                                                              newPath,
                                                              _workProductName,
                                                              _lines);
                }
        }
        return _lines;
    }
    
    /**
     * Allows to update the concreteActivity's state for a project
     * 
     * @param _project
     */
    public void updateConcreteActivitiesStateFromProject(wilos.model.misc.project.Project _project) {
        this.projectDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                  _project);
        this.projectDao.getSessionFactory().getCurrentSession().refresh(
                                                                  _project);
        this.stateService.updateStateTo(_project,
                                        wilos.utils.Constantes.State.READY);
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
     * Allows to get the concreteRoleDescriptorDao
     * 
     * @return the concreteRoleDescriptorDao
     */
    public wilos.hibernate.misc.concreterole.ConcreteRoleDescriptorDao getConcreteRoleDescriptorDao() {
        return concreteRoleDescriptorDao;
    }
    
    /**
     * Allows to set the concreteRoleDescriptorDao
     * 
     * @param concreteRoleDescriptorDao
     */
    public void setConcreteRoleDescriptorDao(wilos.hibernate.misc.concreterole.ConcreteRoleDescriptorDao concreteRoleDescriptorDao) {
        this.concreteRoleDescriptorDao = concreteRoleDescriptorDao;
    }
    
    /**
     * Allows to get the taskDescriptorDao
     * 
     * @return the taskDescriptorDao
     */
    public wilos.hibernate.spem2.task.TaskDescriptorDao getTaskDescriptorDao() {
        return taskDescriptorDao;
    }
    
    /**
     * Allows to set the taskDescriptorDao
     * 
     * @param taskDescriptorDao
     */
    public void setTaskDescriptorDao(wilos.hibernate.spem2.task.TaskDescriptorDao taskDescriptorDao) {
        this.taskDescriptorDao = taskDescriptorDao;
    }
    
    /**
     * Allows to get the roleDescriptorDao
     * 
     * @return the roleDescriptorDao
     */
    public wilos.hibernate.spem2.role.RoleDescriptorDao getRoleDescriptorDao() {
        return roleDescriptorDao;
    }
    
    /**
     * Allows to set the roleDescriptorDao
     * 
     * @param roleDescriptorDao
     */
    public void setRoleDescriptorDao(wilos.hibernate.spem2.role.RoleDescriptorDao roleDescriptorDao) {
        this.roleDescriptorDao = roleDescriptorDao;
    }
    
    /**
     * Allows to create a role
     * 
     * @param _roleName
     * @param _roleDescription
     * @param _project
     * @param _task
     * @param _cact
     * @return true if the role is created, false in the other case
     */
    public boolean createRole(java.lang.String _roleName,
                              java.lang.String _roleDescription,
                              wilos.model.misc.project.Project _project,
                              wilos.model.spem2.task.TaskDescriptor _task,
                              wilos.model.misc.concreteactivity.ConcreteActivity _cact) {
        if (_task != null) {
            this.taskDescriptorDao.getSessionFactory().getCurrentSession(
                                                         ).saveOrUpdate(_task);
        }
        this.concreteActivityDao.getSessionFactory().getCurrentSession(
                                                       ).saveOrUpdate(_cact);
        wilos.model.spem2.role.RoleDescriptor rd =
          this.createRoleDescriptor(_roleName, _roleDescription, _task,
                                    _roleName);
        if (rd == null) { return false; }
        java.util.Iterator extfor$iter =
          _cact.getConcreteBreakdownElements().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement cbe =
              (wilos.model.misc.concretebreakdownelement.ConcreteBreakdownElement)
                extfor$iter.next();
            if (cbe instanceof wilos.model.misc.concreteactivity.ConcreteActivity) {
                if (!this.createConcreteRoleDescriptor(
                            _roleName,
                            _project,
                            rd,
                            (wilos.model.misc.concreteactivity.ConcreteActivity)
                              cbe)) return false;
            }
        }
        if (!this.createConcreteRoleDescriptor(_roleName, _project, rd, _cact))
            return false; else return true;
    }
    
    /**
     * Allows to create a roleDescriptor
     * 
     * @param _presentationName
     * @param _description
     * @param _mainTask
     * @param _guid
     * @return the roleDescriptor created, null in the other case
     */
    public wilos.model.spem2.role.RoleDescriptor createRoleDescriptor(java.lang.String _presentationName,
                                                                      java.lang.String _description,
                                                                      wilos.model.spem2.task.TaskDescriptor _mainTask,
                                                                      java.lang.String _guid) {
        wilos.model.spem2.role.RoleDescriptor roleDesc =
          new wilos.model.spem2.role.RoleDescriptor();
        roleDesc.setIsOutOfProcess(true);
        roleDesc.setPresentationName(_presentationName);
        roleDesc.setDescription(_description);
        roleDesc.setGuid(_guid);
        roleDesc.setPrefix("");
        roleDesc.setIsPlanned(true);
        roleDesc.setHasMultipleOccurrences(false);
        roleDesc.setIsOptional(false);
        if (_mainTask != null) { _mainTask.addMainRole(roleDesc); }
        this.roleDescriptorDao.saveOrUpdateRoleDescriptor(roleDesc);
        if (roleDesc.getId() != null) return roleDesc; else return null;
    }
    
    /**
     * Allows to create a concreteRoleDescriptor
     * 
     * @param _concreteName
     * @param _project
     * @param _rd
     * @param _cact
     * @return true if the concreteRoleDescriptor is created, false in the other
     *         case
     */
    public boolean createConcreteRoleDescriptor(java.lang.String _concreteName,
                                                wilos.model.misc.project.Project _project,
                                                wilos.model.spem2.role.RoleDescriptor _rd,
                                                wilos.model.misc.concreteactivity.ConcreteActivity _cact) {
        wilos.model.misc.concreterole.ConcreteRoleDescriptor concRoleDesc =
          new wilos.model.misc.concreterole.ConcreteRoleDescriptor();
        concRoleDesc.setConcreteName(_concreteName);
        concRoleDesc.setProject(_project);
        concRoleDesc.setInstanciationOrder(1);
        concRoleDesc.addSuperConcreteActivity(_cact);
        concRoleDesc.setRoleDescriptor(_rd);
        concRoleDesc.setBreakdownElement(_rd);
        this.concreteRoleDescriptorDao.saveOrUpdateConcreteRoleDescriptor(
                                         concRoleDesc);
        return concRoleDesc.getId() != null;
    }
    
    /**
     * Allows to get the concreteWorkBreakdownElementService
     * 
     * @return the concreteWorkBreakdownElementService
     */
    public wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementService getConcreteWorkBreakdownElementService() {
        return this.concreteWorkBreakdownElementService;
    }
    
    /**
     * Allows to set the concreteWorkBreakdownElementService
     * 
     * @param _concreteWorkBreakdownElementService
     */
    public void setConcreteWorkBreakdownElementService(wilos.business.services.misc.concreteworkbreakdownelement.ConcreteWorkBreakdownElementService _concreteWorkBreakdownElementService) {
        this.concreteWorkBreakdownElementService =
          _concreteWorkBreakdownElementService;
    }
    
    /**
     * Allows to get the service's state
     * 
     * @return the stateService
     */
    public wilos.business.services.misc.stateservice.StateService getStateService() {
        return this.stateService;
    }
    
    /**
     * Allows to set the service's state
     * 
     * @param _stateService
     */
    public void setStateService(wilos.business.services.misc.stateservice.StateService _stateService) {
        this.stateService = _stateService;
    }
    
    public ProjectService() { super(); }
}

