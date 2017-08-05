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
import ru.autohelp.dbcontrollers.exceptions.NonexistentEntityException;
import ru.autohelp.dbcontrollers.exceptions.PreexistingEntityException;
import ru.autohelp.dbentity.Blacklist;

/**
 *
 * @author Ivan
 */
public class BlacklistJpaController implements Serializable {

    public BlacklistJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Blacklist blacklist) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(blacklist);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findBlacklist(blacklist.getMsisdn()) != null) {
                throw new PreexistingEntityException("Blacklist " + blacklist + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Blacklist blacklist) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            blacklist = em.merge(blacklist);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = blacklist.getMsisdn();
                if (findBlacklist(id) == null) {
                    throw new NonexistentEntityException("The blacklist with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Blacklist blacklist;
            try {
                blacklist = em.getReference(Blacklist.class, id);
                blacklist.getMsisdn();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The blacklist with id " + id + " no longer exists.", enfe);
            }
            em.remove(blacklist);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Blacklist> findBlacklistEntities() {
        return findBlacklistEntities(true, -1, -1);
    }

    public List<Blacklist> findBlacklistEntities(int maxResults, int firstResult) {
        return findBlacklistEntities(false, maxResults, firstResult);
    }

    private List<Blacklist> findBlacklistEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Blacklist.class));
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

    public Blacklist findBlacklist(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Blacklist.class, id);
        } finally {
            em.close();
        }
    }

    public int getBlacklistCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Blacklist> rt = cq.from(Blacklist.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
