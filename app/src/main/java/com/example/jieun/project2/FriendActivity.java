package com.example.jieun.project2;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

// 친구 신청할 때 호출되는 클래스
public class FriendActivity extends AppCompatActivity {

    String friend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
    }

    // Add 버튼을 누르면 입력된 친구이름을 MapsActivity로 반환
    public void addPress(View view){
        EditText editText = (EditText)findViewById(R.id.editText);
        friend = editText.getText().toString();

        Intent intent = getIntent();

        intent.putExtra("friend", friend);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
