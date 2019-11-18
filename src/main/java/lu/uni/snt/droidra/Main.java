package lu.uni.snt.droidra;

import com.google.gson.Gson;
import edu.psu.cse.siis.coal.DefaultCommandLineArguments;
import edu.psu.cse.siis.coal.DefaultCommandLineParser;
import edu.psu.cse.siis.coal.DefaultResult;
import edu.psu.cse.siis.coal.Result;
import lu.uni.snt.droidra.booster.ApkBooster;
import lu.uni.snt.droidra.model.ReflectionExchangable;
import lu.uni.snt.droidra.model.ReflectionProfile;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.model.UniqStmt;
import lu.uni.snt.droidra.retarget.DummyMainGenerator;
import lu.uni.snt.droidra.retarget.RetargetWithDummyMainGenerator;
import lu.uni.snt.droidra.retarget.SootSetup;
import lu.uni.snt.droidra.typeref.soot.SootStmtRef;
import lu.uni.snt.droidra.util.ApplicationClassFilter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.xmlpull.v1.XmlPullParserException;
import simidroid.TestApps.SimiDroidClient;
import soot.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.resources.LayoutFileParser;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;

import java.io.*;
import java.util.*;

/**
 * at soot.toDex.PrimitiveType.getByName(PrimitiveType.java:24)
	at soot.toDex.ExprVisitor.castPrimitive(ExprVisitor.java:587)
	at soot.toDex.ExprVisitor.caseCastExpr(ExprVisitor.java:548)
	at soot.jimple.internal.AbstractCastExpr.apply(AbstractCastExpr.java:134)
	at soot.toDex.StmtVisitor.caseAssignStmt(StmtVisitor.java:371)
	at soot.jimple.internal.JAssignStmt.apply(JAssignStmt.java:238)
	at soot.toDex.DexPrinter.toInstructions(DexPrinter.java:1152)
	at soot.toDex.DexPrinter.toMethodImplementation(DexPrinter.java:1035)
	at soot.toDex.DexPrinter.toMethods(DexPrinter.java:932)
	at soot.toDex.DexPrinter.addAsClassDefItem(DexPrinter.java:496)
	at soot.toDex.DexPrinter.add(DexPrinter.java:1281)
	at soot.PackManager.writeClass(PackManager.java:1004)
	at soot.PackManager.writeOutput(PackManager.java:620)
	at soot.PackManager.writeOutput(PackManager.java:526)
	at soot.Main.run(Main.java:250)
	at soot.Main.main(Main.java:152)
	at lu.uni.snt.droidra.booster.ApkBooster.apkBooster(ApkBooster.java:69)
	at lu.uni.snt.droidra.Main.main(Main.java:145)
 */



/**
 * COAL contribution
 * 
 * No parameter exception
 * Providing a parameter for manually specifying a main method
 * Model.toString() exception, some Field is null, finding the reasons
 * 
 * Check sub-class model and override-method
 * 
 * @author li.li
 *
 */
public class Main 
{
	/**
	 * 0. Some inits
	 * 
	 * 1. Retarget Android app to class (with a single main entrance).
	 * 
	 * 2. Launch COAL for reflection string extractions.
	 * 		In this step, we can also put the results into a database for better usage. (heuristic results)
	 *     
	 * 
	 * 3 Revist the Android app to make sure all the involved Class and methods, 
	 *     fields exist in the current classpath, if not, 
	 *     1) try to dynamically load them, or 
	 *     2) create fake one for all of them.
	 *     
	 *     ==> it can also provide heuristic results for human analysis (e.g., how the app code is dynamically loaded)
	 * 
	 * 4. Revisit the Android app for instrumentation.
	 * 	   Even in this step, if some methods, fields or constructors do not exist, 
	 *     a robust implementation should be able to create them on-the-fly.
	 *      
	 * 5. Based on the instrumented results to perform furture static analysis.
	 * 
	 * @param args
	 */

