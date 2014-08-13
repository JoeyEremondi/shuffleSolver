package nfa;

import algorithms.InclusionAnti;
import algorithms.Options;
import automata.FAState;
import automata.FiniteAutomaton;
import DFA.LabeledNFA;

public class Test {
	
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
			states[i][j] = aut.createState();
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

	
	return aut;
}
	
public static void main(String[] args)
{
	//Default options
	Options.quotient=false;
	Options.C1=false;
	Options.CPT=false;
	Options.EB=false;
	Options.backward=false;
	Options.opt2=true;
	Options.debug=false;
	Options.fplus=false;
	Options.rd=false;
	Options.DFS=false;
	Options.qr=false;
	Options.verbose=false;
	Options.very_verbose=false;
	Options.superpruning=false;
	Options.delayed=false;
	Options.finite=false;
	Options.finite2=false;				
	Options.blamin=false;
	Options.blasub=false;
	Options.blaoffset=0;
	Options.blafixed=1;
	Options.fast=false;
	Options.onlyminimize=false;
	Options.transient_pruning=false;
	Options.jumpsim_quotienting=false;
	Options.jumping_fairsim=false;
	Options.jumping_fairsim_extra=false;
	
	
	//Fastest options
	Options.fast=true;
    Options.backward=true;
    Options.rd=true;
    Options.fplus=true;
    Options.SFS=true;
    Options.qr=true;
    Options.C1=true;
    Options.EB=true;
    Options.CPT=true;
    Options.superpruning=true;
    Options.delayed=true;
    Options.blamin=true;
    Options.blasub=true;
    Options.transient_pruning=true;
    Options.jumpsim_quotienting=true;
	
	
	FiniteAutomaton aut2 = shuffleNFA("aabbaabbaabbaabababbaabb", "aabbaabbaabb");
	FiniteAutomaton aut1 = shuffleNFA("aabbaabbaabbaabbaabbaabb", "aabbaabbaabb");
	
	System.out.println("Starting inclusion test");
	
	
	InclusionAnti inclusion=new InclusionAnti(aut1,aut2);
	
	
	
	inclusion.run();
	if(inclusion.isIncluded()){
		System.out.println("Included");
	}else{
		System.out.println("Not Included");
	}
}

}
