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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ru.sfedu.zenin.R;

public class Login extends AppCompatActivity {

    private final Context context = this;

    private Button signIn, back;

    private EditText mEmail, mPassword;

    private static final String TAG = "logger";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Проверяем есть ли пользователь, зарегистрированный на этом устройстве
        checkUserExist();

        // Возвращаемся назад по кнопке Назад
        back = findViewById(R.id.buttonBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(context, LoginAndRegistration.class);
                finish();
                return;
            }
        });

        signIn = findViewById(R.id.buttonSignIn);
        mEmail = findViewById(R.id.emailField);
        mPassword = findViewById(R.id.passwordField);

        // Вход в профиль
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logInUserProfile();
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
                if(user != null){
                    // Если пользователь есть, пропускаем активности входа/регистрации
                    openActivity(context, Main.class);
                    finish();
                    return;
                }
            }
        };
    }

    public void logInUserProfile(){
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        // Проверка на пустые поля
        if(email.isEmpty()) {
            Log.d(TAG, "no mail");
            Toast.makeText(getBaseContext(), R.string.no_email, Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.isEmpty()) {
            Log.d(TAG, "no password");
            Toast.makeText(getBaseContext(), R.string.no_password, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Если вход в профиль не успешный
                        if(!task.isSuccessful()){
                            Log.d(TAG, "Wrong login or password");
                            Toast.makeText(Login.this, R.string.wrong_login_or_password, Toast.LENGTH_SHORT).show();
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