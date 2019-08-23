//package com.founq.sdk.googlepay;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//import android.util.Log;
//
//import com.founq.sdk.googlepay.util.IabHelper;
//import com.founq.sdk.googlepay.util.IabResult;
//import com.founq.sdk.googlepay.util.Inventory;
//import com.founq.sdk.googlepay.util.Purchase;
//
//public class GooglePayActivity extends AppCompatActivity {
//
//    private String TAG = "GooglePayActivity";
//
//    private IabHelper mIabHelper;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_google_pay);
//        //初始化
//        //TODO 设置自己从Google控制台得到的公钥
//        mIabHelper = new IabHelper(this, "");
//        //调试模式
//        mIabHelper.enableDebugLogging(true);
//        Log.d(TAG, "Starting setup.");
//        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//            @Override
//            public void onIabSetupFinished(IabResult result) {
//                Log.d(TAG, "Setup finished.");
//                if (!result.isSuccess()){
//                    Log.d(TAG, "Setup fail.");
//                    return;
//                }
//                if (mIabHelper == null){
//                    Log.d(TAG, "Setup fail.");
//                    return;
//                }
//                Log.d(TAG, "Setup success.");
//            }
//        });
//
//        //在合适的地方调用购买，应先确保用户没有存在这个商品的购买（买了但是没有消耗）
//        //参数全都不对, sku应该是商品id
////        mIabHelper.launchPurchaseFlow(this, sku, RC_REQUEST, mPurchaseFinishedListener, extra);
//
//        //用户购买成功后，如果是可重复购买的商品，应该立刻将这个商品消耗掉，以及在购买之前应确保用户不存在这个商品，如果存在就调用消耗商品的接口去将商品消耗掉
//        //消耗商品，消耗商品的前提一定是已经获得商品了，所以参数很好获得，onIabPurchaseFinished的返回结果就可以了
////        mIabHelper.consumeAsync(purchase, mConsumeFinishedListener);
//    }
//
//    private IabHelper.QueryInventoryFinishedListener mQueryInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
//        @Override
//        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
//            Log.d(TAG, "查询库存完成.");
//            if (mIabHelper == null){
//                Log.d(TAG, "查询库存失败.");
//                return;
//            }
//
//            if (result.isFailure()){
//                Log.d(TAG, "查询库存失败.");
//                return;
//            }
//            Log.d(TAG, "查询库存成功.");
//
//            //TODO 查询商品id
//            Purchase gasPurchase = inv.getPurchase("");
//            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)){
//
//            }
//        }
//    };
//
//    private boolean verifyDeveloperPayload(Purchase gasPurchase) {
//        String payload = gasPurchase.getDeveloperPayload();
//
//        /*
//         * TODO: verify that the developer payload of the purchase is correct. It will be
//         * the same one that you sent when initiating the purchase.
//         *
//         * WARNING: Locally generating a random string when starting a purchase and
//         * verifying it here might seem like a good approach, but this will fail in the
//         * case where the user purchases an item on one device and then uses your app on
//         * a different device, because on the other device you will not have access to the
//         * random string you originally generated.
//         *
//         * So a good developer payload has these characteristics:
//         *
//         * 1. If two different users purchase an item, the payload is different between them,
//         *    so that one user's purchase can't be replayed to another user.
//         *
//         * 2. The payload must be such that you can verify it even when the app wasn't the
//         *    one who initiated the purchase flow (so that items purchased by the user on
//         *    one device work on other devices owned by the user).
//         *
//         * Using your own server to store and verify developer payloads across app
//         * installations is recommended.
//         */
//
//        return true;
//    }
//
//    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
//        @Override
//        public void onConsumeFinished(Purchase purchase, IabResult result) {
//            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
//            if (result.isSuccess()) {
//                Log.d(TAG, "Consumption successful. Provisioning.");
//            }
//            else {
//                Log.d(TAG,"Error while consuming: " + result);
//            }
//        }
//    };
//
//    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
//        @Override
//        public void onIabPurchaseFinished(IabResult result, Purchase info) {
//            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + info);
//            if (result.isFailure()){
//                Log.d(TAG,"Error purchasing: " + result);
//                return;
//            }
//            Log.d(TAG, "Purchase successful.");
//
//            //模拟检测 public key
//            //购买成功后，应该将购买返回的信息发送到自己的服务端，自己的服务端再去利用public key去验签
////            checkPk(info);
//
//        }
//    };
//
//    /**
//     * 查询库存
//     */
//    private void queryInventory(){
//        Log.e(TAG, "Query inventory start");
//        try {
//            mIabHelper.queryInventoryAsync(mListener);
//        } catch (IabHelper.IabAsyncInProgressException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 查询库存的回调
//     */
//    private IabHelper.QueryInventoryFinishedListener mListener = new IabHelper.QueryInventoryFinishedListener() {
//        @Override
//        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
//            Log.e(TAG, "Query inventory finished.");
//            if (result.isFailure()){
//                Log.e(TAG, "Failed to query inventory: " + result);
//                return;
//            }
//            Log.e(TAG, "Query inventory was successful.");
//            //TODO 查询商品id
//            if (inv.hasPurchase("")){
//                //库存存在用户购买的产品，先去消耗
//            }else {
//                //库存不存在
//            }
//        }
//    };
//}
