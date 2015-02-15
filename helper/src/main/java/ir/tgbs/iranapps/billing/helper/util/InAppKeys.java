package ir.tgbs.iranapps.billing.helper.util;

/**
 * @author Shima Zeinali
 * @author Khaled Bakhtiari
 * @since 2015-02-14
 */
public class InAppKeys {
    public static final String
            IRANAPPS_PACKAGE_NAME = "ir.tgbs.android.iranapp",
            CAFEBAZAAR_PACKAGE_NAME = "com.farsitel.bazaar",
            GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending",
            RESPONSE_CODE = "RESPONSE_CODE",
            DETAILS_LIST = "DETAILS_LIST",
            ITEM_ID_LIST = "ITEM_ID_LIST",
            USER_IS_LOGIN = "LOGIN_USER",
            BUY_INTENT = "BUY_INTENT",
            LOGIN_INTENT = "LOGIN_INTENT",
            INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA",
            INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE",
            INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST",
            INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST",
            INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST",
            INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    public static final int
            RESPONSE_OK = 0,
            BILLING_RESPONSE_RESULT_USER_CANCELED = 1,
            BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3,
            BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4,
            BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5,
            BILLING_RESPONSE_RESULT_ERROR = 6,
            BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7,
            BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8,
            BILLING_RESPONSE_USER_NOT_LOGIN = 9,
            BILLING_RESPONSE_IRANAPPS_NOT_AVAILABLE = 10,
            LOCAL_CANT_CONNECT_TO_IAB_SERVICE = 11,
            LOCAL_HELPER_NOT_CONNECTED_TO_SERVICE = 12,
            LOCAL_EXCEPTION = 13;
}
