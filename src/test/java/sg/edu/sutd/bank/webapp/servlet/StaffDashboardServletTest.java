package sg.edu.sutd.bank.webapp.servlet;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class StaffDashboardServletTest {

	StaffDashboardServlet sDs = new StaffDashboardServlet();
	
	@Test
	public void testToBigD() {
		String[] sa= new String[1];
		sa[0] = "100";
		BigDecimal[] res = sDs.toBigDecimalArray(sa);
		System.out.println(sa[0]);
		System.out.println(res[0]);
		assertTrue(res[0].compareTo(new BigDecimal("100")) == 0);
	}

}
