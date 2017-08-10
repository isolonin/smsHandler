package ru.autohelp.utils;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.BaseSm;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.tlv.Tlv;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import ru.autohelp.models.PartsContainer;

/**
 *
 * @author isolonin
 */
public class SmBuilder {
    private static org.apache.logging.log4j.Logger log = LogManager.getLogger();
    
    //Коллекция сообщений формата количество частей = контенер
    private Map<Integer, PartsContainer> partsCollection = new HashMap<>();
    
    public BaseSm buildSAR(BaseSm baseSm){
        int referenceNumber = 0;
        byte totalPartCount = 0;
        byte offsetNumber = 0;
        Tlv sarMsgRefNum;
        Tlv sarTotalSermengt;
        Tlv sarSegmentSeqNum;
        byte[] shortMessage = baseSm.getShortMessage();
        
        if((sarMsgRefNum = baseSm.getOptionalParameter(SmppConstants.TAG_SAR_MSG_REF_NUM)) != null){
            referenceNumber = byteArrayToInt(sarMsgRefNum.getValue());
        }
        if((sarTotalSermengt = baseSm.getOptionalParameter(SmppConstants.TAG_SAR_TOTAL_SEGMENTS)) != null){
            totalPartCount = sarTotalSermengt.getValue()[0];
        }
        if((sarSegmentSeqNum = baseSm.getOptionalParameter(SmppConstants.TAG_SAR_SEGMENT_SEQNUM)) != null){
            offsetNumber = sarSegmentSeqNum.getValue()[0];
        }
        
        //Создаём хэш-ключ из соединения значений адреса отправителя, получателя и reference number
        Integer hashCode = (baseSm.getSourceAddress().getAddress()+baseSm.getDestAddress().getAddress()+referenceNumber).hashCode();
        
        PartsContainer part = partsCollection.get(hashCode);
        //Если в коллекции нет частей сообщения с полученным хэшом
        if(part == null){
            //Создаём новый элемент коллекции 
            part = new PartsContainer(totalPartCount, offsetNumber - 1, shortMessage);            
            if(baseSm.getRegisteredDelivery() == 1){
                part.setRegisteredDelivery(baseSm.getRegisteredDelivery());
            }
            
            partsCollection.put(hashCode, part);
            
        }else {            
            part.addMessage(offsetNumber - 1, shortMessage);
            partsCollection.put(hashCode, part);
            
            //Если все части сообщений получены
            if(part.isFinish()){
                //log.info("concationation finish");
                
                //Создаём submit_sm или deliver_sm в зависимости от того, какого типа сообщение было получено последним
                BaseSm returnSm = null;
                if(baseSm instanceof SubmitSm){
                    returnSm = copySubmitSm((SubmitSm)baseSm);
                }                
                if(baseSm instanceof DeliverSm){
                    returnSm = copyDeliverSm((DeliverSm)baseSm);
                }
                
                //Если хотябы в одной из частей сообщения был запроше отчёт о доставке устанавливаем такой же запрос в создаваемом сообщении
                if(part.getRegisteredDelivery() == 1){
                    returnSm.setRegisteredDelivery((byte)0x1);
                }
                
                try {
                    String smText = "";
                    //Объединяем тексты всех сообщений в переменной smText
                    for(byte[] message: part.getMessageParts()){
                        switch(baseSm.getDataCoding()){
                            case SmppConstants.DATA_CODING_UCS2:
                                smText += CharsetUtil.decode(message, "UCS-2");
                                break;
                            case SmppConstants.DATA_CODING_DEFAULT:
                                smText += CharsetUtil.decode(message, "GSM");                            
                                break;
                        }
                        //smText += new String(message, "UTF-16BE");
                    }
                    
                    //Если текст не пустой и длинна сообщения больше 255 символов
                    if(smText.isEmpty() == false && smText.getBytes().length > 255){
                        //Обнуляем поле ShortMessage                        
                        returnSm.setShortMessage(null);                        
                        //переносим сообщение в опциональную часть MESSAGE_PAYLOAD                        
                        returnSm.setOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, smText.getBytes(), "message_payload"));
                    }else { //Если сообщение пустое или меньше 255 символов оставляем текст в ShortMessage
                        returnSm.setShortMessage(smText.getBytes());
                    }
                    
                    return returnSm;
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        }
        return null;
    }
            
