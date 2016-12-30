/*
 * @(#)CSVLoader.java $Date: Dec 7, 2011 9:32:20 AM $
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;


/**
 * @author Christopher Steel - FortMoon Consulting, Inc.
 *
 * @since Dec 7, 2011 9:32:20 AM
 */
public class CSVLoader {
	
	protected String token = "\\|"; 
	protected File file = null;
	protected String fileName = null;
	protected Connection con = null;
	protected Statement st = null;
	protected ResultSet rs = null;
	protected int batchSize = 5000;
	protected ArrayList<String> columnNames = new ArrayList<String>();
	protected ColumnModel columnModel = new ColumnModel();
	protected String url = "jdbc:oracle:thin:@64.91.229.95:1521:ora";
	protected String user = "WPT_STAGING";
	protected String password = "WPT_STAGING";
	protected FileReader reader;
	protected LineNumberReader lr;
	protected String tableName;
	protected String insertPreamble;
	protected long numLines = 0;
	protected MessageDigest digest;
	protected BlockingQueue<ArrayList<String>> queue = new LinkedBlockingQueue<ArrayList<String>>(1);
	protected boolean skipBlobs = true;
    private Logger log = Logger.getLogger(CSVLoader.class.getName());
    
	
    
    public CSVLoader() {
    	log.info("called");
    	try {
			digest = MessageDigest.getInstance("MD5");
	    	Class.forName ("oracle.jdbc.OracleDriver");
		}
		catch (Exception e) {
			//log.error("Exception in constructor: " + e , e);
			throw new RuntimeException("Exception in constructor: " + e , e);
		}

    }
    
    public void load() throws Exception {
    	file = new File(fileName);
    	tableName = fileName.substring(0, fileName.indexOf('.'));
    	String line = null;
    	int counter = 0;
    	try {
			reader = new FileReader(file);
			lr = new LineNumberReader(reader);
			line = lr.readLine();
			if(line != null)
				getColumns(line); //Now we read the header, mark the file position for future resets

			getColumnClasses();
			resetReader();

			line = lr.readLine();
			while(line != null) {
				//if(line.contains("|VT")) {
					counter++;
					addValues(statement, line, counter);
					// update every 1000 records
					if (counter % batchSize == 0) {
						log.info("Executing batch for line " + counter + " of " + this.numLines);
						executeBatch(statement);
					}
				//}
				line = lr.readLine();
			}
			// get the last ones
			log.info("Executing final batch");
			executeBatch(statement);
			log.info("All batches completed succesfully.");
		} 
    	catch (Exception e) {
			e.printStackTrace();
			throw e;
		} 
    	finally {
    		try {
				reader.close();
				if (con != null)
					con.close();
			}
			catch (IOException e) {
				log.error("Non-Fatal Exception closing FileReader: " + e.getMessage(), e);
			}
    	}
		log.info("\n\nLoad complete. Goodbye.\n");
    }
    
    private void getColumns(String line) {
    	log.trace("called");
    	StringTokenizer tokenizer = new StringTokenizer(line, this.token);
    	while(tokenizer.hasMoreTokens()) {
    		String name = tokenizer.nextToken();
    		name = name.replace(' ', '_');
    		name = name.replace('-', '_');
    		this.columnNames.add(name);
    	}
    	log.info("Column names: " + this.columnNames);
    	log.info("Number of columns: " + this.columnNames.size());

//    	this.columns = new HashMap<String, ColumnBean>(this.columnNames.size());
    	for(String name : this.columnNames) {
    		ColumnBean column = new ColumnBean();
    		column.setName(name);
    		columnModel.add(column);
    	}
    }
    
    private void getColumnClasses() throws Exception {
		log.trace("called");
		for(int i = 0; i < this.columnModel.size(); i++) {
			if(log.isInfoEnabled())
				log.info("Getting column class for column: " + this.columnNames.get(i));
			getColumnClass(i);
			resetReader(); //reset to 2 line of file
		}

		// Let's just get use the first unique Int/BigInt as the PK
		boolean foundPK = false;
		// Set all the null columns to VARCHAR
		for(ColumnBean column : this.columnModel) {
			SQLTYPE type = column.getType();
			if(type.equals(SQLTYPE.NULL)) {
				column.setType(SQLTYPE.VARCHAR);
			}
			if((type.equals(SQLTYPE.INTEGER) || type.equals(SQLTYPE.BIGINT)) 
					&& column.isUnique() && !foundPK) {
				column.setPrimaryKey(true);
				foundPK = true;
			}
			if(type == SQLTYPE.BIGINT || type == SQLTYPE.INTEGER || type == SQLTYPE.FLOAT || type == SQLTYPE.DOUBLE) {
				column.setCharBased(false);
			}
		}
		log.info("Column Types: " + this.columnModel);
    }
	
