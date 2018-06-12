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
 * Implements the UserService interface. See that interface for method
 * descriptions.
 *
 * @see UserService
 */
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_AUTHENTICATOR =
            "org.itracker.services.authentication.DefaultAuthenticator";

    
    
    private String authenticatorClassName = null;
    private Class<?> authenticatorClass = null;
    private boolean allowSelfRegister = false;

    private static final Logger logger = null; //Logger.getLogger(UserServiceImpl.class);

    private PermissionDAO permissionDAO = null;

    private UserDAO userDAO = null;
    private UserPreferencesDAO userPreferencesDAO = null;
    private ProjectService projectService;
    private ConfigurationService configurationService;
    
    /**
     * @param configurationService
     * @param projectService
     * @param userDAO
     * @param permissionDAO
     * @param userPreferencesDAO
     */
    public UserServiceImpl(ConfigurationService configurationService,
            ProjectService projectService,
            UserDAO userDAO,
            PermissionDAO permissionDAO,
            UserPreferencesDAO userPreferencesDAO) {
    	
        
        this.configurationService = configurationService;
        this.projectService = projectService;
        this.userDAO = userDAO;
        this.userPreferencesDAO = userPreferencesDAO;
        this.permissionDAO = permissionDAO;

        try {
            allowSelfRegister = configurationService.getBooleanProperty("allow_self_register", false);

            authenticatorClassName = configurationService.getProperty("authenticator_class", DEFAULT_AUTHENTICATOR);
            authenticatorClass = Class.forName(authenticatorClassName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
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
    public UserServiceImpl(ConfigurationService configurationService,
                           ProjectService projectService,
                           UserDAO userDAO,
                           ProjectDAO projectDAO,
                           ReportDAO reportDAO,
                           PermissionDAO permissionDAO,
                           UserPreferencesDAO userPreferencesDAO) {
    	this(configurationService, projectService, userDAO, permissionDAO, userPreferencesDAO);
    }

    public User getUser(Integer userId) {
        User user = userDAO.findByPrimaryKey(userId);
        return user;
    }

    public User getUserByLogin(String login) throws NoSuchEntityException {
        User user = userDAO.findByLogin(login);
        if (user == null)
            throw new NoSuchEntityException("User " + login + " not found.");
        return user;
    }

    public String getUserPasswordByLogin(String login) {
        User user = userDAO.findByLogin(login);
        return user.getPassword();
    }

    public List<User> getAllUsers() {
        List<User> users = userDAO.findAll();

        return users;
    }

    public int getNumberUsers() {
        Collection<User> users = userDAO.findAll();
        return users.size();
    }

    public List<User> getActiveUsers() {
        List<User> users = userDAO.findActive();

        return users;
    }

    public List<User> getSuperUsers() {
        List<User> superUsers = userDAO.findSuperUsers();
        return superUsers;
    }

    /*public boolean isSuperUser(User user) {
        if(user == null) {
            return false;
        }
        
        // Super user has access to all projects, which is indicated by null.
        List<User> users = userDAO.findSuperUsers();
        
        if(users.contains(user)) {
            return true;
        } else {
            return false; }
        
    }*/

    /*
     * accessible from User
     * 
     public UserPreferences  ferencesByUserId(Integer userId) {
        
        UserPreferences userPrefs = userPreferencesDAO.findByUserId(userId);
        if (userPrefs == null)
            return new UserPreferences();
        
        return userPrefs;
    }*/

    public User createUser(User user) throws UserException {
        try {
            if (user == null || user.getLogin() == null || user.getLogin().equals("")) {
                throw new UserException("User data was null, or login was empty.");
            }

            try {
                this.getUserByLogin(user.getLogin());
                throw new UserException("User already exists with login: " + user.getLogin());
            } catch (NoSuchEntityException e) {
                // doesn't exist, we'll create him
            }

            try {
                PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
                if (authenticator != null) {
                    HashMap<String, Object> values = new HashMap<String, Object>();
                    values.put("userService", this);
                    values.put("configurationService", configurationService);
                    authenticator.initialize(values);
                    authenticator.createProfile(user, null, AuthenticationConstants.AUTH_TYPE_UNKNOWN,
                            AuthenticationConstants.REQ_SOURCE_UNKNOWN);
                } else {
                    throw new AuthenticatorException("Unable to create new authenticator.", AuthenticatorException.SYSTEM_ERROR);
                }
            } catch (IllegalAccessException ex) {
                throw new AuthenticatorException(
                        "Authenticator class " + authenticatorClassName + " can not be instantiated.",
                        AuthenticatorException.SYSTEM_ERROR, ex);
            } catch (InstantiationException ex) {
                throw new AuthenticatorException(
                        "Authenticator class " + authenticatorClassName + " can not be instantiated.",
                        AuthenticatorException.SYSTEM_ERROR, ex);
            } catch (ClassCastException ex) {
                throw new AuthenticatorException("Authenticator class " + authenticatorClassName
                        + " does not extend the PluggableAuthenticator class.",
                        AuthenticatorException.SYSTEM_ERROR, ex);
            }
            user.setStatus(UserUtilities.STATUS_ACTIVE);
            user.setRegistrationType(user.getRegistrationType());
//            user.setCreateDate(new Date());
//            user.setLastModifiedDate(user.getCreateDate());
            userDAO.save(user);
            return user;
        } catch (AuthenticatorException ex) {
            throw new UserException("Could not create user.", ex);
        }

    }

    public User updateUser(User user) throws UserException {
        try {
            PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
            if (authenticator != null) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                authenticator.updateProfile(user, AuthenticationConstants.UPDATE_TYPE_CORE, null,
                        AuthenticationConstants.AUTH_TYPE_UNKNOWN, AuthenticationConstants.REQ_SOURCE_UNKNOWN);
            } else {
            	logger.warn("updateUser: no authenticator, throwing AuthenticatorException");
                throw new AuthenticatorException("Unable to create new authenticator.",
                        AuthenticatorException.SYSTEM_ERROR);
            }
        } catch (IllegalAccessException ex) {
        	logger.error("updateUser: IllegalAccessException caught, throwing AuthenticatorException", ex);
            throw new AuthenticatorException(
                    "Authenticator class " + authenticatorClassName + " can not be instantiated.",
                    AuthenticatorException.SYSTEM_ERROR, ex);
        } catch (InstantiationException ex) {
        	logger.error("updateUser: InstantiationException caught, throwing AuthenticatorException", ex);
            throw new AuthenticatorException(
                    "Authenticator class " + authenticatorClassName + " can not be instantiated.",
                    AuthenticatorException.SYSTEM_ERROR, ex);
        } catch (ClassCastException ex) {
        	logger.error("updateUser: ClassCastException caught, throwing AuthenticatorException", ex);
            throw new AuthenticatorException(
                    "Authenticator class " + authenticatorClassName
                            + " does not extend the PluggableAuthenticator class.",
                    AuthenticatorException.SYSTEM_ERROR, ex);
        } catch (AuthenticatorException ex) {
        	logger.error("updateUser: AuthenticatorException caught, throwing AuthenticatorException", ex);
            throw new UserException("Unable to update user.", ex);
        }
        
        // detach, so we can compare the new loaded with changed user
        Integer id = user.getId();
        userDAO.detach(user);
        
        User existinguser = userDAO.findByPrimaryKey(id);
        userDAO.refresh(existinguser);
        
        existinguser.setLogin(user.getLogin());
        existinguser.setFirstName(user.getFirstName());
        existinguser.setLastName(user.getLastName());
        existinguser.setEmail(user.getEmail());
        existinguser.setSuperUser(user.isSuperUser());
        
        existinguser.setStatus(user.getStatus());
        
//        existinguser.setLastModifiedDate(new Timestamp(new Date().getTime()));

//        // Only set the password if it is a new value...
        if (user.getPassword() != null && (!user.getPassword().equals(""))) {
//                && (!user.getPassword().equals(user.getPassword()))) {
        	if (logger.isInfoEnabled()) {
        		logger.info("updateUser: setting new password for " + user.getLogin());
        	}
            existinguser.setPassword(user.getPassword());
        }

        userDAO.saveOrUpdate(existinguser);
        
//        user = userDAO.findByPrimaryKey(id);
//        userDAO.refresh(user);
        return existinguser;
    }

    public String generateUserPassword(User user) throws PasswordException {
        String password = UserUtilities.generatePassword();
        user.setPassword(UserUtilities.encryptPassword(password));
        return password;
        // throw new PasswordException(PasswordException.UNKNOWN_USER);
    }

    public UserPreferences updateUserPreferences(UserPreferences userPrefs) throws UserException {
        UserPreferences newUserPrefs = new UserPreferences();

        try {
            User user = userPrefs.getUser();

            newUserPrefs = userPreferencesDAO.findByUserId(user.getId());

            if (newUserPrefs == null) {
                newUserPrefs = new UserPreferences();
            }
            newUserPrefs.setSaveLogin(userPrefs.getSaveLogin());
            newUserPrefs.setUserLocale(userPrefs.getUserLocale());
            newUserPrefs.setNumItemsOnIndex(userPrefs.getNumItemsOnIndex());
            newUserPrefs.setNumItemsOnIssueList(userPrefs.getNumItemsOnIssueList());
            newUserPrefs.setShowClosedOnIssueList(userPrefs.getShowClosedOnIssueList());
            newUserPrefs.setSortColumnOnIssueList(userPrefs.getSortColumnOnIssueList());
            newUserPrefs.setHiddenIndexSections(userPrefs.getHiddenIndexSections());

            newUserPrefs.setRememberLastSearch(userPrefs.getRememberLastSearch());
            newUserPrefs.setUseTextActions(userPrefs.getUseTextActions());

            // FIXME: it's a bad one-to-one reference, has to be set on both ends. Fix mappings in hibernate.
            newUserPrefs.setUser(user);

            if (userPrefs.isNew()) {
                newUserPrefs.setCreateDate(new Date());
                newUserPrefs.setLastModifiedDate(userPrefs.getCreateDate());
                
                // first time create UserPreferences
                user.setPreferences(newUserPrefs);
                userDAO.saveOrUpdate(user);
            } else {
            	this.userPreferencesDAO.saveOrUpdate(newUserPrefs);
            	newUserPrefs = userPreferencesDAO.findByUserId(user.getId());
            	user.setUserPreferences(newUserPrefs);
            }          

            try {
                PluggableAuthenticator authenticator =
                        (PluggableAuthenticator) authenticatorClass.newInstance();

                if (authenticator != null) {
                    HashMap<String, Object> values = new HashMap<String, Object>();
                    values.put("userService", this);
                    values.put("configurationService", configurationService);
                    authenticator.initialize(values);
                    authenticator.updateProfile(user, AuthenticationConstants.UPDATE_TYPE_PREFERENCE, null,
                            AuthenticationConstants.AUTH_TYPE_UNKNOWN, AuthenticationConstants.REQ_SOURCE_UNKNOWN);
                } else {
                    throw new AuthenticatorException("Unable to create new authenticator.",
                            AuthenticatorException.SYSTEM_ERROR);
                }
            } catch (IllegalAccessException ex) {
                throw new AuthenticatorException(
                        "Authenticator class " + authenticatorClassName + " can not be instantiated.",
                        AuthenticatorException.SYSTEM_ERROR, ex);
            } catch (InstantiationException ex) {
                throw new AuthenticatorException(
                        "Authenticator class " + authenticatorClassName + " can not be instantiated.",
                        AuthenticatorException.SYSTEM_ERROR, ex);
            } catch (ClassCastException ex) {
                throw new AuthenticatorException(
                        "Authenticator class " + authenticatorClassName
                                + " does not extend the PluggableAuthenticator class.",
                        AuthenticatorException.SYSTEM_ERROR, ex);
            }

            if (newUserPrefs != null)
                return newUserPrefs;

        } catch (AuthenticatorException ex) {
            throw new UserException("Unable to create new preferences.", ex);
        }
//        } finally {
        return userPrefs;
//        }
    }

    public void clearOwnedProjects(User user) {
        user.getProjects().clear();
        userDAO.save(user);
    }

    public List<User> findUsersForProjectByPermissionTypeList(Integer projectID, Integer[] permissionTypes) {
        return userDAO.findUsersForProjectByAllPermissionTypeList(projectID, permissionTypes);
    }

    public List<User> getUsersWithPermissionLocal(Integer projectId, int permissionType) {

        List<User> users = new ArrayList<User>();

        if (projectId != null) {
            List<Permission> permissions = permissionDAO.findByProjectIdAndPermission(
                    projectId, permissionType);

            for (Permission permission : permissions) {
                users.add(permission.getUser());
            }

        }

        return users;

    }

    public List<Permission> getUserPermissionsLocal(User user) {
        List<Permission> permissions = permissionDAO.findByUserId(user.getId());
        return permissions;
    }

    public List<Permission> getPermissionsByUserId(Integer userId) {
        List<Permission> permissions = new ArrayList<Permission>();

        User user = getUser(userId);
        if (user != null) {
            try {
                PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
                if (authenticator != null) {
                    HashMap<String, Object> values = new HashMap<String, Object>();
                    values.put("userService", this);
                    values.put("configurationService", configurationService);
                    authenticator.initialize(values);
                    permissions = authenticator.getUserPermissions(user, AuthenticationConstants.REQ_SOURCE_UNKNOWN);
                }
                logger.debug("Found " + permissions.size() + " permissions for user " + user.getLogin());
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Authenticator class "
                        + authenticatorClassName + " can not be instantiated.", ex);
            } catch (InstantiationException ex) {
                throw new RuntimeException("Authenticator class "
                        + authenticatorClassName + " can not be instantiated.", ex);
            } catch (ClassCastException ex) {
                throw new RuntimeException("Authenticator class " + authenticatorClassName
                        + " does not extend the PluggableAuthenticator class.", ex);
            } catch (AuthenticatorException ex) {
                throw new RuntimeException("Authenticator exception: ", ex);
            }
        }
        return permissions;
    }

    public boolean updateAuthenticator(Integer userId, List<Permission> permissions) {
        boolean successful = false;

        try {
            User user = userDAO.findByPrimaryKey(userId);
            user.getPermissions().addAll(permissions);
//            user.setPermissions(permissions);
            try {
                PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
                if (authenticator != null) {
                    HashMap<String, Object> values = new HashMap<String, Object>();
                    values.put("userService", this);
                    values.put("configurationService", configurationService);
                    authenticator.initialize(values);
                    if (authenticator
                            .updateProfile(user, AuthenticationConstants.UPDATE_TYPE_PERMISSION_SET, null,
                                    AuthenticationConstants.AUTH_TYPE_UNKNOWN,
                                    AuthenticationConstants.REQ_SOURCE_UNKNOWN)) {
//                        permissions = user.getPermissions();
                    }
                } else {
                    logger.error("Unable to create new authenticator.");
                    throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
                }
                successful = true;
            } catch (IllegalAccessException iae) {
                logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
                throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
            } catch (InstantiationException ie) {
                logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
                throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
            } catch (ClassCastException cce) {
                logger.error("Authenticator class " + authenticatorClassName
                        + " does not extend the PluggableAuthenticator class.");
                throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
            }

        } catch (AuthenticatorException ae) {
            logger.warn("Error setting user (" + userId + ") permissions.  AuthenticatorException.", ae);
            successful = false;
        }

        return successful;
    }

    public boolean addUserPermissions(Integer userId, List<Permission> newPermissions) {
        boolean successful = false;
        if (newPermissions == null || newPermissions.size() == 0) {
            return successful;
        }

        try {
        	newPermissions.addAll(getUserPermissionsLocal(getUser(userId)));
            setUserPermissions(userId, newPermissions);
            successful = true;
        } catch (AuthenticatorException ae) {
            logger.warn("Error setting user (" + userId + ") permissions.  AuthenticatorException.", ae);
            successful = false;
        }

        return successful;
    }
    
    /**
     * private util for collection searching (contains)
     */
    private static final Permission find(Collection<Permission> permissions, Permission permission) {
    	
    	Iterator<Permission> permssionsIt = permissions.iterator();
    	while (permssionsIt.hasNext()) {
			Permission permission2 = (Permission) permssionsIt.next();
			if (Permission.PERMISSION_PROPERTIES_COMPARATOR.compare(permission, permission2) == 0) {
				// found in list, return the found object
				return permission2;
			}
		}
    	return null;
    }
    
    /**
     * @param userId - id of update-user
     * @param newPermissions - set of new permissions for this user
     */
    public boolean setUserPermissions(final Integer userId, final List<Permission> newPermissions) {

        boolean hasChanges = false;
        // rewriting this method
        
        TreeSet<Permission> pSet = new TreeSet<Permission>(Permission.PERMISSION_PROPERTIES_COMPARATOR);
        pSet.addAll(newPermissions);
        

        User usermodel = this.getUser(userId);
        
        Set<Permission> current = new TreeSet<Permission>(Permission.PERMISSION_PROPERTIES_COMPARATOR);
        
        current.addAll(usermodel.getPermissions());
        
        // setup permissions to be removed
        Set<Permission> remove = new TreeSet<Permission>(Permission.PERMISSION_PROPERTIES_COMPARATOR);
        remove.addAll(current);
        remove.removeAll(pSet);
        // setup permissions to be added
        Set<Permission> add = new TreeSet<Permission>(Permission.PERMISSION_PROPERTIES_COMPARATOR);
        add.addAll(pSet);
        add.removeAll(current);

        // look permission
        Permission p;
        Iterator<Permission> pIt = remove.iterator();
        while (pIt.hasNext()) {
			p = find(usermodel.getPermissions(), (Permission) pIt.next());
			if (null == p) {
				continue;
			}
			if (usermodel.getPermissions().contains(p)) {
				usermodel.getPermissions().remove(p);
				permissionDAO.delete(p);
				hasChanges = true;
			}
		}
        
        pIt = add.iterator();
        while (pIt.hasNext()) {
			p = pIt.next();
			if (null == find(usermodel.getPermissions(), p) && !usermodel.getPermissions().contains(p)) {
				p.setUser(usermodel);
				usermodel.getPermissions().add(p);
				permissionDAO.save(p);
				hasChanges = true;
			}
		}
        
        if (hasChanges) {
        	userDAO.saveOrUpdate(usermodel);
        }
        
        return hasChanges;
    }

    public boolean removeUserPermissions(Integer userId, List<Permission> newPermissions) {
        boolean successful = false;
        if (newPermissions == null || newPermissions.size() == 0) {
            return successful;
        }

        try {
            for (Iterator<Permission> delIterator = newPermissions.iterator(); delIterator.hasNext();) {
                Permission permission = (Permission) delIterator.next();
                permissionDAO.delete(permission);
            }
            
            successful = true;
            
        } catch (AuthenticatorException ae) {
            logger.warn("Error setting user (" + userId + ") permissions.  AuthenticatorException.", ae);
            successful = false;
        }

        return successful;
    }

    @Deprecated
    public Map<Integer, Set<PermissionType>> getUsersMapOfProjectIdsAndSetOfPermissionTypes(User user, int reqSource) {
        Map<Integer, Set<PermissionType>> permissionsMap = new HashMap<Integer, Set<PermissionType>>();

        if (user == null) {
            return permissionsMap;
        }

        List<Permission> permissionList = new ArrayList<Permission>();

        try {
            PluggableAuthenticator authenticator =
                    (PluggableAuthenticator) authenticatorClass.newInstance();

            if (authenticator != null) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                permissionList = authenticator.getUserPermissions(user, (reqSource == 0 ? AuthenticationConstants.REQ_SOURCE_UNKNOWN : reqSource));
            }
            logger.debug("Found " + permissionList.size() + " permissions for user " + user.getLogin());
        } catch (IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
        } catch (InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
        } catch (ClassCastException cce) {
            logger.error("Authenticator class " + authenticatorClassName
                    + " does not extend the PluggableAuthenticator class.");
        } catch (AuthenticatorException ae) {
            logger.error("Authenticator exception: " + ae.getMessage());
            logger.debug("Authenticator exception: ", ae);
        }

        permissionsMap = UserUtilities.mapPermissionTypesByProjectId(permissionList);

        if (allowSelfRegister) {
            List<Project> projects = projectService.getAllProjects();

            for (int i = 0; i < projects.size(); i++) {
                Project project = projects.get(i);

                if (project.getOptions() >= ProjectUtilities.OPTION_ALLOW_SELF_REGISTERED_CREATE) {
                    Set<PermissionType> projectPermissions = permissionsMap.get(project.getId());

                    if (projectPermissions == null) {
                        projectPermissions = new HashSet<PermissionType>();
                        permissionsMap.put(project.getId(), projectPermissions);
                    }

                    if (ProjectUtilities.hasOption(ProjectUtilities.OPTION_ALLOW_SELF_REGISTERED_CREATE, project.getOptions())) {
                        projectPermissions.add(PermissionType.ISSUE_VIEW_USERS);
                        projectPermissions.add(PermissionType.ISSUE_CREATE);
                    }

                    if (ProjectUtilities.hasOption(ProjectUtilities.OPTION_ALLOW_SELF_REGISTERED_VIEW_ALL, project.getOptions())) {
                        projectPermissions.add(PermissionType.ISSUE_VIEW_ALL);
                    }
                }
            }
        }

        return permissionsMap;
    }

    public List<User> getUsersWithProjectPermission(Integer projectId, int permissionType) {
        return getUsersWithProjectPermission(projectId, permissionType, true);
    }

    public List<User> getUsersWithProjectPermission(Integer projectId, int permissionType, boolean activeOnly) {
        return getUsersWithAnyProjectPermission(projectId, new int[]{permissionType}, activeOnly);
    }

    public List<User> getUsersWithAnyProjectPermission(Integer projectId, int[] permissionTypes) {
        return getUsersWithAnyProjectPermission(projectId, permissionTypes, true);
    }
    public Collection<User> getUsersWithAnyProjectPermission(Integer projectId, Integer[] permissionTypes) {
    	int[] perm = new int[permissionTypes.length];
    	
    	for (int i = 0; i < permissionTypes.length; i++) {
			perm[i] = permissionTypes[i];
		}

        return getUsersWithAnyProjectPermission(projectId, perm, true);
    }

    public List<User> getUsersWithAnyProjectPermission(Integer projectId, int[] permissionTypes, boolean activeOnly) {
        return getUsersWithProjectPermission(projectId, permissionTypes, false, activeOnly);
    }

    public List<User> getUsersWithProjectPermission(Integer projectId, int[] permissionTypes, boolean requireAll,
                                                    boolean activeOnly) {
        List<User> userList = new ArrayList<User>();

        try {
            // TODO: use a factory to hide this. 
            PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();

            if (authenticator != null) {
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);

                userList = authenticator.getUsersWithProjectPermission(projectId, permissionTypes, requireAll, activeOnly,
                        AuthenticationConstants.REQ_SOURCE_UNKNOWN);
                
            }

            if (logger.isDebugEnabled()) {
                logger.debug("getUsersWithProjectPermission: Found " + userList.size() + " users with project " + projectId + " permissions "
                        + Arrays.toString(permissionTypes) + (requireAll ? "[AllReq," : "[AnyReq,")
                        + (activeOnly ? "ActiveUsersOnly]" : "AllUsers]"));
            }

            // TODO : don't swallow exceptions!! MUST be propagated to the caller!!
        } catch (IllegalAccessException iae) {
            logger.error("getUsersWithProjectPermission: Authenticator class " + authenticatorClassName + " can not be instantiated.", iae);
        } catch (InstantiationException ie) {
            logger.error("getUsersWithProjectPermission: Authenticator class " + authenticatorClassName + " can not be instantiated.", ie);
        } catch (ClassCastException cce) {
            logger.error("getUsersWithProjectPermission: Authenticator class " + authenticatorClassName
                    + " does not extend the PluggableAuthenticator class.", cce);
        } catch (AuthenticatorException ae) {
            logger.error("getUsersWithProjectPermission: Authenticator exception caught.", ae);
        }

        return userList;
    }

    public List<User> getPossibleOwners(Issue issue, Integer projectId, Integer userId) {
        HashSet<User> users = new HashSet<User>();

        List<User> editUsers = getUsersWithProjectPermission(projectId, UserUtilities.PERMISSION_EDIT, true);
        for (int i = 0; i < editUsers.size(); i++) {
            users.add(editUsers.get(i));
        }
        List<User> otherUsers = getUsersWithProjectPermission(projectId, new int[]{UserUtilities.PERMISSION_EDIT_USERS, UserUtilities.PERMISSION_ASSIGNABLE}, true, true);
        for (int i = 0; i < otherUsers.size(); i++) {
            users.add(otherUsers.get(i));
        }

        if (issue != null) {
            // Now add in the creator if the have edit own issues, and always
            // the owner
            User creator = issue.getCreator();

            if (UserUtilities.hasPermission(getUsersMapOfProjectIdsAndSetOfPermissionTypes(creator, 0), projectId,
                    UserUtilities.PERMISSION_EDIT_USERS)) {
                users.add(creator);
            }
            if (issue.getOwner() != null) {
                User owner = issue.getOwner();
                users.add(owner);
            }
        } else if (userId != null) {
            // New issue, so add in the creator if needed
            User creator = getUser(userId);
            if (UserUtilities.hasPermission(getUsersMapOfProjectIdsAndSetOfPermissionTypes(creator, 0), projectId,
                    UserUtilities.PERMISSION_EDIT_USERS)) {
                users.add(creator);
            }
        }

        int j = 0;
        List<User> userList = new ArrayList<User>();
        for (Iterator<User> iter = users.iterator(); iter.hasNext(); j++) {
            userList.add((User) iter.next());
        }
        return userList;
    }

    public User checkLogin(String login, Object authentication, int authType, int reqSource)
            throws AuthenticatorException {
        try {
            PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
            if (authenticator != null) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.checkLogin(login, authentication, authType,
                        (reqSource == 0 ? AuthenticationConstants.REQ_SOURCE_UNKNOWN : reqSource));
            }

            logger.error("Unable to create new authenticator.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (ClassCastException cce) {
            logger.error("Authenticator class " + authenticatorClassName
                    + " does not extend the PluggableAuthenticator class.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        }
    }

    public boolean allowRegistration(User user, Object authentication, int authType, int reqSource)
            throws AuthenticatorException {
        try {
            PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
            if (authenticator != null) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                if (authenticator.allowProfileCreation(user, authentication, authType,
                        (reqSource == 0 ? AuthenticationConstants.REQ_SOURCE_UNKNOWN : reqSource))) {
                    return authenticator.allowRegistration(user, authentication, authType,
                            (reqSource == 0 ? AuthenticationConstants.REQ_SOURCE_UNKNOWN : reqSource));
                }
                return false;
            }

            logger.error("Unable to create new authenticator.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (ClassCastException cce) {
            logger.error("Authenticator class " + authenticatorClassName
                    + " does not extend the PluggableAuthenticator class.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        }
    }

    public boolean allowProfileCreation(User user, Object authentication, int authType, int reqSource)
            throws AuthenticatorException {
        try {
            PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
            if (authenticator != null) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.allowProfileCreation(user, authentication, authType,
                        (reqSource == 0 ? AuthenticationConstants.REQ_SOURCE_UNKNOWN : reqSource));
            }

            logger.error("Unable to create new authenticator.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (ClassCastException cce) {
            logger.error("Authenticator class " + authenticatorClassName
                    + " does not extend the PluggableAuthenticator class.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        }
    }

    public boolean allowProfileUpdates(User user, Object authentication, int authType, int reqSource)
            throws AuthenticatorException {
        try {
            PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
            if (authenticator != null) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.allowProfileUpdates(user, authentication, authType,
                        (reqSource == 0 ? AuthenticationConstants.REQ_SOURCE_UNKNOWN : reqSource));
            }

            logger.error("Unable to create new authenticator.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (ClassCastException cce) {
            logger.error("Authenticator class " + authenticatorClassName
                    + " does not extend the PluggableAuthenticator class.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        }
    }

    public boolean allowPasswordUpdates(User user, Object authentication, int authType, int reqSource)
            throws AuthenticatorException {
        try {
            PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
            if (authenticator != null) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.allowPasswordUpdates(user, authentication, authType,
                        (reqSource == 0 ? AuthenticationConstants.REQ_SOURCE_UNKNOWN : reqSource));
            }

            logger.error("Unable to create new authenticator.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (ClassCastException cce) {
            logger.error("Authenticator class " + authenticatorClassName
                    + " does not extend the PluggableAuthenticator class.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        }
    }

    public boolean allowPermissionUpdates(User user, Object authentication, int authType, int reqSource)
            throws AuthenticatorException {
        try {
            PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
            if (authenticator != null) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.allowPermissionUpdates(user, authentication, authType,
                        (reqSource == 0 ? AuthenticationConstants.REQ_SOURCE_UNKNOWN : reqSource));
            }

            logger.error("Unable to create new authenticator.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (ClassCastException cce) {
            logger.error("Authenticator class " + authenticatorClassName
                    + " does not extend the PluggableAuthenticator class.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        }
    }

    public boolean allowPreferenceUpdates(User user, Object authentication, int authType, int reqSource)
            throws AuthenticatorException {
        try {
            PluggableAuthenticator authenticator = (PluggableAuthenticator) authenticatorClass.newInstance();
            if (authenticator != null) {
                HashMap<String, Object> values = new HashMap<String, Object>();
                values.put("userService", this);
                values.put("configurationService", configurationService);
                authenticator.initialize(values);
                return authenticator.allowPreferenceUpdates(user, authentication, authType,
                        (reqSource == 0 ? AuthenticationConstants.REQ_SOURCE_UNKNOWN : reqSource));
            }

            logger.error("Unable to create new authenticator.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR); 
        } catch (IllegalAccessException iae) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (InstantiationException ie) {
            logger.error("Authenticator class " + authenticatorClassName + " can not be instantiated.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        } catch (ClassCastException cce) {
            logger.error("Authenticator class " + authenticatorClassName
                    + " does not extend the PluggableAuthenticator class.");
            throw new AuthenticatorException(AuthenticatorException.SYSTEM_ERROR);
        }
    }


    


}
