package nfa;

import java.util.*;
import java.lang.Math;

import DFA.DFAState;
import DFA.LabeledNFA;
import automata.FiniteAutomaton;



public class ShuffleDecomp {
	
	LabeledNFA aut;
	private String uString = null;
	private String vString = null;
	
	public static void printDebug(String s)
	{
		//System.err.println(s);
	}
	
	public String u()
	{
		return uString;
	}
	
	public String v()
	{
		return vString;
	}
	
	public ShuffleDecomp(LabeledNFA inAut) throws NotDecomposableException
	{
		aut = inAut;
		DFAState q0 = DFAState.initialDFAState(aut);
		
		StepInfo x = new StepInfo("", "", q0, empty(), empty(), empty());
		
		if (deltaSet(x.q).size() == 0)
		{
			throw new NotDecomposableException("Automata can't go anywhere from initial state");
		}
		
		if (deltaSet(x.q).size() == 1)
		{
			x = firstDiff(x);
			//printDebug("First diff returned f=" + x.f + ", g=" + x.g + ", q=" + x.q + ", u=" + x.u + ", v=" + x.v);
		}
		else
		{
			x.f = "" + deltaSet(x.q).get(0);
		}
		
		while (deltaSet(x.q).size() == 2 && (x.f+x.g) != "")
		{
			//printDebug("In main loop");
			x = firstDiff(x);
			//printDebug("First diff returned f=" + x.f + ", g=" + x.g + ", q=" + x.q + ", u=" + x.u + ", v=" + x.v);
		}
		if (deltaSet(x.q).isEmpty())
		{
			uString = x.u.toString();
			vString = x.v.toString();
			//help gc
			this.aut = null;
			q0 = null;
			x.q = null;
		}
		else{
			//help gc
			this.aut = null;
			q0 = null;
			x.q = null;
			//printDebug("u=" + x.u + ", v=" + x.v);
			throw new NotDecomposableException("Not a shuffle aut, ctor");
			
		}
	}
	
	private static StringBuilder empty()
	{
		return new StringBuilder("");
	}
	
	
	private DFAState delta(DFAState q, char c)
	{
		return DFAState.delta(q,c);
	}
	
	private DFAState dhat(DFAState q, StringBuilder s)
	{
		return DFAState.dhat(q, s);
	}
	
	private boolean dhatDefined(DFAState q, StringBuilder s)
	{
		return DFAState.dhatDefined(q, s);
	}
	
	private List deltaSet(DFAState q){
		return DFAState.deltaSet(q);
	}
	
	private boolean deltaDefined(DFAState q, char c)
	{
		return  DFAState.deltaDefined(q,c);
	}
	
	private char deltaSetFirst(DFAState q)
	{
		return DFAState.firstDeltaSet(q);
	}
	
	private int dmax(DFAState q, String s) throws NotDecomposableException{
		if (s.isEmpty())
		{
			return 0;
		}
		else if (s.length() == 1)
		{
			return dmax(q, s.charAt(0));
		}
		throw new NotDecomposableException("No dhat for non-empty string");
	}
	
	private int dmax(DFAState q, char a){

		int ret = 0;
		while (dhatDefined(q, spow(a, ret + 1)) )
		{
			////printDebug("In dmax loop " + dhat(q, spow(a, ret + 1)));
			ret += 1;
		}
		//printDebug("Returning from dmax");
		return ret;
	}
	
	public static StringBuilder spow(StringBuilder s, int n)
	{
		StringBuilder ret = empty();
		for (int i = 0; i < n; i++)
		{
			ret.append(s);
		}
		return ret;
	}
	
	public static StringBuilder spow(String s, int n)
	{
		StringBuilder ret = empty();
		for (int i = 0; i < n; i++)
		{
			ret.append(s);
		}
		return ret;
	}
	
	public static StringBuilder spow(char c, int n)
	{
		StringBuilder ret = empty();
		for (int i = 0; i < n; i++)
		{
			ret.append(c);
		}
		return ret;
	}
	
	
	//TODO rewrite not with infinite loops
	private int dmin(DFAState q, char a)
	{
		int ret = 1;
		Set<Character> dset = new TreeSet<Character>();
		for (char b: q.alphabet())
		{
			if (b != a)
			{
				dset.add(b);
			}
		}

		while(true){
			boolean atFinal = dhat(q, spow(a,ret)).isFinal();
			boolean defined = false;
			for (char b : dset)
			{
				defined = defined || dhatDefined(q, spow(a, ret).append(b));
				//TODO return or side effect? check
			}
			if (defined || atFinal ) 
			{
				return ret;
			}
			ret += 1;
		}
	}
	
