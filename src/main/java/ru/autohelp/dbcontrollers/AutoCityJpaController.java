/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.autohelp.dbcontrollers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ru.autohelp.dbentity.AutoUsers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import ru.autohelp.dbentity.AutoCity;
import ru.autohelp.exceptions.NonexistentEntityException;

/**
 *
 * @author Ivan
 */
public class AutoCityJpaController implements Serializable {

    public AutoCityJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(AutoCity autoCity) {
        if (autoCity.getAutoUsersCollection() == null) {
            autoCity.setAutoUsersCollection(new ArrayList<AutoUsers>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<AutoUsers> attachedAutoUsersCollection = new ArrayList<AutoUsers>();
            for (AutoUsers autoUsersCollectionAutoUsersToAttach : autoCity.getAutoUsersCollection()) {
                autoUsersCollectionAutoUsersToAttach = em.getReference(autoUsersCollectionAutoUsersToAttach.getClass(), autoUsersCollectionAutoUsersToAttach.getId());
                attachedAutoUsersCollection.add(autoUsersCollectionAutoUsersToAttach);
            }
            autoCity.setAutoUsersCollection(attachedAutoUsersCollection);
            em.persist(autoCity);
            for (AutoUsers autoUsersCollectionAutoUsers : autoCity.getAutoUsersCollection()) {
                AutoCity oldCityIdOfAutoUsersCollectionAutoUsers = autoUsersCollectionAutoUsers.getCityId();
                autoUsersCollectionAutoUsers.setCityId(autoCity);
                autoUsersCollectionAutoUsers = em.merge(autoUsersCollectionAutoUsers);
                if (oldCityIdOfAutoUsersCollectionAutoUsers != null) {
                    oldCityIdOfAutoUsersCollectionAutoUsers.getAutoUsersCollection().remove(autoUsersCollectionAutoUsers);
                    oldCityIdOfAutoUsersCollectionAutoUsers = em.merge(oldCityIdOfAutoUsersCollectionAutoUsers);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(AutoCity autoCity) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AutoCity persistentAutoCity = em.find(AutoCity.class, autoCity.getId());
            Collection<AutoUsers> autoUsersCollectionOld = persistentAutoCity.getAutoUsersCollection();
            Collection<AutoUsers> autoUsersCollectionNew = autoCity.getAutoUsersCollection();
            Collection<AutoUsers> attachedAutoUsersCollectionNew = new ArrayList<AutoUsers>();
            for (AutoUsers autoUsersCollectionNewAutoUsersToAttach : autoUsersCollectionNew) {
                autoUsersCollectionNewAutoUsersToAttach = em.getReference(autoUsersCollectionNewAutoUsersToAttach.getClass(), autoUsersCollectionNewAutoUsersToAttach.getId());
                attachedAutoUsersCollectionNew.add(autoUsersCollectionNewAutoUsersToAttach);
            }
            autoUsersCollectionNew = attachedAutoUsersCollectionNew;
            autoCity.setAutoUsersCollection(autoUsersCollectionNew);
            autoCity = em.merge(autoCity);
            for (AutoUsers autoUsersCollectionOldAutoUsers : autoUsersCollectionOld) {
                if (!autoUsersCollectionNew.contains(autoUsersCollectionOldAutoUsers)) {
                    autoUsersCollectionOldAutoUsers.setCityId(null);
                    autoUsersCollectionOldAutoUsers = em.merge(autoUsersCollectionOldAutoUsers);
                }
            }
            for (AutoUsers autoUsersCollectionNewAutoUsers : autoUsersCollectionNew) {
                if (!autoUsersCollectionOld.contains(autoUsersCollectionNewAutoUsers)) {
                    AutoCity oldCityIdOfAutoUsersCollectionNewAutoUsers = autoUsersCollectionNewAutoUsers.getCityId();
                    autoUsersCollectionNewAutoUsers.setCityId(autoCity);
                    autoUsersCollectionNewAutoUsers = em.merge(autoUsersCollectionNewAutoUsers);
                    if (oldCityIdOfAutoUsersCollectionNewAutoUsers != null && !oldCityIdOfAutoUsersCollectionNewAutoUsers.equals(autoCity)) {
                        oldCityIdOfAutoUsersCollectionNewAutoUsers.getAutoUsersCollection().remove(autoUsersCollectionNewAutoUsers);
                        oldCityIdOfAutoUsersCollectionNewAutoUsers = em.merge(oldCityIdOfAutoUsersCollectionNewAutoUsers);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = autoCity.getId();
                if (findAutoCity(id) == null) {
                    throw new NonexistentEntityException("The autoCity with id " + id + " no longer exists.");
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
            AutoCity autoCity;
            try {
                autoCity = em.getReference(AutoCity.class, id);
                autoCity.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The autoCity with id " + id + " no longer exists.", enfe);
            }
            Collection<AutoUsers> autoUsersCollection = autoCity.getAutoUsersCollection();
            for (AutoUsers autoUsersCollectionAutoUsers : autoUsersCollection) {
                autoUsersCollectionAutoUsers.setCityId(null);
                autoUsersCollectionAutoUsers = em.merge(autoUsersCollectionAutoUsers);
            }
            em.remove(autoCity);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<AutoCity> findAutoCityEntities() {
        return findAutoCityEntities(true, -1, -1);
    }

    public List<AutoCity> findAutoCityEntities(int maxResults, int firstResult) {
        return findAutoCityEntities(false, maxResults, firstResult);
    }

    private List<AutoCity> findAutoCityEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(AutoCity.class));
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

    public AutoCity findAutoCity(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AutoCity.class, id);
        } finally {
            em.close();
        }
    }

    public int getAutoCityCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<AutoCity> rt = cq.from(AutoCity.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
