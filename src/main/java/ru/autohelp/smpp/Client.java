package ru.autohelp.smpp;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.type.LoggingOptions;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.autohelp.LogicHandler;
import ru.autohelp.models.SMPPClientResult;

/**
 *
 * @author ivan
 */
@Component
public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    @Value("${smpp.host}")
    private String smppHost;
    @Value("${smpp.port}")
    private String smppPort;
    @Value("${smpp.login}")
    private String smppLogin;
    @Value("${smpp.password}")
    private String smppPassword;
    
    private LogicHandler logicHandler;
    
    private DefaultSmppClient smppClient;
    private SmppSessionConfiguration config;
    
    private Future<SMPPClientResult> connectionResultFuture;
    
    public void run(){
        smppClient = new DefaultSmppClient();
        config = new SmppSessionConfiguration();
        LoggingOptions loggingOptions = new LoggingOptions();
        loggingOptions.setLogBytes(false);
        loggingOptions.setLogPdu(false);
        config.setLoggingOptions(loggingOptions);
        config.setType(SmppBindType.RECEIVER);
        config.setHost(smppHost);
        config.setPort(new Integer(smppPort));
        config.setName(smppLogin);
        config.setPassword(smppPassword);
        config.setBindTimeout(30000);
        config.setConnectTimeout(30000);
        
        connectionResultFuture = Executors.newSingleThreadExecutor().submit(new SMPPClient(smppClient, config, this));
    }
    
    public void reconnect(){
        try {
            SMPPClientResult connectionResult = connectionResultFuture.get();
            connectionResult.getScheduleEnquireLink().cancel(true);
            connectionResult.getSmppSession().destroy();
        
            connectionResultFuture = Executors.newSingleThreadExecutor().submit(new SMPPClient(smppClient, config, this));
        } catch (InterruptedException | ExecutionException ex) {
            log.error(ex.getMessage());
        }
    }

    public LogicHandler getLogicHandler() {
        return logicHandler;
    }
    
    
}
