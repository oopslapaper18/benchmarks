package org.
  itracker.
  services.
  implementations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.itracker.core.resources.ITrackerResources;
import org.itracker.model.AbstractEntity;
import org.itracker.model.Component;
import org.itracker.model.CustomField;
import org.itracker.model.Issue;
import org.itracker.model.IssueActivity;
import org.itracker.model.IssueActivityType;
import org.itracker.model.IssueAttachment;
import org.itracker.model.IssueField;
import org.itracker.model.IssueHistory;
import org.itracker.model.IssueRelation;
import org.itracker.model.IssueSearchQuery;
import org.itracker.model.Notification;
import org.itracker.model.PermissionType;
import org.itracker.model.Project;
import org.itracker.model.Status;
import org.itracker.model.User;
import org.itracker.model.Version;
import org.itracker.model.Notification.Role;
import org.itracker.model.Notification.Type;
import org.itracker.persistence.dao.ComponentDAO;
import org.itracker.persistence.dao.CustomFieldDAO;
import org.itracker.persistence.dao.IssueActivityDAO;
import org.itracker.persistence.dao.IssueAttachmentDAO;
import org.itracker.persistence.dao.IssueDAO;
import org.itracker.persistence.dao.IssueHistoryDAO;
import org.itracker.persistence.dao.IssueRelationDAO;
import org.itracker.persistence.dao.ProjectDAO;
import org.itracker.persistence.dao.UserDAO;
import org.itracker.persistence.dao.VersionDAO;
import org.itracker.services.IssueService;
import org.itracker.services.NotificationService;
import org.itracker.services.exceptions.IssueSearchException;
import org.itracker.services.exceptions.ProjectException;
import org.itracker.services.util.IssueUtilities;
import org.itracker.web.util.ServletContextUtils;

/**
 *
 Issue
 related
 service
 layer.
 A
 bit
 "fat"
 at
 this
 time,
 because
 of
 being
 a
 *
 direct
 EJB
 porting.
 Going
 go
 get
 thinner
 over
 time
 *
 
 *
 @author
 ricardo
 * 
 */
