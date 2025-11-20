package com.example.study_app.ui.Notes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Notes.Model.Note;

public class InputNoteActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 101;
    private EditText edtTitle, edtContent;
    private ImageView imgAnh;
    private Button btnSave, btnBack;
    private DatabaseHelper dbHelper;
    private Uri selectedImageUri = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_add);

        dbHelper = new DatabaseHelper(this);
        initView();

        setUpClickListener();

    }

    private void initView(){
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        imgAnh = findViewById(R.id.imgAnhHienThi);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setUpClickListener(){
        imgAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // sự kiện cho nút lưu
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void openImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            selectedImageUri = data.getData();
            imgAnh.setImageURI(selectedImageUri);
            imgAnh.setVisibility(View.VISIBLE);
        } else {
            selectedImageUri = null;
            imgAnh.setImageURI(null);
        }

    }
    private void saveNote(){
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if(title.isEmpty()){
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note();
        note.setTitle(title);
        note.setBody(content);
        note.setColor_tag("#FFFFFF");
        note.setPinned(0);
        note.setUser_id(1);
        note.setMa_hp("COMP103");
        String imagePath = selectedImageUri != null ? selectedImageUri.toString() : null;

        long insertedId = dbHelper.insertNote(note, imagePath);
        if (insertedId > 0) {
            Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lưu thất bại", Toast.LENGTH_SHORT).show();
        }
    }

}
