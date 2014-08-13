package nfa;

import DFA.LabeledNFA;
import automata.FAState;

import java.util.*;

import datastructure.Pair;

public class NFAUtils {

	static class OrderedPair extends Pair<Integer, Integer> implements Comparable
	{
		public OrderedPair(int i, int j)
		{
			super(i,j);
		}

		public int compareTo(Object thatObj)
		{
			OrderedPair that = (OrderedPair)thatObj;
			if (this.getLeft() == that.getLeft())
			{
				return this.getRight().compareTo(that.getRight());
			}
			return this.getLeft().compareTo(that.getLeft());
		}
	}

	public static LabeledNFA shuffleNFA(String u, String v)
	{
		LabeledNFA aut = new LabeledNFA();
		int m = u.length();
		int n = v.length();
		FAState[][] states = new FAState[m+1][n+1];
		for (int i = 0; i <= m; i++)
		{
			for (int j = 0; j <= n; j++)
			{
				states[i][j] = aut.createState("(" + i + ", " + j + ")");
			}
		}

		for (int k = 1; k < m + 1; k++)
		{
			for (int l = 0; l < n + 1; l++)
			{
				aut.addTransition(states[k][l], states[k-1][l], "" + u.charAt(m-k));
			}
		}

		for (int k = 0; k < m + 1; k++)
		{
			for (int l = 1; l < n + 1; l++)
			{
				aut.addTransition(states[k][l], states[k][l-1], "" + v.charAt(n-l));
			}
		}

		aut.setInitialState(states[m][n]);
		aut.F.clear();
		aut.F.add(states[0][0]);

		aut.trim();
		return aut;
	}

	public static LabeledNFA productNFA(LabeledNFA m1, LabeledNFA m2)
	{
		LabeledNFA ret = new LabeledNFA();
		TreeSet<String> alphabet = new TreeSet<String>();
		ret.alphabet = alphabet;
		alphabet.addAll(m1.alphabet);
		alphabet.addAll(m2.alphabet);

		TreeMap<Pair<Integer, Integer>, FAState> stateDict = new TreeMap<Pair<Integer, Integer>, FAState>();

		System.err.println("Making product NFA with " + m1.states.size() * m2.states.size() + " states");

		//Add all the paired state
		for (FAState q1 : m1.states)
		{

			for (FAState q2 : m2.states)
			{

				FAState newState = ret.createState("(" + m1.label(q1) + ", " + m2.label(q2) + ")");
				stateDict.put(new OrderedPair(q1.id, q2.id ),  newState);

				//If both final, then add new state to final set
				if (m1.F.contains(q1) && m2.F.contains(q2))
				{
					ret.F.add(newState);
				}

			}



		}




		//Make paired transition for each state we can get to from our two input states
		for (FAState q1 : m1.states)
		{

			for (FAState q2 : m2.states)
			{

				FAState newState = stateDict.get(new OrderedPair(q1.id, q2.id));
				for (String a : ret.alphabet)
				{
					Set<FAState> allNext1 = q1.getNext(a);
					Set<FAState> allNext2 = q2.getNext(a);
					if (allNext1 != null && allNext2 != null)
					{
						for (FAState next1 : allNext1)
						{
							for (FAState next2 : allNext2)
							{
								FAState pairNext = stateDict.get(new OrderedPair(next1.id, next2.id));
								newState.addNext(a, pairNext, ret);
							}
						}
					}




				}
			}


		}

		//initial state is pair of initial states
		OrderedPair initPair = new OrderedPair(m1.getInitialState().id, m2.getInitialState().id);
		ret.setInitialState(stateDict.get(initPair));
		if (ret.getInitialState() == null)
		{
			throw new RuntimeException("Initial state is still null");
		}



		ret.alphabet = alphabet;

		//Remove excess states
		ret.trim();
		return ret;
	}

	public static String strReverse(String s)
	{
		return new StringBuilder(s).reverse().toString();
	}

