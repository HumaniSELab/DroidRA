package simidroid.TestApps;

import simidroid.AppAbstract;
import simidroid.FeatureExtraction;
import simidroid.SimilarityAnalysis;
import simidroid.plugin.component.ComponentFeatureExtraction;
import simidroid.plugin.component.ComponentSimilarityAnalysis;
import simidroid.plugin.method.MethodFeatureExtraction;
import simidroid.plugin.method.MethodSimilarityAnalysis;
import simidroid.plugin.resource.ResourceFeatureExtraction;
import simidroid.plugin.resource.ResourceSimilarityAnalysis;

/**
 * For pairwise
 * 
 * @author li.li
 *
 */
public class SimiDroidClient4PW {

	public static void start()
	{
		Config.app1Path = Config.appPathes[0];
		Config.app2Path = Config.appPathes[1];
		
		for (PluginName pluginName : Config.supportedPlugins)
		{
			switch (pluginName)
			{
			case METHOD:
				if (null == Config.librarySetPath)
				{
					compare(new MethodFeatureExtraction(Config.app1Path), new MethodFeatureExtraction(Config.app2Path), new MethodSimilarityAnalysis());
				}
				else
				{
					compare(new MethodFeatureExtraction(Config.app1Path), new MethodFeatureExtraction(Config.app2Path), new MethodSimilarityAnalysis(Config.librarySetPath, Config.librarySetExclusive));
				}
				
				break;
			case COMPONENT:
				compare(new ComponentFeatureExtraction(Config.app1Path), new ComponentFeatureExtraction(Config.app2Path), new ComponentSimilarityAnalysis());
				
				break;
			case RESOURCE:
				if (null == Config.resExtensionSetPath)
				{
					compare(new ResourceFeatureExtraction(Config.app1Path), new ResourceFeatureExtraction(Config.app2Path), new ResourceSimilarityAnalysis());
				}
				else
				{
					compare(new ResourceFeatureExtraction(Config.app1Path), new ResourceFeatureExtraction(Config.app2Path), new ResourceSimilarityAnalysis(Config.resExtensionSetPath, Config.resExtensionSetExclusive));
				}
				
				break;
			}
		}
	}

	public static void compare(FeatureExtraction appA1, FeatureExtraction appA2, SimilarityAnalysis sa)
	{
		appA1.extract();
		AppAbstract aa1 = appA1.getAppAbstract();
		
		appA2.extract();
		AppAbstract aa2 = appA2.getAppAbstract();
		
		sa.compare(aa1, aa2);

		sa.output(true);
		sa.output2GlobalRef(appA1, appA2);
		sa.writeResultAsJSON(appA1.getAppAbstract().appName.replace(".apk", "") + "-" + 
				appA2.getAppAbstract().appName.replace(".apk", "") + ".json");
	}
}
