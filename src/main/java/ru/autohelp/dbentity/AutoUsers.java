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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "auto_users")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AutoUsers.findAll", query = "SELECT a FROM AutoUsers a")
    , @NamedQuery(name = "AutoUsers.findById", query = "SELECT a FROM AutoUsers a WHERE a.id = :id")
    , @NamedQuery(name = "AutoUsers.findByDef", query = "SELECT a FROM AutoUsers a WHERE a.def = :def")
    , @NamedQuery(name = "AutoUsers.findByDEFtocheck", query = "SELECT a FROM AutoUsers a WHERE a.dEFtocheck = :dEFtocheck")
    , @NamedQuery(name = "AutoUsers.findByVehicleNumber", query = "SELECT a FROM AutoUsers a WHERE LOWER(a.transportId) =  LOWER(:transportId) and "
            + "LOWER(a.transportChars) =  LOWER(:transportChars) and "
            + "LOWER(a.transportReg) =  LOWER(:transportReg)")
    , @NamedQuery(name = "AutoUsers.findByTransportId", query = "SELECT a FROM AutoUsers a WHERE a.transportId = :transportId")
    , @NamedQuery(name = "AutoUsers.findByTransportChars", query = "SELECT a FROM AutoUsers a WHERE a.transportChars = :transportChars")
    , @NamedQuery(name = "AutoUsers.findByTransportReg", query = "SELECT a FROM AutoUsers a WHERE a.transportReg = :transportReg")
    , @NamedQuery(name = "AutoUsers.findByTransportIdTemp", query = "SELECT a FROM AutoUsers a WHERE a.transportIdTemp = :transportIdTemp")
    , @NamedQuery(name = "AutoUsers.findByTransportCharsTemp", query = "SELECT a FROM AutoUsers a WHERE a.transportCharsTemp = :transportCharsTemp")
    , @NamedQuery(name = "AutoUsers.findByTransportRegTemp", query = "SELECT a FROM AutoUsers a WHERE a.transportRegTemp = :transportRegTemp")
    , @NamedQuery(name = "AutoUsers.findByCertificateCheck", query = "SELECT a FROM AutoUsers a WHERE a.certificateCheck = :certificateCheck")
    , @NamedQuery(name = "AutoUsers.findByCertificate", query = "SELECT a FROM AutoUsers a WHERE a.certificate = :certificate")
    , @NamedQuery(name = "AutoUsers.findByConfirmationSMS", query = "SELECT a FROM AutoUsers a WHERE a.confirmationSMS = :confirmationSMS")
    , @NamedQuery(name = "AutoUsers.findByConfirmationSMSexpire", query = "SELECT a FROM AutoUsers a WHERE a.confirmationSMSexpire = :confirmationSMSexpire")
    , @NamedQuery(name = "AutoUsers.findByConfirmationSMScount", query = "SELECT a FROM AutoUsers a WHERE a.confirmationSMScount = :confirmationSMScount")
    , @NamedQuery(name = "AutoUsers.findByEventEvacuation", query = "SELECT a FROM AutoUsers a WHERE a.eventEvacuation = :eventEvacuation")
    , @NamedQuery(name = "AutoUsers.findByEventParking", query = "SELECT a FROM AutoUsers a WHERE a.eventParking = :eventParking")
    , @NamedQuery(name = "AutoUsers.findByEventAccident", query = "SELECT a FROM AutoUsers a WHERE a.eventAccident = :eventAccident")
    , @NamedQuery(name = "AutoUsers.findByNotificationSMS", query = "SELECT a FROM AutoUsers a WHERE a.notificationSMS = :notificationSMS")
    , @NamedQuery(name = "AutoUsers.findByNotificationCall", query = "SELECT a FROM AutoUsers a WHERE a.notificationCall = :notificationCall")
    , @NamedQuery(name = "AutoUsers.findByNotificationEmail", query = "SELECT a FROM AutoUsers a WHERE a.notificationEmail = :notificationEmail")
    , @NamedQuery(name = "AutoUsers.findByNotificationWeb", query = "SELECT a FROM AutoUsers a WHERE a.notificationWeb = :notificationWeb")
    , @NamedQuery(name = "AutoUsers.findByLimitMinutes", query = "SELECT a FROM AutoUsers a WHERE a.limitMinutes = :limitMinutes")
    , @NamedQuery(name = "AutoUsers.findByLimitSms", query = "SELECT a FROM AutoUsers a WHERE a.limitSms = :limitSms")
    , @NamedQuery(name = "AutoUsers.findByLimitIvr", query = "SELECT a FROM AutoUsers a WHERE a.limitIvr = :limitIvr")})
