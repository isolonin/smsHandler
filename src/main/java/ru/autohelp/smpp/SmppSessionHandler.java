package ru.autohelp.smpp;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.util.SmppUtil;
import org.apache.logging.log4j.LogManager;
import ru.autohelp.utils.SmBuilder;

/**
 *
 * @author ivan
 */
public class SmppSessionHandler extends DefaultSmppSessionHandler{
    private static org.apache.logging.log4j.Logger log = LogManager.getLogger();
    private final Client client;
    private final SmBuilder smBuilder;

    public SmppSessionHandler(Client client) {
        this.client = client;
        smBuilder = new SmBuilder();
    }

    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        log.info("received PDU: {}", pduRequest);
        PduResponse response = pduRequest.createResponse();
        try{
            //Если получен deliver_sm
            if(pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
                DeliverSm deliverSm = (DeliverSm)pduRequest;

                //Если это отчёт о доставке
                if(deliverSm.getEsmClass() == SmppConstants.ESM_CLASS_MT_SMSC_DELIVERY_RECEIPT){

                }

                //Если сообщение содержит UDH - сообщение составное
                if(SmppUtil.isUserDataHeaderIndicatorEnabled(deliverSm.getEsmClass())) {
                    //Отдаём все части сообщения сборщику
                    DeliverSm longDeliverSm = (DeliverSm)smBuilder.buildUDH(deliverSm);

                    //Если сборщик вернул не NULL значит сообщение собрано и его можно передавать в логику
                    if(longDeliverSm != null) {
                        String text;
                        //Если тело сообщения пустое, значит оно в MESSAGE_PAYLOAD
                        if(longDeliverSm.getShortMessage() == null) {
                            text = new String(longDeliverSm.getOptionalParameter(SmppConstants.TAG_MESSAGE_PAYLOAD).getValue());
                        }else {
                            text = new String(longDeliverSm.getShortMessage());
                        }                    
                        smBuilder.remove(deliverSm);

                        log.info("Recv text:\"{}\"",text);
                        client.getLogicHandler().smsHandle(longDeliverSm, text);
                    }
                }else {
                    String text = null;
                    switch(deliverSm.getDataCoding()){
                        case SmppConstants.DATA_CODING_UCS2:
                            //only for test
                            text = new String(deliverSm.getShortMessage());
    //                        text = CharsetUtil.decode(deliverSm.getShortMessage(), "UCS-2");
                            break;
                        case SmppConstants.DATA_CODING_DEFAULT:
                            text = CharsetUtil.decode(deliverSm.getShortMessage(), "GSM");                            
                            break;
                    }
                    log.info("Recv text:\"{}\"",text);
                    client.getLogicHandler().smsHandle(deliverSm, text);
                }

                log.info("send PDU response: {}",response);
                return response;
            }
            response.setCommandStatus(SmppConstants.STATUS_INVCMDID);
            log.error("send PDU response: {}", response);
        }catch(Exception ex){
            log.error("handle exception: {}",ex.getMessage());
            response.setCommandStatus(SmppConstants.STATE_UNKNOWN);            
        }
        return response;
    }
    
    @Override
    public void fireChannelUnexpectedlyClosed() {
        log.error("Connect close/ Reconnect");
        client.reconnect();
    }
}
