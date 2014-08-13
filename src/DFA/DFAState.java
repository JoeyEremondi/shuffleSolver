package DFA;

import java.util.*;
import automata.FAState;



/**
 * A module for representing Deterministic Finite Automata as sets of NFA states,
 * lazily computing transitions and other states from a given NFA.
 * 
 * 
 * 
 * This means if you access a linear number of states, at most a quadratic amount of memory will be used
 * since unaccessed states are never generated.
 * Very useful for the shuffle decomposition algorithm, which only accesses a linear number of states.
 *  
 * @author Joey Eremondi
 *
 */
public class DFAState {
	
	 /**
	  * Set of NFA states represented by this DFA state
	  */
	 Set<FAState> superState;
	 /**
	  * The NFA whose determinization this DFA is
	  */
	 LabeledNFA owner;
	
	 /**
	  * Generate a DFA state from a set of NFA states
	  * @param subStates NFA states in this super-state
	  * @param owner the NFA the states belong to
	  */
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
	

	
	/**
	 * @return true if this is an empty super-state, that is, it represents failure in the NFA. False otherwise
	 */
	public boolean isSink()
	{
		return superState.isEmpty();
	}
	
	/**
	 * Test if this state is final in the DFA
	 * @return true if this state contains a final NFA state
	 */
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
	
	/**
	 * Accessor for the NFA this DFA is based off of
	 * @return the NFA whose determinization this state is from
	 */
	public LabeledNFA owner()
	{
		return this.owner;
	}
	
	/**
	 * Get the set of all characters over which the DFA or a state has defined transitions
	 * @return the DFA's alphabet
	 */
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
	
	/**
	 * Print a nice representation of a DFA state
	 */
	public String toString()
	{
		List<String> labels = new LinkedList<String>();
		for (FAState sub : this.superState)
		{
			labels.add(this.owner.label(sub));
		}
		return labels.toString();
	}
	
	//////// Static methods /////////////
	
	/**
	 * Get an arbitrary element of a non-empty set of states
	 * @param s the set of states to access
	 * @return
	 */
	private static FAState first(Set<FAState> s){
		for (FAState q : s)
		{
			return q;
		}
		throw new RuntimeException("No first element in empty list");
	}
	
	/**
	 * The one-step transition function for a DFA
	 * @param q The "current" state
	 * @param c An input character
	 * @return The state reached after reading c from state q
	 */
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
	
	/**
	 * The extended transition function for a DFA
	 * @param q The "current" state
	 * @param s Some string of input to be read
	 * @return The state reached after reading all characters of s starting in state q
	 */
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
	
	/**
	 * Returns true if we can read the given word without failing
	 * @param q the "current" state
	 * @param s The word to be read
	 * @return True if we can read s starting in state q without failing in the current DFA
	 */
	public static boolean dhatDefined(DFAState q, StringBuilder s)
	{
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
	

	
	/**
	 * 
	 * @param q The "current" state
	 * @return The set of all letters we can read from q without failing
	 */
	public static List<Character> deltaSet(DFAState q)
	{
		if (q.superState.isEmpty())
		{
			return new LinkedList<Character>();
		}
		else
		{
			
			FAState firstState = first(q.superState);
			//LabeledNFA aut = (LabeledNFA) firstState.getowner();
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
	
	/**
	 * @param q The "current" state
	 * @return an arbitrary member of deltaSet(q)
	 */
	public static char firstDeltaSet(DFAState q)
	{
		FAState firstState = first(q.superState);
		Set<String> alphabetStr = firstState.getowner().getAllTransitionSymbols();
		Set<Character> alphabet = new TreeSet<Character>();
		for (String s : alphabetStr)
		{
			alphabet.add(s.charAt(0));
		}
		
		
		//LinkedList<Character> accum = new LinkedList<Character>();
		for (char a : alphabet)
		{
			if (deltaDefined(q,a))
			{
				return a;
			}
		}
		return 0;
	}
	
	/**
	 * Returns true if we can read the given letter without failing
	 * @param q the "current" state
	 * @param c The word to be read
	 * @return True if we can read c starting in state q without failing in the current DFA
	 */
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

	/**
	 * Given an NFA with labeled states, return the start state of a DFA accepting the same language
	 * @param aut The NFA to determinize
	 * @return The determinization of aut
	 */
	public static DFAState initialDFAState(LabeledNFA aut)
	{
		//System.err.println("Making DFA with init state " + aut.getInitialState()); 
		Set<FAState> init = new TreeSet<FAState>();
		init.add(aut.getInitialState());
		return new DFAState(init, aut);
	}
	
	/**
	 * Print all states and transitions of the determinization of an NFA
	 * Runs forever on cyclic NFA's
	 * @param aut An acyclic NFA to print
	 */
	public static void printAcyclic(LabeledNFA aut)
	{
		DFAState q = initialDFAState(aut);
		printStateRecursive(q);
	}
	
	/**
	 * Print a state's information, then recursively print the information of all states it can access
	 * @param q The first state whose information we print
	 */
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
	


}
