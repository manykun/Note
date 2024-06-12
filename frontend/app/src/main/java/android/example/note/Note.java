package android.example.note;

import android.app.Application;
import android.content.Context;

import com.xuexiang.xormlite.InternalDataBaseRepository;
import com.xuexiang.xormlite.annotation.DataBase;
import com.xuexiang.xormlite.enums.DataBaseType;

@DataBase(name = "internal", type = DataBaseType.INTERNAL)
public class Note extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        InternalDataBaseRepository.getInstance()
                .setIDatabase(new InternalDataBase())  //设置内部存储的数据库实现接口
                .init(this);

    }

}
