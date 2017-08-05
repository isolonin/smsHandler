/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.autohelp.dbentity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Ivan
 */
@Entity
@Table(name = "wp_users")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "WpUsers.findAll", query = "SELECT w FROM WpUsers w")
    , @NamedQuery(name = "WpUsers.findById", query = "SELECT w FROM WpUsers w WHERE w.id = :id")
    , @NamedQuery(name = "WpUsers.findByUserLogin", query = "SELECT w FROM WpUsers w WHERE w.userLogin = :userLogin")
    , @NamedQuery(name = "WpUsers.findByUserPass", query = "SELECT w FROM WpUsers w WHERE w.userPass = :userPass")
    , @NamedQuery(name = "WpUsers.findByUserNicename", query = "SELECT w FROM WpUsers w WHERE w.userNicename = :userNicename")
    , @NamedQuery(name = "WpUsers.findByUserEmail", query = "SELECT w FROM WpUsers w WHERE w.userEmail = :userEmail")
    , @NamedQuery(name = "WpUsers.findByUserUrl", query = "SELECT w FROM WpUsers w WHERE w.userUrl = :userUrl")
    , @NamedQuery(name = "WpUsers.findByUserRegistered", query = "SELECT w FROM WpUsers w WHERE w.userRegistered = :userRegistered")
    , @NamedQuery(name = "WpUsers.findByUserActivationKey", query = "SELECT w FROM WpUsers w WHERE w.userActivationKey = :userActivationKey")
    , @NamedQuery(name = "WpUsers.findByUserStatus", query = "SELECT w FROM WpUsers w WHERE w.userStatus = :userStatus")
    , @NamedQuery(name = "WpUsers.findByDisplayName", query = "SELECT w FROM WpUsers w WHERE w.displayName = :displayName")})
public class WpUsers implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "user_login")
    private String userLogin;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "user_pass")
    private String userPass;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "user_nicename")
    private String userNicename;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "user_email")
    private String userEmail;
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 100)
    @Column(name = "user_url")
    private String userUrl = "";
    @Basic(optional = false)
    @NotNull
    @Column(name = "user_registered")
    @Temporal(TemporalType.TIMESTAMP)
    private Date userRegistered;
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 255)
    @Column(name = "user_activation_key")
    private String userActivationKey = "";
    @Basic(optional = false)
    @NotNull
    @Column(name = "user_status")
    private int userStatus;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "display_name")
    private String displayName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "wpUsersId")
    private Collection<AutoUsers> autoUsersCollection;

    public WpUsers() {
    }

    public WpUsers(Long id) {
        this.id = id;
    }

    public WpUsers(Long id, String userLogin, String userPass, String userNicename, String userEmail, String userUrl, Date userRegistered, String userActivationKey, int userStatus, String displayName) {
        this.id = id;
        this.userLogin = userLogin;
        this.userPass = userPass;
        this.userNicename = userNicename;
        this.userEmail = userEmail;
        this.userUrl = userUrl;
        this.userRegistered = userRegistered;
        this.userActivationKey = userActivationKey;
        this.userStatus = userStatus;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public String getUserNicename() {
        return userNicename;
    }

    public void setUserNicename(String userNicename) {
        this.userNicename = userNicename;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    public Date getUserRegistered() {
        return userRegistered;
    }

    public void setUserRegistered(Date userRegistered) {
        this.userRegistered = userRegistered;
    }

    public String getUserActivationKey() {
        return userActivationKey;
    }

    public void setUserActivationKey(String userActivationKey) {
        this.userActivationKey = userActivationKey;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
        if (!(object instanceof WpUsers)) {
            return false;
        }
        WpUsers other = (WpUsers) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.autohelp.dbentity.WpUsers[ id=" + id + " ]";
    }    
}