	//Loop over all index combinations
	public static LabeledNFA shuffleIntersection(String u, String v, String x, String y)
	{
		LabeledNFA ret = new LabeledNFA();
		FAState[][][][] states = new FAState[u.length() + 1][v.length() + 1][x.length() + 1][y.length() + 1];
		int totalLength = u.length() + v.length();
		if (totalLength != x.length() + y.length())
		{
			throw new RuntimeException("Can't make shuffle product for different lengths");
		}

		//First pass: create our states
		for (int dist = 0; dist <= totalLength; dist++)
		{

			for (int uRead = 0;   uRead <= dist; uRead++)
			{

				int vRead = dist - uRead;
				int i = u.length() - uRead;
				int j = v.length() - vRead;

				if (uRead <= u.length() && vRead <= v.length())
				{

					for (int xRead = 0; xRead <= dist; xRead++)
					{
						int yRead = dist - xRead;
						int k = x.length() - xRead;
						int l = y.length() - yRead;

						if (xRead <= x.length() && yRead <= y.length())
						{
							//Now that we have position in word, create a new state for it
							FAState newState = ret.createState("(" + i + ", " + j + ", " + k + ", " + l + ")");
							states[i][j][k][l] = newState;
						}
					}
				}

			}
		}


		//Set initial and final
		ret.setInitialState(states[u.length()][v.length()][x.length()][y.length()]);
		ret.F.add(states[0][0][0][0]);

		//Now add transitions

		for (int dist = 0; dist <= totalLength; dist++)
		{

			for (int uRead = 0;   uRead <= dist; uRead++)
			{

				int vRead = dist - uRead;
				int i = u.length() - uRead;
				int j = v.length() - vRead;

				if (uRead <= u.length() && vRead <= v.length())
				{

					for (int xRead = 0; xRead <= dist; xRead++)
					{
						int yRead = dist - xRead;
						int k = x.length() - xRead;
						int l = y.length() - yRead;

						if (xRead <= x.length() && yRead <= y.length())
						{
							//We have 4 combinations: read ux, read vx, read uy, read vy
							char uc = 0;
							char vc = 0;
							char xc = 0;
							char yc = 0;

							if (i > 0)
							{
								uc = u.charAt(u.length() - i);
							}

							if (j > 0)
							{
								vc = v.charAt(v.length() - j);
							}

							if (k > 0)
							{
								xc = x.charAt(x.length() - k);
							}

							if (l > 0)
							{
								yc = y.charAt(y.length() - l);
							}

							if (uc == xc && uc != 0)
							{
								ret.addTransition(states[i][j][k][l], states[i-1][j][k-1][l], ""+uc);
							}

							if (uc == yc && uc != 0)
							{
								ret.addTransition(states[i][j][k][l], states[i-1][j][k][l-1], ""+uc);
							}

							if (vc == xc && vc != 0)
							{
								ret.addTransition(states[i][j][k][l], states[i][j-1][k-1][l], ""+vc);
							}

							if (vc == yc  && vc != 0)
							{
								ret.addTransition(states[i][j][k][l], states[i][j-1][k][l-1], ""+vc);
							}
						}
					}
				}

			}
		}



		//Finally, calculate reachable states and return;
		ret.trim();
		return ret;
	}
	
	//Create an NFA accepting the union of two shuffle sets
	public static LabeledNFA shuffleUnion(String u, String v, String x, String y)
	{
		LabeledNFA aut = new LabeledNFA();
		int ulen = u.length();
		int vlen = v.length();
		
		//Create shared initial state, and put in proper place of both arrays
		FAState startState = aut.createState("START");
		
		FAState[][] states1 = new FAState[ulen+1][vlen+1];
		states1[ulen][vlen] = startState;
		
		
		
		for (int i = 0; i <= ulen; i++)
		{
			for (int j = 0; j <= vlen; j++)
			{
				if (i + j < ulen + vlen)
				{
				states1[i][j] = aut.createState("A(" + i + ", " + j + ")");
				}
			}
		}
		//Add shuffle transitions
		for (int k = 1; k < ulen + 1; k++)
		{
			for (int l = 0; l < vlen + 1; l++)
			{
				aut.addTransition(states1[k][l], states1[k-1][l], "" + u.charAt(ulen-k));
			}
		}

		
		for (int k = 0; k < ulen + 1; k++)
		{
			for (int l = 1; l < vlen + 1; l++)
			{
				aut.addTransition(states1[k][l], states1[k][l-1], "" + v.charAt(vlen-l));
			}
		}
		
		//Repeat for next shuffle set
		int xlen = x.length();
		int ylen = y.length();
		
		FAState[][] states2 = new FAState[xlen+1][ylen+1];
		states2[xlen][ylen] = startState;
		
		for (int i = 0; i <= xlen; i++)
		{
			for (int j = 0; j <= ylen; j++)
			{
				if (i + j < ulen + vlen)
				{
					states2[i][j] = aut.createState("B(" + i + ", " + j + ")");
				}
			}
		}

		for (int k = 1; k < xlen + 1; k++)
		{
			for (int l = 0; l < ylen + 1; l++)
			{
				aut.addTransition(states2[k][l], states2[k-1][l], "" + x.charAt(xlen-k));
			}
		}

		for (int k = 0; k < xlen + 1; k++)
		{
			for (int l = 1; l < ylen + 1; l++)
			{
				aut.addTransition(states2[k][l], states2[k][l-1], "" + y.charAt(ylen-l));
			}
		}
		
		

		//Add initial states, and both final states
		aut.setInitialState(startState);
		aut.F.clear();
		aut.F.add(states1[0][0]);
		aut.F.add(states2[0][0]);

		//Calculate list of reachable states
		aut.trim();
		
		
		return aut;
	}



}
