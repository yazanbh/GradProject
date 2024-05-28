package com.it.attendance.lecturer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.it.attendance.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.paperdb.Paper;

public class StudentDetail extends AppCompatActivity {
    FirebaseFirestore db;
    ImageView img_btn;
    TextView cname,PresentCount,AbsentCount,percent , alert;
    SwipeRefreshLayout swipeLayout;
    double present,absent , exabsent;
    ChipNavigationBar bottomNavigationView;
    Calendar mCalendar;
    String Email , name , courseNumber;



    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_class_detail);

        boolean isDarkMode = Paper.book().read("DarkMode",false);
        if(isDarkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        alert= findViewById(R.id.stdShowAlert);
        alert.setVisibility(View.GONE);

        pageData();
        //back button of top|left in page
        img_btn=findViewById(R.id.img_btn_back);
        img_btn.setOnClickListener(v -> {
            onBackPressed();
        });


        swipeLayout =  findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform your refresh operation here
                // e.g., fetch new data, update UI
                pageData();

                // Hide the refresh progress indicator once done
                swipeLayout.setRefreshing(false);

            }

        });

        //initialize bottom navbar
        bottomNavigationView = findViewById(R.id.BottomNavigationView);
        //go to another page from navbar
        bottomNavigationView.setOnItemSelectedListener(i -> {
            if(i==R.id.profile){
                startActivity(new Intent(getApplicationContext(), profile.class));
                overridePendingTransition(0,0);
            }
            else if (i== R.id.home){

                startActivity(new Intent(getApplicationContext(), lecturer_Home_Page.class));
                overridePendingTransition(0,0);
            }
        });

    }//end onCreate


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, Class_Detail_lecturer.class));
        overridePendingTransition(0, 0);
    }






    void pageData(){

        CalendarView calendarView = findViewById(R.id.calendarView);
        List<EventDay> events = new ArrayList<>();
         Email = getIntent().getStringExtra("Email");
        assert Email != null;
        Log.e("student email",Email);
         name = getIntent().getStringExtra("name");
         courseNumber = getIntent().getStringExtra("number");
        assert courseNumber != null;
        Log.e("course number",courseNumber);

        present=0;
        absent=0;
        exabsent=0;
        //set course name in text view of top page
        cname=findViewById(R.id.CourseName);
        cname.setText(name);
        //text views for attendance rate
        PresentCount=findViewById(R.id.presentCount);
        AbsentCount=findViewById(R.id.absentCount);
        percent=findViewById(R.id.overallAttendance);


        db = FirebaseFirestore.getInstance();

        //list to get IsPresent Boolean if equal to true or false
        List<String> presentList = new ArrayList<>();
        presentList.add("present");
        presentList.add("absent");
        presentList.add("exabsent");

        //document reference for attendance
        CollectionReference collectionRef = db.collection("attendance").document(courseNumber).collection(Email);

        Query query = collectionRef.whereIn("IsPresent",presentList);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String day = document.getString("day");
                                String month = document.getString("month");
                                String year = document.getString("year");
                                String isPresent = document.getString("IsPresent");

                                //set date to calendar
                                mCalendar = Calendar.getInstance();
                                assert year != null;
                                mCalendar.set(Calendar.YEAR, Integer.parseInt(year));
                                assert month != null;
                                mCalendar.set(Calendar.MONTH, (Integer.parseInt(month))-1);
                                assert day != null;
                                mCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                                assert isPresent != null;
                                if(isPresent.equals("present")) {
                                    present=present+1;
                                    events.add(new EventDay(mCalendar, R.drawable.present));
                                    Log.e("successfully","present done"+present);
                                }//end if
                                else if (isPresent.equals("absent")){
                                    absent= absent+1;
                                    events.add(new EventDay(mCalendar, R.drawable.absent));
                                    Log.e("successfully","absent done"+absent);
                                }//end else if
                                else {
                                    exabsent=exabsent+1;
                                    events.add(new EventDay(mCalendar, R.drawable.exabsent));
                                    Log.e("successfully","exabsent done"+exabsent);
                                }
                            }//end for loop

                            //set present count in text view
                            PresentCount.setText(String.valueOf((int)present));

                            AbsentCount.setText(String.valueOf((int)absent));

                            if(present!=0 || absent!=0) {
                                double OverallAttendance = (((present / (present + absent))) * 100);
                                String formattedPresent = String.format(Locale.ENGLISH,"%.1f", OverallAttendance);
                                percent.setText(formattedPresent+" %");
                                Log.e("percentage",String.valueOf(OverallAttendance));
                            }
                            else{ percent.setText("0 %");}

                            //set event to calendar view
                            calendarView.setEvents(events);
                        } else {
                            Log.w("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });//end OnCompleteListiner

        calendarView.setOnCalendarDayClickListener(calendarDay -> {
            Locale englishLocale = Locale.ENGLISH; // Or Locale.US for US English
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy",englishLocale);
            dateFormat.setCalendar(calendarDay.getCalendar());
            Date today = calendarDay.getCalendar().getTime();
            String selectedDate = dateFormat.format(today);
            //Toast.makeText(context,selectedDate,Toast.LENGTH_SHORT).show();
            Log.e("date",selectedDate);
            StudentUpdateAttendance(selectedDate);

        });

    }//end function pageData
    private void StudentUpdateAttendance(String Date) {
        Dialog dialog = new Dialog(this,android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_attendance);
        dialog.setCanceledOnTouchOutside(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        RadioButton presentRadio = dialog.findViewById(R.id.present);
        RadioButton absentRadio = dialog.findViewById(R.id.absent);
        RadioButton exabsentRadio = dialog.findViewById(R.id.exabsent);
        TextView save = dialog.findViewById(R.id.saveStatus);
        TextView cancel = dialog.findViewById(R.id.cancel_btn);

        // Assuming you have a reference to your Firestore collection
        CollectionReference attendanceRef = FirebaseFirestore.getInstance().collection("attendance")
                .document(courseNumber)
                .collection(Email);

        // Query Firestore to get the attendance status for the selected date
        attendanceRef.document(Date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String status = document.getString("IsPresent");
                            // Set the appropriate radio button based on the status
                            if (status != null) {
                                switch (status) {
                                    case "present":
                                        presentRadio.setChecked(true);
                                        break;
                                    case "absent":
                                        absentRadio.setChecked(true);
                                        break;
                                    case "exabsent":
                                        exabsentRadio.setChecked(true);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            dialog.show();
                        } else {
                            // Document doesn't exist, show a toast or handle it as per your requirement
                            Toast.makeText(StudentDetail.this, "يرجى اختيار تاريخ صجيج", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Error getting document: ", task.getException());
                    }
                });


        // Saving the updated status back to Firestore
        save.setOnClickListener(v -> {
            String newStatus;
            if (presentRadio.isChecked()) {
                newStatus = "present";
            } else if (absentRadio.isChecked()) {
                newStatus = "absent";
            } else {
                newStatus = "exabsent";
            }

            // Update the status in Firestore
            attendanceRef.document(Date)
                    .update("IsPresent", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        // Handle success
                        Log.d("Firestore", "DocumentSnapshot successfully updated!");
                        Toast.makeText(StudentDetail.this,"Success",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // Refresh the page data after updating
                        pageData();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failures
                        Toast.makeText(StudentDetail.this,"Failure",Toast.LENGTH_SHORT).show();
                        Log.w("Firestore", "Error updating document", e);
                        // You might want to inform the user about the failure
                    });
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
    }

}//end class
