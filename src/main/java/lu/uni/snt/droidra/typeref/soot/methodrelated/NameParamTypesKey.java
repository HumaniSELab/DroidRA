package lu.uni.snt.droidra.typeref.soot.methodrelated;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by sun on 2019/9/29.
 */
public class NameParamTypesKey {
    /**
     * refer to methodName/fieldName
     */
    public String name;

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
        if (!(object instanceof NameParamTypesKey)) {
            return false;
        }
        NameParamTypesKey other = (NameParamTypesKey) object;
        if (!StringUtils.equals(other.name, name)) {
            return false;
        }
        if (!StringUtils.equals(other.paramTypes, paramTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name, this.paramTypes);
    }
}
