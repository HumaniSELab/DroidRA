package lu.uni.snt.droidra.typeref.soot;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by sun on 2019/9/27.
 */
public class ClassParamTypesKey {

    /**
     * refer to class name
     */
    public String cls;

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
        if (!(object instanceof ClassParamTypesKey)) {
            return false;
        }
        ClassParamTypesKey other = (ClassParamTypesKey) object;
        if (!StringUtils.equals(other.cls, cls)) {
            return false;
        }
        if (!StringUtils.equals(other.paramTypes, paramTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.cls, this.paramTypes);
    }
}