	public static void main(String[] args) 
	{
		BasicConfigurator.configure();

		long startTime = System.currentTimeMillis();
		System.out.println("==>TIME:" + startTime);

		//args[0]:latest apk load path; args[1]: Android.jar load path; args[2]:incrementalAnalysisSwitch; args[3]: last apk load path
		String apkPath = args[0];
		String forceAndroidJar = args[1];
		String incrementalAnalysisSwitchOn = args[2];

		//simiDroid Analysis
		String[] simiDroidArgs = new String[0];
		if(StringUtils.isNotBlank(incrementalAnalysisSwitchOn) && incrementalAnalysisSwitchOn.equalsIgnoreCase("TRUE")){
			List<String> params = new ArrayList<String>(Arrays.asList(args));
			params.remove(forceAndroidJar);
			params.remove(incrementalAnalysisSwitchOn);

			simiDroidArgs = new String[params.size()];
			simiDroidArgs = params.toArray(simiDroidArgs);
			try {
				SimiDroidClient.simiDroidAnalysis(simiDroidArgs);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("==>simiDroid Analysis error:" + e);
			}
		}

		//calculate EntryPoint to generate dummyMainMethod
		try {
			calculateEntryPoint(apkPath, forceAndroidJar, incrementalAnalysisSwitchOn, simiDroidArgs);
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
			System.out.println("==>calculateEntryPoint error:" + e);
		}
		// sunxiaobiu: 14/10/19 change init to new FlowDroid Setup Method
		//init(apkPath, forceAndroidJar, null);
		
		long afterDummyMain = System.currentTimeMillis();
		System.out.println("==>afterDummyMain TIME:" + afterDummyMain);

		reflectionAnalysis();
		//toReadableText(apkName);
		toJson();
		

		long afterRA = System.currentTimeMillis();
		System.out.println("==>afterRA TIME:" + afterRA);
		
//		booster();
		
		long afterBooster = System.currentTimeMillis();
		System.out.println("==>afterBooster TIME:" + afterBooster);
		
		System.out.println("====>TIME_TOTAL:" + startTime + "," + afterDummyMain + "," + afterRA + "," + afterBooster);
	}

	public static int test()
	{
		return (int) new Object();
	}

