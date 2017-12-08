package com.example.jieun.project2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;


// CREATE ID 버튼을 클릭하면 호출되는 클래스
public class IdIntent extends AppCompatActivity {
    private SQLiteDatabase mDB;
    Cursor mCursor;

    String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_intent);

        // DB
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        mDB = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(mDB);
    }

    public void idPress(View view){
        TextView alertText = (TextView)findViewById(R.id.alertText);
        EditText editText = (EditText)findViewById(R.id.editText);
        nickname = editText.getText().toString();
        boolean flag = false;

        mCursor = mDB.query("myname_table", new String[] {"myname"},
                "myname=?", new String[]{nickname}, null, null, null);

        if(mCursor != null) {   // 이미 있는 이름이면 넣지 않고, 이미 있는 이름이라는 경고 문구를 띄어준다.
            if (mCursor.moveToFirst()) {
                do{
                    if(mCursor.getString(0).equals(nickname)){
                        alertText.setText("This ID already exists! Try another ID");
                        flag = true;
                        break;
                    }
                }while(mCursor.moveToNext());
            }
        }


        if(!flag){  // db에 없는 이름이라면 MainActivity 로 반환한다.
            Intent intent = getIntent();
            flag = false;
            intent.putExtra("nickname", nickname);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}