	private StringBuilder wq(StringBuilder w, DFAState q)
	{
		if (w.length() == 0 || ! deltaSet(q).contains(w.charAt(w.length()-1)) )
		{
			return w;
		}
		//remove last block of a's in w and return
		List<StringBuilder> blockList = blocks(w);
		StringBuilder last = blockList.get(blockList.size() - 1);
		return new StringBuilder(w.subSequence(0, w.length() - (1 + last.length())));
		
		//TODO what if undefined?
	}
		
	private List<StringBuilder> blocks(StringBuilder s)
	{
		    if (s.length() == 0)
		    {
		        return new LinkedList<StringBuilder>();
		    }
		    
		    int i = 0;
		    LinkedList<StringBuilder> ret = new LinkedList<StringBuilder>();
		    while (i < s.length())
		    {
		        StringBuilder accum = empty(); //TODO make faster?
		        char current = s.charAt(i);
		        while (i < s.length() && s.charAt(i) == current)
		        {
		            accum.append(s.charAt(i));
		            i += 1;
		        }
		        ret.addLast(accum);
		    }
		    return ret;
		
	}
	
	private String skeleton(String s){
	    if (s.isEmpty())
	    {
	        return "";
	    }
	    
	    int i = 0;
	    String ret = "";
	    while (i < s.length())
	    {
	        String accum = "";
	        char current = s.charAt(i);
	        while (i < s.length() && s.charAt(i) == current)
	        {
	            accum += s.charAt(i);
	            i += 1;
	        }
	        ret += current;
	    }
	    return ret;
	}
	
	//When we're at the end of shuffle, combine blocks into the "maximum" word
	private StringBuilder maxShuf(StringBuilder s, StringBuilder t)
	{
		List<StringBuilder> sblocks, tblocks;
		sblocks = blocks(s);
		tblocks = blocks(t);
		StringBuilder ret = empty();
		int k = 0;
		int l=0;
		int n = sblocks.size(), m=tblocks.size();
		while (k < n || l < m)
		{
			if (k < n && l < m && sblocks.get(k).charAt(0) == tblocks.get(l).charAt(0))
			{
				ret.append( sblocks.get(k) );
				ret.append(tblocks.get(l));
				k++;
				l++;
			}
			else if(k < n)
			{
				ret.append(sblocks.get(k));
				k++;
			}
			else
			{
				ret.append(tblocks.get(l));
				l++;
			}
		}
		return ret;
	}
	
	StringBuilder findUnique(DFAState qArg) throws NotDecomposableException
	{
		DFAState qq = qArg;
		StringBuilder z = empty();
		
		while (!qq.isFinal())
		{
			
			if (deltaSet(qq).size() == 2 || deltaSet(qq).isEmpty())
			{
				throw new NotDecomposableException("Not shuffle decomposable");
			}
			char a = (Character)(deltaSet(qq).get(0)); //TODO cast valid?
			z.append(a);
						
			qq = delta(qq,a);
						
			
		}
		return z;
	}
	
	class StepInfo
	{
		public String f;
		public String g;
		public DFAState q;
		public StringBuilder u;
		public StringBuilder v;
		public StringBuilder w;
		
		public StepInfo(String f, String g, DFAState q, StringBuilder u, StringBuilder v, StringBuilder w)
		{
			this.f = f;
			this.g = g;
			this.q = q;
			this.u = u;
			this.v = v;
			this.w = w;
		}
		
	}
	
	class MaxInfo
	{
		public StringBuilder s;
		public StringBuilder t;
		public StringBuilder z;
		public DFAState qprev;
		public DFAState qq;
		char c;
		public int eps;
		public int b;
		
