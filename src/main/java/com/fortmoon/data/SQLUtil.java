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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

/**
 * @author Christopher Steel - FortMoon Consulting, Inc.
 *
 * @since Dec 16, 2011 6:28:55 PM
 */
public class SQLUtil {
	private static Logger log = Logger.getLogger(SQLUtil.class);
	private static final String called = "called.";
	/**
	 * @param val
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static boolean isDate(String val) {
		log.trace(called);
		try {
			Date.parse(val);
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
			String str[] = val.split("\\s");
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

		if (string != null && !(string.length() > 1 && string.startsWith("0"))) {
			try {
				Long value = Long.parseLong(string);
				if(Long.MIN_VALUE < value && value < Long.MAX_VALUE) {
					return true;
				}
			} catch (IllegalArgumentException iae) {

			}
		}
		return false;
	}
	
	private static boolean isVarChar(String string) {
		log.trace(called);

		if (string != null && string.length() < 255) {
			return true;
		}
		return false;
	}

	/**
	 * @param string
	 * @return
	 */
	private static boolean isBlob(String string) {
		log.trace(called);

		if(null != string && string.length() < 65535)
			return true;
		return false;
	}
	
	/**
	 * @param string
	 * @return
	 */
	public static SQLTYPE getType(String string) {
		log.trace(called);

		if(isNull(string))
			return SQLTYPE.NULL;
		if(isDateTime(string))
			return SQLTYPE.DATETIME;
		if(isDate(string))
			return SQLTYPE.DATE;
		if(isTime(string))
			return SQLTYPE.TIME;
		if(isBool(string))
			return SQLTYPE.VARCHAR;  //No BOOL for Oracle
		if(isInteger(string))
			return SQLTYPE.INTEGER;
		if(isBigInt(string))
			return SQLTYPE.BIGINT;
		if(isBigInt(string))
			return SQLTYPE.BIGINT;
		if(isFloat(string))
			return SQLTYPE.FLOAT;
		if(isDouble(string))
			return SQLTYPE.DOUBLE;		
		if(isChar(string))
			return SQLTYPE.VARCHAR;  //Should be CHAR but no for Oracle
		if(isVarChar(string))
			return SQLTYPE.VARCHAR;
		if(isBlob(string))
			return SQLTYPE.BLOB;
		return SQLTYPE.LONGBLOB;
	}
	
	public static void setValue(PreparedStatement stmt, int colIndex, String string) throws Exception {
		int index = colIndex + 1;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			if (isNull(string)) {
				stmt.setString(index, null);
				return;
			}
			if (isDateTime(string)) {
				//log.info("DATETIME:	" + string);
				try {
					stmt.setDate(index, (java.sql.Date) format.parse(string));
				}
				catch (ParseException pe) {
					try {
						java.util.Date date = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").parse(string);
						String str = new SimpleDateFormat("yyyy-MM-dd").format(date);
						//log.info("New DATE str:	" +  str);
						Date jdate = Date.valueOf(str);

						//log.info("New DATE date:	" +  jdate.toString());
						stmt.setDate(index, jdate);
					}
					catch (ParseException pex) {
						log.info("Exception parsing DATE: " + pex);
						stmt.setString(index, string);
					}
				}
				return;
			}
			if (isDate(string)) {
				log.debug("DATE:	" + string);
				//String finalexampledt = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd-MM-yyyy").parse(s));
				try {
					java.util.Date date = new SimpleDateFormat("MM/dd/yyyy").parse(string);
					String str = new SimpleDateFormat("yyyy-MM-dd").format(date);
					//log.info("New DATE str:	" +  str);
					Date jdate = Date.valueOf(str);
					//log.info("New DATE date:	" +  jdate.toString());
					stmt.setDate(index, jdate);
				}
				catch (ParseException pe) {
					log.info("Exception parsing DATE: " + pe);
					stmt.setString(index, string);
				}
				return;
			}
			if (isTime(string)) {
				log.info("TIME:	" + string);
				try {
					stmt.setDate(index, (java.sql.Date) format.parse(string));
				}
				catch (ParseException pe) {
					stmt.setString(index, string);
				}
				return;
			}
			if (isInteger(string)) {
				stmt.setInt(index, Integer.parseInt(string));
				return;
			}
			if (isFloat(string)) {
				stmt.setFloat(index, Float.parseFloat(string));
				return;
			}
			if (isDouble(string)) {
				stmt.setDouble(index, Double.parseDouble(string));
				return;
			}
			stmt.setString(index, string);
		}
		catch (Exception e) {
			log.error("Exception at index: " + index + " in string: " + string);
			throw e;
		}

	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String blob = new String(new char[65534]);
		String longBlob = new String(new char[65536]);
		
		System.out.println("Type of <null>			" + getType(null));
		System.out.println("Type of ''			" + getType(""));
		System.out.println("Type of 2011-08-23		" + getType("2011-08-23"));
		System.out.println("Type of 12:02:23		" + getType("12:02:23"));
		System.out.println("Type of 2011-08-23 12:02:23	" + getType("2011-08-23 12:02:23"));
		System.out.println("Type of 12/31/1999		" + getType("12/31/1999"));
		System.out.println("Type of 12/31/1999 7:44:48 PM	" + getType("12/31/1999 7:41:48 PM"));
		System.out.println("Type of 12			" + getType("12"));
		System.out.println("Type of 12.23			" + getType("12.23"));
		System.out.println("Type of 0			" + getType("0"));
		System.out.println("Type of 016			" + getType("016"));
		System.out.println("Type of 16			" + getType("16"));
		System.out.println("Type of 22000000000		" + getType("22000000000"));
		System.out.println("Type of 22000000000.1		" + getType("22000000000.1"));
		System.out.println("Type of 9223372036854775808	" + getType("9223372036854775808"));
		System.out.println("Type of 9223372036854775808.1	" + getType("9223372036854775808.1"));
		System.out.println("Type of 4.4028235E38		" + getType("4.4028235E38"));
		System.out.println("Type of x			" + getType("x"));
		System.out.println("Type of true			" + getType("true"));
		System.out.println("Type of FALSE			" + getType("FALSE"));
		System.out.println("Type of x12.23			" + getType("x12.23"));
		System.out.println("Type of 12.x			" + getType("12.x"));
		System.out.println("Type of abc			" + getType("abc"));
		System.out.println("Type of [String > 255]		" + getType(blob));
		System.out.println("Type of [String > 65535]	" + getType(longBlob));
		System.out.println("Float max			" + Float.MAX_VALUE);
		System.out.println("Int max			" + Integer.MAX_VALUE);
		System.out.println("Long max			" + Long.MAX_VALUE);
		System.out.println("Double max			" + Double.MAX_VALUE);
		System.out.println("TYPE.FLOAT compareTo TYPE.DOUBLE			" + (SQLTYPE.FLOAT.compareTo(SQLTYPE.DOUBLE)));
		System.out.println("TYPE.DOUBLE compareTo TYPE.FLOAT			" + (SQLTYPE.DOUBLE.compareTo(SQLTYPE.FLOAT)));
		
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
