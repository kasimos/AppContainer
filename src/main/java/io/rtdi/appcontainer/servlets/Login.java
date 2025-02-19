package io.rtdi.appcontainer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/login")
public class Login extends HttpServlet {

	private static final long serialVersionUID = 454601115219128919L;

	public Login() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		HttpSession session = req.getSession(false);
		String username = null;
		String password = null;
		if (session != null) {
			username = (String) session.getAttribute("username");
			password = (String) session.getAttribute("password");
			session.removeAttribute("username");
			session.removeAttribute("password");
			if (username != null && password != null) {
				String target = "j_security_check?j_username=" + URLEncoder.encode(username, "UTF-8") + "&j_password=" + URLEncoder.encode(password, "UTF-8");
				resp.sendRedirect(target);
				out.println("<!DOCTYPE html>");
				out.println("<html><head></head><body>");
				out.println("Redirecting to the <a href=\"" + target + "\">tomcat authenticator</a> (j_security_check)</body></html>");
				return;
			}
		}
		if (username == null) username = "";
		if (password == null) password = "";
		String ui5url = "/openui5/resources/sap-ui-core.js";
		
		out.println("<!DOCTYPE html>");
		out.println("<html style=\"height: 100%;\">");
		out.println("<head>");
		out.println("<meta charset=\"ISO-8859-1\">");
		out.println("<title>Login</title>");
		out.println("<script src=\"" + ui5url + "\"");
		out.println("	id=\"sap-ui-bootstrap\""); 
		out.println("	data-sap-ui-theme=\"sap_fiori_3\"");
		out.println("	data-sap-ui-libs=\"sap.m\">");
		out.println("</script>");

		out.println("<script id=\"view1\" type=\"sapui5/xmlview\">");
		out.println("    <mvc:View");
		out.println("        controllerName=\"local.controller\"");
		out.println("        xmlns:mvc=\"sap.ui.core.mvc\"");
		out.println("        xmlns=\"sap.m\">");
		out.println("        <App>");
		out.println("            <Page title=\"Login Form\">");
		out.println("				<content>");
		out.println("					<VBox fitContainer=\"true\" justifyContent=\"Center\" alignItems=\"Center\" alignContent=\"Center\">");
		out.println("						<items>");
		out.println("							<Input id=\"uid\" name=\"j_username\" value=\"" + username + "\" placeholder=\"User ID\"></Input>");
		out.println("							<Input id=\"pasw\" name=\"j_password\" value=\"" + password + "\" placeholder=\"Password\" type=\"Password\"></Input>");
		out.println("							<Button width=\"12rem\" text=\"Login\" type=\"Emphasized\" press=\"onLoginTap\"></Button>");
		out.println("						</items>");
		out.println("					</VBox>");
		out.println("				 </content>");
		out.println("			 </Page>");
		out.println("        </App>");
		out.println("    </mvc:View> ");
		out.println("</script>");
		
		out.println("  <script>");
		out.println("sap.ui.controller(\"local.controller\", {");
		out.println("  onLoginTap:function() {");
		out.println("    document.forms[0].submit();");
		out.println("  }");
		out.println("});");

		out.println("var oView = sap.ui.xmlview({");
		out.println("  viewContent: jQuery('#view1').html()");
		out.println("});");
		out.println("oView.placeAt('content');");
		out.println("</script>");

		out.println("</head>");
		out.println("<body class='sapUiBody' style=\"height: 100%;\" >");
		out.println("<form action=\"j_security_check\" method=\"post\">");
		out.println("    <div id=\"content\">");
		out.println("    </div>");
		out.println("</form>");
		out.println("</body>");
		out.println("</html>");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
