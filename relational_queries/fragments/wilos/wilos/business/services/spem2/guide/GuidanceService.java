package wilos.
  business.
  services.
  spem2.
  guide;

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

@org.
  springframework.
  transaction.
  annotation.
  Transactional(readOnly=false, propagation=org.springframework.transaction.
                                              annotation.Propagation.REQUIRED) 
public class GuidanceService {
    private wilos.hibernate.spem2.guide.GuidanceDao guidanceDao;
    private wilos.business.services.spem2.process.ProcessService processService;
    
    public wilos.model.spem2.guide.Guidance getGuidance(java.lang.String _id) {
        return this.guidanceDao.getGuidance(_id);
    }
    
    public java.util.List<wilos.model.spem2.guide.Guidance> getAllGuidances() {
        return this.guidanceDao.getAllGuidances();
    }
    
    public java.lang.
      String saveGuidance(wilos.model.spem2.guide.Guidance _guidance) {
        return this.guidanceDao.saveOrUpdateGuidance(_guidance);
    }
    
    public void deleteGuidance(wilos.model.spem2.guide.Guidance _guidance) {
        this.guidanceDao.deleteGuidance(_guidance);
    }
    
    public wilos.model.spem2.guide.
      Guidance getGuidanceFromGuid(java.lang.String _guid) {
        return this.guidanceDao.getGuidanceFromGuid(_guid);
    }
    
    public java.lang.String getAttachmentFilePath(java.lang.String idGuidance,
                                                  java.lang.String file) {
        java.lang.String filePathToBeReturn = "";
        java.lang.String folder = "";
        java.lang.String attachment = "";
        wilos.model.spem2.guide.Guidance g = null;
        java.lang.String guidCurrentProcess = null;
        java.lang.System.out.println("Id : " + idGuidance);
        g = this.getGuidanceFromGuid(idGuidance);
        wilos.model.spem2.breakdownelement.BreakdownElement bde = null;
        if (g.getTaskDefinitions().size() != 0)
            bde =
              g.getTaskDefinitions().iterator().next().getTaskDescriptors().
                iterator().next();
        if (g.getRoleDefinitions().size() != 0)
            bde =
              g.getRoleDefinitions().iterator().next().getRoleDescriptors().
                iterator().next();
        if (g.getActivities().size() != 0)
            bde = g.getActivities().iterator().next();
        if (bde != null) {
            while (bde.getSuperActivities().size() != 0) {
                bde = bde.getSuperActivities().iterator().next();
            }
            if (bde instanceof wilos.model.spem2.process.Process) {
                guidCurrentProcess = bde.getGuid();
            }
        }
        if (g != null && guidCurrentProcess != null) {
            java.util.Enumeration attachments = new java.util.StringTokenizer(
              g.getAttachment(), "|", false);
            while (attachments.hasMoreElements()) {
                java.lang.String currentAttachment =
                  (java.lang.String) attachments.nextElement();
                if (currentAttachment.matches(".*" + file)) {
                    attachment = new java.lang.String(currentAttachment);
                    attachment = attachment.replace("/",
                                                    java.io.File.separator);
                }
            }
            folder =
              this.processService.getProcessFromGuid(guidCurrentProcess).
                getFolderPath();
            filePathToBeReturn = folder + java.io.File.separator +
                                 guidCurrentProcess + java.io.File.separator +
                                 attachment;
            java.lang.System.out.println("FOLDER+ATTCH: " + filePathToBeReturn);
        }
        return filePathToBeReturn;
    }
    
    public wilos.hibernate.spem2.guide.GuidanceDao getGuidanceDao() {
        return this.guidanceDao;
    }
    
    public void setGuidanceDao(wilos.hibernate.spem2.guide.
                                 GuidanceDao _guidanceDao) {
        this.guidanceDao = _guidanceDao;
    }
    
    public wilos.business.services.spem2.process.
      ProcessService getProcessService() { return processService; }
    
    public void setProcessService(wilos.business.services.spem2.process.
                                    ProcessService processService) {
        this.processService = processService;
    }
    
    /**
     * @param _retrievedRd
     * @return
     */
    public java.
      util.
      Set<wilos.
      model.
      spem2.
      task.
      TaskDefinition> getTaskDefinitions(wilos.model.spem2.guide.
                                           Guidance _guidance) {
        labeled_1 :
        {
        java.util.Set<wilos.model.spem2.task.TaskDefinition> tmp =
          new java.util.HashSet<wilos.model.spem2.task.TaskDefinition>();
        this.guidanceDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                   _guidance);

            java.util.Iterator extfor$iter =
                    _guidance.getTaskDefinitions().iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.spem2.task.TaskDefinition td =
                        (wilos.model.spem2.task.TaskDefinition) extfor$iter.next();
                tmp.add(td);
            }
        }
        return tmp;
    }
    
    /**
     * @param _retrievedRd
     * @return
     */
    public java.
      util.
      Set<wilos.
      model.
      spem2.
      role.
      RoleDefinition> getRoleDefinitions(wilos.model.spem2.guide.
                                           Guidance _guidance) {
        labeled_2 :
        {
            java.util.Set<wilos.model.spem2.role.RoleDefinition> tmp =
                    new java.util.HashSet<wilos.model.spem2.role.RoleDefinition>();
            this.guidanceDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                    _guidance);
            java.util.Iterator extfor$iter =
                    _guidance.getRoleDefinitions().iterator();
            while (extfor$iter.hasNext())
            {
                wilos.model.spem2.role.RoleDefinition td =
                        (wilos.model.spem2.role.RoleDefinition) extfor$iter.next();
                tmp.add(td);
            }
        }
        return tmp;
    }
    
    /**
     * @param _retrievedRd
     * @return
     */
    public java.util.Set<wilos.model.spem2.activity.
      Activity> getActivities(wilos.model.spem2.guide.Guidance _guidance) {
        java.util.Set<wilos.model.spem2.activity.Activity> tmp =
          new java.util.HashSet<wilos.model.spem2.activity.Activity>();
        this.guidanceDao.getSessionFactory().getCurrentSession().saveOrUpdate(
                                                                   _guidance);
        java.util.Iterator extfor$iter =
          _guidance.getActivities().iterator();
        while (extfor$iter.hasNext()) {
            wilos.model.spem2.activity.Activity act =
              (wilos.model.spem2.activity.Activity) extfor$iter.next();
            tmp.add(act);
        }
        return tmp;
    }
    
    public GuidanceService() { super(); }
}
