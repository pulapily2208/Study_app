package com.example.study_app.ui.Notes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.ui.Notes.Model.Note;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class InputNoteActivity extends AppCompatActivity {

    private EditText edtTitle, edtContent;
    private Button btnSave, btnBack;
    private ImageView btnUndo, btnRedo, btnText, btnAttach, btnShowColorPalette, btnChooseFromGallery, btnTakePhoto;

    private LinearLayout textFormattingToolbar, imageSourceChooserLayout, imageContainer;
    private ImageView btnBold, btnItalic, btnHighlight, btnAlignLeft, btnAlignCenter, btnAlignRight;
    private LinearLayout colorPalette;
    private ImageView color1, color2, color3, color4, color5;

    private DatabaseHelper dbHelper;
    private Note currentNote;
    private Uri cameraImageUri;

    private ActivityResultLauncher<Uri> takePhoto;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultiple;

    private ArrayList<String> imagePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_add);

        dbHelper = new DatabaseHelper(this);

        setupViews();
        setupPhotoPicker();
        setUpClickListener();

        Intent intent = getIntent();
        if (intent.hasExtra("note_id")) {
            int noteId = intent.getIntExtra("note_id", -1);
            loadNoteData(noteId);
        } else {
            currentNote = new Note();
        }
    }

    @SuppressLint("WrongViewCast")
    private void setupViews() {
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnText = findViewById(R.id.btnText);
        btnAttach = findViewById(R.id.btnAttach);
        btnShowColorPalette = findViewById(R.id.btnShowColorPalette);

        imageContainer = findViewById(R.id.imageContainer);
        imageSourceChooserLayout = findViewById(R.id.imageSourceChooserLayout);
        btnChooseFromGallery = findViewById(R.id.btnChooseFromGallery);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);

        textFormattingToolbar = findViewById(R.id.textFormattingToolbar);
        btnBold = findViewById(R.id.btnBold);
        btnItalic = findViewById(R.id.btnItalic);
        btnHighlight = findViewById(R.id.btnHighlight);
        btnAlignLeft = findViewById(R.id.btnAlignLeft);
        btnAlignCenter = findViewById(R.id.btnAlignCenter);
        btnAlignRight = findViewById(R.id.btnAlignRight);

        colorPalette = findViewById(R.id.colorPalette);
        color1 = findViewById(R.id.color1);
        color2 = findViewById(R.id.color2);
        color3 = findViewById(R.id.color3);
        color4 = findViewById(R.id.color4);
        color5 = findViewById(R.id.color5);
    }

    private void setupPhotoPicker() {
        // PICK MULTIPLE IMAGES
        pickMultiple = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
                    if (!uris.isEmpty()) {
                        for (Uri uri : uris) {
                            Uri savedUri = saveImageToAppFolder(uri);
                            if (savedUri != null) {
                                imagePaths.add(savedUri.getPath());
                                addImageToContainer(savedUri);
                            }
                        }
                    }
                });

        // CAMERA
        takePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                Uri savedUri = saveImageToAppFolder(cameraImageUri);
                if (savedUri != null) {
                    imagePaths.add(savedUri.getPath());
                    addImageToContainer(savedUri);
                }
            }
        });
    }

    private Uri saveImageToAppFolder(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            File dir = new File(getFilesDir(), "images");
            if (!dir.exists()) dir.mkdirs();

            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            File destFile = new File(dir, fileName);

            FileOutputStream out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.close();
            inputStream.close();

            return Uri.fromFile(destFile);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addImageToContainer(Uri uri) {
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Glide.with(this).load(uri).into(imageView);
        imageContainer.addView(imageView);
    }

    private void setUpClickListener() {

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveNote());

        btnText.setOnClickListener(v -> {
            colorPalette.setVisibility(View.GONE);
            textFormattingToolbar.setVisibility(toggle(textFormattingToolbar));
        });

        btnShowColorPalette.setOnClickListener(v -> {
            textFormattingToolbar.setVisibility(View.GONE);
            colorPalette.setVisibility(toggle(colorPalette));
        });

        btnAttach.setOnClickListener(v -> {
            imageSourceChooserLayout.setVisibility(toggle(imageSourceChooserLayout));
        });

        btnBold.setOnClickListener(v -> toggleStyle(Typeface.BOLD));
        btnItalic.setOnClickListener(v -> toggleStyle(Typeface.ITALIC));
        btnHighlight.setOnClickListener(v -> toggleHighlight());

        btnAlignLeft.setOnClickListener(v -> applyAlignment(Layout.Alignment.ALIGN_NORMAL));
        btnAlignCenter.setOnClickListener(v -> applyAlignment(Layout.Alignment.ALIGN_CENTER));
        btnAlignRight.setOnClickListener(v -> applyAlignment(Layout.Alignment.ALIGN_OPPOSITE));

        color1.setOnClickListener(v -> selectColor("#FFAB91"));
        color2.setOnClickListener(v -> selectColor("#FFCC80"));
        color3.setOnClickListener(v -> selectColor("#E6EE9B"));
        color4.setOnClickListener(v -> selectColor("#80DEEA"));
        color5.setOnClickListener(v -> selectColor("#CF94DA"));

        btnChooseFromGallery.setOnClickListener(v ->
                pickMultiple.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        btnTakePhoto.setOnClickListener(v -> openCamera());
    }

    private int toggle(View v) {
        return v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
    }

    private void openCamera() {
        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File file = new File(getExternalFilesDir(null), fileName);
        cameraImageUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );
        takePhoto.launch(cameraImageUri);
    }

    private void selectColor(String color) {
        currentNote.setColor_tag(color);
        Toast.makeText(this, "Color selected", Toast.LENGTH_SHORT).show();
    }

    private void toggleStyle(int style) {
        int s = edtContent.getSelectionStart();
        int e = edtContent.getSelectionEnd();
        if (s >= e) return;
        edtContent.getText().setSpan(new StyleSpan(style), s, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    private void toggleHighlight() {
        int s = edtContent.getSelectionStart();
        int e = edtContent.getSelectionEnd();
        if (s >= e) return;
        edtContent.getText().setSpan(new BackgroundColorSpan(Color.YELLOW), s, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    private void applyAlignment(Layout.Alignment alignment) {
        int s = edtContent.getSelectionStart();
        int e = edtContent.getSelectionEnd();
        if (s >= e) return;
        edtContent.getText().setSpan(new AlignmentSpan.Standard(alignment), s, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    private void saveNote() {
        String title = edtTitle.getText().toString().trim();
        String content = Html.toHtml(edtContent.getText(), Html.FROM_HTML_MODE_LEGACY);

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        currentNote.setTitle(title);
        currentNote.setBody(content);
        currentNote.setTimestamp();

        try {
            currentNote.setImagePath(new JSONArray(imagePaths).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (currentNote.getId() == 0) {
            dbHelper.insertNote(currentNote);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateNote(currentNote);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void loadNoteData(int noteId) {
        currentNote = dbHelper.getNoteById(noteId);
        if (currentNote == null) {
            Toast.makeText(this, "Error loading note.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtTitle.setText(currentNote.getTitle());
        edtContent.setText(Html.fromHtml(currentNote.getBody(), Html.FROM_HTML_MODE_LEGACY));

        try {
            JSONArray array = new JSONArray(currentNote.getImagePath());
            for (int i = 0; i < array.length(); i++) {
                String path = array.getString(i);
                File file = new File(path);
                if (file.exists()) {
                    imagePaths.add(path);
                    addImageToContainer(Uri.fromFile(file));
                }
            }
        } catch (Exception ignored) {}
    }
}
