package datamodel;


public class NotificationParams{
	
	public String item_id;
	
	public String new_transactions;
	
	public String webhook_code;
	
	public String new_webhook;
	
	public String accountId;
	
	public String date;
	
	public String offset;
	
	public WebhookError error;
	
	public NotificationParams item_id(String str) {
		this.item_id = str;
		return this;
	}
	
	public NotificationParams new_transactions(String str) {
		this.new_transactions = str;
		return this;
	}
	
	public NotificationParams webhook_code(String str) {
		this.webhook_code = str;
		return this;
	}
	
	public NotificationParams new_webhook(String str) {
		this.new_webhook = str;
		return this;
	}
	
	public NotificationParams accountId(String str) {
		this.accountId = str;
		return this;
	}
	
	public NotificationParams date(String str) {
		this.date = str;
		return this;
	}
	
	public NotificationParams offset(String str) {
		this.offset = str;
		return this;
	}
	
}

 class WebhookError{
	public String display_message;
	
	public String error_code;
}
