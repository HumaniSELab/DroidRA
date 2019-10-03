package lu.uni.snt.droidra.serviceimpl;

import lu.uni.snt.droidra.ClassDescription;
import lu.uni.snt.droidra.GlobalRef;
import lu.uni.snt.droidra.model.DroidRAConstant;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.service.UnknowValueInferService;
import lu.uni.snt.droidra.typeref.soot.ClassMethodValue;
import lu.uni.snt.droidra.typeref.soot.ClassParamTypesKey;
import lu.uni.snt.droidra.typeref.soot.NameParamTypesKey;
import lu.uni.snt.droidra.typeref.soot.ParamTypesKey;
import lu.uni.snt.droidra.util.TypeConversionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UnknowValueInferServiceImpl implements UnknowValueInferService {

    @Override
    public Map<StmtKey, StmtValue> inferClassNameThroughAllSootMethods(Map<StmtKey, StmtValue> stmtKeyValues) {
        Map<StmtKey, StmtValue> newStmtKeyValues = new HashMap<StmtKey, StmtValue>();

        Map<NameParamTypesKey, Set<String>> nameParamTypesKeyClassValueMap = GlobalRef.nameParamTypesKeyClassValueMap;

        stmtKeyValues.entrySet().stream().forEach(entry -> {
            StmtKey key = entry.getKey();
            StmtValue value = entry.getValue();

            Set<ClassDescription> oldSet = new HashSet<ClassDescription>();
            Set<ClassDescription> newSet = new HashSet<ClassDescription>();

            value.getClsSet().stream().forEach(clsDesc -> {
                String clsName = clsDesc.cls;

                if (null != clsName && (StringUtils.equals(clsName, DroidRAConstant.STAR_SYMBOL) || clsName.contains( DroidRAConstant.STAR_SYMBOL))) {
                    //use known field/method to guess className
                    //There is also no way to infer className for CLASS_NEW_INSTANCE/CONSTRUCTOR_CALL
                    switch (value.getType()) {
                        case FIELD_CALL:
                            //If you only know the field name and type, you can't find the class it belongs to.
                            //TODO 2019/9/30 sun
                            break;
                        case METHOD_CALL:
                            String methodName = clsDesc.name;

                            if (StringUtils.isBlank(methodName) || methodName.equals("(.*)")) {
                                break;
                            }

                            NameParamTypesKey nameParamTypesKey = new NameParamTypesKey();
                            nameParamTypesKey.name = methodName;
                            nameParamTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(key.getMethod().getParameterTypes());

                            Set<String> possibleClsNames = nameParamTypesKeyClassValueMap.get(nameParamTypesKey);

                            if (null != possibleClsNames && possibleClsNames.size() > 0) {
                                oldSet.add(clsDesc);
                                possibleClsNames.stream().forEach(possibleClsName -> {
                                    ClassDescription cd = new ClassDescription();
                                    cd.cls = DroidRAConstant.OPTIMIZED + possibleClsName;
                                    cd.name = DroidRAConstant.OPTIMIZED + methodName;
                                    newSet.add(cd);
                                });
                            }

                            break;
                        default:    //SIMPLE_STRING
                            break;
                    }
                }
            });
            value.getClsSet().removeAll(oldSet);
            value.getClsSet().addAll(newSet);

            newStmtKeyValues.put(key, value);
        });
        return newStmtKeyValues;
    }

    @Override
    public Map<StmtKey, StmtValue> inferMethodNameThroughAllSootClasses(Map<StmtKey, StmtValue> stmtKeyValues) {
        Map<StmtKey, StmtValue> newStmtKeyValues = new HashMap<StmtKey, StmtValue>();

        Map<ClassParamTypesKey, Set<String>> classParamTypesKeyMethodValueMap = GlobalRef.classParamTypesKeyMethodValueMap;
        stmtKeyValues.entrySet().stream().forEach(entry -> {

            StmtKey key = entry.getKey();
            StmtValue value = entry.getValue();

            Set<ClassDescription> oldSet = new HashSet<ClassDescription>();
            Set<ClassDescription> newSet = new HashSet<ClassDescription>();

            value.getClsSet().stream().filter(clsDesc -> {
                return StringUtils.isNotBlank(clsDesc.name);
            }).forEach(clsDesc -> {
                String name = clsDesc.name;

                if (StringUtils.equals(name, DroidRAConstant.STAR_SYMBOL) || name.contains(DroidRAConstant.STAR_SYMBOL)) {
                    //use known className to guess field/method
                    //There is also no way to infer className for CLASS_NEW_INSTANCE/CONSTRUCTOR_CALL
                    switch (value.getType()) {
                        case FIELD_CALL:
                            //If you only know the field name and type, you can't find the class it belongs to.
                            //TODO 2019/9/30 sun
                            break;
                        case METHOD_CALL:
                            String clsName = clsDesc.cls;

                            if (StringUtils.isBlank(clsName) || StringUtils.equals(clsName, DroidRAConstant.STAR_SYMBOL)) {
                                break;
                            }

                            ClassParamTypesKey classParamTypesKey = new ClassParamTypesKey();
                            classParamTypesKey.cls = clsName;
                            classParamTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(key.getMethod().getParameterTypes());

                            Set<String> possibleMethodNames = classParamTypesKeyMethodValueMap.get(classParamTypesKey);

                            if (null != possibleMethodNames && possibleMethodNames.size() > 0) {
                                oldSet.add(clsDesc);
                                possibleMethodNames.stream().forEach(possibleMethodName -> {
                                    ClassDescription cd = new ClassDescription();
                                    cd.cls = DroidRAConstant.OPTIMIZED + clsName;
                                    cd.name = DroidRAConstant.OPTIMIZED + possibleMethodName;
                                    newSet.add(cd);
                                });
                            }

                            break;
                        default:    //SIMPLE_STRING
                            break;
                    }
                }

            });
            value.getClsSet().removeAll(oldSet);
            value.getClsSet().addAll(newSet);

            newStmtKeyValues.put(key, value);

        });

        return newStmtKeyValues;
    }

    @Override
    public Map<StmtKey, StmtValue> inferClassMethodNameThroughAllSootMethods(Map<StmtKey, StmtValue> stmtKeyValues) {
        Map<StmtKey, StmtValue> newStmtKeyValues = new HashMap<StmtKey, StmtValue>();

        Map<ParamTypesKey, Set<ClassMethodValue>> paramTypesKeySetMap = GlobalRef.paramTypesKeySetMap;

        stmtKeyValues.entrySet().stream().forEach(entry -> {

            StmtKey key = entry.getKey();
            StmtValue value = entry.getValue();

            Set<ClassDescription> oldSet = new HashSet<ClassDescription>();
            Set<ClassDescription> newSet = new HashSet<ClassDescription>();

            value.getClsSet().stream().filter(clsDesc -> {
                return StringUtils.isNotBlank(clsDesc.name);
            }).forEach(clsDesc -> {
                String name = clsDesc.name;
                String cls = clsDesc.cls;

                if ((StringUtils.equals(name, DroidRAConstant.STAR_SYMBOL) || name.contains(DroidRAConstant.STAR_SYMBOL))
                        && (StringUtils.equals(cls, DroidRAConstant.STAR_SYMBOL) || cls.contains(DroidRAConstant.STAR_SYMBOL))) {
                    //use known className to guess field/method
                    //There is also no way to infer className for CLASS_NEW_INSTANCE/CONSTRUCTOR_CALL
                    switch (value.getType()) {
                        case FIELD_CALL:
                            //If you only know the field name and type, you can't find the class it belongs to.
                            //TODO 2019/9/30 sun
                            break;
                        case METHOD_CALL:
                            ParamTypesKey paramTypesKey = new ParamTypesKey();
                            paramTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(key.getMethod().getParameterTypes());

                            Set<ClassMethodValue> possibleClassMethodNames = paramTypesKeySetMap.get(paramTypesKey);

                            if (null != possibleClassMethodNames && possibleClassMethodNames.size() > 0) {
                                oldSet.add(clsDesc);
                                possibleClassMethodNames.stream().forEach(possibleMethodName -> {
                                    ClassDescription cd = new ClassDescription();
                                    cd.cls = DroidRAConstant.OPTIMIZED + possibleMethodName.cls;
                                    cd.name = DroidRAConstant.OPTIMIZED + possibleMethodName.method;
                                    newSet.add(cd);
                                });
                            }

                            break;
                        default:    //SIMPLE_STRING
                            break;
                    }
                }

            });
            value.getClsSet().removeAll(oldSet);
            value.getClsSet().addAll(newSet);

            newStmtKeyValues.put(key, value);

        });

        return newStmtKeyValues;
    }
}