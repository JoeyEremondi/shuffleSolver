package DFA;

import java.util.*;
import java.lang.Math;



import automata.FAState;
import automata.FiniteAutomaton;




public class DFAState {
	
	 Set<FAState> superState;
	 LabeledNFA owner;
	
	protected DFAState(Set<FAState> subStates, LabeledNFA owner)
	{
		//System.err.println("Making super state " + subStates);
		for (FAState q : subStates)
		{
			if (q == null)
			{
				throw new RuntimeException("Null state in sub list");
			}
		}
		this.superState = new TreeSet<FAState>(subStates);
		this.owner = owner;
	}
	
	private static FAState first(Set<FAState> s){
		for (FAState q : s)
		{
			return q;
		}
		throw new RuntimeException("No first element in empty list");
	}
	
	public boolean isSink()
	{
		return superState.isEmpty();
	}
	
	public boolean isFinal()
	{
		for (FAState q : this.superState)
		{
			if (q.getowner().F.contains(q))
			{
				return true;
			}
		}
		return false;
	}
	
	public LabeledNFA owner()
	{
		return this.owner;
	}
	
	public static DFAState delta(DFAState q, char c)
	{

		Set<FAState> accum = new TreeSet<FAState>();
		//TODO: treat null as dead-state?
		if (q != null)
		{
			for (FAState subState : q.superState)
			{
				
				if (subState.getNext(""+c) != null && q.owner().isLive(subState))
				{
					accum.addAll(subState.getNext(""+c));
				}
			}
		}
		
		
		return new DFAState(accum, q.owner());
	}
	
	public static DFAState dhat(DFAState q, StringBuilder s)
	{
		DFAState ret = q;
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			ret = delta(ret, c);
		}
		return ret;
	}
	
	public static boolean dhatDefined(DFAState q, StringBuilder s)
	{
		boolean isDefined = true;
		DFAState current = q;
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (!deltaDefined(current,  c))
			{
				return false;
			}
			current = delta(current, c);
		}
		
		return true;
	}
	
	public Set<Character> alphabet()
	{
		FAState firstState = first(this.superState);
		Set<String> alphabetStr = firstState.getowner().getAllTransitionSymbols();
		Set<Character> alphabet = new TreeSet<Character>();
		for (String s : alphabetStr)
		{
			alphabet.add(s.charAt(0));
		}
		return alphabet;
	}
	
	public static List<Character> deltaSet(DFAState q)
	{
		if (q.superState.isEmpty())
		{
			return new LinkedList<Character>();
		}
		else
		{
			
			FAState firstState = first(q.superState);
			LabeledNFA aut = (LabeledNFA) firstState.getowner();
			//System.err.println("First state " + aut.label(firstState));
			Set<String> alphabetStr = firstState.getowner().getAllTransitionSymbols();
			Set<Character> alphabet = new TreeSet<Character>();
			for (String s : alphabetStr)
			{
				alphabet.add(s.charAt(0));
			}
			
			
			LinkedList<Character> accum = new LinkedList<Character>();
			for (char a : alphabet)
			{
				if (deltaDefined(q,a))
				{
					accum.add(a);
				}
			}
			return accum;
		}
	}
	
	public static char firstDeltaSet(DFAState q)
	{
		FAState firstState = first(q.superState);
		Set<String> alphabetStr = firstState.getowner().getAllTransitionSymbols();
		Set<Character> alphabet = new TreeSet<Character>();
		for (String s : alphabetStr)
		{
			alphabet.add(s.charAt(0));
		}
		
		
		LinkedList<Character> accum = new LinkedList<Character>();
		for (char a : alphabet)
		{
			if (deltaDefined(q,a))
			{
				return a;
			}
		}
		return 0;
	}
	
	public static boolean deltaDefined(DFAState q,char c)
	{
		if (q.isSink())
		{
			return false;
		}
		boolean foundState = false;
		
		for (FAState subState : q.superState)
		{
			if (subState != null && subState.getNext(""+c) != null)
			{
				boolean notSink = false;
				for (FAState next : subState.getNext())
				{
					if (next != null && q.owner.isLive(next)){
						notSink = true;
					}
				}
				foundState = foundState || notSink ;
			}
			
			
		}
		
		return foundState;
	}

	
	public static DFAState initialDFAState(LabeledNFA aut)
	{
		//System.err.println("Making DFA with init state " + aut.getInitialState()); 
		Set<FAState> init = new TreeSet<FAState>();
		init.add(aut.getInitialState());
		return new DFAState(init, aut);
	}
	
	public static void printAcyclic(LabeledNFA aut)
	{
		DFAState q = initialDFAState(aut);
		printStateRecursive(q);
	}
	
	private static void printStateRecursive(DFAState q)
	{
		if (q.isSink())
		{
			System.out.println("SINK");
		}
		else
		{
			System.out.println("State set: " + q);
			System.out.println("Delta set: " + deltaSet(q));
			
			for (char a : deltaSet(q))
			{
				printStateRecursive(delta(q, a));

			}
			
			
		}
	}
	
	public String toString()
	{
		List<String> labels = new LinkedList<String>();
		for (FAState sub : this.superState)
		{
			labels.add(this.owner.label(sub));
		}
		return labels.toString();
	}

}
