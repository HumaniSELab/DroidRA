package lu.uni.snt.droidra.util;

import org.apache.commons.lang3.StringUtils;
import soot.SootClass;

public class ApplicationClassFilter {

    /**
     *
     * @param sootClass
     * @return
     */
    public static boolean isApplicationClass(SootClass sootClass){
        return isApplicationClass(sootClass.getPackageName());
    }

    /**
     *
     * @param sootClass
     * @return
     */
    public static boolean isApplicationClass(String clsName) {
        if (StringUtils.isBlank(clsName)) {
            return false;
        }
        if (clsName.startsWith("com.google.")
                || clsName.startsWith("soot.")
                || clsName.startsWith("android.")
                || clsName.startsWith("com.android.")
                || clsName.startsWith("androidx.")
                || clsName.startsWith("java.")
                || clsName.startsWith("com.facebook.")
                || clsName.startsWith("org.apache.")
        ) {
            return false;
        }
        return true;
    }

    public static boolean isAndroidSystemAPI(String className) {
        return className.startsWith("<android.")
                || className.startsWith("<com.android.")
                || className.startsWith("<androidx.");
    }
}