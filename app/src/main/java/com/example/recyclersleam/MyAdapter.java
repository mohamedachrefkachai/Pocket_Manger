package com.example.recyclersleam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclersleam.Entity.Personne;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    ArrayList<Personne> personnes;

    public MyAdapter(ArrayList<Personne> personnes) {
        this.personnes = personnes;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.img.setImageResource(personnes.get(position).getImage());
        holder.nom.setText(personnes.get(position).getNom());
        holder.prenom.setText(personnes.get(position).getPrenom());

        holder.img.setOnClickListener(e -> {
                    //Toast.makeText(e.getContext(), "salut "+personnes.get(position).getNom(),Toast.LENGTH_LONG).show();
                }
        );


    }

    @Override
    public int getItemCount() {
        return this.personnes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView nom, prenom;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            nom = itemView.findViewById(R.id.nom);
            prenom = itemView.findViewById(R.id.prenom);


        }
    }
}
