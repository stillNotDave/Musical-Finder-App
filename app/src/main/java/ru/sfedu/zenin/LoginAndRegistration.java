package ru.sfedu.zenin;

import static ru.sfedu.zenin.NavigationUtils.openActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LoginAndRegistration extends AppCompatActivity {

    private Button mLogin, mRegister;
    private final Context context = this;

    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_registration);

        mLogin = findViewById(R.id.buttonSignIn);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(context, Login.class);
                finish();
                return;
            }
        });





        mRegister = findViewById(R.id.buttonRegistration);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(context, Registration.class);
                finish();
                return;
            }
        });

    }

    // Обработка нажатия на системную кнопку "назад"
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();//закрытия всплывающего тоста
            super.onBackPressed();
            return;
        }
        else{
            backToast = Toast.makeText(getBaseContext(), R.string.back_toast,Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

}