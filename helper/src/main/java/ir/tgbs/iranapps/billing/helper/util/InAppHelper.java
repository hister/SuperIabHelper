package ir.tgbs.iranapps.billing.helper.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import java.util.ArrayList;

import ir.tgbs.iranapps.billing.helper.interfaces.BuyProductListener;
import ir.tgbs.iranapps.billing.helper.interfaces.ConsumeListener;
import ir.tgbs.iranapps.billing.helper.interfaces.LoginListener;
import ir.tgbs.iranapps.billing.helper.interfaces.PurchasesListener;
import ir.tgbs.iranapps.billing.helper.interfaces.SkuDetailListener;
import ir.tgbs.iranapps.billing.helper.model.AppStore;
import ir.tgbs.iranapps.billing.helper.model.IabService;

/**
 * <h1>IranApps in-app billing helper class</h1> <p>
 * you can use this helper to make all your in-app billing requests to iranapps billing service.
 *
 * @author Shima Zeinali
 * @author Khaled Bakhtiari
 * @since 2015-02-14
 */
public class InAppHelper {
    /**
     * current version of IranApps in-app billing service
     */
    public static final int IAB_VERSION = 3;
    /**
     * billing type inapp
     */
    public static final String TYPE_INAPP = "inapp";
    /**
     * packageName of the application using helper to make in-app requests to IranApps
     */
    public static String PACKAGE_NAME;

    /**
     * IranApps in-app billing connection
     */
    IabService iabService;

    /**
     * current on the fly request(this request is waiting for its response from onActivityResult)
     */
    InAppRequestHelper pendingRequest;

    /**
     * Service Connection used to bind to IranApps in-app billing service
     */
    ServiceConnection inAppConnection;

    AppStore appStore;

