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
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.itracker.core.resources.ITrackerResources;
import org.itracker.model.Component;
import org.itracker.model.Issue;
import org.itracker.model.IssueActivity;
import org.itracker.model.IssueHistory;
import org.itracker.model.Notification;
import org.itracker.model.Project;
import org.itracker.model.User;
import org.itracker.model.Version;
import org.itracker.model.Notification.Role;
import org.itracker.model.Notification.Type;
import org.itracker.persistence.dao.IssueActivityDAO;
import org.itracker.persistence.dao.IssueDAO;
import org.itracker.persistence.dao.NotificationDAO;
import org.itracker.services.IssueService;
import org.itracker.services.NotificationService;
import org.itracker.services.ProjectService;
import org.itracker.services.util.EmailService;
import org.itracker.services.util.HTMLUtilities;
import org.itracker.services.util.IssueUtilities;
import org.itracker.services.util.ProjectUtilities;
import org.itracker.services.util.UserUtilities;
import org.itracker.web.util.ServletContextUtils;

public class NotificationServiceImpl implements NotificationService {

	// TODO: Cleanup this file, go through all issues, todos, etc.
	
	private EmailService emailService;
	private NotificationDAO notificationDao;
	private ProjectService projectService;
	private IssueActivityDAO issueActivityDao;
	private IssueDAO issueDao;

	private static final Logger logger = null; //Logger
	//		.getLogger(NotificationServiceImpl.class);

	public NotificationServiceImpl() {
  super();
		this.emailService = null;
		this.projectService = null;
		this.notificationDao = null;
	}

	public NotificationServiceImpl(EmailService emailService,
			ProjectService projectService, NotificationDAO notificationDao, IssueActivityDAO issueActivityDao, IssueDAO issueDao) {
		this();
		this.setEmailService(emailService);
		this.setProjectService(projectService);
		this.setNotificationDao(notificationDao);
		this.setIssueActivityDao(issueActivityDao);
		this.setIssueDao(issueDao);
	}

	public void sendNotification(Notification notification, Type type,
			String url) {

		if (logger.isDebugEnabled()) {
			logger.debug("sendNotification: called with notification: "
					+ notification + ", type: " + url + ", url: " + url);
		}
		if (null == notification) {
			throw new IllegalArgumentException("notification must not be null");
		}
		if (null == this.emailService || null == this.notificationDao) {
			throw new IllegalStateException("service not initialized yet");
		}
		if (type == Type.SELF_REGISTER) {
			this.handleSelfRegistrationNotification(notification.getUser()
					.getLogin(), notification.getUser().getEmailAddress(), url);
		} else {
			handleIssueNotification(notification.getIssue(), type, url);

		}

	}

	public void sendNotification(Issue issue, Type type, String baseURL) {
		if (logger.isDebugEnabled()) {
			logger.debug("sendNotification: called with issue: " + issue
					+ ", type: " + type + ", baseURL: " + baseURL);
		}
		handleIssueNotification(issue, type, baseURL);

	}

	public void setEmailService(EmailService emailService) {

		if (null == emailService)
			throw new IllegalArgumentException("email service must not be null");

		if (null != this.emailService) {
			throw new IllegalStateException("email service allready set");
		}
		this.emailService = emailService;

	}

	/**
	 * 
	 * @param notificationMsg
	 * @param url
	 */
	private void handleSelfRegistrationNotification(String login,
			InternetAddress toAddress, String url) {
		if (logger.isDebugEnabled()) {
			logger
					.debug("handleSelfRegistrationNotification: called with login: "
							+ login
							+ ", toAddress"
							+ toAddress
							+ ", url: "
							+ url);
		}
		try {

			if (toAddress != null && !"".equals(toAddress.getAddress())) {
				String subject = ITrackerResources
						.getString("itracker.email.selfreg.subject");
				String msgText = ITrackerResources.getString(
						"itracker.email.selfreg.body", ITrackerResources
								.getDefaultLocale(), new Object[] { login,
								url + "/login.do" });
				emailService.sendEmail(toAddress, subject, msgText);
			} else {
				throw new IllegalArgumentException(
						"To-address must be set for self registration notification.");
			}
		} catch (RuntimeException e) {
			logger.error("failed to handle self registration notification for "
					+ toAddress, e);
			throw e;
		}
	}

