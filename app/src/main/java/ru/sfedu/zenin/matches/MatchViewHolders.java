package ru.sfedu.zenin.matches;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.sfedu.zenin.R;
import ru.sfedu.zenin.activities.Chat;

public class MatchViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView mMatchId, mMatchName;
    public ImageView mMatchImage;

    public MatchViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mMatchId = itemView.findViewById(R.id.matchId);
        mMatchName = itemView.findViewById(R.id.matchName);

        mMatchImage = itemView.findViewById(R.id.matchImage);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), Chat.class);
        Bundle bundle = new Bundle();
        bundle.putString(String.valueOf(R.string.match_id), mMatchId.getText().toString());
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }
}
