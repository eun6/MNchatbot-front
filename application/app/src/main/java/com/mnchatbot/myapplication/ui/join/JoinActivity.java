package com.mnchatbot.myapplication.ui.join;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mnchatbot.myapplication.R;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceAPI;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceGenerator;
import com.mnchatbot.myapplication.ui.login.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinActivity extends AppCompatActivity {

    private JoinUserState joinUserState = new JoinUserState();
    private boolean validate = false;
    private Dialog enterCodeDialog;
    private AlertDialog dialog;
    private int codeEntered;
    private int codeReceived;

    //서버 통신
    private String TOKEN = getToken();
    private ServiceAPI service = ServiceGenerator.createService(ServiceAPI.class, TOKEN);
    public String getToken() {
        return TOKEN;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        final EditText emailEditText = (EditText) findViewById(R.id.editTextEmail);
        final EditText passwordEditText = (EditText) findViewById(R.id.editTextPassword);
        final EditText passwordReEditText = (EditText) findViewById(R.id.editTextPasswordCheck);
        final Button joinButton = (Button) findViewById(R.id.joinButton);
        final Button emailCheckButton = (Button) findViewById(R.id.emailCheckButton);

        // 텍스트가 변경될때마다 동작하는 TextWatcher
        TextWatcher emailAfterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validate = false;
                joinUserState.setEmail(emailEditText.getText().toString());
                if(!joinUserState.isEmailValid()) {
                    emailEditText.setError("이메일 형식이 잘못되었습니다.");
                }
            }
        };
        TextWatcher passwordAfterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                joinUserState.setPassword(passwordEditText.getText().toString());
                if(!joinUserState.isPasswordValid()) {
                    passwordEditText.setError("비밀번호는 여덟자리 이상, 영문자 대소문자, 숫자, 특수문자(!,@,#,^,&,*,(,))를 포함하여 구성해주세요.");
                }
            }
        };
        TextWatcher passwordReEnterAfterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                joinUserState.setRePassword(passwordReEditText.getText().toString());
                if(!joinUserState.isPasswordSame()) {
                    passwordReEditText.setError("비밀번호가 다릅니다.");
                }
            }
        };
        emailEditText.addTextChangedListener(emailAfterTextChangedListener);
        passwordEditText.addTextChangedListener(passwordAfterTextChangedListener);
        passwordReEditText.addTextChangedListener(passwordReEnterAfterTextChangedListener);

        // 이메일 중복 확인
        emailCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!joinUserState.isEmailValid()) {
                    emailEditText.setError("이메일 형식이 잘못되었습니다.");
                    return;
                } else {
                    validateEmail(new EmailValidationData(joinUserState.getEmail()));
                }
            }
        });

        // 회원가입 버튼
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (joinUserState.isValidData() && validate) {
                    sendEmail(new EmailValidationData(joinUserState.getEmail()));
                } else if(!validate) {
                    Toast.makeText(getApplicationContext(), "이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "이메일, 비밀번호 형식을 다시 한 번 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // 이메일 중복 확인
    private void validateEmail(EmailValidationData data) {
        service.emailValidation(data).enqueue(new Callback<JoinResponse>() {
            @Override
            public void onResponse(Call<JoinResponse> call, Response<JoinResponse> response) {
                JoinResponse result = response.body();
                // Toast.makeText(JoinActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                if (result.getCode() == 200) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("사용할 수 있는 이메일입니다.")
                            .setCancelable(false)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    dialog.show();
                    validate = true;
                } else {
                    String faileResult = "사용할 수 없는 이메일입니다.";
                    Toast.makeText(JoinActivity.this, faileResult, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JoinResponse> call, Throwable t) {
                Toast.makeText(JoinActivity.this, "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                Log.e("회원가입 에러 발생", t.getMessage());
                t.printStackTrace(); // 에러 발생시 에러 발생 원인 단계별로 출력해줌
            }
        });
    }
    // 회원가입 버튼 클릭시에 코드를 전송
    private void sendEmail(EmailValidationData data) {
        service.sendEmail(data).enqueue(new Callback<JoinResponse>() {
            @Override
            public void onResponse(Call<JoinResponse> call, retrofit2.Response<JoinResponse> response) {
                JoinResponse result = response.body();
                if (result.getCode() == 200) {
                    Toast.makeText(JoinActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    enterCodeDialog = new Dialog(JoinActivity.this);
                    enterCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    enterCodeDialog.setContentView(R.layout.email_check_dialog);
                    codeReceived = result.getData();
                    showEmailCodeEnterDialog();
                } else {
                    String faileResult = "코드 전송에 실패하였습니다. 다시 시도해주세요.";
                    Toast.makeText(JoinActivity.this, faileResult, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JoinResponse> call, Throwable t) {
                Toast.makeText(JoinActivity.this, "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                Log.e("이메일 인증 에러 발생", t.getMessage());
                t.printStackTrace(); // 에러 발생시 에러 발생 원인 단계별로 출력해줌
            }
        });
    }
    // 이메일로 전송된 코드를 입력하는 다이얼로그
    public void showEmailCodeEnterDialog() {
        enterCodeDialog.show();
        Button button = enterCodeDialog.findViewById(R.id.emailCheckButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText codeEditText = enterCodeDialog.findViewById(R.id.editTextNumberPassword);
                try {
                    codeEntered = Integer.parseInt(codeEditText.getText().toString());
                    enterCodeDialog.cancel();
                    startJoin(new JoinData(joinUserState.getEmail(),joinUserState.getPassword(),codeReceived,codeEntered));
                } catch (NumberFormatException e) {
                    Toast.makeText(JoinActivity.this, "숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // 회원가입
    private void startJoin(JoinData data) {
        service.userJoin(data).enqueue(new Callback<JoinResponse>() {
            @Override
            public void onResponse(Call<JoinResponse> call, Response<JoinResponse> response) {
                JoinResponse result = response.body();
                if (result.getCode() == 200) {
                    Toast.makeText(JoinActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    String faileResult = "회원가입에 실패하였습니다. 다시 시도해주세요.";
                    Toast.makeText(JoinActivity.this, faileResult, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JoinResponse> call, Throwable t) {
                Log.e("회원가입 에러 발생", t.getMessage());
                t.printStackTrace(); // 에러 발생시 에러 발생 원인 단계별로 출력해줌
            }
        });
    }
}
