package android.example.note;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import okhttp3.ResponseBody;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RegisterPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.registerpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 获取当前时间戳
    public static long getTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    private String starttime = "0";
    private String endtime = "0";

    public void sendcode(View view) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        // 读取输入框中的邮箱地址
        EditText editText = findViewById(R.id.editTextTextEmailAddress);
        String email = editText.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "请输入邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        // 判断是否在3分钟内发送过验证码
        if (Long.parseLong(starttime) != 0) {
            endtime = String.valueOf(getTimestamp());
            if (Long.parseLong(endtime) - Long.parseLong(starttime) < 180) {
                Toast.makeText(this, "3分钟内已发送过验证码", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.ip) + "/sendcode")
                .post(body)
                .build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("RegisterPage", "run: " + "验证码发送失败");
                        Toast makeText = Toast.makeText(RegisterPage.this, "验证码发送失败", Toast.LENGTH_SHORT);
                        makeText.setGravity(Gravity.CENTER, 0, 0);
                        makeText.show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Log.d("RegisterPage", "onResponse: " + response.body().string());
                String res = response.body().string();
                // 从返回的字符串中提取验证码
                String code = res.substring(res.indexOf("code") + 7, res.indexOf("code") + 10);
                // 判断返回码
                if (code.equals("200")) {
                    // 异步弹出Toast
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RegisterPage", "run: " + "验证码发送成功");
                            starttime = String.valueOf(getTimestamp());
                            Toast makeText = Toast.makeText(RegisterPage.this, "验证码发送成功", Toast.LENGTH_SHORT);
                            makeText.setGravity(Gravity.CENTER, 0, 0);
                            makeText.show();
                        }
                    });
                } else if (code.equals("403")){
                    // 异步弹出Toast
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RegisterPage", "run: " + "该邮箱已被注册");
                            Toast makeText = Toast.makeText(RegisterPage.this, "该邮箱已被注册", Toast.LENGTH_SHORT);
                            makeText.setGravity(Gravity.CENTER, 0, 0);
                            makeText.show();
                        }
                    });
                } else if (code.equals("202")){
                    // 异步弹出Toast
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RegisterPage", "run: " + "3分钟内已发送过验证码");
                            Toast makeText = Toast.makeText(RegisterPage.this, "3分钟内已发送过验证码", Toast.LENGTH_SHORT);
                            makeText.setGravity(Gravity.CENTER, 0, 0);
                            makeText.show();
                        }
                    });
                } else if (code.equals("401")){
                    // 异步弹出Toast
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RegisterPage", "run: " + "邮箱格式错误");
                            Toast makeText = Toast.makeText(RegisterPage.this, "邮箱格式错误", Toast.LENGTH_SHORT);
                            makeText.setGravity(Gravity.CENTER, 0, 0);
                            makeText.show();
                        }
                    });
                }
            }
        });

    }

    public void back(View view) {
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
    }

    public void register(View view) throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        // 读取输入框中的邮箱地址和验证码
        EditText editText = findViewById(R.id.editTextTextEmailAddress);
        String email = editText.getText().toString();
        EditText editText2 = findViewById(R.id.editTextTextPassword);
        String password = editText2.getText().toString();
        EditText editText3 = findViewById(R.id.editTextNumber);
        String code = editText3.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "请输入邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        } else if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        } else if (code.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("code", code)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.ip) + "/register")
                .post(body)
                .build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("RegisterPage", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                String code = res.substring(res.indexOf("code") + 7, res.indexOf("code") + 10);
                if (code.equals("200")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RegisterPage", "run: " + "注册成功");
                            Toast.makeText(RegisterPage.this, "注册成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterPage.this, LoginPage.class);
                            startActivity(intent);
                        }
                    });
                } else if (code.equals("403")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RegisterPage", "run: " + "验证码错误");
                            Toast.makeText(RegisterPage.this, "验证码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (code.equals("202")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RegisterPage", "run: " + "验证码已过期");
                            Toast.makeText(RegisterPage.this, "验证码已过期", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (code.equals("401")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RegisterPage", "run: " + "邮箱格式错误");
                            Toast.makeText(RegisterPage.this, "邮箱格式错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }       
            }
        });

    }

}