    /**
     * creates an instance of InAppHelper with the given parameters.<br>
     * InAppHelper uses the given activity to connect to IranApps billing service.
     * <p>
     * note that you must call methods of InAppHelper that relate to Billing service after the connection is established.<br>
     * you will be informed of the establishment of the connection using the callback of
     * {@link ir.tgbs.iranapps.billing.helper.util.InAppHelper.InAppHelperListener#onConnectedToIABService()}
     * </p>
     * you must always call {@link ir.tgbs.iranapps.billing.helper.util.InAppHelper#onActivityResult(int, int, android.content.Intent)}
     * from your {@link android.app.Activity#onActivityResult(int, int, android.content.Intent)}
     * otherwise the helper wouldn't work properly <p>
     * you also should call {@link InAppHelper#onActivityDestroy(Activity)} for your {@link android.app.Activity#onDestroy()}
     * for the helper to work properly
     *
     * @param context             used to communicate to IranApps billing service
     * @param inAppHelperListener listener used to notice you when InAppHelper connects to IranApps billing service
     */
    public InAppHelper(Context context, AppStore appStore, final InAppHelperListener inAppHelperListener) {
        PACKAGE_NAME = context.getPackageName();
        this.appStore = appStore;

        //bind to IranApps billing service
        Intent serviceIntent = new Intent(appStore.intentAction);
        serviceIntent.setPackage(appStore.packageName);
        inAppConnection = new InAppServiceConnection(inAppHelperListener);
        boolean canConnect = context.getApplicationContext().bindService(serviceIntent, inAppConnection, Context.BIND_AUTO_CREATE);
        if (!canConnect) {
            final InAppError inAppError;
            if (!isIranAppsInstalled(context)) {
                inAppError = InAppError.BILLING_RESPONSE_IRANAPPS_NOT_AVAILABLE;
            } else {
                inAppError = InAppError.LOCAL_CANT_CONNECT_TO_IAB_SERVICE;
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    inAppHelperListener.onCantConnectToIABService(inAppError);
                }
            });
        }
    }

    /**
     * checks whether IranApps is installed on the device
     *
     * @return true if IranApps is installed on the device otherwise false.
     */
    private boolean isIranAppsInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(appStore.packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * @return true if InAppHelper is connected to IranApps billing service otherwise returns false
     */
    public boolean isConnectedToService() {
        return iabService != null;
    }

    /**
     * checks if its allowed to communicate with IranApps billing service<br>
     * if so gets the details of the skus and returns them on the given listener
     * and if isn't allowed to communicate with the service calls {@link SkuDetailListener#onFailedGettingSkus(InAppError)} <br>
     * with {@link ir.tgbs.iranapps.billing.helper.util.InAppError#LOCAL_CANT_CONNECT_TO_IAB_SERVICE} error<br>
     *
     * @param skus     list of skusId to get their information<br>
     * @param listener a callback listener to inform response of this method
     */
    public SkuDetailGetter getSkuDetails(ArrayList<String> skus, SkuDetailListener listener) {
        SkuDetailGetter skuDetailGetter = new SkuDetailGetter(iabService, skus, listener);
        skuDetailGetter.start();
        return skuDetailGetter;
    }

    /**
     * checks if its allowed to communicate with IranApps billing service<br>
     * if so gets the details of the purchased items and returns them on the given listener
     * and if isn't allowed to communicate with the service calls {@link PurchasesListener#onFailedGettingPurchases(InAppError)}<br>
     * with {@link InAppError#LOCAL_HELPER_NOT_CONNECTED_TO_SERVICE} error<br>
     *
     * @param listener a callback listener to inform response of this method
     * @see #getPurchases(PurchasesListener, String)
     */
    public GetPurchasesHelper getPurchases(PurchasesListener listener) {
        GetPurchasesHelper getPurchasesHelper = new GetPurchasesHelper(iabService, listener, null);
        getPurchasesHelper.start();
        return getPurchasesHelper;
    }

    /**
     * checks if its allowed to communicate with IranApps billing service<br>
     * if so gets the details of the purchased items and returns them on the given listener
     * and if isn't allowed to communicate with the service calls {@link PurchasesListener#onFailedGettingPurchases(InAppError)}<br>
     * with {@link InAppError#LOCAL_HELPER_NOT_CONNECTED_TO_SERVICE} error<br>
     *
     * @param listener          a callback listener to inform response of this method<br>
     * @param continuationToken to be set as null for the first call, if the number of owned skus are too many,
     *                          a continuationToken is returned in the response. This method can
     *                          be called again with the continuation token to get the next set of owned skus.
     */
    public GetPurchasesHelper getPurchases(PurchasesListener listener, String continuationToken) {
        GetPurchasesHelper getPurchasesHelper = new GetPurchasesHelper(iabService, listener, continuationToken);
        getPurchasesHelper.start();
        return getPurchasesHelper;
    }

    /**
     * this method starts buy Process,
     * informs response in given listener<br>
     *
     * @param productId        id(sku) of the requested product to be purchased
     * @param consumable       whether this purchase can be consumed or not
     * @param developerPayload optional parameter that can later be used to check the validity of the purchase data
     * @param listener         purchase callback listener
     */
    public BuyProductHelper buyProduct(Activity activity, final String productId, final String developerPayload, boolean consumable, BuyProductListener listener) {
        BuyProductHelper buyProductHelper = new BuyProductHelper(iabService, productId, consumable, developerPayload, listener);
        buyProductHelper.start(activity);
        pendingRequest = buyProductHelper;
        return buyProductHelper;
    }

    /**
     * this method starts login Process,
     * informs response in given listener<br>
     *
     * @param loginListener a callback listener to inform response of this method
     */
    public InAppLoginHelper loginUser(Activity activity, LoginListener loginListener) {
        InAppLoginHelper loginHelper = new InAppLoginHelper(iabService, loginListener);
        loginHelper.start(activity);
        pendingRequest = loginHelper;
        return loginHelper;
    }

    /**
     * this method starts consume product Process,
     * informs response in given listener<br>
     *
     * @param purchaseToken   token of the purchase that needs to be consumed
     * @param consumeListener A callback listener to inform response of this method
     */
    public ConsumeHelper consumeProduct(String purchaseToken, ConsumeListener consumeListener) {
        ConsumeHelper consumeHelper = new ConsumeHelper(iabService, purchaseToken, consumeListener);
        consumeHelper.start();
        return consumeHelper;
    }

    /**
     * checks whether user is login in IranApps or not
     *
     * @return true if user login in IranApps
     * @throws android.os.RemoteException on connection failure with billing service
     */
    public boolean isUserLogin() throws RemoteException {
        Bundle loginResponse = iabService.getLoginIntent(IAB_VERSION, PACKAGE_NAME);
        return loginResponse.getBoolean(InAppKeys.USER_IS_LOGIN);
    }

    /**
     * checks support for billing API version, package and in-app type<br>
     *
     * @return true if support billing API version
     */
    public boolean isBillingSupported() {
        try {
            return iabService.isBillingSupported(IAB_VERSION, PACKAGE_NAME, TYPE_INAPP) == InAppKeys.RESPONSE_OK;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * this method must be called inside {@link android.app.Activity#onDestroy()} <br>
     * it disconnects the helper from IranApps in-app billing service
     */
    public void onActivityDestroy(Activity activity) {
        if (activity.isFinishing())
            activity.getApplicationContext().unbindService(inAppConnection);
    }

    /**
     * this method should be called inside
     * {@link android.app.Activity#onActivityResult(int, int, android.content.Intent)}
     * or if its in a fragment
     * {@link android.app.Fragment#onActivityResult(int, int, android.content.Intent)} <p>
     * handles the response of the request that is waiting for the response.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (pendingRequest != null) {
            pendingRequest.onActivityResult(requestCode, resultCode, data);
            pendingRequest = null;
        }
    }

    /**
     * listener for helper's connection to IranApps in-app billing service, <br>
     * using it will inform you on the current state of the connection
     */
    public interface InAppHelperListener {
        /**
         * this method is called after the connection to billing service is established. <br>
         * this method is going to be called only once.
         */
        void onConnectedToIABService();

        /**
         * this method is called when helper can't failed to connect to billing service
         *
         * @param error the error that caused the failure of helper to connect to billing service
         */
        void onCantConnectToIABService(InAppError error);

        /**
         * after the connection to billing service is established,
         * whenever the connection is lost to billing service this method is called. <br>
         * this method is going to be called only once.
         */
        void onConnectionLost();
    }

    /**
     * helper's service connection to IranApps in-app billing service
     */
    private class InAppServiceConnection implements ServiceConnection {
        private InAppHelperListener inAppHelperListener;
        private boolean connectedSent;
        private boolean disconnectedSent;

        public InAppServiceConnection(InAppHelperListener inAppHelperListener) {
            this.inAppHelperListener = inAppHelperListener;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //connection is established make IranApps service and inform the listener
            iabService = new IabService(appStore, service);
            if (!connectedSent) {
                inAppHelperListener.onConnectedToIABService();
                connectedSent = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //connection to service is lost inform the listener
            iabService = null;
            inAppHelperListener.onConnectionLost();
            if (!disconnectedSent) {
                inAppHelperListener.onConnectionLost();
                disconnectedSent = true;
            }
        }
    }
}
