package lu.uni.snt.droidra.service;


import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtValue;

import java.util.Map;

public interface UnknowValueInferService {

    /**
     * Infer className through all Soot classes.
     * <p>
     * Given that COAL results might not extract the reflection className accuratly, Soot classes are
     * are used with COAL class set for contrast to locate the expected className .
     *
     * @param stmtKeyValues
     * @return
     */
    public Map<StmtKey, StmtValue> inferClassNameThroughAllSootMethods(Map<StmtKey, StmtValue> stmtKeyValues);

    /**
     * Infer methodName through all Soot classes.
     * <p>
     * Given that COAL results might not extract the reflection methodName accuratly, Soot classes
     * are used with COAL class set for contrast to locate the expected methodName .
     *
     * @param stmtKeyValues
     * @return
     */
    public Map<StmtKey, StmtValue> inferMethodNameThroughAllSootClasses(Map<StmtKey, StmtValue> stmtKeyValues);

    /**
     * Infer className & methodName through all Soot classes.
     * <p>
     * Given that COAL results might not extract the reflection className & methodName accuratly, Soot classes
     * are used with COAL class set for contrast to locate the expected className & methodName .
     *
     * @param stmtKeyValues
     * @return
     */
    public Map<StmtKey, StmtValue> inferClassMethodNameThroughAllSootMethods(Map<StmtKey, StmtValue> stmtKeyValues);

}
