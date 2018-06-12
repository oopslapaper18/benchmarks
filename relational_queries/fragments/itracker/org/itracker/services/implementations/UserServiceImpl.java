package org.
  itracker.
  services.
  implementations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.itracker.model.Issue;
import org.itracker.model.Permission;
import org.itracker.model.PermissionType;
import org.itracker.model.Project;
import org.itracker.model.User;
import org.itracker.model.UserPreferences;
import org.itracker.persistence.dao.NoSuchEntityException;
import org.itracker.persistence.dao.PermissionDAO;
import org.itracker.persistence.dao.ProjectDAO;
import org.itracker.persistence.dao.ReportDAO;
import org.itracker.persistence.dao.UserDAO;
import org.itracker.persistence.dao.UserPreferencesDAO;
import org.itracker.services.ConfigurationService;
import org.itracker.services.ProjectService;
import org.itracker.services.UserService;
import org.itracker.services.authentication.PluggableAuthenticator;
import org.itracker.services.exceptions.AuthenticatorException;
import org.itracker.services.exceptions.PasswordException;
import org.itracker.services.exceptions.UserException;
import org.itracker.services.util.AuthenticationConstants;
import org.itracker.services.util.ProjectUtilities;
import org.itracker.services.util.UserUtilities;

/**
 *
 Implements
 the
 UserService
 interface.
 See
 that
 interface
 for
 method
 *
 descriptions.
 *
 *
 @see UserService
 */
