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
import ru.autohelp.dbentity.AutoEvents;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import ru.autohelp.dbentity.AutoEventType;
import ru.autohelp.exceptions.IllegalOrphanException;
import ru.autohelp.exceptions.NonexistentEntityException;
import ru.autohelp.exceptions.PreexistingEntityException;

/**
 *
 * @author Ivan
 */
public class AutoEventTypeJpaController implements Serializable {

    public AutoEventTypeJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(AutoEventType autoEventType) throws PreexistingEntityException, Exception {
        if (autoEventType.getAutoEventsCollection() == null) {
            autoEventType.setAutoEventsCollection(new ArrayList<AutoEvents>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<AutoEvents> attachedAutoEventsCollection = new ArrayList<AutoEvents>();
            for (AutoEvents autoEventsCollectionAutoEventsToAttach : autoEventType.getAutoEventsCollection()) {
                autoEventsCollectionAutoEventsToAttach = em.getReference(autoEventsCollectionAutoEventsToAttach.getClass(), autoEventsCollectionAutoEventsToAttach.getId());
                attachedAutoEventsCollection.add(autoEventsCollectionAutoEventsToAttach);
            }
            autoEventType.setAutoEventsCollection(attachedAutoEventsCollection);
            em.persist(autoEventType);
            for (AutoEvents autoEventsCollectionAutoEvents : autoEventType.getAutoEventsCollection()) {
                AutoEventType oldEventTypeOfAutoEventsCollectionAutoEvents = autoEventsCollectionAutoEvents.getEventType();
                autoEventsCollectionAutoEvents.setEventType(autoEventType);
                autoEventsCollectionAutoEvents = em.merge(autoEventsCollectionAutoEvents);
                if (oldEventTypeOfAutoEventsCollectionAutoEvents != null) {
                    oldEventTypeOfAutoEventsCollectionAutoEvents.getAutoEventsCollection().remove(autoEventsCollectionAutoEvents);
                    oldEventTypeOfAutoEventsCollectionAutoEvents = em.merge(oldEventTypeOfAutoEventsCollectionAutoEvents);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findAutoEventType(autoEventType.getId()) != null) {
                throw new PreexistingEntityException("AutoEventType " + autoEventType + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(AutoEventType autoEventType) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AutoEventType persistentAutoEventType = em.find(AutoEventType.class, autoEventType.getId());
            Collection<AutoEvents> autoEventsCollectionOld = persistentAutoEventType.getAutoEventsCollection();
            Collection<AutoEvents> autoEventsCollectionNew = autoEventType.getAutoEventsCollection();
            List<String> illegalOrphanMessages = null;
            for (AutoEvents autoEventsCollectionOldAutoEvents : autoEventsCollectionOld) {
                if (!autoEventsCollectionNew.contains(autoEventsCollectionOldAutoEvents)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain AutoEvents " + autoEventsCollectionOldAutoEvents + " since its eventType field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<AutoEvents> attachedAutoEventsCollectionNew = new ArrayList<AutoEvents>();
            for (AutoEvents autoEventsCollectionNewAutoEventsToAttach : autoEventsCollectionNew) {
                autoEventsCollectionNewAutoEventsToAttach = em.getReference(autoEventsCollectionNewAutoEventsToAttach.getClass(), autoEventsCollectionNewAutoEventsToAttach.getId());
                attachedAutoEventsCollectionNew.add(autoEventsCollectionNewAutoEventsToAttach);
            }
            autoEventsCollectionNew = attachedAutoEventsCollectionNew;
            autoEventType.setAutoEventsCollection(autoEventsCollectionNew);
            autoEventType = em.merge(autoEventType);
            for (AutoEvents autoEventsCollectionNewAutoEvents : autoEventsCollectionNew) {
                if (!autoEventsCollectionOld.contains(autoEventsCollectionNewAutoEvents)) {
                    AutoEventType oldEventTypeOfAutoEventsCollectionNewAutoEvents = autoEventsCollectionNewAutoEvents.getEventType();
                    autoEventsCollectionNewAutoEvents.setEventType(autoEventType);
                    autoEventsCollectionNewAutoEvents = em.merge(autoEventsCollectionNewAutoEvents);
                    if (oldEventTypeOfAutoEventsCollectionNewAutoEvents != null && !oldEventTypeOfAutoEventsCollectionNewAutoEvents.equals(autoEventType)) {
                        oldEventTypeOfAutoEventsCollectionNewAutoEvents.getAutoEventsCollection().remove(autoEventsCollectionNewAutoEvents);
                        oldEventTypeOfAutoEventsCollectionNewAutoEvents = em.merge(oldEventTypeOfAutoEventsCollectionNewAutoEvents);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = autoEventType.getId();
                if (findAutoEventType(id) == null) {
                    throw new NonexistentEntityException("The autoEventType with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AutoEventType autoEventType;
            try {
                autoEventType = em.getReference(AutoEventType.class, id);
                autoEventType.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The autoEventType with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<AutoEvents> autoEventsCollectionOrphanCheck = autoEventType.getAutoEventsCollection();
            for (AutoEvents autoEventsCollectionOrphanCheckAutoEvents : autoEventsCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This AutoEventType (" + autoEventType + ") cannot be destroyed since the AutoEvents " + autoEventsCollectionOrphanCheckAutoEvents + " in its autoEventsCollection field has a non-nullable eventType field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(autoEventType);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<AutoEventType> findAutoEventTypeEntities() {
        return findAutoEventTypeEntities(true, -1, -1);
    }

    public List<AutoEventType> findAutoEventTypeEntities(int maxResults, int firstResult) {
        return findAutoEventTypeEntities(false, maxResults, firstResult);
    }

    private List<AutoEventType> findAutoEventTypeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(AutoEventType.class));
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

    public AutoEventType findAutoEventType(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AutoEventType.class, id);
        } finally {
            em.close();
        }
    }

    public int getAutoEventTypeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<AutoEventType> rt = cq.from(AutoEventType.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