		public MaxInfo(StringBuilder s, StringBuilder t, StringBuilder z, DFAState qprev, DFAState qq, char c, int eps, int b)
		{
			this.s = s;
			this.t = t;
			this.z = z;
			this.qprev = qprev;
			this.qq = qq;
			this.c = c;
			this.eps = eps;
			this.b = b;
		}
		
	}
	
	
	private MaxInfo maxEqual(DFAState qprevArg, DFAState qqArg)
	{
		StringBuilder s = empty();
		StringBuilder t = empty();
		StringBuilder z= empty();
		char c = 0;
		int b = -99;
		int eps = -101;
		DFAState qprev = qprevArg;
		DFAState qq = qqArg;
		while (deltaSet(qq).size() == 1)
		{
			//printDebug("In maxEqual loop");
			c = deltaSetFirst(qq);
			b = dmax(qq, c);
			eps = dmin(qq, c);
			if (2*eps == b)
			{
				//printDebug("Had 2e = b " + eps + " " + b);
				s.append(spow(c,eps));
				t.append(spow(c,eps));
				z.append( spow(c,b) );
				qprev = dhat(qq, spow(c, b-1));
				qq = dhat(qq, spow(c,b));
				
				
				//printDebug((""+qq));
				//printDebug(""+deltaSet(qq));
				
				
			}
			else{
				return new MaxInfo(s,t,z,qprev, qq, c, eps, b);
			}
		}
		
		return new MaxInfo(s,t,z,qprev, qq, c, eps, b);
	}
	
