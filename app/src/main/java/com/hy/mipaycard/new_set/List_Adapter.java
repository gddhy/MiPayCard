package com.hy.mipaycard.new_set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hy.mipaycard.R;

import java.util.List;

public class List_Adapter extends ArrayAdapter<List_card> {
    private int resourceId;

    public List_Adapter(Context context, int textViewResourceId, List<List_card> objects) {
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        List_card list_card = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView==null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.cardImage = (ImageView) view.findViewById(R.id.list_card_image);
            viewHolder.cardName = (TextView) view.findViewById(R.id.list_card_name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        Glide.with(getContext()).load(list_card.getImageFile()).into(viewHolder.cardImage);
        //viewHolder.cardImage.setImageBitmap(BitmapFactory.decodeStream(fileInputStream));
        viewHolder.cardName.setText(list_card.getName());
        return view;
    }

    class ViewHolder {
        ImageView cardImage;
        TextView cardName;
    }
}
