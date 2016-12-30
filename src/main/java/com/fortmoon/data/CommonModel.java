/*
 * @(#)CommonModel.java $Date: Apr 4, 2013 4:30:49 PM $
 * 
 * Copyright � 2013 FortMoon Consulting, Inc. All Rights Reserved.
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

/**
 * @author Christopher Steel - FortMoon Consulting, Inc.
 *
 * @since Apr 4, 2013 4:30:49 PM
 */
public class CommonModel {
	private String id;
	private String jobCode;
	private String status;
	private String costCenter;
	private Date startDate;
	private String recruiterName;
	private String candidateName;
	private Number personNumber;
	private int numOpenings = 1;
	private String band;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the jobCode
	 */
	public String getJobCode() {
		return jobCode;
	}
	/**
	 * @param jobCode the jobCode to set
	 */
	public void setJobCode(String jobCode) {
		this.jobCode = jobCode;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the costCenter
	 */
	public String getCostCenter() {
		return costCenter;
	}
	/**
	 * @param costCenter the costCenter to set
	 */
	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}
	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	/**
	 * @return the recruiterName
	 */
	public String getRecruiterName() {
		return recruiterName;
	}
	/**
	 * @param recruiterName the recruiterName to set
	 */
	public void setRecruiterName(String recruiterName) {
		this.recruiterName = recruiterName;
	}
	/**
	 * @return the candidateName
	 */
	public String getCandidateName() {
		return candidateName;
	}
	/**
	 * @param candidateName the candidateName to set
	 */
	public void setCandidateName(String candidateName) {
		this.candidateName = candidateName;
	}
	/**
	 * @return the personNumber
	 */
	public Number getPersonNumber() {
		return personNumber;
	}
	/**
	 * @param personNumber the personNumber to set
	 */
	public void setPersonNumber(Number personNumber) {
		this.personNumber = personNumber;
	}
	/**
	 * @return the numOpenings
	 */
	public int getNumOpenings() {
		return numOpenings;
	}
	/**
	 * @param numOpenings the numOpenings to set
	 */
	public void setNumOpenings(int numOpenings) {
		this.numOpenings = numOpenings;
	}
	/**
	 * @return the band
	 */
	public String getBand() {
		return band;
	}
	/**
	 * @param band the band to set
	 */
	public void setBand(String band) {
		this.band = band;
	}
	
	
}
