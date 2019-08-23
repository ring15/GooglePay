package com.founq.sdk.googlepay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.founq.sdk.googlepay.billing.BillingManager;
import com.founq.sdk.googlepay.billing.BillingProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.json.JSONException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BillingProvider {

    //Google的BASE_64_ENCODED_PUBLIC_KEY，是在BillingManager.java中添加的，BASE_64_ENCODED_PUBLIC_KEY的获取，是从Google后台获取的
    private BillingManager mBillingManager;
    private BillingProvider mBillingProvider;

    private String purchaseId;

    private static final int RC_SIGN_IN = 0x01;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化的时候，链接到Google play
        mBillingManager = new BillingManager(this, new UpdateListener());
        //自己瞎写的，我觉得是这样
        mBillingProvider = this;
        //集成，DEFAULT_GAMES_SIGN_IN代表的是游戏登陆
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestServerAuthCode(getString(R.string.default_web_client_id))
                .build();
        //集成，DEFAULT_SIGN_IN代表的是普通登陆
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //获取用户信息，若没有登陆，account为null
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null){
            //静默登录
            mGoogleSignInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    handleSignInResult(task);
                }
            });
        }
    }

    private void signIn(){
        //开始登录
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            //登录结果
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            //处理登录结果，返回用户信息
            GoogleSignInAccount account = task.getResult(ApiException.class);
            //获取IDtoken，将其发送给服务器进行验证，IdToken不是AccessToken，如果我们的后端需要token，就传给他AccessToken
            String idToken = account.getIdToken();
            authCode = account.getServerAuthCode();
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出用户，此代码清除哪个用户已连接到该应用，要再次登录，用户必须再次选择其账户
     */
    private void signOut(){
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    /**
     * 断开账户
     */
    private void revokeAccess(){
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    //这个json文件是要从控制台获取的
    String CLIENT_SECRET_FILE = "/path/to/client_secret.json";

    String authCode;

    private void checkToken(){
        try {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    JacksonFactory.getDefaultInstance(), new FileReader(CLIENT_SECRET_FILE));
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientSecrets.getDetails().getClientId(),
                    clientSecrets.getDetails().getClientSecret(),
                    authCode,
                    "")//Specify the same redirect URI that you use with your web app. If you don't have a web version of your app, you can specify an empty string.
                    .execute();
            String accessToken = tokenResponse.getAccessToken();

            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            String email = payload.getEmail();//....获取用户信息的
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 购买事件
     * purchaseId：产品ID
     */
    public void googlePay(String purchaseId) {
        try {
            SkuDetails skuDetails = new SkuDetails("");
            purchaseId = skuDetails.getSku();
            mBillingProvider.getBillingManager().initiatePurchaseFlow(skuDetails);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //第二个参数是指定用户正在升级或降级的SKU
//        mBillingProvider.getBillingManager().initiatePurchaseFlow(purchaseId, new ArrayList<String>(), BillingClient.SkuType.INAPP);
        //这个就是购买操作
//        mBillingProvider.getBillingManager().initiatePurchaseFlow(purchaseId, BillingClient.SkuType.INAPP);
        //查询操作（skuList中添加的是你的产品ID。SkuType是产品类型。SkuType.INAPP(用于一次性产品或奖励产品)或SkuType.SUBS(订阅)）
        //查询操作（第一个参数时产品类型，第二个中添加产品id，第三个回调）
//        mBillingProvider.getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.INAPP, new ArrayList<String>(), new SkuDetailsResponseListener() {
//            @Override
//            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
//
//            }
//        });
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
