/*
 * SUTD (Singapore)
 * 
 */

package sg.edu.sutd.bank.webapp.servlet;
import static sg.edu.sutd.bank.webapp.servlet.ServletPaths.STAFF_DASHBOARD_PAGE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sg.edu.sutd.bank.webapp.commons.Constants;
import sg.edu.sutd.bank.webapp.commons.ServiceException;
import sg.edu.sutd.bank.webapp.commons.StringUtils;
import sg.edu.sutd.bank.webapp.model.ClientAccount;
import sg.edu.sutd.bank.webapp.model.ClientInfo;
import sg.edu.sutd.bank.webapp.model.User;
import sg.edu.sutd.bank.webapp.model.UserStatus;
import sg.edu.sutd.bank.webapp.service.ClientAccountDAO;
import sg.edu.sutd.bank.webapp.service.ClientAccountDAOImpl;
import sg.edu.sutd.bank.webapp.service.ClientInfoDAO;
import sg.edu.sutd.bank.webapp.service.ClientInfoDAOImpl;
import sg.edu.sutd.bank.webapp.service.EmailService;
import sg.edu.sutd.bank.webapp.service.EmailServiceImp;
import sg.edu.sutd.bank.webapp.service.TransactionCodesDAO;
import sg.edu.sutd.bank.webapp.service.TransactionCodesDAOImp;
import sg.edu.sutd.bank.webapp.service.UserDAO;
import sg.edu.sutd.bank.webapp.service.UserDAOImpl;

@WebServlet(STAFF_DASHBOARD_PAGE)
public class StaffDashboardServlet extends DefaultServlet {
	public static final String REGISTRATION_DECISION_ACTION = "registrationDecisionAction";
	public static final String TRANSACTION_DECSION_ACTION = "transactionDecisionAction";
	
	private static final long serialVersionUID = 1L;
	private ClientInfoDAO clientInfoDAO = new ClientInfoDAOImpl();
	private UserDAO userDAO = new UserDAOImpl();
	private ClientAccountDAO clientAccountDAO = new ClientAccountDAOImpl();
	private EmailService emailService = new EmailServiceImp();
	private TransactionCodesDAO transactionCodesDAO = new TransactionCodesDAOImp();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			List<ClientInfo> accountList = clientInfoDAO.loadWaitingList();
			req.getSession().setAttribute("registrationList", accountList);
		} catch (ServiceException e) {
			sendError(req, e.getMessage());
		}
		forward(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String actionType = req.getParameter("actionType");
		if (REGISTRATION_DECISION_ACTION.endsWith(actionType)) {
			try {
				onRegistrationDecisionAction(req, resp);
			} catch (ServiceException e) {
				sendError(req, e.getMessage());
				redirect(resp, STAFF_DASHBOARD_PAGE);
			}
		} else if (TRANSACTION_DECSION_ACTION.equals(actionType)) {
			onTransactionDecisionAction(req, resp);
		}
	}

	private void onRegistrationDecisionAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException, ServiceException {
		String[] decisions = req.getParameterValues("decision");
		int[] userIds = toIntegerArray(req.getParameterValues("user_id"));
		String[] userEmails = req.getParameterValues("user_email");
		List<User> users = new ArrayList<User>();
		for (int i = 0; i < userIds.length; i++) {
			int userId = userIds[i];
			Decision decision = Decision.valueOf(decisions[i]);
			if (decision.getStatus() != null) {
				User user = new User();
				user.setId(userId);
				user.setStatus(decision.getStatus());
				users.add(user);
			}
		}
		if (!users.isEmpty()) {
			try {
				userDAO.updateDecision(users);
			} catch (ServiceException e) {
				sendError(req, e.getMessage());
			}
			activateAccount(userEmails, userIds, decisions);
		}
		redirect(resp, STAFF_DASHBOARD_PAGE);
	}
	
	private int[] toIntegerArray(String[] idStrs) {
		int[] result = new int[idStrs.length];
		for (int i = 0; i < idStrs.length; i++) {
			result[i] = Integer.valueOf(idStrs[i]);
		}
		return result;
	}

	private void activateAccount(String[] userEmails, int[] userIds, String[] decisions) throws ServiceException {
		for (int i = 0; i < userIds.length; i++) {
			if (Decision.valueOf(decisions[i]) == Decision.approve) {
				int userId = userIds[i];
				/* init account */
				ClientAccount clientAccount = new ClientAccount();
				clientAccount.setUser(new User(userId));
				clientAccount.setAmount(Constants.INIT_AMOUNT);
				clientAccountDAO.create(clientAccount);
				/* generate and send transaction codes */
				List<String> codes = TransactionCodeGenerator.generateCodes(100);
				transactionCodesDAO.create(codes, userId);
				emailService.sendMail(userEmails[i], "Your account has been approved ",
						"Congratulation, your account has been approved! These are your transaction codes: \n"
								+ StringUtils.join(codes, "\n"));
			}
		}
	}

	private void onTransactionDecisionAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		forward(req, resp);
	}
	
	private static enum Decision {
		waiting(null), 
		approve(UserStatus.APPROVED), 
		decline(UserStatus.DECLINED);
		
		private UserStatus status;
		private Decision(UserStatus status) {
			this.status = status;
		}

		public UserStatus getStatus() {
			return status;
		}
	}
}
