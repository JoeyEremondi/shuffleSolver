package nfa;


import algorithms.InclusionAnti;
import algorithms.InclusionOptBVLayered;
import algorithms.Minimization;
import algorithms.Options;
//import algorithms.Options;
import automata.AutomatonPreprocessingResult;
import automata.FiniteAutomaton;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Iterator;

import nfa.Test2.DirectsimThread;
import nfa.Test2.FairsimThread;
import algorithms.InclusionAnti;
import algorithms.InclusionOptBVLayered;
import algorithms.Minimization;
import automata.AutomatonPreprocessingResult;
import automata.FiniteAutomaton;
import automata.FAState;

public class SimAntiScratch {

	// Transform finite automata into BA s.t. language inclusion is preserved.
	private static FiniteAutomaton toBA(FiniteAutomaton aut){
		if(aut.alphabet.contains("specialaction359")){
			System.out.println("Error: Alphabet name clash. Exiting.");
			System.exit(0);
		}
		FAState acc = aut.createState();
		Iterator<FAState> it=aut.F.iterator();
		while(it.hasNext()){
			FAState state=it.next();
			aut.addTransition(state, acc, "specialaction359");
		}
		aut.addTransition(acc, acc, "specialaction359");
		aut.F.clear();
		aut.F.add(acc);
		return aut;
	}

	public static boolean isSubset(String[] args, FiniteAutomaton aut1, FiniteAutomaton aut2) {
		boolean antichain=false;

		long ttime1;
		//FiniteAutomaton aut1 = new FiniteAutomaton(args[0]);
		aut1.name=args[0];
		//FiniteAutomaton aut2 = new FiniteAutomaton(args[1]);
		aut2.name=args[1];

		
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

		//Set options for finite and fastest

		//if(args[i].compareTo("-finite")==0){
		Options.finite=true;
		//}
		//if(args[i].compareTo("-fast")==0){
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
		//}



		// Display sizes of input automata
		System.out.println("Aut A: # of Trans. "+aut1.trans+", # of States "+aut1.states.size()+".");
		System.out.println("Aut B: # of Trans. "+aut2.trans+", # of States "+aut2.states.size()+".");

		//if(Options.finite){
		System.out.println("Solving finite automata inclusion directly.");
		if(aut1.F.contains(aut1.getInitialState()) && !aut2.F.contains(aut2.getInitialState())){
			System.out.println("Not included (empty word in A, but not in B).");
			return false;
		}
		Minimization Minimizer = new Minimization();
		aut1 = Minimizer.finite_removeDead(aut1);
		aut2 = Minimizer.finite_removeDead(aut2);
		aut1 = Minimizer.FiniteOneAcc(aut1);
		aut2 = Minimizer.FiniteOneAcc(aut2);
/*		if(Options.verbose){
			System.out.println("Removing dead states and transforming into a form with just one accepting state.");
			System.out.println("Aut A (after this transformation): # of Trans. "+aut1.trans+", # of States "+aut1.states.size()+".");
			System.out.println("Aut B (after this transformation): # of Trans. "+aut2.trans+", # of States "+aut2.states.size()+".");
		}*/
		AutomatonPreprocessingResult x = Minimizer.Lightweight_Preprocess_Finite(aut1, aut2);
		if(x.result){
			System.out.println("Included (already proven during lightweight finite-aut. preprocessing).");
			return true;
		}
		aut1=x.system;
		aut2=x.spec;
		/*if(Options.verbose){
			System.out.println("Aut A (after finite aut. light preprocessing): # of Trans. "+aut1.trans+", # of States "+aut1.states.size()+".");
			System.out.println("Aut B (after finite aut. light preprocessing): # of Trans. "+aut2.trans+", # of States "+aut2.states.size()+".");
		}*/
		if(aut1.states.size()+aut2.states.size() <= 600){
			/*if(Options.verbose){
				System.out.println("Trying to find a short counterexample. Translating to a Buchi inclusion problem.");
				System.out.println("Using subsumption method with small bound (50 metagraphs).");
			}*/
			// make a copy and transform into Buchi automata
			FiniteAutomaton aut1x = Minimizer.finite_removeDead(aut1);
			FiniteAutomaton aut2x = Minimizer.finite_removeDead(aut2);
			aut1x=toBA(aut1x);
			aut2x=toBA(aut2x);
			InclusionOptBVLayered inclusion=new InclusionOptBVLayered(aut1x,aut2x,50);
			inclusion.run();
			if(!inclusion.timeout){
				//ttime1=inclusion.getRunTime();
				if(inclusion.isIncluded()){
					System.out.println("Included.");
					return true;
				}else{
					System.out.println("Not Included.");
					return false;
				}
			}
			/*if(Options.verbose){
				System.out.println("Light methods alone could not solve it. Trying heavy preprocessing.");
			}*/
		}
		x = Minimizer.Preprocess_Finite(aut1, aut2);
		if(x.result){
			System.out.println("Included (already proven during finite-aut. preprocessing).");
			return true;
		}
		aut1=x.system;
		aut2=x.spec;
		/*if(Options.verbose){
			System.out.println("Aut A (after full preprocessing): # of Trans. "+aut1.trans+", # of States "+aut1.states.size()+".");
			System.out.println("Aut B (after full preprocessing): # of Trans. "+aut2.trans+", # of States "+aut2.states.size()+".");
			System.out.println("Translating to Buchi inclusion and searching for a counterexample.");
		}*/
		// Starting simulation in separate thread
		//Options.globalstop=false;
		DirectsimThread fst=null;
		
		/*if(Options.jumping_fairsim){
			fst = new DirectsimThread(aut1, aut2);
			fst.start();
		}*/
		
		FiniteAutomaton aut1x = Minimizer.finite_removeDead(aut1);
		FiniteAutomaton aut2x = Minimizer.finite_removeDead(aut2);
		aut1x=toBA(aut1x);
		aut2x=toBA(aut2x);

		InclusionOptBVLayered inclusion=new InclusionOptBVLayered(aut1x,aut2x,0);
		inclusion.run();
		//if(Options.jumping_fairsim) fst.stop();
		if(inclusion.isIncluded()){
			System.out.println("Included.");
			return true;
		}else{
			System.out.println("Not Included.");
			return false;
		}

		//}




	}

}
