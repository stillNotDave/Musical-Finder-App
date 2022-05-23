package ru.sfedu.zenin;

import static ru.sfedu.zenin.NavigationUtils.openActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegistrationInfo extends AppCompatActivity {


    // ЭТОТ КЛАСС ДОБАВИТЬ УЖЕ ПОТОМ


    private final Context context = this;
    private Button buttonBack, buttonContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_info);

        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(context, LoginAndRegistration.class);
                finish();
                return;
            }
        });

        buttonContinue = findViewById(R.id.buttonContinueRegistration);
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(context, Registration.class);
                finish();
                return;
            }
        });
    }
}