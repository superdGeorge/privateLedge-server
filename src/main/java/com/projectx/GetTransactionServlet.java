package com.projectx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.firebase.auth.FirebaseToken;
import com.plaid.client.response.TransactionsGetResponse;
import com.plaid.client.response.TransactionsGetResponse.Transaction;
import com.projectx.firebase.PlaidManager;
import com.projectx.firebase.FirestoreManager;
import com.projectx.firebase.FirestoreManager.FirestoreManagerFactory;
import com.projectx.firebase.PlaidManager.PlaidManagerFactory;

import datamodel.RequestParams;
import util.Pair;
import util.StringUtil;

@SuppressWarnings("serial")
@WebServlet(
		name = "GetTransactionServlet",
		urlPatterns = {"/get/txns"}
		)
public class GetTransactionServlet extends BaseServlet{

	private final static Logger log = Logger.getLogger(GetTransactionServlet.class.getSimpleName());
	
	private final PlaidManager plaidMgr = PlaidManagerFactory.instance.create();
	
	private final FirestoreManager firestoreMgr = FirestoreManagerFactory.instance.create();
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)  
			throws IOException {
		
		log.info("Received request to fetch new transactions from Plaid.");
		
		Pair<FirebaseToken, GetTransactionServletParams> result = prepare(request, GetTransactionServletParams.class);
		FirebaseToken decodedToken = result.getFirst();
		GetTransactionServletParams params = result.getSecond();
		
		if(decodedToken != null && params != null) {
			log.info("Processing request of type: "+params.fetchType);
			
			List<Transaction> txns = new ArrayList<>();
			
			if(params.acctMap !=null && params.acctMap.size() >0) {
				for(String acctId: params.acctMap.keySet()) {
					String accessToken = firestoreMgr.getAccessToken(acctId);
					if(StringUtil.isEmpty(accessToken))	continue;
					
					TransactionRequestParams requestParams = params.acctMap.get(acctId);
					do {
						TransactionsGetResponse getResult = plaidMgr.getTransaction(accessToken, 
																				   acctId, 
																				   requestParams); 
						txns.addAll(getResult.getTransactions());
						
						int offset = requestParams.offset + getResult.getTransactions().size();
						int newTransactions = getResult.getTotalTransactions() - offset;
						
						requestParams.offset = offset;
						requestParams.count = newTransactions;
					}while(requestParams.count>0);
				}
				
				log.info("Returning "+txns.size()+" transactions update to client");
				addResponse("txns", txns);
				log.info("The following txns are responsed.");
				for(Transaction txn : txns) {
					log.info(txn.getName()+" : "+ txn.getDate()+" : "+txn.getAmount());
				}
			}
		}
		
		prepareResponse(response);
	}
	
	private class GetTransactionServletParams extends RequestParams{
		public Map<String, TransactionRequestParams> acctMap;
		
		public String fetchType;
	}

}
