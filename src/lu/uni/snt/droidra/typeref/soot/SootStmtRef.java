package lu.uni.snt.droidra.typeref.soot;

import lu.uni.snt.droidra.typeref.soot.fieldrelated.FieldTypesValue;
import lu.uni.snt.droidra.typeref.soot.methodrelated.*;
import lu.uni.snt.droidra.util.ApplicationClassFilter;
import lu.uni.snt.droidra.util.TypeConversionUtil;
import soot.*;
import soot.options.Options;
import soot.util.Chain;

import java.util.*;

import static soot.SootClass.SIGNATURES;

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
     * Convert all soot stmt to Map<ParamTypesKey, Set<ClassMethodValue>>
     */
    public static Map<ParamTypesKey, Set<ClassMethodValue>> paramTypesKeySetMap = new HashMap<>();

    /**
     * Convert all soot stmt to Map<String, Set<FieldTypesValue>>, String refer to className
     */
    public static Map<String, Set<FieldTypesValue>> classNameFieldTypesMap = new HashMap<>();

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

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.SootStmtMethodRef", new SceneTransformer() {
            @Override
            protected void internalTransform(String s, Map<String, String> map) {
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
                        Body b = methodCopy.retrieveActiveBody();
                        convertToClassParamTypesKeyMethodValueMap(b);
                        convertToNameParamTypesKeyClassValueMap(b);
                        convertToClassMethodKeyParamTypesMap(b);
                        convertToParamTypesKeySetMap(b);
                    });
                }
            }
        }));

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.SootStmtFieldRef", new SceneTransformer() {
            @Override
            protected void internalTransform(String s, Map<String, String> map) {
                Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();

                for (Iterator<SootClass> iter = applicationClasses.snapshotIterator(); iter.hasNext();) {
                    SootClass sootClass = iter.next();

                    if(!ApplicationClassFilter.isApplicationClass(sootClass)){
                        continue;
                    }

                    if(!sootClass.isConcrete()){
                        continue;
                    }

                    sootClass.getFields().stream().forEach(sootField -> {
                        convertToClassNameFieldTypesMap(sootClass.getName(), sootField);
                    });
                }
            }
        }));

        Scene.v().addBasicClass("android.support.annotation.FloatRange", SIGNATURES);

        soot.Main.main(args);

        soot.G.reset();
    }

    private static void convertToClassNameFieldTypesMap(String clsName, SootField sootField){
        FieldTypesValue fieldTypesValue = new FieldTypesValue();
        fieldTypesValue.fieldName = sootField.getName();
        fieldTypesValue.fieldType = sootField.getType().toString();

        if(classNameFieldTypesMap.containsKey(clsName)){
            Set<FieldTypesValue> fieldTypesValues = classNameFieldTypesMap.get(clsName);
            fieldTypesValues.add(fieldTypesValue);
        }else{
            Set<FieldTypesValue> fieldTypesValues = new HashSet<>();
            fieldTypesValues.add(fieldTypesValue);
            classNameFieldTypesMap.put(clsName, fieldTypesValues);
        }
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
        String className = b.getMethod().getDeclaringClass().getName();

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
        classMethodParamTypesKey.cls = b.getMethod().getDeclaringClass().getName();
        classMethodParamTypesKey.method = b.getMethod().getName();
        classMethodParamTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(b.getMethod().getParameterTypes());

        classMethodParamTypesKeyStringMap.put(classMethodParamTypesKey, classMethodParamTypesKey.toString());
    }

    private static void convertToParamTypesKeySetMap(Body b){
        ParamTypesKey paramTypesKey = new ParamTypesKey();
        paramTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(b.getMethod().getParameterTypes());
        ClassMethodValue classMethodValue = new ClassMethodValue();
        classMethodValue.cls = b.getMethod().getDeclaringClass().getName();
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
