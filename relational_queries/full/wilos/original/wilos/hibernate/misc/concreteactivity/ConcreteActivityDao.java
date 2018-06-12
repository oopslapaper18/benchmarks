/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) 2007 Sebastien BALARD <sbalard@wilos-project.org>
 * Copyright (C) 2007-2008 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package wilos.hibernate.misc.concreteactivity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import wilos.model.misc.concreteactivity.ConcreteActivity;

/**
 * ConcreteActivityDao manage requests from the system to store Concrete
 * Activities to the database
 * 
 * @author garwind
 * @author deder
 */
public class ConcreteActivityDao extends HibernateDaoSupport {
	/**
	 * Saves or updates a ConcreteActivity
	 * 
	 * @param _concreteactivity The ConcreteActivity to be saved or updated
	 */
	public String saveOrUpdateConcreteActivity(
			ConcreteActivity _concreteactivity) {
		if (_concreteactivity != null) {
			this.getHibernateTemplate().saveOrUpdate(_concreteactivity);
			return _concreteactivity.getId();
		}
		return "";
	}

	/**
	 * Returns a list of all the ConcreteActivities
	 * 
	 * @return A list of all the ConcreteActivities
	 */
	public List<ConcreteActivity> getAllConcreteActivities() {
		List<ConcreteActivity> concreteActivities = new ArrayList<ConcreteActivity>();
		for (Object obj : this.getHibernateTemplate().loadAll(
				//ConcreteActivity.class)) {
				null)) { // akc
			ConcreteActivity ca = (ConcreteActivity) obj;
			concreteActivities.add(ca);
		}
		return concreteActivities;
	}

	/**
	 * Tests if there are one or more existing ConcreteActivities that have the given ID
	 * 
	 * @param _id The wanted ID
	 * @return True or false
	 */
	public boolean existsConcreteActivity(String _id) {
		List concreteactvities = this.getHibernateTemplate().find(
				"from ConcreteActivity a where a.id=?", _id);
		return (concreteactvities.size() > 0);
	}

	/**
	 * Returns the ConcreteActivities which has the specified ID
	 * 
	 * @param _id The id of the wanted ConcreteActivity
	 * @return The wanted ConcreteActivity
	 */
	public ConcreteActivity getConcreteActivity(String _id) {
	    	if(_id == null)
	    	    return null;
		if (!_id.equals(""))
			return null; 
      // akc
      //(ConcreteActivity) this.getHibernateTemplate().get(
					//ConcreteActivity.class, _id);
		return null;
	}

	/**
	 * Returns the ConcreteActivity which has the given prefix
	 * If there are many ConcreteActivities with the same prefix, it returns the first of them
	 * 
	 * @param _prefix The wanted prefix
	 * @return The wanted ConcreteActivity
	 */
	public ConcreteActivity getConcreteActivityFromPrefix(String _prefix) {
		List concreteactvities = this.getHibernateTemplate().find(
				"from ConcreteActivity a where a.prefix=?", _prefix);
		if (concreteactvities.size() > 0)
			return (ConcreteActivity) concreteactvities.get(0);
		else
			return null;
	}

	/**
	 * Deletes the ConcreteActivity
	 * 
	 * @param _concreteactivity The ConcreteActivity to be deleted
	 */
	public void deleteConcreteActivity(ConcreteActivity _concreteactivity) {
		this.getHibernateTemplate().delete(_concreteactivity);
	}

	/**
	 * Returns the ConcreteActivity which has the given name
	 * If there are many ConcreteActivities with the same name, it returns the first of them
	 * 
	 * @param _name The wanted name
	 * @return The wanted ConcreteActivity
	 */
	public ConcreteActivity getConcreteActivityByName(String _name) {
		if (!_name.equals("")) {
			List activities = this.getHibernateTemplate().find(
					"from Activity a where a.name=?", _name);
			if (activities.size() > 0)
				return (ConcreteActivity) activities.get(0);
		}
		return null;
	}

	/**
	 * Returns the maximum display order for the given concreteActivity
	 * 
	 * @param _cact the wanted concreteActivity
	 * @return the highest displayOrder for the given activity
	 */
	public String getMaxDisplayOrder(ConcreteActivity _cact) {
		List cacts = this.getHibernateTemplate().find(
				"from ConcreteActivity ca where ca.id=? order by displayOrder DESC", _cact.getId());
		String res = ((ConcreteActivity) cacts.get(0)).getDisplayOrder();
		if(res.length() > 0){
		    return res.substring(res.length()-1);
		}
		else 
		    return "0";
	}
}
