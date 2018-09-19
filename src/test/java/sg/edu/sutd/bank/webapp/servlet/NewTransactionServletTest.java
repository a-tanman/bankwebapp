package sg.edu.sutd.bank.webapp.servlet;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class NewTransactionServletTest {

	NewTransactionServlet nTs = new NewTransactionServlet();
	
	@Test
	public void testCheckBalance() {
		assertTrue(nTs.checkBalance((new BigDecimal("10")), (new BigDecimal("100"))));
	}
	
	@Test
	public void testCheckPositive() {
		assertTrue(nTs.checkPositive(new BigDecimal("1")));
	}

}
