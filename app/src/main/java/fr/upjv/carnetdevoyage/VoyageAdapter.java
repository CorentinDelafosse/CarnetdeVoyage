package fr.upjv.carnetdevoyage;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.List;

public class VoyageAdapter extends RecyclerView.Adapter<VoyageAdapter.VoyageViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Voyage voyage);
    }

    private List<Voyage> voyages;
    private OnItemClickListener listener;
    private Context context;
    private String emailUtilisateur;
    private boolean isSearchMode;

    public VoyageAdapter(List<Voyage> voyages, OnItemClickListener listener, Context context, boolean isSearchMode) {
        this.voyages = voyages;
        this.listener = listener;
        this.context = context;
        this.isSearchMode = isSearchMode;

        // Récupère l'email de l'utilisateur connecté pour savoir si on peut afficher le bouton supprimer
        this.emailUtilisateur = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                : "";
    }

    @NonNull
    @Override
    public VoyageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voyage, parent, false);
        return new VoyageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VoyageViewHolder holder, int position) {
        Voyage voyage = voyages.get(position);
        holder.nom.setText(voyage.getNom_voyage());
        holder.periodicite.setText("Périodicité : " + voyage.getPeriodicite() + " min");

        holder.itemView.setOnClickListener(v -> listener.onItemClick(voyage));

        // On cache le bouton supprimer si on est en mode recherche ou si ce n'est pas le propriétaire
        boolean isOwner = voyage.getEmail() == null || voyage.getEmail().equals(emailUtilisateur);
        holder.btnDelete.setVisibility((!isSearchMode && isOwner) ? View.VISIBLE : View.GONE);

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Supprimer ce voyage ?")
                    .setMessage("Tous les points GPS liés seront aussi supprimés.")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String chemin = "users/" + emailUtilisateur + "/voyages/" + voyage.getNom_voyage();

                        db.collection("users")
                                .document(emailUtilisateur)
                                .collection("voyages")
                                .document(voyage.getNom_voyage())
                                .collection("points")
                                .get()
                                .addOnSuccessListener(query -> {
                                    for (QueryDocumentSnapshot doc : query) {
                                        doc.getReference().delete();
                                    }
                                    db.collection("users")
                                            .document(emailUtilisateur)
                                            .collection("voyages")
                                            .document(voyage.getNom_voyage())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                voyages.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, voyages.size());
                                                Toast.makeText(context, "Voyage supprimé", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> Log.e("SUPPRESSION", "Erreur suppression voyage", e));
                                })
                                .addOnFailureListener(e -> Log.e("SUPPRESSION", "Erreur suppression points", e));
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
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
        Button btnDelete;

        VoyageViewHolder(View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.text_nom_voyage);
            periodicite = itemView.findViewById(R.id.text_periodicite);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
