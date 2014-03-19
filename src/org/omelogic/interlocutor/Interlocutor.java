package org.omelogic.interlocutor;

import org.omelogic.interlocutor.data.*;
import org.omelogic.hldbmanager.*;
import org.omelogic.lia.*;
import org.omelogic.locus.*;
import org.omelogic.locus.filter.*;

import cern.jet.stat.Probability;
import cern.colt.bitvector.*;
import java.util.*;
import java.io.*;
import org.apache.commons.math.distribution.*;
import org.apache.commons.math.stat.inference.*;

public class Interlocutor
{
	private static final String TRACK_PREFIX_SEPARATOR = ".";

	private HLDBManager hocusLink;
	private LocusSet qSet;
	private LocusSet flatQSet;
	private String qSetSource;
		
	private boolean controlIsPopulation;
	private boolean controlIsSet;
	
	private boolean parseQueryAnnos;
	private boolean addTrackPrefix;

	//hashmapped on annotation terms, 
	//contains rows corresponding to loci in qSet
	private HashMap< String, BitVector > locutus;
	
	//private ArrayList<String> annoNames;
	
	private LocusSet[] controlSets;
	private LocusSet[] flatControlSets;
	private HashMap< String, BitVector[] > controlocutus;
	
	private String[] queriedTracks;

	private FileOutputStream fos;
	private PrintStream ps;


	public Interlocutor(LocusSet argQSet, HLDBManager dbmLink, boolean argParseQueryAnnos, boolean argAddTrackPrefix) throws Exception{
		fos = new FileOutputStream("Interlocutor.err");
		ps = new PrintStream(fos);
		System.setErr(ps);
		System.err.println("New interLocutor instance");	
		hocusLink = dbmLink;

		qSet = argQSet;
		qSetSource = qSet.getLocusByIndex(0).getSource();
		if (qSetSource == null ){ qSetSource = qSet.getName();}
		locutus = new HashMap<String, BitVector>();

		parseQueryAnnos = argParseQueryAnnos;
		addTrackPrefix = argAddTrackPrefix;	

		controlSets = new LocusSet[0];
		controlocutus = new HashMap<String, BitVector[]>();
	
	}
	
	public void setPopulationControl(LocusSet argCtrl) throws Exception
	{
		controlIsPopulation = true;
		controlSets = new LocusSet[]{ argCtrl };
	
	}
	
	public void setSamplingControls(List<LocusSet> argCtrls) throws Exception
	{
		controlIsPopulation = false;
		controlSets = new LocusSet[argCtrls.size()];
		LocusSet curCtrl;
		for (int i = 0; i < argCtrls.size(); i++){
			curCtrl = argCtrls.get(i);
			controlSets[i] = curCtrl;
		}
	}

	private void parseQuerySet() throws Exception
	{
		System.err.println("Parsing query set");
		String prefix = addTrackPrefix ? "query" + TRACK_PREFIX_SEPARATOR : "";
		for (int i = 0; i < qSet.getSize(); i++)
		{
			Locus currLocus = qSet.getLocusByIndex(i);
			//processAnnoTerm(i, prefix+currLocus.getID());
			processAnnoTerm(i, prefix+currLocus.getChromosome());
			processAnnoTerm(i, prefix+currLocus.getStrandShortString());
			//processAnnoTerm(i, prefix+currLocus.getSource());
			//processAnnoTerm(i, prefix+currLocus.getType());
			
			//add remaining annots
			HashMap<String, Object> tgtAnnots = currLocus.getAnnotationClone();
			
			Iterator< String > annoKeys = tgtAnnots.keySet().iterator();
	
			//add all key-value pairs from here
			while( annoKeys.hasNext() ){
				String iterKey = annoKeys.next();
				if (iterKey.equals("setname")){
					continue;
				}
				Object iterValObj = currLocus.getAnnotation(iterKey);
				String iterVal;
				if (iterValObj instanceof String)
				{
					iterVal = (String) iterValObj;
					processAnnoTerm(i, prefix+ iterKey );
					processAnnoTerm(i, prefix+ iterVal );
				}
				if (iterValObj instanceof String[]){
					String[] iterValList = (String[]) iterValObj;
					for (int k = 0; k < iterValList.length; k++){
						iterVal = iterValList[k];	
						processAnnoTerm(i, prefix+ iterKey);
						processAnnoTerm(i, prefix+ iterVal );
					}
				}
				if (iterValObj instanceof Collection){
					Collection iterValSet = (Collection)iterValObj;
					if (iterValSet.iterator().next() instanceof String){
						Iterator iterValSetIter= iterValSet.iterator();
						while(iterValSetIter.hasNext()){
							iterVal = (String)iterValSetIter.next();
							processAnnoTerm(i, prefix+ iterKey);
							processAnnoTerm(i, prefix+ iterVal );
						}
					}
				}
			}
			
		}
	}
	

