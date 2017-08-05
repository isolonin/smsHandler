/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.autohelp.dbcontrollers;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ru.autohelp.dbentity.WpUsers;
import ru.autohelp.dbentity.AutoCity;
import ru.autohelp.dbentity.AutoUsers;
import ru.autohelp.exceptions.NonexistentEntityException;
import ru.vehicleutils.models.VehicleNumber;

/**
 *
 * @author Ivan
 */
public class AutoUsersJpaController implements Serializable {

    public AutoUsersJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(AutoUsers autoUsers) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            WpUsers wpUsersId = autoUsers.getWpUsersId();
            if (wpUsersId != null) {
                wpUsersId = em.getReference(wpUsersId.getClass(), wpUsersId.getId());
                autoUsers.setWpUsersId(wpUsersId);
            }
            AutoCity cityId = autoUsers.getCityId();
            if (cityId != null) {
                cityId = em.getReference(cityId.getClass(), cityId.getId());
                autoUsers.setCityId(cityId);
            }
            em.persist(autoUsers);
            if (wpUsersId != null) {
                wpUsersId.getAutoUsersCollection().add(autoUsers);
                wpUsersId = em.merge(wpUsersId);
            }
            if (cityId != null) {
                cityId.getAutoUsersCollection().add(autoUsers);
                cityId = em.merge(cityId);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(AutoUsers autoUsers) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AutoUsers persistentAutoUsers = em.find(AutoUsers.class, autoUsers.getId());
            WpUsers wpUsersIdOld = persistentAutoUsers.getWpUsersId();
            WpUsers wpUsersIdNew = autoUsers.getWpUsersId();
            AutoCity cityIdOld = persistentAutoUsers.getCityId();
            AutoCity cityIdNew = autoUsers.getCityId();
            if (wpUsersIdNew != null) {
                wpUsersIdNew = em.getReference(wpUsersIdNew.getClass(), wpUsersIdNew.getId());
                autoUsers.setWpUsersId(wpUsersIdNew);
            }
            if (cityIdNew != null) {
                cityIdNew = em.getReference(cityIdNew.getClass(), cityIdNew.getId());
                autoUsers.setCityId(cityIdNew);
            }
            autoUsers = em.merge(autoUsers);
            if (wpUsersIdOld != null && !wpUsersIdOld.equals(wpUsersIdNew)) {
                wpUsersIdOld.getAutoUsersCollection().remove(autoUsers);
                wpUsersIdOld = em.merge(wpUsersIdOld);
            }
            if (wpUsersIdNew != null && !wpUsersIdNew.equals(wpUsersIdOld)) {
                wpUsersIdNew.getAutoUsersCollection().add(autoUsers);
                wpUsersIdNew = em.merge(wpUsersIdNew);
            }
            if (cityIdOld != null && !cityIdOld.equals(cityIdNew)) {
                cityIdOld.getAutoUsersCollection().remove(autoUsers);
                cityIdOld = em.merge(cityIdOld);
            }
            if (cityIdNew != null && !cityIdNew.equals(cityIdOld)) {
                cityIdNew.getAutoUsersCollection().add(autoUsers);
                cityIdNew = em.merge(cityIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = autoUsers.getId();
                if (findAutoUsers(id) == null) {
                    throw new NonexistentEntityException("The autoUsers with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AutoUsers autoUsers;
            try {
                autoUsers = em.getReference(AutoUsers.class, id);
                autoUsers.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The autoUsers with id " + id + " no longer exists.", enfe);
            }
            WpUsers wpUsersId = autoUsers.getWpUsersId();
            if (wpUsersId != null) {
                wpUsersId.getAutoUsersCollection().remove(autoUsers);
                wpUsersId = em.merge(wpUsersId);
            }
            AutoCity cityId = autoUsers.getCityId();
            if (cityId != null) {
                cityId.getAutoUsersCollection().remove(autoUsers);
                cityId = em.merge(cityId);
            }
            em.remove(autoUsers);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<AutoUsers> findAutoUsersEntities() {
        return findAutoUsersEntities(true, -1, -1);
    }

    public List<AutoUsers> findAutoUsersEntities(int maxResults, int firstResult) {
        return findAutoUsersEntities(false, maxResults, firstResult);
    }

    private List<AutoUsers> findAutoUsersEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(AutoUsers.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public AutoUsers findAutoUsers(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AutoUsers.class, id);
        } finally {
            em.close();
        }
    }
    
    public AutoUsers findByMsisdn(String msisdn) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("AutoUsers.findByDef");
            query.setParameter("def", msisdn);
            List<AutoUsers> resultList = query.getResultList();
            if(resultList != null && resultList.isEmpty() == false){
                return resultList.get(0);
            }
            return null;
        } finally {
            em.close();
        }
    }
    
    public AutoUsers findByVehicleNumber(VehicleNumber vehicleNumber) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("AutoUsers.findByVehicleNumber");
            query.setParameter("transportId", vehicleNumber.getTransportId().toString());
            query.setParameter("transportChars", vehicleNumber.getTransportChars());
            query.setParameter("transportReg", vehicleNumber.getTransportReg().toString());
            List<AutoUsers> resultList = query.getResultList();
            if(resultList != null && resultList.isEmpty() == false){
                return resultList.get(0);
            }
            return null;
        } finally {
            em.close();
        }
    }
    
    public int updateBalance(AutoUsers user, AutoUsers.Type type, int newBalance){
        EntityManager em = getEntityManager();
        int result = 0;
        try {
            if(user != null){
                Query query = null;
                switch(type){
                    case SMS:
                        query = em.createNativeQuery("update auto_users set limit_sms = ? where id = ?");
                        break;
                    case IVR:
                        query = em.createNativeQuery("update auto_users set limit_ivr = ? where id = ?");
                        break;
                    case MINUTES:
                        query = em.createNativeQuery("update auto_users set limit_minutes = ? where id = ?");
                        break;
                }
                em.getTransaction().begin();
                query.setParameter(1, newBalance);
                query.setParameter(2, user.getId());
                result = query.executeUpdate();
                em.getTransaction().commit();
            }
        } catch(Exception ex){
            System.out.println("");
        } finally {
            em.close();
        }
        return result;
    }

    public int getAutoUsersCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<AutoUsers> rt = cq.from(AutoUsers.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
