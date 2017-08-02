package ru.autohelp.smpp;

import com.cloudhopper.smpp.SmppClient;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.autohelp.models.SMPPClientResult;

/**
 *
 * @author ivan
 */
public class SMPPClient implements Callable<SMPPClientResult>{
    private static final Logger log = LoggerFactory.getLogger(SMPPClient.class);
    private final SmppClient smppClient;
    private final SmppSessionConfiguration config;
    private final Client client;
    
    public SMPPClient(SmppClient smppClient, SmppSessionConfiguration config, Client client) {
        this.smppClient = smppClient;
        this.config = config;
        this.client = client;
    }
    
    public SMPPClientResult connect(){
        SMPPClientResult result = new SMPPClientResult();
        while(true){
            try {
                log.info("Try connection to {}:{} with system_id {}...",
                        config.getHost(),config.getPort(),config.getSystemId());
                SmppSession smppSession = smppClient.bind(config, new SmppSessionHandler(client));
                result.setSmppSession(smppSession);
                log.info("Success connection to {}:{} with system_id {}",
                        config.getHost(),config.getPort(),config.getSystemId());

                ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
                
                
                ScheduledFuture<?> scheduleEnquireLink = scheduledExecutor.scheduleWithFixedDelay(
                        new EnquireLinkHandler(smppSession), 1, 30, TimeUnit.SECONDS);
                result.setScheduleEnquireLink(scheduleEnquireLink);
                return result;
            } catch (SmppTimeoutException | SmppChannelException | UnrecoverablePduException | InterruptedException ex) {
                log.error("Exception: {}",ex.getMessage());
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                log.error("Sleep exception: {}",ex.getMessage());
            }
        }
    }

    @Override
    public SMPPClientResult call() throws Exception {
        return connect();
    }
}
