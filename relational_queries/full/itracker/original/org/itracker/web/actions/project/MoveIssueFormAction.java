/*
 * This software was designed and created by Jason Carroll.
 * Copyright (c) 2002, 2003, 2004 Jason Carroll.
 * The author can be reached at jcarroll@cowsultants.com
 * ITracker website: http://www.cowsultants.com
 * ITracker forums: http://www.cowsultants.com/phpBB/index.php
 *
 * This program is free software; you can redistribute it and/or modify
 * it only under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.itracker.web.actions.project;

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

public class MoveIssueFormAction extends ItrackerBaseAction {
	
	private static final Logger log = null; //Logger.getLogger(MoveIssueFormAction.class);
	
    private static final String UNAUTHORIZED_PAGE = "unauthorized";
	private static final String PAGE_TITLE_KEY = "itracker.web.moveissue.title";
	


	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

    	ActionMessages errors = new ActionMessages();
		request.setAttribute("pageTitleKey", PAGE_TITLE_KEY);
		request.setAttribute("pageTitleArg", "itracker.web.generic.unknown");

		try {
			IssueService issueService = getITrackerServices().getIssueService();
			ProjectService projectService = getITrackerServices()
					.getProjectService();

			Integer issueId = Integer
					.valueOf((request.getParameter("id") == null ? "-1"
							: (request.getParameter("id"))));
			Issue issue = issueService.getIssue(issueId);
			if (issue == null) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"itracker.web.error.invalidissue"));
			} else {
				request.setAttribute("pageTitleArg", issue.getId());
				
				if (errors.isEmpty()) {
					if (!isPermissionGranted(request, issue)) {
						return mapping.findForward(UNAUTHORIZED_PAGE);
					}
					
					List<Project> projects = projectService.getAllAvailableProjects();	
					if (projects.size() == 0) {
						return mapping.findForward(UNAUTHORIZED_PAGE);
					}					
					
					List<Project> availableProjects = getAvailableProjects(request,
							projects, issue);
					if (availableProjects.size() == 0) {
						errors.add(ActionMessages.GLOBAL_MESSAGE,
								new ActionMessage("itracker.web.error.noprojects"));
					}
					
					if (errors.isEmpty()) {
						setupMoveIssueForm(request, form, issue, availableProjects);
						return mapping.getInputForward();
					}
				}
			}
		} catch (RuntimeException e) {
			log.error("Exception while creating move issue form.", e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					"itracker.web.error.system"));
		}
		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}
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
	private void setupMoveIssueForm(HttpServletRequest request, ActionForm form, Issue issue, List<Project> availableProjects){
		MoveIssueForm moveIssueForm = (MoveIssueForm) form;
		if (moveIssueForm == null) {
			moveIssueForm = new MoveIssueForm();
		}
		moveIssueForm.setIssueId(issue.getId());
		moveIssueForm.setCaller(request.getParameter("caller"));

		request.setAttribute("moveIssueForm", moveIssueForm);
		request.setAttribute("projects", availableProjects);
		request.setAttribute("issue", issue);
		saveToken(request);
		log.info("No errors while moving issue. Forwarding to move issue form.");	
	}
	
	/**
	 * Returns list of available projects.
	 * 
	 * @param request HttpServletRequest.
	 * @param projects list of all projects.
	 * @param issue operated issue.
	 * @return list of available projects.
	 */
	private List<Project> getAvailableProjects(HttpServletRequest request, List<Project> projects,
			Issue issue) {
		Map<Integer, Set<PermissionType>> userPermissions = getUserPermissions(request.getSession());
		List<Project> availableProjects = new ArrayList<Project>();
		for (int i = 0; i < projects.size(); i++) {
			if (projects.get(i).getId() != null
					&& !projects.get(i).equals(issue.getProject())) {
				if (UserUtilities.hasPermission(userPermissions,
						projects.get(i).getId(), new int[] {
								UserUtilities.PERMISSION_EDIT,
								UserUtilities.PERMISSION_CREATE })) {
					availableProjects.add(projects.get(i));
				}
			}
		}
		Collections.sort(availableProjects, new Project.ProjectComparator());
		return availableProjects;
	}
    /**
     * Checks permissions.
     * 
     * @param request HttpServletRequest.
     * @param issue issue.
     * @return true if permission is granted.
     */
    private boolean isPermissionGranted(HttpServletRequest request, Issue issue) {
        Map<Integer, Set<PermissionType>> userPermissions = getUserPermissions(request.getSession());

		if (!UserUtilities.hasPermission(userPermissions, issue.getProject().getId(),UserUtilities.PERMISSION_EDIT)) {
			log.debug("Unauthorized user requested access to move issue for issue "
							+  issue.getId());
            return false;
		}
        return true;
    }
}
