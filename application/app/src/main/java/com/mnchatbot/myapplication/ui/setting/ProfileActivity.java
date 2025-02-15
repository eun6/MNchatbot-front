/*
[내 프로필 화면] 로그아웃/비밀번호 변경/회원 탈퇴를 보여주고 각 화면으로 연결함.*/

package com.mnchatbot.myapplication.ui.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mnchatbot.myapplication.R;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceGenerator;
import com.mnchatbot.myapplication.ui.join.PasswordChangeActivity;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceAPI;
import com.mnchatbot.myapplication.ui.login.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends SettingActivity {
    private Context context;
    private TextView ID, pwchange, logout, deleteinfo;
    private SharedPreferences preferences, pre2;

    //서버통신
    public String getToken() {
        preferences = context.getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = preferences.getString("TOKEN", null);
        return token;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_profile);

        context = getApplicationContext();

        ID = findViewById(R.id.userID);
        pwchange = findViewById(R.id.pwchange);
        logout = findViewById(R.id.logout);
        deleteinfo = findViewById(R.id.deleteinfo);

        calluserInfo();

        //비밀번호 변경
        pwchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //비밀번호 재설정 화면으로 넘어가기
                Intent intent = new Intent(getApplicationContext(), PasswordChangeActivity.class);
                startActivity(intent);
            }
        });
        
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그인 화면으로 돌아가기
                pre2 = context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pre2.edit();
                editor.clear();
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        //회원 탈퇴
        deleteinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder name = new AlertDialog.Builder(ProfileActivity.this);
                name.setTitle("회원탈퇴");
                name.setMessage("정말로 탈퇴하시겠습니까?");

                name.setPositiveButton("탈퇴", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        ProfileDelete();
                    }
                });
                name.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
                name.show();
            }
        });
    }
    
    //회원 탈퇴
    private void ProfileDelete() {
        ServiceAPI service = ServiceGenerator.createService(ServiceAPI.class, getToken());
        String userID = ID.getText().toString();
        service.deletePost(userID).enqueue(new Callback<PetProfileResponse>() {
            @Override
            public void onResponse(Call<PetProfileResponse> call, Response<PetProfileResponse> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    startActivity(intent);
                    ProfileActivity.this.finish();
                } else { Log.d("response 실패", "404");}
            }

            @Override
            public void onFailure(Call<PetProfileResponse> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("알림")
                        .setMessage("잠시 후에 다시 시도해주세요.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }
    public void calluserInfo(){
        ServiceAPI service = ServiceGenerator.createService(ServiceAPI.class, getToken());
        service.GetmemberEmail().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful()) {
                    ID.setText(response.body().memberEmail);
                } else { Log.d("response 실패", "404");}
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("알림")
                        .setMessage("잠시 후에 다시 시도해주세요.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });


    }
}
