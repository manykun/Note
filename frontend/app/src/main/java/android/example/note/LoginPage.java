package android.example.note;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// import com.xuexiang.xui.XUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
 
public class LoginPage extends AppCompatActivity{
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_UID = "uid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_SIGNATURE = "signature";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.loginpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        String password = sharedPreferences.getString(KEY_PASSWORD, "");
        String uid = sharedPreferences.getString(KEY_UID, "");
        if (!email.equals("") && !password.equals("")) {
            // 自动登录
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            RequestBody formBody = new FormBody.Builder()
                    .add("email", email)
                    .add("password", password)
                    .build();
            Request request = new Request.Builder()           
                    .url(getString(R.string.ip) + "/login")
                    .post(formBody)
                    .build();
            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("LoginPage", "onFailure: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = response.body().string();
                    Log.d("LoginPage", "onResponse: " + res);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(res);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        Intent intent = new Intent(LoginPage.this, MainActivity.class);
                        startActivity(intent);
                    } else if (code.equals("401")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginPage.this, "密码错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (code.equals("403")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginPage.this, "该邮箱未注册", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    public void toRegister(View view) {
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
    }

    public void toLogin(View view) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        EditText emailText = findViewById(R.id.editTextTextEmailAddress);
        EditText passwordText = findViewById(R.id.editTextTextPassword);
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        if (email.equals("") || password.equals("")) {
            Toast.makeText(LoginPage.this, "邮箱或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.ip) + "/login")
                .post(formBody)
                .build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("LoginPage", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(res);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String code = jsonObject.optString("code");

                if (code.equals("200")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_EMAIL, email);
                    editor.putString(KEY_PASSWORD, password);

                    // 获取uid
                    String uid = jsonObject.optString("uid");
                    editor.putString(KEY_UID, uid);

                    String username = jsonObject.optString("username");
                    editor.putString(KEY_USERNAME, username);

                    String avatar = jsonObject.optString("avatar");
                    editor.putString(KEY_AVATAR, avatar);

                    String signature = jsonObject.optString("signature");
                    editor.putString(KEY_SIGNATURE, signature);

                    editor.apply();
                    Log.d("LoginPage", "uid: " + uid);

                    Intent intent = new Intent(LoginPage.this, MainActivity.class);
                    startActivity(intent);
                } else if (code.equals("401")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginPage.this, "密码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (code.equals("403")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginPage.this, "该邮箱未注册", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

}
