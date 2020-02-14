package simidroid.TestApps;

import simidroid.AppAbstract;
import simidroid.FeatureExtraction;
import simidroid.MultiInstanceSimilarityAnalysis;
import simidroid.SimilarityAnalysis;
import simidroid.plugin.component.ComponentFeatureExtraction;
import simidroid.plugin.component.ComponentSimilarityAnalysis;
import simidroid.plugin.method.MethodFeatureExtraction;
import simidroid.plugin.method.MethodSimilarityAnalysis;
import simidroid.plugin.resource.ResourceFeatureExtraction;
import simidroid.plugin.resource.ResourceSimilarityAnalysis;

/**
 * For multiple instances
 * 
 * @author li.li
 *
 */
public class SimiDroidClient4MI {

	public static void start() throws Exception
	{
		FeatureExtraction[] apps = new FeatureExtraction[Config.appPathes.length];
		
		for (PluginName pluginName : Config.supportedPlugins)
		{
			switch (pluginName)
			{
			case METHOD:
				for (int i = 0; i < Config.appPathes.length; i++)
				{
					apps[i] = new MethodFeatureExtraction(Config.appPathes[i]);
				}
				
				compare(apps, MethodSimilarityAnalysis.class);
				
				break;
			case COMPONENT:
				for (int i = 0; i < Config.appPathes.length; i++)
				{
					apps[i] = new ComponentFeatureExtraction(Config.appPathes[i]);
				}
				
				compare(apps, ComponentSimilarityAnalysis.class);
				
				break;
			case RESOURCE:
				for (int i = 0; i < Config.appPathes.length; i++)
				{
					apps[i] = new ResourceFeatureExtraction(Config.appPathes[i]);
				}
				
				compare(apps, ResourceSimilarityAnalysis.class);
				
				break;
			}
		}
	}
	
	public static void compare(FeatureExtraction[] apps, Class<? extends SimilarityAnalysis> saClass) throws Exception
	{

		AppAbstract[] aas = new AppAbstract[apps.length];
		for (int i = 0; i < apps.length; i++)
		{
			apps[i].extract();
			aas[i] = apps[i].getAppAbstract();
		}
		
		MultiInstanceSimilarityAnalysis misa = new MultiInstanceSimilarityAnalysis();
		misa.compare(aas, saClass);
		misa.output(false);
	}
}
