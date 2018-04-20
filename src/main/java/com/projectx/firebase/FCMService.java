package com.projectx.firebase;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.auth.oauth2.AccessToken;

import util.JsonHelper;

public class FCMService {
	
	private static final Logger log	= Logger.getLogger(FCMService.class.getSimpleName());
	
	private static final String BASE_URL = "https://fcm.googleapis.com/v1/projects/";
	
	private static final String PROJECT_ID = "mengmengspricetag";
	
	private static final String FCM_SEND_ENDPOINT = "/messages:send";
	
	private final HttpURLConnection m_connection;
	
	public FCMService(AccessToken token) {
		m_connection = initConnection(token);
	}
	
	private HttpURLConnection initConnection(AccessToken token) {
		try {
			URL url = new URL(BASE_URL + PROJECT_ID + FCM_SEND_ENDPOINT); 
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Authorization", "Bearer " + token.getTokenValue());
			httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
			httpURLConnection.setDoOutput(true);
			return httpURLConnection;
		}catch(Exception e) {
			log.warning("Failed to open connection to FCM Endpoint: " + FCM_SEND_ENDPOINT);
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean pushNotification(Map<String, Object> map) {
		if(m_connection == null) {
			log.warning("Failed to connect to FCM.");
			return false;
		}
		
		log.info("Pushing notification..");
		
		boolean isSuccessful = false;
		int code = -1;
		try (DataOutputStream output = new DataOutputStream(m_connection.getOutputStream())){
			Map<String, Object> payload = new HashMap<>(); 
			payload.put("message", map);
			log.info("Sending message: " + JsonHelper.toJson(payload));
			output.writeBytes(JsonHelper.toJson(payload));
			
			code = m_connection.getResponseCode();
			log.info("Received response code: " + code);
			isSuccessful = isSuccessful(code);
			if(!isSuccessful) {
				InputStream input = m_connection.getErrorStream();
				log.warning(readString(input, "UTF-8"));
				input.close();
			}
			
			output.close();
		} catch (IOException e) {
			log.warning("Failed to get response from the connection");				
			e.printStackTrace();
		}finally {
			m_connection.disconnect();
		}
		
		return isSuccessful;
	}

    private String readString(InputStream inputStream, String encoding) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString(encoding);
    }
	
	private boolean isSuccessful(int code) {
		log.info("Received response code from FCM :" + code);
		return code>=200 && code < 300;
	}
	
	public enum FCMServiceFactory{
		instance;
		
		public FCMService create(AccessToken token) {
			return new FCMService(token);
		}
	}
}
