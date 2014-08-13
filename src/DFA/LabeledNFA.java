package DFA;

import java.util.*;

import automata.FAState;
import automata.FiniteAutomaton;

/**
 * An extension of the RABIT NFA class which requires each state to have a label
 * Makes for nice pretty-printing of automata, and easier debugging
 * 
 * Also adds some functionality, such as finding the set of reachable states
 * 
 * @author Joey Eremondi
 *
 */
public class LabeledNFA extends FiniteAutomaton
{
	//The mapping of states to labels
	protected HashMap<FAState, String> labels = new  HashMap<FAState, String>();
	//The set of all accessible states
	protected HashSet<FAState> liveStates = new HashSet<FAState>();

	/**
	 * Create a state and add its label to the NFA's label table
	 * @param s The state's label
	 * @return A new state of this NFA
	 */
	public FAState createState(String s) {
		FAState ret = super.createState();
		labels.put(ret, s);
		return ret;
	}

	@Override
	public FAState createState()
	{
		throw new RuntimeException("Can't create state without label");
	}

	/**
	 * Return the label of a given state, just straightforward lookup
	 * @param q The state to look up
	 * @return The state's label
	 */
	public String label(FAState q)
	{
		return labels.get(q);
	}

	@Override
	public String toString() {
		String result="\n";
		Iterator<FAState> st_it=states.iterator();
		while(st_it.hasNext()){
			FAState st=st_it.next();
			Iterator<String> label_it=st.nextIt();
			while(label_it.hasNext()){
				String label=label_it.next();
				Iterator<FAState> to_it=st.getNext(label).iterator();
				while(to_it.hasNext()){
					FAState to=to_it.next();
					result+=(this.label(st)+" --"+label+"-->"+this.label(to)+"\n");
				}
			}
		}
		result+="\nInit:"+label(this.getInitialState());
		result+="\nACC:"+F+"\n";
		return result;
	}


	/**
	 * Assuming this NFA is acyclic, calculate the set of all states which are reachable when reading some word.
	 */
	public void trim()
	{
		Set<FAState> reachable = new HashSet<FAState>();
		dfs(this.getInitialState(), reachable);

		Set<FAState> canReachEnd = new HashSet<FAState>();
		for (FAState f : this.F)
		{
			reverseDfs(f, canReachEnd);
		}

		//Add all valid states to our final list
		this.liveStates.clear();
		for (FAState q : this.states)
		{
			if (reachable.contains(q) && canReachEnd.contains(q))
			{
				this.liveStates.add(q);
			}
		}


	}

	/**
	 * Query whether a state is live or not. Must be called after trim() has been run.
	 * @param q The state to query
	 * @return true if it is reachable, false otherwise
	 */
	public boolean isLive(FAState q)
	{
		return this.liveStates.contains(q);
	}

	/**
	 * Find reachable states using recursive DFS
	 * @param q The state to start DFS at
	 * @param seen The set of all states already reached by DFS
	 */
	private void dfs(FAState q, Set<FAState> seen)
	{
		seen.add(q);
		for (FAState next : q.getNext())
		{
			if (!seen.contains(next))
			{
				dfs(next, seen);
			}
		}
	}

	/**
	 * Perform a backwards DFS, seeing which states can reach some final state
	 * @param q The state to start at
	 * @param seen the states already seen
	 */
	private void reverseDfs(FAState q, Set<FAState> seen)
	{
		seen.add(q);
		for (FAState prev : q.getPre())
		{
			if (!seen.contains(prev))
			{
				reverseDfs(prev, seen);
			}
		}
	}




}
