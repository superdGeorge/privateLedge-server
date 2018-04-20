package com.projectx;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.projectx.firebase.FCMService;
import com.projectx.firebase.FirebaseServiceProvider;
import com.projectx.firebase.FirestoreManager;
import com.projectx.firebase.FirestoreManager.FirestoreManagerFactory;

import datamodel.NotificationParams;

import java.util.logging.Logger;

import util.JsonHelper;
import util.StringUtil;

@SuppressWarnings("serial")
@WebServlet(
		name = "UpdateItemServlet",
		urlPatterns = {"/update/plaid/item"}
		)
public class UpdateItemServlet extends BaseServlet{
	
	private static final Logger log = Logger.getLogger(UpdateItemServlet.class.getName());
	
	private static final FirestoreManager firestoreMgr = FirestoreManagerFactory.instance.create();
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)  
			throws IOException {
		log.info("Received request from Plaid webhook.");
		NotificationParams params = JsonHelper.fromJson(request.getReader(), NotificationParams.class);
		
		// add reg id and get from firestore
		log.info("Item id: " + params.item_id + ", count:	 " + params.new_transactions);
		List<String> regTokens = firestoreMgr.getRegistrationIdByItemId(params.item_id);
		String accountId = firestoreMgr.getAccountIdByItemId(params.item_id);
		params.accountId = accountId;
		
		log.info("Sending messages to "+ regTokens.size()+ " devices.");
		for(String regToken : regTokens) {
			if(!StringUtil.isEmpty(regToken)) {
				
				Map<String, Object> message = new HashMap<>();
				message.put("token", regToken);
				message.put("data", params);
				
				FCMService fcm = FirebaseServiceProvider.getFCMService();
				if(!fcm.pushNotification(message)) {
					// retry
					log.warning("Failed in pushing notification, needs to retry");
				}else {
					log.info("Successfully pushed notification to FCM");
				}
			}
		}
	}		
}