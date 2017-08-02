package ru.autohelp.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author isolonin
 */
public class PartsContainer {
    private final Date createTime;
    private byte registeredDelivery;
    
    private final byte totalPartCount;
    List<byte[]> messageParts;
    private String messageId;
    
    public void addMessage(int offsetNumber, byte[] message){
        messageParts.add(offsetNumber,message);
    }

    public PartsContainer(byte totalPartCount, int offsetNumber, byte[] message) {
        this.createTime = new Date();
        this.totalPartCount = totalPartCount;
        
        messageParts = new ArrayList<>(totalPartCount);
        
        //if array is empty - zerroing it
        if(messageParts.size() < offsetNumber){
            for(int i=0; i<(offsetNumber-messageParts.size());i++){
                messageParts.add(i,null);
            }
        }
        messageParts.add(offsetNumber,message);
    }

    public byte getRegisteredDelivery() {
        return registeredDelivery;
    }

    public void setRegisteredDelivery(byte registeredDelivery) {
        this.registeredDelivery = registeredDelivery;
    }
    
    public boolean isFinish(){
        return messageParts.size() == totalPartCount;
    }

    public List<byte[]> getMessageParts() {
        return messageParts;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