public class AutoUsers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 14)
    @Column(name = "DEF")
    private String def;
    @Size(max = 14)
    @Column(name = "DEF_to_check")
    private String dEFtocheck;
    @Size(max = 4)
    @Column(name = "transport_id")
    private String transportId;
    @Size(max = 6)
    @Column(name = "transport_chars")
    private String transportChars;
    @Size(max = 3)
    @Column(name = "transport_reg")
    private String transportReg;
    @Size(max = 4)
    @Column(name = "transport_id_temp")
    private String transportIdTemp;
    @Size(max = 6)
    @Column(name = "transport_chars_temp")
    private String transportCharsTemp;
    @Size(max = 3)
    @Column(name = "transport_reg_temp")
    private String transportRegTemp;
    @Column(name = "certificate_check")
    private Boolean certificateCheck;
    @Size(max = 200)
    @Column(name = "certificate")
    private String certificate;
    @Size(max = 64)
    @Column(name = "confirmation_SMS")
    private String confirmationSMS;
    @Basic(optional = false)
    @NotNull
    @Column(name = "confirmation_SMS_expire")
    @Temporal(TemporalType.TIMESTAMP)
    private Date confirmationSMSexpire;
    @Column(name = "confirmation_SMS_count")
    private Integer confirmationSMScount;
    @Column(name = "event_evacuation")
    private Boolean eventEvacuation;
    @Column(name = "event_parking")
    private Boolean eventParking;
    @Column(name = "event_accident")
    private Boolean eventAccident;
    @Column(name = "notification_SMS")
    private Boolean notificationSMS;
    @Column(name = "notification_call")
    private Boolean notificationCall;
    @Column(name = "notification_email")
    private Boolean notificationEmail;
    @Column(name = "notification_web")
    private Boolean notificationWeb;
    @Basic(optional = false)
    @NotNull
    @Column(name = "limit_minutes")
    private int limitMinutes;
    @Basic(optional = false)
    @NotNull
    @Column(name = "limit_sms")
    private int limitSms;
    @Basic(optional = false)
    @NotNull
    @Column(name = "limit_ivr")
    private int limitIvr;
    @JoinColumn(name = "wp_users_id", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private WpUsers wpUsersId;
    @JoinColumn(name = "city_id", referencedColumnName = "id")
    @ManyToOne
    private AutoCity cityId;

    public AutoUsers() {
    }

    public AutoUsers(Integer id) {
        this.id = id;
    }

    public AutoUsers(Integer id, String def, Date confirmationSMSexpire, int limitMinutes, int limitSms, int limitIvr) {
        this.id = id;
        this.def = def;
        this.confirmationSMSexpire = confirmationSMSexpire;
        this.limitMinutes = limitMinutes;
        this.limitSms = limitSms;
        this.limitIvr = limitIvr;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getDEFtocheck() {
        return dEFtocheck;
    }

    public void setDEFtocheck(String dEFtocheck) {
        this.dEFtocheck = dEFtocheck;
    }

    public String getTransportId() {
        return transportId;
    }

    public void setTransportId(String transportId) {
        this.transportId = transportId;
    }

    public String getTransportChars() {
        return transportChars;
    }

    public void setTransportChars(String transportChars) {
        this.transportChars = transportChars;
    }

    public String getTransportReg() {
        return transportReg;
    }

    public void setTransportReg(String transportReg) {
        this.transportReg = transportReg;
    }

    public String getTransportIdTemp() {
        return transportIdTemp;
    }

    public void setTransportIdTemp(String transportIdTemp) {
        this.transportIdTemp = transportIdTemp;
    }

    public String getTransportCharsTemp() {
        return transportCharsTemp;
    }

    public void setTransportCharsTemp(String transportCharsTemp) {
        this.transportCharsTemp = transportCharsTemp;
    }

    public String getTransportRegTemp() {
        return transportRegTemp;
    }

    public void setTransportRegTemp(String transportRegTemp) {
        this.transportRegTemp = transportRegTemp;
    }

    public Boolean getCertificateCheck() {
        return certificateCheck;
    }

    public void setCertificateCheck(Boolean certificateCheck) {
        this.certificateCheck = certificateCheck;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getConfirmationSMS() {
        return confirmationSMS;
    }

    public void setConfirmationSMS(String confirmationSMS) {
        this.confirmationSMS = confirmationSMS;
    }

    public Date getConfirmationSMSexpire() {
        return confirmationSMSexpire;
    }

    public void setConfirmationSMSexpire(Date confirmationSMSexpire) {
        this.confirmationSMSexpire = confirmationSMSexpire;
    }

    public Integer getConfirmationSMScount() {
        return confirmationSMScount;
    }

    public void setConfirmationSMScount(Integer confirmationSMScount) {
        this.confirmationSMScount = confirmationSMScount;
    }

    public Boolean getEventEvacuation() {
        return eventEvacuation;
    }

    public void setEventEvacuation(Boolean eventEvacuation) {
        this.eventEvacuation = eventEvacuation;
    }

    public Boolean getEventParking() {
        return eventParking;
    }

    public void setEventParking(Boolean eventParking) {
        this.eventParking = eventParking;
    }

    public Boolean getEventAccident() {
        return eventAccident;
    }

    public void setEventAccident(Boolean eventAccident) {
        this.eventAccident = eventAccident;
    }

    public Boolean getNotificationSMS() {
        return notificationSMS;
    }

    public void setNotificationSMS(Boolean notificationSMS) {
        this.notificationSMS = notificationSMS;
    }

    public Boolean getNotificationCall() {
        return notificationCall;
    }

    public void setNotificationCall(Boolean notificationCall) {
        this.notificationCall = notificationCall;
    }

    public Boolean getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(Boolean notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    public Boolean getNotificationWeb() {
        return notificationWeb;
    }

    public void setNotificationWeb(Boolean notificationWeb) {
        this.notificationWeb = notificationWeb;
    }

    public int getLimitMinutes() {
        return limitMinutes;
    }

    public void setLimitMinutes(int limitMinutes) {
        this.limitMinutes = limitMinutes;
    }

    public int getLimitSms() {
        return limitSms;
    }

    public void setLimitSms(int limitSms) {
        this.limitSms = limitSms;
    }

    public int getLimitIvr() {
        return limitIvr;
    }

    public void setLimitIvr(int limitIvr) {
        this.limitIvr = limitIvr;
    }

    public WpUsers getWpUsersId() {
        return wpUsersId;
    }

    public void setWpUsersId(WpUsers wpUsersId) {
        this.wpUsersId = wpUsersId;
    }

    public AutoCity getCityId() {
        return cityId;
    }

    public void setCityId(AutoCity cityId) {
        this.cityId = cityId;
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
        if (!(object instanceof AutoUsers)) {
            return false;
        }
        AutoUsers other = (AutoUsers) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return def+"("+id+")";
    }
    
    public enum Type {
        SMS(1),
        MINUTES(2),
        IVR(3);

        private final Integer id;

        private Type(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }
    }
}
