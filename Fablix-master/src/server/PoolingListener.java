package server;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;



public class PoolingListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		  try {
			    // Obtain our environment naming context
			   
			    Context envCtx = (Context) new InitialContext().
			    lookup("java:comp/env");
			   
			    // Look up our data source
			    DataSource  ds = (DataSource) envCtx.lookup
			       ("jdbc/moviedb");
			    sce.getServletContext().setAttribute("DBCPool", ds);
			   } catch(NamingException e){ e.printStackTrace();}
	}

}
