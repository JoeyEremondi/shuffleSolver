package nfa;

import DFA.LabeledNFA;
import automata.FAState;
import automata.FiniteAutomaton;

import java.io.*;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.*;


public class ShuffleTest {

	Random rand = new Random();
	Set<String> seen = new HashSet<String>();

	public  int falseCount = 0;
	public  int trueCount = 0;

	private LinkedList<TestRun> runs = new LinkedList<TestRun>();

	public Writer outFile;


	public ShuffleTest()
	{
		String fileName = new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());
		try
		{
			outFile = new BufferedWriter(new FileWriter(fileName, true));
			System.err.println("Opened file");


		}
		catch (IOException e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}

	public void addRun(TestRun run)
	{
		this.runs.add(run);
		try{
			outFile.write("" + run + "\n");
			
		}
		catch (IOException e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}


	public String randomString(int as, int bs)
	{
		int asLeft = as;
		int bsLeft = bs;
		StringBuffer ret = new StringBuffer();
		while (asLeft > 0 && bsLeft > 0)
		{
			if (rand.nextBoolean())
			{
				ret.append('a');
				asLeft--;
			}
			else
			{
				ret.append('b');
				bsLeft--;
			}
		}
		//Add any remaining as and bs to the end
		while (asLeft > 0)
		{
			ret.append('a');
			asLeft--;
		}
		while (bsLeft > 0)
		{
			ret.append('b');
			bsLeft--;
		}
		return ret.toString();
	}

	public String joinDollar(String u, String v, String x, String y)
	{
		return u + "$" + v + "$" + x + "$" + y;
	}

	public String reverseDollar(String u, String v, String x, String y)
	{
		return NFAUtils.strReverse(u) + "$" + NFAUtils.strReverse(v) + "$" + NFAUtils.strReverse(x) + "$" + NFAUtils.strReverse(y);
	}

	public List<String> variations(String u, String v, String x, String y)
	{
		List<String> ret = new LinkedList<String>();

		ret.add(joinDollar(u,v,x,y));
		ret.add(joinDollar(u,v,y,x));
		ret.add(joinDollar(v,u,x,y));
		ret.add(joinDollar(v,u,y,x));

		ret.add(reverseDollar(u,v,x,y));
		ret.add(reverseDollar(u,v,y,x));
		ret.add(reverseDollar(v,u,x,y));
		ret.add(reverseDollar(v,u,y,x));

		return ret;
	}

	public void randomTestLength(int n)
	{
		int ulen = rand.nextInt(n-1) + 1;
		int vlen = n - ulen + 1;

		int xlen = rand.nextInt(n-1) + 1;
		int ylen = n - xlen + 1;

		int ua = rand.nextInt(ulen);
		int ub = ulen - ua;

		int va = rand.nextInt(vlen);
		int vb = vlen - va;

		int xa = rand.nextInt(xlen);
		int xb = xlen - xa;

		int ya = rand.nextInt(ylen);
		int yb = ylen - ya;

		if (ua+va == 0 || ub+vb == 0)
		{
			return;
		}

		String u,v,x,y;

		u = randomString(ua, ub);
		v = randomString(va, vb);
		x = randomString(xa, xb);
		y = randomString(ya,yb);

		System.out.println("Trying strings " + u + " " + v + " " + x + " " + y);

		for (String var : variations(u,v,x,y))
		{
			if (seen.contains(var))
			{
				System.err.println("Skipping duplicate" + u + " " + v + " " + x + " " + y );
				return;
			}
		}
		//seen.add(joinDollar(u,v,x,y));
		//TODO prevent duplicates?

		//TODO timer stuff
		TestRun run = isShuffleSubset(u,v,x,y);
		this.addRun(run);
		//System.out.println("Is subset? " + run);
		if (run.isSubset)
		{
			trueCount++;
		}
		else
		{
			falseCount++;
		}
		//Help gc?
		//System.gc();
	}


	public static boolean decompCheck(String u, String v, String x, String y)
	{
		//LabeledNFA m1 = NFAUtils.shuffleNFA(u,v);
		//LabeledNFA m2 = NFAUtils.shuffleNFA(x,y);

		LabeledNFA mUnion = NFAUtils.shuffleUnion(u,v,x,y);

		try
		{
			ShuffleDecomp unionDecomp = new ShuffleDecomp(mUnion);

			//help GC
			mUnion = null;

			boolean order1 = x.equals(unionDecomp.u()) && y.equals(unionDecomp.v());
			boolean order2 = x.equals(unionDecomp.v()) && y.equals(unionDecomp.u());

			if (!(order1 || order2))
			{
				mUnion = null;
				return false;
			}
			return true;
		}
		catch (NotDecomposableException e)
		{
			return false;
		}
	}

	public static TestRun isShuffleSubset(String u, String v, String x, String y)
	{

		boolean isSubset;
		boolean usedRABIT;
		long startTime;
		long decompTime;
		long endTime;

		startTime = System.nanoTime();

		if (!decompCheck(u,v,x,y))
		{
			//System.err.println("Failed after decomp check");
			endTime = System.nanoTime();
			decompTime = endTime;
			isSubset = false;
			usedRABIT = false;
		}
		if (!decompCheck(NFAUtils.strReverse(u), NFAUtils.strReverse(v), NFAUtils.strReverse(x), NFAUtils.strReverse(y)))
		{
			//System.err.println("Failed after decomp reverse check");
			endTime = System.nanoTime();
			decompTime = endTime;
			isSubset = false;
			usedRABIT = false;
		}
		else
		{
			decompTime = System.nanoTime();
			usedRABIT = true;


			String[] args;
			LabeledNFA m1 = NFAUtils.shuffleNFA(u,v);
			LabeledNFA m2 = NFAUtils.shuffleNFA(x,y);


			isSubset = SimAntiCheck.isSubset( m1, m2).getLeft();
			endTime = System.nanoTime();
		}

		return new TestRun(u,v,x,y,isSubset, usedRABIT, startTime, decompTime, endTime);



	}

	public static void main(String[] args) {
		ShuffleTest tester = new ShuffleTest();
		
		

		for (int i = 1; i < 15; i++)
		{
			for (int times = 0; times < 30; times++)
			{
				System.out.println("n = " + (int)Math.pow(2,i) + ", iteration " + times);
				System.out.println("Num true: " + tester.trueCount + ", Num false " + tester.falseCount);
				tester.randomTestLength((int)Math.pow(2,i));
			}
		}

		try
		{
			tester.outFile.close();
		}catch (Exception e) {}

	}

	static class TestRun
	{
		public String u,v,x,y;

		public boolean isSubset;
		public boolean usedRABIT;

		public double decompDuration;
		public double totalDuration;

		public int n;

		public TestRun(String u, String v, String x, String y, boolean isSubset, boolean usedRABIT, long st, long dt, long et)
		{
			this.u = u;
			this.v = v;
			this.x = x;
			this.y = y;
			//Store time in miliseconds
			this.decompDuration = (dt - st)/1000000.0;
			this.totalDuration  = (et - st)/1000000.0;

			this.isSubset = isSubset;
			this.usedRABIT = usedRABIT;

			this.n = u.length() + v.length();
		}

		public String toString()
		{
			return "STRINGS:" + u + " " + v + " " + x + " " + y + " IS_SUBSET" + isSubset + " USED_RABIT" + usedRABIT + " DECOMP_TIME" + decompDuration + " TOTAL_TIME" + totalDuration;
		}


	}



}
