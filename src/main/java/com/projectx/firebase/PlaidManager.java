package com.projectx.firebase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.plaid.client.PlaidClient;
import com.plaid.client.PlaidClient.Builder;
import com.plaid.client.request.AccountsGetRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.Account;
import com.plaid.client.response.AccountsGetResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.TransactionsGetResponse;
import com.projectx.TransactionRequestParams;
import com.projectx.UserInfoServlet;

import retrofit2.Response;
import util.Pair;
import util.StringUtil;


public class PlaidManager {
	
	private final Logger log = Logger.getLogger(UserInfoServlet.class.getName());

	private final PlaidClient m_client;
	
	private final static String ISO8601DateFormat = "YYYY-MM-DD";
	
	public PlaidManager(PlaidClient client) {
		m_client = client;
	}
	
	public Pair<String, String> getAccessToken(String publicToken) {
		try {
			Response<ItemPublicTokenExchangeResponse> response = m_client.service()
				    .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
				    .execute();
			if(response.isSuccessful()) {
				return new Pair<>(response.body().getAccessToken(), response.body().getItemId());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Account> getAccountInfo(String accessToken, List<String> acctIds) {
		
		if(!acctIds.isEmpty() && !StringUtil.isEmpty(accessToken)) {
			try {
				log.info("Fetching account info of "+acctIds.size()+" for access token: "+ accessToken);
				AccountsGetRequest request = new AccountsGetRequest(accessToken).withAccountIds(acctIds);
				Response<AccountsGetResponse> response = m_client.service()
						.accountsGet(request)
						.execute();
				if(response.isSuccessful()) {
					log.info("Received response from Plaid.");
					return response.body().getAccounts();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.info("Failed to receive response from Plaid.");
		return Collections.emptyList();
	}
	
	public List<Account> getAllAccount(String accessToken) {
		log.info("Getting all the account info from Plaid");
		if(StringUtil.isEmpty(accessToken)) {
			try {
				Response<AccountsGetResponse> response = m_client.service()
						.accountsGet(new AccountsGetRequest(accessToken))
						.execute();
				if(response.isSuccessful()) {
					return response.body().getAccounts();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Collections.emptyList();
	}
	
	public TransactionsGetResponse getTransaction(String accessToken, String acctId, TransactionRequestParams params){
		if(params != null && !StringUtil.isEmpty(accessToken)) {
			log.info("Getting transactions from Plaid for account: "+acctId+" from date " + params.date +
					" to now with count: "+params.count + " and offset: "+params.offset);

			try {
				Date startDate = new SimpleDateFormat(ISO8601DateFormat).parse(params.date);  
				
				Response<TransactionsGetResponse> response = m_client.service()
															.transactionsGet(new TransactionsGetRequest(accessToken, startDate, new Date())
														    .withAccountIds(Arrays.asList(acctId))
														    .withCount(params.count == -1? 500 : params.count)
														    .withOffset(params.offset))
															.execute();
				if(response.isSuccessful()) {
					return response.body();
				}
			}catch(Exception e) {
				log.warning("Failed to get transactions for acct: "+acctId +" from date: " +params.date);
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	public enum PlaidManagerFactory{
		instance;
		
		private static final String CLIENT_ID = "5a24e46b4e95b836d37e3814";
		private static final String SECRET = "2849772fe7a6d89d983e9aba174764";
		private static final String PUBLIC_KEY = "0c7d6a80f78d83bd38e09d0f8d5480";
		
		
		public PlaidManager create() {
			Builder bldr = PlaidClient.newBuilder();
			PlaidClient client = bldr.clientIdAndSecret(CLIENT_ID, SECRET)
								  .publicKey(PUBLIC_KEY) 
								  .developmentBaseUrl()
								  .build();
			return new PlaidManager(client);
		}
	}
}
