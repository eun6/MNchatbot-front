/*
[펫 추가 화면] 새로운 펫 추가.
축종, 품종, 이름, 나이, 성별, 중성화여부의 내용을 설정함. */

package com.mnchatbot.myapplication.ui.petSelect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mnchatbot.myapplication.R;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceAPI;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceGenerator;
import com.mnchatbot.myapplication.ui.setting.PetProfileResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPetActivity extends AppCompatActivity {
    private MaterialButtonToggleGroup Gendertoggle, Neuteringtoggle;
    private MaterialButton man, woman, NeuteringYes, NeuteringNo;
    private String CATDOG = "DOG";
    private ImageView petprofile;
    private TextView petAge;
    private EditText petBreed,petNickName;
    private Button btnAge, btnSave, btnCancel, selectCatButton, selectDogButton;

    int count = 0;
    private SharedPreferences preferences;

    //서버통신
    public String getToken() {
        preferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = preferences.getString("TOKEN", null);
        return token;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_newpetprofile);

        petprofile = findViewById(R.id.pic);
        btnAge = findViewById(R.id.btnAge);
        btnSave = findViewById(R.id.btnAddPetSave);
        btnCancel = findViewById(R.id.btnAddPetCancel);
        selectCatButton = findViewById(R.id.selectCat);
        selectDogButton = findViewById(R.id.selectDog);

        Gendertoggle = findViewById(R.id.Gendertoggle);
        Neuteringtoggle = findViewById(R.id.Neuteringtoggle);
        man = Gendertoggle.findViewById(R.id.man);
        woman = Gendertoggle.findViewById(R.id.woman);
        NeuteringYes = Neuteringtoggle.findViewById(R.id.Neuteringyes);
        NeuteringNo = Neuteringtoggle.findViewById(R.id.Neuteringno);

        petBreed = findViewById(R.id.petbreed);
        petNickName = findViewById(R.id.petNickname);
        petAge = findViewById(R.id.petAge);


        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if ( petNickName.getText().toString().isEmpty() || petAge.getText().toString().isEmpty() || petBreed.getText().toString().isEmpty()
                        || (man.isChecked() == false && woman.isChecked()==false)
                        || (NeuteringYes.isChecked() == false && NeuteringNo.isChecked()==false)
                        || (count == 0)){
                    Toast.makeText(AddPetActivity.this, "입력하지 않은 항목이 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddPetActivity.this);
                    builder.setTitle("알림")
                            .setMessage("개/고양이 선택 정보는 바꿀 수 없습니다. 확인하셨습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    setPetinfo();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), PetSelectActivity.class);
                startActivity(intent);
            }
        });

        //나이 변경
        btnAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder AgePicker = new AlertDialog.Builder(AddPetActivity.this);

                AgePicker.setTitle("나이변경");
                final NumberPicker AP = new NumberPicker(AddPetActivity.this);
                AgePicker.setView(AP);

                AP.setMinValue(0);
                AP.setMaxValue(30);
                AP.setWrapSelectorWheel(false);
                AP.setValue(0);

                AP.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    }
                });

                AgePicker.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        petAge.setText(String.valueOf(AP.getValue()));
                        dialog.dismiss();
                    }
                });
                AgePicker.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
                AgePicker.show();
            }
        });
        //개/고양이 버튼 클릭 시에 해당 사진으로 이미지뷰 변경
        selectDogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                petprofile.setImageResource(R.drawable.dog2);
                CATDOG = "DOG";
                count = 1;
            }
        });
        selectCatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                petprofile.setImageResource(R.drawable.cat2);
                CATDOG = "CAT";
                count = 2;
            }
        });

    }
    //반려동물 정보 설정
    public void setPetinfo(){
        ServiceAPI service = ServiceGenerator.createService(ServiceAPI.class, getToken());

        String Species = CATDOG;
        String Name = petNickName.getText().toString().trim();
        int Age = Integer.parseInt(petAge.getText().toString());
        String Breed = petBreed.getText().toString().trim();
        String Gender = null;
        String Neutering = null;
        if (man.isChecked()) { Gender = "MALE";
        } else if (woman.isChecked()) { Gender = "FEMALE";}

        if (NeuteringYes.isChecked()) { Neutering = "NEUTER";
        } else if (NeuteringNo.isChecked()) { Neutering = "NOTNEUTER"; }


        Petinfo petinfo = new Petinfo(Species, Name, Age, Breed, Gender, Neutering);
        service.setPetinfo(petinfo).enqueue(new Callback<PetProfileResponse>() {
            @Override
            public void onResponse(Call<PetProfileResponse> call, Response<PetProfileResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),"등록되었습니다.", Toast.LENGTH_SHORT).show();
                } else {Log.d("response 실패", "404");}
            }

            @Override
            public void onFailure(Call<PetProfileResponse> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPetActivity.this);
                builder.setTitle("알림")
                        .setMessage("잠시 후에 다시 시도해주세요.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }
}