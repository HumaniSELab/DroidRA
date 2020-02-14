package lu.uni.snt.droidra.util;

import org.apache.commons.lang3.StringUtils;
import soot.Type;

import java.util.List;
import java.util.stream.Collectors;

public class TypeConversionUtil {

    private static final String NONE_STRING = "NONE_STRING";

    public static String convertSootParamtypes2String(List<Type> parameterTypes){
        if(null == parameterTypes || parameterTypes.size() == 0){
            return NONE_STRING;
        }

        List<String> parameterTypeList = parameterTypes.stream().map(parameterType -> {
            return parameterType.toString();
        }).collect(Collectors.toList());

       return StringUtils.join(parameterTypeList.toArray(), ",");
    }
}