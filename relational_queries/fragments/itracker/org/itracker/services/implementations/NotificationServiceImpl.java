package org.
  itracker.
  services.
  implementations;

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

public class NotificationServiceImpl implements org.
  itracker.
  services.
  NotificationService {
    private org.itracker.services.util.EmailService emailService;
    private org.itracker.persistence.dao.NotificationDAO notificationDao;
    private org.itracker.services.ProjectService projectService;
    private org.itracker.persistence.dao.IssueActivityDAO issueActivityDao;
    private org.itracker.persistence.dao.IssueDAO issueDao;
    private static final org.apache.log4j.Logger logger = null;
    
    public NotificationServiceImpl() {
        super();
        this.emailService = null;
        this.projectService = null;
        this.notificationDao = null;
    }
    
    public NotificationServiceImpl(org.itracker.services.util.
                                     EmailService emailService,
                                   org.itracker.services.
                                     ProjectService projectService,
                                   org.itracker.persistence.dao.
                                     NotificationDAO notificationDao,
                                   org.itracker.persistence.dao.
                                     IssueActivityDAO issueActivityDao,
                                   org.itracker.persistence.dao.
                                     IssueDAO issueDao) {
        this();
        this.setEmailService(emailService);
        this.setProjectService(projectService);
        this.setNotificationDao(notificationDao);
        this.setIssueActivityDao(issueActivityDao);
        this.setIssueDao(issueDao);
    }
    
    public void sendNotification(org.itracker.model.Notification notification,
                                 org.itracker.model.Notification.Type type,
                                 java.lang.String url) {
        if (logger.isDebugEnabled()) {
            logger.debug("sendNotification: called with notification: " +
                           notification + ", type: " + url + ", url: " + url);
        }
        if (null == notification) {
            throw new java.lang.IllegalArgumentException(
              "notification must not be null");
        }
        if (null == this.emailService || null == this.notificationDao) {
            throw new java.lang.IllegalStateException(
              "service not initialized yet");
        }
        if (type == org.itracker.model.Notification.Type.SELF_REGISTER) {
            this.handleSelfRegistrationNotification(
                   notification.getUser().getLogin(),
                   notification.getUser().getEmailAddress(), url);
        } else {
            handleIssueNotification(notification.getIssue(), type, url);
        }
    }
    
    public void sendNotification(org.itracker.model.Issue issue,
                                 org.itracker.model.Notification.Type type,
                                 java.lang.String baseURL) {
        if (logger.isDebugEnabled()) {
            logger.debug("sendNotification: called with issue: " + issue +
                           ", type: " + type + ", baseURL: " + baseURL);
        }
        handleIssueNotification(issue, type, baseURL);
    }
    
    public void setEmailService(org.itracker.services.util.
                                  EmailService emailService) {
        if (null == emailService)
            throw new java.lang.IllegalArgumentException(
              "email service must not be null");
        if (null != this.emailService) {
            throw new java.lang.IllegalStateException(
              "email service allready set");
        }
        this.emailService = emailService;
    }
    
    /**
    
     * 
    
     * @param notificationMsg
    
     * @param url
    
     */
    private void handleSelfRegistrationNotification(java.lang.String login,
                                                    javax.mail.internet.
                                                      InternetAddress toAddress,
                                                    java.lang.String url) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                     "handleSelfRegistrationNotification: called with login: " +
                       login + ", toAddress" + toAddress + ", url: " + url);
        }
        try {
            if (toAddress != null && !"".equals(toAddress.getAddress())) {
                java.lang.String subject =
                  org.itracker.core.resources.ITrackerResources.
                  getString("itracker.email.selfreg.subject");
                java.
                  lang.
                  String
                  msgText =
                  org.
                  itracker.
                  core.
                  resources.
                  ITrackerResources.
                  getString(
                    "itracker.email.selfreg.body",
                    org.itracker.core.resources.ITrackerResources.
                        getDefaultLocale(),
                    new java.lang.Object[] { login, url + "/login.do" });
                emailService.sendEmail(toAddress, subject, msgText);
            }
            else {
                throw new java.lang.IllegalArgumentException(
                  "To-address must be set for self registration notification.");
            }
        }
        catch (java.lang.RuntimeException e) {
            logger.error(
                     "failed to handle self registration notification for " +
                       toAddress, e);
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
    private void handleIssueNotification(org.itracker.model.Issue issue,
                                         org.itracker.model.Notification.
                                           Type type, java.lang.String url) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleIssueNotification: called with issue: " +
                           issue + ", type: " + type + "url: " + url);
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
    private void handleIssueNotification(org.itracker.model.Issue issue,
                                         org.itracker.model.Notification.
                                           Type type,
                                         java.lang.String url,
                                         javax.mail.internet.
                                           InternetAddress[] receipients,
                                         java.lang.Integer lastModifiedDays) {
        try {
            if (logger.isDebugEnabled()) {
                logger.
                  debug(
                    ("handleIssueNotificationhandleIssueNotification: called with " +
                     "issue: ") +
                      issue +
                      ", type: " +
                      type +
                      "url: " +
                      url +
                      ", receipients: " +
                      (null ==
                         receipients
                         ? "<null>"
                         : java.lang.String.
                         valueOf(java.util.Arrays.asList(receipients))) +
                      ", lastModifiedDays: " +
                      lastModifiedDays);
            }
            java.util.List<org.itracker.model.Notification> notifications;
            if (issue == null) {
                logger.
                  warn(
                    ("handleIssueNotification: issue was null. Notification will n" +
                     "ot be handled"));
                return;
            }
            if (lastModifiedDays == null || lastModifiedDays.intValue() < 0) {
                lastModifiedDays =
                  java.
                    lang.
                    Integer.
                    valueOf(
                      org.itracker.web.scheduler.tasks.ReminderNotification.
                        DEFAULT_ISSUE_AGE);
            }
            if (receipients == null) {
                java.util.ArrayList<javax.mail.internet.InternetAddress>
                  recList =
                  new java.util.ArrayList<javax.mail.internet.InternetAddress>(
                  );
                notifications = this.getIssueNotifications(issue);
                java.util.Iterator<org.itracker.model.Notification> it =
                  notifications.iterator();
                org.itracker.model.User currentUser;
                while (it.hasNext()) {
                    currentUser = it.next().getUser();
                    if (null != currentUser && null !=
                          currentUser.getEmailAddress() && null !=
                          currentUser.getEmail() &&
                          !recList.contains(currentUser.getEmailAddress()) &&
                          currentUser.getEmail().indexOf('@') >= 0) {
                        recList.add(currentUser.getEmailAddress());
                    }
                }
                receipients =
                  recList.toArray(
                            new javax.mail.internet.InternetAddress[] {  });
            }

            labeled_1 :
            {
                java.util.List<org.itracker.model.IssueActivity> activity =
                        getIssueService().getIssueActivity(issue.getId(), false);
                issue.getActivities();
                java.util.List<org.itracker.model.IssueHistory> histories =
                        issue.getHistory();
                java.util.Iterator<org.itracker.model.IssueHistory> it =
                        histories.iterator();
                org.itracker.model.IssueHistory history = null;
                org.itracker.model.IssueHistory currentHistory;
                history = getIssueService().getLastIssueHistory(issue.getId());
                java.lang.Integer historyId = 0;
                while (it.hasNext())
                {
                    currentHistory = (org.itracker.model.IssueHistory) it.next();
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("handleIssueNotification: found history: " +
                                currentHistory.getDescription() +
                                " (time: " + currentHistory.getCreateDate());
                    }
                    if (currentHistory.getId() > historyId)
                    {
                        historyId = currentHistory.getId();
                        history = currentHistory;
                    }
                }
            }
            if (logger.isDebugEnabled() && null != history) {
                logger.debug(
                         "handleIssueNotification: got most recent history: " +
                           history + " (" + history.getDescription() + ")");
            }
            java.util.List<org.itracker.model.Component> components =
              issue.getComponents();
            java.util.List<org.itracker.model.Version> versions =
              issue.getVersions();
            if (receipients.length > 0) {
                java.lang.String subject = "";
                if (type == org.itracker.model.Notification.Type.CREATED) {
                    subject =
                      org.itracker.core.resources.ITrackerResources.
                        getString("itracker.email.issue.subject.created",
                                  new java.lang.Object[] { issue.getId(),
                                    issue.getProject().getName(),
                                    lastModifiedDays });
                }
                else
                    if (type ==
                          org.itracker.model.Notification.Type.ASSIGNED) {
                        subject =
                          org.itracker.core.resources.ITrackerResources.
                            getString("itracker.email.issue.subject.assigned",
                                      new java.lang.Object[] { issue.getId(),
                                        issue.getProject().getName(),
                                        lastModifiedDays });
                    }
                    else
                        if (type ==
                              org.itracker.model.Notification.Type.CLOSED) {
                            subject =
                              org.itracker.core.resources.ITrackerResources.
                                getString(
                                  "itracker.email.issue.subject.closed",
                                  new java.lang.Object[] { issue.getId(),
                                    issue.getProject().getName(),
                                    lastModifiedDays });
                        }
                        else
                            if (type ==
                                  org.itracker.model.Notification.Type.
                                    ISSUE_REMINDER) {
                                subject =
                                  org.itracker.core.resources.ITrackerResources.
                                    getString(
                                      "itracker.email.issue.subject.reminder",
                                      new java.lang.Object[] { issue.getId(),
                                        issue.getProject().getName(),
                                        lastModifiedDays });
                            }
                            else {
                                subject =
                                  org.itracker.core.resources.ITrackerResources.
                                    getString(
                                      "itracker.email.issue.subject.updated",
                                      new java.lang.Object[] { issue.getId(),
                                        issue.getProject().getName(),
                                        lastModifiedDays });
                            }
                java.lang.String activityString;
                java.lang.String componentString = "";
                java.lang.String versionString = "";
                java.lang.StringBuffer sb = new java.lang.StringBuffer();
                int i = 0;
                while (i < activity.size()) {
                    sb.
                      append(
                        org.itracker.services.util.IssueUtilities.
                            getActivityName(activity.get(i).getActivityType())).
                      append(": ").append(activity.get(i).getDescription()).
                      append("\n");
                    i++;
                }
                activityString = sb.toString();
                i = 0;
                while (i < components.size()) {
                    componentString += (i != 0 ? ", " : "") +
                                       components.get(i).getName();
                    i++;
                }
                i = 0;
                while (i < versions.size()) {
                    versionString += (i != 0 ? ", " : "") +
                                     versions.get(i).getNumber();
                    i++;
                }
                java.lang.String msgText = "";
                if (type ==
                      org.itracker.model.Notification.Type.ISSUE_REMINDER) {
                    msgText =
                      org.
                        itracker.
                        core.
                        resources.
                        ITrackerResources.
                        getString(
                          "itracker.email.issue.body.reminder",
                          new java.lang.Object[] { url +
                            "/module-projects/view_issue.do?id=" +
                            issue.
                              getId(),
                            issue.
                              getProject().getName(),
                            issue.
                              getDescription(),
                            org.itracker.services.util.IssueUtilities.
                              getStatusName(issue.getStatus()),
                            org.itracker.services.util.IssueUtilities.
                              getSeverityName(issue.getSeverity()),
                            (issue.getOwner().getFirstName() != null
                               ? issue.getOwner().getFirstName()
                               : "") +
                            " " +
                            (issue.getOwner().getLastName() != null
                               ? issue.getOwner().getLastName()
                               : ""),
                            componentString,
                            history ==
                              null
                              ? ""
                              : history.getUser().getFirstName() +
                            " " +
                            history.getUser().getLastName(),
                            history ==
                              null
                              ? ""
                              : org.itracker.services.util.HTMLUtilities.
                              removeMarkup(history.getDescription()),
                            lastModifiedDays,
                            activityString });
                }
                else {
                    java.lang.String resolution =
                      issue.getResolution() ==
                      null
                      ? ""
                      : issue.getResolution();
                    if (!resolution.
                          equals("") &&
                          org.
                          itracker.
                          services.
                          util.
                          ProjectUtilities.
                          hasOption(
                            org.itracker.services.util.ProjectUtilities.
                              OPTION_PREDEFINED_RESOLUTIONS,
                            issue.getProject().getOptions())) {
                        resolution =
                          org.
                            itracker.
                            services.
                            util.
                            IssueUtilities.
                            getResolutionName(
                              resolution,
                              org.itracker.core.resources.ITrackerResources.
                                  getLocale());
                    }
                    msgText =
                      org.
                        itracker.
                        core.
                        resources.
                        ITrackerResources.
                        getString(
                          "itracker.email.issue.body.standard",
                          new java.lang.Object[] { new java.lang.StringBuffer(
                              url).
                              append("/module-projects/view_issue.do?id=").
                              append(issue.getId()).toString(),
                            issue.getProject().getName(),
                            issue.getDescription(),
                            org.itracker.services.util.IssueUtilities.
                              getStatusName(issue.getStatus()),
                            resolution,
                            org.itracker.services.util.IssueUtilities.
                              getSeverityName(issue.getSeverity()),
                            (null != issue.getOwner() && null !=
                               issue.getOwner().getFirstName()
                               ? issue.getOwner().getFirstName()
                               : "") +
                            " " +
                            (null != issue.getOwner() && null !=
                               issue.getOwner().getLastName()
                               ? issue.getOwner().getLastName()
                               : ""),
                            componentString,
                            history ==
                              null
                              ? ""
                              : history.getUser().getFirstName() +
                            " " +
                            history.getUser().getLastName(),
                            history ==
                              null
                              ? ""
                              : org.itracker.services.util.HTMLUtilities.
                              removeMarkup(history.getDescription()),
                            activityString });
                }
                emailService.sendEmail(receipients, subject, msgText);
                updateIssueActivityNotification(issue, true);
            }
        }
        catch (java.lang.Exception e) {
            logger.
              error(
                ("handleIssueNotification: unexpected exception caught, throwi" +
                 "ng runtime exception"), e);
            throw new java.lang.RuntimeException(e);
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
    @java.lang.SuppressWarnings("unused") 
    private void handleLocalizedIssueNotification(final org.itracker.model.
                                                    Issue issue,
                                                  final org.itracker.model.
                                                    Notification.Type type,
                                                  final java.lang.String url,
                                                  final javax.mail.internet.
                                                    InternetAddress[] receipients,
                                                  java.lang.
                                                    Integer lastModifiedDays) {
        try {
            if (logger.isDebugEnabled()) {
                logger.
                  debug(
                    ("handleIssueNotificationhandleIssueNotification: running as t" +
                     "hread, called with issue: ") +
                      issue +
                      ", type: " +
                      type +
                      "url: " +
                      url +
                      ", receipients: " +
                      (null ==
                         receipients
                         ? "<null>"
                         : java.lang.String.
                         valueOf(java.util.Arrays.asList(receipients))) +
                      ", lastModifiedDays: " +
                      lastModifiedDays);
            }
            final java.lang.Integer notModifiedSince;
            if (lastModifiedDays == null || lastModifiedDays.intValue() < 0) {
                notModifiedSince =
                  java.
                    lang.
                    Integer.
                    valueOf(
                      org.itracker.web.scheduler.tasks.ReminderNotification.
                        DEFAULT_ISSUE_AGE);
            } else {
                notModifiedSince = lastModifiedDays;
            }
            try {
                if (logger.isDebugEnabled()) {
                    logger.
                      debug(
                        ("handleIssueNotificationhandleIssueNotification.run: running " +
                         "as thread, called with issue: ") +
                          issue +
                          ", type: " +
                          type +
                          "url: " +
                          url +
                          ", receipients: " +
                          (null ==
                             receipients
                             ? "<null>"
                             : java.lang.String.
                             valueOf(java.util.Arrays.asList(receipients))) +
                          ", notModifiedSince: " +
                          notModifiedSince);
                }
                final java.util.List<org.itracker.model.Notification>
                  notifications;
                if (issue == null) {
                    logger.
                      warn(
                        ("handleIssueNotification: issue was null. Notification will n" +
                         "ot be handled"));
                    return;
                }
                java.util.Map<javax.mail.internet.InternetAddress,
                java.util.Locale> localeMapping = null;
                if (receipients == null) {
                    notifications = this.getIssueNotifications(issue);
                    localeMapping =
                      new java.util.Hashtable<javax.mail.internet.
                        InternetAddress,
                      java.util.Locale>(notifications.size());
                    java.util.Iterator<org.itracker.model.Notification> it =
                      notifications.iterator();
                    org.itracker.model.User currentUser;
                    while (it.hasNext()) {
                        currentUser = it.next().getUser();
                        if (null !=
                              currentUser &&
                              null !=
                              currentUser.getEmailAddress() &&
                              null !=
                              currentUser.getEmail() &&
                              !localeMapping.keySet().
                              contains(currentUser.getEmailAddress())) {
                            try {
                                localeMapping.
                                  put(
                                    currentUser.getEmailAddress(),
                                    org.itracker.core.resources.
                                        ITrackerResources.
                                        getLocale(
                                          currentUser.getPreferences(
                                                        ).getUserLocale()));
                            }
                            catch (java.lang.RuntimeException re) {
                                localeMapping.
                                  put(
                                    currentUser.getEmailAddress(),
                                    org.itracker.core.resources.
                                        ITrackerResources.getLocale());
                            }
                        }
                    }
                }
                else {
                    localeMapping =
                      new java.util.Hashtable<javax.mail.internet.
                        InternetAddress, java.util.Locale>(1);
                    java.util.Locale locale =
                      org.itracker.core.resources.ITrackerResources.getLocale();
                    java.util.Iterator<javax.mail.internet.InternetAddress> it =
                      java.util.Arrays.asList(receipients).iterator();
                    while (it.hasNext()) {
                        javax.mail.internet.InternetAddress internetAddress =
                          (javax.mail.internet.InternetAddress) it.next();
                        localeMapping.put(internetAddress, locale);
                    }
                }
                this.handleNotification(issue, type, notModifiedSince,
                                        localeMapping, url);
            }
            catch (java.lang.Exception e) {
                logger.error("run: failed to process notification", e);
            }
        }
        catch (java.lang.Exception e) {
            logger.
              error(
                ("handleIssueNotification: unexpected exception caught, throwi" +
                 "ng runtime exception"), e);
            throw new java.lang.RuntimeException(e);
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
    private void handleNotification(org.itracker.model.Issue issue,
                                    org.itracker.model.Notification.Type type,
                                    java.lang.Integer notModifiedSince,
                                    java.util.Map<javax.mail.internet.
                                      InternetAddress,
                                    java.util.Locale> recipientsLocales,
                                    final java.lang.String url) {
        java.util.Set<javax.mail.internet.InternetAddress> recipients =
          recipientsLocales.keySet();
        java.util.Map<java.util.Locale,
        java.util.Set<javax.mail.internet.InternetAddress>> localeRecipients =
          new java.util.Hashtable<java.util.Locale,
        java.util.Set<javax.mail.internet.InternetAddress>>();
        java.util.List<org.itracker.model.Component> components =
          issue.getComponents();
        java.util.List<org.itracker.model.Version> versions =
          issue.getVersions();
        java.util.List<org.itracker.model.IssueActivity> activity =
          getIssueService().getIssueActivity(issue.getId(), false);
        issue.getActivities();
        java.util.List<org.itracker.model.IssueHistory> histories =
          issue.getHistory();
        java.util.Iterator<org.itracker.model.IssueHistory> it =
          histories.iterator();
        labeled_2 :
        {
            org.itracker.model.IssueHistory history = null;
            org.itracker.model.IssueHistory currentHistory;
            history = getIssueService().getLastIssueHistory(issue.getId());
            java.lang.Integer historyId = 0;
            while (it.hasNext())
            {
                currentHistory = (org.itracker.model.IssueHistory) it.next();
                if (logger.isDebugEnabled())
                {
                    logger.debug("handleIssueNotification: found history: " +
                            currentHistory.getDescription() + " (time: " +
                            currentHistory.getCreateDate());
                }
                if (currentHistory.getId() > historyId)
                {
                    historyId = currentHistory.getId();
                    history = currentHistory;
                }
            }
        }
        if (logger.isDebugEnabled() && null != history) {
            logger.debug("handleIssueNotification: got most recent history: " +
                           history + " (" + history.getDescription() + ")");
        }
        java.util.Iterator<javax.mail.internet.InternetAddress> iaIt =
          recipientsLocales.keySet().iterator();
        while (iaIt.hasNext()) {
            javax.mail.internet.InternetAddress internetAddress =
              (javax.mail.internet.InternetAddress) iaIt.next();
            if (localeRecipients.keySet().
                  contains(recipientsLocales.get(internetAddress))) {
                localeRecipients.get(recipientsLocales.get(internetAddress)).
                  add(internetAddress);
            }
            else {
                java.util.Set<javax.mail.internet.InternetAddress> addresses =
                  new java.util.HashSet<javax.mail.internet.InternetAddress>();
                localeRecipients.put(recipientsLocales.get(internetAddress),
                                     addresses);
            }
        }
        java.util.Iterator<java.util.Locale> localesIt =
          localeRecipients.keySet().iterator();
        try {
            while (localesIt.hasNext()) {
                java.util.Locale currentLocale = (java.util.Locale)
                                                   localesIt.next();
                recipients = localeRecipients.get(currentLocale);
                if (recipients.size() > 0) {
                    java.lang.String subject = "";
                    if (type == org.itracker.model.Notification.Type.CREATED) {
                        subject =
                          org.itracker.core.resources.ITrackerResources.
                            getString("itracker.email.issue.subject.created",
                                      currentLocale,
                                      new java.lang.Object[] { issue.getId(),
                                        issue.getProject().getName(),
                                        notModifiedSince });
                    }
                    else
                        if (type ==
                              org.itracker.model.Notification.Type.ASSIGNED) {
                            subject =
                              org.itracker.core.resources.ITrackerResources.
                                getString(
                                  "itracker.email.issue.subject.assigned",
                                  currentLocale,
                                  new java.lang.Object[] { issue.getId(),
                                    issue.getProject().getName(),
                                    notModifiedSince });
                        }
                        else
                            if (type ==
                                  org.itracker.model.Notification.Type.CLOSED) {
                                subject =
                                  org.itracker.core.resources.ITrackerResources.
                                    getString(
                                      "itracker.email.issue.subject.closed",
                                      currentLocale,
                                      new java.lang.Object[] { issue.getId(),
                                        issue.getProject().getName(),
                                        notModifiedSince });
                            }
                            else
                                if (type ==
                                      org.itracker.model.Notification.Type.
                                        ISSUE_REMINDER) {
                                    subject =
                                      org.itracker.core.resources.
                                        ITrackerResources.
                                        getString(
                                          "itracker.email.issue.subject.reminder",
                                          currentLocale,
                                          new java.lang.Object[] { issue.getId(
                                                                           ),
                                            issue.getProject().getName(),
                                            notModifiedSince });
                                }
                                else {
                                    subject =
                                      org.itracker.core.resources.
                                        ITrackerResources.
                                        getString(
                                          "itracker.email.issue.subject.updated",
                                          currentLocale,
                                          new java.lang.Object[] { issue.getId(
                                                                           ),
                                            issue.getProject().getName(),
                                            notModifiedSince });
                                }
                    java.lang.String activityString;
                    java.lang.String componentString = "";
                    java.lang.String versionString = "";
                    java.lang.StringBuffer sb = new java.lang.StringBuffer();
                    int i = 0;
                    while (i < activity.size()) {
                        sb.
                          append(
                            org.itracker.services.util.IssueUtilities.
                                getActivityName(
                                  activity.get(i).getActivityType(),
                                  currentLocale)).append(": ").
                          append(activity.get(i).getDescription()).append("\n");
                        i++;
                    }
                    activityString = sb.toString();
                    i = 0;
                    while (i < components.size()) {
                        componentString += (i != 0 ? ", " : "") +
                                           components.get(i).getName();
                        i++;
                    }
                    i = 0;
                    while (i < versions.size()) {
                        versionString += (i != 0 ? ", " : "") +
                                         versions.get(i).getNumber();
                        i++;
                    }
                    java.lang.String msgText = "";
                    if (type ==
                          org.itracker.model.Notification.Type.ISSUE_REMINDER) {
                        msgText =
                          org.
                            itracker.
                            core.
                            resources.
                            ITrackerResources.
                            getString(
                              "itracker.email.issue.body.reminder",
                              currentLocale,
                              new java.lang.Object[] { url +
                                "/module-projects/view_issue.do?id=" +
                                issue.
                                  getId(),
                                issue.
                                  getProject().getName(),
                                issue.
                                  getDescription(),
                                org.itracker.services.util.IssueUtilities.
                                  getStatusName(issue.getStatus()),
                                org.itracker.services.util.IssueUtilities.
                                  getSeverityName(issue.getSeverity()),
                                (issue.getOwner().getFirstName() != null
                                   ? issue.getOwner().getFirstName()
                                   : "") +
                                " " +
                                (issue.getOwner().getLastName() != null
                                   ? issue.getOwner().getLastName()
                                   : ""),
                                componentString,
                                history ==
                                  null
                                  ? ""
                                  : history.getUser().getFirstName() +
                                " " +
                                history.getUser().getLastName(),
                                history ==
                                  null
                                  ? ""
                                  : org.itracker.services.util.HTMLUtilities.
                                  removeMarkup(history.getDescription()),
                                notModifiedSince,
                                activityString });
                    }
                    else {
                        java.lang.String resolution =
                          issue.getResolution() ==
                          null
                          ? ""
                          : issue.getResolution();
                        if (!resolution.
                              equals("") &&
                              org.
                              itracker.
                              services.
                              util.
                              ProjectUtilities.
                              hasOption(
                                org.itracker.services.util.ProjectUtilities.
                                  OPTION_PREDEFINED_RESOLUTIONS,
                                issue.getProject().getOptions())) {
                            resolution =
                              org.
                                itracker.
                                services.
                                util.
                                IssueUtilities.
                                getResolutionName(
                                  resolution,
                                  org.itracker.core.resources.ITrackerResources.
                                      getLocale());
                        }
                        msgText =
                          org.
                            itracker.
                            core.
                            resources.
                            ITrackerResources.
                            getString(
                              "itracker.email.issue.body.standard",
                              currentLocale,
                              new java.lang.Object[] { new java.lang.
                                  StringBuffer(
                                  url).append(
                                         "/module-projects/view_issue.do?id=").
                                  append(issue.getId()).toString(),
                                issue.getProject().getName(),
                                issue.getDescription(),
                                org.itracker.services.util.IssueUtilities.
                                  getStatusName(issue.getStatus()),
                                resolution,
                                org.itracker.services.util.IssueUtilities.
                                  getSeverityName(issue.getSeverity()),
                                (null != issue.getOwner() && null !=
                                   issue.getOwner().getFirstName()
                                   ? issue.getOwner().getFirstName()
                                   : "") +
                                " " +
                                (null != issue.getOwner() && null !=
                                   issue.getOwner().getLastName()
                                   ? issue.getOwner().getLastName()
                                   : ""),
                                componentString,
                                history ==
                                  null
                                  ? ""
                                  : history.getUser().getFirstName() +
                                " " +
                                history.getUser().getLastName(),
                                history ==
                                  null
                                  ? ""
                                  : org.itracker.services.util.HTMLUtilities.
                                  removeMarkup(history.getDescription()),
                                activityString });
                    }
                    if (logger.isInfoEnabled()) {
                        logger.
                          info(
                            new java.lang.StringBuilder(
                                "handleNotification: sending notification for ").
                                append(issue).append(" (").append(type).
                                append(") to ").append(currentLocale).
                                append("-users (").append(recipients + ")").
                                toString());
                    }
                    emailService.sendEmail(recipients, subject, msgText);
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                 "handleNotification: sent notification for " +
                                   issue);
                    }
                }
                updateIssueActivityNotification(issue, true);
                if (logger.isDebugEnabled()) {
                    logger.
                      debug(
                        "handleNotification: sent notification for locales " +
                          localeRecipients.keySet() + " recipients: " +
                          localeRecipients.values());
                }
            }
        }
        catch (java.lang.RuntimeException e) {
            logger.error("handleNotification: failed to notify: " + issue +
                           " (locales: " + localeRecipients.keySet() + ")", e);
        }
    }
    
    private org.itracker.services.IssueService getIssueService() {
        return org.itracker.web.util.ServletContextUtils.getItrackerServices().
          getIssueService();
    }
    
    public void updateIssueActivityNotification(org.itracker.model.Issue issue,
                                                java.lang.
                                                  Boolean notificationSent) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateIssueActivityNotification: called with " +
                           issue + ", notificationSent: " + notificationSent);
        }
        java.util.Collection<org.itracker.model.IssueActivity> activity =
          getIssueActivityDao().findByIssueId(issue.getId());
        java.util.Iterator<org.itracker.model.IssueActivity> iter =
          activity.iterator();
        while (iter.hasNext()) {
            ((org.itracker.model.IssueActivity) iter.next()).
              setNotificationSent(notificationSent);
        }
    }
    
    /**
    
     */
    public boolean addIssueNotification(org.itracker.model.
                                          Notification notification) {
        if (logger.isDebugEnabled()) {
            logger.debug("addIssueNotification: called with notification: " +
                           notification);
        }
        org.itracker.model.Issue issue = notification.getIssue();
        if (!issue.getNotifications().contains(notification)) {
            if (notification.getCreateDate() == null) {
                notification.setCreateDate(new java.util.Date());
            }
            if (notification.getLastModifiedDate() == null) {
                notification.setLastModifiedDate(new java.util.Date());
            }
            getNotificationDao().save(notification);
            issue.getNotifications().add(notification);
            getIssueDao().merge(issue);
            return true;
        }
        if (logger.isDebugEnabled()) {
            logger.
              debug(
                ("addIssueNotification: attempted to add duplicate notificatio" +
                 "n ") + notification + " for issue: " + issue);
        }
        return false;
    }
    
    /**
    
     * 
    
     */
    public java.util.List<org.itracker.model.
      Notification> getIssueNotifications(org.itracker.model.Issue issue,
                                          boolean primaryOnly,
                                          boolean activeOnly) {
        if (logger.isDebugEnabled()) {
            logger.debug("getIssueNotifications: called with issue: " + issue +
                           ", primaryOnly: " + primaryOnly + ", activeOnly: " +
                           activeOnly);
        }
        java.util.List<org.itracker.model.Notification> issueNotifications =
          new java.util.ArrayList<org.itracker.model.Notification>();
        if (issue == null) {
            logger.warn("getIssueNotifications: no issue, throwing exception");
            throw new java.lang.IllegalArgumentException(
              "issue must not be null");
        }
        if (!primaryOnly) {
            labeled_3 :
            {
                java.util.List<org.itracker.model.Notification> notifications =
                        getNotificationDao().findByIssueId(issue.getId());
                java.util.Iterator<org.itracker.model.Notification> iterator =
                        notifications.iterator();
                while (iterator.hasNext())
                {
                    org.itracker.model.Notification notification = iterator.next();
                    org.itracker.model.User notificationUser =
                            notification.getUser();
                    if (!activeOnly || notificationUser.getStatus() ==
                            org.itracker.services.util.UserUtilities.STATUS_ACTIVE)
                    {
                        issueNotifications.add(notification);
                    }
                }
            }
        }
        boolean hasOwner = false;
        if (issue != null) {
            if (issue.getOwner() != null) {
                org.itracker.model.User ownerModel = issue.getOwner();
                if (ownerModel !=
                      null &&
                      (!activeOnly ||
                         ownerModel.
                         getStatus() ==
                         org.itracker.services.util.UserUtilities.
                           STATUS_ACTIVE)) {
                    issueNotifications.
                      add(
                        new org.itracker.model.Notification(
                            ownerModel, issue,
                            org.itracker.model.Notification.Role.OWNER));
                    hasOwner = true;
                }
            }
            if (!primaryOnly || !hasOwner) {
                org.itracker.model.User creatorModel = issue.getCreator();
                if (creatorModel !=
                      null &&
                      (!activeOnly ||
                         creatorModel.
                         getStatus() ==
                         org.itracker.services.util.UserUtilities.
                           STATUS_ACTIVE)) {
                    issueNotifications.
                      add(
                        new org.itracker.model.Notification(
                            creatorModel, issue,
                            org.itracker.model.Notification.Role.CREATOR));
                }
            }
            org.itracker.model.Project project =
              getProjectService().getProject(issue.getProject().getId());
            java.util.Collection<org.itracker.model.User> projectOwners =
              project.getOwners();
            java.util.Iterator<org.itracker.model.User> iterator =
              projectOwners.iterator();
            while (iterator.hasNext()) {
                org.itracker.model.User projectOwner = (org.itracker.model.User)
                                                         iterator.next();
                if (projectOwner !=
                      null &&
                      (!activeOnly ||
                         projectOwner.
                         getStatus() ==
                         org.itracker.services.util.UserUtilities.
                           STATUS_ACTIVE)) {
                    issueNotifications.
                      add(
                        new org.itracker.model.Notification(
                            projectOwner, issue,
                            org.itracker.model.Notification.Role.PO));
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getIssueNotifications: returning " +
                           issueNotifications);
        }
        return issueNotifications;
    }
    
    public java.util.List<org.itracker.model.
      Notification> getIssueNotifications(org.itracker.model.Issue issue) {
        if (logger.isDebugEnabled()) {
            logger.debug("getIssueNotifications: called with: " + issue);
        }
        return this.getIssueNotifications(issue, false, true);
    }
    
    public java.
      util.
      List<org.
      itracker.
      model.
      Notification> getPrimaryIssueNotifications(org.itracker.model.
                                                   Issue issue) {
        if (logger.isDebugEnabled()) {
            logger.debug("getPrimaryIssueNotifications: called with: " + issue);
        }
        return this.getIssueNotifications(issue, true, false);
    }
    
    public boolean hasIssueNotification(org.itracker.model.Issue issue,
                                        java.lang.Integer userId) {
        if (logger.isDebugEnabled()) {
            logger.debug("hasIssueNotification: called with: " + issue +
                           ", userId: " + userId);
        }
        return hasIssueNotification(issue, userId,
                                    org.itracker.model.Notification.Role.ANY);
    }
    
    /**
    
     * @param issueId
    
     * @param userId
    
     * @param role
    
     * @return
    
     */
    public boolean hasIssueNotification(org.itracker.model.Issue issue,
                                        java.lang.Integer userId,
                                        org.itracker.model.Notification.
                                          Role role) {
        labeled_4 :
        {
            boolean result = false;
            if (issue != null && userId != null)
            {
                java.util.List<org.itracker.model.Notification> notifications =
                        getIssueNotifications(issue, false, false);
                int i = 0;
                while (i < notifications.size())
                {
                    if (role == org.itracker.model.Notification.Role.ANY ||
                            notifications.get(i).getRole() == role)
                    {
                        if (notifications.get(i).getUser().getId().equals(userId))
                        {
                            result = true;
                        }
                    }
                    i++;
                }
            }
            result = false;
        }
            return result;
    }
    
    public boolean removeIssueNotification(java.lang.Integer notificationId) {
        org.itracker.model.Notification notification =
          this.getNotificationDao().findById(notificationId);
        getNotificationDao().delete(notification);
        return true;
    }
    
    public void sendNotification(org.itracker.model.Issue issue,
                                 org.itracker.model.Notification.Type type,
                                 java.lang.String baseURL,
                                 javax.mail.internet.
                                   InternetAddress[] receipients,
                                 java.lang.Integer lastModifiedDays) {
        this.handleIssueNotification(issue, type, baseURL, receipients,
                                     lastModifiedDays);
    }
    
    /**
    
     * @return the emailService
    
     */
    public org.itracker.services.util.EmailService getEmailService() {
        return emailService;
    }
    
    /**
    
     * @return the notificationDao
    
     */
    private org.itracker.persistence.dao.NotificationDAO getNotificationDao() {
        return notificationDao;
    }
    
    /**
    
     * @return the projectService
    
     */
    public org.itracker.services.ProjectService getProjectService() {
        return projectService;
    }
    
    /**
    
     * @param projectService
    
     *            the projectService to set
    
     */
    public void setProjectService(org.itracker.services.
                                    ProjectService projectService) {
        this.projectService = projectService;
    }
    
    /**
    
     * @param notificationDao
    
     *            the notificationDao to set
    
     */
    public void setNotificationDao(org.itracker.persistence.dao.
                                     NotificationDAO notificationDao) {
        if (null == notificationDao) {
            throw new java.lang.IllegalArgumentException(
              "notification dao must not be null");
        }
        if (null != this.notificationDao) {
            throw new java.lang.IllegalStateException(
              "notification dao allready set");
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
    public void sendNotification(java.lang.Integer notificationId,
                                 org.itracker.model.Notification.Type type,
                                 java.lang.String url) {
        org.itracker.model.Notification notification =
          notificationDao.findById(notificationId);
        this.sendNotification(notification, type, url);
    }
    
    /**
    
     * @return the issueActivityDao
    
     */
    public org.itracker.persistence.dao.IssueActivityDAO getIssueActivityDao() {
        return issueActivityDao;
    }
    
    /**
    
     * @param issueActivityDao
    
     *            the issueActivityDao to set
    
     */
    public void setIssueActivityDao(org.itracker.persistence.dao.
                                      IssueActivityDAO issueActivityDao) {
        this.issueActivityDao = issueActivityDao;
    }
    
    /**
    
     * @return the issueDao
    
     */
    public org.itracker.persistence.dao.IssueDAO getIssueDao() {
        return issueDao;
    }
    
    /**
    
     * @param issueDao
    
     *            the issueDao to set
    
     */
    public void setIssueDao(org.itracker.persistence.dao.IssueDAO issueDao) {
        this.issueDao = issueDao;
    }
}
