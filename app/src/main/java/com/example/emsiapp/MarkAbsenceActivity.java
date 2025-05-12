package com.example.emsiapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MarkAbsenceActivity extends AppCompatActivity {

    private Spinner groupSpinner;
    private Spinner siteSpinner;
    private TextView dateTextView;
    private RecyclerView studentsRecyclerView;
    private EditText remarksEditText;
    private Button saveButton;

    private StudentAttendanceAdapter studentAdapter;
    private List<Student> studentList;
    private List<String> groupList;
    private List<String> siteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_absence);

        // Initialisation des vues
        initializeViews();

        // Configuration de la date actuelle
        setupCurrentDate();

        // Chargement des données
        loadData();

        // Configuration des spinners
        setupSpinners();

        // Configuration de la liste des étudiants
        setupStudentList();

        // Configuration du bouton d'enregistrement
        setupSaveButton();
    }

    private void initializeViews() {
        groupSpinner = findViewById(R.id.groupSpinner);
        siteSpinner = findViewById(R.id.siteSpinner);
        dateTextView = findViewById(R.id.dateTextView);
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        remarksEditText = findViewById(R.id.remarksEditText);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        dateTextView.setText(currentDate);
    }

    private void loadData() {
        // Charger les groupes (exemple - à remplacer par une vraie source de données)
        groupList = new ArrayList<>();
        groupList.add("Groupe A");
        groupList.add("Groupe B");
        groupList.add("Groupe C");

        // Charger les sites (exemple - à remplacer par une vraie source de données)
        siteList = new ArrayList<>();
        siteList.add("Site Principal");
        siteList.add("Annexe Nord");
        siteList.add("Annexe Sud");

        // Charger la liste d'étudiants (sera mise à jour lorsqu'un groupe est sélectionné)
        studentList = new ArrayList<>();
    }

    private void setupSpinners() {
        // Configuration du spinner pour les groupes
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, groupList);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupAdapter);

        // Configuration du spinner pour les sites
        ArrayAdapter<String> siteAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, siteList);
        siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        siteSpinner.setAdapter(siteAdapter);

        // Écouter les changements de sélection de groupe
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadStudentsByGroup(groupList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ne rien faire
            }
        });
    }

    private void loadStudentsByGroup(String groupName) {
        // Cette méthode devrait charger les étudiants depuis une base de données
        // Pour l'exemple, nous créons une liste fictive
        studentList.clear();

        // Exemple de données
        if (groupName.equals("Groupe A")) {
            studentList.add(new Student(1, "Martin Dupont"));
            studentList.add(new Student(2, "Sophie Leclerc"));
            studentList.add(new Student(3, "Thomas Bernard"));
        } else if (groupName.equals("Groupe B")) {
            studentList.add(new Student(4, "Marie Lambert"));
            studentList.add(new Student(5, "Lucas Moreau"));
            studentList.add(new Student(6, "Emma Petit"));
        } else if (groupName.equals("Groupe C")) {
            studentList.add(new Student(7, "Hugo Dubois"));
            studentList.add(new Student(8, "Camille Durand"));
            studentList.add(new Student(9, "Nicolas Robert"));
        }

        // Mettre à jour l'adaptateur
        studentAdapter.notifyDataSetChanged();
    }

    private void setupStudentList() {
        studentAdapter = new StudentAttendanceAdapter(studentList);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentsRecyclerView.setAdapter(studentAdapter);
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> saveAttendance());
    }

    private void saveAttendance() {
        // Récupérer les données
        String selectedGroup = groupSpinner.getSelectedItem().toString();
        String selectedSite = siteSpinner.getSelectedItem().toString();
        String currentDate = dateTextView.getText().toString();
        String remarks = remarksEditText.getText().toString();

        // Récupérer la liste des présences
        List<AttendanceRecord> attendanceRecords = new ArrayList<>();
        for (int i = 0; i < studentList.size(); i++) {
            Student student = studentList.get(i);
            boolean isPresent = student.isPresent();
            attendanceRecords.add(new AttendanceRecord(
                    student.getId(),
                    currentDate,
                    isPresent,
                    selectedGroup,
                    selectedSite
            ));
        }

        // Enregistrer les données (ceci devrait être implémenté avec une base de données réelle)
        saveAttendanceRecords(attendanceRecords, remarks);

        // Afficher un message de confirmation
        Toast.makeText(this, "Présences enregistrées avec succès", Toast.LENGTH_SHORT).show();
    }

    private void saveAttendanceRecords(List<AttendanceRecord> records, String remarks) {
        // Cette méthode devrait enregistrer les données dans une base de données
        // Pour cet exemple, nous allons simplement afficher les données dans la console

        System.out.println("===== ENREGISTREMENT DES PRÉSENCES =====");
        System.out.println("Groupe: " + groupSpinner.getSelectedItem().toString());
        System.out.println("Site: " + siteSpinner.getSelectedItem().toString());
        System.out.println("Date: " + dateTextView.getText().toString());
        System.out.println("Remarques: " + remarks);
        System.out.println("Présences:");

        for (AttendanceRecord record : records) {
            String studentName = "";
            for (Student student : studentList) {
                if (student.getId() == record.getStudentId()) {
                    studentName = student.getName();
                    break;
                }
            }
            System.out.println(" - " + studentName + ": " + (record.isPresent() ? "Présent" : "Absent"));
        }

        System.out.println("=======================================");

        // Dans une vraie application, vous utiliseriez une base de données
        // DatabaseHelper dbHelper = new DatabaseHelper(this);
        // dbHelper.saveAttendanceRecords(records, remarks);
    }

    // Classe pour représenter un étudiant
    public static class Student {
        private int id;
        private String name;
        private boolean present;

        public Student(int id, String name) {
            this.id = id;
            this.name = name;
            this.present = false; // Par défaut, non coché
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isPresent() {
            return present;
        }

        public void setPresent(boolean present) {
            this.present = present;
        }
    }

    // Classe pour représenter un enregistrement de présence
    public static class AttendanceRecord {
        private int studentId;
        private String date;
        private boolean present;
        private String group;
        private String site;

        public AttendanceRecord(int studentId, String date, boolean present, String group, String site) {
            this.studentId = studentId;
            this.date = date;
            this.present = present;
            this.group = group;
            this.site = site;
        }

        public int getStudentId() {
            return studentId;
        }

        public String getDate() {
            return date;
        }

        public boolean isPresent() {
            return present;
        }

        public String getGroup() {
            return group;
        }

        public String getSite() {
            return site;
        }
    }
}