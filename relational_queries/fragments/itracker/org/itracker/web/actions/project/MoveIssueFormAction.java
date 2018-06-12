package org.
  itracker.
  web.
  actions.
  project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.itracker.model.Issue;
import org.itracker.model.PermissionType;
import org.itracker.model.Project;
import org.itracker.services.IssueService;
import org.itracker.services.ProjectService;
import org.itracker.services.util.UserUtilities;
import org.itracker.web.actions.base.ItrackerBaseAction;
import org.itracker.web.forms.MoveIssueForm;

public class MoveIssueFormAction extends org.
  itracker.
  web.
  actions.
  base.
  ItrackerBaseAction {
    private static final org.apache.log4j.Logger log = null;
    private static final java.lang.String UNAUTHORIZED_PAGE = "unauthorized";
    private static final java.lang.String PAGE_TITLE_KEY =
      "itracker.web.moveissue.title";
    
    public org.apache.struts.action.
      ActionForward execute(org.apache.struts.action.ActionMapping mapping,
                            org.apache.struts.action.ActionForm form,
                            javax.servlet.http.HttpServletRequest request,
                            javax.servlet.http.HttpServletResponse response)
          throws javax.servlet.ServletException, java.io.IOException {
        org.apache.struts.action.ActionMessages errors =
          new org.apache.struts.action.ActionMessages();
        request.setAttribute("pageTitleKey", PAGE_TITLE_KEY);
        request.setAttribute("pageTitleArg", "itracker.web.generic.unknown");
        try {
            org.itracker.services.IssueService issueService =
              getITrackerServices().getIssueService();
            org.itracker.services.ProjectService projectService =
              getITrackerServices().getProjectService();
            java.lang.Integer issueId =
              java.lang.Integer.valueOf(request.getParameter("id") == null
                                            ? "-1"
                                            : request.getParameter("id"));
            org.itracker.model.Issue issue = issueService.getIssue(issueId);
            if (issue == null) {
                errors.add(
                         org.apache.struts.action.ActionMessages.GLOBAL_MESSAGE,
                         new org.apache.struts.action.ActionMessage(
                             "itracker.web.error.invalidissue"));
            }
            else {
                request.setAttribute(
                          "pageTitleArg", issue.getId());
                if (errors.isEmpty()) {
                    if (!isPermissionGranted(
                           request, issue)) {
                        return mapping.findForward(UNAUTHORIZED_PAGE);
                    }
                    java.util.List<org.itracker.model.Project> projects =
                      projectService.getAllAvailableProjects();
                    if (projects.size() == 0) {
                        return mapping.findForward(UNAUTHORIZED_PAGE);
                    }
                    java.util.List<org.itracker.model.Project>
                      availableProjects = getAvailableProjects(request,
                                                               projects, issue);
                    if (availableProjects.size() == 0) {
                        errors.
                          add(
                            org.apache.struts.action.ActionMessages.
                              GLOBAL_MESSAGE,
                            new org.apache.struts.action.ActionMessage(
                                "itracker.web.error.noprojects"));
                    }
                    if (errors.isEmpty()) {
                        setupMoveIssueForm(request, form, issue,
                                           availableProjects);
                        return mapping.getInputForward();
                    }
                }
            }
        }
        catch (java.lang.RuntimeException e) {
            log.error("Exception while creating move issue form.", e);
            errors.add(
                     org.apache.struts.action.ActionMessages.GLOBAL_MESSAGE,
                     new org.apache.struts.action.ActionMessage(
                         "itracker.web.error.system"));
        }
        if (!errors.isEmpty()) { saveErrors(request, errors); }
        return mapping.findForward("error");
    }
    
    /**
     * Sets request attributes and fills MoveIssueForm.
     * 
     * @param request HttpServletRequest.
     * @param form ActionForm.
     * @param issue issue.
     * @param availableProjects list of available projects.
     */
    private void setupMoveIssueForm(javax.servlet.http.
                                      HttpServletRequest request,
                                    org.apache.struts.action.ActionForm form,
                                    org.itracker.model.Issue issue,
                                    java.util.List<org.itracker.model.
                                      Project> availableProjects) {
        org.itracker.web.forms.MoveIssueForm moveIssueForm =
          (org.itracker.web.forms.MoveIssueForm) form;
        if (moveIssueForm == null) {
            moveIssueForm = new org.itracker.web.forms.MoveIssueForm();
        }
        moveIssueForm.setIssueId(issue.getId());
        moveIssueForm.setCaller(request.getParameter("caller"));
        request.setAttribute("moveIssueForm", moveIssueForm);
        request.setAttribute("projects", availableProjects);
        request.setAttribute("issue", issue);
        saveToken(request);
        log.info(
              "No errors while moving issue. Forwarding to move issue form.");
    }
    
    /**
     * Returns list of available projects.
     * 
     * @param request HttpServletRequest.
     * @param projects list of all projects.
     * @param issue operated issue.
     * @return list of available projects.
     */
    private java.
      util.
      List<org.
      itracker.
      model.
      Project> getAvailableProjects(javax.servlet.http.
                                      HttpServletRequest request,
                                    java.util.List<org.itracker.model.
                                      Project> projects,
                                    org.itracker.model.Issue issue) {
        labeled_1 :
        {
            java.util.Map<java.lang.Integer,
                    java.util.Set<org.itracker.model.PermissionType>> userPermissions =
                    getUserPermissions(request.getSession());
            java.util.List<org.itracker.model.Project> availableProjects =
                    new java.util.ArrayList<org.itracker.model.Project>();
            int i = 0;
            while (i < projects.size())
            {
                if (projects.get(i).getId() != null &&
                        !projects.get(i).equals(issue.getProject()))
                {
                    if (org.
                            itracker.
                            services.
                            util.
                            UserUtilities.
                            hasPermission(
                                    userPermissions,
                                    projects.
                                            get(i).getId(),
                                    new int[]{org.itracker.services.util.UserUtilities.
                                            PERMISSION_EDIT,
                                            org.itracker.services.util.UserUtilities.
                                                    PERMISSION_CREATE}))
                    {
                        availableProjects.add(projects.get(i));
                    }
                }
                i++;
            }
            java.util.Collections.
                    sort(availableProjects,
                            new org.itracker.model.Project.ProjectComparator());
        }
        return availableProjects;
    }
    
    /**
     * Checks permissions.
     * 
     * @param request HttpServletRequest.
     * @param issue issue.
     * @return true if permission is granted.
     */
    private boolean isPermissionGranted(javax.servlet.http.
                                          HttpServletRequest request,
                                        org.itracker.model.Issue issue) {
        java.util.Map<java.lang.Integer,
        java.util.Set<org.itracker.model.PermissionType>> userPermissions =
          getUserPermissions(request.getSession());
        if (!org.itracker.services.util.UserUtilities.
              hasPermission(
                userPermissions, issue.getProject().getId(),
                org.itracker.services.util.UserUtilities.PERMISSION_EDIT)) {
            log.
              debug(
                "Unauthorized user requested access to move issue for issue " +
                  issue.getId());
            return false;
        }
        return true;
    }
    
    public MoveIssueFormAction() { super(); }
}

