package com.subhadeep.messiahlayer;

import java.util.StringTokenizer;

public class Tokenizer {
	
	private String str;
	private final String DELIM = "#";
	
	public Tokenizer()
	{
		str = null;
	}
	
	public Tokenizer(String str)
	{
		this.str = str;
	}
	
	public int countTokens()
	{
		int n = 0;
		if(str != null)
		{
			StringTokenizer stok = new StringTokenizer(str, DELIM);
			n = stok.countTokens();
			stok = null;
		}
		return n;
	}
	
	public String[] getTokens()
	{
		int n = countTokens();
		if(n == 0)
			return null;
		String tokens[] = new String[n];
		StringTokenizer stok = new StringTokenizer(str, DELIM);
		for(int i = 0; i < n; i++)
		{
			tokens[i] = stok.nextToken();			
		}
		stok = null;
		return tokens;
	}
	
	public String addToken(String str)
	{
		if(this.str == null)
			this.str = new String();
		this.str += str + DELIM;
		return this.str;
	}
	
	public String deleteToken()
	{
		//deletion is done form the beginning of string
		if(this.str == null)
			return null;
		String result = "";
		StringTokenizer stok = new StringTokenizer(this.str, DELIM);
		stok.nextToken(); //skipping one token
		while(stok.hasMoreElements())		
			result += stok.nextToken() + DELIM;	
		stok = null;
		return result;
	}
	
	public String getString()
	{
		return str;
	}
	
}
