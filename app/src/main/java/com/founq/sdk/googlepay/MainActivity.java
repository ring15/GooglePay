package com.founq.sdk.googlepay;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.founq.sdk.googlepay.billing.BillingManager;
import com.founq.sdk.googlepay.billing.BillingProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BillingProvider {

    //Google的BASE_64_ENCODED_PUBLIC_KEY，是在BillingManager.java中添加的，BASE_64_ENCODED_PUBLIC_KEY的获取，是从Google后台获取的
    private BillingManager mBillingManager;
    private BillingProvider mBillingProvider;

    private String purchaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBillingManager = new BillingManager(this, new UpdateListener());
        //自己瞎写的，我觉得是这样
        mBillingProvider = this;
    }

    /**
     * 购买事件
     * purchaseId：产品ID
     */
    public void googlePay(String purchaseId) {
        mBillingProvider.getBillingManager().initiatePurchaseFlow(purchaseId, new ArrayList<String>(), BillingClient.SkuType.INAPP);
    }

    private class UpdateListener implements BillingManager.BillingUpdatesListener {

        @Override
        public void onBillingClientSetupFinished() {
            //通过商品ID，去查询Google后台是否有该ID的商品
            final List<String> skuList = new ArrayList();
            skuList.add(purchaseId);//purchaseId 哪来的
            mBillingProvider.getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.INAPP, skuList,
                    new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                            if (responseCode != BillingClient.BillingResponse.OK){

                            }else if (skuDetailsList != null && skuDetailsList.size() > 0){
                                for (SkuDetails details :skuDetailsList){
                                    //获取到所查商品信息
                                }
                            }else {
                                //没有要消耗的产品
                            }
                        }
                    });
        }

        @Override
        public void onConsumeFinished(String token, int result) {
            if (result == BillingClient.BillingResponse.OK){
                //消耗成功
            }else {
                //消耗失败
            }
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchases) {
            for (Purchase purchase : purchases){
                //拿到订单信息，做自己的处理，发生到服务端验证订单信息，然后去消耗

                //购买成功，拿着令牌去消耗
                mBillingProvider.getBillingManager().consumeAsync(purchase.getPurchaseToken());
            }
        }
    }

    @Override
    public BillingManager getBillingManager() {
        return mBillingManager;
    }

    @Override
    public boolean isPremiumPurchased() {
        return false;
    }

    @Override
    public boolean isGoldMonthlySubscribed() {
        return false;
    }

    @Override
    public boolean isTankFull() {
        return false;
    }

    @Override
    public boolean isGoldYearlySubscribed() {
        return false;
    }
}
