package android.example.note;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomePageFragment extends Fragment {

    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_UID = "uid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_SIGNATURE = "signature";
    private static final int SELECT_PHOTO = 1;
    String UID = "";
    ImageView imageViewAvatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepagefragment, container, false);

        // 获取用户信息
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        String password = sharedPreferences.getString(KEY_PASSWORD, "");
        UID = sharedPreferences.getString(KEY_UID, "");
        String avatarurl = sharedPreferences.getString(KEY_AVATAR, "");
        String user_name = sharedPreferences.getString(KEY_USERNAME, "username");
        String signature_ = sharedPreferences.getString(KEY_SIGNATURE, "signature");

        TextView username = view.findViewById(R.id.username);
        username.setText(user_name);
        final String[] newUsername = new String[1];

        TextView signature = view.findViewById(R.id.signature);
        signature.setText(signature_);

        Button changeUsername = view.findViewById(R.id.change_username_button);
        
        changeUsername.setOnClickListener(v -> {
            // 修改用户名
            // 弹出对话框
            Log.d("HomePageFragment", "change username");

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("修改用户名");
            final EditText editText = new EditText(requireActivity());
            builder.setView(editText);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newUsername[0] = editText.getText().toString();
                    // 上传服务器
                    updateUsername(newUsername);
                    username.setText(newUsername[0]);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", newUsername[0]);
                    editor.apply();

                }

                private void updateUsername(String[] newUsername) {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .build();

                    RequestBody formBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("uid", UID)
                            .addFormDataPart("username", newUsername[0])
                            .build();
                    Request request = new Request.Builder()
                            .url(getString(R.string.ip) + "/updateusername")
                            .post(formBody)
                            .build();
                    Call call = client.newCall(request);

                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.d("HomePageFragment", "onFailure: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String res = response.body().string();
                            Log.d("HomePageFragment", "onResponse: " + res);
                        }
                    });
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        });

        Button changePassword = view.findViewById(R.id.change_password_button);
        changePassword.setOnClickListener(v -> {
            // 修改密码
            // 弹出对话框
            Log.d("HomePageFragment", "change password");

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("修改密码");

            // 第一个 EditText
            final EditText editText = new EditText(requireActivity());
            editText.setHint("请输入旧密码");

            // 第二个 EditText
            final EditText editText2 = new EditText(requireActivity());
            editText2.setHint("请输入新密码");

            LinearLayout linearLayout = new LinearLayout(requireActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(editText);
            linearLayout.addView(editText2);

            builder.setView(linearLayout);

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String oldPassword = editText.getText().toString();
                    String newPassword = editText2.getText().toString();

                    String password = sharedPreferences.getString(KEY_PASSWORD, "");
                    if (!oldPassword.equals(password)) {
                        Log.d("HomePageFragment", "old password is wrong");
                        // 弹窗提示
                        Toast.makeText(requireActivity(), "旧密码错误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 上传服务器

                    updatePassword(oldPassword, newPassword);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("password", newPassword);
                    editor.apply();

                }

                private void updatePassword(String oldPassword, String newPassword) {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .build();

                    RequestBody formBody = new FormBody.Builder()
                            .add("uid", UID)
                            .add("oldpassword", oldPassword)
                            .add("newpassword", newPassword)
                            .build();
                    Request request = new Request.Builder()
                            .url(getString(R.string.ip) + "/changepwd")
                            .post(formBody)
                            .build();
                    Call call = client.newCall(request);

                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.d("HomePageFragment", "onFailure: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String res = response.body().string();
                            Log.d("HomePageFragment", "onResponse: " + res);
                        }
                    });
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        });

        Button changesignature = view.findViewById(R.id.modify_signature_button);
        changesignature.setOnClickListener(v -> {
            // 修改签名
            // 弹出对话框
            Log.d("HomePageFragment", "change signature");

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("修改签名");
            final EditText editText = new EditText(requireActivity());
            builder.setView(editText);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newsignature = editText.getText().toString();
                    // 上传服务器
                    updateSignature(newsignature);

                    signature.setText(newsignature);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("signature", newsignature);
                    editor.apply();

                }

                private void updateSignature(String signature) {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .build();

                    RequestBody formBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("uid", UID)
                            .addFormDataPart("signature", signature)
                            .build();
                    Request request = new Request.Builder()
                            .url(getString(R.string.ip) + "/updatesignature")
                            .post(formBody)
                            .build();
                    Call call = client.newCall(request);

                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.d("HomePageFragment", "onFailure: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String res = response.body().string();
                            Log.d("HomePageFragment", "onResponse: " + res);
                        }
                    });
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        });

        TextView uidview = view.findViewById(R.id.uid);
        uidview.setText(UID);

        imageViewAvatar = view.findViewById(R.id.avatar);

        imageViewAvatar.setImageURI(Uri.parse(avatarurl));

        imageViewAvatar.setOnClickListener(v -> {
            // 在Fragment的某个地方，例如onCreateView或其他适当的位置
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button logoutbn = requireView().findViewById(R.id.logout_button);
        logoutbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(getActivity(), LoginPage.class);
                startActivity(intent);
            }
        });
 
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                InputStream imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);

                imageViewAvatar.setImageBitmap(selectedBitmap);
                // 保存图片到本地（可选）
                saveImage(selectedBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImage(Bitmap bitmap) {
        // 指定路径
        String dir = requireActivity().getFilesDir().getAbsolutePath();
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        UID = sharedPreferences.getString(KEY_UID, "");
        File imageFile = new File(file, UID + "avatar.jpg");
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            String avatarurl = imageFile.getAbsolutePath();
            
            editor.putString("avatar", avatarurl);
            editor.apply();

            // 上传图片
            // uploadImage(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
