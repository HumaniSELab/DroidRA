package simidroid.plugin.method;

import lu.uni.snt.droidra.GlobalRef;
import soot.*;
import soot.jimple.Stmt;
import soot.util.Chain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MethodFeatureTransformer extends SceneTransformer
{
	private Map<String, MethodAbstract> methodFeatures = new HashMap<String, MethodAbstract>();
	
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) 
	{
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		
		for (Iterator<SootClass> iter = sootClasses.snapshotIterator(); iter.hasNext();)
		{
			SootClass sc = iter.next();
			
			List<SootMethod> sootMethods = sc.getMethods();
			
			for (int i = 0; i < sootMethods.size(); i++)
			{
				SootMethod sm = sootMethods.get(i);
				
				extractStmtInfo(sm);
			}
		}
	}

	public void extractStmtInfo(SootMethod sm)
	{
		try
		{
			Body body = sm.retrieveActiveBody();

			PatchingChain<Unit> units = body.getUnits();
			
			MethodAbstract ma = new MethodAbstract();
			ma.methodSignature = sm.getSignature();
			
			for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
			{
				Stmt stmt = (Stmt) iterU.next();

				StmtAbstract sa = new StmtAbstract(stmt);
				ma.stmts.add(sa);
				
				if (stmt.containsInvokeExpr())
				{
					ma.callEdges.add(sm.getSignature() + "/" + stmt.getInvokeExpr().getMethod().getSignature());
				}
			}
			
			methodFeatures.put(sm.getSignature(), ma);
		}
		catch (RuntimeException ex)
		{
			//System.out.println("No body for method " + sm.getSignature());
		}
	}

	public Map<String, MethodAbstract> getMethodFeatures() 
	{
		return methodFeatures;
	}
}