	/**
	 * Method for internal sending of a notification of specific type.
	 * 
	 * @param notificationMsg
	 * @param type
	 * @param url
	 */
	private void handleIssueNotification(Issue issue, Type type, String url) {

		if (logger.isDebugEnabled()) {
			logger.debug("handleIssueNotification: called with issue: " + issue
					+ ", type: " + type + "url: " + url);
		}
		this.handleIssueNotification(issue, type, url, null, null);
	}

	/**
	 * Method for internal sending of a notification of specific type.
	 * 
	 * @param notificationMsg
	 * @param type
	 * @param url
	 */
	private void handleIssueNotification(Issue issue, Type type, String url,
			InternetAddress[] receipients, Integer lastModifiedDays) {
		try {

			if (logger.isDebugEnabled()) {
				logger
						.debug("handleIssueNotificationhandleIssueNotification: called with issue: "
								+ issue
								+ ", type: "
								+ type
								+ "url: "
								+ url
								+ ", receipients: "
								+ (null == receipients ? "<null>" : String
										.valueOf(Arrays.asList(receipients)))
								+ ", lastModifiedDays: " + lastModifiedDays);
			}
			List<Notification> notifications;

			if (issue == null) {
				logger
						.warn("handleIssueNotification: issue was null. Notification will not be handled");
				return;
			}

			if (lastModifiedDays == null || lastModifiedDays.intValue() < 0) {
				lastModifiedDays = Integer
						.valueOf(org.itracker.web.scheduler.tasks.ReminderNotification.DEFAULT_ISSUE_AGE);
			}

			if (receipients == null) {
				ArrayList<InternetAddress> recList = new ArrayList<InternetAddress>();
				notifications = this.getIssueNotifications(issue);
				Iterator<Notification> it = notifications.iterator();
				User currentUser;
				while (it.hasNext()) {
					currentUser = it.next().getUser();
					if (null != currentUser
							&& null != currentUser.getEmailAddress()
							&& null != currentUser.getEmail()
							&& (!recList
									.contains(currentUser.getEmailAddress()))
							&& currentUser.getEmail().indexOf('@') >= 0) {

						recList.add(currentUser.getEmailAddress());
					}
				}
				receipients = recList.toArray(new InternetAddress[] {});
			}

			List<IssueActivity> activity = getIssueService().getIssueActivity(
					issue.getId(), false);
			issue.getActivities();
			List<IssueHistory> histories = issue.getHistory();
			Iterator<IssueHistory> it = histories.iterator();
			IssueHistory history = null, currentHistory;
			history = getIssueService().getLastIssueHistory(issue.getId());

			Integer historyId = 0;
			// find history with greatest id
			while (it.hasNext()) {
				currentHistory = (IssueHistory) it.next();
				if (logger.isDebugEnabled()) {
					logger.debug("handleIssueNotification: found history: "
							+ currentHistory.getDescription() + " (time: "
							+ currentHistory.getCreateDate());
				}
				if (currentHistory.getId() > historyId) {
					historyId = currentHistory.getId();
					history = currentHistory;
				}
			}
			if (logger.isDebugEnabled() && null != history) {
				logger
						.debug("handleIssueNotification: got most recent history: "
								+ history
								+ " ("
								+ history.getDescription()
								+ ")");
			}

			List<Component> components = issue.getComponents();
			// issueService
			// .getIssueComponents(issue.getId());
			
			List<Version> versions = issue.getVersions();
			// issueService.getIssueVersions(issue.getId());
			
			if (receipients.length > 0) {
				String subject = "";
				if (type == Type.CREATED) {
					subject = ITrackerResources.getString(
							"itracker.email.issue.subject.created",
							new Object[] { issue.getId(),
									issue.getProject().getName(),
									lastModifiedDays });
				} else if (type == Type.ASSIGNED) {
					subject = ITrackerResources.getString(
							"itracker.email.issue.subject.assigned",
							new Object[] { issue.getId(),
									issue.getProject().getName(),
									lastModifiedDays });
				} else if (type == Type.CLOSED) {
					subject = ITrackerResources.getString(
							"itracker.email.issue.subject.closed",
							new Object[] { issue.getId(),
									issue.getProject().getName(),
									lastModifiedDays });
				} else if (type == Type.ISSUE_REMINDER) {
					subject = ITrackerResources.getString(
							"itracker.email.issue.subject.reminder",
							new Object[] { issue.getId(),
									issue.getProject().getName(),
									lastModifiedDays });
				} else {
					subject = ITrackerResources.getString(
							"itracker.email.issue.subject.updated",
							new Object[] { issue.getId(),
									issue.getProject().getName(),
									lastModifiedDays });
				}

				String activityString;
				String componentString = "";
				String versionString = "";
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < activity.size(); i++) {
					sb.append(
							IssueUtilities.getActivityName(activity.get(i)
									.getActivityType())).append(": ").append(
							activity.get(i).getDescription()).append("\n");

				}
				activityString = sb.toString();
				for (int i = 0; i < components.size(); i++) {
					componentString += (i != 0 ? ", " : "")
							+ components.get(i).getName();
				}
				for (int i = 0; i < versions.size(); i++) {
					versionString += (i != 0 ? ", " : "")
							+ versions.get(i).getNumber();
				}

				String msgText = "";
				if (type == Type.ISSUE_REMINDER) {
					msgText = ITrackerResources
							.getString(
									"itracker.email.issue.body.reminder",
									new Object[] {
											url
													+ "/module-projects/view_issue.do?id="
													+ issue.getId(),
											issue.getProject().getName(),
											issue.getDescription(),
											IssueUtilities.getStatusName(issue
													.getStatus()),
											IssueUtilities
													.getSeverityName(issue
															.getSeverity()),
											(issue.getOwner().getFirstName() != null ? issue
													.getOwner().getFirstName()
													: "")
													+ " "
													+ (issue.getOwner()
															.getLastName() != null ? issue
															.getOwner()
															.getLastName()
															: ""),
											componentString,
											(history == null ? "" : history
													.getUser().getFirstName()
													+ " "
													+ history.getUser()
															.getLastName()),
											(history == null ? ""
													: HTMLUtilities
															.removeMarkup(history
																	.getDescription())),
											lastModifiedDays, activityString });
				} else {
					String resolution = (issue.getResolution() == null ? ""
							: issue.getResolution());
					if (!resolution.equals("")
							&& ProjectUtilities
									.hasOption(
											ProjectUtilities.OPTION_PREDEFINED_RESOLUTIONS,
											issue.getProject().getOptions())) {
						resolution = IssueUtilities.getResolutionName(
								resolution, ITrackerResources.getLocale());
					}

					msgText = ITrackerResources
							.getString(
									"itracker.email.issue.body.standard",
									new Object[] {
											new StringBuffer(url).append("/module-projects/view_issue.do?id=").append(issue.getId()).toString(),
											issue.getProject().getName(),
											issue.getDescription(),
											IssueUtilities.getStatusName(issue
													.getStatus()),
											resolution,
											IssueUtilities
													.getSeverityName(issue
															.getSeverity()),
											(null != issue.getOwner() && null != issue.getOwner().getFirstName() ? issue
													.getOwner().getFirstName()
													: "")
													+ " "
													+ (null != issue.getOwner() && null != issue.getOwner()
															.getLastName() ? issue
															.getOwner()
															.getLastName()
															: ""),
											componentString,
											(history == null ? "" : history
													.getUser().getFirstName()
													+ " "
													+ history.getUser()
															.getLastName()),
											(history == null ? ""
													: HTMLUtilities
															.removeMarkup(history
																	.getDescription())),
											activityString });
				}
				emailService.sendEmail(receipients, subject, msgText);

				updateIssueActivityNotification(issue, true);
			}

		} catch (Exception e) {
			logger
					.error(
							"handleIssueNotification: unexpected exception caught, throwing runtime exception",
							e);
			throw new RuntimeException(e);
		}
	}


	/**
	 * Method for internal sending of a notification of specific type.
	 * 
	 * TODO: final debugging/integration/implementation
	 * TODO: Decide if this code is really needed and document for what
	 * 
	 * @param notificationMsg
	 * @param type
	 * @param url
	 */
	
	@SuppressWarnings("unused")
	private void handleLocalizedIssueNotification(final Issue issue, final Type type, final String url,
			final InternetAddress[] receipients, Integer lastModifiedDays) {
		try {
			
			if (logger.isDebugEnabled()) {
				logger
						.debug("handleIssueNotificationhandleIssueNotification: running as thread, called with issue: "
								+ issue
								+ ", type: "
								+ type
								+ "url: "
								+ url
								+ ", receipients: "
								+ (null == receipients ? "<null>" : String
										.valueOf(Arrays.asList(receipients)))
								+ ", lastModifiedDays: " + lastModifiedDays);
			}
			
			final Integer notModifiedSince;
			
			if (lastModifiedDays == null || lastModifiedDays.intValue() < 0) {
				notModifiedSince = Integer
						.valueOf(org.itracker.web.scheduler.tasks.ReminderNotification.DEFAULT_ISSUE_AGE);
			} else {
				notModifiedSince = lastModifiedDays;
			}

					try {
						if (logger.isDebugEnabled()) {
							logger
									.debug("handleIssueNotificationhandleIssueNotification.run: running as thread, called with issue: "
											+ issue
											+ ", type: "
											+ type
											+ "url: "
											+ url
											+ ", receipients: "
											+ (null == receipients ? "<null>" : String
													.valueOf(Arrays.asList(receipients)))
											+ ", notModifiedSince: " + notModifiedSince);
						}
						final List<Notification> notifications;
						if (issue == null) {
							logger
									.warn("handleIssueNotification: issue was null. Notification will not be handled");
							return;
						}
						Map<InternetAddress, Locale> localeMapping = null;
			
						if (receipients == null) {
							
							
							notifications = this.getIssueNotifications(issue);
							
							localeMapping = new Hashtable<InternetAddress, Locale>(notifications.size());
							Iterator<Notification> it = notifications.iterator();
							User currentUser;
							while (it.hasNext()) {
								currentUser = it.next().getUser();
								if (null != currentUser
										&& null != currentUser.getEmailAddress()
										&& null != currentUser.getEmail()
										&& (!localeMapping.keySet()
												.contains(currentUser.getEmailAddress()))) {
									
									try {
										localeMapping.put(currentUser.getEmailAddress(), ITrackerResources.getLocale(currentUser.getPreferences().getUserLocale()));
									} catch (RuntimeException re) {
										localeMapping.put(currentUser.getEmailAddress(), ITrackerResources.getLocale());
									}
								}
							}
						} else {
							localeMapping = new Hashtable<InternetAddress, Locale>(1);
							Locale locale = ITrackerResources.getLocale();
							Iterator<InternetAddress> it = Arrays.asList(receipients).iterator();
							while (it.hasNext()) {
								InternetAddress internetAddress = (InternetAddress) it
										.next();
								localeMapping.put(internetAddress, locale);
							}
						}
			
						this.handleNotification(issue, type, notModifiedSince, localeMapping, url);
					} catch (Exception e) {
						logger.error("run: failed to process notification", e);
					}	

		} catch (Exception e) {
			logger
					.error(
							"handleIssueNotification: unexpected exception caught, throwing runtime exception",
							e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Send notifications to mapped addresses by locale.
	 * @param issue
	 * @param type
	 * @param notModifiedSince
	 * @param recipientsLocales
	 * @param url
	 */
	private void handleNotification(Issue issue, Type type, Integer notModifiedSince, Map<InternetAddress, Locale> recipientsLocales, final String url) {
		Set<InternetAddress> recipients = recipientsLocales.keySet();
		Map<Locale, Set<InternetAddress>> localeRecipients = new Hashtable<Locale, Set<InternetAddress>>();

		List<Component> components = issue.getComponents();
		List<Version> versions = issue.getVersions();
		
		List<IssueActivity> activity = getIssueService().getIssueActivity(
				issue.getId(), false);
		issue.getActivities();
		List<IssueHistory> histories = issue.getHistory();
		Iterator<IssueHistory> it = histories.iterator();
		IssueHistory history = null, currentHistory;
		history = getIssueService().getLastIssueHistory(issue.getId());

		Integer historyId = 0;
		// find history with greatest id
		while (it.hasNext()) {
			currentHistory = (IssueHistory) it.next();
			if (logger.isDebugEnabled()) {
				logger.debug("handleIssueNotification: found history: "
						+ currentHistory.getDescription() + " (time: "
						+ currentHistory.getCreateDate());
			}
			if (currentHistory.getId() > historyId) {
				historyId = currentHistory.getId();
				history = currentHistory;
			}
		}
		if (logger.isDebugEnabled() && null != history) {
			logger
					.debug("handleIssueNotification: got most recent history: "
							+ history
							+ " ("
							+ history.getDescription()
							+ ")");
		}
		
		Iterator<InternetAddress> iaIt = recipientsLocales.keySet().iterator();
		while (iaIt.hasNext()) {
			InternetAddress internetAddress = (InternetAddress) iaIt.next();
			if (localeRecipients.keySet().contains(recipientsLocales.get(internetAddress))) {
				localeRecipients.get(recipientsLocales.get(internetAddress)).add(internetAddress);
			} else {
				Set<InternetAddress> addresses = new HashSet<InternetAddress>();
				localeRecipients.put(recipientsLocales.get(internetAddress), addresses);
			}
		}
		
		Iterator<Locale> localesIt = localeRecipients.keySet().iterator();
		try {
			while (localesIt.hasNext()) {
				Locale currentLocale = (Locale) localesIt.next();
				recipients = localeRecipients.get(currentLocale);
				
				
				if (recipients.size() > 0) {
					String subject = "";
					if (type == Type.CREATED) {
						subject = ITrackerResources.getString(
								"itracker.email.issue.subject.created",
								currentLocale,
								new Object[] { issue.getId(),
										issue.getProject().getName(),
										notModifiedSince });
					} else if (type == Type.ASSIGNED) {
						subject = ITrackerResources.getString(
								"itracker.email.issue.subject.assigned",
								currentLocale,
								new Object[] { issue.getId(),
										issue.getProject().getName(),
										notModifiedSince });
					} else if (type == Type.CLOSED) {
						subject = ITrackerResources.getString(
								"itracker.email.issue.subject.closed",
								currentLocale,
								new Object[] { issue.getId(),
										issue.getProject().getName(),
										notModifiedSince });
					} else if (type == Type.ISSUE_REMINDER) {
						subject = ITrackerResources.getString(
								"itracker.email.issue.subject.reminder",
								currentLocale,
								new Object[] { issue.getId(),
										issue.getProject().getName(),
										notModifiedSince });
					} else {
						subject = ITrackerResources.getString(
								"itracker.email.issue.subject.updated",
								currentLocale,
								new Object[] { issue.getId(),
										issue.getProject().getName(),
										notModifiedSince });
					}
	
					String activityString;
					String componentString = "";
					String versionString = "";
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < activity.size(); i++) {
						sb.append(
								IssueUtilities.getActivityName(activity.get(i)
										.getActivityType(), currentLocale)).append(": ").append(
								activity.get(i).getDescription()).append("\n");
	
					}
					
					activityString = sb.toString();
					// TODO localize..
					for (int i = 0; i < components.size(); i++) {
						componentString += (i != 0 ? ", " : "")
								+ components.get(i).getName();
					}
					for (int i = 0; i < versions.size(); i++) {
						versionString += (i != 0 ? ", " : "")
								+ versions.get(i).getNumber();
					}
	
					String msgText = "";
					if (type == Type.ISSUE_REMINDER) {
						msgText = ITrackerResources
								.getString(
										"itracker.email.issue.body.reminder",
										currentLocale,
										new Object[] {
												url
														+ "/module-projects/view_issue.do?id="
														+ issue.getId(),
												issue.getProject().getName(),
												issue.getDescription(),
												IssueUtilities.getStatusName(issue
														.getStatus()),
												IssueUtilities
														.getSeverityName(issue
																.getSeverity()),
												(issue.getOwner().getFirstName() != null ? issue
														.getOwner().getFirstName()
														: "")
														+ " "
														+ (issue.getOwner()
																.getLastName() != null ? issue
																.getOwner()
																.getLastName()
																: ""),
												componentString,
												(history == null ? "" : history
														.getUser().getFirstName()
														+ " "
														+ history.getUser()
																.getLastName()),
												(history == null ? ""
														: HTMLUtilities
																.removeMarkup(history
																		.getDescription())),
																		notModifiedSince, activityString });
					} else {
						String resolution = (issue.getResolution() == null ? ""
								: issue.getResolution());
						if (!resolution.equals("")
								&& ProjectUtilities
										.hasOption(
												ProjectUtilities.OPTION_PREDEFINED_RESOLUTIONS,
												issue.getProject().getOptions())) {
							resolution = IssueUtilities.getResolutionName(
									resolution, ITrackerResources.getLocale());
						}
	
						msgText = ITrackerResources
								.getString(
										"itracker.email.issue.body.standard",
										currentLocale,
										new Object[] {
												new StringBuffer(url).append("/module-projects/view_issue.do?id=").append(issue.getId()).toString(),
												issue.getProject().getName(),
												issue.getDescription(),
												IssueUtilities.getStatusName(issue
														.getStatus()),
												resolution,
												IssueUtilities
														.getSeverityName(issue
																.getSeverity()),
												(null != issue.getOwner() && null != issue.getOwner().getFirstName() ? issue
														.getOwner().getFirstName()
														: "")
														+ " "
														+ (null != issue.getOwner() && null != issue.getOwner()
																.getLastName() ? issue
																.getOwner()
																.getLastName()
																: ""),
												componentString,
												(history == null ? "" : history
														.getUser().getFirstName()
														+ " "
														+ history.getUser()
																.getLastName()),
												(history == null ? ""
														: HTMLUtilities
																.removeMarkup(history
																		.getDescription())),
												activityString });
					}
					
					if (logger.isInfoEnabled()) {
						logger.info(new StringBuilder("handleNotification: sending notification for ").append(issue).append(" (").append(type).append(") to ").append(currentLocale).append("-users (").append(recipients + ")").toString());
						
					}
					
					emailService.sendEmail(recipients, subject, msgText);
	
					if (logger.isDebugEnabled()) {
						logger.debug("handleNotification: sent notification for " + issue);
					}
				}
	
				updateIssueActivityNotification(issue, true);
				if (logger.isDebugEnabled()) {
					logger.debug("handleNotification: sent notification for locales " + localeRecipients.keySet() + " recipients: " + localeRecipients.values());
				}
			}
		} catch (RuntimeException e) {
			logger.error("handleNotification: failed to notify: " + issue + " (locales: " + localeRecipients.keySet() + ")", e);
		
		}
		
		
	}
	private IssueService getIssueService() {
		return ServletContextUtils.getItrackerServices().getIssueService();
	}

	public void updateIssueActivityNotification(Issue issue,
			Boolean notificationSent) {
		if (logger.isDebugEnabled()) {
			logger.debug("updateIssueActivityNotification: called with "
					+ issue + ", notificationSent: " + notificationSent);
		}

		Collection<IssueActivity> activity = getIssueActivityDao()
				.findByIssueId(issue.getId());

		for (Iterator<IssueActivity> iter = activity.iterator(); iter.hasNext();) {

			((IssueActivity) iter.next()).setNotificationSent(notificationSent);

		}
	}

	/**
	 */
	public boolean addIssueNotification(Notification notification) {
		if (logger.isDebugEnabled()) {
			logger.debug("addIssueNotification: called with notification: "
					+ notification);
		}
		Issue issue = notification.getIssue();
		if (!issue.getNotifications().contains(notification)) {
			if (notification.getCreateDate() == null) {
				notification.setCreateDate(new Date());
			}
			if (notification.getLastModifiedDate() == null) {
				notification.setLastModifiedDate(new Date());
			}
	//		List<Notification> notifications = new ArrayList<Notification>();
			// TODO: check these 3 lines - do we need them?:
	//		notifications.addAll(issue.getNotifications());
	//		notifications.add(notification);
	//		issue.setNotifications(notifications);
			getNotificationDao().save(notification);
			// TODO: is it needed to update issue too?
			issue.getNotifications().add(notification);
			getIssueDao().merge(issue);
			
			return true;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("addIssueNotification: attempted to add duplicate notification " + notification + " for issue: " + issue);
		}
		return false;
	}

	/**
	 * 
	 */
	public List<Notification> getIssueNotifications(Issue issue,
			boolean primaryOnly, boolean activeOnly) {
		if (logger.isDebugEnabled()) {
			logger.debug("getIssueNotifications: called with issue: " + issue
					+ ", primaryOnly: " + primaryOnly + ", activeOnly: "
					+ activeOnly);
		}
		List<Notification> issueNotifications = new ArrayList<Notification>();
		if (issue == null) {
			logger.warn("getIssueNotifications: no issue, throwing exception");
			throw new IllegalArgumentException("issue must not be null");
		}
		if (!primaryOnly) {
			List<Notification> notifications = getNotificationDao()
					.findByIssueId(issue.getId());

			for (Iterator<Notification> iterator = notifications.iterator(); iterator
					.hasNext();) {
				Notification notification = iterator.next();
				User notificationUser = notification.getUser();

				if (!activeOnly
						|| notificationUser.getStatus() == UserUtilities.STATUS_ACTIVE) {
					issueNotifications.add(notification);
				}
			}
		}

		// Now add in other notifications like owner, creator, project owners,
		// etc...

		boolean hasOwner = false;
		// getIssueDAO().findByPrimaryKey(issueId);
		if (issue != null) {
			if (issue.getOwner() != null) {
				User ownerModel = issue.getOwner();

				if (ownerModel != null
						&& (!activeOnly || ownerModel.getStatus() == UserUtilities.STATUS_ACTIVE)) {
					issueNotifications.add(new Notification(ownerModel, issue,
							Role.OWNER));
					hasOwner = true;
				}
			}

			if (!primaryOnly || !hasOwner) {
				User creatorModel = issue.getCreator();

				if (creatorModel != null
						&& (!activeOnly || creatorModel.getStatus() == UserUtilities.STATUS_ACTIVE)) {
					issueNotifications.add(new Notification(creatorModel,
							issue, Role.CREATOR));
				}
			}

			Project project = getProjectService().getProject(
					issue.getProject().getId());
			Collection<User> projectOwners = project.getOwners();

			for (Iterator<User> iterator = projectOwners.iterator(); iterator
					.hasNext();) {
				User projectOwner = (User) iterator.next();

				if (projectOwner != null
						&& (!activeOnly || projectOwner.getStatus() == UserUtilities.STATUS_ACTIVE)) {
					issueNotifications.add(new Notification(projectOwner,
							issue, Role.PO));
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("getIssueNotifications: returning "
					+ issueNotifications);
		}
		return issueNotifications;
	}

	public List<Notification> getIssueNotifications(Issue issue) {
		if (logger.isDebugEnabled()) {
			logger.debug("getIssueNotifications: called with: " + issue);
		}
		return this.getIssueNotifications(issue, false, true);
	}

	public List<Notification> getPrimaryIssueNotifications(Issue issue) {
		if (logger.isDebugEnabled()) {
			logger.debug("getPrimaryIssueNotifications: called with: " + issue);
		}
		return this.getIssueNotifications(issue, true, false);
	}

	public boolean hasIssueNotification(Issue issue, Integer userId) {
		if (logger.isDebugEnabled()) {
			logger.debug("hasIssueNotification: called with: " + issue
					+ ", userId: " + userId);
		}
		return hasIssueNotification(issue, userId, Role.ANY);
	}

	/**
	 * @param issueId
	 * @param userId
	 * @param role
	 * @return
	 */
	public boolean hasIssueNotification(Issue issue, Integer userId, Role role) {

		if (issue != null && userId != null) {

			List<Notification> notifications = getIssueNotifications(issue,
					false, false);

			for (int i = 0; i < notifications.size(); i++) {

				if (role == Role.ANY || notifications.get(i).getRole() == role) {

					if (notifications.get(i).getUser().getId().equals(userId)) {

						return true;

					}

				}

			}

		}

		return false;

	}

	public boolean removeIssueNotification(Integer notificationId) {
		Notification notification = this.getNotificationDao().findById(
				notificationId);
		getNotificationDao().delete(notification);
		return true;
	}

	public void sendNotification(Issue issue, Type type, String baseURL,
			InternetAddress[] receipients, Integer lastModifiedDays) {
		this.handleIssueNotification(issue, type, baseURL, receipients,
				lastModifiedDays);

	}



	/**
	 * @return the emailService
	 */
	public EmailService getEmailService() {
		return emailService;
	}

	/**
	 * @return the notificationDao
	 */
	private NotificationDAO getNotificationDao() {
		return notificationDao;
	}

	/**
	 * @return the projectService
	 */
	public ProjectService getProjectService() {
		return projectService;
	}

	/**
	 * @param projectService
	 *            the projectService to set
	 */
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * @param notificationDao
	 *            the notificationDao to set
	 */
	public void setNotificationDao(NotificationDAO notificationDao) {
		if (null == notificationDao) {
			throw new IllegalArgumentException(
					"notification dao must not be null");
		}
		if (null != this.notificationDao) {
			throw new IllegalStateException("notification dao allready set");
		}
		this.notificationDao = notificationDao;
	}

	/**
	 * TODO url should be automatically generated by configuration (baseurl) and
	 * notification (issue-id).
	 * 
	 * @param notificationId
	 * @param url
	 */
	public void sendNotification(Integer notificationId, Type type, String url) {

		Notification notification = notificationDao.findById(notificationId);
		this.sendNotification(notification, type, url);

	}

	/**
	 * @return the issueActivityDao
	 */
	public IssueActivityDAO getIssueActivityDao() {
		return issueActivityDao;
	}

	/**
	 * @param issueActivityDao
	 *            the issueActivityDao to set
	 */
	public void setIssueActivityDao(IssueActivityDAO issueActivityDao) {
		this.issueActivityDao = issueActivityDao;
	}

	/**
	 * @return the issueDao
	 */
	public IssueDAO getIssueDao() {
		return issueDao;
	}

	/**
	 * @param issueDao
	 *            the issueDao to set
	 */
	public void setIssueDao(IssueDAO issueDao) {
		this.issueDao = issueDao;
	}

}