    public BaseSm buildUDH(BaseSm baseSm){
        int udh_length  = 0;
        int referenceNumber = 0;
        byte totalPartCount = 0;
        byte offsetNumber = 0;
        byte InformationElementIndicator = 0;
        //byte InformationElementLength = 0;
        byte[] shortMessage = baseSm.getShortMessage();
       
        //Читаем заголовок UDH
        udh_length = shortMessage[0];
        InformationElementIndicator = shortMessage[1];
        if (InformationElementIndicator == 0x0) {
            //InformationElementLength = shortMessage[2];
            referenceNumber = shortMessage[3];
            totalPartCount = shortMessage[4];
            offsetNumber = shortMessage[5];
        }
        
        //Удаляем заголовок udh из сообщения
        shortMessage = Arrays.copyOfRange(shortMessage, udh_length + 1, shortMessage.length);
        
        //Создаём хэш-ключ из соединения значений адреса отправителя, получателя и reference number
        Integer hashCode = (baseSm.getSourceAddress().getAddress()+baseSm.getDestAddress().getAddress()+referenceNumber).hashCode();
        
        
        PartsContainer part = partsCollection.get(hashCode);
        //Если в коллекции нет частей сообщения с полученным хэшом
        if(part == null){
            //Создаём новую элемент коллекции 
            part = new PartsContainer(totalPartCount, offsetNumber - 1, shortMessage);            
            if(baseSm.getRegisteredDelivery() == 1){
                part.setRegisteredDelivery(baseSm.getRegisteredDelivery());
            }
            
            partsCollection.put(hashCode, part);    
            
        }else {
            //not first part
//            if(part.isFinish()){
//                log.info("concationation finish");
//            }
            
            part.addMessage(offsetNumber - 1, shortMessage);
            partsCollection.put(hashCode, part);
            
            //Если все части сообщений получены
            if(part.isFinish()){
                //log.info("concationation finish");
                
                //Создаём submit_sm или deliver_sm в зависимости от того, какого типа сообщение было получено последним
                BaseSm returnSm = null;
                if(baseSm instanceof SubmitSm){
                    returnSm = copySubmitSm((SubmitSm)baseSm);
                }                
                if(baseSm instanceof DeliverSm){
                    returnSm = copyDeliverSm((DeliverSm)baseSm);
                }
                
                //Если хотябы в одной из частей сообщения был запроше отчёт о доставке устанавливаем такой же запрос в создаваемом сообщении
                if(part.getRegisteredDelivery() == 1){
                    returnSm.setRegisteredDelivery((byte)0x1);
                }
                
//                if(returnSm.getDestAddress().getAddress().equals("79202599178")){
//                    System.out.println("catch");
//                }
                
                try {
                    String smText = "";
                    //Объединяем тексты всех сообщений в переменной smText
                    for(byte[] message: part.getMessageParts()){
                        switch(baseSm.getDataCoding()){
                            case SmppConstants.DATA_CODING_UCS2:
                                smText = CharsetUtil.decode(message, "UCS-2");
                                break;
                            case SmppConstants.DATA_CODING_DEFAULT:
                                smText = CharsetUtil.decode(message, "GSM");                            
                                break;
                        }
                        //smText += new String(message, "UTF-16BE");
                    }
                    
                    //Если текст не пустой и длинна сообщения больше 255 символов                    
                    if(smText != null && smText.getBytes().length > 255){
                        //Обнуляем поле ShortMessage                        
                        returnSm.setShortMessage(null);
                        
                        //переносим сообщение в опциональную часть MESSAGE_PAYLOAD                        
                        returnSm.setOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, smText.getBytes(), "message_payload"));
                    }else {
                        //Если сообщение пустое и меньше 255 символов оставляем текст в ShortMessage
                        returnSm.setShortMessage(smText.getBytes());
                    }
                    
                    return returnSm;
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        }
        
        return null;
    }
    
