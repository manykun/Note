package android.example.note;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.GridLayout;
import android.widget.ImageView;
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

import java.io.IOException;
import java.sql.SQLException;

public class NoteAddFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_PICK_AUDIO = 3;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private GridLayout gridLayout;
    private FloatingActionButton fabStopRecording;

    DBService<NoteModel> mDBService = InternalDataBaseRepository.getInstance().getDataBase(NoteModel.class);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.noteaddfragment, container, false);

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        }

        fabStopRecording = view.findViewById(R.id.fab_stop_recording);
        fabStopRecording.setVisibility(View.GONE); // 初始时隐藏停止录音按钮

        fabStopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        gridLayout = view.findViewById(R.id.gridLayout);

        // 设置toolbar作为actionbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.add_note);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_photo) {
                    // Handle save action
                    showImageSourceDialog();
                    return true;
                } else if (item.getItemId() == R.id.action_record) {
                    // Handle delete action
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
        ImageView imageView = new ImageView(requireContext());
        imageView.setImageResource(R.drawable.mic);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels / 3; // 设置每个视图的宽度为屏幕宽度的1/3
        params.height = getResources().getDisplayMetrics().widthPixels / 3; // 设置每个视图的高度为屏幕宽度的1/3
        imageView.setLayoutParams(params);
        gridLayout.addView(imageView);

        // 设置点击事件
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 播放录音
                playAudio(audioFilePath);
            }
        });

        fabStopRecording.setVisibility(View.GONE);
    }

    private void playAudio(String audioFilePath) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {

        mediaRecorder = new MediaRecorder();
        audioFilePath = requireContext().getExternalCacheDir().getAbsolutePath() + "/audio.3gp";
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);

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

    private void dispatchPickImageIntent() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/*");
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK);
    }

    private void dispatchTakePictureIntent() {
        // 调用系统相机拍照
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
                addImageView(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Process image picked from gallery
                Uri imageUri = data.getData();
                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                addImageView(imageBitmap);
            } else if (requestCode == REQUEST_PICK_AUDIO) {
                // Process audio picked from gallery
                Uri audioUri = data.getData();
                Log.d("NoteAddFragment", "audioUri: " + audioUri);
                Toast.makeText(requireContext(), "audioUri: " + audioUri, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addImageView(Bitmap imageBitmap) {
        ImageView imageView = new ImageView(requireContext());
        imageView.setImageBitmap(imageBitmap);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels / 3; // 设置每个视图的宽度为屏幕宽度的1/3
        params.height = getResources().getDisplayMetrics().widthPixels / 3; // 设置每个视图的高度为屏幕宽度的1/3
        imageView.setLayoutParams(params);
        gridLayout.addView(imageView);
    }

}