public class IssueServiceImpl implements org.
  itracker.
  services.
  IssueService {
    private static final org.apache.log4j.Logger logger = null;
    private org.itracker.persistence.dao.CustomFieldDAO customFieldDAO;
    private org.itracker.persistence.dao.UserDAO userDAO;
    private org.itracker.persistence.dao.ProjectDAO projectDAO;
    private org.itracker.persistence.dao.IssueDAO issueDAO;
    private org.itracker.persistence.dao.IssueHistoryDAO issueHistoryDAO;
    private org.itracker.persistence.dao.IssueRelationDAO issueRelationDAO;
    private org.itracker.persistence.dao.IssueAttachmentDAO issueAttachmentDAO;
    private org.itracker.persistence.dao.ComponentDAO componentDAO;
    private org.itracker.persistence.dao.IssueActivityDAO issueActivityDAO;
    private org.itracker.persistence.dao.VersionDAO versionDAO;
    private org.itracker.services.NotificationService notificationService;
    
    public IssueServiceImpl(org.itracker.persistence.dao.UserDAO userDAO,
                            org.itracker.persistence.dao.ProjectDAO projectDAO,
                            org.itracker.persistence.dao.IssueDAO issueDAO,
                            org.itracker.persistence.dao.
                              IssueHistoryDAO issueHistoryDAO,
                            org.itracker.persistence.dao.
                              IssueRelationDAO issueRelationDAO,
                            org.itracker.persistence.dao.
                              IssueAttachmentDAO issueAttachmentDAO,
                            org.itracker.persistence.dao.
                              ComponentDAO componentDAO,
                            org.itracker.persistence.dao.
                              IssueActivityDAO issueActivityDAO,
                            org.itracker.persistence.dao.VersionDAO versionDAO,
                            org.itracker.persistence.dao.
                              CustomFieldDAO customFieldDAO,
                            org.itracker.services.
                              NotificationService notificationService) {
        super();
        this.userDAO = userDAO;
        this.projectDAO = projectDAO;
        this.issueDAO = issueDAO;
        this.issueHistoryDAO = issueHistoryDAO;
        this.issueRelationDAO = issueRelationDAO;
        this.issueAttachmentDAO = issueAttachmentDAO;
        this.componentDAO = componentDAO;
        this.issueActivityDAO = issueActivityDAO;
        this.versionDAO = versionDAO;
        this.customFieldDAO = customFieldDAO;
        this.notificationService = notificationService;
    }
    
    public org.itracker.model.Issue getIssue(java.lang.Integer issueId) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        return issue;
    }
    
    /**
     * @deprecated don't use to expensive memory use!
     */
    public java.util.List<org.itracker.model.Issue> getAllIssues() {
        logger.warn("getAllIssues: use of deprecated API");
        if (logger.isDebugEnabled()) {
            logger.debug("getAllIssues: stacktrace was",
                         new java.lang.RuntimeException());
        }
        return getIssueDAO().findAll();
    }
    
    /**
     * Added implementation to make proper count of ALL issues, instead select
     * them in a list and return its size
     */
    public java.lang.Long getNumberIssues() {
        return getIssueDAO().countAllIssues();
    }
    
    public java.util.List<org.itracker.model.
      Issue> getIssuesCreatedByUser(java.lang.Integer userId) {
        return getIssuesCreatedByUser(userId, true);
    }
    
    public java.util.List<org.itracker.model.
      Issue> getIssuesCreatedByUser(java.lang.Integer userId,
                                    boolean availableProjectsOnly) {
        final java.util.List<org.itracker.model.Issue> issues;
        if (availableProjectsOnly) {
            issues =
              getIssueDAO().
                findByCreatorInAvailableProjects(
                  userId,
                  org.itracker.services.util.IssueUtilities.STATUS_CLOSED);
        }
        else {
            issues =
              getIssueDAO().
                findByCreator(
                  userId,
                  org.itracker.services.util.IssueUtilities.STATUS_CLOSED);
        }
        return issues;
    }
    
    public java.util.List<org.itracker.model.
      Issue> getIssuesOwnedByUser(java.lang.Integer userId) {
        return getIssuesOwnedByUser(userId, true);
    }
    
    public java.util.List<org.itracker.model.
      Issue> getIssuesOwnedByUser(java.lang.Integer userId,
                                  boolean availableProjectsOnly) {
        final java.util.List<org.itracker.model.Issue> issues;
        if (availableProjectsOnly) {
            issues =
              getIssueDAO().
                findByOwnerInAvailableProjects(
                  userId,
                  org.itracker.services.util.IssueUtilities.STATUS_RESOLVED);
        }
        else {
            issues =
              getIssueDAO().
                findByOwner(
                  userId,
                  org.itracker.services.util.IssueUtilities.STATUS_RESOLVED);
        }
        return issues;
    }
    
    public java.util.List<org.itracker.model.
      Issue> getIssuesWatchedByUser(java.lang.Integer userId) {
        return getIssuesWatchedByUser(userId, true);
    }
    
    /**
     * TODO move to {@link NotificationService}
     */
    public java.util.List<org.itracker.model.
      Issue> getIssuesWatchedByUser(java.lang.Integer userId,
                                    boolean availableProjectsOnly) {
        final java.util.List<org.itracker.model.Issue> issues;
        if (availableProjectsOnly) {
            issues =
              getIssueDAO().
                findByNotificationInAvailableProjects(
                  userId,
                  org.itracker.services.util.IssueUtilities.STATUS_CLOSED);
        }
        else {
            issues =
              getIssueDAO().
                findByNotification(
                  userId,
                  org.itracker.services.util.IssueUtilities.STATUS_CLOSED);
        }
        return issues;
    }
    
    public java.util.List<org.itracker.model.Issue> getUnassignedIssues() {
        return getUnassignedIssues(true);
    }
    
    public java.util.List<org.itracker.model.
      Issue> getUnassignedIssues(boolean availableProjectsOnly) {
        final java.util.List<org.itracker.model.Issue> issues;
        if (availableProjectsOnly) {
            issues =
              getIssueDAO().
                findByStatusLessThanEqualToInAvailableProjects(
                  org.itracker.services.util.IssueUtilities.STATUS_UNASSIGNED);
        }
        else {
            issues =
              getIssueDAO().
                findByStatusLessThanEqualTo(
                  org.itracker.services.util.IssueUtilities.STATUS_UNASSIGNED);
        }
        return issues;
    }
    
    /**
     * 
     * Returns all issues with a status equal to the given status number
     * 
     * 
     * 
     * @param status
     * 
     *            the status to compare
     * 
     * @return an array of IssueModels that match the criteria
     * 
     */
    public java.util.List<org.itracker.model.
      Issue> getIssuesWithStatus(int status) {
        java.util.List<org.itracker.model.Issue> issues =
          getIssueDAO().findByStatus(status);
        return issues;
    }
    
    /**
     * 
     * Returns all issues with a status less than the given status number
     * 
     * 
     * 
     * @param status
     * 
     *            the status to compare
     * 
     * @return an array of IssueModels that match the criteria
     */
    public java.util.List<org.itracker.model.
      Issue> getIssuesWithStatusLessThan(int status) {
        java.util.List<org.itracker.model.Issue> issues =
          getIssueDAO().findByStatusLessThan(status);
        return issues;
    }
    
    /**
     * 
     * Returns all issues with a severity equal to the given severity number
     * 
     * 
     * 
     * @param severity
     * 
     *            the severity to compare
     * 
     * @return an array of IssueModels that match the criteria
     * 
     */
    public java.util.List<org.itracker.model.
      Issue> getIssuesWithSeverity(int severity) {
        java.util.List<org.itracker.model.Issue> issues =
          getIssueDAO().findBySeverity(severity);
        return issues;
    }
    
    public java.util.List<org.itracker.model.
      Issue> getIssuesByProjectId(java.lang.Integer projectId) {
        return getIssuesByProjectId(
                 projectId,
                 org.itracker.services.util.IssueUtilities.STATUS_END);
    }
    
    public java.util.List<org.itracker.model.
      Issue> getIssuesByProjectId(java.lang.Integer projectId, int status) {
        java.util.List<org.itracker.model.Issue> issues =
          getIssueDAO().findByProjectAndLowerStatus(projectId, status);
        return issues;
    }
    
    public org.itracker.model.User getIssueCreator(java.lang.Integer issueId) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        org.itracker.model.User user = issue.getCreator();
        return user;
    }
    
    public org.itracker.model.User getIssueOwner(java.lang.Integer issueId) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        org.itracker.model.User user = issue.getOwner();
        return user;
    }
    
    public java.util.List<org.itracker.model.
      Component> getIssueComponents(java.lang.Integer issueId) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        java.util.List<org.itracker.model.Component> components =
          issue.getComponents();
        return components;
    }
    
    public java.util.List<org.itracker.model.
      Version> getIssueVersions(java.lang.Integer issueId) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        java.util.List<org.itracker.model.Version> versions =
          issue.getVersions();
        return versions;
    }
    
    public java.util.List<org.itracker.model.
      IssueAttachment> getIssueAttachments(java.lang.Integer issueId) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        java.util.List<org.itracker.model.IssueAttachment> attachments =
          issue.getAttachments();
        return attachments;
    }
    
    /**
     * Old implementation is left here, commented, because it checked for
     * history entry status. This feature was not finished, I think (RJST)
     */
    public java.util.List<org.itracker.model.
      IssueHistory> getIssueHistory(java.lang.Integer issueId) {
        return getIssueDAO().findByPrimaryKey(issueId).getHistory();
    }
    
    public org.itracker.model.Issue createIssue(org.itracker.model.Issue issue,
                                                java.lang.Integer projectId,
                                                java.lang.Integer userId,
                                                java.lang.Integer createdById)
          throws org.itracker.services.exceptions.ProjectException {
        org.itracker.model.Project project =
          getProjectDAO().findByPrimaryKey(projectId);
        org.itracker.model.User creator = getUserDAO().findByPrimaryKey(userId);
        if (project.getStatus() != org.itracker.model.Status.ACTIVE) {
            throw new org.itracker.services.exceptions.ProjectException(
              "Project is not active.");
        }
        org.itracker.model.IssueActivity activity =
          new org.itracker.model.IssueActivity(
          issue, creator, org.itracker.model.IssueActivityType.ISSUE_CREATED);
        activity.
          setDescription(
            org.itracker.core.resources.ITrackerResources.
                getString("itracker.activity.system.createdfor") + " " +
              creator.getFirstName() + " " + creator.getLastName());
        activity.setIssue(issue);
        if (!(createdById == null || createdById.equals(userId))) {
            org.itracker.model.User createdBy =
              getUserDAO().findByPrimaryKey(createdById);
            activity.setUser(createdBy);
            org.itracker.model.Notification watchModel =
              new org.itracker.model.Notification();
            watchModel.setUser(createdBy);
            watchModel.setIssue(issue);
            watchModel.setRole(
                         org.itracker.model.Notification.Role.CONTRIBUTER);
            issue.getNotifications().add(watchModel);
        }
        java.util.List<org.itracker.model.IssueActivity> activities =
          new java.util.ArrayList<org.itracker.model.IssueActivity>();
        activities.add(activity);
        issue.setActivities(activities);
        issue.setProject(project);
        issue.setCreator(creator);
        getIssueDAO().save(issue);
        return issue;
    }
    
    /**
     * Save a modified issue to the persistence layer
     * 
     * @param issueDirty
     *            the changed, unsaved issue to update on persistency layer
     * @param userId
     *            the user-id of the changer
     * 
     */
    public org.itracker.model.
      Issue updateIssue(final org.itracker.model.Issue issueDirty,
                        final java.lang.Integer userId)
          throws org.itracker.services.exceptions.ProjectException {
        java.lang.String existingTargetVersion = null;
        getIssueDAO().detach(issueDirty);
        org.itracker.model.Issue persistedIssue =
          getIssueDAO().findByPrimaryKey(issueDirty.getId());
        getIssueDAO().refresh(persistedIssue);
        if (logger.isDebugEnabled()) {
            logger.debug("updateIssue: updating issue " + issueDirty +
                           "\n(from " + persistedIssue + ")");
        }
        org.itracker.model.User user = getUserDAO().findByPrimaryKey(userId);
        if (persistedIssue.getProject().getStatus() !=
              org.itracker.model.Status.ACTIVE) {
            throw new org.itracker.services.exceptions.ProjectException(
              "Project " + persistedIssue.getProject().getName() +
                " is not active.");
        }
        if (!persistedIssue.getDescription().equalsIgnoreCase(
                                               issueDirty.getDescription())) {
            if (logger.isDebugEnabled()) {
                logger.debug("updateIssue: updating description from " +
                               persistedIssue.getDescription());
            }
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity();
            activity.setActivityType(
                       org.itracker.model.IssueActivityType.DESCRIPTION_CHANGE);
            activity.
              setDescription(
                org.itracker.core.resources.ITrackerResources.
                    getString("itracker.web.generic.from") + ": " +
                  persistedIssue.getDescription());
            activity.setUser(user);
            activity.setIssue(issueDirty);
            issueDirty.getActivities().add(activity);
        }
        if (persistedIssue.getResolution() !=
              null &&
              !persistedIssue.getResolution().equalsIgnoreCase(
                                                issueDirty.getResolution())) {
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity();
            activity.setActivityType(
                       org.itracker.model.IssueActivityType.RESOLUTION_CHANGE);
            activity.
              setDescription(
                org.itracker.core.resources.ITrackerResources.
                    getString("itracker.web.generic.from") + ": " +
                  persistedIssue.getResolution());
            activity.setUser(user);
            activity.setIssue(issueDirty);
            issueDirty.getActivities().add(activity);
        }
        if (null == persistedIssue.getStatus() ||
              !persistedIssue.getStatus().equals(issueDirty.getStatus())) {
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity();
            activity.setActivityType(
                       org.itracker.model.IssueActivityType.STATUS_CHANGE);
            activity.
              setDescription(
                org.itracker.services.util.IssueUtilities.
                    getStatusName(persistedIssue.getStatus()) +
                  " " +
                  org.itracker.core.resources.ITrackerResources.
                    getString("itracker.web.generic.to") +
                  " " +
                  org.itracker.services.util.IssueUtilities.
                    getStatusName(issueDirty.getStatus()));
            activity.setUser(user);
            activity.setIssue(issueDirty);
            issueDirty.getActivities().add(activity);
        }
        if (issueDirty.getSeverity() != null &&
              !issueDirty.getSeverity().equals(persistedIssue.getSeverity()) &&
              issueDirty.getSeverity() != -1) {
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity();
            activity.setActivityType(
                       org.itracker.model.IssueActivityType.SEVERITY_CHANGE);
            activity.
              setDescription(
                org.itracker.services.util.IssueUtilities.
                    getSeverityName(persistedIssue.getSeverity()) +
                  " " +
                  org.itracker.core.resources.ITrackerResources.
                    getString("itracker.web.generic.to") +
                  " " +
                  org.itracker.services.util.IssueUtilities.
                    getSeverityName(issueDirty.getSeverity()));
            activity.setUser(user);
            activity.setIssue(issueDirty);
            issueDirty.getActivities().add(activity);
        }
        if (persistedIssue.getTargetVersion() !=
              null &&
              issueDirty.getTargetVersion() !=
              null &&
              !persistedIssue.getTargetVersion().getId().
              equals(issueDirty.getTargetVersion().getId())) {
            existingTargetVersion =
              persistedIssue.getTargetVersion().getNumber();
            org.itracker.model.Version version =
              this.getVersionDAO().findByPrimaryKey(
                                     issueDirty.getTargetVersion().getId());
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity();
            activity.
              setActivityType(
                org.itracker.model.IssueActivityType.TARGETVERSION_CHANGE);
            java.lang.String description =
              existingTargetVersion +
            " " +
            org.itracker.core.resources.ITrackerResources.
              getString("itracker.web.generic.to") + " ";
            description += version.getNumber();
            activity.setDescription(description);
            activity.setUser(user);
            activity.setIssue(issueDirty);
            issueDirty.getActivities().add(activity);
        }
        org.itracker.model.User newOwner = issueDirty.getOwner();
        issueDirty.setOwner(persistedIssue.getOwner());
        if (logger.isDebugEnabled()) {
            logger.debug("updateIssue: assigning from " +
                           issueDirty.getOwner() + " to " + newOwner);
        }
        assignIssue(issueDirty, newOwner, user, false);
        if (logger.isDebugEnabled()) {
            logger.debug("updateIssue: updated assignment: " + issueDirty);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("updateIssue: merging issue " + issueDirty + " to " +
                           persistedIssue);
        }
        persistedIssue = getIssueDAO().merge(issueDirty);
        if (logger.isDebugEnabled()) {
            logger.debug("updateIssue: merged issue for saving: " +
                           persistedIssue);
        }
        getIssueDAO().saveOrUpdate(persistedIssue);
        if (logger.isDebugEnabled()) {
            logger.debug("updateIssue: saved issue: " + persistedIssue);
        }
        return persistedIssue;
    }
    
    /**
     * 
     * Moves an issues from its current project to a new project.
     * 
     * 
     * 
     * @param issue
     * 
     *            an Issue of the issue to move
     * 
     * @param projectId
     * 
     *            the id of the target project
     * 
     * @param userId
     * 
     *            the id of the user that is moving the issue
     * 
     * @return an Issue of the issue after it has been moved
     */
    public org.itracker.model.Issue moveIssue(org.itracker.model.Issue issue,
                                              java.lang.Integer projectId,
                                              java.lang.Integer userId) {
        if (logger.isDebugEnabled()) {
            logger.debug("moveIssue: " + issue + " to project#" + projectId +
                           ", user#" + userId);
        }
        org.itracker.model.Project project =
          getProjectDAO().findByPrimaryKey(projectId);
        org.itracker.model.User user = getUserDAO().findByPrimaryKey(userId);
        if (logger.isDebugEnabled()) {
            logger.debug("moveIssue: " + issue + " to project: " + project +
                           ", user: " + user);
        }
        org.itracker.model.IssueActivity activity =
          new org.itracker.model.IssueActivity();
        activity.setActivityType(
                   org.itracker.model.IssueActivityType.ISSUE_MOVE);
        activity.
          setDescription(
            issue.getProject().getName() +
              " " +
              org.itracker.core.resources.ITrackerResources.
                getString("itracker.web.generic.to") + " " + project.getName());
        activity.setUser(user);
        activity.setIssue(issue);
        issue.setProject(project);
        issue.getActivities().add(activity);
        if (logger.isDebugEnabled()) {
            logger.debug("moveIssue: updated issue: " + issue);
        }
        try { getIssueDAO().saveOrUpdate(issue); }
        catch (java.lang.Exception e) {
            logger.error("moveIssue: failed to save issue: " + issue, e);
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("moveIssue: saved move-issue to " + project);
        }
        return issue;
    }
    
    /**
     * this should not exist. adding an history entry should be adding the
     * history entry to the domain object and saving the object...
     */
    public boolean addIssueHistory(org.itracker.model.IssueHistory history) {
        getIssueHistoryDAO().saveOrUpdate(history);
        history.getIssue().getHistory().add(history);
        getIssueDAO().saveOrUpdate(history.getIssue());
        return true;
    }
    
    /**
     * TODO maybe it has no use at all. is it obsolete? when I'd set the
     * issue-fields on an issue and then save/update issue, would it be good
     * enough?
     */
    public boolean setIssueFields(java.lang.Integer issueId,
                                  java.util.List<org.itracker.model.
                                    IssueField> fields) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        setIssueFields(issue, fields, true);
        return true;
    }
    
    private boolean setIssueFields(org.itracker.model.Issue issue,
                                   java.util.List<org.itracker.model.
                                     IssueField> fields, boolean save) {
        java.util.List<org.itracker.model.IssueField> issueFields =
          issue.getFields();
        if (fields.size() > 0) {
            int i = 0;
            while (i < fields.size()) {
                org.itracker.model.IssueField field = fields.get(i);
                if (issueFields.contains(field)) { issueFields.remove(field); }
                org.itracker.model.CustomField customField =
                  getCustomFieldDAO().findByPrimaryKey(
                                        fields.get(i).getCustomField().getId());
                field.setCustomField(customField);
                field.setIssue(issue);
                issueFields.add(field);
                i++;
            }
        }
        issue.setFields(issueFields);
        if (save) {
            logger.debug("setIssueFields: save was true");
            getIssueDAO().saveOrUpdate(issue);
        }
        return true;
    }
    
    public boolean setIssueComponents(java.lang.Integer issueId,
                                      java.util.HashSet<java.lang.
                                        Integer> componentIds,
                                      java.lang.Integer userId) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        java.util.List<org.itracker.model.Component> components =
          new java.util.ArrayList<org.itracker.model.Component>(
          componentIds.size());
        org.itracker.model.User user = userDAO.findByPrimaryKey(userId);
        java.util.Iterator<java.lang.Integer> idIt = componentIds.iterator();
        while (idIt.hasNext()) {
            java.lang.Integer id = (java.lang.Integer) idIt.next();
            org.itracker.model.Component c = getComponentDAO().findById(id);
            components.add(c);
        }
        setIssueComponents(issue, components, user, true);
        return true;
    }
    
    private boolean setIssueComponents(org.itracker.model.Issue issue,
                                       java.util.List<org.itracker.model.
                                         Component> components,
                                       org.itracker.model.User user,
                                       boolean save) {
        if (issue.getComponents() == null) {
            if (logger.isInfoEnabled()) {
                logger.info("setIssueComponents: components was null");
            }
            issue.setComponents(
                    new java.util.ArrayList<org.itracker.model.Component>(
                        components.size()));
        }
        if (components.isEmpty() && !issue.getComponents().isEmpty()) {
            addComponentsModifiedActivity(
              issue,
              user,
              new java.
                  lang.
                  StringBuilder(
                  org.itracker.core.resources.ITrackerResources.
                      getString("itracker.web.generic.all")).
                  append(" ").
                  append(
                    org.itracker.core.resources.ITrackerResources.
                        getString("itracker.web.generic.removed")).toString());
            issue.getComponents().clear();
        }
        else {
            java.util.Collections.
              sort(issue.getComponents(),
                   org.itracker.model.Component.NAME_COMPARATOR);
            java.util.Iterator<org.itracker.model.Component> iterator =
              issue.getComponents().iterator();
            while (iterator.hasNext()) {
                org.itracker.model.Component component =
                  (org.itracker.model.Component) iterator.next();
                if (components.contains(component)) {
                    components.remove(component);
                }
                else {
                    addComponentsModifiedActivity(
                      issue,
                      user,
                      new java.
                          lang.
                          StringBuilder(
                          org.itracker.core.resources.ITrackerResources.
                              getString("itracker.web.generic.removed")).
                          append(": ").append(component.getName()).toString());
                    iterator.remove();
                }
            }
            java.util.Collections.
              sort(components, org.itracker.model.Component.NAME_COMPARATOR);
            iterator = components.iterator();
            while (iterator.hasNext()) {
                org.itracker.model.Component component = iterator.next();
                if (!issue.getComponents().contains(component)) {
                    addComponentsModifiedActivity(
                      issue,
                      user,
                      new java.
                          lang.
                          StringBuilder(
                          org.itracker.core.resources.ITrackerResources.
                              getString("itracker.web.generic.added")).append(
                                                                         ": ").
                          append(component.getName()).toString());
                    issue.getComponents().add(component);
                }
            }
        }
        if (save) {
            if (logger.isDebugEnabled()) {
                logger.debug("setIssueComponents: save was true");
            }
            getIssueDAO().saveOrUpdate(issue);
        }
        return true;
    }
    
    /**
     * used by setIssueComponents for adding change activities
     * 
     * @param issue
     * @param user
     * @param description
     */
    private void addComponentsModifiedActivity(org.itracker.model.Issue issue,
                                               org.itracker.model.User user,
                                               java.lang.String description) {
        org.itracker.model.IssueActivity activity =
          new org.itracker.model.IssueActivity();
        activity.setActivityType(
                   org.itracker.model.IssueActivityType.COMPONENTS_MODIFIED);
        activity.setDescription(description);
        activity.setIssue(issue);
        activity.setUser(user);
        issue.getActivities().add(activity);
    }
    
    private boolean setIssueVersions(org.itracker.model.Issue issue,
                                     java.util.List<org.itracker.model.
                                       Version> versions,
                                     org.itracker.model.User user,
                                     boolean save) {
        if (issue.getVersions() == null) {
            if (logger.isInfoEnabled()) {
                logger.info("setIssueVersions: versions were null!");
            }
            issue.setVersions(
                    new java.util.ArrayList<org.itracker.model.Version>());
        }
        if (versions.isEmpty() && !issue.getVersions().isEmpty()) {
            addVersionsModifiedActivity(
              issue,
              user,
              new java.
                  lang.
                  StringBuilder(
                  org.itracker.core.resources.ITrackerResources.
                      getString("itracker.web.generic.all")).
                  append(" ").
                  append(
                    org.itracker.core.resources.ITrackerResources.
                        getString("itracker.web.generic.removed")).toString());
            issue.getVersions().clear();
        }
        else {
            java.util.Collections.
              sort(issue.getVersions(),
                   org.itracker.model.Version.VERSION_COMPARATOR);
            java.lang.StringBuilder changesBuf = new java.lang.StringBuilder();
            java.util.Iterator<org.itracker.model.Version> iterator =
              issue.getVersions().iterator();
            while (iterator.hasNext()) {
                org.itracker.model.Version version = iterator.next();
                if (versions.contains(version)) {
                    versions.remove(version);
                } else {
                    if (changesBuf.length() > 0) { changesBuf.append(", "); }
                    changesBuf.append(version.getNumber());
                    iterator.remove();
                }
            }
            if (changesBuf.length() > 0) {
                addVersionsModifiedActivity(
                  issue,
                  user,
                  new java.
                      lang.
                      StringBuilder(
                      org.itracker.core.resources.ITrackerResources.
                          getString("itracker.web.generic.removed")).append(
                                                                       ": ").
                      append(changesBuf).toString());
            }
            changesBuf = new java.lang.StringBuilder();
            java.util.Collections.
              sort(versions, org.itracker.model.Version.VERSION_COMPARATOR);
            iterator = versions.iterator();
            while (iterator.hasNext()) {
                org.itracker.model.Version version = iterator.next();
                if (changesBuf.length() > 0) { changesBuf.append(", "); }
                changesBuf.append(version.getNumber());
                issue.getVersions().add(version);
            }
            if (changesBuf.length() > 0) {
                addVersionsModifiedActivity(
                  issue,
                  user,
                  new java.
                      lang.
                      StringBuilder(
                      org.itracker.core.resources.ITrackerResources.
                          getString("itracker.web.generic.added")).append(": ").
                      append(changesBuf).toString());
            }
        }
        if (save) {
            if (logger.isDebugEnabled()) {
                logger.debug("setIssueVersions: updating issue: " + issue);
            }
            getIssueDAO().saveOrUpdate(issue);
        }
        return true;
    }
    
    /**
     * used by setIssueComponents for adding change activities
     * 
     * @param issue
     * @param user
     * @param description
     */
    private void addVersionsModifiedActivity(org.itracker.model.Issue issue,
                                             org.itracker.model.User user,
                                             java.lang.String description) {
        org.itracker.model.IssueActivity activity =
          new org.itracker.model.IssueActivity();
        activity.setActivityType(
                   org.itracker.model.IssueActivityType.TARGETVERSION_CHANGE);
        activity.setDescription(description);
        activity.setIssue(issue);
        activity.setUser(user);
        issue.getActivities().add(activity);
    }
    
    public boolean setIssueVersions(java.lang.Integer issueId,
                                    java.util.HashSet<java.lang.
                                      Integer> versionIds,
                                    java.lang.Integer userId) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        org.itracker.model.User user = userDAO.findByPrimaryKey(userId);
        java.util.ArrayList<org.itracker.model.Version> versions =
          new java.util.ArrayList<org.itracker.model.Version>(
          versionIds.size());
        java.util.Iterator<java.lang.Integer> versionsIdIt =
          versionIds.iterator();
        while (versionsIdIt.hasNext()) {
            java.lang.Integer id = versionsIdIt.next();
            versions.add(getVersionDAO().findByPrimaryKey(id));
        }
        return setIssueVersions(issue, versions, user, true);
    }
    
    public org.itracker.model.
      IssueRelation getIssueRelation(java.lang.Integer relationId) {
        org.itracker.model.IssueRelation issueRelation =
          getIssueRelationDAO().findByPrimaryKey(relationId);
        return issueRelation;
    }
    
    /**
     * add a relation between two issues.
     * 
     * TODO: There is no relation saved to database yet?
     */
    public boolean addIssueRelation(java.lang.Integer issueId,
                                    java.lang.Integer relatedIssueId,
                                    int relationType,
                                    java.lang.Integer userId) {
        org.itracker.model.User user = getUserDAO().findByPrimaryKey(userId);
        if (null == user) {
            throw new java.lang.IllegalArgumentException("Invalid user-id: " +
                                                           userId);
        }
        if (issueId != null && relatedIssueId != null) {
            int matchingRelationType =
              org.itracker.services.util.IssueUtilities.getMatchingRelationType(
                                                          relationType);
            org.itracker.model.Issue issue =
              getIssueDAO().findByPrimaryKey(issueId);
            org.itracker.model.Issue relatedIssue =
              getIssueDAO().findByPrimaryKey(relatedIssueId);
            org.itracker.model.IssueRelation relationA =
              new org.itracker.model.IssueRelation();
            relationA.setRelationType(relationType);
            relationA.setIssue(issue);
            relationA.setRelatedIssue(relatedIssue);
            relationA.setMatchingRelationId(0);
            relationA.setLastModifiedDate(
                        new java.sql.Timestamp(new java.util.Date().getTime()));
            getIssueRelationDAO().saveOrUpdate(relationA);
            org.itracker.model.IssueRelation relationB =
              new org.itracker.model.IssueRelation();
            relationB.setRelationType(matchingRelationType);
            relationB.setIssue(relatedIssue);
            relationB.setRelatedIssue(issue);
            relationB.setMatchingRelationId(relationA.getId());
            relationB.setLastModifiedDate(
                        new java.sql.Timestamp(new java.util.Date().getTime()));
            getIssueRelationDAO().saveOrUpdate(relationB);
            relationA.setMatchingRelationId(relationB.getId());
            getIssueRelationDAO().saveOrUpdate(relationA);
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity();
            activity.setActivityType(
                       org.itracker.model.IssueActivityType.RELATION_ADDED);
            activity.
              setDescription(
                org.
                    itracker.
                    core.
                    resources.
                    ITrackerResources.
                    getString(
                      "itracker.activity.relation.add",
                      new java.lang.Object[] { org.itracker.services.util.
                          IssueUtilities.getRelationName(relationType),
                        relatedIssueId }));
            activity.setIssue(issue);
            issue.getActivities().add(activity);
            activity.setUser(user);
            getIssueDAO().saveOrUpdate(issue);
            activity = new org.itracker.model.IssueActivity();
            activity.setActivityType(
                       org.itracker.model.IssueActivityType.RELATION_ADDED);
            activity.
              setDescription(
                org.
                    itracker.
                    core.
                    resources.
                    ITrackerResources.
                    getString(
                      "itracker.activity.relation.add",
                      new java.lang.Object[] { org.itracker.services.util.
                          IssueUtilities.getRelationName(matchingRelationType),
                        issueId }));
            activity.setIssue(relatedIssue);
            activity.setUser(user);
            relatedIssue.getActivities().add(activity);
            getIssueDAO().saveOrUpdate(relatedIssue);
            return true;
        }
        return false;
    }
    
    public void removeIssueRelation(java.lang.Integer relationId,
                                    java.lang.Integer userId) {
        org.itracker.model.IssueRelation issueRelation =
          getIssueRelationDAO().findByPrimaryKey(relationId);
        java.lang.Integer issueId = issueRelation.getIssue().getId();
        java.lang.Integer relatedIssueId =
          issueRelation.getRelatedIssue().getId();
        java.lang.Integer matchingRelationId =
          issueRelation.getMatchingRelationId();
        if (matchingRelationId != null) {
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity();
            activity.setActivityType(
                       org.itracker.model.IssueActivityType.RELATION_REMOVED);
            activity.
              setDescription(
                org.itracker.core.resources.ITrackerResources.
                    getString("itracker.activity.relation.removed",
                              issueId.toString()));
        }
        org.itracker.model.IssueActivity activity =
          new org.itracker.model.IssueActivity();
        activity.setActivityType(
                   org.itracker.model.IssueActivityType.RELATION_REMOVED);
        activity.
          setDescription(
            org.itracker.core.resources.ITrackerResources.
                getString("itracker.activity.relation.removed",
                          relatedIssueId.toString()));
        getIssueRelationDAO().delete(issueRelation);
    }
    
    public boolean assignIssue(java.lang.Integer issueId,
                               java.lang.Integer userId) {
        return assignIssue(issueId, userId, userId);
    }
    
    /**
     * only use for updating issue from actions..
     */
    public boolean assignIssue(java.lang.Integer issueId,
                               java.lang.Integer userId,
                               java.lang.Integer assignedByUserId) {
        return assignIssue(getIssueDAO().findByPrimaryKey(issueId),
                           getUserDAO().findByPrimaryKey(userId),
                           getUserDAO().findByPrimaryKey(assignedByUserId),
                           true);
    }
    
    /**
     * Only for use
     * 
     * @param issueId
     * @param userId
     * @param assignedByUserId
     * @param save
     *            save issue and send notification
     * @return
     */
    private boolean assignIssue(org.itracker.model.Issue issue,
                                org.itracker.model.User user,
                                org.itracker.model.User assignedByUser,
                                final boolean save) {
        if (issue.getOwner() == user || null != issue.getOwner() &&
              issue.getOwner().equals(user)) {
            if (logger.isDebugEnabled()) {
                logger.debug("assignIssue: attempted to reassign " + issue +
                               " to current owner " + user);
            }
            return false;
        }
        if (null == user) {
            if (logger.isInfoEnabled()) {
                logger.info("assignIssue: call to unasign " + issue);
            }
            return unassignIssue(issue, assignedByUser, save);
        }
        if (logger.isInfoEnabled()) {
            logger.info("assignIssue: assigning " + issue + " to " + user);
        }
        org.itracker.model.User currOwner = issue.getOwner();
        if (!user.equals(currOwner)) {
            if (currOwner !=
                  null &&
                  !notificationService.
                  hasIssueNotification(
                    issue, currOwner.getId(),
                    org.itracker.model.Notification.Role.IP)) {
                org.itracker.model.Notification notification =
                  new org.itracker.model.Notification(
                  currOwner, issue, org.itracker.model.Notification.Role.IP);
                if (save) {
                    notificationService.addIssueNotification(notification);
                } else {
                    issue.getNotifications().add(notification);
                }
            }
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity();
            activity.setActivityType(
                       org.itracker.model.IssueActivityType.OWNER_CHANGE);
            activity.
              setDescription(
                (currOwner ==
                   null
                   ? "[" +
                 org.itracker.core.resources.ITrackerResources.
                   getString("itracker.web.generic.unassigned") +
                 "]"
                   : currOwner.getLogin()) +
                  " " +
                  org.itracker.core.resources.ITrackerResources.
                    getString("itracker.web.generic.to") +
                  " " +
                  user.getLogin());
            activity.setUser(assignedByUser);
            activity.setIssue(issue);
            issue.getActivities().add(activity);
            issue.setOwner(user);
            if (logger.isDebugEnabled()) {
                logger.debug("assignIssue: current status: " +
                               issue.getStatus());
            }
            if (issue.getStatus() <
                  org.itracker.services.util.IssueUtilities.STATUS_ASSIGNED) {
                issue.
                  setStatus(
                    org.itracker.services.util.IssueUtilities.STATUS_ASSIGNED);
                if (logger.isDebugEnabled()) {
                    logger.debug("assignIssue: new status set to " +
                                   issue.getStatus());
                }
            }
            if (save) {
                if (logger.isDebugEnabled()) {
                    logger.debug("assignIssue: saving re-assigned issue");
                }
                getIssueDAO().saveOrUpdate(issue);
                notificationService.
                  sendNotification(
                    issue,
                    org.itracker.model.Notification.Type.ASSIGNED,
                    org.itracker.web.util.ServletContextUtils.
                        getItrackerServices().getConfigurationService().
                        getSystemBaseURL());
            }
        }
        return true;
    }
    
    /**
     * 
     * @param issue
     * @param unassignedByUser
     * @param save
     *            save issue and send notification
     * @return
     */
    private boolean unassignIssue(org.itracker.model.Issue issue,
                                  org.itracker.model.User unassignedByUser,
                                  boolean save) {
        if (logger.isDebugEnabled()) {
            logger.debug("unassignIssue: " + issue);
        }
        if (issue.getOwner() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("unassignIssue: unassigning from " +
                               issue.getOwner());
            }
            if (!notificationService.
                  hasIssueNotification(
                    issue, issue.getOwner().getId(),
                    org.itracker.model.Notification.Role.CONTRIBUTER)) {
                org.itracker.model.Notification notification =
                  new org.itracker.model.Notification(
                  issue.getOwner(), issue,
                  org.itracker.model.Notification.Role.CONTRIBUTER);
                if (save) {
                    notificationService.addIssueNotification(notification);
                } else {
                    issue.getNotifications().add(notification);
                }
            }
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity(
              issue, unassignedByUser,
              org.itracker.model.IssueActivityType.OWNER_CHANGE);
            activity.
              setDescription(
                issue.getOwner().
                    getLogin() +
                  " " +
                  org.itracker.core.resources.ITrackerResources.
                    getString("itracker.web.generic.to") +
                  " [" +
                  org.itracker.core.resources.ITrackerResources.
                    getString("itracker.web.generic.unassigned") + "]");
            issue.setOwner(null);
            if (issue.getStatus() >=
                  org.itracker.services.util.IssueUtilities.STATUS_ASSIGNED) {
                issue.
                  setStatus(
                    org.itracker.services.util.IssueUtilities.
                      STATUS_UNASSIGNED);
            }
            if (save) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unassignIssue: saving unassigned issue..");
                }
                getIssueDAO().saveOrUpdate(issue);
                notificationService.
                  sendNotification(
                    issue,
                    org.itracker.model.Notification.Type.ASSIGNED,
                    org.itracker.web.util.ServletContextUtils.
                        getItrackerServices().getConfigurationService().
                        getSystemBaseURL());
            }
        }
        return true;
    }
    
    /**
     * System-Update an issue, adds the action to the issue and updates the
     * issue
     */
    public org.itracker.model.
      Issue systemUpdateIssue(org.itracker.model.Issue updateissue,
                              java.lang.Integer userId)
          throws org.itracker.services.exceptions.ProjectException {
        org.itracker.model.IssueActivity activity =
          new org.itracker.model.IssueActivity();
        activity.setActivityType(
                   org.itracker.model.IssueActivityType.SYSTEM_UPDATE);
        activity.
          setDescription(
            org.itracker.core.resources.ITrackerResources.
                getString("itracker.activity.system.status"));
        java.util.ArrayList<org.itracker.model.IssueActivity> activities =
          new java.util.ArrayList<org.itracker.model.IssueActivity>();
        activity.setIssue(updateissue);
        activity.setUser(getUserDAO().findByPrimaryKey(userId));
        updateissue.getActivities().add(activity);
        org.itracker.model.Issue updated = updateIssue(updateissue, userId);
        updated.getActivities().addAll(activities);
        getIssueDAO().saveOrUpdate(updated);
        return updated;
    }
    
    /**
     * I think this entire method is useless - RJST TODO move to
     * {@link NotificationService}
     * 
     * @param model
     * @param issue
     * @param user
     * @return
     */
    public void updateIssueActivityNotification(java.lang.Integer issueId,
                                                boolean notificationSent) {
        if (issueId == null) { return; }
        java.util.Collection<org.itracker.model.IssueActivity> activity =
          getIssueActivityDAO().findByIssueId(issueId);
        java.util.Iterator<org.itracker.model.IssueActivity> iter =
          activity.iterator();
        while (iter.hasNext()) {
            ((org.itracker.model.IssueActivity) iter.next()).
              setNotificationSent(notificationSent);
        }
    }
    
    /**
     * Adds an attachment to an issue
     * 
     * @param model
     *            The attachment data
     * @param data
     *            The byte data
     */
    public boolean addIssueAttachment(org.itracker.model.
                                        IssueAttachment attachment,
                                      byte[] data) {
        org.itracker.model.Issue issue = attachment.getIssue();
        attachment.setFileName("attachment_issue_" + issue.getId() + "_" +
                                 attachment.getOriginalFileName());
        attachment.setFileData(data == null ? (new byte[0]) : data);
        if (logger.isDebugEnabled()) {
            logger.debug("addIssueAttachment: adding attachment " + attachment);
        }
        issue.getAttachments().add(attachment);
        if (logger.isDebugEnabled()) {
            logger.debug("addIssueAttachment: saving updated issue " + issue);
        }
        this.getIssueDAO().saveOrUpdate(issue);
        return true;
    }
    
    public boolean setIssueAttachmentData(java.lang.Integer attachmentId,
                                          byte[] data) {
        if (attachmentId != null && data != null) {
            org.itracker.model.IssueAttachment attachment =
              getIssueAttachmentDAO().findByPrimaryKey(attachmentId);
            attachment.setFileData(data);
            return true;
        }
        return false;
    }
    
    public boolean setIssueAttachmentData(java.lang.String fileName,
                                          byte[] data) {
        if (fileName != null && data != null) {
            org.itracker.model.IssueAttachment attachment =
              getIssueAttachmentDAO().findByFileName(fileName);
            attachment.setFileData(data);
            return true;
        }
        return false;
    }
    
    /**
     * Removes a attachement (deletes it)
     * 
     * @param attachmentId
     *            the id of the <code>IssueAttachmentBean</code>
     */
    public boolean removeIssueAttachment(java.lang.Integer attachmentId) {
        org.itracker.model.IssueAttachment attachementBean =
          this.getIssueAttachmentDAO().findByPrimaryKey(attachmentId);
        getIssueAttachmentDAO().delete(attachementBean);
        return true;
    }
    
    public java.lang.Integer removeIssueHistoryEntry(java.lang.Integer entryId,
                                                     java.lang.Integer userId) {
        org.itracker.model.IssueHistory history =
          getIssueHistoryDAO().findByPrimaryKey(entryId);
        if (history != null) {
            history.
              setStatus(
                org.itracker.services.util.IssueUtilities.
                  HISTORY_STATUS_REMOVED);
            org.itracker.model.IssueActivity activity =
              new org.itracker.model.IssueActivity();
            activity.setActivityType(
                       org.itracker.model.IssueActivityType.REMOVE_HISTORY);
            activity.
              setDescription(
                org.itracker.core.resources.ITrackerResources.
                    getString("itracker.web.generic.entry") +
                  " " +
                  entryId +
                  " " +
                  org.itracker.core.resources.ITrackerResources.
                    getString("itracker.web.generic.removed") + ".");
            getIssueHistoryDAO().delete(history);
            return history.getIssue().getId();
        }
        return java.lang.Integer.valueOf(-1);
    }
    
    public org.itracker.model.
      Project getIssueProject(java.lang.Integer issueId) {
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        org.itracker.model.Project project = issue.getProject();
        return project;
    }
    
    public java.util.HashSet<java.lang.
      Integer> getIssueComponentIds(java.lang.Integer issueId) {
        labeled_1 :
        {
            java.util.HashSet<java.lang.Integer> componentIds =
                    new java.util.HashSet<java.lang.Integer>();
            org.itracker.model.Issue issue =
                    getIssueDAO().findByPrimaryKey(issueId);
            java.util.Collection<org.itracker.model.Component> components =
                    issue.getComponents();
            java.util.Iterator<org.itracker.model.Component> iterator =
                    components.iterator();
            while (iterator.hasNext())
            {
                componentIds.add(((org.itracker.model.Component)
                        iterator.next()).getId());
            }
        }
        return componentIds;
    }
    
    public java.util.HashSet<java.lang.
      Integer> getIssueVersionIds(java.lang.Integer issueId) {
        labeled_2 :
        {
            java.util.HashSet<java.lang.Integer> versionIds =
                    new java.util.HashSet<java.lang.Integer>();
            org.itracker.model.Issue issue =
                    getIssueDAO().findByPrimaryKey(issueId);
            java.util.Collection<org.itracker.model.Version> versions =
                    issue.getVersions();
            java.util.Iterator<org.itracker.model.Version> iterator =
                    versions.iterator();
            while (iterator.hasNext())
            {
                versionIds.add(((org.itracker.model.Version)
                        iterator.next()).getId());
            }
        }
        return versionIds;
    }
    
    public java.util.List<org.itracker.model.
      IssueActivity> getIssueActivity(java.lang.Integer issueId) {
        int i = 0;
        java.util.Collection<org.itracker.model.IssueActivity> activity =
          getIssueActivityDAO().findByIssueId(issueId);
        org.itracker.model.IssueActivity[] activityArray =
          new org.itracker.model.IssueActivity[activity.size()];
        java.util.Iterator<org.itracker.model.IssueActivity> iterator =
          activity.iterator();
        while (iterator.hasNext()) {
            activityArray[i] = (org.itracker.model.IssueActivity)
                                 iterator.next();
            i++;
        }
        return java.util.Arrays.asList(activityArray);
    }
    
    /**
     * TODO move to {@link NotificationService} ?
     */
    public java.util.List<org.itracker.model.
      IssueActivity> getIssueActivity(java.lang.Integer issueId,
                                      boolean notificationSent) {
        labeled_3 :
        {
            int i = 0;
            java.util.Collection<org.itracker.model.IssueActivity> activity =
                    getIssueActivityDAO().findByIssueIdAndNotification(issueId,
                            notificationSent);
            org.itracker.model.IssueActivity[] activityArray =
                    new org.itracker.model.IssueActivity[activity.size()];
            java.util.Iterator<org.itracker.model.IssueActivity> iterator =
                    activity.iterator();
            while (iterator.hasNext())
            {
                activityArray[i] = (org.itracker.model.IssueActivity)
                        iterator.next();
                i++;
            }
        }
        return java.util.Arrays.asList(activityArray);
    }
    
    public java.lang.Long getAllIssueAttachmentCount() {
        return getIssueAttachmentDAO().countAll().longValue();
    }
    
    /**
     * @deprecated do not use this due to expensive memory use! use explicit
     *             hsqldb queries instead.
     */
    public java.util.List<org.itracker.model.
      IssueAttachment> getAllIssueAttachments() {
        logger.warn("getAllIssueAttachments: use of deprecated API");
        if (logger.isDebugEnabled()) {
            logger.debug("getAllIssueAttachments: stacktrace was",
                         new java.lang.RuntimeException());
        }
        java.util.List<org.itracker.model.IssueAttachment> attachments =
          getIssueAttachmentDAO().findAll();
        return attachments;
    }
    
    public org.itracker.model.
      IssueAttachment getIssueAttachment(java.lang.Integer attachmentId) {
        org.itracker.model.IssueAttachment attachment =
          getIssueAttachmentDAO().findByPrimaryKey(attachmentId);
        return attachment;
    }
    
    public byte[] getIssueAttachmentData(java.lang.Integer attachmentId) {
        byte[] data;
        org.itracker.model.IssueAttachment attachment =
          getIssueAttachmentDAO().findByPrimaryKey(attachmentId);
        data = attachment.getFileData();
        return data;
    }
    
    public int getIssueAttachmentCount(java.lang.Integer issueId) {
        int i = 0;
        org.itracker.model.Issue issue =
          getIssueDAO().findByPrimaryKey(issueId);
        java.util.Collection<org.itracker.model.IssueAttachment> attachments =
          issue.getAttachments();
        i = attachments.size();
        return i;
    }
    
    /**
     * 
     * Returns the latest issue history entry for a particular issue.
     * 
     * 
     * 
     * @param issueId
     * 
     *            the id of the issue to return the history entry for.
     * 
     * @return the latest IssueHistory, or null if no entries could be found
     */
    public org.itracker.model.
      IssueHistory getLastIssueHistory(java.lang.Integer issueId) {
        labeled_4 :
        {
            java.util.List<org.itracker.model.IssueHistory> history =
                    getIssueHistoryDAO().findByIssueId(issueId);
            if (null != history && history.size() > 0)
            {
                java.util.Collections.
                        sort(history, org.itracker.model.AbstractEntity.ID_COMPARATOR);
                return history.get(history.size() - 1);
            }
        }
        return null;
    }
    
    public int getOpenIssueCountByProjectId(java.lang.Integer projectId) {
        labeled_5 :
        {
            java.util.Collection<org.itracker.model.Issue> issues =
                    getIssueDAO().
                            findByProjectAndLowerStatus(
                                    projectId,
                                    org.itracker.services.util.IssueUtilities.STATUS_RESOLVED);
            int size = issues.size();
        }
        return size;
    }
    
    public int getResolvedIssueCountByProjectId(java.lang.Integer projectId) {
        labeled_6 : {
            java.util.Collection<org.itracker.model.Issue> issues =
                    getIssueDAO().
                            findByProjectAndHigherStatus(
                                    projectId,
                                    org.itracker.services.util.IssueUtilities.STATUS_RESOLVED);
            int size = issues.size();
        }
        return size;
    }
    
    public int getTotalIssueCountByProjectId(java.lang.Integer projectId) {
        labeled_7 :
        {
            java.util.Collection<org.itracker.model.Issue> issues =
                    getIssueDAO().findByProject(projectId);
            int size = issues.size();
        }
        return size;
    }
    
    public java.util.
      Date getLatestIssueDateByProjectId(java.lang.Integer projectId) {
        return getIssueDAO().latestModificationDate(projectId);
    }
    
    public boolean canViewIssue(java.lang.Integer issueId,
                                org.itracker.model.User user) {
        org.itracker.model.Issue issue = getIssue(issueId);
        java.util.Map<java.lang.Integer,
        java.util.Set<org.itracker.model.PermissionType>> permissions =
          getUserDAO().getUsersMapOfProjectsAndPermissionTypes(user);
        return org.itracker.services.util.IssueUtilities.canViewIssue(
                                                           issue, user.getId(),
                                                           permissions);
    }
    
    public boolean canViewIssue(org.itracker.model.Issue issue,
                                org.itracker.model.User user) {
        java.util.Map<java.lang.Integer,
        java.util.Set<org.itracker.model.PermissionType>> permissions =
          getUserDAO().getUsersMapOfProjectsAndPermissionTypes(user);
        return org.itracker.services.util.IssueUtilities.canViewIssue(
                                                           issue, user.getId(),
                                                           permissions);
    }
    
    private org.itracker.persistence.dao.UserDAO getUserDAO() {
        return userDAO;
    }
    
    private org.itracker.persistence.dao.IssueDAO getIssueDAO() {
        return issueDAO;
    }
    
    private org.itracker.persistence.dao.ProjectDAO getProjectDAO() {
        return projectDAO;
    }
    
    private org.itracker.persistence.dao.
      IssueActivityDAO getIssueActivityDAO() { return issueActivityDAO; }
    
    private org.itracker.persistence.dao.VersionDAO getVersionDAO() {
        return this.versionDAO;
    }
    
    private org.itracker.persistence.dao.ComponentDAO getComponentDAO() {
        return this.componentDAO;
    }
    
    private org.itracker.persistence.dao.CustomFieldDAO getCustomFieldDAO() {
        return customFieldDAO;
    }
    
    private org.itracker.persistence.dao.IssueHistoryDAO getIssueHistoryDAO() {
        return issueHistoryDAO;
    }
    
    private org.itracker.persistence.dao.
      IssueRelationDAO getIssueRelationDAO() { return issueRelationDAO; }
    
    private org.itracker.persistence.dao.
      IssueAttachmentDAO getIssueAttachmentDAO() { return issueAttachmentDAO; }
    
    /**
     * get total size of all attachments in database
     */
    public java.lang.Long getAllIssueAttachmentSize() {
        return getIssueAttachmentDAO().totalAttachmentsSize().longValue() /
          1024;
    }
    
    public java.
      util.
      List<org.
      itracker.
      model.
      Issue> searchIssues(org.itracker.model.IssueSearchQuery queryModel,
                          org.itracker.model.User user,
                          java.util.Map<java.lang.Integer,
                          java.util.Set<org.itracker.model.
                            PermissionType>> userPermissions)
          throws org.itracker.services.exceptions.IssueSearchException {
        return getIssueDAO().query(queryModel, user, userPermissions);
    }
    
    public java.lang.Long totalSystemIssuesAttachmentSize() {
        return getIssueAttachmentDAO().totalAttachmentsSize();
    }
}