    private DeliverSm copyDeliverSm(DeliverSm sourceDeliverSm){
        DeliverSm deliverSm = new DeliverSm();
        deliverSm.setDataCoding(sourceDeliverSm.getDataCoding());
        deliverSm.setDestAddress(sourceDeliverSm.getDestAddress());
        deliverSm.setEsmClass(sourceDeliverSm.getEsmClass());
        deliverSm.setPriority(sourceDeliverSm.getPriority());
        deliverSm.setProtocolId(sourceDeliverSm.getProtocolId());
        deliverSm.setRegisteredDelivery(sourceDeliverSm.getRegisteredDelivery());
        deliverSm.setReplaceIfPresent(sourceDeliverSm.getReplaceIfPresent());
        deliverSm.setScheduleDeliveryTime(sourceDeliverSm.getScheduleDeliveryTime());
        deliverSm.setServiceType(sourceDeliverSm.getServiceType());
        deliverSm.setSourceAddress(sourceDeliverSm.getSourceAddress());
        deliverSm.setValidityPeriod(sourceDeliverSm.getValidityPeriod());

        ArrayList<Tlv> optionalParameters = sourceDeliverSm.getOptionalParameters();
        if(optionalParameters != null){
            for(Tlv tlv:optionalParameters){
                deliverSm.setOptionalParameter(new Tlv(tlv.getTag(), tlv.getValue(), tlv.getTagName()));
            }
        }
        
        return deliverSm;
    }
    
    private SubmitSm copySubmitSm(SubmitSm sourceSubmitSm){
        SubmitSm submitSm = new SubmitSm();
        submitSm.setDataCoding(sourceSubmitSm.getDataCoding());
        submitSm.setDestAddress(sourceSubmitSm.getDestAddress());
        submitSm.setEsmClass(sourceSubmitSm.getEsmClass());
        submitSm.setPriority(sourceSubmitSm.getPriority());
        submitSm.setProtocolId(sourceSubmitSm.getProtocolId());
        submitSm.setRegisteredDelivery(sourceSubmitSm.getRegisteredDelivery());
        submitSm.setReplaceIfPresent(sourceSubmitSm.getReplaceIfPresent());
        submitSm.setScheduleDeliveryTime(sourceSubmitSm.getScheduleDeliveryTime());
        submitSm.setServiceType(sourceSubmitSm.getServiceType());
        submitSm.setSourceAddress(sourceSubmitSm.getSourceAddress());
        submitSm.setValidityPeriod(sourceSubmitSm.getValidityPeriod());

        ArrayList<Tlv> optionalParameters = sourceSubmitSm.getOptionalParameters();
        if(optionalParameters != null){
            for(Tlv tlv:optionalParameters){
                submitSm.setOptionalParameter(new Tlv(tlv.getTag(), tlv.getValue(), tlv.getTagName()));
            }
        }
        
        return submitSm;
    }
    
    private Integer getHashCode(BaseSm submitSm){
        byte referenceNumber = 0;
        byte InformationElementIndicator = 0;
        byte[] shortMessage = submitSm.getShortMessage();
        
        InformationElementIndicator = shortMessage[1];
        if (InformationElementIndicator == 0x0) {
            referenceNumber = shortMessage[3];
        }        
        Integer hashCode = (submitSm.getSourceAddress().getAddress()+submitSm.getDestAddress().getAddress()+referenceNumber).hashCode();
        return hashCode;
    }
    
    public static int byteArrayToInt(byte[] b) {
        return  (b[1] & 0xFF) |
                (b[0] & 0xFF) << 8;
    }
    
    public String getMessageId(BaseSm baseSm) {        
        Integer hashCode = getHashCode(baseSm);
        PartsContainer part = partsCollection.get(hashCode);
        if(part != null){
            return part.getMessageId();
        }else {
            return null;
        }
    }

    public void setMessageId(BaseSm baseSm, String messageId) {        
        Integer hashCode = getHashCode(baseSm);
        
        PartsContainer part = partsCollection.get(hashCode);
        if(part != null){
            part.setMessageId(messageId);
        }
    }
    
    public void remove(BaseSm baseSm){
        Integer hashCode = getHashCode(baseSm);
        partsCollection.remove(hashCode);
    }
}
