package ru.sfedu.zenin.utils;

import android.content.Context;
import android.content.Intent;

public class NavigationUtils {

    public static void openActivity(Context context,Class<?> cls){
        Intent intent = new Intent(context,cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        return;
    }

}
