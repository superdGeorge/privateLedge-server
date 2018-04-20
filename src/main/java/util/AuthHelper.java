package util;

import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

public class AuthHelper {

	
	public static FirebaseToken verifyToken(String token, List<String> errMsg) {
		
		try {
			return FirebaseAuth.getInstance().verifyIdTokenAsync(token).get();
		}catch(Exception e) {
			errMsg.add("Failed to verify the auth token. ERROR:"+e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}