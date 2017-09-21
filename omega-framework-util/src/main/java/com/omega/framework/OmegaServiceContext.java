package com.omega.framework;

public class OmegaServiceContext {

    public static final String GRAY_TAG_COOKIE_NAME = "X_GRAY_TAG";
    public static final String GRAY_TAG_ENV_NAME = "ZANE_GRAY_TAG";

    private static String grayTag;

    public static String getGrayTag() {
        return grayTag;
    }

    public static void setGrayTag(String tag) {
        grayTag = tag;
    }

}
