package com.example.mygvp.student;

import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygvp.R;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class StudentResultsActivity extends AppCompatActivity {

    Spinner spinnerYear, spinnerSemester;
    Button btnViewResult, btnDownload;
    LinearLayout layoutResult;
    TextView tvStudentInfo, tvCgpa;
    RecyclerView rvSubjects;

    String rollNo, year, sem;
    DataSnapshot cachedSnapshot;
    List<SubjectModel> subjectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_results);

        rollNo = getIntent().getStringExtra("rollNo");

        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        btnViewResult = findViewById(R.id.btnViewResult);
        btnDownload = findViewById(R.id.btnDownload);
        layoutResult = findViewById(R.id.layoutResult);
        tvStudentInfo = findViewById(R.id.tvStudentInfo);
        tvCgpa = findViewById(R.id.tvCgpa);
        rvSubjects = findViewById(R.id.rvSubjects);

        rvSubjects.setLayoutManager(new LinearLayoutManager(this));

        spinnerYear.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"1","2","3","4"}));

        spinnerSemester.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"1","2"}));

        btnViewResult.setOnClickListener(v -> loadResult());
        btnDownload.setOnClickListener(v -> generatePdf());
    }

    private void loadResult() {

        year = spinnerYear.getSelectedItem().toString();
        sem = spinnerSemester.getSelectedItem().toString();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("students")
                .child(rollNo)
                .child("results")
                .child(year)
                .child(sem);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(StudentResultsActivity.this,
                            "Result not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                cachedSnapshot = snapshot;
                subjectList.clear();

                for (DataSnapshot s : snapshot.child("subjects").getChildren()) {
                    subjectList.add(new SubjectModel(
                            s.getKey(),
                            Objects.requireNonNull(s.child("credits").getValue()).toString(),
                            Objects.requireNonNull(s.child("grade").getValue()).toString(),
                            Objects.requireNonNull(s.child("points").getValue()).toString()
                    ));
                }

                subjectList.sort((a, b) ->
                        Integer.parseInt(b.points) - Integer.parseInt(a.points));

                rvSubjects.setAdapter(new SubjectAdapter(subjectList));

                tvStudentInfo.setText(
                        "GAYATRI VIDYA PARISHAD FOR DEGREE AND P.G COURSES(A)\n" +
                                "Register No: " + rollNo +
                                "\nBranch: CSE\nYear: " + year +
                                "\nSemester: " + sem +"\n"
//                                "\n\nThe following grades were secured by the candidate."
                );

                tvCgpa.setText(
                        "CGPA: " + snapshot.child("cgpa").getValue() +
                                "     SGPA: " + snapshot.child("sgpa").getValue()
                );

                layoutResult.setVisibility(View.VISIBLE);
                btnDownload.setVisibility(View.VISIBLE);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void generatePdf() {

        if (cachedSnapshot == null) return;

        PdfDocument pdf = new PdfDocument();
        PdfDocument.Page page = pdf.startPage(
                new PdfDocument.PageInfo.Builder(595, 842, 1).create());

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);

        int y = 40;
        canvas.drawText("GAYATRI VIDYA PARISHAD - SEMESTER RESULT", 40, y, paint);
        y += 20;

        for (SubjectModel s : subjectList) {
            canvas.drawText(
                    s.name + "  " + s.grade + "  " + s.points,
                    40, y, paint);
            y += 18;
        }

        pdf.finishPage(page);

        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "Result_" + rollNo + "_Y" + year + "_S" + sem + ".pdf");

            pdf.writeTo(new FileOutputStream(file));
            pdf.close();

            Toast.makeText(this,
                    "PDF downloaded successfully",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
