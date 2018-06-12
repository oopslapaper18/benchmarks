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

package org.itracker.services.implementations;

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
 * Issue related service layer. A bit "fat" at this time, because of being a
 * direct EJB porting. Going go get thinner over time
 * 
 * @author ricardo
 * 
 */

//TODO: Cleanup this file, go through all issues, todos, etc.

public class IssueServiceImpl implements IssueService {

	private static final Logger logger = null; //Logger
	//.getLogger(IssueServiceImpl.class);

	private CustomFieldDAO customFieldDAO;

	private UserDAO userDAO;

	private ProjectDAO projectDAO;

	private IssueDAO issueDAO;

	private IssueHistoryDAO issueHistoryDAO;

	private IssueRelationDAO issueRelationDAO;

	private IssueAttachmentDAO issueAttachmentDAO;

	private ComponentDAO componentDAO;

	private IssueActivityDAO issueActivityDAO;

	private VersionDAO versionDAO;

	private NotificationService notificationService;

	public IssueServiceImpl(UserDAO userDAO, ProjectDAO projectDAO,
			IssueDAO issueDAO, IssueHistoryDAO issueHistoryDAO,
			IssueRelationDAO issueRelationDAO,
			IssueAttachmentDAO issueAttachmentDAO, ComponentDAO componentDAO,
			IssueActivityDAO issueActivityDAO, VersionDAO versionDAO,
			CustomFieldDAO customFieldDAO,
			NotificationService notificationService) {

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

	public Issue getIssue(Integer issueId) {
		Issue issue = getIssueDAO().findByPrimaryKey(issueId);
		return issue;
	}

	/**
	 * @deprecated don't use to expensive memory use!
	 */
	public List<Issue> getAllIssues() {
		logger.warn("getAllIssues: use of deprecated API");
		if (logger.isDebugEnabled()) {
			logger
			.debug("getAllIssues: stacktrace was",
					new RuntimeException());
		}
		return getIssueDAO().findAll();
	}

	/**
	 * Added implementation to make proper count of ALL issues, instead select
	 * them in a list and return its size
	 */
	public Long getNumberIssues() {
		return getIssueDAO().countAllIssues();
	}

	public List<Issue> getIssuesCreatedByUser(Integer userId) {
		return getIssuesCreatedByUser(userId, true);
	}

	public List<Issue> getIssuesCreatedByUser(Integer userId,
			boolean availableProjectsOnly) {
		final List<Issue> issues;

		if (availableProjectsOnly) {
			issues = getIssueDAO().findByCreatorInAvailableProjects(userId,
					IssueUtilities.STATUS_CLOSED);
		} else {
			issues = getIssueDAO().findByCreator(userId,
					IssueUtilities.STATUS_CLOSED);
		}
		return issues;
	}

	public List<Issue> getIssuesOwnedByUser(Integer userId) {

		return getIssuesOwnedByUser(userId, true);

	}

	public List<Issue> getIssuesOwnedByUser(Integer userId,
			boolean availableProjectsOnly) {
		final List<Issue> issues;

		if (availableProjectsOnly) {
			issues = getIssueDAO().findByOwnerInAvailableProjects(userId,
					IssueUtilities.STATUS_RESOLVED);
		} else {
			issues = getIssueDAO().findByOwner(userId,
					IssueUtilities.STATUS_RESOLVED);
		}
		return issues;
	}

	public List<Issue> getIssuesWatchedByUser(Integer userId) {
		return getIssuesWatchedByUser(userId, true);
	}

	/**
	 * TODO move to {@link NotificationService}
	 */
	public List<Issue> getIssuesWatchedByUser(Integer userId,
			boolean availableProjectsOnly) {
		final List<Issue> issues;

		if (availableProjectsOnly) {
			issues = getIssueDAO().findByNotificationInAvailableProjects(
					userId, IssueUtilities.STATUS_CLOSED);
		} else {
			issues = getIssueDAO().findByNotification(userId,
					IssueUtilities.STATUS_CLOSED);
		}
		return issues;
	}

	public List<Issue> getUnassignedIssues() {
		return getUnassignedIssues(true);
	}

	public List<Issue> getUnassignedIssues(boolean availableProjectsOnly) {
		final List<Issue> issues;

		if (availableProjectsOnly) {
			issues = getIssueDAO()
			.findByStatusLessThanEqualToInAvailableProjects(
					IssueUtilities.STATUS_UNASSIGNED);
		} else {
			issues = getIssueDAO().findByStatusLessThanEqualTo(
					IssueUtilities.STATUS_UNASSIGNED);
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

	public List<Issue> getIssuesWithStatus(int status) {
		List<Issue> issues = getIssueDAO().findByStatus(status);
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

	public List<Issue> getIssuesWithStatusLessThan(int status) {
		List<Issue> issues = getIssueDAO().findByStatusLessThan(status);
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

	public List<Issue> getIssuesWithSeverity(int severity) {
		List<Issue> issues = getIssueDAO().findBySeverity(severity);
		return issues;

	}

	public List<Issue> getIssuesByProjectId(Integer projectId) {
		return getIssuesByProjectId(projectId, IssueUtilities.STATUS_END);
	}

	public List<Issue> getIssuesByProjectId(Integer projectId, int status) {
		List<Issue> issues = getIssueDAO().findByProjectAndLowerStatus(
				projectId, status);
		return issues;
	}

	public User getIssueCreator(Integer issueId) {
		Issue issue = getIssueDAO().findByPrimaryKey(issueId);
		User user = issue.getCreator();
		return user;

	}

	public User getIssueOwner(Integer issueId) {
		Issue issue = getIssueDAO().findByPrimaryKey(issueId);
		User user = issue.getOwner();

		return user;

	}

	public List<Component> getIssueComponents(Integer issueId) {
		Issue issue = getIssueDAO().findByPrimaryKey(issueId);
		List<Component> components = issue.getComponents();

		return components;
	}

	public List<Version> getIssueVersions(Integer issueId) {
		Issue issue = getIssueDAO().findByPrimaryKey(issueId);

		List<Version> versions = issue.getVersions();
		return versions;
	}

	public List<IssueAttachment> getIssueAttachments(Integer issueId) {
		Issue issue = getIssueDAO().findByPrimaryKey(issueId);

		List<IssueAttachment> attachments = issue.getAttachments();
		return attachments;
	}

	/**
	 * Old implementation is left here, commented, because it checked for
	 * history entry status. This feature was not finished, I think (RJST)
	 */
	public List<IssueHistory> getIssueHistory(Integer issueId) {
		return getIssueDAO().findByPrimaryKey(issueId).getHistory();
	}

	public Issue createIssue(Issue issue, Integer projectId, Integer userId,
			Integer createdById) throws ProjectException {
		Project project = getProjectDAO().findByPrimaryKey(projectId);
		User creator = getUserDAO().findByPrimaryKey(userId);

		if (project.getStatus() != Status.ACTIVE) {
			throw new ProjectException("Project is not active.");
		}

		IssueActivity activity = new IssueActivity(issue, creator,
				IssueActivityType.ISSUE_CREATED);
		activity.setDescription(ITrackerResources
				.getString("itracker.activity.system.createdfor")
				+ " " + creator.getFirstName() + " " + creator.getLastName());

		activity.setIssue(issue);

		if (!(createdById == null || createdById.equals(userId))) {

			User createdBy = getUserDAO().findByPrimaryKey(createdById);
			activity.setUser(createdBy);

			Notification watchModel = new Notification();

			watchModel.setUser(createdBy);

			watchModel.setIssue(issue);

			watchModel.setRole(Notification.Role.CONTRIBUTER);

			issue.getNotifications().add(watchModel);
			
//			Does save issue after saving notificaton ->  not-null property references a null or transient value: org.itracker.model.Notification.issue
//			Will be saved by cascade when saving issue
//			notificationService.addIssueNotification(watchModel);

		}

		List<IssueActivity> activities = new ArrayList<IssueActivity>();
		activities.add(activity);
		issue.setActivities(activities);

		issue.setProject(project);

		issue.setCreator(creator);

		// save
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
	public Issue updateIssue(final Issue issueDirty, final Integer userId)
	throws ProjectException {

		String existingTargetVersion = null;

		// detach the modified Issue form the Hibernate Session
		getIssueDAO().detach(issueDirty);
		// Retrieve the Issue from Hibernate Session and refresh it from
		// Hibernate Session to previous state.
		Issue persistedIssue = getIssueDAO().findByPrimaryKey(
				issueDirty.getId());

		getIssueDAO().refresh(persistedIssue);
		if (logger.isDebugEnabled()) {
			logger.debug("updateIssue: updating issue " + issueDirty
					+ "\n(from " + persistedIssue + ")");
		}

		User user = getUserDAO().findByPrimaryKey(userId);

		if (persistedIssue.getProject().getStatus() != Status.ACTIVE) {
			throw new ProjectException("Project "
					+ persistedIssue.getProject().getName() + " is not active.");
		}

		if (!persistedIssue.getDescription().equalsIgnoreCase(
				issueDirty.getDescription())) {

			if (logger.isDebugEnabled()) {
				logger.debug("updateIssue: updating description from "
						+ persistedIssue.getDescription());
			}
			IssueActivity activity = new IssueActivity();
			activity.setActivityType(IssueActivityType.DESCRIPTION_CHANGE);
			activity.setDescription(ITrackerResources
					.getString("itracker.web.generic.from")
					+ ": " + persistedIssue.getDescription());
			activity.setUser(user);
			activity.setIssue(issueDirty);
			issueDirty.getActivities().add(activity);

		}

		if (persistedIssue.getResolution() != null
				&& !persistedIssue.getResolution().equalsIgnoreCase(
						issueDirty.getResolution())) {

			IssueActivity activity = new IssueActivity();
			activity.setActivityType(IssueActivityType.RESOLUTION_CHANGE);
			activity.setDescription(ITrackerResources
					.getString("itracker.web.generic.from")
					+ ": " + persistedIssue.getResolution());
			activity.setUser(user);
			activity.setIssue(issueDirty);
			issueDirty.getActivities().add(activity);
		}

		if (null == persistedIssue.getStatus()
				|| !persistedIssue.getStatus().equals(issueDirty.getStatus())) {
			IssueActivity activity = new IssueActivity();
			activity.setActivityType(IssueActivityType.STATUS_CHANGE);
			activity.setDescription(IssueUtilities.getStatusName(persistedIssue
					.getStatus())
					+ " "
					+ ITrackerResources.getString("itracker.web.generic.to")
					+ " "
					+ IssueUtilities.getStatusName(issueDirty.getStatus()));
			activity.setUser(user);
			activity.setIssue(issueDirty);
			issueDirty.getActivities().add(activity);
		}

		if (issueDirty.getSeverity() != null
				&& !issueDirty.getSeverity().equals(
						persistedIssue.getSeverity())
						&& issueDirty.getSeverity() != -1) {

			IssueActivity activity = new IssueActivity();
			activity.setActivityType(IssueActivityType.SEVERITY_CHANGE);
			// FIXME why does it state Critical to Critical when it should Major to Critical!?
			activity.setDescription(IssueUtilities
					.getSeverityName(persistedIssue.getSeverity())
					+ " "
					+ ITrackerResources.getString("itracker.web.generic.to")
					+ " "
					+ IssueUtilities.getSeverityName(issueDirty.getSeverity()));

			activity.setUser(user);
			activity.setIssue(issueDirty);
			issueDirty.getActivities().add(activity);
		}

		if (persistedIssue.getTargetVersion() != null
				&& issueDirty.getTargetVersion() != null
				&& !persistedIssue.getTargetVersion().getId().equals(
						issueDirty.getTargetVersion().getId())) {
			existingTargetVersion = persistedIssue.getTargetVersion()
			.getNumber();
			Version version = this.getVersionDAO().findByPrimaryKey(
					issueDirty.getTargetVersion().getId());

			IssueActivity activity = new IssueActivity();
			activity.setActivityType(IssueActivityType.TARGETVERSION_CHANGE);
			String description = existingTargetVersion + " "
			+ ITrackerResources.getString("itracker.web.generic.to")
			+ " ";
			description += version.getNumber();
			activity.setDescription(description);
			activity.setUser(user);
			activity.setIssue(issueDirty);
			issueDirty.getActivities().add(activity);
		}

		// (re-)assign issue
		User newOwner = issueDirty.getOwner();
		issueDirty.setOwner(persistedIssue.getOwner());
		if (logger.isDebugEnabled()) {
			logger.debug("updateIssue: assigning from " + issueDirty.getOwner()
					+ " to " + newOwner);
		}
		assignIssue(issueDirty, newOwner, user, false);
		if (logger.isDebugEnabled()) {
			logger.debug("updateIssue: updated assignment: " + issueDirty);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("updateIssue: merging issue " + issueDirty + " to "
					+ persistedIssue);
		}

		persistedIssue = getIssueDAO().merge(issueDirty);

		if (logger.isDebugEnabled()) {
			logger.debug("updateIssue: merged issue for saving: "
					+ persistedIssue);
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

	public Issue moveIssue(Issue issue, Integer projectId, Integer userId) {

		if (logger.isDebugEnabled()) {
			logger.debug("moveIssue: " + issue + " to project#" + projectId
					+ ", user#" + userId);
		}

		Project project = getProjectDAO().findByPrimaryKey(projectId);
		User user = getUserDAO().findByPrimaryKey(userId);

		if (logger.isDebugEnabled()) {
			logger.debug("moveIssue: " + issue + " to project: " + project
					+ ", user: " + user);
		}

		IssueActivity activity = new IssueActivity();
		activity
		.setActivityType(org.itracker.model.IssueActivityType.ISSUE_MOVE);
		activity.setDescription(issue.getProject().getName() + " "
				+ ITrackerResources.getString("itracker.web.generic.to") + " "
				+ project.getName());
		activity.setUser(user);
		activity.setIssue(issue);
		issue.setProject(project);

		issue.getActivities().add(activity);

		if (logger.isDebugEnabled()) {
			logger.debug("moveIssue: updated issue: " + issue);
		}
		try {
			getIssueDAO().saveOrUpdate(issue);
		} catch (Exception e) {
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
	public boolean addIssueHistory(IssueHistory history) {
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
	public boolean setIssueFields(Integer issueId, List<IssueField> fields) {
		Issue issue = getIssueDAO().findByPrimaryKey(issueId);

		setIssueFields(issue, fields, true);

		return true;
	}

	private boolean setIssueFields(Issue issue, List<IssueField> fields,
			boolean save) {

		List<IssueField> issueFields = issue.getFields();

		if (fields.size() > 0) {
			for (int i = 0; i < fields.size(); i++) {

				IssueField field = fields.get(i);
				if (issueFields.contains(field)) {
					issueFields.remove(field);
				}

				CustomField customField = getCustomFieldDAO().findByPrimaryKey(
						fields.get(i).getCustomField().getId());
				field.setCustomField(customField);
				field.setIssue(issue);

				// what date value?
				// field.setDateValue(new Timestamp(new Date().getTime()));
				issueFields.add(field);
			}
		}
		issue.setFields(issueFields);

		if (save) {
			logger.debug("setIssueFields: save was true");
			getIssueDAO().saveOrUpdate(issue);
		}
		return true;
	}

	public boolean setIssueComponents(Integer issueId,
			HashSet<Integer> componentIds, Integer userId) {

		Issue issue = getIssueDAO().findByPrimaryKey(issueId);
		List<Component> components = new ArrayList<Component>(componentIds
				.size());
		User user = userDAO.findByPrimaryKey(userId);
		Iterator<Integer> idIt = componentIds.iterator();
		while (idIt.hasNext()) {
			Integer id = (Integer) idIt.next();
			Component c = getComponentDAO().findById(id);
			components.add(c);
		}

		setIssueComponents(issue, components, user, true);
		return true;
	}

	private boolean setIssueComponents(Issue issue, List<Component> components,
			User user, boolean save) {

		if (issue.getComponents() == null) {
			if (logger.isInfoEnabled()) {
				logger.info("setIssueComponents: components was null");
			}
			issue.setComponents(new ArrayList<Component>(components.size()));
		}
		if (components.isEmpty() && !issue.getComponents().isEmpty()) {
			addComponentsModifiedActivity(issue, user, new StringBuilder(
					ITrackerResources.getString("itracker.web.generic.all"))
			.append(" ").append(
					ITrackerResources
					.getString("itracker.web.generic.removed"))
					.toString());
			issue.getComponents().clear();
		} else {
			Collections.sort(issue.getComponents(), Component.NAME_COMPARATOR);

			for (Iterator<Component> iterator = issue.getComponents()
					.iterator(); iterator.hasNext();) {
				Component component = (Component) iterator.next();
				if (components.contains(component)) {
					components.remove(component);
				} else {
					addComponentsModifiedActivity(issue, user,
							new StringBuilder(ITrackerResources
									.getString("itracker.web.generic.removed"))
					.append(": ").append(component.getName())
					.toString());
					iterator.remove();
				}
			}
			Collections.sort(components, Component.NAME_COMPARATOR);
			for (Iterator<Component> iterator = components.iterator(); iterator
			.hasNext();) {

				Component component = iterator.next();
				if (!issue.getComponents().contains(component)) {
					addComponentsModifiedActivity(issue, user,
							new StringBuilder(ITrackerResources
									.getString("itracker.web.generic.added"))
					.append(": ").append(component.getName())
					.toString());
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
	private void addComponentsModifiedActivity(Issue issue, User user,
			String description) {
		IssueActivity activity = new IssueActivity();
		activity
		.setActivityType(org.itracker.model.IssueActivityType.COMPONENTS_MODIFIED);
		activity.setDescription(description);
		activity.setIssue(issue);
		activity.setUser(user);
		issue.getActivities().add(activity);
	}

	private boolean setIssueVersions(Issue issue, List<Version> versions,
			User user, boolean save) {

		if (issue.getVersions() == null) {
			if (logger.isInfoEnabled()) {
				logger.info("setIssueVersions: versions were null!");
			}
			issue.setVersions(new ArrayList<Version>());
		}

		if (versions.isEmpty() && !issue.getVersions().isEmpty()) {

			addVersionsModifiedActivity(issue, user, new StringBuilder(
					ITrackerResources.getString("itracker.web.generic.all"))
			.append(" ").append(
					ITrackerResources
					.getString("itracker.web.generic.removed"))
					.toString());
			issue.getVersions().clear();
		} else {

			Collections.sort(issue.getVersions(), Version.VERSION_COMPARATOR);

			StringBuilder changesBuf = new StringBuilder();
			for (Iterator<Version> iterator = issue.getVersions().iterator(); iterator
			.hasNext();) {

				Version version = iterator.next();
				if (versions.contains(version)) {
					versions.remove(version);
				} else {
					if (changesBuf.length() > 0) {
						changesBuf.append(", ");
					}
					changesBuf.append(version.getNumber());
					iterator.remove();
				}
			}

			if (changesBuf.length() > 0) {
				addVersionsModifiedActivity(issue, user, new StringBuilder(
						ITrackerResources
						.getString("itracker.web.generic.removed"))
				.append(": ").append(changesBuf).toString());
			}

			changesBuf = new StringBuilder();

			Collections.sort(versions, Version.VERSION_COMPARATOR);
			for (Iterator<Version> iterator = versions.iterator(); iterator
			.hasNext();) {

				Version version = iterator.next();
				if (changesBuf.length() > 0) {
					changesBuf.append(", ");
				}
				changesBuf.append(version.getNumber());
				issue.getVersions().add(version);
			}
			if (changesBuf.length() > 0) {
				addVersionsModifiedActivity(issue, user, new StringBuilder(
						ITrackerResources
						.getString("itracker.web.generic.added"))
				.append(": ").append(changesBuf).toString());
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
	private void addVersionsModifiedActivity(Issue issue, User user,
			String description) {
		IssueActivity activity = new IssueActivity();
		activity
		.setActivityType(org.itracker.model.IssueActivityType.TARGETVERSION_CHANGE);
		activity.setDescription(description);
		activity.setIssue(issue);
		activity.setUser(user);
		issue.getActivities().add(activity);
	}

	public boolean setIssueVersions(Integer issueId,
			HashSet<Integer> versionIds, Integer userId) {

		Issue issue = getIssueDAO().findByPrimaryKey(issueId);
		User user = userDAO.findByPrimaryKey(userId);
		// load versions from ids
		ArrayList<Version> versions = new ArrayList<Version>(versionIds.size());
		Iterator<Integer> versionsIdIt = versionIds.iterator();
		while (versionsIdIt.hasNext()) {
			Integer id = versionsIdIt.next();
			versions.add(getVersionDAO().findByPrimaryKey(id));
		}

		return setIssueVersions(issue, versions, user, true);
	}

	public IssueRelation getIssueRelation(Integer relationId) {

		IssueRelation issueRelation = getIssueRelationDAO().findByPrimaryKey(
				relationId);

		return issueRelation;

	}

	/**
	 * add a relation between two issues.
	 * 
	 * TODO: There is no relation saved to database yet?
	 */
	public boolean addIssueRelation(Integer issueId, Integer relatedIssueId,
			int relationType, Integer userId) {

		User user = getUserDAO().findByPrimaryKey(userId);

		if (null == user) {
			throw new IllegalArgumentException("Invalid user-id: " + userId);
		}

		if (issueId != null && relatedIssueId != null) {

			int matchingRelationType = IssueUtilities
			.getMatchingRelationType(relationType);

			// if(matchingRelationType < 0) {

			// throw new CreateException("Unable to find matching relation type

			// for type: " + relationType);

			// }

			Issue issue = getIssueDAO().findByPrimaryKey(issueId);

			Issue relatedIssue = getIssueDAO().findByPrimaryKey(relatedIssueId);

			IssueRelation relationA = new IssueRelation();

			relationA.setRelationType(relationType);

			// relationA.setMatchingRelationId(relationBId);

			relationA.setIssue(issue);

			relationA.setRelatedIssue(relatedIssue);

			// set to 0 first, later reassign to relationB.id
			relationA.setMatchingRelationId(0);

			relationA.setLastModifiedDate(new java.sql.Timestamp(new Date()
			.getTime()));

			getIssueRelationDAO().saveOrUpdate(relationA);

			IssueRelation relationB = new IssueRelation();

			relationB.setRelationType(matchingRelationType);

			// relationB.setMatchingRelationId(relationAId);

			relationB.setIssue(relatedIssue);

			relationB.setRelatedIssue(issue);

			relationB.setMatchingRelationId(relationA.getId());

			relationB.setLastModifiedDate(new java.sql.Timestamp(new Date()
			.getTime()));

			getIssueRelationDAO().saveOrUpdate(relationB);

			relationA.setMatchingRelationId(relationB.getId());
			getIssueRelationDAO().saveOrUpdate(relationA);

			IssueActivity activity = new IssueActivity();
			activity
			.setActivityType(org.itracker.model.IssueActivityType.RELATION_ADDED);
			activity.setDescription(ITrackerResources.getString(
					"itracker.activity.relation.add", new Object[] {
							IssueUtilities.getRelationName(relationType),
							relatedIssueId }));

			activity.setIssue(issue);
			issue.getActivities().add(activity);
			// need to set user here
			activity.setUser(user);
			// need to save here
			getIssueDAO().saveOrUpdate(issue);

			activity = new IssueActivity();
			activity
			.setActivityType(org.itracker.model.IssueActivityType.RELATION_ADDED);
			activity.setDescription(ITrackerResources.getString(
					"itracker.activity.relation.add", new Object[] {
							IssueUtilities
							.getRelationName(matchingRelationType),
							issueId }));
			activity.setIssue(relatedIssue);
			activity.setUser(user);
			relatedIssue.getActivities().add(activity);
			getIssueDAO().saveOrUpdate(relatedIssue);
			return true;

		}

		return false;

	}

	public void removeIssueRelation(Integer relationId, Integer userId) {
		IssueRelation issueRelation = getIssueRelationDAO().findByPrimaryKey(
				relationId);
		Integer issueId = issueRelation.getIssue().getId();

		Integer relatedIssueId = issueRelation.getRelatedIssue().getId();

		Integer matchingRelationId = issueRelation.getMatchingRelationId();

		if (matchingRelationId != null) {
			IssueActivity activity = new IssueActivity();
			activity
			.setActivityType(org.itracker.model.IssueActivityType.RELATION_REMOVED);
			activity.setDescription(ITrackerResources.getString(
					"itracker.activity.relation.removed", issueId.toString()));
			// FIXME need to fix the commented code and save
			// activity.setIssue(relatedIssueId);
			// activity.setUser(userId);
			// IssueRelationDAO.remove(matchingRelationId);
		}

		IssueActivity activity = new IssueActivity();
		activity
		.setActivityType(org.itracker.model.IssueActivityType.RELATION_REMOVED);
		activity.setDescription(ITrackerResources
				.getString("itracker.activity.relation.removed", relatedIssueId
						.toString()));
		// activity.setIssue(issueId);
		// activity.setUser(userId);
		// irHome.remove(relationId);
		// need to save

		getIssueRelationDAO().delete(issueRelation);
	}

	public boolean assignIssue(Integer issueId, Integer userId) {
		return assignIssue(issueId, userId, userId);
	}

	/**
	 * only use for updating issue from actions..
	 */
	public boolean assignIssue(Integer issueId, Integer userId,
			Integer assignedByUserId) {

		return assignIssue(getIssueDAO().findByPrimaryKey(issueId),
				getUserDAO().findByPrimaryKey(userId), getUserDAO()
				.findByPrimaryKey(assignedByUserId), true);
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
	private boolean assignIssue(Issue issue, User user, User assignedByUser,
			final boolean save) {

		if (issue.getOwner() == user
				|| (null != issue.getOwner() && issue.getOwner().equals(user))) {
			// nothing to do.
			if (logger.isDebugEnabled()) {
				logger.debug("assignIssue: attempted to reassign " + issue
						+ " to current owner " + user);
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

		User currOwner = issue.getOwner();

		if (!user.equals(currOwner)) {
			if (currOwner != null
					&& !notificationService.hasIssueNotification(issue,
							currOwner.getId(), Role.IP)) {
				// Notification notification = new Notification();
				Notification notification = new Notification(currOwner, issue,
						Role.IP);
				if (save) {
					notificationService.addIssueNotification(notification);
				} else {
					issue.getNotifications().add(notification);
				}
			}

			IssueActivity activity = new IssueActivity();
			activity
			.setActivityType(org.itracker.model.IssueActivityType.OWNER_CHANGE);
			activity.setDescription((currOwner == null ? "["
					+ ITrackerResources
					.getString("itracker.web.generic.unassigned") + "]"
					: currOwner.getLogin())
					+ " "
					+ ITrackerResources.getString("itracker.web.generic.to")
					+ " " + user.getLogin());
			activity.setUser(assignedByUser);
			activity.setIssue(issue);
			issue.getActivities().add(activity);

			issue.setOwner(user);

			if (logger.isDebugEnabled()) {
				logger.debug("assignIssue: current status: "
						+ issue.getStatus());
			}
			if (issue.getStatus() < IssueUtilities.STATUS_ASSIGNED) {
				issue.setStatus(IssueUtilities.STATUS_ASSIGNED);
				if (logger.isDebugEnabled()) {
					logger.debug("assignIssue: new status set to "
							+ issue.getStatus());
				}
			}

			// send assignment notification
			// TODO: configurationService should be set from context
			if (save) {
				if (logger.isDebugEnabled()) {
					logger.debug("assignIssue: saving re-assigned issue");
				}
				getIssueDAO().saveOrUpdate(issue);
				notificationService.sendNotification(issue, Type.ASSIGNED,
						ServletContextUtils.getItrackerServices()
						.getConfigurationService().getSystemBaseURL());

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
	private boolean unassignIssue(Issue issue, User unassignedByUser,
			boolean save) {
		if (logger.isDebugEnabled()) {
			logger.debug("unassignIssue: " + issue);
		}
		if (issue.getOwner() != null) {

			if (logger.isDebugEnabled()) {
				logger.debug("unassignIssue: unassigning from "
						+ issue.getOwner());
			}
			if (!notificationService.hasIssueNotification(issue, issue
					.getOwner().getId(), Role.CONTRIBUTER)) {
				// Notification notification = new Notification();
				Notification notification = new Notification(issue.getOwner(),
						issue, Role.CONTRIBUTER);
				if (save) {
					notificationService.addIssueNotification(notification);
				} else {
					issue.getNotifications().add(notification);
				}
			}
			IssueActivity activity = new IssueActivity(issue, unassignedByUser,
					IssueActivityType.OWNER_CHANGE);
			activity
			.setDescription(issue.getOwner().getLogin()
					+ " "
					+ ITrackerResources
					.getString("itracker.web.generic.to")
					+ " ["
					+ ITrackerResources
					.getString("itracker.web.generic.unassigned")
					+ "]");

			issue.setOwner(null);

			if (issue.getStatus() >= IssueUtilities.STATUS_ASSIGNED) {
				issue.setStatus(IssueUtilities.STATUS_UNASSIGNED);
			}
			if (save) {
				if (logger.isDebugEnabled()) {
					logger.debug("unassignIssue: saving unassigned issue..");
				}
				getIssueDAO().saveOrUpdate(issue);
				notificationService.sendNotification(issue, Type.ASSIGNED,
						ServletContextUtils.getItrackerServices()
						.getConfigurationService().getSystemBaseURL());
			}
		}

		return true;
	}

	/**
	 * System-Update an issue, adds the action to the issue and updates the
	 * issue
	 */
	public Issue systemUpdateIssue(Issue updateissue, Integer userId)
	throws ProjectException {

		IssueActivity activity = new IssueActivity();
		activity.setActivityType(IssueActivityType.SYSTEM_UPDATE);
		activity.setDescription(ITrackerResources
				.getString("itracker.activity.system.status"));
		ArrayList<IssueActivity> activities = new ArrayList<IssueActivity>();

		activity.setIssue(updateissue);
		activity.setUser(getUserDAO().findByPrimaryKey(userId));
		updateissue.getActivities().add(activity);

		Issue updated = updateIssue(updateissue, userId);
		updated.getActivities().addAll(activities);
		getIssueDAO().saveOrUpdate(updated);

		return updated;
	}

	/*
	 * public boolean addIssueActivity(IssueActivityModel model) {
	 * 
	 * Issue issue = ifHome.findByPrimaryKey(model.getIssueId());
	 * 
	 * User user = ufHome.findByPrimaryKey(model.getUserId());
	 * 
	 * //return addIssueActivity(model, issue, user); return
	 * addIssueActivity(null, issue, user); }
	 */

	/*
	 * public boolean addIssueActivity(IssueActivityModel model, Issue issue) {
	 * 
	 * User user = ufHome.findByPrimaryKey(model.getUserId());
	 * 
	 * return true;//addIssueActivity(model, issue, user); }
	 */

	/**
	 * I think this entire method is useless - RJST TODO move to
	 * {@link NotificationService}
	 * 
	 * @param model
	 * @param issue
	 * @param user
	 * @return
	 */
	/*
	 * public boolean addIssueActivity(IssueActivityBean model, Issue issue,
	 * User user) {
	 * 
	 * IssueActivityBean activity = new IssueActivityBean();
	 * 
	 * //activity.setModel(model);
	 * 
	 * activity.setIssue(issue);
	 * 
	 * activity.setUser(user);
	 * 
	 * return true; }
	 */

	public void updateIssueActivityNotification(Integer issueId,
			boolean notificationSent) {

		if (issueId == null) {

			return;

		}

		Collection<IssueActivity> activity = getIssueActivityDAO()
		.findByIssueId(issueId);

		for (Iterator<IssueActivity> iter = activity.iterator(); iter.hasNext();) {

			((IssueActivity) iter.next()).setNotificationSent(notificationSent);

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
	public boolean addIssueAttachment(IssueAttachment attachment, byte[] data) {
		Issue issue = attachment.getIssue();
		// User user = attachment.getUser();

		attachment.setFileName("attachment_issue_" + issue.getId() + "_"
				+ attachment.getOriginalFileName());
		attachment.setFileData((data == null ? new byte[0] : data));

		// attachment.setIssue(issue);
		// attachment.setUser(user);

		// TODO: activity for adding attachment?
		// IssueActivity activityAdd = new IssueActivity(attachment.getIssue(),
		// user, IssueActivity.Type.ATTACHEMENT_ADDED)

		if (logger.isDebugEnabled()) {
			logger.debug("addIssueAttachment: adding attachment " + attachment);
		}
		// add attachment to issue
		issue.getAttachments().add(attachment);
		if (logger.isDebugEnabled()) {
			logger.debug("addIssueAttachment: saving updated issue " + issue);
		}
		this.getIssueDAO().saveOrUpdate(issue);
		return true;
	}

	public boolean setIssueAttachmentData(Integer attachmentId, byte[] data) {

		if (attachmentId != null && data != null) {

			IssueAttachment attachment = getIssueAttachmentDAO()
			.findByPrimaryKey(attachmentId);

			attachment.setFileData(data);

			return true;

		}

		return false;

	}

	public boolean setIssueAttachmentData(String fileName, byte[] data) {

		if (fileName != null && data != null) {

			IssueAttachment attachment = getIssueAttachmentDAO()
			.findByFileName(fileName);

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
	public boolean removeIssueAttachment(Integer attachmentId) {

		IssueAttachment attachementBean = this.getIssueAttachmentDAO()
		.findByPrimaryKey(attachmentId);

		getIssueAttachmentDAO().delete(attachementBean);

		return true;
	}

	public Integer removeIssueHistoryEntry(Integer entryId, Integer userId) {

		IssueHistory history = getIssueHistoryDAO().findByPrimaryKey(entryId);

		if (history != null) {

			history.setStatus(IssueUtilities.HISTORY_STATUS_REMOVED);

			// moved date stuff to BaseHibernateDAO
			// history.setLastModifiedDate(new Timestamp(new Date().getTime()));

			IssueActivity activity = new IssueActivity();
			activity
			.setActivityType(org.itracker.model.IssueActivityType.REMOVE_HISTORY);
			activity.setDescription(ITrackerResources
					.getString("itracker.web.generic.entry")
					+ " "
					+ entryId
					+ " "
					+ ITrackerResources
					.getString("itracker.web.generic.removed") + ".");

			getIssueHistoryDAO().delete(history);

			// need to fix this - RJST
			// activity.setIssue(history.getIssue().getId());
			// activity.setUser(userId);
			return history.getIssue().getId();

		}

		return Integer.valueOf(-1);

	}

	public Project getIssueProject(Integer issueId) {
		Issue issue = getIssueDAO().findByPrimaryKey(issueId);
		Project project = issue.getProject();

		return project;
	}

	public HashSet<Integer> getIssueComponentIds(Integer issueId) {

		HashSet<Integer> componentIds = new HashSet<Integer>();
		Issue issue = getIssueDAO().findByPrimaryKey(issueId);
		Collection<Component> components = issue.getComponents();

		for (Iterator<Component> iterator = components.iterator(); iterator
		.hasNext();) {
			componentIds.add(((Component) iterator.next()).getId());
		}

		return componentIds;

	}

	public HashSet<Integer> getIssueVersionIds(Integer issueId) {

		HashSet<Integer> versionIds = new HashSet<Integer>();

		Issue issue = getIssueDAO().findByPrimaryKey(issueId);

		Collection<Version> versions = issue.getVersions();

		for (Iterator<Version> iterator = versions.iterator(); iterator
		.hasNext();) {

			versionIds.add(((Version) iterator.next()).getId());

		}

		return versionIds;

	}

	public List<IssueActivity> getIssueActivity(Integer issueId) {

		int i = 0;

		Collection<IssueActivity> activity = getIssueActivityDAO()
		.findByIssueId(issueId);

		IssueActivity[] activityArray = new IssueActivity[activity.size()];

		for (Iterator<IssueActivity> iterator = activity.iterator(); iterator
		.hasNext(); i++) {

			activityArray[i] = ((IssueActivity) iterator.next());

		}

		return Arrays.asList(activityArray);

	}

	/**
	 * TODO move to {@link NotificationService} ?
	 */
	public List<IssueActivity> getIssueActivity(Integer issueId,
			boolean notificationSent) {

		int i = 0;

		Collection<IssueActivity> activity = getIssueActivityDAO()
		.findByIssueIdAndNotification(issueId, notificationSent);

		IssueActivity[] activityArray = new IssueActivity[activity.size()];

		for (Iterator<IssueActivity> iterator = activity.iterator(); iterator
		.hasNext(); i++) {

			activityArray[i] = ((IssueActivity) iterator.next());

		}

		return Arrays.asList(activityArray);

	}

	public Long getAllIssueAttachmentCount() {
		return getIssueAttachmentDAO().countAll().longValue();
	}

	/**
	 * @deprecated do not use this due to expensive memory use! use explicit
	 *             hsqldb queries instead.
	 */
	public List<IssueAttachment> getAllIssueAttachments() {
		logger.warn("getAllIssueAttachments: use of deprecated API");
		if (logger.isDebugEnabled()) {
			logger.debug("getAllIssueAttachments: stacktrace was",
					new RuntimeException());
		}

		List<IssueAttachment> attachments = getIssueAttachmentDAO().findAll();

		return attachments;
	}

	public IssueAttachment getIssueAttachment(Integer attachmentId) {
		IssueAttachment attachment = getIssueAttachmentDAO().findByPrimaryKey(
				attachmentId);

		return attachment;

	}

	public byte[] getIssueAttachmentData(Integer attachmentId) {

		byte[] data;

		IssueAttachment attachment = getIssueAttachmentDAO().findByPrimaryKey(
				attachmentId);

		data = attachment.getFileData();

		return data;

	}

	public int getIssueAttachmentCount(Integer issueId) {

		int i = 0;

		Issue issue = getIssueDAO().findByPrimaryKey(issueId);

		Collection<IssueAttachment> attachments = issue.getAttachments();

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
	public IssueHistory getLastIssueHistory(Integer issueId) {

		List<IssueHistory> history = getIssueHistoryDAO()
		.findByIssueId(issueId);

		if (null != history && history.size() > 0) {
			// sort ascending by id
			Collections.sort(history, AbstractEntity.ID_COMPARATOR);
			// return last entry in list
			return history.get(history.size() - 1);
		}

		return null;

	}

	public int getOpenIssueCountByProjectId(Integer projectId) {

		Collection<Issue> issues = getIssueDAO().findByProjectAndLowerStatus(
				projectId, IssueUtilities.STATUS_RESOLVED);

		return issues.size();

	}

	public int getResolvedIssueCountByProjectId(Integer projectId) {

		Collection<Issue> issues = getIssueDAO().findByProjectAndHigherStatus(
				projectId, IssueUtilities.STATUS_RESOLVED);

		return issues.size();

	}

	public int getTotalIssueCountByProjectId(Integer projectId) {

		Collection<Issue> issues = getIssueDAO().findByProject(projectId);

		return issues.size();

	}

	public Date getLatestIssueDateByProjectId(Integer projectId) {

		return getIssueDAO().latestModificationDate(projectId);

	}

	public boolean canViewIssue(Integer issueId, User user) {

		Issue issue = getIssue(issueId);

		Map<Integer, Set<PermissionType>> permissions = getUserDAO()
		.getUsersMapOfProjectsAndPermissionTypes(user);

		return IssueUtilities.canViewIssue(issue, user.getId(), permissions);

	}

	public boolean canViewIssue(Issue issue, User user) {

		Map<Integer, Set<PermissionType>> permissions = getUserDAO()
		.getUsersMapOfProjectsAndPermissionTypes(user);

		return IssueUtilities.canViewIssue(issue, user.getId(), permissions);

	}

	private UserDAO getUserDAO() {
		return userDAO;
	}

	private IssueDAO getIssueDAO() {
		return issueDAO;
	}

	private ProjectDAO getProjectDAO() {
		return projectDAO;
	}

	private IssueActivityDAO getIssueActivityDAO() {
		return issueActivityDAO;
	}

	private VersionDAO getVersionDAO() {
		return this.versionDAO;
	}

	private ComponentDAO getComponentDAO() {
		return this.componentDAO;
	}

	private CustomFieldDAO getCustomFieldDAO() {
		return customFieldDAO;
	}

	private IssueHistoryDAO getIssueHistoryDAO() {
		return issueHistoryDAO;
	}

	private IssueRelationDAO getIssueRelationDAO() {
		return issueRelationDAO;
	}

	private IssueAttachmentDAO getIssueAttachmentDAO() {
		return issueAttachmentDAO;
	}

	/**
	 * get total size of all attachments in database
	 */
	public Long getAllIssueAttachmentSize() {

		return getIssueAttachmentDAO().totalAttachmentsSize().longValue() / 1024;

	}

	public List<Issue> searchIssues(IssueSearchQuery queryModel, User user,
			Map<Integer, Set<PermissionType>> userPermissions)
			throws IssueSearchException {
		return getIssueDAO().query(queryModel, user, userPermissions);
	}

	public Long totalSystemIssuesAttachmentSize() {
		return getIssueAttachmentDAO().totalAttachmentsSize();
	}

}
