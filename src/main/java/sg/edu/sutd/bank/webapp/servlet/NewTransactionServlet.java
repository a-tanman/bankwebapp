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
import sg.edu.sutd.bank.webapp.model.ClientAccount;
import sg.edu.sutd.bank.webapp.model.ClientInfo;
import sg.edu.sutd.bank.webapp.model.ClientTransaction;
import sg.edu.sutd.bank.webapp.model.User;
import sg.edu.sutd.bank.webapp.service.ClientAccountDAO;
import sg.edu.sutd.bank.webapp.service.ClientAccountDAOImpl;
import sg.edu.sutd.bank.webapp.service.ClientInfoDAO;
import sg.edu.sutd.bank.webapp.service.ClientInfoDAOImpl;
import sg.edu.sutd.bank.webapp.service.ClientTransactionDAO;
import sg.edu.sutd.bank.webapp.service.ClientTransactionDAOImpl;
import sg.edu.sutd.bank.webapp.service.TransactionCodesDAO;
import sg.edu.sutd.bank.webapp.service.TransactionCodesDAOImp;

@WebServlet(NEW_TRANSACTION)
public class NewTransactionServlet extends DefaultServlet {
	private static final long serialVersionUID = 1L;
	private ClientTransactionDAO clientTransactionDAO = new ClientTransactionDAOImpl();
	private ClientInfoDAO clientInfoDAO = new ClientInfoDAOImpl(); // Create DAO to get client info and account balance
	private ClientAccountDAO clientAccountDAO = new ClientAccountDAOImpl();
	private TransactionCodesDAO transactionCodesDAO = new TransactionCodesDAOImp();
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			ClientTransaction clientTransaction = new ClientTransaction();
			
			//User user = userDAO.loadUser(userName);
			int userId = getUserId(req);
			User user = new User(userId);
			
			ClientInfo ci = clientInfoDAO.loadAccountInfo(req.getUserPrincipal().getName()); 
			
			BigDecimal reqAmt = new BigDecimal(req.getParameter("amount"));
			BigDecimal balAmt = ci.getAccount().getAmount();
			String transCode = req.getParameter("transcode");
						
			if (!checkBalance(reqAmt, balAmt)) {
				sendError(req, "You have insufficient funds to make this transfer!");
				forward(req, resp);
			} else if (!checkPositive(reqAmt)) {
				sendError(req, "You entered a non-positive transaction value!");
				forward(req, resp);
			} else if (!transactionCodesDAO.checkTransCode(transCode, userId)){
				sendError(req, "You entered a invalid transaction code!");
				forward(req, resp);
			} else {
				clientTransaction.setUser(user);
				clientTransaction.setAmount(new BigDecimal(req.getParameter("amount")));
				clientTransaction.setTransCode(transCode);
				clientTransaction.setToAccountNum(req.getParameter("toAccountNum"));
				clientTransactionDAO.create(clientTransaction);
				transactionCodesDAO.updateTransCodeStatus(transCode, userId, true);
				
				//updateBalance(user, balAmt, reqAmt); // This deducts the amount regardless of whether transaction is approved
				
				redirect(resp, ServletPaths.CLIENT_DASHBOARD_PAGE);
			}
			
		} catch (ServiceException e) {
			log(e.getMessage());
			sendError(req, e.getMessage());
			forward(req, resp);
		}
	}
	
	private void updateBalance(User user, BigDecimal balAmt, BigDecimal reqAmt) {
		// TODO Auto-generated method stub
		ClientAccount account = new ClientAccount();
		account.setUser(user);
		account.setId(user.getId());
		
		BigDecimal newBal = balAmt.subtract(reqAmt);
		
		account.setAmount(newBal);
		
		try {
			clientAccountDAO.update(account);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	boolean checkBalance(BigDecimal reqAmt, BigDecimal balAmt) {
		return (reqAmt.compareTo(balAmt) < Double.MIN_VALUE);
	}
	
	boolean checkPositive(BigDecimal reqAmt) {
		return (reqAmt.compareTo(BigDecimal.ZERO) > 0);
	}
	
}