	private void getColumnClass(int columnNum) throws Exception {
		log.trace("called");
		// Let's try and find uniques without running out of memory. Dump the list as soon as we find a dup.
		
		TreeSet<String> uniques = new TreeSet<String>();
		numLines = 1;
		String line = lr.readLine();
		ColumnBean column = this.columnModel.get(columnNum);
		try {
			while (line != null) {
				log.debug("Line: " + line);
				// StringTokenizer tokenizer = new StringTokenizer(line,
				// this.token);
				String[] result = line.split(this.token);
				// Special case for last column as null
				String val = null;
				if(result.length == this.columnNames.size() - 1)  {//last column must be null
					val = null;
				}
				else {
					val = result[columnNum];
					log.debug("Result of string parse: " + val);
				}
				// String colName = this.columnNames.get(columnNum);
				if (val == null || val.isEmpty() || val.equalsIgnoreCase("null")) {
					if (log.isDebugEnabled())
						log.trace("********NULL Value for column: " + column + "*********");
					column.setNullable(true);
					column.setUnique(false);
				}
				else {
					if (log.isDebugEnabled())
						log.debug("Value for column: " + column.getName() + " = " + val + " line = " + line);
					SQLTYPE colType = SQLUtil.getType(val);
					if (skipBlobs && (colType == SQLTYPE.BLOB || colType == SQLTYPE.LONGBLOB))
						colType = SQLTYPE.VARCHAR;

					log.debug("SQL Type: " + colType);
					if (column.getType().getValue() > colType.getValue()) {
						column.setType(colType);
					}
					int valSize = val.length();
					if (skipBlobs && valSize > 255)
						valSize = 255;
					if (column.getColumnSize() < valSize) {
						column.setColumnSize(valSize);
						log.debug("Size for column: " + column.getName() + " = " + column.getColumnSize());
					}
					digest.update(val.getBytes());
					String hash = new String(digest.digest());
					if (uniques != null && !uniques.contains(hash))
						uniques.add(hash);
					else {
						uniques = null;
						column.setUnique(false);
					}

				}
				line = lr.readLine();
				numLines++;
				if (numLines % 1000 == 0)
					log.debug("Finished processing number of lines in first pass scan: " + numLines);
			}
		}
		catch (Exception e) {
			log.error("Exception getting column class at linenum : " + numLines + " [" + line + "]\n" + e);
			throw e;
		}
	}	

	public void resetReader() {
    	try {
    		// lr.mark and lr.reset don't work for very large files (failed after 500000 lines)
    		reader.close();
			reader = new FileReader(file);
    		lr = new LineNumberReader(reader);
			// reread the header row to skip.
			lr.readLine();
			//lr.reset();
		} catch (IOException e) {
			log.error("Exception resetting reader: " + e, e);
		}
    }

	private void addValues(PreparedStatement statement, String line, long lineNum) throws Exception {
		String[] result = line.split(token);
		int colIndex = 0;
		for(String val : result) {
//			if(skipBlobs && val.length() > 255)
//				val = val.substring(0, 254);
//			if(val.contains("'")) {
//				log.warn("*****WARNING****** Found \' in data, replacing with ' '" + " at column: " + this.columnNames.get(colIndex) + " line: " + lineNum + " index: " + line.indexOf("\""));
//				val = val.replace('\'', ' ');
//			}
			SQLUtil.setValue(statement, colIndex, val);
			colIndex++;
		}
		try {
			statement.addBatch();
		}
		catch(Exception e) {
			log.info("Exception adding values at line: " + lineNum + "    String: " + line + "   " + e.getMessage());
			throw e;
		}
	}

