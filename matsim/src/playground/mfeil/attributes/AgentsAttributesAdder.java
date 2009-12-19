/* *********************************************************************** *
 * project: org.matsim.*
 * AgentsAttributesAdder.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.mfeil.attributes;

import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.basic.v01.IdImpl;




/**
 * Reads agents attributes from a given *.txt file.
 *
 * @author mfeil
 */
public class AgentsAttributesAdder {

	private static final Logger log = Logger.getLogger(AgentsHighestEducationAdder.class);
	private Map<Id, Double> income;
	private Map<Id, Integer> carAvail;
	private Map<Id, Integer> seasonTicket;	
	private Map<Id, Double> agentsWeight;
	


	public AgentsAttributesAdder() {
		this.income = new TreeMap<Id, Double>();
		this.carAvail = new TreeMap<Id, Integer>();
		this.seasonTicket = new TreeMap<Id, Integer>();
		this.agentsWeight = new TreeMap<Id, Double> ();
	}
	
	public static void main (String[]args){
		final String input1 = "D:/Documents and Settings/Matthias Feil/Desktop/workspace/MATSim/plans/MobTSet_1.txt";
		final String input2 = "D:/Documents and Settings/Matthias Feil/Desktop/workspace/MATSim/plans/plans.dat";
		final String output = "D:/Documents and Settings/Matthias Feil/Desktop/workspace/MATSim/plans/output.txt";
		
		ArrayList<String> ids = new AgentsAttributesAdder().readPlans (input2);
		new AgentsAttributesAdder().runMZZurich10(input1, output, ids);
	}
	
	// reads the Ids of the Zurich10 agents from a Biogeme estimation data file
	public ArrayList<String> readPlans (final String input2){
		log.info("Reading input2 file...");
		ArrayList<String> ids = new ArrayList<String>();
		try {

			FileReader fr = new FileReader(input2);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			StringTokenizer tokenizer = null;
			line = br.readLine(); // do not parse first line which just
									// contains column headers
			line = br.readLine();
			String tokenId = null;
			while (line != null) {		
				
				tokenizer = new StringTokenizer(line);
				
				tokenId = tokenizer.nextToken();
				ids.add(tokenId);
				
				line = br.readLine();
			}		
		} catch (Exception ex) {
			System.out.println(ex);
		}
		log.info("done...");
		return ids;
	}
	