	private void parseControlSets() throws Exception
	{
		System.err.println("parsing Control query terms");
		for (int ctrlIndex = 0; ctrlIndex < controlSets.length; ctrlIndex++)
		{
			LocusSet cSet = controlSets[ctrlIndex];
		
			String prefix = addTrackPrefix ? "query" + TRACK_PREFIX_SEPARATOR: "";
			for (int i = 0; i < cSet.getSize(); i++)
			{
				Locus currLocus = cSet.getLocusByIndex(i);
				//processCtrlAnnoTerm(i, prefix+currLocus.getID(), ctrlIndex);
				processCtrlAnnoTerm(i, prefix+currLocus.getChromosome(), ctrlIndex);
				processCtrlAnnoTerm(i, prefix+currLocus.getStrandShortString(), ctrlIndex);
				//processCtrlAnnoTerm(i, prefix+currLocus.getSource(), ctrlIndex);
				processCtrlAnnoTerm(i, prefix+currLocus.getType(), ctrlIndex);
				
				//add remaining annots
				HashMap<String, Object> tgtAnnots = currLocus.getAnnotationClone();
				
				Iterator< String > annoKeys = tgtAnnots.keySet().iterator();
		
				//add all key-value pairs from here
				while( annoKeys.hasNext() ){
					String iterKey = annoKeys.next();
					if (iterKey.equals("setname")){
						continue;
					}
					Object iterValObj = currLocus.getAnnotation(iterKey);
					String iterVal;
					if (iterValObj instanceof String)
					{
						iterVal = (String) iterValObj;
						processCtrlAnnoTerm(i, prefix+ iterKey, ctrlIndex );
						processCtrlAnnoTerm(i, prefix+ iterVal, ctrlIndex );
					}
					if (iterValObj instanceof String[]){
						String[] iterValList = (String[]) iterValObj;
						for (int k = 0; k < iterValList.length; k++){
							iterVal = iterValList[k];	
							processCtrlAnnoTerm(i, prefix+ iterKey , ctrlIndex );
							processCtrlAnnoTerm(i, prefix+ iterVal, ctrlIndex );
						}
					}
					if (iterValObj instanceof Collection){
						Collection iterValSet = (Collection)iterValObj;
						if (iterValSet.iterator().next() instanceof String){
							Iterator iterValSetIter= iterValSet.iterator();
							while(iterValSetIter.hasNext()){
								iterVal = (String)iterValSetIter.next();
								processCtrlAnnoTerm(i, prefix+ iterKey, ctrlIndex );
								processCtrlAnnoTerm(i, prefix+ iterVal, ctrlIndex );
							}
						}
					}
				}
				
			}
		}
	}
	

	


	public void interLocute(List<TableDescriptor> selectedAnnots) throws Exception{
		
		List<Locution> locuParams = new ArrayList<Locution>();
		for(int i = 0; i<selectedAnnots.size(); i++){
			locuParams.add( new Locution() );
		}
		interLocute( selectedAnnots, locuParams );
	}
		
	public void interLocute(List<TableDescriptor> selectedAnnots, List<Locution> locutionParameters) throws Exception{

		if (selectedAnnots.size() == 0){
			throw new Exception("selectedAnnots is EMPTY!");
		}
	
		if (locutionParameters.size() != selectedAnnots.size()){
			throw new Exception("There must be as many Locution[parameter] items as annotation tracks.");
		}
		
		if (parseQueryAnnos){
			parseQuerySet();
			parseControlSets();
		}
				
		flatQSet = LocusSet.flattenSet(qSet);
		System.err.println("Made a flat set of size:"+flatQSet.getSize()+" from qSet of size:"+qSet.getSize()+"!");
		flatControlSets = new LocusSet[controlSets.length];
		for (int i = 0; i < controlSets.length; i++){
			flatControlSets[i] = LocusSet.flattenSet( controlSets[i] );
		}
		
		queriedTracks = new String[selectedAnnots.size()];
		
		for (int annDex=0; annDex < selectedAnnots.size(); annDex++){
			System.err.println("interlocuting track#: "+ annDex);
			queriedTracks[annDex] = selectedAnnots.get(annDex).getFriendlyName();
			this.interLocute(selectedAnnots.get(annDex), locutionParameters.get(annDex) );
		}
		System.err.println("done interlocuting");
		//System.err.println("Done building locutus arrays! annoNames.size(): "+ annoNames.size());
		
		
		return;
	}
	//ideal scenario for efficient crosswise mining
	// given the logic of multi-set intersect operation,
	// efficient way to get all pairwise intersections.
	
