package ru.autohelp;

import com.cloudhopper.smpp.pdu.DeliverSm;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.autohelp.dbcontrollers.AutoEventsJpaController;
import ru.autohelp.dbcontrollers.AutoUsersJpaController;
import ru.autohelp.dbcontrollers.BlacklistJpaController;
import ru.autohelp.dbentity.AutoEventType;
import ru.autohelp.dbentity.AutoEvents;
import ru.autohelp.dbentity.AutoUsers;
import ru.autohelp.http.SmsController;
import ru.autohelp.mail.EmailController;
import ru.autohelp.model.megafon.MegaFonSmsResponse;
import ru.autohelp.utils.Common;
import ru.vehicleutils.models.VehicleNumber;
import static ru.vehicleutils.utils.Utils.getVehicleNumberByText;

/**
 *
 * @author ivan
 */
@Component
public class LogicHandler {
    private static Logger log = LogManager.getLogger();
    
    @Autowired
    private SmsController smsController;
    @Autowired
    private EmailController emailController;
    
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("ru.112-24");
    
    @Value("${sms.text.invalidformat}")
    private String smsTextInvalidformat;
    @Value("${sms.text.invalidformat.unreg}")
    private String smsTextInvalidformatUnreg;    
    @Value("${sms.text.emptyreg}")
    private String smsTextEmptyreg;
    @Value("${sms.text.emptyreg.unreg}")
    private String smsTextEmptyregUnreg;    
    @Value("${sms.text.dstunreg}")
    private String smsTextDstUnreg;
    @Value("${sms.text.dstunreg.unreg}")
    private String smsTextDstunregUnreg;
    @Value("${sms.text.chatempty}")
    private String smsTextChatEmpty;
    
    private final BlacklistJpaController blacklistJpaController;
    private final AutoUsersJpaController autoUsersJpaController;
    private final AutoEventsJpaController autoEventsJpaController;
    
    @PostConstruct
    public void init(){
        smsTextInvalidformat = Common.latin1ToUtf8(smsTextInvalidformat);
        smsTextInvalidformatUnreg = Common.latin1ToUtf8(smsTextInvalidformatUnreg);
        smsTextEmptyreg = Common.latin1ToUtf8(smsTextEmptyreg);
        smsTextEmptyregUnreg = Common.latin1ToUtf8(smsTextEmptyregUnreg);
        smsTextDstUnreg = Common.latin1ToUtf8(smsTextDstUnreg);
        smsTextDstunregUnreg = Common.latin1ToUtf8(smsTextDstunregUnreg);
        smsTextChatEmpty = Common.latin1ToUtf8(smsTextChatEmpty);
    }

    public LogicHandler() {
        blacklistJpaController = new BlacklistJpaController(emf);
        autoUsersJpaController = new AutoUsersJpaController(emf);
        autoEventsJpaController = new AutoEventsJpaController(emf);
    }
    
    private Long stringToLong(String value){
        Long result = null;
        try{
            result = new Long(value);
        }catch(NumberFormatException ex){
            log.error("String {} can't be long", value);
        }
        return result;
    }
    
    @Async
    public void smsHandle(DeliverSm deliverSm, String text){
        Long srcMsisdn = stringToLong(deliverSm.getSourceAddress().getAddress());
        if(srcMsisdn == null){
            return;
        }
        
        if(blacklistJpaController.findBlacklist(srcMsisdn.toString()) != null){
            log.warn("Number {} in blacklist", srcMsisdn);
            return;
        }
        AutoUsers srcUser = autoUsersJpaController.findByMsisdn(srcMsisdn.toString());
        VehicleNumber vehicleNumber = getVehicleNumberByText(text);
        if(vehicleNumber == null || vehicleNumber.getTextTail() == null || vehicleNumber.getTextTail().isEmpty()){
            if(srcUser != null){
                smsController.sendDirectly(srcMsisdn, smsTextInvalidformat);
            }else {
                smsController.sendDirectly(srcMsisdn, smsTextInvalidformatUnreg);
            }
            log.warn(vehicleNumber == null?"Vehicle not found":"Can't find text");
            return;
        }
        log.info("vehicleNumber: {}",vehicleNumber);
        if(vehicleNumber.getTransportReg() == null){
            if(srcUser != null){
                smsController.sendDirectly(srcMsisdn, smsTextEmptyreg);
            }else {
                smsController.sendDirectly(srcMsisdn, smsTextEmptyregUnreg);
            }
            log.warn("Vehicle REG not found");
            return;
        }
        
        AutoUsers dstUser = autoUsersJpaController.findByVehicleNumber(vehicleNumber);
        if(dstUser == null){
            if(srcUser != null){
                smsController.sendDirectly(srcMsisdn, smsTextDstUnreg);
            }else {
                smsController.sendDirectly(srcMsisdn, smsTextDstunregUnreg);
            }
            log.warn("Destination user not found");
            return;
        }
        
        String type = vehicleNumber.getTextTail().replaceAll("^([1-4])\\s?.*", "$1");
        String message = vehicleNumber.getTextTail().replaceAll("^[1-4]\\s?(.*)", "$1");
        switch(type){
            case "1":
                smsController.sendNotifyAsync(srcMsisdn.toString(), dstUser.getDef(), 1, message);
                break;
            case "2":
                smsController.sendNotifyAsync(srcMsisdn.toString(), dstUser.getDef(), 2, message);
                break;
            case "3":
                smsController.sendNotifyAsync(srcMsisdn.toString(), dstUser.getDef(), 3, message);
                break;
            case "4":
                if(message != null && message.isEmpty() == false){
                    sendAsChat(srcUser, dstUser, message);
                }else {
                    smsController.sendDirectly(srcMsisdn, smsTextChatEmpty);
                }                
                break;
            default:
                if(srcUser != null){
                    smsController.sendDirectly(srcMsisdn, smsTextInvalidformat);
                }else {
                    smsController.sendDirectly(srcMsisdn, smsTextInvalidformatUnreg);
                }
        }
    }
    
