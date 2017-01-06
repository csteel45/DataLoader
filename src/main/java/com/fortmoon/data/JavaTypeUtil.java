/*
 * @(#)SQLUtil.java $Date: Dec 16, 2011 6:28:55 PM $
 * 
 * Copyright � 2011 FortMoon Consulting, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of FortMoon
 * Consulting, Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with FortMoon Consulting.
 * 
 * FORTMOON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. FORTMOON SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.
 * 
 */
package com.fortmoon.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

/**
 * @author Christopher Steel - FortMoon Consulting, Inc.
 *
 * @since Dec 16, 2011 6:28:55 PM
 */
public class JavaTypeUtil {
	private static Logger log = Logger.getLogger(JavaTypeUtil.class);
	private static final String called = "called.";

	@SuppressWarnings("deprecation")
	private static boolean isDate(String val) {
		log.trace(called);
		try {
			String str = val.replace('-', '/');
			Date.parse(str);
			return true;
		}
		catch(IllegalArgumentException ie) {
		}
		if (val.contains("/")) {
			String str = val.replace('/', '0');
			if (isInteger(str))
				return true;
		}
		return false;
	}
	
	private static boolean isBool(String val) {
		log.trace(called);

		if(val != null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")))
			return true;
		try {
			if (val.length() == 1 && (Integer.valueOf(val).equals(1) || Integer.valueOf(val).equals(0)))
				return true;
		}
		catch (NumberFormatException nfe) {
		}
			
		return false;
	}
	
	private static boolean isChar(String val) {
		log.trace(called);

		if(val != null && val.length() == 1)
			return true;
		return false;
	}
	
	private static boolean isTime(String val) {
		log.trace(called);
		String str = val;
		if(val.endsWith("AM") || val.endsWith("am") || val.endsWith("PM") || val.endsWith("pm")) {
			str = val.substring(0, val.length() - 2);
			//System.out.println("STR = " + str);
		}
		try {
		if(null != java.sql.Time.valueOf(str))
			return true;
		}
		catch(IllegalArgumentException iae) {
			
		}
		return false;
	}

	private static boolean isDateTime(String val) {
		log.trace(called);

		if(val != null) {
			String str[] = val.split(" ");
			if(str.length == 2 && isDate(str[0]) && isTime(str[1]))
				return true;

			String str2[] = val.split(" ");
			if(str2.length > 1) {
				//System.out.println("SPLIT = " + str2[1]);
				if(isDate(str2[0]) && isTime(str2[1]))
					return true;
			}
		}
		return false;
	}
	
	private static boolean isNull(String val) {
		log.trace(called);

		if(val == null || val.isEmpty() || val.equalsIgnoreCase("null"))
			return true;
		return false;
	}
	
	private static boolean isShort(String string) {
		log.trace(called);

		// Discard strings starting with 0 as not numbers
		if (string != null && !(string.length() > 1 && string.startsWith("0"))) {
			try {
				Short.parseShort(string);
				return true;
			} catch (IllegalArgumentException iae) {
			}
		}
		return false;
	}

	/**
	 * @param string
	 * @return
	 */
	private static boolean isInteger(String string) {
		log.trace(called);

		// Discard strings starting with 0 as not numbers
		if (string != null && !(string.length() > 1 && string.startsWith("0"))) {
			try {
				Integer.parseInt(string);
				return true;
			} catch (IllegalArgumentException iae) {

			}
		}
		return false;
	}
	
	private static boolean isLong(String string) {
		log.trace(called);

		// Discard strings starting with 0 as not numbers
		if (string != null && !(string.length() > 1 && string.startsWith("0"))) {
			try {
				Long.parseLong(string);
				return true;
			} catch (IllegalArgumentException iae) {

			}
		}
		return false;
	}

	private static boolean isFloat(String string) {
		log.trace(called);

		if (string != null && string.contains(".")) {
			try {
				Float value = Float.parseFloat(string);
				if(Float.NEGATIVE_INFINITY < value && value < Float.MAX_VALUE) {
					return true;
				}
			} catch (IllegalArgumentException iae) {

			}
		}
		return false;
	}

	private static boolean isDouble(String string) {
		log.trace(called);

		if (string != null  && string.contains(".")) {
			try {
				Double value = Double.parseDouble(string);
				if(Double.NEGATIVE_INFINITY < value && value < Double.MAX_VALUE) {
					return true;
				}
			} catch (IllegalArgumentException iae) {

			}
		}
		return false;
	}
	/**
	 * @param string
	 * @return
	 */
	private static boolean isBigInt(String string) {
		log.trace(called);

		if (string == null || (string.length() > 1 && string.startsWith("0")))
			return false;
		
		try {
				BigInteger bi = new BigInteger(string);
			} 
		catch (IllegalArgumentException iae) {
				return false;
		}
		
		return true;
	}

	/**
	 * @param string
	 * @return
	 */
	private static boolean isBigDecimal(String string) {
		log.trace(called);

		if (string == null || (string.length() > 1 && string.startsWith("0") && !string.startsWith("0.")))
			return false;

		try {
			BigDecimal bd = new BigDecimal(string);
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * @param string
	 * @return
	 */
	public static JavaType getType(String string) {
		log.trace(called);

		if(isNull(string))
			return JavaType.NULL;
		if(isDateTime(string))
			return JavaType.DATETIME;
		if(isDate(string))
			return JavaType.DATE;
		if(isTime(string))
			return JavaType.TIME;
		if(isBool(string))
			return JavaType.BOOL;
		if(isShort(string))
			return JavaType.SHORT;
		if(isInteger(string))
			return JavaType.INTEGER;
		if(isLong(string))
			return JavaType.LONG;
		if(isBigInt(string))
			return JavaType.BIGINT;
		if(isFloat(string))
			return JavaType.FLOAT;
		if(isDouble(string))
			return JavaType.DOUBLE;		
		if(isBigDecimal(string))
			return JavaType.BIGDECIMAL;
		if(isChar(string))
			return JavaType.CHAR;
		
		return JavaType.STRING;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String blob = new String(new char[65534]);
		String longBlob = new String(new char[65536]);
		
		System.out.println("Type of <null>					" + getType(null));
		System.out.println("Type of ''						" + getType(""));
		System.out.println("Type of 2011-08-23				" + getType("2011-08-23"));
		System.out.println("Type of 12:02:23				" + getType("12:02:23"));
		System.out.println("Type of 2011-08-23 12:02:23		" + getType("2011-08-23 12:02:23"));
		System.out.println("Type of 12/31/1999				" + getType("12/31/1999"));
		System.out.println("Type of 12/31/1999 7:44:48 PM	" + getType("12/31/1999 7:41:48 PM"));
		System.out.println("Type of 16						" + getType("16"));
		System.out.println("Type of 32768					" + getType("32768"));
		System.out.println("Type of 22000000000				" + getType("22000000000"));
		System.out.println("Type of 9223372036854775808		" + getType("9223372036854775808"));
		System.out.println("Type of 12.23					" + getType("12.23"));
		System.out.println("Type of 4.4028235E39			" + getType("4.4028235E39"));
		System.out.println("Type of 1.7976931348E309		" + getType("1.7976931348E309"));
		System.out.println("Type of x						" + getType("x"));
		System.out.println("Type of 016						" + getType("016"));
		System.out.println("Type of 0.16					" + getType("0.16"));
		System.out.println("Type of 00.16					" + getType("00.16"));
		System.out.println("Type of 0.1.6					" + getType("0.1.6"));
		System.out.println("Type of true					" + getType("true"));
		System.out.println("Type of FALSE					" + getType("FALSE"));
		System.out.println("Type of 0						" + getType("0"));
		System.out.println("Type of 1						" + getType("1"));
		System.out.println("Type of 2						" + getType("2"));
		System.out.println("Type of x12.23					" + getType("x12.23"));
		System.out.println("Type of 12.x					" + getType("12.x"));
		System.out.println("Type of abc						" + getType("abc"));
		System.out.println("Short max						" + Short.MAX_VALUE);
		System.out.println("Int max							" + Integer.MAX_VALUE);
		System.out.println("Long max						" + Long.MAX_VALUE);
		System.out.println("Float max						" + Float.MAX_VALUE);
		System.out.println("Double max						" + Double.MAX_VALUE);
		System.out.println("TYPE.FLOAT compareTo DOUBLE		" + (SQLTYPE.FLOAT.compareTo(SQLTYPE.DOUBLE)));
		System.out.println("TYPE.DOUBLE compareTo FLOAT		" + (SQLTYPE.DOUBLE.compareTo(SQLTYPE.FLOAT)));
		
		String str;
		try {
			str = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("MM/dd/yyyy").parse("12/31/1999"));
			System.out.println("Str = " + str);
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
