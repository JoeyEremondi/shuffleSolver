package DFA;

import java.util.*;

import automata.FAState;
import automata.FiniteAutomaton;

public class LabeledNFA extends FiniteAutomaton
{
	protected HashMap<FAState, String> labels = new  HashMap<FAState, String>();
	protected HashSet<FAState> liveStates = new HashSet<FAState>();
	
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
	
		//Assumes NFA has no loops
		//TODO deal with loops?
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
		
		public boolean isLive(FAState q)
		{
			return this.liveStates.contains(q);
		}
		
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
		
		public  int minWordLength()
		{
			this.trim();
			return -1;
		}
		
		
}
