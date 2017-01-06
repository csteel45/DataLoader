/*
 * @(#)ColumnStats.java $Date: Jan 1, 2017 12:08:37 PM $
 * 
 * Copyright © 2017 FortMoon Consulting, Inc. All Rights Reserved.
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

import java.util.HashMap;

/**
 * @author Christopher Steel - FortMoon Consulting, Inc.
 *
 * @since Jan 1, 2017 12:08:37 PM
 */
public class ColumnStats {
	protected HashMap<JavaType, Long> typeCountMap;
}