public class UserServiceImpl implements org.
  itracker.
  services.
  UserService {
    private static final java.lang.String DEFAULT_AUTHENTICATOR =
      "org.itracker.services.authentication.DefaultAuthenticator";
    private java.lang.String authenticatorClassName = null;
    private java.lang.Class<?> authenticatorClass = null;
    private boolean allowSelfRegister = false;
    private static final org.apache.log4j.Logger logger = null;
    private org.itracker.persistence.dao.PermissionDAO permissionDAO = null;
    private org.itracker.persistence.dao.UserDAO userDAO = null;
    private org.itracker.persistence.dao.UserPreferencesDAO userPreferencesDAO =
      null;
    private org.itracker.services.ProjectService projectService;
    private org.itracker.services.ConfigurationService configurationService;
    
    /**
     *
     @param
     configurationService
     *
     @param
     projectService
     *
     @param
     userDAO
     *
     @param
     permissionDAO
     *
     @param userPreferencesDAO
     */
    public UserServiceImpl(org.itracker.services.
                             ConfigurationService configurationService,
                           org.itracker.services.ProjectService projectService,
                           org.itracker.persistence.dao.UserDAO userDAO,
                           org.itracker.persistence.dao.
                             PermissionDAO permissionDAO,
                           org.itracker.persistence.dao.
                             UserPreferencesDAO userPreferencesDAO) {
        super();
        this.configurationService = configurationService;
        this.projectService = projectService;
        this.userDAO = userDAO;
        this.userPreferencesDAO = userPreferencesDAO;
        this.permissionDAO = permissionDAO;
        try {
            allowSelfRegister =
              configurationService.getBooleanProperty("allow_self_register",
                                                      false);
            authenticatorClassName =
              configurationService.getProperty("authenticator_class",
                                               DEFAULT_AUTHENTICATOR);
            authenticatorClass =
              java.lang.Class.forName(authenticatorClassName);
        }
        catch (java.lang.ClassNotFoundException ex) {
            throw new java.lang.RuntimeException(ex);
        }
    }
    
    /**
     * @deprecated use constructor without projectDA= und reportDAO instead
     * @param configurationService
     * @param projectService
     * @param userDAO
     * @param projectDAO
     * @param reportDAO
     * @param permissionDAO
     * @param userPreferencesDAO
     */
    public UserServiceImpl(org.itracker.services.
                             ConfigurationService configurationService,
                           org.itracker.services.ProjectService projectService,
                           org.itracker.persistence.dao.UserDAO userDAO,
                           org.itracker.persistence.dao.ProjectDAO projectDAO,
                           org.itracker.persistence.dao.ReportDAO reportDAO,
                           org.itracker.persistence.dao.
                             PermissionDAO permissionDAO,
                           org.itracker.persistence.dao.
                             UserPreferencesDAO userPreferencesDAO) {
        this(configurationService, projectService, userDAO, permissionDAO,
             userPreferencesDAO);
    }
    
    public org.itracker.model.User getUser(java.lang.Integer userId) {
        org.itracker.model.User user = userDAO.findByPrimaryKey(userId);
        return user;
    }
    
    public org.itracker.model.User getUserByLogin(java.lang.String login)
          throws org.itracker.persistence.dao.NoSuchEntityException {
        org.itracker.model.User user = userDAO.findByLogin(login);
        if (user == null)
            throw new org.itracker.persistence.dao.NoSuchEntityException(
              "User " + login + " not found.");
        return user;
    }
    
    public java.lang.String getUserPasswordByLogin(java.lang.String login) {
        org.itracker.model.User user = userDAO.findByLogin(login);
        return user.getPassword();
    }
    
    public java.util.List<org.itracker.model.User> getAllUsers() {
        java.util.List<org.itracker.model.User> users = userDAO.findAll();
        return users;
    }
    
    public int getNumberUsers() {
        labeled_1 :
        {
            java.util.Collection<org.itracker.model.User> users = userDAO.findAll();
            int size = users.size();
        }
        return size;
    }
    
    public java.util.List<org.itracker.model.User> getActiveUsers() {
        java.util.List<org.itracker.model.User> users = userDAO.findActive();
        return users;
    }
    
    public java.util.List<org.itracker.model.User> getSuperUsers() {
        java.util.List<org.itracker.model.User> superUsers =
          userDAO.findSuperUsers();
        return superUsers;
    }
    
    public org.itracker.model.User createUser(org.itracker.model.User user)
          throws org.itracker.services.exceptions.UserException {
        try {
            if (user == null || user.getLogin() == null ||
                  user.getLogin().equals("")) {
                throw new org.itracker.services.exceptions.UserException(
                  "User data was null, or login was empty.");
            }
            try {
                this.getUserByLogin(user.getLogin());
                throw new org.itracker.services.exceptions.UserException(
                  "User already exists with login: " + user.getLogin());
            }
            catch (org.itracker.persistence.dao.NoSuchEntityException e) {  }
            try {
                org.itracker.services.authentication.PluggableAuthenticator
                  authenticator =
                  (org.itracker.services.authentication.PluggableAuthenticator)
                    authenticatorClass.newInstance();
                if (authenticator != null) {
                    java.util.HashMap<java.lang.String,
                    java.lang.Object> values =
                      new java.util.HashMap<java.lang.String,
                    java.lang.Object>();
                    values.put("userService", this);
                    values.put("configurationService", configurationService);
                    authenticator.initialize(values);
                    authenticator.
                      createProfile(
                        user,
                        null,
                        org.itracker.services.util.AuthenticationConstants.
                          AUTH_TYPE_UNKNOWN,
                        org.itracker.services.util.AuthenticationConstants.
                          REQ_SOURCE_UNKNOWN);
                }
                else {
                    throw new org.
                      itracker.
                      services.
                      exceptions.
                      AuthenticatorException(
                      "Unable to create new authenticator.",
                      org.itracker.services.exceptions.AuthenticatorException.
                        SYSTEM_ERROR);
                }
            }
            catch (java.lang.IllegalAccessException ex) {
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  "Authenticator class " +
                    authenticatorClassName + " can not be instantiated.",
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR, ex);
            }
            catch (java.lang.InstantiationException ex) {
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  "Authenticator class " +
                    authenticatorClassName + " can not be instantiated.",
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR, ex);
            }
            catch (java.lang.ClassCastException ex) {
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  "Authenticator class " +
                    authenticatorClassName +
                    " does not extend the PluggableAuthenticator class.",
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR, ex);
            }
            user.setStatus(
                   org.itracker.services.util.UserUtilities.STATUS_ACTIVE);
            user.setRegistrationType(user.getRegistrationType());
            userDAO.save(user);
            return user;
        }
        catch (org.itracker.services.exceptions.AuthenticatorException ex) {
            throw new org.itracker.services.exceptions.UserException(
              "Could not create user.", ex);
        }
    }
    
    public org.itracker.model.User updateUser(org.itracker.model.User user)
          throws org.itracker.services.exceptions.UserException {
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.HashMap<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                authenticator.
                  updateProfile(
                    user,
                    org.itracker.services.util.AuthenticationConstants.
                      UPDATE_TYPE_CORE,
                    null,
                    org.itracker.services.util.AuthenticationConstants.
                      AUTH_TYPE_UNKNOWN,
                    org.itracker.services.util.AuthenticationConstants.
                      REQ_SOURCE_UNKNOWN);
            }
            else {
                logger.
                  warn(
                    ("updateUser: no authenticator, throwing AuthenticatorExceptio" +
                     "n"));
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  "Unable to create new authenticator.",
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR);
            }
        }
        catch (java.lang.IllegalAccessException ex) {
            logger.
              error(
                ("updateUser: IllegalAccessException caught, throwing Authenti" +
                 "catorException"), ex);
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              "Authenticator class " +
                authenticatorClassName + " can not be instantiated.",
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR, ex);
        }
        catch (java.lang.InstantiationException ex) {
            logger.
              error(
                ("updateUser: InstantiationException caught, throwing Authenti" +
                 "catorException"), ex);
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              "Authenticator class " +
                authenticatorClassName + " can not be instantiated.",
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR, ex);
        }
        catch (java.lang.ClassCastException ex) {
            logger.
              error(
                ("updateUser: ClassCastException caught, throwing Authenticato" +
                 "rException"), ex);
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              "Authenticator class " +
                authenticatorClassName +
                " does not extend the PluggableAuthenticator class.",
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR, ex);
        }
        catch (org.itracker.services.exceptions.AuthenticatorException ex) {
            logger.
              error(
                ("updateUser: AuthenticatorException caught, throwing Authenti" +
                 "catorException"), ex);
            throw new org.itracker.services.exceptions.UserException(
              "Unable to update user.", ex);
        }
        java.lang.Integer id = user.getId();
        userDAO.detach(user);
        org.itracker.model.User existinguser = userDAO.findByPrimaryKey(id);
        userDAO.refresh(existinguser);
        existinguser.setLogin(user.getLogin());
        existinguser.setFirstName(user.getFirstName());
        existinguser.setLastName(user.getLastName());
        existinguser.setEmail(user.getEmail());
        existinguser.setSuperUser(user.isSuperUser());
        existinguser.setStatus(user.getStatus());
        if (user.getPassword() != null && !user.getPassword().equals("")) {
            if (logger.isInfoEnabled()) {
                logger.info("updateUser: setting new password for " +
                              user.getLogin());
            }
            existinguser.setPassword(user.getPassword());
        }
        userDAO.saveOrUpdate(existinguser);
        return existinguser;
    }
    
    public java.lang.String generateUserPassword(org.itracker.model.User user)
          throws org.itracker.services.exceptions.PasswordException {
        java.lang.String password =
          org.itracker.services.util.UserUtilities.generatePassword();
        user.setPassword(
               org.itracker.services.util.UserUtilities.encryptPassword(
                                                          password));
        return password;
    }
    
    public org.
      itracker.
      model.
      UserPreferences updateUserPreferences(org.itracker.model.
                                              UserPreferences userPrefs)
          throws org.itracker.services.exceptions.UserException {
        org.itracker.model.UserPreferences newUserPrefs =
          new org.itracker.model.UserPreferences();
        try {
            org.itracker.model.User user = userPrefs.getUser();
            newUserPrefs = userPreferencesDAO.findByUserId(user.getId());
            if (newUserPrefs == null) {
                newUserPrefs = new org.itracker.model.UserPreferences();
            }
            newUserPrefs.setSaveLogin(userPrefs.getSaveLogin());
            newUserPrefs.setUserLocale(userPrefs.getUserLocale());
            newUserPrefs.setNumItemsOnIndex(userPrefs.getNumItemsOnIndex());
            newUserPrefs.setNumItemsOnIssueList(
                           userPrefs.getNumItemsOnIssueList());
            newUserPrefs.setShowClosedOnIssueList(
                           userPrefs.getShowClosedOnIssueList());
            newUserPrefs.setSortColumnOnIssueList(
                           userPrefs.getSortColumnOnIssueList());
            newUserPrefs.setHiddenIndexSections(
                           userPrefs.getHiddenIndexSections());
            newUserPrefs.setRememberLastSearch(
                           userPrefs.getRememberLastSearch());
            newUserPrefs.setUseTextActions(userPrefs.getUseTextActions());
            newUserPrefs.setUser(user);
            if (userPrefs.isNew()) {
                newUserPrefs.setCreateDate(new java.util.Date());
                newUserPrefs.setLastModifiedDate(userPrefs.getCreateDate());
                user.setPreferences(newUserPrefs);
                userDAO.saveOrUpdate(user);
            } else {
                this.userPreferencesDAO.saveOrUpdate(newUserPrefs);
                newUserPrefs = userPreferencesDAO.findByUserId(user.getId());
                user.setUserPreferences(newUserPrefs);
            }
            try {
                org.itracker.services.authentication.PluggableAuthenticator
                  authenticator =
                  (org.itracker.services.authentication.PluggableAuthenticator)
                    authenticatorClass.newInstance();
                if (authenticator != null) {
                    java.util.HashMap<java.lang.String,
                    java.lang.Object> values =
                      new java.util.HashMap<java.lang.String,
                    java.lang.Object>();
                    values.put("userService", this);
                    values.put("configurationService", configurationService);
                    authenticator.initialize(values);
                    authenticator.
                      updateProfile(
                        user,
                        org.itracker.services.util.AuthenticationConstants.
                          UPDATE_TYPE_PREFERENCE,
                        null,
                        org.itracker.services.util.AuthenticationConstants.
                          AUTH_TYPE_UNKNOWN,
                        org.itracker.services.util.AuthenticationConstants.
                          REQ_SOURCE_UNKNOWN);
                }
                else {
                    throw new org.
                      itracker.
                      services.
                      exceptions.
                      AuthenticatorException(
                      "Unable to create new authenticator.",
                      org.itracker.services.exceptions.AuthenticatorException.
                        SYSTEM_ERROR);
                }
            }
            catch (java.lang.IllegalAccessException ex) {
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  "Authenticator class " +
                    authenticatorClassName + " can not be instantiated.",
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR, ex);
            }
            catch (java.lang.InstantiationException ex) {
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  "Authenticator class " +
                    authenticatorClassName + " can not be instantiated.",
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR, ex);
            }
            catch (java.lang.ClassCastException ex) {
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  "Authenticator class " +
                    authenticatorClassName +
                    " does not extend the PluggableAuthenticator class.",
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR, ex);
            }
            if (newUserPrefs != null) return newUserPrefs;
        }
        catch (org.itracker.services.exceptions.AuthenticatorException ex) {
            throw new org.itracker.services.exceptions.UserException(
              "Unable to create new preferences.", ex);
        }
        return userPrefs;
    }
    
    public void clearOwnedProjects(org.itracker.model.User user) {
        user.getProjects().clear();
        userDAO.save(user);
    }
    
    public java.
      util.
      List<org.
      itracker.
      model.
      User> findUsersForProjectByPermissionTypeList(java.lang.Integer projectID,
                                                    java.lang.
                                                      Integer[] permissionTypes) {
        return userDAO.findUsersForProjectByAllPermissionTypeList(
                         projectID, permissionTypes);
    }
    
    public java.util.List<org.itracker.model.
      User> getUsersWithPermissionLocal(java.lang.Integer projectId,
                                        int permissionType) {
        labeled_2 :
        {
            java.util.List<org.itracker.model.User> users =
                    new java.util.ArrayList<org.itracker.model.User>();
            if (projectId != null)
            {
                java.util.List<org.itracker.model.Permission> permissions =
                        permissionDAO.findByProjectIdAndPermission(projectId,
                                permissionType);
                for (org.itracker.model.Permission permission : permissions)
                {
                    users.add(permission.getUser());
                }
            }
        }
        return users;
    }
    
    public java.util.List<org.itracker.model.
      Permission> getUserPermissionsLocal(org.itracker.model.User user) {
        java.util.List<org.itracker.model.Permission> permissions =
          permissionDAO.findByUserId(user.getId());
        return permissions;
    }
    
    public java.util.List<org.itracker.model.
      Permission> getPermissionsByUserId(java.lang.Integer userId) {
        java.util.List<org.itracker.model.Permission> permissions =
          new java.util.ArrayList<org.itracker.model.Permission>();
        org.itracker.model.User user = getUser(userId);
        if (user != null) {
            try {
                org.itracker.services.authentication.PluggableAuthenticator
                  authenticator =
                  (org.itracker.services.authentication.PluggableAuthenticator)
                    authenticatorClass.newInstance();
                if (authenticator != null) {
                    java.util.HashMap<java.lang.String,
                    java.lang.Object> values =
                      new java.util.HashMap<java.lang.String,
                    java.lang.Object>();
                    values.put("userService", this);
                    values.put("configurationService", configurationService);
                    authenticator.initialize(values);
                    permissions =
                      authenticator.
                        getUserPermissions(
                          user,
                          org.itracker.services.util.AuthenticationConstants.
                            REQ_SOURCE_UNKNOWN);
                }
                logger.debug("Found " + permissions.size() +
                               " permissions for user " + user.getLogin());
            }
            catch (java.lang.IllegalAccessException ex) {
                throw new java.lang.RuntimeException(
                  "Authenticator class " + authenticatorClassName +
                    " can not be instantiated.", ex);
            }
            catch (java.lang.InstantiationException ex) {
                throw new java.lang.RuntimeException(
                  "Authenticator class " + authenticatorClassName +
                    " can not be instantiated.", ex);
            }
            catch (java.lang.ClassCastException ex) {
                throw new java.lang.RuntimeException(
                  "Authenticator class " + authenticatorClassName +
                    " does not extend the PluggableAuthenticator class.", ex);
            }
            catch (org.itracker.services.exceptions.AuthenticatorException ex) {
                throw new java.lang.RuntimeException(
                  "Authenticator exception: ", ex);
            }
        }
        return permissions;
    }
    
    public boolean updateAuthenticator(java.lang.Integer userId,
                                       java.util.List<org.itracker.model.
                                         Permission> permissions) {
        boolean successful = false;
        try {
            org.itracker.model.User user = userDAO.findByPrimaryKey(userId);
            user.getPermissions().addAll(permissions);
            try {
                org.itracker.services.authentication.PluggableAuthenticator
                  authenticator =
                  (org.itracker.services.authentication.PluggableAuthenticator)
                    authenticatorClass.newInstance();
                if (authenticator != null) {
                    java.util.HashMap<java.lang.String,
                    java.lang.Object> values =
                      new java.util.HashMap<java.lang.String,
                    java.lang.Object>();
                    values.put("userService", this);
                    values.put("configurationService", configurationService);
                    authenticator.initialize(values);
                    if (authenticator.
                          updateProfile(
                            user,
                            org.itracker.services.util.AuthenticationConstants.
                              UPDATE_TYPE_PERMISSION_SET,
                            null,
                            org.itracker.services.util.AuthenticationConstants.
                              AUTH_TYPE_UNKNOWN,
                            org.itracker.services.util.AuthenticationConstants.
                              REQ_SOURCE_UNKNOWN)) {  }
                }
                else {
                    logger.error("Unable to create new authenticator.");
                    throw new org.
                      itracker.
                      services.
                      exceptions.
                      AuthenticatorException(
                      org.itracker.services.exceptions.AuthenticatorException.
                        SYSTEM_ERROR);
                }
                successful = true;
            }
            catch (java.lang.IllegalAccessException iae) {
                logger.error("Authenticator class " + authenticatorClassName +
                               " can not be instantiated.");
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR);
            }
            catch (java.lang.InstantiationException ie) {
                logger.error("Authenticator class " + authenticatorClassName +
                               " can not be instantiated.");
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR);
            }
            catch (java.lang.ClassCastException cce) {
                logger.
                  error("Authenticator class " + authenticatorClassName +
                          " does not extend the PluggableAuthenticator class.");
                throw new org.
                  itracker.
                  services.
                  exceptions.
                  AuthenticatorException(
                  org.itracker.services.exceptions.AuthenticatorException.
                    SYSTEM_ERROR);
            }
        }
        catch (org.itracker.services.exceptions.AuthenticatorException ae) {
            logger.warn("Error setting user (" + userId +
                          ") permissions.  AuthenticatorException.", ae);
            successful = false;
        }
        return successful;
    }
    
    public boolean addUserPermissions(java.lang.Integer userId,
                                      java.util.List<org.itracker.model.
                                        Permission> newPermissions) {
        boolean successful = false;
        if (newPermissions == null || newPermissions.size() == 0) {
            return successful;
        }
        try {
            newPermissions.addAll(getUserPermissionsLocal(getUser(userId)));
            setUserPermissions(userId, newPermissions);
            successful = true;
        }
        catch (org.itracker.services.exceptions.AuthenticatorException ae) {
            logger.warn("Error setting user (" + userId +
                          ") permissions.  AuthenticatorException.", ae);
            successful = false;
        }
        return successful;
    }
    
    /**
     * private util for collection searching (contains)
     */
    private static final org.
      itracker.
      model.
      Permission find(java.util.Collection<org.itracker.model.
                        Permission> permissions,
                      org.itracker.model.Permission permission) {
        java.util.Iterator<org.itracker.model.Permission> permssionsIt =
          permissions.iterator();
        while (permssionsIt.hasNext()) {
            org.itracker.model.Permission permission2 =
              (org.itracker.model.Permission) permssionsIt.next();
            if (org.itracker.model.Permission.PERMISSION_PROPERTIES_COMPARATOR.
                  compare(permission, permission2) == 0) { return permission2; }
        }
        return null;
    }
    
    /**
     * @param userId - id of update-user
     * @param newPermissions - set of new permissions for this user
     */
    public boolean setUserPermissions(final java.lang.Integer userId,
                                      final java.util.List<org.itracker.model.
                                        Permission> newPermissions) {
        boolean hasChanges = false;
        java.util.TreeSet<org.itracker.model.Permission> pSet =
          new java.util.TreeSet<org.itracker.model.Permission>(
          org.itracker.model.Permission.PERMISSION_PROPERTIES_COMPARATOR);
        pSet.addAll(newPermissions);
        org.itracker.model.User usermodel = this.getUser(userId);
        java.util.Set<org.itracker.model.Permission> current =
          new java.util.TreeSet<org.itracker.model.Permission>(
          org.itracker.model.Permission.PERMISSION_PROPERTIES_COMPARATOR);
        current.addAll(usermodel.getPermissions());
        java.util.Set<org.itracker.model.Permission> remove =
          new java.util.TreeSet<org.itracker.model.Permission>(
          org.itracker.model.Permission.PERMISSION_PROPERTIES_COMPARATOR);
        remove.addAll(current);
        remove.removeAll(pSet);
        java.util.Set<org.itracker.model.Permission> add =
          new java.util.TreeSet<org.itracker.model.Permission>(
          org.itracker.model.Permission.PERMISSION_PROPERTIES_COMPARATOR);
        add.addAll(pSet);
        add.removeAll(current);
        org.itracker.model.Permission p;
        java.util.Iterator<org.itracker.model.Permission> pIt =
          remove.iterator();
        boolean skip_0 = false;
        while (pIt.hasNext()) {
            skip_0 = false;
            p = find(usermodel.getPermissions(),
                     (org.itracker.model.Permission) pIt.next());
            if (!skip_0) if (null == p) { skip_0 = true; }
            if (!skip_0)
                if (usermodel.getPermissions().contains(p)) {
                    if (!skip_0) usermodel.getPermissions().remove(p);
                    if (!skip_0) permissionDAO.delete(p);
                    if (!skip_0) hasChanges = true;
                }
        }
        pIt = add.iterator();
        while (pIt.hasNext()) {
            p = pIt.next();
            if (null == find(usermodel.getPermissions(), p) &&
                  !usermodel.getPermissions().contains(p)) {
                p.setUser(usermodel);
                usermodel.getPermissions().add(p);
                permissionDAO.save(p);
                hasChanges = true;
            }
        }
        if (hasChanges) { userDAO.saveOrUpdate(usermodel); }
        return hasChanges;
    }
    
    public boolean removeUserPermissions(java.lang.Integer userId,
                                         java.util.List<org.itracker.model.
                                           Permission> newPermissions) {
        boolean successful = false;
        if (newPermissions == null || newPermissions.size() == 0) {
            return successful;
        }
        try {
            java.util.Iterator<org.itracker.model.Permission> delIterator =
              newPermissions.iterator();
            while (delIterator.hasNext()) {
                org.itracker.model.Permission permission =
                  (org.itracker.model.Permission) delIterator.next();
                permissionDAO.delete(permission);
            }
            successful = true;
        }
        catch (org.itracker.services.exceptions.AuthenticatorException ae) {
            logger.warn("Error setting user (" + userId +
                          ") permissions.  AuthenticatorException.", ae);
            successful = false;
        }
        return successful;
    }
    
    @java.lang.Deprecated
    public java.
      util.
      Map<java.
      lang.
      Integer,
    java.
      util.
      Set<org.
      itracker.
      model.
      PermissionType>> getUsersMapOfProjectIdsAndSetOfPermissionTypes(org.
                                                                        itracker.
                                                                        model.
                                                                        User user,
                                                                      int reqSource) {
        java.util.Map<java.lang.Integer,
        java.util.Set<org.itracker.model.PermissionType>> permissionsMap =
          new java.util.HashMap<java.lang.Integer,
        java.util.Set<org.itracker.model.PermissionType>>();
        if (user == null) { return permissionsMap; }
        java.util.List<org.itracker.model.Permission> permissionList =
          new java.util.ArrayList<org.itracker.model.Permission>();
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.HashMap<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                permissionList =
                  authenticator.
                    getUserPermissions(
                      user,
                      reqSource ==
                          0
                          ? org.itracker.services.util.AuthenticationConstants.
                              REQ_SOURCE_UNKNOWN
                          : reqSource);
            }
            logger.debug("Found " + permissionList.size() +
                           " permissions for user " + user.getLogin());
        }
        catch (java.lang.IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
        }
        catch (java.lang.InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
        }
        catch (java.lang.ClassCastException cce) {
            logger.error(
                     "Authenticator class " + authenticatorClassName +
                       " does not extend the PluggableAuthenticator class.");
        }
        catch (org.itracker.services.exceptions.AuthenticatorException ae) {
            logger.error("Authenticator exception: " + ae.getMessage());
            logger.debug("Authenticator exception: ", ae);
        }
        permissionsMap =
          org.itracker.services.util.UserUtilities.
            mapPermissionTypesByProjectId(permissionList);
        if (allowSelfRegister) {
            java.util.List<org.itracker.model.Project> projects =
              projectService.getAllProjects();
            int i = 0;
            while (i < projects.size()) {
                org.itracker.model.Project project = projects.get(i);
                if (project.
                      getOptions() >=
                      org.itracker.services.util.ProjectUtilities.
                        OPTION_ALLOW_SELF_REGISTERED_CREATE) {
                    java.util.Set<org.itracker.model.PermissionType>
                      projectPermissions = permissionsMap.get(project.getId());
                    if (projectPermissions == null) {
                        projectPermissions =
                          new java.util.HashSet<org.itracker.model.
                            PermissionType>();
                        permissionsMap.put(project.getId(), projectPermissions);
                    }
                    if (org.
                          itracker.
                          services.
                          util.
                          ProjectUtilities.
                          hasOption(
                            org.itracker.services.util.ProjectUtilities.
                              OPTION_ALLOW_SELF_REGISTERED_CREATE,
                            project.getOptions())) {
                        projectPermissions.
                          add(
                            org.itracker.model.PermissionType.ISSUE_VIEW_USERS);
                        projectPermissions.
                          add(org.itracker.model.PermissionType.ISSUE_CREATE);
                    }
                    if (org.
                          itracker.
                          services.
                          util.
                          ProjectUtilities.
                          hasOption(
                            org.itracker.services.util.ProjectUtilities.
                              OPTION_ALLOW_SELF_REGISTERED_VIEW_ALL,
                            project.getOptions())) {
                        projectPermissions.
                          add(org.itracker.model.PermissionType.ISSUE_VIEW_ALL);
                    }
                }
                i++;
            }
        }
        return permissionsMap;
    }
    
    public java.util.List<org.itracker.model.
      User> getUsersWithProjectPermission(java.lang.Integer projectId,
                                          int permissionType) {
        return getUsersWithProjectPermission(projectId, permissionType, true);
    }
    
    public java.util.List<org.itracker.model.
      User> getUsersWithProjectPermission(java.lang.Integer projectId,
                                          int permissionType,
                                          boolean activeOnly) {
        return getUsersWithAnyProjectPermission(projectId,
                                                new int[] { permissionType },
                                                activeOnly);
    }
    
    public java.util.List<org.itracker.model.
      User> getUsersWithAnyProjectPermission(java.lang.Integer projectId,
                                             int[] permissionTypes) {
        return getUsersWithAnyProjectPermission(projectId, permissionTypes,
                                                true);
    }
    
    public java.
      util.
      Collection<org.
      itracker.
      model.
      User> getUsersWithAnyProjectPermission(java.lang.Integer projectId,
                                             java.lang.
                                               Integer[] permissionTypes) {
        int[] perm = new int[permissionTypes.length];
        int i = 0;
        while (i < permissionTypes.length) {
            perm[i] = permissionTypes[i];
            i++;
        }
        return getUsersWithAnyProjectPermission(projectId, perm, true);
    }
    
    public java.util.List<org.itracker.model.
      User> getUsersWithAnyProjectPermission(java.lang.Integer projectId,
                                             int[] permissionTypes,
                                             boolean activeOnly) {
        return getUsersWithProjectPermission(projectId, permissionTypes, false,
                                             activeOnly);
    }
    
    public java.util.List<org.itracker.model.
      User> getUsersWithProjectPermission(java.lang.Integer projectId,
                                          int[] permissionTypes,
                                          boolean requireAll,
                                          boolean activeOnly) {
        java.util.List<org.itracker.model.User> userList =
          new java.util.ArrayList<org.itracker.model.User>();
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.Map<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                userList =
                  authenticator.
                    getUsersWithProjectPermission(
                      projectId,
                      permissionTypes,
                      requireAll,
                      activeOnly,
                      org.itracker.services.util.AuthenticationConstants.
                        REQ_SOURCE_UNKNOWN);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("getUsersWithProjectPermission: Found " +
                               userList.size() + " users with project " +
                               projectId + " permissions " +
                               java.util.Arrays.toString(permissionTypes) +
                               (requireAll ? "[AllReq," : "[AnyReq,") +
                               (activeOnly ? "ActiveUsersOnly]" : "AllUsers]"));
            }
        }
        catch (java.lang.IllegalAccessException iae) {
            logger.error("getUsersWithProjectPermission: Authenticator class " +
                           authenticatorClassName + " can not be instantiated.",
                         iae);
        }
        catch (java.lang.InstantiationException ie) {
            logger.error("getUsersWithProjectPermission: Authenticator class " +
                           authenticatorClassName + " can not be instantiated.",
                         ie);
        }
        catch (java.lang.ClassCastException cce) {
            logger.error("getUsersWithProjectPermission: Authenticator class " +
                           authenticatorClassName +
                           " does not extend the PluggableAuthenticator class.",
                         cce);
        }
        catch (org.itracker.services.exceptions.AuthenticatorException ae) {
            logger.
              error(
                ("getUsersWithProjectPermission: Authenticator exception caugh" +
                 "t."), ae);
        }
        return userList;
    }
    
    public java.util.List<org.itracker.model.
      User> getPossibleOwners(org.itracker.model.Issue issue,
                              java.lang.Integer projectId,
                              java.lang.Integer userId) {
        java.util.HashSet<org.itracker.model.User> users =
          new java.util.HashSet<org.itracker.model.User>();
        java.util.List<org.itracker.model.User> editUsers =
          getUsersWithProjectPermission(
            projectId, org.itracker.services.util.UserUtilities.PERMISSION_EDIT,
            true);
        int i = 0;
        while (i < editUsers.size()) {
            users.add(editUsers.get(i));
            i++;
        }
        java.
          util.
          List<org.
          itracker.
          model.
          User>
          otherUsers =
          getUsersWithProjectPermission(
            projectId,
            new int[] { org.itracker.services.util.UserUtilities.
                          PERMISSION_EDIT_USERS,
              org.itracker.services.util.UserUtilities.PERMISSION_ASSIGNABLE },
            true, true);
        i = 0;
        while (i < otherUsers.size()) {
            users.add(otherUsers.get(i));
            i++;
        }
        if (issue != null) {
            org.itracker.model.User creator = issue.getCreator();
            if (org.
                  itracker.
                  services.
                  util.
                  UserUtilities.
                  hasPermission(
                    getUsersMapOfProjectIdsAndSetOfPermissionTypes(
                      creator, 0),
                    projectId,
                    org.itracker.services.util.UserUtilities.
                      PERMISSION_EDIT_USERS)) { users.add(creator); }
            if (issue.getOwner() != null) {
                org.itracker.model.User owner = issue.getOwner();
                users.add(owner);
            }
        }
        else
            if (userId != null) {
                org.itracker.model.User creator =
                  getUser(userId);
                if (org.
                      itracker.
                      services.
                      util.
                      UserUtilities.
                      hasPermission(
                        getUsersMapOfProjectIdsAndSetOfPermissionTypes(
                          creator, 0),
                        projectId,
                        org.itracker.services.util.UserUtilities.
                          PERMISSION_EDIT_USERS)) { users.add(creator); }
            }
        int j = 0;
        java.util.List<org.itracker.model.User> userList =
          new java.util.ArrayList<org.itracker.model.User>();
        java.util.Iterator<org.itracker.model.User> iter = users.iterator();
        while (iter.hasNext()) {
            userList.add((org.itracker.model.User) iter.next());
            j++;
        }
        return userList;
    }
    
    public org.itracker.model.User checkLogin(java.lang.String login,
                                              java.lang.Object authentication,
                                              int authType, int reqSource)
          throws org.itracker.services.exceptions.AuthenticatorException {
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.HashMap<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.
                  checkLogin(
                    login,
                    authentication,
                    authType,
                    reqSource ==
                        0
                        ? org.itracker.services.util.AuthenticationConstants.
                            REQ_SOURCE_UNKNOWN
                        : reqSource);
            }
            logger.error("Unable to create new authenticator.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.ClassCastException cce) {
            logger.error(
                     "Authenticator class " + authenticatorClassName +
                       " does not extend the PluggableAuthenticator class.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
    }
    
    public boolean allowRegistration(org.itracker.model.User user,
                                     java.lang.Object authentication,
                                     int authType, int reqSource)
          throws org.itracker.services.exceptions.AuthenticatorException {
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.HashMap<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                if (authenticator.
                      allowProfileCreation(
                        user,
                        authentication,
                        authType,
                        reqSource ==
                            0
                            ? org.itracker.services.util.
                                AuthenticationConstants.REQ_SOURCE_UNKNOWN
                            : reqSource)) {
                    return authenticator.
                      allowRegistration(
                        user,
                        authentication,
                        authType,
                        reqSource ==
                            0
                            ? org.itracker.services.util.
                                AuthenticationConstants.REQ_SOURCE_UNKNOWN
                            : reqSource);
                }
                return false;
            }
            logger.error("Unable to create new authenticator.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.ClassCastException cce) {
            logger.error(
                     "Authenticator class " + authenticatorClassName +
                       " does not extend the PluggableAuthenticator class.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
    }
    
    public boolean allowProfileCreation(org.itracker.model.User user,
                                        java.lang.Object authentication,
                                        int authType, int reqSource)
          throws org.itracker.services.exceptions.AuthenticatorException {
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.HashMap<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.
                  allowProfileCreation(
                    user,
                    authentication,
                    authType,
                    reqSource ==
                        0
                        ? org.itracker.services.util.AuthenticationConstants.
                            REQ_SOURCE_UNKNOWN
                        : reqSource);
            }
            logger.error("Unable to create new authenticator.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.ClassCastException cce) {
            logger.error(
                     "Authenticator class " + authenticatorClassName +
                       " does not extend the PluggableAuthenticator class.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
    }
    
    public boolean allowProfileUpdates(org.itracker.model.User user,
                                       java.lang.Object authentication,
                                       int authType, int reqSource)
          throws org.itracker.services.exceptions.AuthenticatorException {
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.HashMap<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.
                  allowProfileUpdates(
                    user,
                    authentication,
                    authType,
                    reqSource ==
                        0
                        ? org.itracker.services.util.AuthenticationConstants.
                            REQ_SOURCE_UNKNOWN
                        : reqSource);
            }
            logger.error("Unable to create new authenticator.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.ClassCastException cce) {
            logger.error(
                     "Authenticator class " + authenticatorClassName +
                       " does not extend the PluggableAuthenticator class.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
    }
    
    public boolean allowPasswordUpdates(org.itracker.model.User user,
                                        java.lang.Object authentication,
                                        int authType, int reqSource)
          throws org.itracker.services.exceptions.AuthenticatorException {
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.HashMap<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.
                  allowPasswordUpdates(
                    user,
                    authentication,
                    authType,
                    reqSource ==
                        0
                        ? org.itracker.services.util.AuthenticationConstants.
                            REQ_SOURCE_UNKNOWN
                        : reqSource);
            }
            logger.error("Unable to create new authenticator.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.ClassCastException cce) {
            logger.error(
                     "Authenticator class " + authenticatorClassName +
                       " does not extend the PluggableAuthenticator class.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
    }
    
    public boolean allowPermissionUpdates(org.itracker.model.User user,
                                          java.lang.Object authentication,
                                          int authType, int reqSource)
          throws org.itracker.services.exceptions.AuthenticatorException {
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.HashMap<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.
                  allowPermissionUpdates(
                    user,
                    authentication,
                    authType,
                    reqSource ==
                        0
                        ? org.itracker.services.util.AuthenticationConstants.
                            REQ_SOURCE_UNKNOWN
                        : reqSource);
            }
            logger.error("Unable to create new authenticator.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.ClassCastException cce) {
            logger.error(
                     "Authenticator class " + authenticatorClassName +
                       " does not extend the PluggableAuthenticator class.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
    }
    
    public boolean allowPreferenceUpdates(org.itracker.model.User user,
                                          java.lang.Object authentication,
                                          int authType, int reqSource)
          throws org.itracker.services.exceptions.AuthenticatorException {
        try {
            org.itracker.services.authentication.PluggableAuthenticator
              authenticator =
              (org.itracker.services.authentication.PluggableAuthenticator)
                authenticatorClass.newInstance();
            if (authenticator != null) {
                java.util.HashMap<java.lang.String,
                java.lang.Object> values =
                  new java.util.HashMap<java.lang.String, java.lang.Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.
                  allowPreferenceUpdates(
                    user,
                    authentication,
                    authType,
                    reqSource ==
                        0
                        ? org.itracker.services.util.AuthenticationConstants.
                            REQ_SOURCE_UNKNOWN
                        : reqSource);
            }
            logger.error("Unable to create new authenticator.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName +
                           " can not be instantiated.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
        catch (java.lang.ClassCastException cce) {
            logger.error(
                     "Authenticator class " + authenticatorClassName +
                       " does not extend the PluggableAuthenticator class.");
            throw new org.
              itracker.
              services.
              exceptions.
              AuthenticatorException(
              org.itracker.services.exceptions.AuthenticatorException.
                SYSTEM_ERROR);
        }
    }
}

