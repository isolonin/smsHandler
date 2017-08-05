package ru.autohelp.mail;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 *
 * @author Ivan
 */
@Component
public class EmailController {
    private final Logger log = LoggerFactory.getLogger(EmailController.class);
    @Value("${smtp.mta.host}")
    private String smtpMtaHost;
    @Value("${smtp.mta.port}")
    private Integer smtpMtaPort;
    @Value("${smtp.authuser}")
    private String smtpAuthUser;
    @Value("${smtp.authupass}")
    private String smtpAuthuPass;
    @Value("${notify.email.subject}")
    private String notifyEmailSubject;
    
    @Async
    public void smsEmail(String to, String message){
        try{
            MultiPartEmail email = new MultiPartEmail();
            email.setHostName(smtpMtaHost);
            email.setSmtpPort(smtpMtaPort == null?465:smtpMtaPort);
            email.setDebug(true);
            email.setAuthenticator(new DefaultAuthenticator(smtpAuthUser, smtpAuthuPass));
            email.setSSLOnConnect(true);
            email.setFrom(smtpAuthUser);
            email.setSubject(notifyEmailSubject);
            email.setMsg(message);
            email.addTo(to);
            email.send();
        } catch (EmailException ex) {
            log.error(ex.getMessage());
        }
    }
}