	//two set operation
	// trace through starts and ends using a min-count optimization
	// essentially 1-
	private void interLocute( TableDescriptor selectedTrack , Locution locuParams ) throws Exception
	{
		//grab all records in annot as a sequence set
		
		LocusSet filteredQLoci;
		LocusSet flatAnnoLoci;
		
		flatAnnoLoci = hocusLink.getCompleteLoci( selectedTrack );	
		System.err.println("got locuset: "+ flatAnnoLoci.getName());
		flatAnnoLoci = LocusSet.flattenSet(flatAnnoLoci);
		System.err.println("done flattening anno loci");
		flatAnnoLoci = LocusSieve.siftSet( flatAnnoLoci, locuParams.getTargetSieveList() );
		System.err.println("done filtering annoloci");
		if (flatAnnoLoci.getSize() ==0){
			return;
		}

		if (locuParams.getFlattenQuery()){
			filteredQLoci = flatQSet;
		}else{
			filteredQLoci = qSet;
		}
		
		filteredQLoci = LocusSieve.siftSet( flatQSet, locuParams.getQuerySieveList() );
		System.err.println("done filtering flatQLoci");
		if (filteredQLoci.getSize() ==0 ){
			return;
		}
		
		System.err.println("done flattening anno loci size = " + flatAnnoLoci.getSize());
		LIA interLia = new LIA();
		//add to interLia as positive set
		interLia.addLocusSet(flatAnnoLoci, true);
		//add query set
		interLia.addLocusSet(filteredQLoci, true);
		interLia.intersect(locuParams.getComparisonType(), locuParams.getComparisonValue(), locuParams.getComparisonStrand() );
		String prefix = selectedTrack.getTableName();
		System.err.println("processing locus nexus: " + prefix);
		try {
			//count the annotation terms for the intersections
			processLocusNexus( prefix, interLia.getLocusNexus() );
		}	catch (Exception e)		{
			throw new Exception("InterLocutorERROR: Can't processLocusNexus!"+ e.toString());
		}
		System.err.println("removing set: " + qSet.getName());
		//remove the current querySet set so we can process the controls
		interLia.removeLocusSet(filteredQLoci.getName());
		
		//process control sets
		{
			System.err.println("processing controlsets. ");
			LocusSet curCtrlSet;
			for (int ctrlDex = 0; ctrlDex < controlSets.length; ctrlDex++){
				if (locuParams.getFlattenQuery()){
					filteredQLoci = flatControlSets[ctrlDex];
				}else{
					filteredQLoci = controlSets[ctrlDex];
				}
				filteredQLoci = LocusSieve.siftSet( filteredQLoci, locuParams.getQuerySieveList() );
				if (filteredQLoci.getSize() ==0 ){
					continue;
				}
				interLia.addLocusSet(filteredQLoci, true);
				interLia.intersect(locuParams.getComparisonType(), locuParams.getComparisonValue(), locuParams.getComparisonStrand() );
				processControlLocusNexus( prefix, interLia.getLocusNexus() , ctrlDex );
				interLia.removeLocusSet( filteredQLoci.getName() );
			}
		}			
	}
	
