package ru.autohelp.smpp;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author isolonin
 */
public class EnquireLinkHandler implements Runnable{
    private static org.apache.logging.log4j.Logger log = LogManager.getLogger();
    private SmppSession smppSession;
    
    public EnquireLinkHandler(SmppSession session) {
        this.smppSession = session;
    }

    @Override
    public void run() {
        try {
            log.debug("send sync enquire_link");
            EnquireLinkResp enquireLinkResp = smppSession.enquireLink(new EnquireLink(), 25000);
            log.debug("enquire_link_resp #1: commandStatus [" + enquireLinkResp.getCommandStatus() + "=" + enquireLinkResp.getResultMessage() + "]");
        } catch (RecoverablePduException | UnrecoverablePduException | SmppTimeoutException | SmppChannelException | InterruptedException ex) {
            log.error(ex.getMessage());
        }
    }   
}
