package com.projectx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.firebase.auth.FirebaseToken;
import com.plaid.client.response.Account;
import com.projectx.firebase.FirestoreManager;
import com.projectx.firebase.FirestoreManager.FirestoreManagerFactory;
import com.projectx.firebase.PlaidManager;
import com.projectx.firebase.PlaidManager.PlaidManagerFactory;

import datamodel.RequestParams;
import datamodel.UserInfo;
import util.Pair;
import util.StringUtil;

@SuppressWarnings("serial")
@WebServlet(
		name = "UserInfoServlet",
		urlPatterns = {"/userinfo"}
		)
public class UserInfoServlet extends BaseServlet {
	
	private static final Logger log = Logger.getLogger(UserInfoServlet.class.getName());
	
	private final FirestoreManager m_storeMgr = FirestoreManagerFactory.instance.create();
	
	private final PlaidManager m_plaidMgr = PlaidManagerFactory.instance.create();

	// check if user info is exist, create one if not, and return item ids on the user info. 
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws IOException {
		log.info("Retrieving UserInfo...");
		Pair<FirebaseToken, UserInfoServletParams> result = prepare(request, UserInfoServletParams.class);
		FirebaseToken decodedToken = result.getFirst();
		UserInfoServletParams params = result.getSecond();
		
		if(decodedToken != null) {
			log.info("Successfully verified the incoming request for UserInfo.");
			String uid = decodedToken.getUid();
			if(!StringUtil.isEmpty(uid)) {
				log.info("The request is from user :" + uid);
				UserInfo info = m_storeMgr.getUserInfo(uid);
				if(info != null) {
					log.info("User is found");
					// get all user account info
					if(info.getAccountIds() != null && !info.getAccountIds().isEmpty()) {
						
						Map<String, List<String>> tokenMap = new HashMap<>();
						
						// get acct id from store
						log.info(info.getAccountIds().size()+" account id(s) stored in Firestore.");
						for(String acctId : info.getAccountIds()) {
							String accessToken = m_storeMgr.getAccessToken(acctId);
							log.info("Fetching for acct id: "+ acctId +" with access token: "+accessToken);
							
							if(!tokenMap.containsKey(accessToken)) {
								tokenMap.put(accessToken, new ArrayList<String>());
							}
							tokenMap.get(accessToken).add(acctId);
						}
						
						// get acct info from plaid
						log.info("Getting accounts from Plaid.");
						List<Account> accts = new ArrayList<>();
						
						for(Entry<String, List<String>> entry : tokenMap.entrySet()) {
							List<Account> tmpAccts = m_plaidMgr.getAccountInfo(entry.getKey(), entry.getValue());
							
							log.info("Received "+tmpAccts.size()+" accounts from Plaid for accessToken: "+entry.getKey());
							accts.addAll(tmpAccts);
						}
						
						log.info("Returning "+accts.size()+" accounts from server.");
						addResponse("accounts", accts);
					}else {
						log.info("No account is found for the user.");
					}
				}else {
					log.info("User is not found, thus creating new user with UID: "+uid);
					info = new UserInfo();
					info.setUid(uid)
						.setEmail(decodedToken.getEmail())
						.setFcmToken(Arrays.asList(params.token));
					m_storeMgr.addUserInfo(info);
				}
			}else {
				addErrorMessage("UID from decoded token is empty.");
			}
		}

		prepareResponse(response);
	}
	
	private class UserInfoServletParams extends RequestParams{
		public String token;
	}
}




