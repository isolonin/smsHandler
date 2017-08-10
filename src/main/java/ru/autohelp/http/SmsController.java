package ru.autohelp.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import ru.autohelp.model.megafon.MegaFonSmsResponse;
import ru.autohelp.models.MegaFonSms;

/**
 *
 * @author Ivan
 */
@Component
public class SmsController {
    private static org.apache.logging.log4j.Logger log = LogManager.getLogger();
    @Value("${notify.local.path}")
    private String notifyLocalPath;
    @Value("${sms.megafon.path}")
    private String smsMegafonPath;
    @Value("${sms.megafon.token}")
    private String smsMegafonToken;
    @Value("${sms.megafon.src}")
    private String smsMegafonSrc;
    
    //curl -X POST -H "Content-Type: application/json" -H 'Authorization: Basic Q05UX2N5YmVyc3Q6b2hlVnVwa3U=' -d '{"from":"112-24.ru","to":79200014949,"message":"Тест. Сообщения работают"}' 'https://a2p-api.megalabs.ru/sms/v1/sms'
    public MegaFonSmsResponse sendDirectly(String to, String message){
        try{
            log.info("Send message \"{}\" from \"{}\" to \"{}\"",message, smsMegafonSrc, to);
            HttpClient httpClient = HttpClientBuilder.create().build();
            MegaFonSms sms = new MegaFonSms(smsMegafonSrc, to, message);
            JSONObject smsJson = new JSONObject(sms);
            HttpPost post = new HttpPost(smsMegafonPath);
            post.addHeader("Content-Type", "application/json");
            post.addHeader("Authorization", "Basic "+smsMegafonToken);
            post.setEntity(new StringEntity(smsJson.toString()));
            
            log.info("Send: {}",post.toString());
            HttpResponse response = httpClient.execute(post);
            String responseString = getContextFromResponse(response);
            
            log.info("Recv from MF: {} ({})",response.getStatusLine(), responseString);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(responseString, MegaFonSmsResponse.class);
        }catch(Exception ex){
            log.error(ex.getMessage());
        }
        return null;
    }
    
    @Async
    public Future<Boolean> sendNotifyAsync(String src, String dst, Integer type, String message){
        return new AsyncResult<>(sendNotify(src, dst, type, message));
    }
    
    public boolean sendNotify(String src, String dst, Integer type, String message){
        try{
            StringBuilder sb = new StringBuilder(notifyLocalPath);
            sb.append("def_initiator=");
            sb.append(src);
            sb.append("&def=");
            sb.append(dst);
            sb.append("&event_type=");
            sb.append(type);
            if(message != null){
                sb.append("&event_description=");
                sb.append(URLEncoder.encode(message,"UTF-8"));
            }
            
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(sb.toString());
            log.info("Send: {}",request.toString());
            HttpResponse response = httpClient.execute(request);
            String responseString = getContextFromResponse(response);
            log.info("Recv local: {} ({})",response.getStatusLine(), responseString);
            
            return response.getStatusLine().getStatusCode() == 220;
        }catch(Exception ex){
            log.error(ex.getMessage());
        }
        return false;
    }
    
    private String getContextFromResponse(HttpResponse response){
        try{
            HttpEntity entity = response.getEntity();
            ContentType contentType = ContentType.getOrDefault(entity);
            Charset charset = contentType.getCharset();
            
            BufferedReader rd = null;
            if(charset != null){
                rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), charset));
            }else {
                rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            }
            StringBuilder resultText = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                resultText.append(line);
            }
            return resultText.toString();
        }catch(Exception ex){
            log.error(ex.getMessage());
        }
        return null;
    }
}
