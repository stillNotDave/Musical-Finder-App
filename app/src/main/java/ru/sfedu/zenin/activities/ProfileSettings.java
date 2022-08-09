package ru.sfedu.zenin.activities;

import static ru.sfedu.zenin.utils.NavigationUtils.openActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ru.sfedu.zenin.R;

public class ProfileSettings extends AppCompatActivity {

    private EditText mNameField, mPhoneField;

    private Button mBack, mConfirm, signOut;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDb;

    private String userId, name, phone, profileImageUrl, userProfileType;

    private Uri resultUri;

    private final Context context = this;

    private static final String TAG = "logger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        //String userProfileType = getIntent().getExtras().getString(getString(R.string.user_profile_type));

        mNameField = findViewById(R.id.profileName);
        mPhoneField = findViewById(R.id.profilePhone);

        mProfileImage = findViewById(R.id.profileImage);

        mConfirm = findViewById(R.id.buttonConfirm);
        mBack = findViewById(R.id.buttonCancel);
        signOut = findViewById(R.id.buttonSignOut);

        // Для работы с конкретным пользователем чьи данные потом будут меняться
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mUsersDb = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.users))
                .child(userId);

        getUserInformation();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // С помощью этого интента получаем возможность переходить из приложения в галерею
                Intent intent = new Intent(Intent.ACTION_PICK);
                // Получаем тим файла который нужно получить
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        // Сохраняем информацию в бд
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });
    }

    // Выход из учетной записи пользовтеля
    public void logoutUser(){
        mAuth.signOut();
        openActivity(context, LoginAndRegistration.class);
        finish();
        return;
    }

    // Получаем данные о конкретном пользователе
    private void getUserInformation(){
        mUsersDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get(getString(R.string.name_map))!=null){
                        name = map.get(getString(R.string.name_map)).toString();
                        mNameField.setText(name);
                    }
                    if(map.get(getString(R.string.phone_map))!=null){
                        phone = map.get(getString(R.string.phone_map)).toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get(getString(R.string.user_profile_type))!=null){
                        userProfileType = map.get(getString(R.string.user_profile_type)).toString();

                    }
                    Glide.clear(mProfileImage);
                    if(map.get(getString(R.string.profile_image_url))!=null){
                        profileImageUrl = map.get(getString(R.string.profile_image_url)).toString();

                        // Ставим стандартное фото зарегистрировавшемуся пользователю
                        switch (profileImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.drawable.default_user_profile_image).into(mProfileImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                                break;
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    // Сохраняем информацию от пользователя
    private void saveUserInformation(){
        name = mNameField.getText().toString();
        phone = mPhoneField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put(getString(R.string.name_map), name);
        userInfo.put(getString(R.string.phone_map), phone);
        mUsersDb.updateChildren(userInfo);

        if(resultUri != null){
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child(getString(R.string.profile_images))
                    .child(userId);
            // Помещаем в битмап то фото, которое выбрал пользователь
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(ProfileSettings.this, R.string.upload_image_error, Toast.LENGTH_LONG).show();
                //e.printStackTrace();
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            // Фото добавилось удачно
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put(getString(R.string.profile_image_url), uri.toString());
                            mUsersDb.updateChildren(newImage);

                            finish();
                            return;
                        }
                        // Фото добавилось неудачно
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileSettings.this, R.string.upload_image_error, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    });
                }
            });

        }
        else{
            finish();
        }

        Toast.makeText(ProfileSettings.this, R.string.change_data, Toast.LENGTH_SHORT).show();
    }

    // Помещаем выбранное пользователем фото как фото профиля
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}