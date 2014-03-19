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

public class Locutus implements Serializable
{
	private String anno;
	private BitVector queryBits;
	private BitVector[] controlBits;
	
	public Locutus (String name, BitVector query, BitVector[] ctrls)
	{
		this.anno = name;
		this.queryBits = query;
		this.controlBits = ctrls;
	}
	
	public String getName()
	{
		return anno;
	}
	
	public BitVector getQueryBits()
	{
		return this.queryBits;
	}
	
	public BitVector[] getControlBits()
	{
		return this.controlBits;
	}
	
	
}

