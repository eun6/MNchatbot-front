/*
[상담신청 내역 화면] 신청한 상담 목록을 보여줌.*/

package com.mnchatbot.myapplication.ui.history;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mnchatbot.myapplication.R;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceAPI;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<HistoryViewItem> mList;
    private HistoryAdapter mAdapter;

    List<HistoryListResponse.HistoryDataList> HistoryList;
    private SharedPreferences pre, pre2;

    //서버통신
    public String getToken() {
        pre = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = pre.getString("TOKEN", null);
        return token;
    }
    public int getpetSerial() {
        pre2 = getSharedPreferences("Serial", MODE_PRIVATE);
        int petSerial = pre2.getInt("petSerial", 0);
        return petSerial;
    }
    public int getapptSerial() {
        pre2 = getSharedPreferences("Serial", MODE_PRIVATE);
        int apptSerial = pre2.getInt("apptSerial", 0);
        return apptSerial;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_history);

        mRecyclerView = findViewById(R.id.recyclerView);
        mList = new ArrayList<>();

        mAdapter = new HistoryAdapter(mList, HistoryActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                Intent intent = new Intent(getApplicationContext(), HistoryDetail.class);
                startActivity(intent);
            }
        });
        mAdapter.setOnLongItemClickListener(new HistoryAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("상담신청 내역 삭제")
                        .setMessage("정말로 삭제하시겠습니까?")
                        .setPositiveButton("아니오", null)
                        .setNegativeButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteHistoryList(pos);
                            }
                        })
                        .create()
                        .show();
            }
        });
        setHistoryList();

        //최신 등록순으로 정렬
        LinearLayoutManager manger = new LinearLayoutManager(HistoryActivity.this);
        manger.setReverseLayout(true);
        manger.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(manger);
        return;
    }

    // 리사이클러뷰에 데이터추가
    public void addItem(int apptSerial,String HospitalName, String Date) {
        HistoryViewItem item = new HistoryViewItem();
        item.setapptSerial(apptSerial);
        item.setHospitalName(HospitalName);
        item.setHospitalDate(Date);
        mList.add(item);
    }

    //상담내역 로드
    public void setHistoryList() {
        HistoryList = new ArrayList<>();
        ServiceAPI service = ServiceGenerator.createService(ServiceAPI.class, getToken());

        service.getHistoryList(getpetSerial()).enqueue(new Callback<HistoryListResponse>() {
            @Override
            public void onResponse(Call<HistoryListResponse> call, Response<HistoryListResponse> response) {
                if(response.isSuccessful()) {
                    HistoryList = response.body().data;
                    if(HistoryList == null) {
                        Log.d("비어있음", "성공");
                    } else {
                        Log.d("자료있음", String.valueOf(HistoryList.size()));
                        for(int i=0; i< HistoryList.size(); i++) {
                            Log.d("비어있음", HistoryList.toString());
                            int aSerial = HistoryList.get(i).getapptSerial();
                            String Name = HistoryList.get(i).getpartnerName();
                            String Date = HistoryList.get(i).getHistoryDate();
                            addItem(aSerial, Name, Date);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                } else {Log.d("response 실패", "404");}
            }

            @Override
            public void onFailure(Call<HistoryListResponse> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("알림")
                        .setMessage("잠시 후에 다시 시도해주세요.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }

    //상담내역 삭제
    public void deleteHistoryList(int pos) {
        ServiceAPI service = ServiceGenerator.createService(ServiceAPI.class, getToken());

        service.deleteHistory(getapptSerial()).enqueue(new Callback<HistoryListResponse>() {
            @Override
            public void onResponse(Call<HistoryListResponse> call, Response<HistoryListResponse> response) {
                if (response.isSuccessful()) {
                    HistoryViewItem item = mList.get(pos);
                    mList.remove(item);
                    mAdapter.notifyDataSetChanged();
                } else {Log.d("response 실패", "404");}
            }
            @Override
            public void onFailure(Call<HistoryListResponse> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("알림")
                        .setMessage("잠시 후에 다시 시도해주세요.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }
}
