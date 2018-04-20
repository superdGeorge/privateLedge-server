package com.projectx.firebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import datamodel.UserInfo;
import util.StringUtil;

public class FirestoreManager {
	private static final Logger log = Logger.getLogger(FirestoreManager.class.getName());
	
	private final Firestore m_store;
	
	private static final String USER_COLLECTION = "user";
	
	private static final String TOKEN_COLLECTION = "plaidToken";
	
	private static final String ITEM_ID_COLLECTION = "itemId";
	
	public FirestoreManager() {
		m_store = FirebaseServiceProvider.getFirestore();
	}
	
	public UserInfo getUserInfo(String uid) {
		ApiFuture<DocumentSnapshot> future = m_store.collection(USER_COLLECTION)
													.document(uid)
													.get();
		
		try {
			DocumentSnapshot snapshot = future.get();
			if(snapshot.exists()) {
				 return snapshot.toObject(UserInfo.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public void addUserInfo(UserInfo userInfo) {
		ApiFuture<WriteResult> future = m_store.collection(USER_COLLECTION)
												.document(userInfo.getUid())
												.set(userInfo);
		try {
			log.info("Successfully added user info for uid: " + userInfo.getUid() + " at time: " + future.get().getUpdateTime());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void addAccessToken(String acctId, String itemId, String accessToken) {
		log.info("Adding new access token: "+accessToken+" and item id: "+itemId+" for acct id: " + acctId);
		Map<String, String> params = new HashMap<>();
		params.put("accessToken", accessToken);
		params.put("itemId", itemId);
		
		ApiFuture<WriteResult> future = m_store.collection(TOKEN_COLLECTION)
												.document(acctId)
												.set(params);
		try {
			log.info("Successfully added access token for acctId: " + acctId + " at time: " + future.get().getUpdateTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getAccessToken(String acctId) {
		ApiFuture<DocumentSnapshot> future = m_store.collection(TOKEN_COLLECTION)
													.document(acctId)
													.get();

		try {
			DocumentSnapshot snapshot = future.get();
			if(snapshot.exists()) {
				return snapshot.getString("accessToken");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public void addAccountId(String userId, String acctId) {
		UserInfo userinfo = getUserInfo(userId);
		
		List<String> acctIds = userinfo.getAccountIds();
		
		ApiFuture<WriteResult> future = null;
		if(acctIds == null || acctIds.isEmpty()) {
			log.info("Adding new account id.");
			acctIds = new ArrayList<>();
		}
		
		if(!acctIds.contains(acctId)) {
			log.info("Updating userinfo with accountId: "+acctId);
			acctIds.add(acctId);
			Map<String, Object> map = new HashMap<>();
			map.put("accountIds", acctIds);
			future = m_store.collection(USER_COLLECTION)
					  .document(userId)
					  .update(map);
		}
		
		if(future!= null) {
			try {
				log.info("Successfully added account id for userid: " + userId + " at time: " + future.get().getUpdateTime());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			log.info("AccountId: "+acctId+" already exist.");
		}
	}
	
	public String getAccountIdByItemId(String itemId) {
		String acctId = null;
		
		ApiFuture<DocumentSnapshot> future = m_store.collection(ITEM_ID_COLLECTION)
												   .document(itemId)
												   .get();
		try {
			DocumentSnapshot snapshot = future.get();
			if(snapshot.exists()) {
				 acctId = snapshot.getString("accountId");
			}
		} catch (Exception e) {
			log.warning("Failed to get any account related to the notified item: "+ itemId);
			e.printStackTrace();
		} 
		
		return 	acctId;
	}
	
	public List<String> getRegistrationIdByItemId(String itemid) {
		String uid = null;
		if(!StringUtil.isEmpty(itemid) && !StringUtil.isEmpty((uid = getUIDByItemId(itemid)))) {
			UserInfo user = getUserInfo(uid);
			if(user!=null) {
				return user.getFcmTokens();
			}
		}
		
		return new ArrayList<>();
	}
	
	private String getUIDByItemId(String itemId) {
		String uid = null;
		
		ApiFuture<DocumentSnapshot> future = m_store.collection(ITEM_ID_COLLECTION)
												   .document(itemId)
												   .get();
		try {
			DocumentSnapshot snapshot = future.get();
			if(snapshot.exists()) {
				 uid = snapshot.getString("uid");
			}
		} catch (Exception e) {
			log.warning("Failed to get any user related to the notified item: "+ itemId);
			e.printStackTrace();
		} 
		
		return 	uid;
	}
	
	public void updateFCMToken(String curr, String prev) {
		log.info("Updating FCM Token : "+ curr +" from :"+ prev);
		
		List<UserInfo> users = getAllUserInfo();
		for(UserInfo user : users) {
			List<String>	tokens = user.getFcmTokens();
			if(tokens == null) {
				tokens = new ArrayList<>();
				user.setFcmToken(tokens);
			}
			tokens.remove(prev);
			tokens.add(curr);
			
			ApiFuture<WriteResult> future = null;
			Map<String, Object> map = new HashMap<>();
			map.put("fcmTokens", tokens);
			future = m_store.collection(USER_COLLECTION)
					  .document(user.getUid())
					  .update(map);
			
			if(future!= null) {
				try {
					log.info("Successfully updated FCM token for userid: " + user.getUid() + " at time: " + future.get().getUpdateTime());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				log.info("Failed to update FCM token for userid: " + user.getUid());
			}
		}
	}
	
	private List<UserInfo> getAllUserInfo(){
		log.info("Getting all user info from firestore");
		ApiFuture<QuerySnapshot> future = m_store.collection(USER_COLLECTION).get();
		// future.get() blocks on response
		List<UserInfo> users = new ArrayList<>();
		try {
			List<DocumentSnapshot> documents = future.get().getDocuments();
			for (DocumentSnapshot document : documents) {
			  users.add(document.toObject(UserInfo.class));
			}
		} catch (Exception e) {
			log.info("Failed to get users from Firestore.");
			e.printStackTrace();
		}
		
		return users;
	}
	
	public void addItemIdToUID(String uid, String acctId, String itemid) {
		log.info("Mapping new item id: "+itemid+" to uid: " + uid);
		Map<String, String> params = new HashMap<>();
		params.put("uid", uid);
		params.put("accountId", acctId);
		
		ApiFuture<WriteResult> future = m_store.collection(ITEM_ID_COLLECTION)
												.document(itemid)
												.set(params);
		try {
			log.info("Successfully mapped item id to uid: " + uid + " at time: " + future.get().getUpdateTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public enum FirestoreManagerFactory{
		instance;
		
		public FirestoreManager create() {
			return new FirestoreManager();
		}
	}
}

