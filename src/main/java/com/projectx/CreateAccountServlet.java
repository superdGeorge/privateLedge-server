package com.projectx;

import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.firebase.auth.FirebaseToken;
import com.plaid.client.response.Account;
import com.projectx.firebase.FirestoreManager;
import com.projectx.firebase.FirestoreManager.FirestoreManagerFactory;
import com.projectx.firebase.PlaidManager.PlaidManagerFactory;
import datamodel.RequestParams;
import com.projectx.firebase.PlaidManager;
import util.Pair;

@SuppressWarnings("serial")
@WebServlet(
		name = "CreateAccountServlet",
		urlPatterns = {"/create/acct"}
		)
public class CreateAccountServlet extends BaseServlet{
	private final FirestoreManager m_storeMgr = FirestoreManagerFactory.instance.create();

	private final PlaidManager m_plaidMgr = PlaidManagerFactory.instance.create();

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)  
			throws IOException {

		Pair<FirebaseToken, CreateAccountRequestParams> result = prepare(request, CreateAccountRequestParams.class);
		FirebaseToken decodedToken = result.getFirst();
		CreateAccountRequestParams params = result.getSecond();
		
		if(decodedToken != null && params != null) {
			String publicToken = params.publicToken;
			List<String> acctIds = params.acctIds;

			// exchange for access token
			if(acctIds != null && acctIds.size()>0) {
				Pair<String, String> tokenResult = m_plaidMgr.getAccessToken(publicToken);

				if(tokenResult != null) {
					String accessToken = tokenResult.getFirst(), itemId = tokenResult.getSecond();
					
					// get account info 	
					List<Account> accts = m_plaidMgr.getAccountInfo(accessToken, acctIds);
					
					if(accts != null && !accts.isEmpty()) {
						// store to firestore
						for(String id : acctIds) {
							m_storeMgr.addAccessToken(id, itemId, accessToken);
							m_storeMgr.addAccountId(decodedToken.getUid(), id);
							
							m_storeMgr.addItemIdToUID(decodedToken.getUid(), id, itemId);
						}
						
						
						addResponse("accounts", accts);
					}else {
						addErrorMessage("Account Provider: (Plaid) cannot provide account details");
					}
				}else {
					addErrorMessage("Failed to exchange access token from Plaid.");
				}
			}else {
				addErrorMessage("No acct id is received in requeset. ");
			}
		}

		prepareResponse(response);
	}

	private class CreateAccountRequestParams extends RequestParams{
		public String publicToken;

		public List<String> acctIds;
	}
}