	/**
	 * calculate Entry Point in the given APK file.
	 */
	public static void calculateEntryPoint(String apkPath, String forceAndroidJar, String incrementalAnalysisSwitchOn, String[] simiDroidArgs) throws IOException, XmlPullParserException {
		DummyMainGenerator dummyMainGenerator = new DummyMainGenerator(apkPath);
		InfoflowAndroidConfiguration config = dummyMainGenerator.config;
		// sunxiaobiu: 14/10/19 you can modify your own config by changing "config" parameters
		config.getAnalysisFileConfig().setTargetAPKFile(apkPath);
		config.getAnalysisFileConfig().setAndroidPlatformDir(forceAndroidJar);
		//config.getCallbackConfig().setCallbackAnalyzer(InfoflowAndroidConfiguration.CallbackAnalyzer.Fast);
		//config.getCallbackConfig().setEnableCallbacks(false);
		config.setWriteOutputFiles(true);

		SootSetup.initializeSoot(config, forceAndroidJar);

		if (! new File(GlobalRef.SOOTOUTPUT).exists())
		{
			File sootOutput = new File(GlobalRef.SOOTOUTPUT);
			sootOutput.mkdirs();
		}

		try
		{
			FileUtils.cleanDirectory(new File(GlobalRef.SOOTOUTPUT));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		//collect all Dynamic loaded Fragments
		collectDynamicFragments();

		DroidRAUtils.extractApkInfo(apkPath);
		GlobalRef.clsPath = forceAndroidJar;

		if (config.getCallbackConfig().getEnableCallbacks()) {
			dummyMainGenerator.parseAppResources();
			LayoutFileParser lfp = dummyMainGenerator.createLayoutFileParser();
			switch (config.getCallbackConfig().getCallbackAnalyzer()) {
				case Fast:
					dummyMainGenerator.calculateCallbackMethodsFast(lfp, null);
					break;
				case Default:
					dummyMainGenerator.calculateCallbackMethods(lfp, null);
					break;
				default:
					throw new RuntimeException("Unknown callback analyzer");
			}

		} else {
			// Create the new iteration of the main method
			dummyMainGenerator.createMainMethod(null);
			dummyMainGenerator.constructCallgraphInternal();
		}

		SootClass originSootClass = Scene.v().getSootClass("dummyMainClass");
		if(CollectionUtils.isNotEmpty(originSootClass.getMethods())){
			for(int i = 0; i < originSootClass.getMethods().size(); i++){
				if(originSootClass.getMethods().get(i).getName().equals("dummyMainMethod")){
					originSootClass.getMethods().get(i).setName("main");
					GlobalRef.dummyMainMethod = originSootClass.getMethods().get(i);
					GlobalRef.dummyMainClass = originSootClass;
				}
			}
		}


		//before ouput soot classes, run simiDroid analysis
		//simiDroidAnalysis();
		if(incrementalAnalysisSwitchOn.equals("true")){
			incrementalAnalysis();

			//pruningAnalysis();

			deleteSootMethodsInDummyMain();
		}

		PackManager.v().writeOutput();
	}

	private static void pruningAnalysis(){
		Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();

		for (Iterator<SootClass> iter = applicationClasses.snapshotIterator(); iter.hasNext();) {
			SootClass sootClass = iter.next();

			if(!ApplicationClassFilter.isApplicationClass(sootClass)){
				continue;
			}

			if(!sootClass.isConcrete()){
				continue;
			}

			List<SootMethod> methodCopyList = new ArrayList<>(sootClass.getMethods());
			methodCopyList.stream().filter(methodCopy -> {
				return methodCopy.isConcrete();
			}).forEach(methodCopy -> {

				boolean flag = true;
				Body body = methodCopy.retrieveActiveBody();
				PatchingChain<Unit> units = body.getUnits();

				for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
				{
					Stmt stmt = (Stmt) iterU.next();

					if (stmt.containsInvokeExpr()){
						List<String> methodInvokeChain = new ArrayList<>();
						methodInvokeChain.add(methodCopy.getSignature());
						Iterator<Edge> edgeIt = Scene.v().getCallGraph().edgesOutOf(stmt);
						while (edgeIt.hasNext()) {
							Edge edge = edgeIt.next();
							String targetMethodtSignature = edge.getTgt().method().getSignature();
							String targetClass = edge.getTgt().method().getDeclaringClass().getName();
							if(ApplicationClassFilter.isApplicationClass(targetClass)){
								methodInvokeChain.add(targetMethodtSignature);
							}
						}

						for(String ms : methodInvokeChain){
							if(ms.contains("java.lang.reflect")){
								flag = false;
							}
						}
					}
				}

				if(flag){
					GlobalRef.toBeDeleteSootMethods.add(methodCopy);
				}
			});
		}
	}

	private static void incrementalAnalysis(){
		Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();

		for (Iterator<SootClass> iter = applicationClasses.snapshotIterator(); iter.hasNext();) {
			SootClass sootClass = iter.next();

			if(!ApplicationClassFilter.isApplicationClass(sootClass)){
				continue;
			}

			if(!sootClass.isConcrete()){
				continue;
			}

			List<SootMethod> methodCopyList = new ArrayList<>(sootClass.getMethods());
			methodCopyList.stream().filter(methodCopy -> {
				return methodCopy.isConcrete();
			}).forEach(methodCopy -> {

				Body body = methodCopy.retrieveActiveBody();
				PatchingChain<Unit> units = body.getUnits();
				boolean flag = true;

				if(GlobalRef.newFeatures.contains(methodCopy.getSignature())){
					flag = false;
				}

				if(GlobalRef.similarFeatures.contains(methodCopy.getSignature())){
					flag = false;
				}

				for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
				{
					Stmt stmt = (Stmt) iterU.next();

					if (stmt.containsInvokeExpr()){

						List<String> methodInvokeChain = new ArrayList<>();
						methodInvokeChain.add(methodCopy.getSignature());
						Iterator<Edge> edgeIt = Scene.v().getCallGraph().edgesOutOf(stmt);
						while (edgeIt.hasNext()) {
							Edge edge = edgeIt.next();
							String targetMethodtSignature = edge.getTgt().method().getSignature();
							String targetClass = edge.getTgt().method().getDeclaringClass().getName();
							if(ApplicationClassFilter.isApplicationClass(targetClass)){
								methodInvokeChain.add(targetMethodtSignature);
							}
						}

						for(String ms : methodInvokeChain){
							if(!GlobalRef.identicalFeatures.contains(ms)){
								flag = false;
							}
						}
					}
				}

				if(flag){
					GlobalRef.toBeDeleteSootMethods.add(methodCopy);
				}
			});
		}
	}

	private static void deleteSootMethodsInDummyMain() {
		// delete  toBeDeleteSootMethods in dummyMain
		Scene.v().getSootClass("dummyMainClass").getMethods().stream().filter(sootMethod -> {
			return !sootMethod.getSignature().equals("<dummyMainClass: void main(java.lang.String[])>");
		}).forEach(method->{
			Body body = method.getActiveBody();
			UnitPatchingChain units = body.getUnits();
			units.removeIf((Unit unit) ->{
				Stmt stmt = (Stmt) unit;
				if(stmt.containsInvokeExpr()){
					System.out.println(stmt.getInvokeExprBox().getValue().toString());
					String st = stmt.getInvokeExprBox().getValue().toString();

					for(SootMethod sm : GlobalRef.toBeDeleteSootMethods){
						if(st.contains(sm.getSignature())){
							System.out.println("Delete this stmt in dummyMainClass:"+st);
							return true;
						}
					}
				}
				return false;
			});
		});
	}

	public static void init(String apkPath, String forceAndroidJar, String additionalDexes)
	{
		DroidRAUtils.extractApkInfo(apkPath);	
		GlobalRef.clsPath = forceAndroidJar;

		if (null != additionalDexes)
		{
			RetargetWithDummyMainGenerator.retargetWithDummyMainGeneration(apkPath, forceAndroidJar, GlobalRef.WORKSPACE, additionalDexes.split(File.pathSeparator));
		}
		else
		{
			RetargetWithDummyMainGenerator.retargetWithDummyMainGeneration(apkPath, forceAndroidJar, GlobalRef.WORKSPACE);
		}
	}
	
	public static void reflectionAnalysis()
	{
		String[] args = {
			"-cp", GlobalRef.clsPath,
			"-model", GlobalRef.coalModelPath,
			"-input", GlobalRef.SOOTOUTPUT
		};

//		ArrayVarItemTypeRef.setup(GlobalRef.apkPath, GlobalRef.clsPath);
//		GlobalRef.arrayTypeRef = ArrayVarItemTypeRef.arrayTypeRef;

		SootStmtRef.setup(GlobalRef.apkPath, GlobalRef.clsPath);
		GlobalRef.classParamTypesKeyMethodValueMap = SootStmtRef.classParamTypesKeyMethodValueMap;
		GlobalRef.nameParamTypesKeyClassValueMap = SootStmtRef.nameParamTypesKeyClassValueMap;
		GlobalRef.classMethodParamTypesKeyStringMap = SootStmtRef.classMethodParamTypesKeyStringMap;
		GlobalRef.paramTypesKeySetMap = SootStmtRef.paramTypesKeySetMap;
		GlobalRef.classNameFieldTypesMap = SootStmtRef.classNameFieldTypesMap;

		DroidRAAnalysis<DefaultCommandLineArguments> analysis = new DroidRAAnalysis<>();
		DefaultCommandLineParser parser = new DefaultCommandLineParser();
		DefaultCommandLineArguments commandLineArguments =
		    parser.parseCommandLine(args, DefaultCommandLineArguments.class);
		if (commandLineArguments != null) 
		{
			AndroidMethodReturnValueAnalyses.registerAndroidMethodReturnValueAnalyses("");
			analysis.performAnalysis(commandLineArguments);
		}
		GlobalRef.uniqStmtKeyValues = DroidRAResult.toUniqStmtKeyValues(HeuristicUnknownValueInfer.getInstance().infer(DroidRAResult.stmtKeyValues));
		
		ReflectionProfile.fillReflectionProfile(DroidRAResult.stmtKeyValues);
		GlobalRef.rClasses = ReflectionProfile.rClasses;
		ReflectionProfile.dump();
		ReflectionProfile.dump("==>0:");
	}
	
	public static void booster()
	{
		ApkBooster.apkBooster(GlobalRef.apkPath, GlobalRef.clsPath, GlobalRef.WORKSPACE);
	}
	
	public static void toReadableText(String apkName)
	{
		try 
		{
			PrintStream systemPrintStream = System.out;
					
			PrintStream fileStream = new PrintStream(new File("droidra_" + apkName + "_" + GlobalRef.pkgName + "_v" + GlobalRef.apkVersionCode + ".txt"));
			System.setOut(fileStream);
			
			System.out.println("The following values were found:");
		    for (Result result : DroidRAResultProcessor.results) 
		    {
		    	((DefaultResult) result).dump();
		    }
			
			System.setOut(systemPrintStream);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void toJson()
	{
		String jsonFilePath = GlobalRef.jsonFile;
		
		Gson gson = new Gson();
		
		ReflectionExchangable re = new ReflectionExchangable();
		re.set(GlobalRef.uniqStmtKeyValues);
		
		try 
		{
			FileWriter fileWriter = new FileWriter(jsonFilePath);
			fileWriter.write(gson.toJson(re));
			
			fileWriter.flush();
			fileWriter.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void loadJsonBack()
	{
		String jsonFilePath = GlobalRef.jsonFile;
		
		Gson gson = new Gson();
		
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath));
			ReflectionExchangable re = gson.fromJson(reader, ReflectionExchangable.class);
			
			Map<UniqStmt, StmtValue> map = re.get();
           	
           	for (Map.Entry<UniqStmt, StmtValue> entry : map.entrySet())
           	{
           		System.out.println(entry.getKey().className);
           		System.out.println("    " + entry.getValue());
           	}
           	
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public static void collectDynamicFragments(){
		Set<SootClass> dynamicFragment = new HashSet<>();
		Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
		for (Iterator<SootClass> iter = applicationClasses.snapshotIterator(); iter.hasNext(); ) {
			SootClass sootClass = iter.next();

			// We copy the list of methods to emulate a snapshot iterator which
			// doesn't exist for methods in Soot
			List<SootMethod> methodCopyList = new ArrayList<>(sootClass.getMethods());
			for (SootMethod sootMethod : methodCopyList) {
				if (sootMethod.isConcrete()) {
					final Body body = sootMethod.retrieveActiveBody();
					final LocalGenerator lg = new LocalGenerator(body);

					for (Iterator<Unit> unitIter = body.getUnits().snapshotIterator(); unitIter.hasNext(); ) {
						Stmt stmt = (Stmt) unitIter.next();

						if (stmt.containsInvokeExpr()) {
							SootMethod callee = stmt.getInvokeExpr().getMethod();

							// For Messenger.send(), we directly call the respective handler
							if (callee ==  Scene.v().grabMethod("<android.support.v4.app.FragmentTransaction: android.support.v4.app.FragmentTransaction add(int,android.support.v4.app.Fragment)>")) {

								SootClass fragmentClass = Scene.v().getSootClass(stmt.getInvokeExpr().getArgBox(1).getValue().getType().toString());
								if(ApplicationClassFilter.isApplicationClass(fragmentClass)){
									System.out.println(callee);
									dynamicFragment.add(Scene.v().getSootClass(stmt.getInvokeExpr().getArgBox(1).getValue().getType().toString()));
								}
							}
						}
					}
				}
			}
		}
		GlobalRef.dynamicFragment.addAll(dynamicFragment);
	}


}
