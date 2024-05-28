package com.it.attendance.Adapters.ClassDetailLeacturer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.it.attendance.R;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<stdShow> studentList;
    FirebaseFirestore db;
    Context context;
    private final AttendanceViewInterface recyclerViewInterface;


    public StudentAdapter(Context context,List<stdShow> studentList,AttendanceViewInterface recyclerViewInterface) {
        this.studentList = studentList;
        this.context=context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lecturer_student_details_info, parent, false);
        return new ViewHolder(view,recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        stdShow student = studentList.get(position);
        holder.nameText.setText(student.getName());
        holder.emailText.setText(student.getEmail().substring(0,student.getEmail().indexOf("@")));
        Paper.init(context);

        String cNumber= Paper.book().read("courseNumber");

Log.e("yazaaaaaaaaaaan",cNumber);
        db = FirebaseFirestore.getInstance();

        //list to get IsPresent Boolean if equal to true or false
        List<String> presentList = new ArrayList<>();
        presentList.add("present");
        presentList.add("absent");
        presentList.add("exabsent");

        //document reference for attendance
        CollectionReference collectionRef = db.collection("attendance").document(cNumber).collection(student.getEmail());

        Query query = collectionRef.whereIn("IsPresent",presentList);
        query.get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        int present=0;
                        int absent=0;
                        int exabsent=0;

                        for (DocumentSnapshot document : task.getResult()) {
                            String isPresent = document.getString("IsPresent");
                            Log.e("successfully", "Adapter done");

                            if (isPresent.equals("present")) {
                                present = present + 1;
                                Log.e("successfully", "present done" + String.valueOf(present));
                            }//end if
                            else if (isPresent.equals("absent")){
                                absent = absent + 1;
                                Log.e("successfully", "absent done" + String.valueOf(absent));
                            }//end else
                            else {
                                holder.linearLayout.setVisibility(View.VISIBLE);
                                exabsent = exabsent +1;
                            }
                        }//end for loop

                        holder.Present.setText(String.valueOf(present));
                        holder.Absent.setText(String.valueOf(absent));
                        holder.ExAbsent.setText(String.valueOf(exabsent));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("successfully", "Adapter failuer");

                    }
                });



    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameText,emailText,Absent,Present,ExAbsent;
        private LinearLayout linearLayout;

        public ViewHolder(View itemView, AttendanceViewInterface recyclerViewInterface) {
            super(itemView);
            nameText = itemView.findViewById(R.id.student_name_detail_adapter);
            emailText = itemView.findViewById(R.id.student_regNo_detail_adapter);
            Absent=itemView.findViewById(R.id.absent);
            Present=itemView.findViewById(R.id.present);
            ExAbsent=itemView.findViewById(R.id.exabsent);
            linearLayout=itemView.findViewById(R.id.exABSENT);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos=getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
