package fr.upjv.carnetdevoyage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VoyageAdapter extends RecyclerView.Adapter<VoyageAdapter.VoyageViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Voyage voyage);
    }
    private List<Voyage> voyages;
    private OnItemClickListener listener;

    public VoyageAdapter(List<Voyage> voyages, OnItemClickListener listener) {
        this.voyages = voyages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoyageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VoyageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VoyageViewHolder holder, int position) {
        Voyage voyage = voyages.get(position);
        holder.nom.setText(voyage.getNom_voyage());
        holder.periodicite.setText(voyage.getPeriodicite() + " min");
        holder.itemView.setOnClickListener(v -> listener.onItemClick(voyage));
    }

    @Override
    public int getItemCount() {
        return voyages.size();
    }

    public void setVoyages(List<Voyage> voyages) {
        this.voyages = voyages;
        notifyDataSetChanged();
    }

    static class VoyageViewHolder extends RecyclerView.ViewHolder {
        TextView nom, periodicite;
        VoyageViewHolder(View itemView) {
            super(itemView);
            nom = itemView.findViewById(android.R.id.text1);
            periodicite = itemView.findViewById(android.R.id.text2);
        }
    }
}