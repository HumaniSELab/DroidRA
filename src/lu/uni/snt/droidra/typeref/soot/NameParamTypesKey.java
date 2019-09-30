package lu.uni.snt.droidra.typeref.soot;

import soot.Type;

import java.util.List;

/**
 * Created by sun on 2019/9/29.
 */
public class NameParamTypesKey {
    /**
     * refer to methodName/fieldName
     */
    public String name;
    public List<Type> paramTypes;
}
