package ru.sfedu.zenin.activities;

import static ru.sfedu.zenin.utils.NavigationUtils.openActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import ru.sfedu.zenin.R;

public class Registration extends AppCompatActivity {

    private Button mRegister, back;
    private final Context context = this;
    private EditText mEmail, mPassword, mName;

    private RadioGroup mRadioGroup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private static final String TAG = "logger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Проверяем начилие вошеднего в профиль пользователя
        checkUserExist();

        back = findViewById(R.id.buttonBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(context, LoginAndRegistration.class);
            }
        });

        mRegister = findViewById(R.id.buttonRegister);
        mEmail = findViewById(R.id.emailField);
        mPassword = findViewById(R.id.passwordField);
        mName = findViewById(R.id.nameField);
        mRadioGroup = findViewById(R.id.radioGroup);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
                // здесь был код функции
            }
        });
    }

    // Проверяем есть ли пользователь, зарегистрированный на этом устройстве
    public void checkUserExist(){
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    // Если пользователь есть, пропускаем активности входа/регистрации
                    openActivity(context, Main.class);
                    finish();
                    return;
                }
            }
        };
    }

    public void registerUser(){
        int selectId = mRadioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = findViewById(selectId);

        // Проверка на пустоту радио кнопок
        if(radioButton.getText() == null){
            Log.d(TAG, "radio button is empty");
            Toast.makeText(Registration.this, R.string.no_radio_button, Toast.LENGTH_SHORT).show();
            return;
        }

        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        final String name = mName.getText().toString();

        // Проверка на пустоту других полей
        if(email.isEmpty()) {
            Log.d(TAG, "no mail");
            Toast.makeText(getBaseContext(), R.string.no_email, Toast.LENGTH_SHORT).show();
            return; //если возникает ошибка, с помощью return сразу же выходим из этой ф-ции
        }
        if(password.isEmpty()) {
            Log.d(TAG, "no password");
            Toast.makeText(getBaseContext(), R.string.no_password, Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length() < 8) {
            Log.d(TAG, "short password");
            Toast.makeText(getBaseContext(), R.string.short_password, Toast.LENGTH_SHORT).show();
            return;
        }
        if(name.isEmpty()){
            Log.d(TAG, "no name");
            Toast.makeText(getBaseContext(), R.string.no_name, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Registration.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Если регистрация не успешная
                        if(!task.isSuccessful()){
                            Log.d(TAG, "registration was not successful", task.getException());
                            Toast.makeText(Registration.this, R.string.unsuccessful_registration, Toast.LENGTH_SHORT).show();
                        }
                        // Добавляем пользователя
                        else{
                            String userId = mAuth.getCurrentUser().getUid();

                            if(userId.isEmpty()){
                                Log.d(TAG, "null pointer exception, user id is null");
                            }
                            DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(userId);

                            // Добавляем пользователю имя, стандартное фото профиля и тип профиля
                            Map userInfo = new HashMap();
                            userInfo.put(getString(R.string.name_map), name);
                            userInfo.put(getString(R.string.user_profile_type), radioButton.getText().toString());
                            userInfo.put(getString(R.string.profile_image_url), getString(R.string.default_image_map));

                            currentUserDb.updateChildren(userInfo);
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}