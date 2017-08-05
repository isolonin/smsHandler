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
@Table(name = "auto_events")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AutoEvents.findAll", query = "SELECT a FROM AutoEvents a")
    , @NamedQuery(name = "AutoEvents.findById", query = "SELECT a FROM AutoEvents a WHERE a.id = :id")
    , @NamedQuery(name = "AutoEvents.findByEventDatetime", query = "SELECT a FROM AutoEvents a WHERE a.eventDatetime = :eventDatetime")
    , @NamedQuery(name = "AutoEvents.findByGuiltyTransport", query = "SELECT a FROM AutoEvents a WHERE a.guiltyTransport = :guiltyTransport")
    , @NamedQuery(name = "AutoEvents.findByGuiltyColor", query = "SELECT a FROM AutoEvents a WHERE a.guiltyColor = :guiltyColor")
    , @NamedQuery(name = "AutoEvents.findByEventDescription", query = "SELECT a FROM AutoEvents a WHERE a.eventDescription = :eventDescription")})
public class AutoEvents implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "event_datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventDatetime;
    @Size(max = 9)
    @Column(name = "guilty_transport")
    private String guiltyTransport;
    @Size(max = 50)
    @Column(name = "guilty_color")
    private String guiltyColor;
    @Size(max = 500)
    @Column(name = "event_description")
    private String eventDescription;
    @JoinColumn(name = "wp_users_id", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private WpUsers wpUsersId;
    @JoinColumn(name = "event_type", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private AutoEventType eventType;

    public AutoEvents() {
    }

    public AutoEvents(Integer id) {
        this.id = id;
    }

    public AutoEvents(Integer id, Date eventDatetime) {
        this.id = id;
        this.eventDatetime = eventDatetime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getEventDatetime() {
        return eventDatetime;
    }

    public void setEventDatetime(Date eventDatetime) {
        this.eventDatetime = eventDatetime;
    }

    public String getGuiltyTransport() {
        return guiltyTransport;
    }

    public void setGuiltyTransport(String guiltyTransport) {
        this.guiltyTransport = guiltyTransport;
    }

    public String getGuiltyColor() {
        return guiltyColor;
    }

    public void setGuiltyColor(String guiltyColor) {
        this.guiltyColor = guiltyColor;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public WpUsers getWpUsersId() {
        return wpUsersId;
    }

    public void setWpUsersId(WpUsers wpUsersId) {
        this.wpUsersId = wpUsersId;
    }

    public AutoEventType getEventType() {
        return eventType;
    }

    public void setEventType(AutoEventType eventType) {
        this.eventType = eventType;
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
        if (!(object instanceof AutoEvents)) {
            return false;
        }
        AutoEvents other = (AutoEvents) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.autohelp.dbentity.AutoEvents[ id=" + id + " ]";
    }
    
    public enum Type {
        VOICE_PARKING(1),
        VOICE_EVACUATION(2),
        VOICE_ACCIDENT(3),
        VOICE_OTHER(4),
        VOICE_DIALOG(5),
        SMS_PARKING(6),
        SMS_EVACUATION(7),
        SMS_ACCIDENT(8),
        SMS_OTHER(9),
        SMS_CHAT(10),
        WEB_PARKING(11),
        WEB_EVACUATION(12),
        WEB_ACCIDENT(13),
        WEB_OTHER(14),
        EMAIL_PARKING(15),
        EMAIL_EVACUATION(16),
        EMAIL_ACCIDENT(17),
        EMAIL_OTHER(18);

        private final Integer id;

        private Type(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }
    }
}
