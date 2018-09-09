/*
 * Copyright 2017 SUTD Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

https://opensource.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
 */

package sg.edu.sutd.bank.webapp.servlet;

import static sg.edu.sutd.bank.webapp.servlet.ServletPaths.NEW_TRANSACTION;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sg.edu.sutd.bank.webapp.commons.ServiceException;
import sg.edu.sutd.bank.webapp.model.ClientInfo;
import sg.edu.sutd.bank.webapp.model.ClientTransaction;
import sg.edu.sutd.bank.webapp.model.User;
import sg.edu.sutd.bank.webapp.service.ClientInfoDAO;
import sg.edu.sutd.bank.webapp.service.ClientInfoDAOImpl;
import sg.edu.sutd.bank.webapp.service.ClientTransactionDAO;
import sg.edu.sutd.bank.webapp.service.ClientTransactionDAOImpl;
import sg.edu.sutd.bank.webapp.service.UserDAO;
import sg.edu.sutd.bank.webapp.service.UserDAOImpl;

@WebServlet(NEW_TRANSACTION)
public class NewTransactionServlet extends DefaultServlet {
	private static final long serialVersionUID = 1L;
	private ClientTransactionDAO clientTransactionDAO = new ClientTransactionDAOImpl();
	private UserDAO userDAO = new UserDAOImpl();
	ClientInfoDAO clientInfoDAO = new ClientInfoDAOImpl(); // Create DAO to get client info and account balance
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log("Debugging...");
		try {
			ClientTransaction clientTransaction = new ClientTransaction();
			
			//User user = userDAO.loadUser(userName);
			User user = new User(getUserId(req));
			
			ClientInfo ci = clientInfoDAO.loadAccountInfo(req.getUserPrincipal().getName()); 
			log("Req amount: " + new BigDecimal(req.getParameter("amount")));
			log("Acc amount: " + ci.getAccount().getAmount());
			System.out.println("Acc amount: " + ci.getAccount().getAmount());
			if (new BigDecimal(req.getParameter("amount")).compareTo(ci.getAccount().getAmount()) < 1) { // Ensure that user has enough funds in account
				clientTransaction.setUser(user);
				clientTransaction.setAmount(new BigDecimal(req.getParameter("amount")));
				clientTransaction.setTransCode(req.getParameter("transcode"));
				clientTransaction.setToAccountNum(req.getParameter("toAccountNum"));
				clientTransactionDAO.create(clientTransaction);
				redirect(resp, ServletPaths.CLIENT_DASHBOARD_PAGE);
			} else {
				sendError(req, "You have insufficient funds to make this transfer");
				forward(req, resp);
			}
			
		} catch (ServiceException e) {
			log(e.getMessage());
			sendError(req, e.getMessage());
			forward(req, resp);
		}
	}
}
