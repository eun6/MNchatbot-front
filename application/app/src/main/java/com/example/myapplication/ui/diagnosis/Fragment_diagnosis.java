package com.example.myapplication.ui.diagnosis;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.ui.Dictionary.dsPageResponse;
import com.example.myapplication.ui.QuestionNaire.QuestionAdapter;
import com.example.myapplication.ui.ServiceSetting.ServiceAPI;
import com.example.myapplication.ui.ServiceSetting.ServiceGenerator;
import com.example.myapplication.ui.mainPage.MainActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_diagnosis extends Fragment {

    MainActivity mainActivity;

    private RecyclerView mRecyclerView;
    private ArrayList<diagnosisViewItem> mList;
    private diagnosisAdapter mAdapter;

    List<diagListResponse.DiagList> DiagList;

    private SharedPreferences pre, pre2;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootview = (ViewGroup)inflater.inflate(R.layout.f_diagnosis,container,false);
        mRecyclerView = rootview.findViewById(R.id.recyclerView);
        mList = new ArrayList<>();

        setDiagList();
        mAdapter = new diagnosisAdapter(mList);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new diagnosisAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                mainActivity.onChangeFragment(6);
            }
        });
        mAdapter.setOnLongItemClickListener(new diagnosisAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("예상진단 삭제")
                        .setMessage("정말로 삭제하시겠습니까?")
                        .setPositiveButton("아니오", null)
                        .setNegativeButton("예", null)
                        .create()
                        .show();
            }
        });

        //최신 등록순으로 정렬
        LinearLayoutManager manger = new LinearLayoutManager(getActivity());
        manger.setReverseLayout(true);
        manger.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(manger);
        return rootview;
    }
    // 리사이클러뷰에 데이터추가
    public void addItem(String DiseaseName, String Date){
        diagnosisViewItem item = new diagnosisViewItem();
        item.setDiseaseName(DiseaseName);
        item.setDiseaseDate(Date);
        mList.add(item);
    }

    public void setDiagList() {
        DiagList = new ArrayList<>();
        pre = getActivity().getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = pre.getString("TOKEN", null);
        pre2 = getActivity().getSharedPreferences("Serial", MODE_PRIVATE);
        int petSerial = pre2.getInt("petSerial", 0);
        ServiceAPI DiagAPI = ServiceGenerator.createService(ServiceAPI.class, token);

        Call<diagListResponse> call = DiagAPI.getDiagList(petSerial);

        call.enqueue(new Callback<diagListResponse>() {
            @Override
            public void onResponse(Call<diagListResponse> call, Response<diagListResponse> response) {
                Log.d("통신성공", "성공");
                if(response.isSuccessful()) {
                    DiagList = response.body().data;

                    if(DiagList == null) {
                        Log.d("비어있음", "성공");
                    } else {
                        Log.d("자료있음", "성공");
                        for(int i=0; i< DiagList.size(); i++) {
                            String Name = DiagList.get(i).getDsName();
                            String Date = DiagList.get(i).getDsDate();
                            addItem(Name, Date);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<diagListResponse> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("알림")
                        .setMessage("잠시 후에 다시 시도해주세요.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }


}