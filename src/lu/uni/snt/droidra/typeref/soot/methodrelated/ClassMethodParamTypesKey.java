package lu.uni.snt.droidra.typeref.soot.methodrelated;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

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
     *Comma separated string
     */
    public String paramTypes;

    @Override
    public boolean equals(Object object){
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!(object instanceof ClassMethodParamTypesKey)) {
            return false;
        }
        ClassMethodParamTypesKey other = (ClassMethodParamTypesKey) object;
        if (!StringUtils.equals(other.cls, cls)) {
            return false;
        }
        if (!StringUtils.equals(other.method, method)) {
            return false;
        }
        if (!StringUtils.equals(other.paramTypes, paramTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.cls, this.method, this.paramTypes);
    }
}