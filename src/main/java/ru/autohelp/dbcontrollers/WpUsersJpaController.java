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
import ru.autohelp.dbentity.WpUsers;
import ru.autohelp.exceptions.IllegalOrphanException;
import ru.autohelp.exceptions.NonexistentEntityException;

/**
 *
 * @author Ivan
 */
public class WpUsersJpaController implements Serializable {

    public WpUsersJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(WpUsers wpUsers) {
        if (wpUsers.getAutoUsersCollection() == null) {
            wpUsers.setAutoUsersCollection(new ArrayList<AutoUsers>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<AutoUsers> attachedAutoUsersCollection = new ArrayList<AutoUsers>();
            for (AutoUsers autoUsersCollectionAutoUsersToAttach : wpUsers.getAutoUsersCollection()) {
                autoUsersCollectionAutoUsersToAttach = em.getReference(autoUsersCollectionAutoUsersToAttach.getClass(), autoUsersCollectionAutoUsersToAttach.getId());
                attachedAutoUsersCollection.add(autoUsersCollectionAutoUsersToAttach);
            }
            wpUsers.setAutoUsersCollection(attachedAutoUsersCollection);
            em.persist(wpUsers);
            for (AutoUsers autoUsersCollectionAutoUsers : wpUsers.getAutoUsersCollection()) {
                WpUsers oldWpUsersIdOfAutoUsersCollectionAutoUsers = autoUsersCollectionAutoUsers.getWpUsersId();
                autoUsersCollectionAutoUsers.setWpUsersId(wpUsers);
                autoUsersCollectionAutoUsers = em.merge(autoUsersCollectionAutoUsers);
                if (oldWpUsersIdOfAutoUsersCollectionAutoUsers != null) {
                    oldWpUsersIdOfAutoUsersCollectionAutoUsers.getAutoUsersCollection().remove(autoUsersCollectionAutoUsers);
                    oldWpUsersIdOfAutoUsersCollectionAutoUsers = em.merge(oldWpUsersIdOfAutoUsersCollectionAutoUsers);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(WpUsers wpUsers) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            WpUsers persistentWpUsers = em.find(WpUsers.class, wpUsers.getId());
            Collection<AutoUsers> autoUsersCollectionOld = persistentWpUsers.getAutoUsersCollection();
            Collection<AutoUsers> autoUsersCollectionNew = wpUsers.getAutoUsersCollection();
            List<String> illegalOrphanMessages = null;
            for (AutoUsers autoUsersCollectionOldAutoUsers : autoUsersCollectionOld) {
                if (!autoUsersCollectionNew.contains(autoUsersCollectionOldAutoUsers)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain AutoUsers " + autoUsersCollectionOldAutoUsers + " since its wpUsersId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<AutoUsers> attachedAutoUsersCollectionNew = new ArrayList<AutoUsers>();
            for (AutoUsers autoUsersCollectionNewAutoUsersToAttach : autoUsersCollectionNew) {
                autoUsersCollectionNewAutoUsersToAttach = em.getReference(autoUsersCollectionNewAutoUsersToAttach.getClass(), autoUsersCollectionNewAutoUsersToAttach.getId());
                attachedAutoUsersCollectionNew.add(autoUsersCollectionNewAutoUsersToAttach);
            }
            autoUsersCollectionNew = attachedAutoUsersCollectionNew;
            wpUsers.setAutoUsersCollection(autoUsersCollectionNew);
            wpUsers = em.merge(wpUsers);
            for (AutoUsers autoUsersCollectionNewAutoUsers : autoUsersCollectionNew) {
                if (!autoUsersCollectionOld.contains(autoUsersCollectionNewAutoUsers)) {
                    WpUsers oldWpUsersIdOfAutoUsersCollectionNewAutoUsers = autoUsersCollectionNewAutoUsers.getWpUsersId();
                    autoUsersCollectionNewAutoUsers.setWpUsersId(wpUsers);
                    autoUsersCollectionNewAutoUsers = em.merge(autoUsersCollectionNewAutoUsers);
                    if (oldWpUsersIdOfAutoUsersCollectionNewAutoUsers != null && !oldWpUsersIdOfAutoUsersCollectionNewAutoUsers.equals(wpUsers)) {
                        oldWpUsersIdOfAutoUsersCollectionNewAutoUsers.getAutoUsersCollection().remove(autoUsersCollectionNewAutoUsers);
                        oldWpUsersIdOfAutoUsersCollectionNewAutoUsers = em.merge(oldWpUsersIdOfAutoUsersCollectionNewAutoUsers);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = wpUsers.getId();
                if (findWpUsers(id) == null) {
                    throw new NonexistentEntityException("The wpUsers with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            WpUsers wpUsers;
            try {
                wpUsers = em.getReference(WpUsers.class, id);
                wpUsers.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The wpUsers with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<AutoUsers> autoUsersCollectionOrphanCheck = wpUsers.getAutoUsersCollection();
            for (AutoUsers autoUsersCollectionOrphanCheckAutoUsers : autoUsersCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This WpUsers (" + wpUsers + ") cannot be destroyed since the AutoUsers " + autoUsersCollectionOrphanCheckAutoUsers + " in its autoUsersCollection field has a non-nullable wpUsersId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(wpUsers);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<WpUsers> findWpUsersEntities() {
        return findWpUsersEntities(true, -1, -1);
    }

    public List<WpUsers> findWpUsersEntities(int maxResults, int firstResult) {
        return findWpUsersEntities(false, maxResults, firstResult);
    }

    private List<WpUsers> findWpUsersEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(WpUsers.class));
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

    public WpUsers findWpUsers(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(WpUsers.class, id);
        } finally {
            em.close();
        }
    }

    public int getWpUsersCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<WpUsers> rt = cq.from(WpUsers.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
