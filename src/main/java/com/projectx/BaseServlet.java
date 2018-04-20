package com.projectx;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.firebase.auth.FirebaseToken;

import datamodel.RequestParams;
import util.AuthHelper;
import util.JsonHelper;
import util.Pair;

@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {
	
	private static final int BAD_REQUEST = 400;

	private List<String> errorMsg = new ArrayList<>();
	
	private Map<String, Object> map = new HashMap<>();
	
	public List<String> getErrorMessageList(){
		return errorMsg;
	}
	
	public void addErrorMessage(String error) {
		errorMsg.add(error);
	}
	
	public Map<String, Object> getMap(){
		return map;
	}
	
	public void addResponse(String key, Object val) {
		map.put(key, val);
	}
	
	public <T extends RequestParams> Pair<FirebaseToken, T> prepare(HttpServletRequest request, Class<T> clazz) throws IOException{
		T requestParams = JsonHelper.fromJson(request.getReader(), clazz);
		if(requestParams == null) {
			return null;
		}
		FirebaseToken decodedToken = null;
		if(requestParams.needAuth) {
			decodedToken = AuthHelper.verifyToken(requestParams.authToken, errorMsg);
		}
		return new Pair<FirebaseToken, T>(decodedToken, requestParams); 
	}
	
	public void prepareResponse(HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		if(!errorMsg.isEmpty()) {
			response.setStatus(BAD_REQUEST);
			map.put("error", errorMsg);
		}
		out.print(JsonHelper.toJson(map));
		out.flush();
	}
}









