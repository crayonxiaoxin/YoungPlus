package com.ormediagroup.youngplus.lau;

/**
 * Created by Lau on 2019/2/15.
 */

public class API {
    private static String DOMAIN = "http://youngplus.com.hk";

    private static String api(String str) {
        return DOMAIN + "/" + str + "/";
    }

    public static String API_TEST = api("lau-test-2");

    public static String API_GET_SERVICES = api("app-get-services");
    public static String API_GET_SERVICE_DETAIL = api("app-get-service-detail");
    public static String API_GET_REPORT = api("app-get-report");
    public static String API_GET_SCHEDULE = api("app-get-schedule");

    public static String API_BOOKING = api("app-booking");

    public static String API_GET_PROMOTION = api("app-get-promotions");
    public static String API_ADD_PROMOTION = api("app-promotion");

    public static String API_LOGIN = api("app-login");
    public static String API_REGISTER = api("app-register");
    public static String API_RESET_PASSWORD = api("app-reset-pass");

    public static String API_REPORT_DISEASE_RISK = api("disease-risk");

    public static String ACTION_ALARM_ALERT = "com.ormediagroup.youngplus.action.alertsystem";
    public static String ACTION_UPLOAD = "com.ormediagroup.youngplus.action.UPLOAD";
}
