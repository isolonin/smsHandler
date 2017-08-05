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
import ru.autohelp.dbentity.AutoEventType;
import ru.autohelp.dbentity.AutoEvents;
import ru.autohelp.exceptions.NonexistentEntityException;

/**
 *
 * @author Ivan
 */
public class AutoEventsJpaController implements Serializable {

    public AutoEventsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(AutoEvents autoEvents) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AutoEventType eventType = autoEvents.getEventType();
            if (eventType != null) {
                eventType = em.getReference(eventType.getClass(), eventType.getId());
                autoEvents.setEventType(eventType);
            }
            em.persist(autoEvents);
            if (eventType != null) {
                eventType.getAutoEventsCollection().add(autoEvents);
                eventType = em.merge(eventType);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(AutoEvents autoEvents) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AutoEvents persistentAutoEvents = em.find(AutoEvents.class, autoEvents.getId());
            AutoEventType eventTypeOld = persistentAutoEvents.getEventType();
            AutoEventType eventTypeNew = autoEvents.getEventType();
            if (eventTypeNew != null) {
                eventTypeNew = em.getReference(eventTypeNew.getClass(), eventTypeNew.getId());
                autoEvents.setEventType(eventTypeNew);
            }
            autoEvents = em.merge(autoEvents);
            if (eventTypeOld != null && !eventTypeOld.equals(eventTypeNew)) {
                eventTypeOld.getAutoEventsCollection().remove(autoEvents);
                eventTypeOld = em.merge(eventTypeOld);
            }
            if (eventTypeNew != null && !eventTypeNew.equals(eventTypeOld)) {
                eventTypeNew.getAutoEventsCollection().add(autoEvents);
                eventTypeNew = em.merge(eventTypeNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = autoEvents.getId();
                if (findAutoEvents(id) == null) {
                    throw new NonexistentEntityException("The autoEvents with id " + id + " no longer exists.");
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
            AutoEvents autoEvents;
            try {
                autoEvents = em.getReference(AutoEvents.class, id);
                autoEvents.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The autoEvents with id " + id + " no longer exists.", enfe);
            }
            AutoEventType eventType = autoEvents.getEventType();
            if (eventType != null) {
                eventType.getAutoEventsCollection().remove(autoEvents);
                eventType = em.merge(eventType);
            }
            em.remove(autoEvents);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<AutoEvents> findAutoEventsEntities() {
        return findAutoEventsEntities(true, -1, -1);
    }

    public List<AutoEvents> findAutoEventsEntities(int maxResults, int firstResult) {
        return findAutoEventsEntities(false, maxResults, firstResult);
    }

    private List<AutoEvents> findAutoEventsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(AutoEvents.class));
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

    public AutoEvents findAutoEvents(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AutoEvents.class, id);
        } finally {
            em.close();
        }
    }

    public int getAutoEventsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<AutoEvents> rt = cq.from(AutoEvents.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
