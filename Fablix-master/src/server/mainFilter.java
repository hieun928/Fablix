package server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class mainFilter implements Filter  {

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) arg0;
		HttpSession session = req.getSession();
		Boolean shouldLogin = (Boolean) session.getAttribute("shouldLogin");
		String url = req.getRequestURI();
		url = url.substring(req.getContextPath().length(), url.length());
		System.out.println(url);
		if ((shouldLogin == null || shouldLogin == false) && !(url.equals("/_dashboard") || 
				url.equals("/_dashboardLogin") || url.equals("/css/navBar.css") || 
				url.equals("/index.js") || url.equals("/autoComplete") || url.equals("/mobileLogin") || url.equals("/mobileSearch")))
			arg0.getRequestDispatcher("/login").forward(arg0, arg1);
		else 
		{
			String qs = req.getQueryString();
			if (qs != null && qs != "")
				url += "?" + qs;
			arg0.getRequestDispatcher(url).forward(arg0, arg1);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {}

}
