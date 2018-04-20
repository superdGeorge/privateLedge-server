package com.projectx;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(
		name = "testServlet",
		urlPatterns = {"/test/msg"}
		)
public class testServlet extends BaseServlet{
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)  
			throws IOException {
		
	}
	
}
