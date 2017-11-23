/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，
 * 也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2014年 mob.com. All rights reserved.
 */
package com.example.huangweicai.mobsmsdemo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.CommonDialog;
import cn.smssdk.gui.ContactsPage;
import cn.smssdk.gui.RegisterPage;

//请注意：测试短信条数限制发送数量：20条/天，APP开发完成后请到mob.com后台提交审核，获得不限制条数的免费短信权限。
public class MainActivity extends Activity implements OnClickListener, Callback {
    // 短信注册，随机产生头像
    private static final String[] AVATARS = {
            "http://tupian.qqjay.com/u/2011/0729/e755c434c91fed9f6f73152731788cb3.jpg",
            "http://99touxiang.com/public/upload/nvsheng/125/27-011820_433.jpg",
            "http://img1.touxiang.cn/uploads/allimg/111029/2330264224-36.png",
            "http://img1.2345.com/duoteimg/qqTxImg/2012/04/09/13339485237265.jpg",
            "http://diy.qqjay.com/u/files/2012/0523/f466c38e1c6c99ee2d6cd7746207a97a.jpg",
            "http://img1.touxiang.cn/uploads/20121224/24-054837_708.jpg",
            "http://img1.touxiang.cn/uploads/20121212/12-060125_658.jpg",
            "http://img1.touxiang.cn/uploads/20130608/08-054059_703.jpg",
            "http://diy.qqjay.com/u2/2013/0422/fadc08459b1ef5fc1ea6b5b8d22e44b4.jpg",
            "http://img1.2345.com/duoteimg/qqTxImg/2012/04/09/13339510584349.jpg",
            "http://img1.touxiang.cn/uploads/20130515/15-080722_514.jpg",
            "http://diy.qqjay.com/u2/2013/0401/4355c29b30d295b26da6f242a65bcaad.jpg"
    };

    private boolean ready;
    private boolean gettingFriends;
    private Dialog pd;
    private TextView tvNum;
    private EditText ed_input_phone;
    private EditText ed_input_code;
    private Button btn_send;
    private Button btn_code_send;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ed_input_phone = findViewById(R.id.ed_input_phone);
        ed_input_code = findViewById(R.id.ed_input_code);
        btn_send = findViewById(R.id.btn_send);
        btn_code_send = findViewById(R.id.btn_code_send);

        Button btnRegist = (Button) findViewById(R.id.btn_bind_phone);
        View btnContact = findViewById(R.id.rl_contact);
        tvNum = (TextView) findViewById(R.id.tv_num);
        tvNum.setVisibility(View.GONE);
        btn_send.setOnClickListener(this);
        btn_code_send.setOnClickListener(this);
        btnRegist.setOnClickListener(this);
        btnContact.setOnClickListener(this);
        gettingFriends = false;

