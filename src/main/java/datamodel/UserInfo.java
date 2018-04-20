package datamodel;

import java.util.List;

public class UserInfo {

	private String uid;

	private String email;
	
	private List<String> accountIds;
	
	private List<String> fcmTokens;

	public String getUid() {
		return uid;
	}

	public String getEmail(){
		return email;
	}
	

	public List<String> getAccountIds(){
		return this.accountIds;
	}
	
	public List<String> getFcmTokens(){
		return this.fcmTokens;
	}

	public UserInfo setUid(String uid) {
		this.uid = uid;
		return this;
	}

	public UserInfo setEmail(String email) {
		this.email = email;
		return this;
	}
	
	public UserInfo setFcmToken(List<String> tokens) {
		this.fcmTokens = tokens;
		return this;
	}

	public UserInfo setAccountIds(List<String> acctIds) {
		accountIds = acctIds;
		return this;
	}
}
