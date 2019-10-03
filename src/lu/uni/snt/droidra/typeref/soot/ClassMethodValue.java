package lu.uni.snt.droidra.typeref.soot;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

public class ClassMethodValue {
    /**
     * refer to class name
     */
    public String cls;

    /**
     * refer to method name
     */
    public String method;

    @Override
    public boolean equals(Object object){
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!(object instanceof ClassMethodValue)) {
            return false;
        }
        ClassMethodValue other = (ClassMethodValue) object;
        if (!StringUtils.equals(other.cls, cls)) {
            return false;
        }
        if (!StringUtils.equals(other.method, method)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.cls, this.method);
    }

}