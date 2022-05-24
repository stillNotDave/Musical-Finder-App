package ru.sfedu.zenin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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

public class UserSettings extends AppCompatActivity {

    private EditText mNameField, mPhoneField;

    private Button buttonBack, buttonConfirm;

    private ImageView profileImage;

    // Для сохранения URI фото в бд
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDataBase;
    private String userId, name, phone, profileImageUrl;
    private Uri resultUri;

    private static final String TAG = "logger";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        mNameField = findViewById(R.id.userProfileName);
        mPhoneField = findViewById(R.id.userProfilePhone);

        profileImage = findViewById(R.id.userProfileImage);

        buttonBack = findViewById(R.id.buttonBack);
        buttonConfirm = findViewById(R.id.buttonConfirm);

        // Работаем с бд и сохраняем данные о пользователе
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mCustomerDataBase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users))
                .child(getString(R.string.customers)).child(userId);

        getUserInformation();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // С помощью этого интента мы сможем перейти вне приложения( в галерею
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });
    }

    public void saveUserInformation(){
        name = mNameField.getText().toString();
        phone = mPhoneField.getText().toString();

        // Обновляем данные о пользователе
        Map userInfo = new HashMap();
        userInfo.put(getString(R.string.map_name), name);
        userInfo.put(getString(R.string.map_phone), phone);
        mCustomerDataBase.updateChildren(userInfo);

        // Загружаем изображение в бд
        if(resultUri != null){
            StorageReference filePath = FirebaseStorage.getInstance().getReference()
                    .child(getString(R.string.profile_images)).child(userId);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                //Log.e(TAG, "bitmap error", e.);

            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20,byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            UploadTask uploadTask = filePath.putBytes(data);

            // Неудачная попытка получить фото из галереи
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserSettings.this, R.string.add_photo_error, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

            // Удачная попытка получить фото из галереи
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // Сохраняем URL в бд
                    Uri downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().getResult();

                    // Обновляем данные о пользователе
                    Map userInfo = new HashMap();
                    userInfo.put(getString(R.string.profile_images_url), downloadUrl);
                    mCustomerDataBase.updateChildren(userInfo);

                    finish();
                    return;
                }
            });

        }
        else{
            // Если пользователь не выбрал фото, закрываем активность
            finish();
        }
    }

    public void getUserInformation(){
        // Слушатель для текущего пользователя
        mCustomerDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Экземпляр DataSnapshot содержит данные из расположения базы данных Firebase
                // Каждый раз, когда вы читаете данные базы данных, вы получаете данные в виде моментального снимка данных.
                // Если экземпляр данных существует, то проверяем не равны ли нулю данные, введенные пользоватем
                // И затем меняем старые данные на новые
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get(getString(R.string.map_name))!= null){
                        name = map.get(getString(R.string.map_name)).toString();
                        mNameField.setText(name);
                    }
                    if(map.get(getString(R.string.map_phone))!= null){
                        phone = map.get(getString(R.string.map_phone)).toString();
                        mPhoneField.setText(phone);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Результат входа в галерею и выбора там фото
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // startActivityForResult(intent, !1!);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            assert data != null;
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profileImage.setImageURI(resultUri);
        }
    }
}