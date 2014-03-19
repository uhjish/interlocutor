package org.omelogic.interlocutor.data;

/*
 *      CommodusResult.java
 *      
 *      Copyright 2008 Ajish D. George <ajish@hocuslocus.com>
 *      
 */

import java.io.Serializable;
import java.io.*;
import java.util.*;
import cern.colt.bitvector.BitVector;
import cern.colt.function.IntProcedure;


public class Interlocution implements Serializable 
{
	private static final long serialVersionUID = 1630080582353592164L;
	
	private Map<String, Locutus> annorows;
	private String[] queryIDs;
	private String[][] controlIDs;
	private String[] queriedTracks;
	boolean controlIsPopulation;
	boolean parsedQueryAnnos;
	boolean trackPrefixAppended;
	
	
	public Interlocution(String[] argQueryIDs, boolean argParsedQueryAnnos, boolean argTrackPrefixAppended)
	{
		this.queryIDs = argQueryIDs;
		controlIDs = new String[0][];
		
		annorows = new HashMap<String, Locutus>();	
		
		queriedTracks = new String[0];
		
		parsedQueryAnnos = argParsedQueryAnnos;
		trackPrefixAppended = argTrackPrefixAppended;
	}

	public void addControlIdentifiers( String[][] argControlIDs, boolean argControlIsPopulation)
	{
		this.controlIDs = argControlIDs;
		this.controlIsPopulation = argControlIsPopulation;
	}
	
	public void setLocutus( Locutus row )
	{
		annorows.put(row.getName(), row);
	}
	
	public Set<String> getAllTerms()
	{
		return annorows.keySet();
	}
	
	public int getQuerySize(){
		return queryIDs.length;
	}
	
	public boolean hasTrackPrefix()
	{
		return trackPrefixAppended;
	}
	
	public boolean parsedQueryAnnos(){
		return parsedQueryAnnos;
	}
	
	public String[] getQueriedTracks(){
		return queriedTracks;
	}
	
	public void setQueriedTracks( String[] qTracks ){
		queriedTracks = qTracks;
	}
	
	public boolean hasControls()
	{
		if (controlIDs == null) return false;
		if (controlIDs.length <1) return false;
		return true;
	}
	
	public boolean populationIsControl()
	{
		return controlIsPopulation;
	}
	
	public int getControlsCount()
	{
		if (!hasControls()){
			return 0;
		}
		if (controlIsPopulation){
			return controlIDs[0].length;
		}else{
			return controlIDs.length;
		}
	}
	
	public String[] getAllQueryIDs(){
		return queryIDs;
	}
	
	public Set<String> getOneTermQueryIDs( String anno )
	{
		final Set<String> returnSet = new HashSet<String>();
		
		BitVector qBits = annorows.get(anno).getQueryBits();
		
		qBits.forEachIndexFromToInState(0, qBits.size()-1, true, new IntProcedure()
			{
				public boolean apply(int element)
				{
					returnSet.add(queryIDs[element]);
					return true;
				}
			});
			
		return returnSet;
	}
	
	public Set<String> getIntersectQueryIDs( Set<String> annoSet )
	{
		final Set<String> returnSet = new HashSet<String>();
		
		Iterator<String> annoIter = annoSet.iterator();
		String curAnno;
		BitVector queryVector = new BitVector(queryIDs.length);
		queryVector.not();
		Locutus currLocutus;
		while( annoIter.hasNext() )
		{
			curAnno = annoIter.next();
			currLocutus = (Locutus)annorows.get(curAnno);
			queryVector.and( currLocutus.getQueryBits() );
		}
		
		queryVector.forEachIndexFromToInState(0, queryVector.size()-1, true, new IntProcedure()
			{
				public boolean apply(int element)
				{
					returnSet.add(queryIDs[element]);
					return true;
				}
			});
			
		return returnSet;
	}

