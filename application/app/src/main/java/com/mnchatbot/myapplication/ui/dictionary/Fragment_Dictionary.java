/*
[질병백과 목록 화면] 등록된 질병명 목록을 10개 먼저 보여줌.*/

package com.mnchatbot.myapplication.ui.dictionary;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mnchatbot.myapplication.R;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceAPI;
import com.mnchatbot.myapplication.ui.serviceSetting.ServiceGenerator;
import com.mnchatbot.myapplication.ui.mainPage.MainActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Dictionary extends Fragment {

    MainActivity mainActivity;

    private RecyclerView mRecyclerView;
    private ArrayList<DictionaryViewItem> mList;
    private DictionaryAdapter mAdapter;
    private EditText editText;
    private Button btnDictionary;
    private Context context;

    DsPageResponse.DsPageList dsPageList;
    List<DsPageResponse.DsPageListItem> dsPageItems;
    List<DsListResponse.DsDataList> DsSearchdata;

    private SharedPreferences preferences;

    private long totalCnt = 0;
    private boolean hasNext = false;
    private int page = 0;
    private int itemCnt = 10;

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
    //서버통신
    public String getToken() {
        preferences = getActivity().getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = preferences.getString("TOKEN", null);
        return token;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootview = (ViewGroup)inflater.inflate(R.layout.f_dictionary,container,false);
        mRecyclerView = rootview.findViewById(R.id.recyclerView);
        editText = rootview.findViewById(R.id.editText);
        btnDictionary = rootview.findViewById(R.id.btnDictionary);

        mList = new ArrayList<>();
        // 리사이클러뷰에 데이터추가 (함수가 밑에 구현되어있음)

        loadDsinfo();
        ScrollListener();
        mAdapter = new DictionaryAdapter(mList, context);
        mRecyclerView.setAdapter(mAdapter);


        mAdapter.setOnItemClickListener(new DictionaryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                mainActivity.onChangeFragment(8);
            }
        });

        btnDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mList.clear();
                getDsinfo();
            }
        });
        //정렬
        LinearLayoutManager manger = new LinearLayoutManager(getActivity());
        manger.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(manger);
        return rootview;
    }

    // 리사이클러뷰에 데이터추가
    public void addItem(String DiseaseName, String DiseaseId){
        DictionaryViewItem item = new DictionaryViewItem();
        item.setDiseaseName(DiseaseName);
        item.setDiseaseId(DiseaseId);
        mList.add(item);
    }
    //로드 화면
    public void loadDsinfo() {
        ServiceAPI service = ServiceGenerator.createService(ServiceAPI.class, getToken());

        int item = itemCnt;
        service.callDsinfo(getPage(),item).enqueue(new Callback<DsPageResponse>() {
            @Override
            public void onResponse(Call<DsPageResponse> call, Response<DsPageResponse> response) {
                if(response.isSuccessful()) {
                    dsPageList = response.body().data;
                    totalCnt = dsPageList.gettotalCnt();
                    hasNext = dsPageList.getnextPage();
                    Log.d("dsPage", String.valueOf(hasNext));

                    if(hasNext != false) {
                        dsPageItems = dsPageList.dsPageList;
                        for(int i=0; i< dsPageItems.size(); i++) {
                            String Name = dsPageItems.get(i).getdiseaseName();
                            String ID = dsPageItems.get(i).getdiseaseId();
                            addItem(Name, ID);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DsPageResponse> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("알림")
                        .setMessage("잠시 후에 다시 시도해주세요.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }
    public void ScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (hasNextPage()) {
                    int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                    int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;
                    if (lastVisibleItemPosition == itemTotalCount) {
                        loadDsinfo();
                        setNextPage(false);
                    }
                }

            }
        });
    }
    public void getDsinfo(){
        DsSearchdata = new ArrayList<>();
        String dsName = editText.getText().toString();

        ServiceAPI service = ServiceGenerator.createService(ServiceAPI.class, getToken());
        service.getDsinfo(dsName).enqueue(new Callback<DsListResponse>() {
            @Override
            public void onResponse(Call<DsListResponse> call, Response<DsListResponse> response) {
                if (response.isSuccessful()) {
                    DsSearchdata = response.body().data;
                    if (DsSearchdata == null) {
                        Toast.makeText(getActivity(), "정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        for(int i=0; i< DsSearchdata.size(); i++) {
                            String Name = DsSearchdata.get(i).getdiseaseName();
                            String ID = DsSearchdata.get(i).getdiseaseId();
                            addItem(Name, ID);
                            mAdapter.notifyDataSetChanged();
                            setNextPage(false);
                        }}

                } else {Log.d("response 실패", "404");}
            }

            @Override
            public void onFailure(Call<DsListResponse> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("알림")
                        .setMessage("잠시 후에 다시 시도해주세요.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }

    private int getPage() {
        page++;
        return page;
    }
    private Boolean hasNextPage() {
        return hasNext;
    }
    private void setNextPage(Boolean b) {
        hasNext = b;
    }
}
