package simidroid.plugin.resource;

import simidroid.FeatureExtraction;
import simidroid.utils.ApkUtils;

import java.util.Map;

public class ResourceFeatureExtraction extends FeatureExtraction
{	
	public ResourceFeatureExtraction(String appPath) 
	{
		super(appPath);
	}

	public void extract()
	{
		Map<String, String> pathHashes = ApkUtils.getPathHashes(this.appPath);
		appAbstract.features.putAll(pathHashes);
	}
}
