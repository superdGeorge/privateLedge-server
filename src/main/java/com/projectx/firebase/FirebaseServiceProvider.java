package com.projectx.firebase;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.projectx.firebase.FCMService.FCMServiceFactory;

public class FirebaseServiceProvider {
	private static final Logger log = Logger.getLogger(FirebaseServiceProvider.class.getSimpleName());
	
	private static final String ACCOUNT_CONFIG_PATH = "WEB-INF/acctConfig.json";
	
	private static final String FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
	
	private static FirebaseApp m_app;
	
	private static GoogleCredentials m_credentials;
	
	static {
		InputStream serviceAccount;
		try {
			serviceAccount = new FileInputStream(ACCOUNT_CONFIG_PATH);
			m_credentials = GoogleCredentials.fromStream(serviceAccount);
			FirebaseOptions options = new FirebaseOptions.Builder()
			    .setCredentials(m_credentials)
			    .build();
			m_app = FirebaseApp.initializeApp(options);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public static Firestore getFirestore() {
		return FirestoreClient.getFirestore(m_app);
	}
	
	public static FCMService getFCMService() {
		return FCMServiceFactory.instance.create(getFCMAccessToken());
	}

	
	private static AccessToken getFCMAccessToken() {
		GoogleCredentials credentials = m_credentials.createScoped(Arrays.asList(FCM_SCOPE));
		
		AccessToken token = credentials.getAccessToken();
		if(token == null || token.getExpirationTime().before(new Date())) {
			try {
				token = credentials.refreshAccessToken();
				
			} catch (IOException e) {
				log.warning("Failed to refresh FCM access token");
				e.printStackTrace();
			}
		}
		return token;
	}
}
