package ru.autohelp.models;

import com.cloudhopper.smpp.SmppSession;
import java.util.concurrent.ScheduledFuture;

/**
 *
 * @author ivan
 */
public class SMPPClientResult {
    private SmppSession smppSession;
    private ScheduledFuture<?> scheduleEnquireLink;

    public SmppSession getSmppSession() {
        return smppSession;
    }

    public void setSmppSession(SmppSession smppSession) {
        this.smppSession = smppSession;
    }

    public ScheduledFuture<?> getScheduleEnquireLink() {
        return scheduleEnquireLink;
    }

    public void setScheduleEnquireLink(ScheduledFuture<?> scheduleEnquireLink) {
        this.scheduleEnquireLink = scheduleEnquireLink;
    }
    
    
}
