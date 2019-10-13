package lu.uni.snt.droidra.service;


import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtValue;

import java.util.Map;

public interface CoalResultAccuracyVerifyService {

    /**
     * Check if COAL results matches Soot className and methodName.
     *
     * @param stmtKeyValues
     * @return matched Map<StmtKey, StmtValue>
     */
    public Map<StmtKey, StmtValue> verifyCoalResult(Map<StmtKey, StmtValue> stmtKeyValues);

}
