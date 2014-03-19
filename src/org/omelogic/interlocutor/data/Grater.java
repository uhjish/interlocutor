package org.omelogic.interlocutor.data;

/*
 *      AnnoRow.java
 *      
 *      Copyright 2008 Ajish D. George <ajish@hocuslocus.com>
 *      
 */

import java.io.Serializable;
import java.util.*;
import cern.colt.bitvector.BitVector;

public class Grater implements Serializable
{
	private String anno;
	private DoubleArrayList queryValues;
	private DoubleArrayList[] controlValues;
	private IntArrayList queryLengths;
	private IntArrayList[] controlLengths;
	private IntArrayList queryDistances;
	private IntArrayList[] controlDistances;
	
	public Locutus (String name, DoubleArrayList qVals, DoubleArrayList[] cVals, IntArrayList qLens, IntArrayList[] cLens, IntArrayList qDists, IntArrayList[] cDists)
	{
		this.anno = name;
		this.queryValues = qVals;
		this.controlValues = cVals;
		this.queryLengths = qLens;
		this.controlLengths = cLens;
		this.queryDistances = qDists;
		this.controlDistances = cDists;

	}
	
	public String getName()
	{
		return anno;
	}
	
	public DoubleArrayList getQueryValues()
	{
		return this.queryValues;
	}
	
	public DoubleArrayList[] getControlValues()
	{
		return this.controlValues;
	}
	
	public IntArrayList getQueryLengths()
	{
		return this.queryLengths;
	}
	
	public IntArrayList[] getControlLengths()
	{
		return this.controlLengths;
	}

	public IntArrayList getQueryDistances()
	{
		return this.queryDistances;
	}
	
	public IntArrayList[] getControlDistances()
	{
		return this.controlDistances;
	}
	
}

