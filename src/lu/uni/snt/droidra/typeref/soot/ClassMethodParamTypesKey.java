package lu.uni.snt.droidra.typeref.soot;

import soot.Type;

import java.util.List;

public class ClassMethodParamTypesKey {
    /**
     * refer to class name
     */
    public String cls;

    /**
     * refer to method name
     */
    public String method;

    /**
     * refer to paramTypes of stmt
     */
    public List<Type> paramTypes;
}