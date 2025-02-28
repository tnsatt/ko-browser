package com.xlab.vbrowser.z.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

//import androidx.appcompat.app.AppCompatActivity;

//import com.andrognito.pinlockview.IndicatorDots;
//import com.andrognito.pinlockview.PinLockListener;
//import com.andrognito.pinlockview.PinLockView;
import com.libs.module.AESEncryption;
import com.libs.module.KeyHelper;
import com.libs.module.PasswordUtil;
import com.xlab.vbrowser.R;
import com.xlab.vbrowser.z.PIN;
import com.xlab.vbrowser.z.Z;

import javax.crypto.SecretKey;

public class PinLockActivity extends AppCompatActivity {
    public final static int CREATE_MODE = 1;
    public final static String CREATE_PIN = "CREATE_PIN";
    FrameLayout container;
//    PinLockView pinLockView;
    Button createPin;
    TextView titleView;
    TextView statusView;
    SharedPreferences pref;
    int mode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        String action = getIntent().getAction();
        mode = action!=null && action.equals(CREATE_PIN)?CREATE_MODE:0;

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        container = (FrameLayout) findViewById(R.id.pin_lock_container);
        createPin = (Button) findViewById(R.id.createPin);
        titleView = (TextView) findViewById(R.id.title);
        statusView = (TextView) findViewById(R.id.status);
//        pinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
//        IndicatorDots mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
//        pinLockView.attachIndicatorDots(mIndicatorDots);
//        pinLockView.resetPinLockView();

        if(mode == CREATE_MODE){
            setCreateMode();
        }else {
            setLock(this);
        }
    }
    public void stat(String e){
        stat(e, -1);
    }
    public void stat(String e, int color){
        statusView.setText(e==null?"":e);
        if(color!=-1) statusView.setTextColor(color);
    }
    public String setEncodedPin(String pin) throws Exception {
        SecretKey key = KeyHelper.generateKey();
        String enc = getEncodedPin(pin, key);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("secretKey", KeyHelper.secretKeyToString(key));
        edit.putString("encodedPin", enc);
        edit.commit();
        return enc;
    }
    public String getEncodedPin(String pin, SecretKey key) throws Exception {
//        String enc = AESEncryption.encryptFix(pin, key);
        String enc = PasswordUtil.hashPassword(pin, KeyHelper.secretKeyToString(key));
        return enc;
    }
    public Boolean isMatchPin(String pin) throws Exception {
        String key = pref.getString("secretKey", null);
        if(key == null) return false;
        String enc = pref.getString("encodedPin", null);
        if(enc == null) return false;
        String pinenc = getEncodedPin(pin, KeyHelper.stringToSecretKey(key));
        return pinenc.equals(enc);
    }
    public void showCreatePinButton(String pin){
        createPin.setText("Create PIN");
        createPin.setVisibility(View.VISIBLE);
        createPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setEncodedPin(pin);
                    setResult(RESULT_OK);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    stat("Create Failed", Color.RED);
                }
            }
        });
    }
    public void setCreateMode(){
        titleView.setText("Create PIN");
//        pinLockView.setPinLockListener(new PinLockListener() {
//            @Override
//            public void onComplete(String pin) {
//                showCreatePinButton(pin);
//            }
//
//            @Override
//            public void onEmpty() {
//                createPin.setVisibility(View.INVISIBLE);
//                stat(null);
//            }
//
//            @Override
//            public void onPinChange(int pinLength, String intermediatePin) {
//                if (pinLength >= 4) {
//                    showCreatePinButton(intermediatePin);
//                }else{
//                    createPin.setVisibility(View.INVISIBLE);
//                    stat(null);
//                }
//            }
//        });
    }
    public void setLock(Activity context){
        titleView.setText("Auth");
//        String pincode = pref.getString("pin", null);
//        if(pincode == null || !pincode.matches("^\\d+$")) pincode = PIN.PIN;
//        String finalPincode = pincode;
//        pinLockView.setPinLockListener(new PinLockListener() {
//            @Override
//            public void onComplete(String pin) {
//                try {
//                    if (isMatchPin(pin)) {
//                        setResult(RESULT_OK);
//                        finish();
//                    } else {
//                        stat("Not Match", Color.RED);
//                        (new Handler(context.getMainLooper())).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        }, 400);
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                    stat("Failed", Color.RED);
//                }
//            }
//
//            @Override
//            public void onEmpty() {
//                stat(null);
//            }
//
//            @Override
//            public void onPinChange(int pinLength, String intermediatePin) {
//                if(pinLength >= 4){
//
//                }else{
//                    stat(null);
//                }
//            }
//        });
    }
    @Override
    public void onBackPressed() {
        if(mode == CREATE_MODE){
            finish();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        moveTaskToBack(false); // Prevent activity from going to the background
    }
}