	// reads agent attributes for the selected Ids of the above class from the MobTSet_1 file
	public void runMZZurich10 (final String inputFile, final String outputFile, final ArrayList<String> ids){
		
		log.info("Reading input1 file...");
		
		String outputfile = outputFile;
		PrintStream stream;
		try {
			stream = new PrintStream (new File(outputfile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		stream.println("Agent_id\tweight\tage\tgender\tlicense\tincome_simulated\tinc_4\tinc_4_8\tincome_8_12\tincome_12_on\tincome_clustered\tcar_avail");					
		try {

			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			StringTokenizer tokenizer = null;
			line = br.readLine(); // do not parse first line which just
									// contains column headers
			line = br.readLine();
			String tokenId = null;
			String token1 = null;
			String token2 = null;
			String token3 = null;
			String token4 = null;
			while (line != null) {
				tokenizer = new StringTokenizer(line);
				
				tokenId = tokenizer.nextToken();
				if (!ids.contains(tokenId)) {
					line = br.readLine();
					continue;
				}
								
				// Watch out that the order is equal to the order in the file!
				stream.print(tokenId+"\t"+tokenizer.nextToken()+"\t"+tokenizer.nextToken()+"\t"+tokenizer.nextToken()+"\t"+tokenizer.nextToken()+"\t");					
				tokenizer.nextToken();				
				stream.print(tokenizer.nextToken()+"\t");
				
				int income = 0;
				token1 = tokenizer.nextToken();
				token2 = tokenizer.nextToken();
				token3 = tokenizer.nextToken();
				token4 = tokenizer.nextToken();
				if (token1.equals("1")) income = 2000;
				else if (token2.equals("1")) income = 6000;
				else if (token3.equals("1")) income = 10000;
				else if (token4.equals("1")) income = 16000;
				else log.warn("For agent "+tokenId+", no valid income could be detected!");
				stream.print(token1+"\t"+token2+"\t"+token3+"\t"+token4+"\t"+income+"\t");		
				
				for (int i=0;i<3;i++) tokenizer.nextToken();		
				stream.println(tokenizer.nextToken());
				
				line = br.readLine();
			}		
		} catch (Exception ex) {
			System.out.println(ex);
		}
		log.info("done...");
	}	
	
	/** reads some Biogeme-estimation attributes for the selected Ids of the above class from the MobTSet_1 file and writes them
	 * Biogeme compatible
	 * @param inputFile
	 * @param outputFile
	 * @param ids
	 */
	public void runMZZurich10ForBiogeme (final String inputFile, final String outputFile, final ArrayList<String> ids){
		
		log.info("Starting Biogeme compilation...");
		
		String outputfile = outputFile;
		PrintStream stream;
		try {
			stream = new PrintStream (new File(outputfile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		stream.println("Id\tChoice\tAge\tGender\tLicense\tIncome\tCar_always\tCar_sometimes\tav1\tav2\tav3");					
		try {

			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			StringTokenizer tokenizer = null;
			line = br.readLine(); // do not parse first line which just
									// contains column headers
			line = br.readLine();
			String tokenId = null;
			
			while (line != null) {
				tokenizer = new StringTokenizer(line);
				
				tokenId = tokenizer.nextToken();
				if (!ids.contains(tokenId)) {
					line = br.readLine();
					continue;
				}
								
				// Id
				stream.print(tokenId+"\t");
				tokenizer.nextToken();
				
				// Age, gender, license
				String age = tokenizer.nextToken();
				String gender = tokenizer.nextToken();
				String license = tokenizer.nextToken();	
				tokenizer.nextToken();
				
				// Income	
				String income = tokenizer.nextToken();
				for (int i=0;i<7;i++) tokenizer.nextToken();
				
				// Car Avail	
				int carAlways = 0;
				int carSometimes = 0;
				String carAvail = tokenizer.nextToken();
				if (carAvail.equals("1")) carAlways = 1;
				else if (carAvail.equals("2")) carSometimes = 1;
				for (int i=0;i<11;i++) tokenizer.nextToken();
				
				String ticket = tokenizer.nextToken();
				int choice = 0;
				if (ticket.equals("2") || ticket.equals("3")) choice = 3;
				else if (ticket.equals("11")) choice = 1;
				else choice = 2;
				
				stream.println(choice+"\t"+age+"\t"+gender+"\t"+license+"\t"+income+"\t"+carAlways+"\t"+carSometimes+"\t1\t1\t1");		
				
				line = br.readLine();
			}		
		} catch (Exception ex) {
			System.out.println(ex);
		}
		log.info("done...");
	}	
	
	// Reads the agent attributes from MobTSet_1 as requested by PlansConstructor
	public void runMZ (final String inputFile){
		
		log.info("Reading input file...");
		
		try {

			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			StringTokenizer tokenizer = null;
			line = br.readLine(); // do not parse first line which just
									// contains column headers
			line = br.readLine();
			String tokenId = null;
			String token = null;
			while (line != null) {		
				
				tokenizer = new StringTokenizer(line);
				
				tokenId = tokenizer.nextToken();
				
				// Watch out that the order is equal to the order in the file!
				token = tokenizer.nextToken();				
				income.put(new IdImpl(tokenId), Double.parseDouble(token)*1000);
				
				token = tokenizer.nextToken();				
				carAvail.put(new IdImpl(tokenId), (int)(Double.parseDouble(token)));
				
				token = tokenizer.nextToken();				
				seasonTicket.put(new IdImpl(tokenId), (int)(Double.parseDouble(token)));
				
				token = tokenizer.nextToken();				
				this.agentsWeight.put(new IdImpl(tokenId), Double.parseDouble(token));
				
				line = br.readLine();
			}		
		} catch (Exception ex) {
			System.out.println(ex);
		}
		log.info("done...");
	}	
	
	public void runZurich10 (final String inputFile){
		
		log.info("Reading input file...");
		
		try {

			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			StringTokenizer tokenizer = null;
			line = br.readLine(); // do not parse first line which just
									// contains column headers
			line = br.readLine();
			String tokenId = null;
			String token = null;
			while (line != null) {		
				
				tokenizer = new StringTokenizer(line);
				
				tokenId = tokenizer.nextToken();
				
				// Watch out that the order is equal to the order in the file!
				for (int i=0;i<5;i++) tokenizer.nextToken(); // jump over irrelevant information
				token = tokenizer.nextToken();		
				income.put(new IdImpl(tokenId), Double.parseDouble(token));
				
				line = br.readLine();
			}		
		} catch (Exception ex) {
			System.out.println(ex);
		}
		log.info("done...");
	}	
	
	public Map<Id, Double> getIncome (){
		return this.income;
	}
	
	public Map<Id, Integer> getCarAvail (){
		return this.carAvail;
	}
	
	public Map<Id, Integer> getSeasonTicket (){
		return this.seasonTicket;
	}
}

