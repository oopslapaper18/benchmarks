package org.
  itracker.
  web.
  actions.
  admin.
  project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.itracker.model.Component;
import org.itracker.model.CustomField;
import org.itracker.model.NameValuePair;
import org.itracker.model.PermissionType;
import org.itracker.model.Project;
import org.itracker.model.ProjectScript;
import org.itracker.model.Status;
import org.itracker.model.User;
import org.itracker.model.Version;
import org.itracker.services.ITrackerServices;
import org.itracker.services.ProjectService;
import org.itracker.services.UserService;
import org.itracker.services.util.CustomFieldUtilities;
import org.itracker.services.util.IssueUtilities;
import org.itracker.services.util.ProjectUtilities;
import org.itracker.services.util.UserUtilities;
import org.itracker.services.util.WorkflowUtilities;
import org.itracker.web.forms.ProjectForm;
import org.itracker.web.ptos.ProjectScriptPTO;
import org.itracker.web.util.Constants;
import org.itracker.web.util.LoginUtilities;
import org.itracker.web.util.RequestHelper;
import org.itracker.web.util.ServletContextUtils;

public class EditProjectFormActionUtil {
    private static final org.apache.log4j.Logger log = null;
    public class CustomFieldInfo {
        private int id;
        private java.lang.String name;
        private java.lang.String type;
        
        public CustomFieldInfo(java.lang.Integer id,
                               java.lang.String customFieldName,
                               java.lang.String string) {
            super();
            this.id = id;
            this.name = customFieldName;
            this.type = string;
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public java.lang.String getName() {
            return name;
        }
        
        public void setName(java.lang.String name) {
            this.name = name;
        }
        
        public java.lang.String getType() {
            return type;
        }
        
        public void setType(java.lang.String type) {
            this.type = type;
        }
    }
    
    public class VersionInfo {
        private int id;
        private java.lang.String number;
        private java.lang.String description;
        private java.util.Date lastModifiedDate;
        private java.lang.Long countIssuesByVersion;
        
        public VersionInfo(int id,
                           java.lang.String number,
                           java.lang.String description,
                           java.util.Date lastModifiedDate,
                           java.lang.Long countIssuesByVersion) {
            super();
            this.id = id;
            this.number = number;
            this.description = description;
            this.lastModifiedDate = lastModifiedDate;
            this.countIssuesByVersion = countIssuesByVersion;
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public java.lang.String getNumber() {
            return number;
        }
        
        public void setNumber(java.lang.String number) {
            this.number = number;
        }
        
        public java.lang.String getDescription() {
            return description;
        }
        
        public void setDescription(java.lang.String description) {
            this.description = description;
        }
        
        public java.util.Date getDate() {
            return lastModifiedDate;
        }
        
        public void setDate(java.util.Date lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
        }
        
        public java.lang.Long getCount() {
            return countIssuesByVersion;
        }
        
        public void setCount(java.lang.Long countIssuesByVersion) {
            this.countIssuesByVersion = countIssuesByVersion;
        }
    }
    
    public class ComponentInfo {
        private int id;
        private java.lang.String name;
        private java.lang.String description;
        private java.util.Date lastModifiedDate;
        private java.lang.Long countIssuesByComponent;
        
        public ComponentInfo(java.lang.Integer id,
                             java.lang.String name,
                             java.lang.String description,
                             java.util.Date lastModifiedDate,
                             java.lang.Long countIssuesByComponent) {
            super();
            this.id = id;
            this.name = name;
            this.description = description;
            this.lastModifiedDate = lastModifiedDate;
            this.countIssuesByComponent = countIssuesByComponent;
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public java.lang.String getName() {
            return name;
        }
        
        public void setName(java.lang.String name) {
            this.name = name;
        }
        
        public java.lang.String getDescription() {
            return description;
        }
        
        public void setDescription(java.lang.String description) {
            this.description = description;
        }
        
        public java.util.Date getDate() {
            return lastModifiedDate;
        }
        
        public void setDate(java.util.Date date) {
            this.lastModifiedDate = date;
        }
        
        public java.lang.Long getCount() {
            return countIssuesByComponent;
        }
        
        public void setCount(java.lang.Long countIssuesByComponent) {
            this.countIssuesByComponent = countIssuesByComponent;
        }
    }
    
