package ru.breffi.smartlibrary.feed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.breffi.smartlibrary.R;
import ru.breffi.story.domain.models.PresentationEntity;

import java.util.ArrayList;
import java.util.List;


public class PresentationAdapter extends RecyclerView.Adapter<PresentationViewHolder> {

    private List<PresentationEntity> data;
    private PresentationClickListener presentationClickListener;
    private Context mContext;
    private ArrayList<PresentationViewHolder> viewHolders = new ArrayList<>();

    public PresentationAdapter(List<PresentationEntity> data,
                               PresentationClickListener presentationClickListener,
                               Context context) {
        this.data = data;
        this.presentationClickListener = presentationClickListener;
        mContext = context;
    }

    @Override
    public PresentationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_presentation, parent, false);
        PresentationViewHolder presentationViewHolder = new PresentationViewHolder(view);
        viewHolders.add(presentationViewHolder);
        return presentationViewHolder;
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(PresentationViewHolder holder, int position) {
        PresentationEntity presentation = data.get(position);
        Log.e("update pres", "onBindViewHolder " + presentation.toString());
        holder.setPresentation(presentation, presentationClickListener, mContext);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public List<PresentationEntity> getData() {
        return data;
    }

    public void setData(List<PresentationEntity> data) {
        this.data = data;
    }

    public ArrayList<PresentationViewHolder> getViewHolders() {
        return viewHolders;
    }

    public void setViewHolders(ArrayList<PresentationViewHolder> viewHolders) {
        this.viewHolders = viewHolders;
    }
}