	public void processLocusNexus(String prefix, LocusNexus basisNexus ) throws Exception
	{
		System.err.println("within processLocusNexus" + qSet.getName() + "-- numRows: " + basisNexus.numRows());
		prefix = addTrackPrefix? prefix+TRACK_PREFIX_SEPARATOR: "";
		if (basisNexus == null){ return; }
		if(basisNexus.numRows() > 0){
			if(basisNexus.numColumns() !=2){
				//die: basisNexus is not a pair-wise intersection
				throw new Exception("Malformed LocusNexus for Interlocutor!!!");
			}
		}
		
		for ( int i=0; i < basisNexus.numRows(); i++ ){
			Locus tLocus = basisNexus.get(i)[0];
			Locus qLocus = basisNexus.get(i)[1];
			if (qSetSource.equals(tLocus.getSource()))
			{
				qLocus = tLocus;
				tLocus = basisNexus.get(i)[1];
			}
			if (qSetSource.equals(tLocus.getSource()))
			{
				throw( new Exception("InterLocutor: Cannot determine locus from target set at row - " + Integer.toString(i) + "!") );
			}
			Locus qParent = qLocus.getParent();
			if (qParent != null)
			{
				qLocus = qParent;
				//throw( new Exception("InterLocutor: there should be no children!" ) );
			}
			int qPos = qSet.indexOf(qLocus);
			if (qPos < 0){
				throw( new Exception("InterLocutor: Locus is not in qSet but is in flatQSet!"));
			}
			processAnnoTerm(qPos, prefix+tLocus.getID());
			//processAnnoTerm(qPos, prefix+tLocus.getChromosome());
			//processAnnoTerm(qPos, prefix+tLocus.getStrandShortString());
			//processAnnoTerm(qPos, prefix+tLocus.getSource());
			//processAnnoTerm(qPos, prefix+tLocus.getType());
			
			//add remaining annots
			
			Iterator<String> annoKeys = tLocus.getAnnotationKeys();
	
			//add all key-value pairs from here
			while( annoKeys.hasNext() ){
				String iterKey = annoKeys.next();
				if (iterKey.equals("setname")){
					continue;
				}
				Object iterValObj = tLocus.getAnnotation(iterKey);
				String iterVal;
				if (iterValObj instanceof String)
				{
					iterVal = (String) iterValObj;
					processAnnoTerm(qPos, prefix+ iterKey );
					processAnnoTerm(qPos, prefix+ iterVal );
				}
				if (iterValObj instanceof String[]){
					String[] iterValList = (String[]) iterValObj;
					for (int k = 0; k < iterValList.length; k++){
						iterVal = iterValList[k];	
						processAnnoTerm(qPos, prefix+ iterKey);
						processAnnoTerm(qPos, prefix+ iterVal );
					}
				}
				if (iterValObj instanceof Collection){
					Collection iterValSet = (Collection)iterValObj;
					if (iterValSet.iterator().next() instanceof String){
						Iterator iterValSetIter= iterValSet.iterator();
						while(iterValSetIter.hasNext()){
							iterVal = (String)iterValSetIter.next();
							processAnnoTerm(qPos, prefix+ iterKey);
							processAnnoTerm(qPos, prefix+ iterVal );
						}
					}
				}
			}
			
		}
	}
	
	
	public void processControlLocusNexus(String prefix, LocusNexus basisNexus, int ctrlIndex ) throws Exception
	{
		prefix = addTrackPrefix? prefix+TRACK_PREFIX_SEPARATOR: "";
		if(basisNexus.numRows() > 0){
			if(basisNexus.numColumns() !=2){
				//die: basisNexus is not a pair-wise intersection
				throw new Exception("Malformed LocusNexus for Interlocutor!!!");
			}
		}else{
			System.err.println("EMPTY LOCUSNEXUS");
			//die:no entries in nexus
			return;
		}
		
		for ( int i=0; i < basisNexus.numRows(); i++ ){
			Locus tLocus = basisNexus.get(i)[0];
			Locus qLocus = basisNexus.get(i)[1];
			if (qSetSource.equals(tLocus.getSource()))
			{
				qLocus = tLocus;
				tLocus = basisNexus.get(i)[1];
			}
			//if (qSetSource.equals(tLocus.getSource()))
			//{
			//	throw( new Exception("InterLocutor: Cannot determine locus from target set at row - " + Integer.toString(i) + "!") );
			//}
			Locus qParent = qLocus.getParent();
			if (qParent != null)
			{
				qLocus = qParent;
			}
			int qPos = controlSets[ctrlIndex].indexOf(qLocus);
			processCtrlAnnoTerm(qPos, prefix+tLocus.getID(), ctrlIndex);
			//processCtrlAnnoTerm(qPos, prefix+tLocus.getSource(), ctrlIndex);
			//processCtrlAnnoTerm(qPos, prefix+tLocus.getType(), ctrlIndex);
			
			//add remaining annots
			
			Iterator<String> annoKeys = tLocus.getAnnotationKeys();
	
			//add all key-value pairs from here
			while( annoKeys.hasNext() ){
				String iterKey = annoKeys.next();
				if (iterKey.equals("setname")){
					continue;
				}
				Object iterValObj = tLocus.getAnnotation(iterKey);
				String iterVal;
				if (iterValObj instanceof String)
				{
					iterVal = (String) iterValObj;
					processCtrlAnnoTerm(qPos, prefix+ iterKey, ctrlIndex );
					processCtrlAnnoTerm(qPos, prefix+ iterVal, ctrlIndex );
				}
				if (iterValObj instanceof String[]){
					String[] iterValList = (String[]) iterValObj;
					for (int k = 0; k < iterValList.length; k++){
						iterVal = iterValList[k];	
						processCtrlAnnoTerm(qPos, prefix+ iterKey , ctrlIndex );
						processCtrlAnnoTerm(qPos, prefix+ iterVal, ctrlIndex );
					}
				}
				if (iterValObj instanceof Collection){
					Collection iterValSet = (Collection)iterValObj;
					if (iterValSet.iterator().next() instanceof String){
						Iterator iterValSetIter= iterValSet.iterator();
						while(iterValSetIter.hasNext()){
							iterVal = (String)iterValSetIter.next();
							processCtrlAnnoTerm(qPos, prefix+ iterKey, ctrlIndex );
							processCtrlAnnoTerm(qPos, prefix+ iterVal, ctrlIndex );
						}
					}
				}
			}
			
		}
	}
	private void processAnnoTerm( int qPos, String annoTerm ) throws Exception
	{
		if (qPos >= qSet.getSize())
		{
			throw new Exception("Error in processAnnoTerm: qPos("+qPos+" >= qSet.getSize()("+qSet.getSize()+")");
		}
		
		BitVector Locutus;
		if (!(locutus.containsKey(annoTerm)))
		{
			Locutus = new BitVector(qSet.getSize());
			//Locutus.clear(0, qSet.getSize());
			//annoNames.add( annoTerm );
			locutus.put(annoTerm, Locutus);
			{ //this ensures that all words in locutus are mirrored
				//in controlocutus
				BitVector[] ctrlLocutuss = new BitVector[controlSets.length];
				for (int i = 0 ; i < controlSets.length; i++){
					ctrlLocutuss[i] = new BitVector( controlSets[i].getSize() );
					//ctrlLocutuss[i].clear(0, controlSetSizes[i]);
				}
				controlocutus.put(annoTerm, ctrlLocutuss);				
			}
		}else{
			Locutus = (BitVector)(locutus.get(annoTerm));
		}
		
		Locutus.set(qPos);
		
	}
	private void processCtrlAnnoTerm( int qPos, String annoTerm, int ctrlIndex ) throws Exception
	{
		if (qPos >= controlSets[ctrlIndex].getSize())
		{
			throw new Exception("Error in processCtrlAnnoTerm: Pos("+qPos+" >= SetSize("+qSet.getSize()+")");
		}
		
		BitVector[] Locutuss;
		
		//check if controlocutus contains term
		if (controlocutus.containsKey(annoTerm))
		{
			Locutuss = (BitVector[]) controlocutus.get(annoTerm);
			Locutuss[ctrlIndex].set(qPos);
		}
		//since all words have already been ensured to be in controlocutus
		//no worry about them here
		
	}

	
	public Interlocution getResult()
	{
		Interlocution result;

		String[] locusNames = new String[qSet.getSize()];
		for (int i = 0; i < qSet.getSize(); i++){
			locusNames[i]=qSet.getLocusByIndex(i).getID();
		}		

		result = new Interlocution( locusNames, parseQueryAnnos, addTrackPrefix );
		
		result.setQueriedTracks( queriedTracks );
		
		String[][] controlNames = new String[controlSets.length][];
		
		for (int i = 0; i < controlSets.length; i++){
			controlNames[i] = new String[ controlSets[i].getSize() ];
			for ( int j = 0; j < controlSets[i].getSize(); j++){
				controlNames[i][j] = controlSets[i].getLocusByIndex(i).getID();
			}
		}
		
		result.addControlIdentifiers(controlNames, controlIsPopulation);
		
		Iterator<String> annoIter = locutus.keySet().iterator();
		
		while ( annoIter.hasNext() )
		{
			String currAnno = annoIter.next();
			Locutus currLocutus = new Locutus( currAnno, locutus.get(currAnno), controlocutus.get(currAnno) );
			result.setLocutus( currLocutus );
		}
		
		return result;
	}
}
