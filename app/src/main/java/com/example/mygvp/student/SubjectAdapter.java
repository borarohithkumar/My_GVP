package com.example.mygvp.student;

import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mygvp.R;
import java.util.List;

public class SubjectAdapter
        extends RecyclerView.Adapter<SubjectAdapter.SubjectVH> {

    List<SubjectModel> list;

    public SubjectAdapter(List<SubjectModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public SubjectVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject_row, parent, false);
        return new SubjectVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectVH h, int p) {
        SubjectModel s = list.get(p);
        h.tvSub.setText(s.name);
        h.tvCredits.setText(s.credits);
        h.tvGrade.setText(s.grade);
        h.tvPoints.setText(s.points);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class SubjectVH extends RecyclerView.ViewHolder {

        TextView tvSub, tvCredits, tvGrade, tvPoints;

        public SubjectVH(@NonNull View v) {
            super(v);
            tvSub = v.findViewById(R.id.tvSub);
            tvCredits = v.findViewById(R.id.tvCredits);
            tvGrade = v.findViewById(R.id.tvGrade);
            tvPoints = v.findViewById(R.id.tvPoints);
        }
    }
}
