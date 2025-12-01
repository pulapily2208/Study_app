package com.example.study_app.ui.Notes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.study_app.R;
import com.example.study_app.data.NotesDao;
import com.example.study_app.ui.Notes.Model.Note;

import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class InputNoteActivity extends AppCompatActivity {

    private EditText edtTitle, edtContent;
    private Button btnSave, btnBack;
    private ImageView btnUndo, btnRedo, btnText, btnAttach, btnShowColorPalette, btnChooseFromGallery, btnTakePhoto,
            btnChecklist;

    private LinearLayout textFormattingToolbar, imageSourceChooserLayout, imageContainer;
    private ImageView btnBold, btnItalic, btnHighlight, btnAlignLeft, btnAlignCenter, btnAlignRight, btnUploadPdf,
            btnAudio, btnRecordVoice, btnCancelVoice, btnFinishVoice;
    private LinearLayout colorPalette, voiceContainer;
    private ImageView color1, color2, color3, color4, color5;

    private NotesDao dbHelper;
    private Note currentNote;
    private Uri cameraImageUri;

    private ActivityResultLauncher<Uri> takePhoto;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultiple;

    private ActivityResultLauncher<String> pickPdf;

    private List<String> imagePaths = new ArrayList<>();

    private List<String> pdfPaths = new ArrayList<>();

    private final List<String> audioPaths = new ArrayList<>();

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private File audioFile;
    private Uri audioUri;

    private final Stack<String> undoStack = new Stack<>();
    private final Stack<String> redoStack = new Stack<>();
    private boolean isUndoingOrRedoing = false;

    private static final int REQUEST_AUDIO_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.note_add);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new NotesDao(this);

        setupViews();
        setupPhotoPicker();
        setUpClickListener();

        Intent intent = getIntent();
        if (intent.hasExtra("note_id")) {
            int noteId = intent.getIntExtra("note_id", -1);
            loadNoteData(noteId);
        } else {
            currentNote = new Note();
            undoStack.push(Html.toHtml(edtContent.getText(), Html.FROM_HTML_MODE_LEGACY));
        }

        edtContent.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUndoingOrRedoing)
                    return;

                makeCheckboxesClickable();

                String currentHtml = Html.toHtml(s, Html.FROM_HTML_MODE_LEGACY);
                if (undoStack.isEmpty() || !undoStack.peek().equals(currentHtml)) {
                    undoStack.push(currentHtml);
                }
                redoStack.clear();
            }
        });
    }

    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] { Manifest.permission.RECORD_AUDIO },
                REQUEST_AUDIO_PERMISSION);
    }

    @SuppressLint("WrongViewCast")
    private void setupViews() {
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        edtContent.setHorizontallyScrolling(false);
        edtContent.setMaxLines(Integer.MAX_VALUE);

        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnText = findViewById(R.id.btnText);
        btnAttach = findViewById(R.id.btnAttach);
        btnShowColorPalette = findViewById(R.id.btnShowColorPalette);
        btnChecklist = findViewById(R.id.btnChecklist);

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

        btnUploadPdf = findViewById(R.id.btnUploadPdf);

        btnAudio = findViewById(R.id.btnAudio);

        voiceContainer = findViewById(R.id.voiceContainer);
        btnRecordVoice = findViewById(R.id.btnRecordVoice);
        btnCancelVoice = findViewById(R.id.btnCancelVoice);
        btnFinishVoice = findViewById(R.id.btnFinishVoice);

        colorPalette = findViewById(R.id.colorPalette);
        color1 = findViewById(R.id.color1);
        color2 = findViewById(R.id.color2);
        color3 = findViewById(R.id.color3);
        color4 = findViewById(R.id.color4);
        color5 = findViewById(R.id.color5);
    }

    private void setupPhotoPicker() {
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

        takePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                Uri savedUri = saveImageToAppFolder(cameraImageUri);
                if (savedUri != null) {
                    imagePaths.add(savedUri.getPath());
                    addImageToContainer(savedUri);
                }
            }
        });

        pickPdf = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Uri savedUri = savePdfToAppFolder(uri);
                        if (savedUri != null) {
                            pdfPaths.add(savedUri.getPath());
                            addPdfToContainer(savedUri);
                        }
                    }
                });
    }

    private Uri saveImageToAppFolder(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null)
                return null;

            File dir = new File(getFilesDir(), "images");
            if (!dir.exists())
                dir.mkdirs();

            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            File destFile = new File(dir, fileName);

            try (InputStream in = inputStream; FileOutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            return Uri.fromFile(destFile);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Uri savePdfToAppFolder(Uri sourceUri) {
        try {
            String originalName = getFileNameFromUri(sourceUri);
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null)
                return null;

            File dir = new File(getFilesDir(), "pdfs");
            if (!dir.exists())
                dir.mkdirs();

            File destFile = new File(dir, originalName);

            try (InputStream in = inputStream; FileOutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            return Uri.fromFile(destFile);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileNameFromUri(Uri sourceUri) {
        String result = null;
        try (Cursor cursor = getContentResolver().query(sourceUri, null, null, null, null)) {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = sourceUri.getLastPathSegment();
        }
        return result;
    }

    private void addImageToContainer(Uri uri) {
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Glide.with(this).load(uri).into(imageView);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Xóa ảnh");
                builder.setMessage("Bạn có chắc chắn muốn xóa ảnh này?");
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String path = uri.getPath();
                        imagePaths.remove(path);
                        imageContainer.removeView(imageView);

                        File file = new File(path);
                        if (file.exists()) {
                            file.delete();
                        }

                        Toast.makeText(InputNoteActivity.this, "Ảnh đã được xóa", Toast.LENGTH_SHORT).show();

                    }
                });
                builder.setNegativeButton("Hủy", null);
                builder.show();
                return true;
            }
        });
        imageContainer.addView(imageView);
    }

    private void addPdfToContainer(Uri uri) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(8, 8, 8, 8);

        String pdfPath = uri.getPath();

        layout.setTag(pdfPath);
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.note_ic_pdf);
        icon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TextView fileName = new TextView(this);
        fileName.setText(new File(pdfPath).getName());
        fileName.setTextSize(16);
        fileName.setPadding(20, 20, 0, 0);
        fileName.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Chiếm không gian còn lại
        ));

        ImageView btnMenu = new ImageView(this);
        btnMenu.setImageResource(R.drawable.note_ic_more);
        LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        menuParams.setMargins(0, 20, 5, 0);
        btnMenu.setLayoutParams(menuParams);

        layout.addView(icon);
        layout.addView(fileName);
        layout.addView(btnMenu);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(pdfPath), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), btnMenu);
                popupMenu.getMenu().add("Xóa");
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("Xóa")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setTitle("Xóa tệp");
                            builder.setMessage("Bạn có chắc chắn muốn xóa tệp này?");
                            builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pdfPaths.remove(pdfPath);

                                    imageContainer.removeView(layout);

                                    File file = new File(pdfPath);
                                    if (file.exists()) {
                                        file.delete();
                                    }

                                    Toast.makeText(InputNoteActivity.this, "PDF đã được xóa", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                            builder.setNegativeButton("Hủy", null);
                            builder.show();

                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        imageContainer.addView(layout);
    }

    private void showVoiceContainer() {
        voiceContainer.setVisibility(View.VISIBLE);

        btnRecordVoice.setVisibility(View.VISIBLE);
        btnCancelVoice.setVisibility(View.VISIBLE);
        btnFinishVoice.setVisibility(View.VISIBLE);

        // Ẩn các thanh khác
        colorPalette.setVisibility(View.GONE);
        textFormattingToolbar.setVisibility(View.GONE);
        imageSourceChooserLayout.setVisibility(View.GONE);
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

        btnAttach.setOnClickListener(v -> imageSourceChooserLayout.setVisibility(toggle(imageSourceChooserLayout)));

        btnUndo.setOnClickListener(v -> performUndo());
        btnRedo.setOnClickListener(v -> performRedo());

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

        btnChooseFromGallery.setOnClickListener(v -> pickMultiple.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnTakePhoto.setOnClickListener(v -> openCamera());

        btnChecklist.setOnClickListener(v -> insertCheckboxAtCurrentLine());

        btnUploadPdf.setOnClickListener(v -> pickPdf.launch("application/pdf"));

        btnAudio.setOnClickListener(v -> {
            showVoiceContainer();
        });

        btnRecordVoice.setOnClickListener(v -> {
            if (!hasAudioPermission()) {
                requestAudioPermission();
            }
            startRecording();
        });

        btnFinishVoice.setOnClickListener(v -> stopRecording());

        btnCancelVoice.setOnClickListener(v -> {
            // Ẩn giao diện
            voiceContainer.setVisibility(View.GONE);

            // Xoá file nếu đã tạo
            if (audioFile != null) {
                File f = new File(audioFile.toURI());
                if (f.exists())
                    f.delete();
            }
        });
    }

    private void startRecording() {
        try {
            String fileName = "AUD_" + System.currentTimeMillis() + ".3gp";
            File dir = new File(getFilesDir(), "audio");
            if (!dir.exists())
                dir.mkdirs();
            audioFile = new File(dir, fileName);
            audioUri = Uri.fromFile(audioFile);

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            Toast.makeText(this, "Đang ghi âm...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

            audioPaths.add(audioUri.getPath());
            Toast.makeText(this, "Đã lưu audio", Toast.LENGTH_SHORT).show();
            addAudioToContainer(audioUri);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAudioToContainer(Uri audioUri) {
        String audioPath = audioUri.getPath();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(8, 8, 8, 8);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_audio);
        icon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));

        TextView fileName = new TextView(this);
        fileName.setText(new File(audioUri.getPath()).getName());
        fileName.setTextSize(16);
        fileName.setPadding(20, 20, 0, 0);

        layout.addView(icon);
        layout.addView(fileName);

        icon.setOnClickListener(v -> {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioUri.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> mp.release());
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    mediaPlayer.release();
                } catch (Exception ignored) {
                }
            }
        });

        layout.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Xóa audio");
            builder.setMessage("Bạn có chắc chắn muốn xóa audio này?");
            builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    audioPaths.remove(audioPath);

                    imageContainer.removeView(layout);

                    File file = new File(audioPath);
                    if (file.exists()) {
                        file.delete();
                    }

                    Toast.makeText(InputNoteActivity.this, "Audio đã được xóa", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Hủy", null);
            builder.show();
            return true;

        });

        imageContainer.addView(layout);
    }

    private void insertCheckboxAtCurrentLine() {
        int start = Math.max(edtContent.getSelectionStart(), 0);
        Editable editable = edtContent.getText();
        String text = editable.toString();
        int lineStart = start;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n')
            lineStart--;

        // Chỉ thêm checkbox nếu dòng chưa có
        if (!text.startsWith("☐", lineStart) && !text.startsWith("☑", lineStart)) {
            editable.insert(lineStart, "☐ ");
        }
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
                file);
        takePhoto.launch(cameraImageUri);
    }

    private void selectColor(String color) {
        currentNote.setColor_tag(color);
        Toast.makeText(this, "Color selected", Toast.LENGTH_SHORT).show();
    }

    private void toggleStyle(int style) {
        int s = edtContent.getSelectionStart();
        int e = edtContent.getSelectionEnd();
        if (s >= e)
            return;

        StyleSpan[] spans = edtContent.getText().getSpans(s, e, StyleSpan.class);
        boolean exists = false;
        for (StyleSpan span : spans) {
            if (span.getStyle() == style) {
                edtContent.getText().removeSpan(span);
                exists = true;
            }
        }
        if (!exists) {
            edtContent.getText().setSpan(new StyleSpan(style), s, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
    }

    private void toggleHighlight() {
        int s = edtContent.getSelectionStart();
        int e = edtContent.getSelectionEnd();
        if (s >= e)
            return;
        edtContent.getText().setSpan(new BackgroundColorSpan(Color.YELLOW), s, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    private void applyAlignment(Layout.Alignment alignment) {
        int s = edtContent.getSelectionStart();
        int e = edtContent.getSelectionEnd();
        if (s >= e)
            return;
        edtContent.getText().setSpan(new AlignmentSpan.Standard(alignment), s, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    private void makeCheckboxesClickable() {
        Editable editable = edtContent.getText();
        ClickableSpan[] oldSpans = editable.getSpans(0, editable.length(), ClickableSpan.class);
        for (ClickableSpan span : oldSpans)
            editable.removeSpan(span);

        String text = editable.toString();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c == '☐' || c == '☑') && (i == 0 || text.charAt(i - 1) == '\n')) {
                final int index = i;
                editable.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Editable e = edtContent.getText();
                        if (index < e.length()) {
                            char current = e.charAt(index);
                            if (current == '☐')
                                e.replace(index, index + 1, "☑");
                            else if (current == '☑')
                                e.replace(index, index + 1, "☐");
                        }
                    }

                    @Override
                    public void updateDrawState(@NonNull android.text.TextPaint ds) {
                        ds.setColor(ds.linkColor); // giữ màu text mặc định
                        ds.setUnderlineText(false); // bỏ gạch chân
                    }
                }, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        if (!(edtContent.getMovementMethod() instanceof LinkMovementMethod)) {
            edtContent.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void performUndo() {
        if (undoStack.size() > 1) {
            isUndoingOrRedoing = true;
            String currentState = undoStack.pop();
            redoStack.push(currentState);

            String prevState = undoStack.peek();
            edtContent.setText(HtmlCompat.fromHtml(prevState, HtmlCompat.FROM_HTML_MODE_COMPACT));
            edtContent.setSelection(edtContent.length());

            makeCheckboxesClickable();
            isUndoingOrRedoing = false;
        }
    }

    private void performRedo() {
        if (!redoStack.isEmpty()) {
            isUndoingOrRedoing = true;
            String nextState = redoStack.pop();
            undoStack.push(nextState);

            edtContent.setText(HtmlCompat.fromHtml(nextState, HtmlCompat.FROM_HTML_MODE_COMPACT));
            edtContent.setSelection(edtContent.length());

            makeCheckboxesClickable();
            isUndoingOrRedoing = false;
        }
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

        currentNote.setImagePaths(new ArrayList<>(imagePaths));
        currentNote.setPdfPaths(pdfPaths);
        currentNote.setAudioPaths(audioPaths);

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
            Toast.makeText(this, "Lỗi tải ghi chú", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtTitle.setText(currentNote.getTitle());
        edtContent.setText(Html.fromHtml(currentNote.getBody(), Html.FROM_HTML_MODE_LEGACY));

        undoStack.push(Html.toHtml(edtContent.getText(), Html.FROM_HTML_MODE_LEGACY));

        // Load danh sách ảnh
        if (currentNote.getImagePaths() != null) {
            for (String path : currentNote.getImagePaths()) {
                File file = new File(path);
                if (file.exists()) {
                    imagePaths.add(path);
                    addImageToContainer(Uri.fromFile(file));
                }
            }
        }

        if (currentNote.getPdfPaths() != null) {
            for (String path : currentNote.getPdfPaths()) {
                File file = new File(path);
                if (file.exists()) {
                    addPdfToContainer(Uri.fromFile(file));
                }
            }
        }

        if (currentNote.getAudioPaths() != null) {
            for (String path : currentNote.getAudioPaths()) {
                File file = new File(path);
                if (file.exists()) {
                    audioPaths.add(path);
                    addAudioToContainer(Uri.fromFile(file));
                }
            }
        }
    }
}
