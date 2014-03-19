/*
 *      Locution.java
 *      
 *      Copyright 2008 Ajish D. George <ajish@hocuslocus.com>
 *      
 */
package org.omelogic.interlocutor;

import org.omelogic.locus.*;
import org.omelogic.locus.filter.*;
import org.omelogic.hldbmanager.TableDescriptor;

import java.util.*;
import java.io.Serializable;

public class Locution implements java.io.Serializable
{
	//locution: word/phrase used by a particular group
	//			in a particular situation
	//holds the parameters for a particular intersection
	//in either of the interLocutor tools
	//parameters include:
	// 1] query set filters
	// 2] target set filters
	// 3] intersection parameters
	//This allows fine-grained track by track parametrization
	//while still allowing the en masse approach to hypothesis mining.
	
	private List<LocusSieve> querySieveList;
	private List<LocusSieve> targetSieveList;
	
	private int comparisonType;
	private float comparisonValue;
	private int comparisonStrand;	
	
	private boolean flattenQuery;
	
	public Locution()	{

		querySieveList = new ArrayList<LocusSieve>();
		targetSieveList = new ArrayList<LocusSieve>();

		//default LIA parameters
		comparisonType = Locus.COMPARISON_TYPE.PERCENT;
		comparisonValue = 1;
		comparisonStrand = Locus.COMPARISON_STRAND.MATCH_PERMISSIVE;	
	}
	
	public void setFlattenQuery( boolean flatten ){
		flattenQuery = flatten;
	}
	
	public boolean getFlattenQuery( ){
		return flattenQuery;
	}
	
	public void setComparisonType( int type )	{
		comparisonType = type;
	}
	
	public void setComparisonValue( float value )	{
		comparisonValue = value;
	}
	
	public void setComparisonStrand( int compstrand )	{
		comparisonStrand = compstrand;
	}
	
	public void addQuerySieve( LocusSieve qSieve )	{
		querySieveList.add(qSieve);
	}
	
	public void addTargetSieve( LocusSieve tSieve )	{
		targetSieveList.add(tSieve);
	}
	
	public void addQuerySieveList( List<LocusSieve> qSieve )	{
		querySieveList.addAll(qSieve);
	}
	
	public void addTargetSieveList( List<LocusSieve> tSieve )	{
		targetSieveList.addAll(tSieve);
	}
	
	public void setQuerySieveList( List<LocusSieve> qSieve )	{
		querySieveList = (qSieve);
	}
	
	public void setTargetSieveList( List<LocusSieve> tSieve )	{
		targetSieveList = (tSieve);
	}

	public int getComparisonType(){
		return comparisonType;
	}
	
	public float getComparisonValue(){
		return comparisonValue;
	}
	
	public int getComparisonStrand(){
		return comparisonStrand;
	}
	
	public List<LocusSieve> getQuerySieveList(){
		return querySieveList;
	}
	
	public List<LocusSieve> getTargetSieveList(){
		return targetSieveList;
	}


}
