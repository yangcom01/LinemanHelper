package com.tianjininstitute.yang.linemanhelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class LogInActivity extends AppCompatActivity {
    private EditText userName,password;
    private CheckBox rem_pw,auto_login;
    private Button btn_login;
    private String userNameValue,passwordValue;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        userName = (EditText)findViewById(R.id.et_userName);
        password = (EditText)findViewById(R.id.et_password);
        rem_pw = (CheckBox)findViewById(R.id.re_password);
        auto_login = (CheckBox)findViewById(R.id.re_auto);
        btn_login = (Button)findViewById(R.id.signIn);

        //检测是否选中自动登录，记住密码
        if(sp.getBoolean("ISCHECK", false))
        {
            //
            rem_pw.setChecked(true);
            userName.setText(sp.getString("USER_NAME", ""));
            password.setText(sp.getString("PASSWORD", ""));
            //
            if(sp.getBoolean("AUTO_ISCHECK", false))
            {
                //
                auto_login.setChecked(true);
                //
                Intent intent = new Intent(LogInActivity.this,MainActivity.class);
                LogInActivity.this.startActivity(intent);

            }
        }

        //
        btn_login.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                userNameValue = userName.getText().toString();
                passwordValue = password.getText().toString();

                if(userNameValue.equals("yang")&&passwordValue.equals("123"))
                {
                    Toast.makeText(LogInActivity.this,"登录成功", Toast.LENGTH_SHORT).show();
                    //
                    if(rem_pw.isChecked())
                    {
                        //记住密码
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("USER_NAME", userNameValue);
                        editor.putString("PASSWORD",passwordValue);
                        editor.commit();
                    }
                    Intent intent = new Intent(LogInActivity.this,MainActivity.class);
                    LogInActivity.this.startActivity(intent);
                    //finish();

                }else{

                    Toast.makeText(LogInActivity.this,"密码或账号错误", Toast.LENGTH_LONG).show();
                }

            }
        });
        rem_pw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (rem_pw.isChecked()) {

                    System.out.println("记住密码");
                    sp.edit().putBoolean("ISCHECK", true).commit();

                }else {

                    System.out.println("未记住密码");
                    sp.edit().putBoolean("ISCHECK", false).commit();

                }

            }
        });

        //
        auto_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (auto_login.isChecked()) {
                    System.out.println("自动登录");
                    sp.edit().putBoolean("AUTO_ISCHECK", true).commit();

                } else {
                    System.out.println("未设置自动登录");
                    sp.edit().putBoolean("AUTO_ISCHECK", false).commit();
                }
            }
        });
    }
}