	public Set<String> getUnionQueryIDs( Set<String> annoSet )
	{
		final Set<String> returnSet = new HashSet<String>();
		if (annoSet.size() == 0) return returnSet;
		Iterator<String> annoIter = annoSet.iterator();
		String curAnno;
		BitVector queryVector = new BitVector(queryIDs.length);
		//queryVector.not();
		Locutus currLocutus;
		while( annoIter.hasNext() )
		{
			curAnno = annoIter.next();
			currLocutus = (Locutus)annorows.get(curAnno);
			queryVector.or( currLocutus.getQueryBits() );
		}
		
		queryVector.forEachIndexFromToInState(0, queryVector.size()-1, true, new IntProcedure()
			{
				public boolean apply(int element)
				{
					returnSet.add(queryIDs[element]);
					return true;
				}
			});
			
		return returnSet;
	}

	public int getIntersectQueryCount( Set<String> annoSet ) throws Exception
	{
		if (annoSet.size() ==0) return queryIDs.length;
		
		Iterator<String> annoIter = annoSet.iterator();
		String curAnno;
		BitVector queryVector = new BitVector(queryIDs.length);
		queryVector.not();
		Locutus currLocutus;
		while( annoIter.hasNext() )
		{
			curAnno = annoIter.next();
			currLocutus = (Locutus)annorows.get(curAnno);
			queryVector.and( currLocutus.getQueryBits() );
		}
		
		return queryVector.cardinality();
	}

	public int[] getIntersectControlCounts( Set<String> annoSet ) throws Exception
	{
		if (annoSet.size() ==0) return new int[controlIDs.length];
		
		Iterator<String> annoIter = annoSet.iterator();
		String curAnno;
		BitVector[] controlVectors = new BitVector[controlIDs.length];
		for (int i = 0; i < controlIDs.length; i++){
			controlVectors[i] = new BitVector(controlIDs[i].length);
			controlVectors[i].not();
		}
		Locutus currLocutus;
		BitVector[] cBits;
		while( annoIter.hasNext() )
		{
			curAnno = annoIter.next();
			currLocutus = (Locutus)annorows.get(curAnno);
			cBits = currLocutus.getControlBits();
			for (int j = 0; j < cBits.length; j++){
				controlVectors[j].and( cBits[j] );
			}
		}
		int[] retVals = new int[controlIDs.length];
		for (int k = 0; k < controlIDs.length; k++){
			retVals[k]=controlVectors[k].cardinality();
		}
		
		return retVals;
	}
	public int getUnionQueryCount( Set<String> annoSet ) throws Exception
	{
		if (annoSet.size() ==0) return queryIDs.length;
		
		Iterator<String> annoIter = annoSet.iterator();
		String curAnno;
		BitVector queryVector = new BitVector(queryIDs.length);
		//queryVector.not();
		Locutus currLocutus;
		while( annoIter.hasNext() )
		{
			curAnno = annoIter.next();
			currLocutus = (Locutus)annorows.get(curAnno);
			queryVector.or( currLocutus.getQueryBits() );
		}
		
		return queryVector.cardinality();
	}
	