        if (Build.VERSION.SDK_INT >= 23) {
            int readPhone = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
            int receiveSms = checkSelfPermission(Manifest.permission.RECEIVE_SMS);
            int readSms = checkSelfPermission(Manifest.permission.READ_SMS);
            int readContacts = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            int readSdcard = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            int requestCode = 0;
            ArrayList<String> permissions = new ArrayList<String>();
            if (readPhone != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 0;
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (receiveSms != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 1;
                permissions.add(Manifest.permission.RECEIVE_SMS);
            }
            if (readSms != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 2;
                permissions.add(Manifest.permission.READ_SMS);
            }
            if (readContacts != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 3;
                permissions.add(Manifest.permission.READ_CONTACTS);
            }
            if (readSdcard != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 4;
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (requestCode > 0) {
                String[] permission = new String[permissions.size()];
                this.requestPermissions(permissions.toArray(permission), requestCode);
                return;
            }
        }
        registerSDK();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        registerSDK();
    }

    private void registerSDK() {
        // 在尝试读取通信录时以弹窗提示用户（可选功能）
        SMSSDK.setAskPermisionOnReadContact(true);

        //此APPKEY仅供测试使用，且不定期失效，请到mob.com后台申请正式APPKEY
        //配置文件已经使用正式appkey，这里只是提示作用，不需理会
//		if ("moba6b6c6d6".equalsIgnoreCase(MobSDK.getAppkey())) {
//			Toast.makeText(this, R.string.smssdk_dont_use_demo_appkey, Toast.LENGTH_SHORT).show();
//		}

        final Handler handler = new Handler(this);
        EventHandler eventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        // 注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
        ready = true;

        // 获取新好友个数
        showDialog();
        SMSSDK.getNewFriendsCount();
        gettingFriends = true;
    }

    protected void onDestroy() {
        if (ready) {
            // 销毁回调监听接口
            SMSSDK.unregisterAllEventHandler();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ready && !gettingFriends) {
            // 获取新好友个数
            showDialog();
            SMSSDK.getNewFriendsCount();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private OnSendMessageHandler osmHandler = new OnSendMessageHandler() {
        @Override
        public boolean onSendMessage(String s, String s1) {
            Log.d("TAG", "s:"+s);
            Log.d("TAG", "s1:"+s1);
            return false;
        }
    };
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send: {
                String inputCode = ed_input_code.getText().toString();
                String phoneNum = ed_input_phone.getText().toString().trim().replaceAll("\\s*", "");
                String code = "86";
                if (TextUtils.isEmpty(phoneNum)) {
                    Toast.makeText(this, "手机号码为空", Toast.LENGTH_LONG).show();
                    return;
                }
                //获取验证码
                SMSSDK.getVerificationCode(code, phoneNum, osmHandler);
            }
            break;
            case R.id.btn_code_send: {
                String inputCode = ed_input_code.getText().toString();
                String phoneNum = ed_input_phone.getText().toString().trim().replaceAll("\\s*", "");
                String code = "86";

                if (TextUtils.isEmpty(inputCode)) {
                    Toast.makeText(this, "验证码为空", Toast.LENGTH_LONG).show();
                    return;
                }
                // 验证验证码，短信平台注册行为，已经注册则回调提示
                SMSSDK.submitVerificationCode(code, phoneNum, inputCode);
            }
            break;
            case R.id.btn_bind_phone: {
                // 打开注册页面
                RegisterPage registerPage = new RegisterPage();
                registerPage.setRegisterCallback(new EventHandler() {
                    public void afterEvent(int event, int result, Object data) {
                        // 解析注册结果
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            @SuppressWarnings("unchecked")
                            HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                            String country = (String) phoneMap.get("country");
                            String phone = (String) phoneMap.get("phone");
                            // 提交用户信息
                            registerUser(country, phone);
                        }
                    }
                });
                registerPage.show(this);
            }
            break;
            case R.id.rl_contact: {
                tvNum.setVisibility(View.GONE);
                // 打开通信录好友列表页面
                ContactsPage contactsPage = new ContactsPage();
                contactsPage.show(this);
            }
            break;
        }
    }




    public boolean handleMessage(Message msg) {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }

        int event = msg.arg1;
        int result = msg.arg2;
        Object data = msg.obj;

//        if (event == SMSSDK.EVENT_SUBMIT_USER_INFO) {
//            // 短信注册成功后，返回MainActivity,然后提示新好友
//            if (result == SMSSDK.RESULT_COMPLETE) {
//                //用户资料已提交
//                Toast.makeText(this, R.string.smssdk_user_info_submited, Toast.LENGTH_SHORT).show();
//            } else {
//                ((Throwable) data).printStackTrace();
//            }
//        } else if (event == SMSSDK.EVENT_GET_NEW_FRIENDS_COUNT) {
//            if (result == SMSSDK.RESULT_COMPLETE) {
//                refreshViewCount(data);
//                gettingFriends = false;
//            } else {
//                ((Throwable) data).printStackTrace();
//            }
//        }

        //根据源码补充
        if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
            //提交验证码成功回调
            Toast.makeText(this, "提交验证码", Toast.LENGTH_SHORT).show();
        } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
            //获取验证码成功回调
            Toast.makeText(this, "获取验证码成功后的执行动作", Toast.LENGTH_SHORT).show();
        } else if (event == SMSSDK.EVENT_GET_VOICE_VERIFICATION_CODE) {
            /** 获取语音版验证码成功后的执行动作 */
            //Toast.makeText(this, "获取语音版验证码成功后的执行动作", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    // 更新，新好友个数
    private void refreshViewCount(Object data) {
        int newFriendsCount = 0;
        try {
            newFriendsCount = Integer.parseInt(String.valueOf(data));
        } catch (Throwable t) {
            newFriendsCount = 0;
        }
        if (newFriendsCount > 0) {
            tvNum.setVisibility(View.VISIBLE);
            tvNum.setText(String.valueOf(newFriendsCount));
        } else {
            tvNum.setVisibility(View.GONE);
        }
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    // 弹出加载框
    private void showDialog() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        pd = CommonDialog.ProgressDialog(this);
        pd.show();
    }

    // 提交用户信息
    private void registerUser(String country, String phone) {
        Random rnd = new Random();
        int id = Math.abs(rnd.nextInt());
        String uid = String.valueOf(id);
        String nickName = "SmsSDK_User_" + uid;
        String avatar = AVATARS[id % 12];
        SMSSDK.submitUserInfo(uid, nickName, avatar, country, phone);
    }
}
