/*
 * SUTD (Singapore)
 * 
 */

package sg.edu.sutd.bank.webapp.service;

import sg.edu.sutd.bank.webapp.commons.ServiceException;
import sg.edu.sutd.bank.webapp.model.ClientAccount;


public interface ClientAccountDAO {

	int create(ClientAccount account) throws ServiceException;

}
