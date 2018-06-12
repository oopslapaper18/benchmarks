package org.itracker.web.actions.project;

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

public class ListProjectsAction extends ItrackerBaseAction {
	private static final Logger log = null; //Logger
//			.getLogger(ListProjectsAction.class);

	/**
	 * returns listing of ALL projects with given permissions. Also locked and
	 * view-only projects are selected.
	 * 
	 * @param projectService
	 * @param permissionFlags
	 * @param permissions
	 * @return
	 */
	protected static List<ProjectPTO> getAllPTOs(ProjectService projectService,
			int[] permissionFlags,
			final Map<Integer, Set<PermissionType>> permissions) {
		List<Project> projects = projectService.getAllProjects();

		ArrayList<Project> projects_tmp = new ArrayList<Project>(projects);
		Iterator<Project> projectIt = projects.iterator();
		while (projectIt.hasNext()) {
			Project project = (Project) projectIt.next();
			if (!UserUtilities.hasPermission(permissions, project.getId(),
					permissionFlags)) {
				projects_tmp.remove(project);
			}
		}

		projects = projects_tmp;
		Collections.sort(projects, new Project.ProjectComparator());

		ArrayList<ProjectPTO> ptos = new ArrayList<ProjectPTO>(projects_tmp
				.size());

		projectIt = projects.iterator();

		while (projectIt.hasNext()) {
			Project project = projectIt.next();
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
	protected static List<ProjectPTO> getPTOs(ProjectService projectService,
			int[] permissionFlags,
			final Map<Integer, Set<PermissionType>> permissions) {
		List<Project> projects = projectService.getAllAvailableProjects();

		ArrayList<Project> projects_tmp = new ArrayList<Project>(projects);
		Iterator<Project> projectIt = projects.iterator();
		while (projectIt.hasNext()) {
			Project project = (Project) projectIt.next();
			if (!UserUtilities.hasPermission(permissions, project.getId(),
					permissionFlags)) {
				projects_tmp.remove(project);
			}
		}

		projects = projects_tmp;
		Collections.sort(projects, new Project.ProjectComparator());

		ArrayList<ProjectPTO> ptos = new ArrayList<ProjectPTO>(projects_tmp
				.size());

		projectIt = projects.iterator();

		while (projectIt.hasNext()) {
			Project project = projectIt.next();
			ptos.add(createProjectPTO(project, projectService, permissions));
		}

		return ptos;
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		final Map<Integer, Set<PermissionType>> permissions = RequestHelper
				.getUserPermissions(request.getSession());

		ProjectService projectService = this.getITrackerServices()
				.getProjectService();

		request.setAttribute("projects", getPTOs(projectService, new int[] {
				UserUtilities.PERMISSION_VIEW_ALL,
				UserUtilities.PERMISSION_VIEW_USERS }, permissions));

		String pageTitleKey = "itracker.web.listprojects.title";
		String pageTitleArg = "";

		request.setAttribute("pageTitleKey", pageTitleKey);
		request.setAttribute("pageTitleArg", pageTitleArg);

		log.info("ListProjectsAction: Forward: listprojects");
		return mapping.findForward("list_projects");
	}

//	private static final void setupNumberOfIssues(ProjectPTO pto,
//			ProjectService service) {
//		pto.setTotalNumberIssues(service.getTotalNumberIssuesByProject(pto
//				.getId()));
//	}
//
//	private static final void setupNumberOfOpenIssues(ProjectPTO pto,
//			ProjectService service) {
//		pto.setTotalOpenIssues(service.getTotalNumberOpenIssuesByProject(pto
//				.getId()));
//	}
//
//	private static final void setupNumberOfResolvedIssues(ProjectPTO pto,
//			ProjectService service) {
//		pto.setTotalResolvedIssues(service
//				.getTotalNumberResolvedIssuesByProject(pto.getId()));
//	}
//
//	private static final void setupCanCreate(ProjectPTO pto,
//			final Map<Integer, Set<PermissionType>> permissions) {
//		pto.setCanCreate(UserUtilities.hasPermission(permissions, pto.getId(),
//				UserUtilities.PERMISSION_CREATE));
//	}
//
//	private static final void setupLastIssueUpdateDate(ProjectPTO pto,
//			ProjectService service) {
//		pto.setLastUpdatedIssueDate(service
//				.getLatestIssueUpdatedDateByProjectId(pto.getId()));
//	}

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
	private static final ProjectPTO createProjectPTO(Project project,
			ProjectService projectService,
			final Map<Integer, Set<PermissionType>> permissions) {
		ProjectPTO pto = new ProjectPTO(project, projectService, permissions);
//		if (stats) {
//			setupNumberOfOpenIssues(pto, projectService);
//			setupNumberOfResolvedIssues(pto, projectService);
//			setupLastIssueUpdateDate(pto, projectService);
//		} else {
//			// just add a total-numbers issuess
//			setupNumberOfIssues(pto, projectService);
//		}
//		setupCanCreate(pto, permissions);
		return pto;
	}
}
