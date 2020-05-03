package com.example.studious;

import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MatchRunnable implements Runnable {
    private String username;
    private DatabaseReference dataRef;
    private ArrayList<String> matchesList;

    MatchRunnable(String username) {
        this.username = username;
        dataRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void run() {
        DatabaseReference userMatches = dataRef.child("UserMatches").child(username);

        userMatches.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String matchString = dataSnapshot.child("Matches").getValue(String.class);
                if(matchString == null)
                    matchString = "";
                method1(matchString);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void method1(String matchString) {
        String[] array = matchString.split(", ");
        List<String> list = Arrays.asList(array);
        matchesList = new ArrayList<String>(list);
        DatabaseReference allUsers = dataRef.child("UserPref");
        allUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String> users = new ArrayList<String>();
            ArrayList<String> courses = new ArrayList<String>();
            ArrayList<String> days = new ArrayList<String>();
            ArrayList<String> locations = new ArrayList<String>();
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    users.add(postSnapshot.getKey());
                    //for(DataSnapshot userChild: postSnapshot.getChildren()) {
                    days.add(postSnapshot.child("Days").getValue(String.class));
                    courses.add(postSnapshot.child("Courses").getValue(String.class));
                    locations.add(postSnapshot.child("Locations").getValue(String.class));
                    //}
                }
                    method2(matchesList, users, courses, days, locations);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void method2(ArrayList<String> matches, ArrayList<String> users, ArrayList<String> courses,
                        ArrayList<String> days, ArrayList<String> locations) {
        ArrayList<ArrayList<String>> courses2 = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> days2 = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> locations2 = new ArrayList<ArrayList<String>>();

        for(int i = 0; i < courses.size(); i++) {
            String[] array = courses.get(i).split(", ");
            List<String> placeHolder = Arrays.asList(array);
                courses2.add(new ArrayList<String>(placeHolder));

        }

        for(int i = 0; i < days.size(); i++) {
            String[] array = days.get(i).split(", ");
            List<String> placeHolder = Arrays.asList(array);
            days2.add(new ArrayList<String>(placeHolder));
        }

        for(int i = 0; i < locations.size(); i++) {
            String[] array = locations.get(i).split(", ");
            List<String> placeHolder = Arrays.asList(array);
            locations2.add(new ArrayList<String>(placeHolder));
        }

        matchMaker(matches, users, courses2, days2, locations2);
    }

    public void matchMaker(ArrayList<String> matches, ArrayList<String> users,
                           ArrayList<ArrayList<String>> courses, ArrayList<ArrayList<String>> days,
                           ArrayList<ArrayList<String>> locations) {

        //Get the current user's preferences, then remove the user's preferences from that list
        int userIndex = users.indexOf(username);

        ArrayList<String> userCourses = new ArrayList<String>(courses.get(userIndex));
        ArrayList<String> userDays = new ArrayList<String>(days.get(userIndex));
        ArrayList<String> userLocations = new ArrayList<String>(locations.get(userIndex));

        users.remove(userIndex);
        courses.remove(userIndex);
        days.remove(userIndex);
        locations.remove(userIndex);

        //this will remove all matches the user has from the users list
        for(int i = 0; i < matches.size(); i++) {
            int matchIndex = users.indexOf(matches.get(i));
            if(matchIndex >= 0) {
                users.remove(matchIndex);
                courses.remove(matchIndex);
                days.remove(matchIndex);
                locations.remove(matchIndex);
            }
        }

        //Narrow down list of possible matches
        //Courses
        ArrayList<Integer> indicesToRemove = new ArrayList<Integer>();
        for(int i = 0; i < users.size(); i++) {
            for(int j = 0; j < courses.get(i).size(); j++) {
                if(userCourses.contains(courses.get(i).get(j))) {
                    j = courses.get(i).size();
                } else if(j == (courses.get(i).size() - 1)) {
                    indicesToRemove.add(i);
                }
            }
        }

        //sorts indicestoRemove numerically. This allows us to remove the highest indices first so
        // that the indices are not changing in users
        Collections.sort(indicesToRemove);
        if(indicesToRemove.size() > 0) {
            for (int i = indicesToRemove.size() -1; i >= 0; i--) {
                users.remove(indicesToRemove.get(i));
                courses.remove(indicesToRemove.get(i));
                days.remove(indicesToRemove.get(i));
                locations.remove(indicesToRemove.get(i));
            }
        }
        if(users.size() == 1) {
            matchCreator(users.get(0));
            return;
        } else if(users.size() == 0) {
            return;
        }
        //Days
        indicesToRemove = new ArrayList<Integer>();
        for(int i = 0; i < users.size(); i++) {
            for(int j = 0; j < days.get(i).size(); j++) {
                if(userDays.contains(days.get(i).get(j))) {
                    j = days.get(i).size();
                } else if(j == (days.get(i).size() - 1)) {
                    indicesToRemove.add(i);
                }
            }
        }

        Collections.sort(indicesToRemove);
        for(int i = indicesToRemove.size(); i > -1; i--){
            users.remove(indicesToRemove.get(i));
            courses.remove(indicesToRemove.get(i));
            days.remove(indicesToRemove.get(i));
            locations.remove(indicesToRemove.get(i));
            if(users.size() == 1) {
                matchCreator(users.get(0));
                return;
            }
        }
        if(users.size() == 1) {
            matchCreator(users.get(0));
            return;
        } else if(users.size() == 0) {
            return;
        }

        //Location
        indicesToRemove = new ArrayList<Integer>();
        for(int i = 0; i < users.size(); i++) {
            for(int j = 0; j < locations.get(i).size(); j++) {
                if(userLocations.contains(locations.get(i).get(j))) {
                    j = locations.get(i).size();
                } else if(j == (locations.get(i).size() - 1)) {
                    indicesToRemove.add(i);
                }
            }
        }

        Collections.sort(indicesToRemove);
        for(int i = indicesToRemove.size(); i > -1; i--){
            users.remove(indicesToRemove.get(i));
            courses.remove(indicesToRemove.get(i));
            days.remove(indicesToRemove.get(i));
            locations.remove(indicesToRemove.get(i));
            if(users.size() == 1) {
                matchCreator(users.get(0));
                return;
            }
        }
        if(users.size() == 0) {
            return;
        }
        matchCreator(users.get(0));


    }

    private void matchCreator(final String matchToAdd) {
        DatabaseReference matchFB = dataRef.child("UserMatches");

        matchFB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newMatches = "";
                String userNewMatches = "";
                String oldMatches = dataSnapshot.child(matchToAdd).child("Matches").getValue().toString();
                if(oldMatches.isEmpty())
                    newMatches = username;
                else
                    newMatches = oldMatches + ", " + username;


                String userMatches = dataSnapshot.child(username).child("Matches").getValue().toString();
                if(userMatches.isEmpty())
                    userNewMatches = matchToAdd;
                else
                    userNewMatches = userMatches + ", " + matchToAdd;

                updateMatches(userNewMatches, newMatches, matchToAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void updateMatches(String userMatches, String otherMatches, String otherUser) {
        dataRef.child("UserMatches").child(username).child("Matches").setValue(userMatches);
        dataRef.child("UserMatches").child(otherUser).child("Matches").setValue(otherMatches);
    }

}
