/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.autohelp.dbentity;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Ivan
 */
@Entity
@Table(name = "auto_event_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AutoEventType.findAll", query = "SELECT a FROM AutoEventType a")
    , @NamedQuery(name = "AutoEventType.findById", query = "SELECT a FROM AutoEventType a WHERE a.id = :id")
    , @NamedQuery(name = "AutoEventType.findByEventType", query = "SELECT a FROM AutoEventType a WHERE a.eventType = :eventType")})
public class AutoEventType implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Size(max = 100)
    @Column(name = "event_type")
    private String eventType;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "eventType")
    private Collection<AutoEvents> autoEventsCollection;

    public AutoEventType() {
    }

    public AutoEventType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @XmlTransient
    public Collection<AutoEvents> getAutoEventsCollection() {
        return autoEventsCollection;
    }

    public void setAutoEventsCollection(Collection<AutoEvents> autoEventsCollection) {
        this.autoEventsCollection = autoEventsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoEventType)) {
            return false;
        }
        AutoEventType other = (AutoEventType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.autohelp.dbentity.AutoEventType[ id=" + id + " ]";
    }
    
}
