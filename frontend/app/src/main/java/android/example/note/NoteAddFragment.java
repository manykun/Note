package android.example.note;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.media.MediaRecorder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xuexiang.xormlite.InternalDataBaseRepository;
import com.xuexiang.xormlite.db.DBService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class NoteAddFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_PICK_AUDIO = 3;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private String audioFileName;
    private GridLayout gridLayout;
    private FloatingActionButton fabStopRecording;
    private int Note_id;
    private EditText titleview;
    private EditText contentview;

    private DBService<NoteModel> mDBService;

    private NoteModel noteModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Note_id = getArguments().getInt("Note_id");
        }
    }

    private void initNote() {
        if (Note_id == 0) {
            noteModel = new NoteModel();
        } else {
            try {
                noteModel = mDBService.queryById(Note_id);
                titleview.setText(noteModel.getTitle());
                contentview.setText(noteModel.getContent());

                // 添加图片
                ArrayList<String> images = noteModel.getImages();
                if (images != null) {
                    for (String image : images) {
                        File file = new File(requireContext().getFilesDir(), image);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), Uri.fromFile(file));
                        addImageView(bitmap, image, 0);
                    }
                }

                // 添加录音
                ArrayList<String> audioList = noteModel.getAudio();
                if (audioList != null) {
                    for (String audio : audioList) {
                        addImageView(null, audio, 1);
                    }
                }



            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.noteaddfragment, container, false);

        mDBService = InternalDataBaseRepository.getInstance().getDataBase(NoteModel.class);

        titleview = view.findViewById(R.id.edit_text_title);
        contentview = view.findViewById(R.id.edit_text_content);
        gridLayout = view.findViewById(R.id.gridLayout);

        initNote();

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        }

        titleview = view.findViewById(R.id.edit_text_title);
        contentview = view.findViewById(R.id.edit_text_content);

        fabStopRecording = view.findViewById(R.id.fab_stop_recording);
        fabStopRecording.setVisibility(View.GONE); // 初始时隐藏停止录音按钮

        fabStopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        // 设置toolbar作为actionbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.add_note);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_photo) {
                    showImageSourceDialog();
                    return true;
                } else if (item.getItemId() == R.id.action_record) {
                    showRecordSourceDialog();
                    return true;
                } else {
                    return false;
                }
            }
        });


        return view;

    }



    private void showRecordSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Record");
        builder.setItems(new CharSequence[]{"Recording", "Select Audio"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        startRecording();
                        break;
                    case 1:
                        importAudio();
                        break;
                }
            }
        });
        builder.show();
    }

    private void importAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), REQUEST_PICK_AUDIO);
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (IllegalStateException e) {
                // Handle IllegalStateException
                e.printStackTrace();
            }
        }

        // 将录音文件添加到gridLayout中

        addImageView(null, audioFileName, 1);

        // 加入数据库
        ArrayList<String> audioList = noteModel.getAudio();
        if (audioList == null) {
            audioList = new ArrayList<>();
        }
        audioList.add(audioFileName);
        noteModel.setAudio(audioList);

        fabStopRecording.setVisibility(View.GONE);
    }

    private void playAudio(String audioname) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        audioFilePath = requireContext().getFilesDir() + "/" + audioname;
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        audioFilePath = requireContext().getFilesDir() + "/audio_" + timestamp + ".3gp";
        audioFileName = "audio_" + timestamp + ".3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
        fabStopRecording.setVisibility(View.VISIBLE);
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Image");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        dispatchTakePictureIntent();
                        break;
                    case 1:
                        dispatchPickImageIntent();
                        break;
                }
            }
        });
        builder.show();
    }

    // 从相册选取照片
    private void dispatchPickImageIntent() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/*");
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK);
    }

    // 调用系统相机拍照
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_note, menu);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Process image captured from camera
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                // 保存图片到数据库
                String imageNmae = saveImage(imageBitmap);
                ArrayList<String> imageList = noteModel.getImages();
                if (imageList == null) {
                    imageList = new ArrayList<>();
                }
                imageList.add(imageNmae);
                noteModel.setImages(imageList);

                addImageView(imageBitmap, imageNmae, 0);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Process image picked from gallery
                Uri imageUri = data.getData();
                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                    String imageNmae = saveImage(imageBitmap);
                    ArrayList<String> imageList = noteModel.getImages();
                    if (imageList == null) {
                        imageList = new ArrayList<>();
                    }
                    imageList.add(imageNmae);
                    noteModel.setImages(imageList);
                    addImageView(imageBitmap, imageNmae, 0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else if (requestCode == REQUEST_PICK_AUDIO) {
                // Process audio picked from gallery
                Uri audioUri = data.getData();

                

                // 加入数据库
                audioFileName = audioUri.getLastPathSegment();
                ArrayList<String> audioList = noteModel.getAudio();
                if (audioList == null) {
                    audioList = new ArrayList<>();
                }
                audioList.add(audioFileName);
                noteModel.setAudio(audioList);


                // 将录音文件添加到gridLayout中
                addImageView(null, audioFileName, 1);


            }
        }
    }

    private String saveImage(Bitmap imageBitmap) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String FileName = "IMG_" + timestamp + ".jpg";
        File file = new File(requireContext().getFilesDir(), FileName);

        try {
            FileOutputStream out = new FileOutputStream(file);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileName;
    }

    private void addImageView(Bitmap imageBitmap, String imageName, int type) {
        ImageView imageView = new ImageView(requireContext());
        // imageView.setImageBitmap(imageBitmap);
        // imageView.setImageResource(R.drawable.mic);

        if (type == 0) {
            imageView.setImageBitmap(imageBitmap);
        } else if (type == 1) {
            imageView.setImageResource(R.drawable.mic);

                    // 设置点击事件
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 播放录音
                    playAudio(imageName);
                }
            });
        }

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels / 3; // 设置每个视图的宽度为屏幕宽度的1/3
        params.height = getResources().getDisplayMetrics().widthPixels / 3; // 设置每个视图的高度为屏幕宽度的1/3
        imageView.setLayoutParams(params);
        imageView.setTag(imageName);
        gridLayout.addView(imageView);
        
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteDialog(imageView, type);
                return true;
            }
        });
    }

    private void deleteImage(ImageView imageView, int type) {
        String imageNmae = imageView.getTag().toString();

        if (type == 0) {
            ArrayList<String> imageList = noteModel.getImages();
            if (imageList != null) {
                imageList.remove(imageNmae);
                noteModel.setImages(imageList);
            }
        } else if (type == 1) {
            ArrayList<String> audioList = noteModel.getAudio();
            if (audioList != null) {
                audioList.remove(imageNmae);
                noteModel.setAudio(audioList);
            }
        }

        // 在应用文件目录中删除图片文件
        File file = new File(requireContext().getFilesDir(), imageNmae);
        file.delete();
    }

    private void showDeleteDialog(ImageView imageView, int type) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        gridLayout.removeView(imageView);
                        deleteImage(imageView, type);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        savenote();
    }

    private void savenote() {
        String title = titleview.getText().toString();
        String content = contentview.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        noteModel.setTitle(title);
        if (!content.isEmpty()) {
            noteModel.setContent(content);
        }
        
        if (Note_id == 0) {
            try {
                // 设置uid
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                String uid = sharedPreferences.getString("uid", "");
                noteModel.setUid(uid);

                mDBService.insert(noteModel);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            try {
                mDBService.updateData(noteModel);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}
