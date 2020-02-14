package lu.uni.snt.droidra.retarget;

import lu.uni.snt.droidra.DroidRAUtils;
import lu.uni.snt.droidra.GlobalRef;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import soot.*;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.resources.LayoutFileParser;

import java.io.File;
import java.io.IOException;

public class RetargetWithDummyMainGenerator 
{
	public static void retargetWithDummyMainGeneration(String apkPath, String androidJar, String outputDir)
	{
		retargetWithDummyMainGeneration(apkPath, androidJar, outputDir, null, false);
	}
	
	public static void retargetWithDummyMainGeneration(String apkPath, String androidJar, String outputDir, boolean outjar)
	{
		retargetWithDummyMainGeneration(apkPath, androidJar, outputDir, null, true);
	}
	
	public static void retargetWithDummyMainGeneration(String apkPath, String androidJar, String outputDir, String[] additionalDexes)
	{
		retargetWithDummyMainGeneration(apkPath, androidJar, outputDir, additionalDexes, false);
	}

	public static void retargetWithDummyMainGeneration(String apkPath, String androidJar, String outputDir, String[] additionalDexes, boolean outjar){
		try
		{
			FileUtils.cleanDirectory(new File(outputDir));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		G.reset();

		String[] args2 =
        {
            "-force-android-jar", androidJar,
            "-process-dir", apkPath,
            "-ire",
			"-pp",
			"-keep-line-number",
			"-allow-phantom-refs",
			"-w",
			"-p", "cg", "enabled:true",
			"-p", "wjtp.rdc", "enabled:true",
			"-src-prec", "apk"
        };

		DummyMainGenerator dmGenerator = new DummyMainGenerator(apkPath);

		PackManager.v().getPack("wjtp").add(new Transform("wjtp.DummyMainGenerator", dmGenerator));

		soot.Main.main(args2);

		G.reset();

		if (null != additionalDexes && 0 < additionalDexes.length)
		{
			for (String dexPath : additionalDexes)
			{
				DexRetargetor.retargetDex(dexPath, androidJar, outputDir);
			}
		}
	}
}
