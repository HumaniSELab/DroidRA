package lu.uni.snt.droidra.typeref.soot.fieldrelated;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

public class FieldTypesValue {

    /**
     * refer to filed type
     */
    public String fieldType;

    /**
     * refer to filed name
     */
    public String fieldName;

    @Override
    public boolean equals(Object object){
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!(object instanceof FieldTypesValue)) {
            return false;
        }
        FieldTypesValue other = (FieldTypesValue) object;
        if (!StringUtils.equals(other.fieldName, fieldName)) {
            return false;
        }
        if (!StringUtils.equals(other.fieldType, fieldType)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.fieldName, this.fieldType);
    }
}