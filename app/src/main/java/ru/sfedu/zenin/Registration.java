package ru.sfedu.zenin;

import static ru.sfedu.zenin.NavigationUtils.openActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Registration extends AppCompatActivity {

    private Button register, back;
    private Context context = this;
    private EditText mEmail, mPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Проверка на отсутсвие пользователя на этом телефоне
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Toast.makeText(Registration.this, R.string.successful_registration, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Registration.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        back = findViewById(R.id.buttonBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(context, RegistrationInfo.class);
            }
        });

        register = findViewById(R.id.buttonRegister);
        mEmail = findViewById(R.id.emailField);
        mPassword = findViewById(R.id.passwordField);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                if(email.isEmpty()) {
                    Toast.makeText(getBaseContext(), R.string.no_email, Toast.LENGTH_SHORT).show();
                    return; //если возникает ошибка, с помощью return сразу же выходим из этой ф-ции
                }
                if(password.isEmpty()) {
                    Toast.makeText(getBaseContext(), R.string.no_password, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length() < 7) {
                    Toast.makeText(getBaseContext(), R.string.short_password, Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Registration.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Если регистрация не успешная
                                if(!task.isSuccessful()){
                                    Toast.makeText(Registration.this, R.string.unsuccessful_registration,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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