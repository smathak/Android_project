package com.example.jieun.project2;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class IdIntent extends AppCompatActivity {

    String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_intent);
    }

    public void idPress(View view){
        EditText editText = (EditText)findViewById(R.id.editText);
        nickname = editText.getText().toString();

        Intent intent = getIntent();

        intent.putExtra("nickname", nickname);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
