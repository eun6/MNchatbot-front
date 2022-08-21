package com.example.myapplication.ui.petSelect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.mainPage.Home;
import com.example.myapplication.ui.mainPage.MainActivity;

import java.util.ArrayList;

public class PetSelectActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<RecyclerViewItem> mList;
    private RecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pet_activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mList = new ArrayList<>();

        // 리사이클러뷰에 데이터추가 (함수가 밑에 구현되어있음)
        addItem("cat", "밤이");

        mRecyclerViewAdapter = new RecyclerViewAdapter(mList);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        // 리사이클러뷰 안의 아이템(반려동물 아이콘) 클릭시 메인화면으로 이동
        mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "안녕!" + mList.get(pos).getMainText(), Toast.LENGTH_SHORT).show();
            }
        });

        // 반려동물 추가 버튼 클릭
        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), AddPetActivity.class);
                startActivityForResult(intent, 101);
            }

        });

    }
    //사용자가 작성한 반려동물 정보 불러오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            if (intent !=null) {
                String petName = intent.getStringExtra("petName");
                String CATDOG = intent.getStringExtra("CATDOG");
                addItem(CATDOG, petName);
                mRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }

    // 리사이클러뷰에 데이터추가
    public void addItem(String imgName, String mainText){
        RecyclerViewItem item = new RecyclerViewItem();
        item.setImgName(imgName);
        item.setMainText(mainText);
        mList.add(item);
    }
}