    public void sendAsChat(AutoUsers srcUser, AutoUsers dstUser, String message){
        try{
            String vehicleNumberString = dstUser.getTransportChars()+dstUser.getTransportId()+dstUser.getTransportReg();
            //Проверяем разрешил ли абонент-получатель приём смс
            if(dstUser.getNotificationSMS()){                
                //кол-во смс на счету у абонента-отправителя >= 5
                if(srcUser.getLimitSms() >= 5){
                    //отправляем смс-сообщение
                    Long to = stringToLong(dstUser.getDef());
                    if(to == null){
                        return;
                    }
                    MegaFonSmsResponse response = smsController.sendDirectly(to, message);
                    
                    //если статус отправки успешный
                    if(response != null && response.getResult().getStatus().getCode().equals(0)){
                        //Списивыем 5 смс сбаланса
                        int updateCount = autoUsersJpaController.updateBalance(srcUser, AutoUsers.Type.SMS, srcUser.getLimitSms()-5);
                        if(updateCount != 1){
                            log.error("Update balance from {} to {} for {} return {}",srcUser.getLimitSms(),srcUser.getLimitSms()-5,srcUser,updateCount);
                        }
                        log.info("Send chat from {} to {} with text \"{}\"/ New balance {}",srcUser, dstUser, message, srcUser.getLimitSms());
                        //Записываем для обоих абонентов событие чат
                        createAutoEvents(srcUser, AutoEvents.Type.SMS_CHAT, message);
                        createAutoEvents(dstUser, AutoEvents.Type.SMS_CHAT, message);
                    }else {
                        log.warn("Error in send sms");
                        sendToEmailAndWeb(srcUser, "Отправить SMS транспортному средству "+vehicleNumberString+" не удалось вследствии ошибки на этапе отправки");
                    }
                }else {
                    log.warn("sms balance subscriber {} Insufficient {}",srcUser,srcUser.getLimitSms());
                    sendToEmailAndWeb(srcUser, "Отправить SMS транспортному средству "+vehicleNumberString+" не удалось. На Вашем счету недостаточно средств");
                }
            }else {
                log.warn("{} diable receive sms",dstUser);
                sendToEmailAndWeb(srcUser, "Отправить SMS транспортному средству "+vehicleNumberString+" не удалось, т.к. пользователь отключил SMS уведомления");
            }
        }catch(Exception ex){
            log.error(ex.getMessage());
        }
    }
    
    private void sendToEmailAndWeb(AutoUsers user, String message){
        //-------- EMAIL --------
        //Проверяем подключены ли у пользователя уведомления, и задан ли email
        if(user != null && user.getWpUsersId() != null && 
                user.getNotificationEmail() &&
                user.getWpUsersId().getUserEmail() != null && user.getWpUsersId().getUserEmail().isEmpty() == false){
            if(emailController.sendEmail(user.getWpUsersId().getUserEmail(), message)){
                createAutoEvents(user, AutoEvents.Type.EMAIL_OTHER, message);
            }
        }
        //-------- SMS --------
        if(user != null && user.getWpUsersId() != null && user.getNotificationWeb()){
            createAutoEvents(user, AutoEvents.Type.WEB_OTHER, message);
        }
    }
    
    private AutoEvents createAutoEvents(AutoUsers user, AutoEvents.Type type, String eventDescription){
        AutoEvents autoEvent = new AutoEvents();
        autoEvent.setEventDatetime(new Date());
        autoEvent.setEventDescription(eventDescription);
        autoEvent.setEventType(new AutoEventType(type.getId()));
        autoEvent.setWpUsersId(user.getWpUsersId());        
        try{
            autoEventsJpaController.create(autoEvent);
            return autoEvent;
        }catch(Exception ex){
            log.error(ex.getMessage());
            return null;
        }
    }
}
