package com.it.attendance.lecturer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.it.attendance.Adapters.Students.StudentAttendance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface FirestoreCallback {
        void onCallback(Map<String, List<StudentAttendance>> attendanceMap);
    }

    public void getAttendanceData(String courseNumber, List<String> studentEmails, FirestoreCallback firestoreCallback) {
        Map<String, List<StudentAttendance>> attendanceMap = new HashMap<>();

        int[] remainingTasks = {studentEmails.size()};

        for (String studentEmail : studentEmails) {
            CollectionReference studentRef = db.collection("attendance").document(courseNumber).collection(studentEmail);
            studentRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<StudentAttendance> attendanceList = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        StudentAttendance attendance = doc.toObject(StudentAttendance.class);
                        attendanceList.add(attendance);
                    }
                    attendanceMap.put(studentEmail, attendanceList);
                }

                if (--remainingTasks[0] == 0) {
                    firestoreCallback.onCallback(attendanceMap);
                }
            });
        }
    }
}
