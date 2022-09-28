package com.example.myapplication.ui.hospital;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.ui.ServiceSetting.ServiceAPI;
import com.example.myapplication.ui.ServiceSetting.ServiceGenerator;
import com.example.myapplication.ui.mainPage.MainActivity;
import com.example.myapplication.ui.petSelect.AddPetActivity;
import com.example.myapplication.ui.petSelect.PetSelectActivity;
import com.example.myapplication.ui.setting.PetProfileResponse;
import com.example.myapplication.ui.setting.PetinfoData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_hospital_detail extends Fragment {

    MainActivity mainActivity;
    //서버 통신
    private SharedPreferences pre1, pre2, pre3, pre4;

    private EditText Name, Number, Reason;
    private Spinner spn_quesionNaire;
    private ImageView datePicker, camera;
    private TextView Tdate, Ttime;
    private Button timePicker, send, backTolist;
    private RadioGroup radioGroup;

    boolean check;
    private Uri mImageCaptureUri;
    private String absoultePath;

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;

    DatePickerDialog datePickerDialog;
    Calendar calendar = Calendar.getInstance();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootview = (ViewGroup)inflater.inflate(R.layout.f_hospital_detail,container,false);
        Name = rootview.findViewById(R.id.h_Name);
        Number = rootview.findViewById(R.id.h_num);
        spn_quesionNaire = rootview.findViewById(R.id.spn_questionNaire);
        datePicker = rootview.findViewById(R.id.btn_DatePicker);
        timePicker = rootview.findViewById(R.id.btn_TimePicker);
        Tdate = rootview.findViewById(R.id.h_date);
        Ttime = rootview.findViewById(R.id.h_time);
        Reason = rootview.findViewById(R.id.h_reson);
        camera = rootview.findViewById(R.id.btn_img);
        send = rootview.findViewById(R.id.btn_send);
        backTolist = rootview.findViewById(R.id.btn_backTolist);
        radioGroup =rootview.findViewById(R.id.h_radioGroup);

        Number.addTextChangedListener(new PhoneNumberFormattingTextWatcher());


        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pYear = calendar.get(Calendar.YEAR);
                int pMonth = calendar.get(Calendar.MONTH);
                int pDay = calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month +1;
                        String date = year + "-" + month + "-" + day;
                        Tdate.setText(date);
                    }
                }, pYear, pMonth, pDay);
                datePickerDialog.show();
            }
        });
        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checked) {
                switch (checked) {
                    case R.id.h_radioYes:
                        check = true;
                        Toast.makeText(getActivity(), "예!!", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.h_radioNo:
                        check = false;
                        Toast.makeText(getActivity(), "아니오!!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        List<String> spnArray = new ArrayList<>();

        for (int i=0; i<20; i++) {
            spnArray.add("구토 증세");
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.row_spinner, spnArray);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spn_quesionNaire.setAdapter(adapter);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TakePhoto();
                    }
                };
                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TakeAlbum();
                    }
                };

                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                };

                new AlertDialog.Builder(getActivity())
                        .setTitle("업로드할 이미지 선택")
                        .setPositiveButton("사진촬영", cameraListener)
                        .setNegativeButton("앨범선택", albumListener)
                        .setNeutralButton("취소", cancelListener)
                        .show();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //sendData(File);
            }
        });
        backTolist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.onChangeFragment(5);
            }
        });

        return rootview;
    }
    
    //상담신청 정보 보내는 메소드
    public void sendData(File imageFile) {
        String name = Name.getText().toString();
        String number = Number.getText().toString();
        
        //스피너 sharedPreference로 불러와야함.

        String date = Tdate.toString();
        String time = Ttime.toString();
        boolean bill = check;
        String reason = Reason.getText().toString();

        pre1 = getActivity().getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = pre1.getString("TOKEN", null);
        pre2 = getActivity().getSharedPreferences("Serial", MODE_PRIVATE);
        int Serial = pre2.getInt("petSerial", 0);
        int hospSerial = pre3.getInt("hospSerial", 0);

        ServiceAPI SendAPI = ServiceGenerator.createService(ServiceAPI.class, token);
        ApplyData applyData = new ApplyData(Serial, 0, hospSerial, name, number, date, time, bill, reason);

        //RequestBody 내용
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", imageFile.getName(), requestFile);

        Call<hospitalListResponse> call = SendAPI.apply(applyData, body);

        call.enqueue(new Callback<hospitalListResponse>() {
            @Override
            public void onResponse(Call<hospitalListResponse> call, Response<hospitalListResponse> response) {
                if (!response.equals(200)) {
                    Toast.makeText(getActivity(),"신청되었습니다.", Toast.LENGTH_SHORT).show();
                    mainActivity.onChangeFragment(5);
                }
            }

            @Override
            public void onFailure(Call<hospitalListResponse> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("알림")
                        .setMessage("잠시 후에 다시 시도해주세요.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }

    public void showTimePicker() {
        int pHour = calendar.get(Calendar.HOUR);
        int pMinutes = calendar.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {
                    calendar.set(Calendar.HOUR, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    Ttime.setText(String.format("%02d:%02d", hourOfDay, minute));
                }
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar, myTimeListener, pHour, pMinutes, true);
        timePickerDialog.setTitle("희망 시간대");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    // 카메라 촬영 후 이미지 가져오기
    public void TakePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // 임시로 사용할 파일의 경로를 생성
        String url = "tmp_" + System.currentTimeMillis() + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }
    // 앨범에서 이미지 가져오기
    public void TakeAlbum() {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }
    @Override

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode != RESULT_OK) { return; }
        switch(requestCode) {
            case PICK_FROM_ALBUM:
            {
                mImageCaptureUri = data.getData();
                Log.d("AAI", String.valueOf(mImageCaptureUri.getPath()));
            }
            case PICK_FROM_CAMERA:
            {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");

                // CROP할 이미지를 200*200 크기로 저장
                intent.putExtra("outputX", 200); // CROP한 이미지의 x축 크기
                intent.putExtra("outputY", 200); // CROP한 이미지의 y축 크기
                intent.putExtra("aspectX", 1); // CROP 박스의 X축 비율
                intent.putExtra("aspectY", 1); // CROP 박스의 Y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                Log.d("크롭!", "성공");
                startActivityForResult(intent, CROP_FROM_IMAGE); // CROP_FROM_CAMERA case문 이동
                break;
            }

            case CROP_FROM_IMAGE:
            {
                Log.d("크롭~", "성공");
                if(resultCode != RESULT_OK) { return;}
                final Bundle extras = data.getExtras();
                // CROP된 이미지를 저장하기 위한 FILE 경로
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+
                        "/AAI/"+System.currentTimeMillis()+".jpg";
                if(extras != null) {
                    Bitmap photo = extras.getParcelable("data"); // CROP된 BITMAP
                    camera.setImageBitmap(photo); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌
                    storeCropImage(photo, filePath); // CROP된 이미지를 외부저장소, 앨범에 저장한다.
                    absoultePath = filePath;
                    break;
                }

                // 임시 파일 삭제
                File f = new File(mImageCaptureUri.getPath());

                if(f.exists()) {
                    f.delete();
                }
            }
        }

    }
    private void storeCropImage(Bitmap bitmap, String filePath) {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AAI";
        File directory_AAI = new File(dirPath);
        if(!directory_AAI.exists()) // AAI 디렉터리에 폴더가 없다면 (새로 이미지를 저장할 경우에 속한다.)
            directory_AAI.mkdirs(); //디렉토리 새로 만드는 메소드

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {
            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