	public double getIntersectSignificance( Set<String> annoSet ) throws Exception
	{	
		if (annoSet.size() ==0) return 1;
		if (controlIDs.length == 0) return 1;
		
		Iterator<String> annoIter = annoSet.iterator();
		BitVector queryVector = new BitVector(queryIDs.length);
		BitVector[] ctrlVectors = new BitVector[controlIDs.length];
		BitVector[] tempCtrlVectors;
		queryVector.not();
		
		String curAnno;
		Locutus currLocutus;		
		int curCtrlIndex;

		int querySize = queryIDs.length;
		int[] controlSetSizes = new int[controlIDs.length];
		try{
			for (curCtrlIndex = 0; curCtrlIndex < ctrlVectors.length; curCtrlIndex++)
			{
				controlSetSizes[curCtrlIndex] = controlIDs[curCtrlIndex].length;
				ctrlVectors[curCtrlIndex] = new BitVector(controlIDs[curCtrlIndex].length);
				ctrlVectors[curCtrlIndex].not();
			}
		}catch (Exception e){
			throw new Exception("Interlocution.getSignificance | Could not get controlID vectors of valid length in: " + e.toString());
		}
		
		while( annoIter.hasNext() )
		{
			curAnno = annoIter.next();
			currLocutus = (Locutus)annorows.get(curAnno);
			queryVector.and( currLocutus.getQueryBits() );
			tempCtrlVectors = currLocutus.getControlBits();
			for (curCtrlIndex=0;curCtrlIndex < ctrlVectors.length; curCtrlIndex++)
			{
				ctrlVectors[curCtrlIndex].and(tempCtrlVectors[curCtrlIndex]);
			}
			
		}
		
		int queryCount = queryVector.cardinality();
		int[] controlCounts = new int[controlIDs.length];
		for (curCtrlIndex=0; curCtrlIndex < ctrlVectors.length; curCtrlIndex++)
		{
			controlCounts[curCtrlIndex] = ctrlVectors[curCtrlIndex].cardinality();
		}
		
		double significance;
		
		if (controlIsPopulation)
		{
			significance = getPopulationSignificance( queryCount, controlCounts[0], querySize, controlSetSizes[0]);
		}else{
			significance = getSamplingSignificance( queryCount, controlCounts, querySize, controlSetSizes);
		}
		//System.out.println(significance);
		return significance;
		
	}	
	
	public double getUnionSignificance( Set<String> annoSet ) throws Exception
	{	
		if (annoSet.size() ==0) return 1;
		if (controlIDs.length == 0) return 1;
		
		Iterator<String> annoIter = annoSet.iterator();
		BitVector queryVector = new BitVector(queryIDs.length);
		BitVector[] ctrlVectors = new BitVector[controlIDs.length];
		BitVector[] tempCtrlVectors;
		//queryVector.not();
		
		String curAnno;
		Locutus currLocutus;		
		int curCtrlIndex;

		int querySize = queryIDs.length;
		int[] controlSetSizes = new int[controlIDs.length];
		try{
			for (curCtrlIndex = 0; curCtrlIndex < ctrlVectors.length; curCtrlIndex++)
			{
				controlSetSizes[curCtrlIndex] = controlIDs[curCtrlIndex].length;
				ctrlVectors[curCtrlIndex] = new BitVector(controlIDs[curCtrlIndex].length);
				//ctrlVectors[curCtrlIndex].not();
			}
		}catch (Exception e){
			throw new Exception("Interlocution.getSignificance | Could not get controlID vectors of valid length in: " + e.toString());
		}
		
		while( annoIter.hasNext() )
		{
			curAnno = annoIter.next();
			currLocutus = (Locutus)annorows.get(curAnno);
			queryVector.or( currLocutus.getQueryBits() );
			tempCtrlVectors = currLocutus.getControlBits();
			for (curCtrlIndex=0;curCtrlIndex < ctrlVectors.length; curCtrlIndex++)
			{
				ctrlVectors[curCtrlIndex].or(tempCtrlVectors[curCtrlIndex]);
			}
			
		}
		
		int queryCount = queryVector.cardinality();
		int[] controlCounts = new int[controlIDs.length];
		for (curCtrlIndex=0; curCtrlIndex < ctrlVectors.length; curCtrlIndex++)
		{
			controlCounts[curCtrlIndex] = ctrlVectors[curCtrlIndex].cardinality();
		}
		
		double significance;
		
		if (controlIsPopulation)
		{
			significance = getPopulationSignificance( queryCount, controlCounts[0], querySize, controlSetSizes[0]);
		}else{
			significance = getSamplingSignificance( queryCount, controlCounts, querySize, controlSetSizes);
		}
		//System.out.println(significance);
		return significance;
		
	}	
	
    private double getPopulationSignificance( int support, int popCount, int qSetSize, int controlSetSize) throws Exception
    {
    	double popSig, popFreq;
    	//System.out.println(support + " " + qSetSize + " " +popCount+ " " + controlSetSize);
    	popFreq = (double)popCount/(double)controlSetSize; 
    	if (popFreq == 1){
    		return 1;
		}
    	popSig = cern.jet.stat.Probability.binomialComplemented(support, qSetSize, popFreq);
		return popSig;	
	}
	
