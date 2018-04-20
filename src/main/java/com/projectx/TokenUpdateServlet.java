package com.projectx;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.firebase.auth.FirebaseToken;
import com.projectx.firebase.FirestoreManager;
import com.projectx.firebase.FirestoreManager.FirestoreManagerFactory;

import datamodel.RequestParams;
import util.Pair;

@SuppressWarnings("serial")
@WebServlet(
		name = "TokenUpdateServlet",
		urlPatterns = {"/update/fcmtoken"}
		)
public class TokenUpdateServlet extends BaseServlet{
	
	private static final Logger log = Logger.getLogger(TokenUpdateServlet.class.getName());
	
	private static final FirestoreManager firestoreMgr = FirestoreManagerFactory.instance.create();
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws IOException {
		log.info("Received request for updating fcm tokens.");
		
		Pair<FirebaseToken, TokenUpdateServletParams> result = prepare(request, TokenUpdateServletParams.class);
		FirebaseToken decodedToken = result.getFirst();
		TokenUpdateServletParams params = result.getSecond();
		
		if(decodedToken != null || !params.needAuth) {
			log.info("Successfully verified the incoming request for token update.");
			firestoreMgr.updateFCMToken(params.currToken, params.prevToken);
			
			
		}else {
			addErrorMessage("UID from decoded token is empty.");
		}
		
		prepareResponse(response);
	}
	
	private class TokenUpdateServletParams extends RequestParams{
		public String currToken;
		
		public String prevToken;
	}
}
