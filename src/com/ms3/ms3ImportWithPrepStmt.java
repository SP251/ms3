package com.ms3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ms3ImportWithPrepStmt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection c = null;
		int totalNUmberOfRecordsProcessed = 0;
		int countOfEmptyCellRecords = 0;
		int count = 0;
		final Logger logger = Logger.getLogger(ms3ImportWithPrepStmt.class
				.getName());
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager
					.getConnection("jdbc:sqlite:C://sqlite/db/Database.db");
			PreparedStatement ps = c
					.prepareStatement("INSERT INTO ms3Interview  "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			c.setAutoCommit(false);
			BufferedReader br = new BufferedReader(new FileReader(
					"../ms3Interview/ms3Interview.csv"));
			br.readLine();
			String myRecord;
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
					.format(new Date());
			String csv = "../ms3Interview/bad-data" + timeStamp + ".csv";

			FileWriter file = new FileWriter(csv);
			final int batchSize = 500;

			while ((myRecord = br.readLine()) != null) {
				totalNUmberOfRecordsProcessed += 1;
				String[] tokens = myRecord
						.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				boolean isemptyCellFound = false;
				isemptyCellFound = isEmptyCellFound(tokens);
				if (isemptyCellFound) {
					countOfEmptyCellRecords += 1;
					CSVUtils.writeLine(file, Arrays.asList(tokens));
				} else {
					ps.setString(1, tokens[0]);
					ps.setString(2, tokens[1]);
					ps.setString(3, tokens[2]);
					ps.setString(4, tokens[3]);
					ps.setString(5, tokens[4]);
					ps.setString(6, tokens[5]);
					ps.setString(7, tokens[6]);
					ps.setString(8, tokens[7]);
					ps.setString(9, tokens[8]);
					ps.setString(10, tokens[9]);
					ps.addBatch();
					if (++count % batchSize == 0) {
						ps.executeBatch();
						c.commit();
					}
				}
			}
			ps.executeBatch();
			c.commit();
			ps.close();
			c.close();
			file.flush();
			file.close();
			br.close();
			logger.log(Level.INFO, "# records received ::::: "
					+ totalNUmberOfRecordsProcessed);
			logger.log(Level.INFO, "# records successful ::::: " + count);
			logger.log(Level.INFO, "# records failed ::::: "
					+ countOfEmptyCellRecords);
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.INFO, "# records received ::::: "
					+ totalNUmberOfRecordsProcessed);
			logger.log(Level.INFO, "# records successful ::::: " + count);
			logger.log(Level.INFO, "# records failed ::::: "
					+ countOfEmptyCellRecords);
			logger.log(Level.WARNING,
					e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public static boolean isEmptyCellFound(String[] tokens) {
		boolean isEmptyCellFound = false;
		for (int j = 0; j < tokens.length; j++) {
			if (tokens[j] == "" || tokens[j].isEmpty()) {
				isEmptyCellFound = true;
				break;
			}
		}
		return isEmptyCellFound;
	}

}
