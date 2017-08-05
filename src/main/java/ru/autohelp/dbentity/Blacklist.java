/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.autohelp.dbentity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Ivan
 */
@Entity
@Table(name = "blacklist")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Blacklist.findAll", query = "SELECT b FROM Blacklist b")
    , @NamedQuery(name = "Blacklist.findByMsisdn", query = "SELECT b FROM Blacklist b WHERE b.msisdn = :msisdn")
    , @NamedQuery(name = "Blacklist.findByInsertTime", query = "SELECT b FROM Blacklist b WHERE b.insertTime = :insertTime")})
public class Blacklist implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 11)
    @Column(name = "msisdn")
    private String msisdn;
    @Basic(optional = false)
    @NotNull
    @Column(name = "insert_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date insertTime;

    public Blacklist() {
    }

    public Blacklist(String msisdn) {
        this.msisdn = msisdn;
    }

    public Blacklist(String msisdn, Date insertTime) {
        this.msisdn = msisdn;
        this.insertTime = insertTime;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (msisdn != null ? msisdn.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Blacklist)) {
            return false;
        }
        Blacklist other = (Blacklist) object;
        if ((this.msisdn == null && other.msisdn != null) || (this.msisdn != null && !this.msisdn.equals(other.msisdn))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.autohelp.dbentity.Blacklist[ msisdn=" + msisdn + " ]";
    }
    
}
