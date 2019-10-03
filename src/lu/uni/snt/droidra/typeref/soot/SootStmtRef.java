package lu.uni.snt.droidra.typeref.soot;

import lu.uni.snt.droidra.util.TypeConversionUtil;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sun on 2019/9/26.
 *
 * For each invoke statement, use soot to transform it to Map<UniqStmt, StmtValue> description.
 *
 */
public class SootStmtRef {

    /**
     * Convert all soot stmt to <Class-ParamTypes, Set<String>>, Set<String> refer to MethodName set
     */
    public static Map<ClassParamTypesKey, Set<String>> classParamTypesKeyMethodValueMap = new HashMap<>();
    /**
     * Convert all soot stmt to <MethodName-ParamTypes, Set<String>>, Set<String> refer to ClassName set
     */
    public static Map<NameParamTypesKey, Set<String>> nameParamTypesKeyClassValueMap = new HashMap<>();
    /**
     * Convert all soot stmt to <ClassMethodParamTypesKey, String>, String refer to ClassMethodParamTypesKey.string
     */
    public static Map<ClassMethodParamTypesKey, String> classMethodParamTypesKeyStringMap = new HashMap<>();

    /**
     *  Convert all soot stmt to Map<ParamTypesKey, Set<ClassMethodValue>>
     */
    public static Map<ParamTypesKey, Set<ClassMethodValue>> paramTypesKeySetMap = new HashMap<>();

    public static void setup(String input, String clsPath){
        String[] args =
                {
                        "-process-dir", input,
                        "-ire",
                        "-pp",
                        "-allow-phantom-refs",
                        "-w",
                        "-cp", clsPath,
                        "-p", "cg", "enabled:false",
                        "-p", "jop.cpf", "enabled:true"
                };

        soot.G.reset();

        if (input.endsWith(".apk"))
        {
            Options.v().set_src_prec(Options.src_prec_apk);
            Options.v().set_force_android_jar(clsPath);
        }
        else
        {
            Options.v().set_src_prec(Options.src_prec_class);
        }

        Options.v().set_output_format(Options.output_format_none);
        //Options.v().set_output_format(Options.output_format_class);


        PackManager.v().getPack("jtp").add(new Transform("jtp.SootStmtRef", new BodyTransformer() {

            @Override
            protected void internalTransform(Body b, String phaseName, Map<String, String> options) {

                convertToClassParamTypesKeyMethodValueMap(b);
                convertToNameParamTypesKeyClassValueMap(b);
                convertToClassMethodKeyParamTypesMap(b);
                convertToParamTypesKeySetMap(b);
            }

        }));

        soot.Main.main(args);

        soot.G.reset();
    }

    private static void convertToClassParamTypesKeyMethodValueMap(Body b) {
        ClassParamTypesKey classParamTypesKey = new ClassParamTypesKey();
        classParamTypesKey.cls = b.getMethod().getDeclaringClass().getName();
        classParamTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(b.getMethod().getParameterTypes());
        String methodName = b.getMethod().getName();

        if (classParamTypesKeyMethodValueMap.containsKey(classParamTypesKey)) {
            Set<String> methodValueSet = classParamTypesKeyMethodValueMap.get(classParamTypesKey);
            methodValueSet.add(methodName);
        }else{
            Set<String> methodValueSet = new HashSet<String>();
            methodValueSet.add(methodName);
            classParamTypesKeyMethodValueMap.put(classParamTypesKey, methodValueSet);
        }
    }

    private static void convertToNameParamTypesKeyClassValueMap(Body b) {
        NameParamTypesKey nameParamTypesKey = new NameParamTypesKey();
        nameParamTypesKey.name = b.getMethod().getName();
        nameParamTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(b.getMethod().getParameterTypes());
        String className = b.getClass().getName();

        if(nameParamTypesKeyClassValueMap.containsKey(nameParamTypesKey)){
            Set<String> classValueSet = nameParamTypesKeyClassValueMap.get(nameParamTypesKey);
            classValueSet.add(className);
        }else {
            Set<String> classValueSet = new HashSet<>();
            classValueSet.add(className);
            nameParamTypesKeyClassValueMap.put(nameParamTypesKey, classValueSet);
        }
    }

    private static void convertToClassMethodKeyParamTypesMap(Body b){
        ClassMethodParamTypesKey classMethodParamTypesKey = new ClassMethodParamTypesKey();
        classMethodParamTypesKey.cls = b.getClass().getName();
        classMethodParamTypesKey.method = b.getMethod().getName();
        classMethodParamTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(b.getMethod().getParameterTypes());

        classMethodParamTypesKeyStringMap.put(classMethodParamTypesKey, classMethodParamTypesKey.toString());
    }

    private static void convertToParamTypesKeySetMap(Body b){
        ParamTypesKey paramTypesKey = new ParamTypesKey();
        paramTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(b.getMethod().getParameterTypes());
        ClassMethodValue classMethodValue = new ClassMethodValue();
        classMethodValue.cls = b.getClass().getName();
        classMethodValue.method = b.getMethod().getName();

        if(paramTypesKeySetMap.containsKey(paramTypesKey)){
            Set<ClassMethodValue> classMethodValues = paramTypesKeySetMap.get(paramTypesKey);
            classMethodValues.add(classMethodValue);
        } else {
            Set<ClassMethodValue> classMethodValues = new HashSet<>();
            classMethodValues.add(classMethodValue);
            paramTypesKeySetMap.put(paramTypesKey, classMethodValues);
        }
    }


}