    public org.apache.struts.action.
      ActionForward init(org.apache.struts.action.ActionMapping mapping,
                         javax.servlet.http.HttpServletRequest request,
                         org.itracker.web.forms.ProjectForm form) {
        org.itracker.services.ITrackerServices itrackerServices =
          org.itracker.web.util.ServletContextUtils.getItrackerServices();
        org.itracker.services.ProjectService projectService =
          itrackerServices.getProjectService();
        org.itracker.services.UserService userService =
          itrackerServices.getUserService();
        javax.servlet.http.HttpSession session = request.getSession(true);
        org.itracker.model.User user =
          (org.itracker.model.User)
            session.getAttribute(org.itracker.web.util.Constants.USER_KEY);
        java.lang.Boolean allowPermissionUpdate =
          userService.
          allowPermissionUpdates(
            user, null,
            org.itracker.services.util.UserUtilities.AUTH_TYPE_UNKNOWN,
            org.itracker.services.util.UserUtilities.REQ_SOURCE_WEB);
        final java.util.Map<java.lang.Integer,
        java.util.Set<org.itracker.model.PermissionType>> permissions =
          org.itracker.web.util.RequestHelper.getUserPermissions(session);
        org.itracker.model.Project project =
          (org.itracker.model.Project)
            session.getAttribute(org.itracker.web.util.Constants.PROJECT_KEY);
        boolean isUpdate;
        if (project == null) {
            log.info("EditProjectAction: Forward: unauthorized");
            return mapping.findForward("unauthorized");
        } else {
            isUpdate = false;
            if (!project.isNew()) { isUpdate = true; }
        }
        request.setAttribute("isUpdate", isUpdate);
        setupTitle(request, form, projectService);
        java.util.List<org.itracker.model.NameValuePair> statuses =
          new java.util.ArrayList<org.itracker.model.NameValuePair>();
        statuses.
          add(
            new org.
                itracker.
                model.
                NameValuePair(
                org.itracker.services.util.ProjectUtilities.
                    getStatusName(
                      org.itracker.model.Status.ACTIVE,
                      org.itracker.web.util.LoginUtilities.getCurrentLocale(
                                                             request)),
                java.lang.Integer.toString(
                                    org.itracker.model.Status.ACTIVE.getCode(
                                                                       ))));
        statuses.
          add(
            new org.
                itracker.
                model.
                NameValuePair(
                org.itracker.services.util.ProjectUtilities.
                    getStatusName(
                      org.itracker.model.Status.VIEWABLE,
                      org.itracker.web.util.LoginUtilities.getCurrentLocale(
                                                             request)),
                java.lang.Integer.toString(
                                    org.itracker.model.Status.VIEWABLE.getCode(
                                                                         ))));
        statuses.
          add(
            new org.
                itracker.
                model.
                NameValuePair(
                org.itracker.services.util.ProjectUtilities.
                    getStatusName(
                      org.itracker.model.Status.LOCKED,
                      org.itracker.web.util.LoginUtilities.getCurrentLocale(
                                                             request)),
                java.lang.Integer.toString(
                                    org.itracker.model.Status.LOCKED.getCode(
                                                                       ))));
        request.setAttribute("statuses", statuses);
        java.util.Set<org.itracker.model.User> owners =
          new java.util.TreeSet<org.itracker.model.User>(
          org.itracker.model.User.NAME_COMPARATOR);
        if (!project.isNew()) {
            owners.
              addAll(
                userService.
                    getUsersWithProjectPermission(
                      project.getId(),
                      org.itracker.services.util.UserUtilities.
                        PERMISSION_VIEW_ALL));
        } else {
            owners.addAll(userService.getSuperUsers());
        }
        owners.addAll(project.getOwners());
        request.setAttribute("owners", owners);
        boolean allowPermissionUpdateOption =
          allowPermissionUpdate ==
          null
          ? false
          : allowPermissionUpdate &&
          org.itracker.services.util.UserUtilities.
          hasPermission(
            permissions, new java.lang.Integer(-1),
            org.itracker.services.util.UserUtilities.PERMISSION_USER_ADMIN);
        request.setAttribute("allowPermissionUpdateOption",
                             allowPermissionUpdateOption);
        if (project.isNew()) {
            labeled_1 :
            {
                java.util.List<org.itracker.model.User> users =
                        new java.util.ArrayList<org.itracker.model.User>();
                java.util.List<org.itracker.model.User> activeUsers =
                        userService.getActiveUsers();
                java.util.Collections.sort(activeUsers,
                        org.itracker.model.User.NAME_COMPARATOR);
                int i = 0;
                boolean skip_0 = false;
                while (i < activeUsers.size())
                {
                    skip_0 = false;
                    if (!skip_0)
                        if (owners.contains(activeUsers.get(i)))
                        {
                            skip_0 = true;
                        }
                    if (!skip_0) users.add(activeUsers.get(i));
                    i++;
                }
            }
            request.setAttribute("users", users);
        }
        java.util.List<org.itracker.model.NameValuePair> permissionNames =
          org.itracker.services.util.UserUtilities.
          getPermissionNames(
            org.itracker.web.util.LoginUtilities.getCurrentLocale(request));
        request.setAttribute("permissions", permissionNames);
        request.
          setAttribute(
            "optionSupressHistoryHtml",
            java.
                lang.
                Integer.
                toString(
                  org.itracker.services.util.ProjectUtilities.
                    OPTION_SURPRESS_HISTORY_HTML));
        request.
          setAttribute(
            "optionPredefinedResolutions",
            java.
                lang.
                Integer.
                toString(
                  org.itracker.services.util.ProjectUtilities.
                    OPTION_PREDEFINED_RESOLUTIONS));
        request.
          setAttribute(
            "optionAllowAssignToClose",
            java.
                lang.
                Integer.
                toString(
                  org.itracker.services.util.ProjectUtilities.
                    OPTION_ALLOW_ASSIGN_TO_CLOSE));
        request.
          setAttribute(
            "optionAllowSefRegisteredCreate",
            java.
                lang.
                Integer.
                toString(
                  org.itracker.services.util.ProjectUtilities.
                    OPTION_ALLOW_SELF_REGISTERED_CREATE));
        request.
          setAttribute(
            "optionLiteralHistoryHtml",
            java.
                lang.
                Integer.
                toString(
                  org.itracker.services.util.ProjectUtilities.
                    OPTION_LITERAL_HISTORY_HTML));
        request.
          setAttribute(
            "optionNoAttachments",
            java.
                lang.
                Integer.
                toString(
                  org.itracker.services.util.ProjectUtilities.
                    OPTION_NO_ATTACHMENTS));
        request.
          setAttribute(
            "optionAllowSelfRegisteredViewAll",
            java.
                lang.
                Integer.
                toString(
                  org.itracker.services.util.ProjectUtilities.
                    OPTION_ALLOW_SELF_REGISTERED_VIEW_ALL));
        java.util.List<org.itracker.model.CustomField> customFields =
          org.itracker.services.util.IssueUtilities.getCustomFields();
        java.util.List<org.itracker.web.actions.admin.project.
          EditProjectFormActionUtil.CustomFieldInfo> fieldInfos =
          new java.util.ArrayList<org.itracker.web.actions.admin.project.
          EditProjectFormActionUtil.CustomFieldInfo>(customFields.size());
        java.util.Iterator<org.itracker.model.CustomField> fieldsIt =
          customFields.iterator();
        org.itracker.model.CustomField ci;
        while (fieldsIt.hasNext()) {
            ci = (org.itracker.model.CustomField) fieldsIt.next();
            fieldInfos.
              add(
                new org.
                    itracker.
                    web.
                    actions.
                    admin.
                    project.
                    EditProjectFormActionUtil.
                    CustomFieldInfo(
                    ci.
                        getId(),
                    org.itracker.services.util.CustomFieldUtilities.
                        getCustomFieldName(
                          ci.getId(),
                          org.itracker.web.util.LoginUtilities.getCurrentLocale(
                                                                 request)),
                    org.itracker.services.util.CustomFieldUtilities.
                        getTypeString(
                          ci.getFieldType(),
                          org.itracker.web.util.LoginUtilities.getCurrentLocale(
                                                                 request))));
        }
        request.setAttribute("customFields", fieldInfos);
        java.util.List<org.itracker.model.ProjectScript> scripts =
          project.getScripts();
        java.util.Collections.
          sort(scripts,
               org.itracker.model.ProjectScript.FIELD_PRIORITY_COMPARATOR);
        java.util.Locale locale =
          org.itracker.web.util.LoginUtilities.getCurrentLocale(request);
        java.util.Iterator<org.itracker.model.ProjectScript> it =
          scripts.iterator();
        java.util.List<org.itracker.web.ptos.ProjectScriptPTO> scriptPTOs =
          new java.util.ArrayList<org.itracker.web.ptos.ProjectScriptPTO>(
          scripts.size());
        while (it.hasNext()) {
            org.itracker.web.ptos.ProjectScriptPTO projectScript =
              new org.itracker.web.ptos.ProjectScriptPTO(it.next(), locale);
            scriptPTOs.add(projectScript);
        }
        request.setAttribute("projectScripts", scriptPTOs);
        java.util.List<org.itracker.model.Version> versions =
          project.getVersions();
        java.util.Collections.
          sort(versions, new org.itracker.model.Version.VersionComparator());
        java.util.List<org.itracker.web.actions.admin.project.
          EditProjectFormActionUtil.VersionInfo> vis =
          new java.util.ArrayList<org.itracker.web.actions.admin.project.
          EditProjectFormActionUtil.VersionInfo>();
        for (org.itracker.model.Version v : versions)
            vis.
              add(
                new org.itracker.web.actions.admin.project.
                    EditProjectFormActionUtil.VersionInfo(
                    v.getId(), v.getNumber(), v.getDescription(),
                    v.getLastModifiedDate(),
                    projectService.countIssuesByVersion(v.getId())));
        request.setAttribute("versions", vis);
        java.util.List<org.itracker.model.Component> components =
          project.getComponents();
        java.util.Collections.sort(components);
        java.util.List<org.itracker.web.actions.admin.project.
          EditProjectFormActionUtil.ComponentInfo> cis =
          new java.util.ArrayList<org.itracker.web.actions.admin.project.
          EditProjectFormActionUtil.ComponentInfo>();
        for (org.itracker.model.Component c : components)
            cis.
              add(
                new org.itracker.web.actions.admin.project.
                    EditProjectFormActionUtil.ComponentInfo(
                    c.getId(), c.getName(), c.getDescription(),
                    c.getLastModifiedDate(),
                    projectService.countIssuesByComponent(c.getId())));
        request.setAttribute("components", cis);
        return null;
    }
    
    /**
     * Setup the title for the Project-Form Action
     * @param request -  the servlet request
     * @param form - must be a ProjectForm
     * @param projectService - project-service
     */
    public void setupTitle(javax.servlet.http.HttpServletRequest request,
                           org.itracker.web.forms.ProjectForm form,
                           org.itracker.services.
                             ProjectService projectService) {
        java.lang.String pageTitleKey;
        java.lang.String pageTitleArg = "";
        if ("update".equals(((org.itracker.web.forms.ProjectForm)
                               form).getAction())) {
            pageTitleKey = "itracker.web.admin.editproject.title.update";
            if (form instanceof org.itracker.web.forms.ProjectForm) {
                org.itracker.model.Project project =
                  projectService.getProject(
                                   ((org.itracker.web.forms.ProjectForm)
                                      form).getId());
                if (null != project) { pageTitleArg = project.getName(); }
            }
        } else {
            ((org.itracker.web.forms.ProjectForm) form).setAction("create");
            pageTitleKey = "itracker.web.admin.editproject.title.create";
        }
        request.setAttribute("pageTitleKey", pageTitleKey);
        request.setAttribute("pageTitleArg", pageTitleArg);
    }
    
    public EditProjectFormActionUtil() { super(); }
}

