/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.autohelp.dbentity;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Ivan
 */
@Entity
@Table(name = "auto_city")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AutoCity.findAll", query = "SELECT a FROM AutoCity a")
    , @NamedQuery(name = "AutoCity.findById", query = "SELECT a FROM AutoCity a WHERE a.id = :id")
    , @NamedQuery(name = "AutoCity.findByCity", query = "SELECT a FROM AutoCity a WHERE a.city = :city")})
public class AutoCity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 100)
    @Column(name = "city")
    private String city;
    @OneToMany(mappedBy = "cityId")
    private Collection<AutoUsers> autoUsersCollection;

    public AutoCity() {
    }

    public AutoCity(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @XmlTransient
    public Collection<AutoUsers> getAutoUsersCollection() {
        return autoUsersCollection;
    }

    public void setAutoUsersCollection(Collection<AutoUsers> autoUsersCollection) {
        this.autoUsersCollection = autoUsersCollection;
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
        if (!(object instanceof AutoCity)) {
            return false;
        }
        AutoCity other = (AutoCity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.autohelp.dbentity.AutoCity[ id=" + id + " ]";
    }
    
}