	private double getSamplingSignificance( int support,  int[] ctrlCounts, int qSetSize, int[] controlSetSizes ) throws Exception
	{
		int numControls = controlSetSizes.length;
		double mean =0, sd =0;
		double prop, sum=0, sumsq=0;
		for (int i = 0; i < numControls; i++){
			prop = (double)ctrlCounts[i] / (double)controlSetSizes[i];
			sum += prop;
			sumsq += prop*prop;
		}
		mean = sum/numControls;
		sd = Math.sqrt(sumsq - sum*sum);
		prop = support / qSetSize;
		double tstat = (mean - prop)/(sd/Math.sqrt(numControls));
		return cern.jet.stat.Probability.studentT(numControls-1, tstat);
		
	}

	
	public static Interlocution parseInterlocution( File file ) throws Exception
	{
		BufferedReader buffer;
		try{
		buffer = new BufferedReader( new InputStreamReader( new FileInputStream(file) ) );
		}catch(Exception e){
			throw new Exception("getGraphFromFileERR(31): Can't make BufferedReader from File "+e.toString()); 
		}

		return parseInterlocution(buffer);

		//throw new Exception("GraphFileHandlerERROR: Invalid format - " + format);
		
	}
	
	private static Interlocution parseInterlocution( BufferedReader buffer ) throws Exception
	{
		
		Interlocution returnData = null;
		

		int qSetSize =0;
		int ctrlSetSize=0;

		int numIntLines =0;
		String line;
		for( ; ; )
		{
			try 
			{ //process each line in buffer
				line = buffer.readLine();
				
				if (line == null)
				{ //exit for loop condition
					break;
				}
			}catch (Exception e){
				
				//catch IOException
				//break and die 
				//fails to make LocusSet
				throw new Exception("OmeLogicOmeletERROR: Couldn't read in line from gtf file! " + e.toString());
			}
			//got a good line, now process it

			String[] fields;
	
			//ignore comments and continue
			if ( line.startsWith("#") )
			{
				fields = line.split( "\\s+" );
				qSetSize = Integer.parseInt(fields[1]);
				ctrlSetSize = Integer.parseInt(fields[3]);
				String[] queryIDs = new String[qSetSize];
				String[] ctrlIDs = new String[ctrlSetSize];
				line = buffer.readLine();
				queryIDs = line.split("\\s+");
				int idCt;
				for(idCt =0; idCt < ctrlSetSize; idCt++){
					ctrlIDs[idCt] = "id."+idCt;
				}
				String[][] ctrlIDsSet = new String[1][];
				ctrlIDsSet[0]=ctrlIDs;
				returnData = new Interlocution( queryIDs , false, false);
				returnData.addControlIdentifiers(ctrlIDsSet, true);
				continue;
			}

			//process locus line
			numIntLines++;

			fields = line.split( "\\t" );
			String anno = fields[0];
			
			String queryPos = fields[1].replaceAll("[\\{\\,\\}]","");
			String ctrlPos = fields[2].replaceAll("[\\{\\,\\}]","");
			
			BitVector queryVector = new BitVector(qSetSize);
			BitVector ctrlVector = new BitVector(ctrlSetSize);
			BitVector[] ctrlVectorSet = new BitVector[1];
			ctrlVectorSet[0]=ctrlVector;
			
			int curpos;
			String[] posns;
			posns = queryPos.split("\\s");
			for(curpos = 0; curpos <posns.length; curpos++)
			{
				try{
					queryVector.set( Integer.parseInt(posns[curpos]) );
				}catch(Exception e){}
			}
			posns = ctrlPos.split("\\s");
			for(curpos = 0; curpos <posns.length; curpos++)
			{
				try{
					ctrlVector.set( Integer.parseInt(posns[curpos]) );
				}catch(Exception e){}
			}
			
			Locutus curLocutus = new Locutus( anno, queryVector, ctrlVectorSet );
			returnData.setLocutus( curLocutus );
		}
		
		return returnData;
	}
			
			
}
