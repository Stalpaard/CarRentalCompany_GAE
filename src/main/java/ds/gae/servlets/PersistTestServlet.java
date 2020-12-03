package ds.gae.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ds.gae.view.JSPSite;

@SuppressWarnings("serial")
public class PersistTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getSession().getAttribute("renter") == null) {
            String email = "test.user@kuleuven.be";
            req.getSession().setAttribute("renter", email);
        }
        resp.sendRedirect(JSPSite.PERSIST_TEST.url());
    }
}
