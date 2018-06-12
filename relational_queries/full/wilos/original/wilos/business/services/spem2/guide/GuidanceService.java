/*
 * Wilos Is a cLever process Orchestration Software - http://www.wilos-project.org
 * Copyright (C) 2006-2007 Paul Sabatier University, IUP ISI (Toulouse, France) <massie@irit.fr>
 * Copyright (C) 2007 Mathieu BENOIT <mathieu-benoit@hotmail.fr>
 * Copyright (C) 2007 Sebastien BALARD <sbalard@wilos-project.org>
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

package wilos.business.services.spem2.guide;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import wilos.business.services.spem2.process.ProcessService;
import wilos.hibernate.spem2.guide.GuidanceDao;
import wilos.model.spem2.activity.Activity;
import wilos.model.spem2.breakdownelement.BreakdownElement;
import wilos.model.spem2.guide.Guidance;
import wilos.model.spem2.process.Process;
import wilos.model.spem2.role.RoleDefinition;
import wilos.model.spem2.task.TaskDefinition;

@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class GuidanceService {

	private GuidanceDao guidanceDao;

	private ProcessService processService;

	public Guidance getGuidance(String _id) {
		return this.guidanceDao.getGuidance(_id);
	}

	public List<Guidance> getAllGuidances() {
		return this.guidanceDao.getAllGuidances();
	}

	public String saveGuidance(Guidance _guidance) {
		return this.guidanceDao.saveOrUpdateGuidance(_guidance);
	}

	public void deleteGuidance(Guidance _guidance) {
		this.guidanceDao.deleteGuidance(_guidance);
	}

	public Guidance getGuidanceFromGuid(String _guid) {
		return this.guidanceDao.getGuidanceFromGuid(_guid);
	}

	public String getAttachmentFilePath(String idGuidance, String file) {
		String filePathToBeReturn = "";
		String folder = "";
		String attachment = "";
		Guidance g = null;
		String guidCurrentProcess = null;

		System.out.println("Id : " + idGuidance);

		g = this.getGuidanceFromGuid(idGuidance);

		BreakdownElement bde = null;

		if (g.getTaskDefinitions().size() != 0)
			bde = g.getTaskDefinitions().iterator().next().getTaskDescriptors()
					.iterator().next();
		if (g.getRoleDefinitions().size() != 0)
			bde = g.getRoleDefinitions().iterator().next().getRoleDescriptors()
					.iterator().next();
		if (g.getActivities().size() != 0)
			bde = g.getActivities().iterator().next();
		if (bde != null) {
			while (bde.getSuperActivities().size() != 0) {
				bde = bde.getSuperActivities().iterator().next();
			}
			if (bde instanceof Process) {
				guidCurrentProcess = bde.getGuid();
			}
		}

		if (g != null && guidCurrentProcess != null) {

			Enumeration attachments = new StringTokenizer(g.getAttachment(),
					"|", false);
			while (attachments.hasMoreElements()) {
				String currentAttachment = (String) attachments.nextElement();
				if (currentAttachment.matches(".*" + file)) {
					attachment = new String(currentAttachment);
					attachment = attachment.replace("/", File.separator);
				}
			}

			folder = this.processService.getProcessFromGuid(guidCurrentProcess)
					.getFolderPath();
			filePathToBeReturn = folder + File.separator + guidCurrentProcess
					+ File.separator + attachment;
			System.out.println("FOLDER+ATTCH: " + filePathToBeReturn);
		}
		return filePathToBeReturn;
	}

	/* Getters & Setters */

	public GuidanceDao getGuidanceDao() {
		return this.guidanceDao;
	}

	public void setGuidanceDao(GuidanceDao _guidanceDao) {
		this.guidanceDao = _guidanceDao;
	}

	public ProcessService getProcessService() {
		return processService;
	}

	public void setProcessService(ProcessService processService) {
		this.processService = processService;
	}

	/**
	 * @param _retrievedRd
	 * @return
	 */
	public Set<TaskDefinition> getTaskDefinitions(Guidance _guidance) {
		Set<TaskDefinition> tmp = new HashSet<TaskDefinition>();
		this.guidanceDao.getSessionFactory().getCurrentSession().saveOrUpdate(
				_guidance);
		for (TaskDefinition td : _guidance.getTaskDefinitions()) {
			tmp.add(td);
		}
		return tmp;
	}

	/**
	 * @param _retrievedRd
	 * @return
	 */
	public Set<RoleDefinition> getRoleDefinitions(Guidance _guidance) {
		Set<RoleDefinition> tmp = new HashSet<RoleDefinition>();
		this.guidanceDao.getSessionFactory().getCurrentSession().saveOrUpdate(
				_guidance);
		for (RoleDefinition td : _guidance.getRoleDefinitions()) {
			tmp.add(td);
		}
		return tmp;
	}

	/**
	 * @param _retrievedRd
	 * @return
	 */
	public Set<Activity> getActivities(Guidance _guidance) {
		Set<Activity> tmp = new HashSet<Activity>();
		this.guidanceDao.getSessionFactory().getCurrentSession().saveOrUpdate(
				_guidance);
		for (Activity act : _guidance.getActivities()) {
			tmp.add(act);
		}
		return tmp;
	}
}