	private StepInfo firstDiff(StepInfo step) throws NotDecomposableException
	{
		String f = step.f;
		String g = step.g;
		DFAState q = step.q;
		StringBuilder u = step.u;
		StringBuilder v = step.v;
		StringBuilder w = step.w;
		
		//printDebug("In first diff f=" + f + ", g=" + g + ", q=" + q + ", u=" + u + ", v=" + v);
		//printDebug("w = " + w);
		if (g.isEmpty())
		{
			int gamma = dmax(q,f);
			DFAState qprev = dhat(q, spow(f, gamma-1));
			DFAState qq = dhat(q, spow(f,gamma));
			StringBuilder s = empty();
			StringBuilder t = empty();
			StringBuilder z = empty();
			
			//printDebug("qprev=" + qprev + ", q' =" + qq + ", gamma=" + gamma);
			
			//printDebug("delta set q'" + deltaSet(qq));
			
			//Case 1
			if (deltaSet(qq).size() == 2)
			{
				List<Character> diff = new LinkedList(deltaSet(qq));
				diff.removeAll(deltaSet(q));
				char d;
				if (diff.isEmpty())
				{
					List<Character> candidates = deltaSet(qq);
					//TODO how to get last character?
					if (u.length() > 0){
						candidates.remove(u.substring(u.length()-1).charAt(0));
					}
					try
					{
						d = candidates.get(0);
					} catch (Exception e)
					{
						throw new NotDecomposableException("No candidate letters, case 1");
					}
					
				}
				else
				{
					d = diff.get(0);
				}
				u.append(spow(f,gamma));
				q = dhat(q, spow(f,gamma));
				w.append(spow(f,gamma));
				return new StepInfo("" + d,"", q, u, v, w);
			}
			else if (deltaSet(qq).size() == 1)
			{
				MaxInfo m = maxEqual(qprev, qq);
				s = m.s;
				t = m.t;
				z = m.z;
				qprev = m.qprev;
				qq = m.qq;
				char c = m.c;
				int eps = m.eps;
				int b = m.b;
				
				//printDebug("MaxEqual returned");
				//printDebug("qprev=" + qprev + ", q' =" + qq + ", gamma=" + gamma);
				//printDebug("c=" + c + ", eps =" + eps + ", b=" + b);
				//printDebug("s=" + s + ", t =" + t);
				//Case 2
				if (deltaSet(qq).size() == 2)
				{
					//printDebug("In case 2");
					String d,e;
					d = "" + deltaSet(qq).get(0);
					e = "" + deltaSet(qq).get(1);
					
					if (dhatDefined(q,maxShuf(empty().append(s).append(e), spow(f,gamma).append(t))))
					{
						u.append(spow(f,gamma).append(s));
						v.append(t);
						q = dhat(q, spow(f,gamma).append(z));
						w.append(spow(f,gamma)).append(z);
						return new StepInfo(d, "", q, u, v, w);
					}
					else
					{
						u.append(spow(f, gamma).append(t));
						v.append(s);
						q = dhat(q, spow(f,gamma).append(z));
						w.append(spow(f,gamma)).append(z);
						return new StepInfo("", d, q, u, v, w);
					}
				}
				else if (eps == b) //Case 3
				{
					//printDebug("In case 3");
					int a = dmax(qprev, c);
					if (a == b)
					{
						//printDebug("Found unique word" + findUnique(qq));
						s.append(findUnique(qq));
						z.append(findUnique(qq));
					}
					else
					{
						//printDebug("adding max and min");
						s.append(spow(c, Math.max(a, b-a)));
						t.append(spow(c, Math.min(a, b-a)));
						z.append(spow(c,b));
					}
					if (dhatDefined(q, maxShuf(s, spow(f,gamma).append(t))))
					{
						u.append(spow(f,gamma).append(t));
						v.append(s);
						q = dhat(q, maxShuf(s, spow(f,gamma).append(z)));
						w.append(spow(f,gamma)).append(z);
						return new StepInfo("", "", q, u, v, w);
					}
					else
					{
						u.append(spow(f, gamma).append(s));
						v.append(t);
						q = dhat(q, maxShuf(s, spow(f,gamma).append(z)));
						w.append(spow(f,gamma)).append(z);
						return new StepInfo("", "", q, u, v, w);
					}
				}
				else if (eps != b)//Case 4
				{
					if (2*eps > b) //Case 4a
					{
						//printDebug("In case 4a");
						qq = dhat(qq, spow(c,b));
						StringBuilder r = findUnique(qq);
						//printDebug("Found unique word" + findUnique(qq));
						if (dhatDefined(q, maxShuf(empty().append(s).append(spow(c,eps)), spow(f,gamma).append(t))))
						{
							u.append(spow(f,gamma).append(s).append(spow(c,b-eps)));
							
							v.append(t);
							v.append(spow(c,eps).append(r));
							
						}
						else
						{
							 //u.append(spow(f,gamma).append(spow(c,eps)).append(r)); //TODO put back?
							u.append(spow(f,gamma).append(s).append(spow(c,eps)).append(r));
							 
							 
							 v.append(t);
							 //v.append(s);
							 v.append(spow(c, b-eps)); //TODO fix
							 
						}
						w.append(spow(f,gamma)).append(z).append(spow(c,b)).append(r);
						q = dhat(q, spow(f,gamma).append(z).append(spow(c,b)).append(r));
						return new StepInfo("", "", q, u, v, w);
					}
					else //case 4b
					{
						//printDebug("In case 4b");
						List<Character> candidates = new LinkedList<Character>();
						for (char x : (List<Character>)deltaSet(dhat(qq, spow(c,eps))))
						{
							if (x != c)
							{
								candidates.add(x);
							}
						}
						
						//TODO is this right?
						char d;
						try{
							d = candidates.get(0);
						}
						catch (Exception e)
						{
							throw new NotDecomposableException("No candidate letters firstDiff 4b");
						}
						if (dhatDefined(q, maxShuf(empty().append(s).append(spow(c, eps + 1)), spow(f,gamma).append(t) )))
						{
							u.append(spow(f,gamma).append(s).append(spow(c,eps)));
							v.append(t);
							q = dhat(q, spow(f,gamma).append(z).append(spow(c,eps)));
							w.append(spow(f,gamma)).append(z).append(spow(c,eps));
							return new StepInfo(""+d, "", q, u, v, w);
						}
						else
						{
							u.append(spow(f,gamma).append(t));
							v.append(s);
							v.append(spow(c,eps));
							q = dhat(q, spow(f,gamma).append(z).append(spow(c,eps)));
							w.append(spow(f,gamma)).append(z).append(spow(c,eps));
							return new StepInfo("", ""+d, q, u, v, w);
						}
						
					}
				}
			}
			else
			{
				throw new NotDecomposableException("Not decomposable firstDiff0");
			}
		}
		else if (f.isEmpty())
		{
			//printDebug("!!!NOTE: flipping in firstDiff");
			StepInfo flipStep = new StepInfo(g,f,q,v,u, w);
			StepInfo next = firstDiff(flipStep);
			return new StepInfo(next.g, next.f, next.q, next.v, next.u, next.w);
		}
		
		throw new NotDecomposableException("Not decomposable firstDiff1");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
