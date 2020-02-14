package lu.uni.snt.droidra.util;

import lu.uni.snt.droidra.GlobalRef;
import soot.*;
import soot.options.Options;
import soot.util.Chain;

import java.util.*;

public class MethodExtraction
{
	private String appPath;
	private String androidJar;

	public MethodExtraction(String appPath, String androidJar)
	{
		this.appPath = appPath;
		this.androidJar = androidJar;
	}

	public void extract()
	{
		String[] args =
				{
						"-force-android-jar", androidJar,
						"-process-dir", this.appPath,
						"-ire",
						"-pp",
						"-keep-line-number",
						"-allow-phantom-refs",
						"-w",
						"-p", "cg", "enabled:false",
						"-src-prec", "apk"
				};

		soot.G.reset();

		Options.v().set_output_format(Options.output_format_none);

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.MethodExtraction", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {

				Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();

				for (Iterator<SootClass> iter = sootClasses.snapshotIterator(); iter.hasNext();)
				{
					SootClass sc = iter.next();

					if(!sc.isConcrete()){
						continue;
					}

					List<SootMethod> methodCopyList = new ArrayList<>(sc.getMethods());
					methodCopyList.stream().filter(methodCopy -> {
						return methodCopy.isConcrete();
					}).forEach(methodCopy -> {
						//Body b = methodCopy.retrieveActiveBody();
						Map<String, SootMethod> latestAppsMethodList = GlobalRef.latestAppsMethodList;
						latestAppsMethodList.putIfAbsent(methodCopy.getSignature(), methodCopy);
					});
				}
			}
		}));

        soot.Main.main(args);
	}
}
