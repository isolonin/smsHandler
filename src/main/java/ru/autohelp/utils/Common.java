package ru.autohelp.utils;

import java.io.UnsupportedEncodingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ivan
 */
public class Common {
    private static Logger log = LogManager.getLogger();
    
    public static String latin1ToUtf8(String string){
        try {
            return new String(string.getBytes("ISO-8859-1"),"UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error(ex.getMessage());
        }
        return string;
    }
}
