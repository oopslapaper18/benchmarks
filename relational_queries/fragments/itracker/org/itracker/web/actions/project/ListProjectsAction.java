package org.
  itracker.
  web.
  actions.
  project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.itracker.model.PermissionType;
import org.itracker.model.Project;
import org.itracker.services.ProjectService;
import org.itracker.services.util.UserUtilities;
import org.itracker.web.actions.base.ItrackerBaseAction;
import org.itracker.web.ptos.ProjectPTO;
import org.itracker.web.util.RequestHelper;

public class ListProjectsAction extends org.
  itracker.
  web.
  actions.
  base.
  ItrackerBaseAction {
    private static final org.apache.log4j.Logger log = null;
    
    /**
    
     *
     returns
     listing
     of
     ALL
     projects
     with
     given
     permissions.
     Also
     locked
     and
    
     *
     view-only
     projects
     are
     selected.
    
     *
     
    
     *
     @param
     projectService
    
     *
     @param
     permissionFlags
    
     *
     @param
     permissions
    
     * @return
    
     */
    protected static java.
      util.
      List<org.
      itracker.
      web.
      ptos.
      ProjectPTO> getAllPTOs(org.itracker.services.
                               ProjectService projectService,
                             int[] permissionFlags,
                             final java.util.Map<java.lang.Integer,
                             java.util.Set<org.itracker.model.
                               PermissionType>> permissions) {
        java.util.List<org.itracker.model.Project> projects =
          projectService.getAllProjects();
        java.util.ArrayList<org.itracker.model.Project> projects_tmp =
          new java.util.ArrayList<org.itracker.model.Project>(projects);
        java.util.Iterator<org.itracker.model.Project> projectIt =
          projects.iterator();
        while (projectIt.hasNext()) {
            org.itracker.model.Project project = (org.itracker.model.Project)
                                                   projectIt.next();
            if (!org.itracker.services.util.UserUtilities.hasPermission(
                                                            permissions,
                                                            project.getId(),
                                                            permissionFlags)) {
                projects_tmp.remove(project);
            }
        }
        projects = projects_tmp;
        java.util.Collections.
          sort(projects, new org.itracker.model.Project.ProjectComparator());
        java.util.ArrayList<org.itracker.web.ptos.ProjectPTO> ptos =
          new java.util.ArrayList<org.itracker.web.ptos.ProjectPTO>(
          projects_tmp.size());
        projectIt = projects.iterator();
        while (projectIt.hasNext()) {
            org.itracker.model.Project project = projectIt.next();
            ptos.add(createProjectPTO(project, projectService, permissions));
        }
        return ptos;
    }
    
    /**
    
     * returns PTOs of all AVAILABLE projects, ensured permissions are set.
    
     * 
    
     * @param projectService
    
     * @param permissionFlags
    
     * @param permissions
    
     * @return
    
     */
    protected static java.
      util.
      List<org.
      itracker.
      web.
      ptos.
      ProjectPTO> getPTOs(org.itracker.services.ProjectService projectService,
                          int[] permissionFlags,
                          final java.util.Map<java.lang.Integer,
                          java.util.Set<org.itracker.model.
                            PermissionType>> permissions) {
        labeled_1 :
        {
            java.util.List<org.itracker.model.Project> projects =
                    projectService.getAllAvailableProjects();
            java.util.ArrayList<org.itracker.model.Project> projects_tmp =
                    new java.util.ArrayList<org.itracker.model.Project>(projects);
            java.util.Iterator<org.itracker.model.Project> projectIt =
                    projects.iterator();
            while (projectIt.hasNext())
            {
                org.itracker.model.Project project = (org.itracker.model.Project)
                        projectIt.next();
                if (!org.itracker.services.util.UserUtilities.hasPermission(
                        permissions,
                        project.getId(),
                        permissionFlags))
                {
                    projects_tmp.remove(project);
                }
            }
        }
        projects = projects_tmp;
        java.util.Collections.
          sort(projects, new org.itracker.model.Project.ProjectComparator());
        java.util.ArrayList<org.itracker.web.ptos.ProjectPTO> ptos =
          new java.util.ArrayList<org.itracker.web.ptos.ProjectPTO>(
          projects_tmp.size());
        projectIt = projects.iterator();
        while (projectIt.hasNext()) {
            org.itracker.model.Project project = projectIt.next();
            ptos.add(createProjectPTO(project, projectService, permissions));
        }
        return ptos;
    }
    
    public org.apache.struts.action.
      ActionForward execute(org.apache.struts.action.ActionMapping mapping,
                            org.apache.struts.action.ActionForm form,
                            javax.servlet.http.HttpServletRequest request,
                            javax.servlet.http.HttpServletResponse response)
          throws java.lang.Exception {
        final java.util.Map<java.lang.Integer,
        java.util.Set<org.itracker.model.PermissionType>> permissions =
          org.itracker.web.util.RequestHelper.getUserPermissions(
                                                request.getSession());
        org.itracker.services.ProjectService projectService =
          this.getITrackerServices().getProjectService();
        request.
          setAttribute(
            "projects",
            getPTOs(
              projectService,
              new int[] { org.itracker.services.util.UserUtilities.
                            PERMISSION_VIEW_ALL,
                org.itracker.services.util.UserUtilities.
                  PERMISSION_VIEW_USERS }, permissions));
        java.lang.String pageTitleKey = "itracker.web.listprojects.title";
        java.lang.String pageTitleArg = "";
        request.setAttribute("pageTitleKey", pageTitleKey);
        request.setAttribute("pageTitleArg", pageTitleArg);
        log.info("ListProjectsAction: Forward: listprojects");
        return mapping.findForward("list_projects");
    }
    
    /**
    
     * 
    
     * Makes a page transfer object for the project in first argument.
    
     * 
    
     * @param project
    
     *            - wrapped project for the pto
    
     * @param projectService
    
     *            - project-service
    
     * @param permissions
    
     *            - users permissions
    
     * @param stats
    
     *            - if true, additional stats will be generated
    
     *            (performance-problem):
    
     *            <ul>
    
     *            <li>no. open issues</li>
    
     *            <li>no. closed issues</li>
    
     *            <li>lasst updated issues updated-date</li>
    
     *            </ul>
    
     * @return
    
     */
    private static final org.
      itracker.
      web.
      ptos.
      ProjectPTO createProjectPTO(org.itracker.model.Project project,
                                  org.itracker.services.
                                    ProjectService projectService,
                                  final java.util.Map<java.lang.Integer,
                                  java.util.Set<org.itracker.model.
                                    PermissionType>> permissions) {
        org.itracker.web.ptos.ProjectPTO pto =
          new org.itracker.web.ptos.ProjectPTO(project, projectService,
                                               permissions);
        return pto;
    }
    
    public ListProjectsAction() { super(); }
}

