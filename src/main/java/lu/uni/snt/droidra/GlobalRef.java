package lu.uni.snt.droidra;

import lu.uni.snt.droidra.model.ReflectionProfile.RClass;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.model.UniqStmt;
import lu.uni.snt.droidra.typeref.ArrayVarValue;
import lu.uni.snt.droidra.typeref.soot.fieldrelated.FieldTypesValue;
import lu.uni.snt.droidra.typeref.soot.methodrelated.*;
import soot.SootClass;
import soot.SootMethod;

import java.util.Map;
import java.util.Set;

public class GlobalRef 
{
	public static String apkPath;
	public static String pkgName;
	public static String apkVersionName;
	public static int apkVersionCode = -1;
	public static int apkMinSdkVersion;
	public static Set<String> apkPermissions;
	
	public static String clsPath;
	
	//Configuration files
	public static final String WORKSPACE = "workspace";
	public static String fieldCallsConfigPath = "res/FieldCalls.txt";
	//public static String coalModelPath = "res/reflection.model";
	public static String coalModelPath = "res/reflection_simple.model";
	public static String rfModelPath = "res/reflection.model";
	public static String dclModelPath = "res/dynamic_code_loading.model";
	
	public static Map<UniqStmt, StmtValue> uniqStmtKeyValues;
	public static Map<String, RClass> rClasses;
	public static Map<UniqStmt, ArrayVarValue[]> arrayTypeRef;
	public static Map<UniqStmt, StmtKey> keyPairs;

	/**
	 * class-method-paramTypes maps
	 */
	public static Map<ClassParamTypesKey, Set<String>> classParamTypesKeyMethodValueMap;
	public static Map<NameParamTypesKey, Set<String>> nameParamTypesKeyClassValueMap;
	public static Map<ClassMethodParamTypesKey, String> classMethodParamTypesKeyStringMap;
	public static Map<ParamTypesKey, Set<ClassMethodValue>> paramTypesKeySetMap;

	/**
	 * class-field-paramTypes maps
	 */
	public static Map<String, Set<FieldTypesValue>> classNameFieldTypesMap;

	public static final String jsonFile = "refl.json";

}
