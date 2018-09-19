package sg.edu.sutd.bank.webapp.servlet;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TransactionCodeGeneratorTest {

	@Test
	public void testGenerateCodes() {
		//check that 10 codes are generated and that they are unique
		List<String> l1 = TransactionCodeGenerator.generateCodes(10);

		assertTrue(l1.size() == 10);
	}
	
	@Test
	public void testUniqueCodes() {
		List<String> l2 = TransactionCodeGenerator.generateCodes(10);
		assertTrue(distinctValues(l2));
	}
	
	public static boolean distinctValues(List<String> arr){
	    Set<String> foundStrs = new HashSet<String>();
	    for (String str : arr) {
	        if(foundStrs.contains(str)){
	            return false;
	        }
	        foundStrs.add(str);
	    }              
	    return true;          
	}

}
