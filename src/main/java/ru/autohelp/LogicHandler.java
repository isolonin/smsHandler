package ru.autohelp;

import com.cloudhopper.smpp.pdu.DeliverSm;
import java.util.Date;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.vehicleutils.models.VehicleNumber;
import static ru.vehicleutils.utils.Utils.getVehicleNumberByText;

/**
 *
 * @author ivan
 */
@Component
public class LogicHandler {
    private final Logger log = LoggerFactory.getLogger(LogicHandler.class);
    
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

    public LogicHandler() {
        blacklistJpaController = new BlacklistJpaController(emf);
        autoUsersJpaController = new AutoUsersJpaController(emf);
        autoEventsJpaController = new AutoEventsJpaController(emf);
    }
    
    @Async
    public void smsHandle(DeliverSm deliverSm, String text){
        String srcMsisdn = deliverSm.getSourceAddress().getAddress();
        if(blacklistJpaController.findBlacklist(srcMsisdn) != null){
            log.warn("Number {} in blacklist", srcMsisdn);
            return;
        }
        AutoUsers srcUser = autoUsersJpaController.findByMsisdn(srcMsisdn);
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
                smsController.sendNotifyAsync(srcMsisdn, deliverSm.getDestAddress().getAddress(), 1, message);
                break;
            case "2":
                smsController.sendNotifyAsync(srcMsisdn, deliverSm.getDestAddress().getAddress(), 2, message);
                break;
            case "3":
                smsController.sendNotifyAsync(srcMsisdn, deliverSm.getDestAddress().getAddress(), 3, message);
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
                    MegaFonSmsResponse response = smsController.sendDirectly(dstUser.getDef(), message);
                    
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
