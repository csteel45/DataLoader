/*
 * @(#)SQLTYPE.java $Date: Dec 16, 2011 10:50:30 PM $
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

public enum JavaType {
	SHORT("SHORT"), INTEGER("INTEGER"), LONG("LONG"), BIGINT("BIGINT"), FLOAT("FLOAT"), DOUBLE("DOUBLE"), BIGDECIMAL("BIGDECIMAL"), BOOL("BOOL"), CHAR("CHAR"), TIME("TIME"), DATE("DATE"), DATETIME("DATETIME"), STRING("STRING"), NULL("NULL");
	private String str;
	
	private JavaType(String str) {
		this.str = str;
	}
	
	public int getValue() {
		switch (this) {
			case DATETIME:
				return 44;
			case DATE:
				return 40;
			case TIME:
				return 36;
			case SHORT:
				return 34;
			case INTEGER:
				return 32;
			case LONG:
				return 32;
			case BIGINT:
				return 28;
			case FLOAT:
				return 24;
			case DOUBLE:
				return 20;
			case BIGDECIMAL:
				return 18;
			case BOOL:
				return 16;
			case CHAR:
				return 12;
			case STRING:
				return 8;
			case NULL:
				return 100;
		}
		return 100;
	}

	public String toString() {
		return this.str;
	}

}