	public void createTable() throws Exception {
        try {
			con = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			log.error("Exception getConnection: " + e ,e);
			return;
		}
        boolean first = true; 
        Statement stmt = null;
		StringBuffer cs;
		cs = new StringBuffer("create table " + tableName + " (");
		for(ColumnBean column : this.columnModel) {
			if(first) {
				first = false;
			}
			else {
			 cs.append(", ");
			}
			cs.append(column.getName());
			cs.append(" " + column.getType().toString());
			if(column.getType() == SQLTYPE.VARCHAR)
				cs.append("(" + (column.getColumnSize()+3000) + ")");
			if(!column.isNullable())
				cs.append(" NOT NULL");
			if(column.isPrimaryKey())
				cs.append(" PRIMARY KEY");
			else
				if(column.isUnique())
					cs.append(" UNIQUE");


		}
		cs.append(")");
		log.info("Table create string: " + cs);
		final String createString = cs.toString();
		try {
			stmt = con.createStatement();
			stmt.executeUpdate(createString);
			String alter = "alter session set nls_date_format='yyyy-mm-dd'";
			stmt.clearBatch();
			stmt.executeUpdate(alter);
		}
		catch (SQLException ex) {
			log.error("SQLException: " + ex.getMessage(), ex);
			throw ex;
		}
		finally {
			try {
				stmt.close();
			}
			catch (SQLException e) {
				log.error("Exception closing statement: " + e.getMessage(), e);
			}
			try {
				con.close();
			}
			catch (SQLException e) {
				log.error("Exception closing DB connection: " + e.getMessage(), e);
			}
		}

    }
	
	protected PreparedStatement createStatement() throws SQLException {
	    boolean first = true;

	    con = DriverManager.getConnection(url, user, password);
	    con.setAutoCommit(false);

	    StringBuffer insert = new StringBuffer("insert into " + this.tableName + " (");
		for (String col : this.columnNames) {
			if (first)
				insert.append(col);
			else
				insert.append(", " + col);
			first = false;
		}
		insert.append(") values(");
		first = true;
		for (String col : this.columnNames) {
			if(log.isTraceEnabled())
				log.trace("Col = " + col);
			if (first)
				insert.append("?");
			else
				insert.append(", ?");
			first = false;
		}
		insert.append(")");
		log.debug("Insert statement: " + insert);

		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(insert.toString());
			//stmt.setExecuteBatch(1);
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
		
		return stmt;
	}
    
	public void executeBatch(PreparedStatement statement) {
		int[] updateCounts = null;
		try {
			log.info("Starting execute.");
			//if (statement.getMaxRows() > 1) {
				updateCounts = statement.executeBatch();
				log.info("Committed number of rows: " + updateCounts.length);
			//}

			log.info("Starting commit.");
			con.commit();
			statement.clearBatch();
			// log.info("Committed number of rows: 1");
		}
		catch (SQLException ex) {
			try {
				log.error("Exception executing batch. rolling back due to: " + ex);
				con.rollback();
			}
			catch (SQLException e) {
				log.error("Exception rolling back batch: " + e);
				e.printStackTrace();
			}
		}
	}

	public String getVersion() throws Exception {
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            rs = st.executeQuery("select * from v$version");

            if (rs.next()) {
                log.debug("Database version number: " + rs.getString(1));
                return rs.getString(1);
            }

        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
            System.out.println(ex);
            throw ex;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return null;
		
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the batchSize
	 */
	public int getBatchSize() {
		return batchSize;
	}

	/**
	 * @param batchSize the batchSize to set
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public static void main(String[] args) throws Exception {
		CSVLoader loader = new CSVLoader();
		loader.getVersion();

		Options options = new Options();

		// add t option
		options.addOption("f", true, "REQUIRED: Filename to load");
		options.addOption("t", true, "token delimiter(s). Defaults to comma.");
		options.addOption("u", true, "Database username. Defaults to presto.");
		options.addOption("p", true, "Database password. Defaults to presto.");
		options.addOption("d", true, "Database connection string  Defaults to jdbc:mysql://presto270db.coksdj9a4svg.us-east-1.rds.amazonaws.com/BigData.");
		options.addOption("s", true, "Batch size for row inserts. Defaults to 1000.");
		options.addOption("n", true, "Database table name to create. Defaults to filename without the extension (i.e. Myfile.csv would create a table Myfile.");
		options.addOption("nb", false, "Truncate entries to 255 character VARCHARs (no blobs)");
		options.addOption("v", false, "Verbose. Turns on debug level logging.");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);
		if(cmd.hasOption("f")) {
			loader.setFileName(cmd.getOptionValue("f"));
		}
		else {
		    System.err.println("USAGE");
		}

		loader.load();

	}

}
