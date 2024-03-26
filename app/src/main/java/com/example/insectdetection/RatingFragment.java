package com.example.insectdetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;


import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;

public class RatingFragment extends Fragment implements CommentAdapter.OnClickListener {

    private SharedPreferences sharedPreferences;


    private DatabaseReference commentsRef;
    private CommentAdapter commentAdapter;
    private ArrayList<Comments> mCommentList = new ArrayList<>();
    private TextView comment;
    private DatabaseReference ratingsRef;
    private TextView averageRatingTextView;

    private int currentPage=1 ;
    private int startPosition = 0;
    private int commentsPerPage = 5 ;
    private int endPosition = 4;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

        // Initialize Firebase Database
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://insectdetection-c56d4-default-rtdb.asia-southeast1.firebasedatabase.app") ;
        commentsRef = db.getReference("comments");
        ratingsRef = db.getReference("ratings");

        //getAllComments();
        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);


        // Initialize TextView to display average rating
        averageRatingTextView = view.findViewById(R.id.average_rating_textview);


        // Calculate and display average rating
        calculateAndDisplayAverageRating();

        // RecyclerView setup
        RecyclerView recyclerView = view.findViewById(R.id.RecyclerviewComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(getContext(), getCurrentUserID(), mCommentList, this, this);
        recyclerView.setAdapter(commentAdapter);
        FloatingActionButton postComment = view.findViewById(R.id.post_comment);
        comment = view.findViewById(R.id.comment);


        // Next button click listener
        Button nextButton = view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startPosition < mCommentList.size() - 1) { // Check if there are more comments to show
                    startPosition += 5; // Increment end position by 5
                    loadComments();
                }
            }
        });

        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        listenForCommentUpdates(5,0,updatedCommentList -> {
            updateCommentListUI(updatedCommentList);
        });


        // WebView setup
        WebView webView = view.findViewById(R.id.webView);
        String video = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/Oo_IZysj4F4?si=XxLiqqxWi1GgYrtn\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>";
        webView.loadData(video, "text/html", "utf-8");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        // Rating functionality
        RatingBar mRating = view.findViewById(R.id.rating);
        Button mSubmit = view.findViewById(R.id.submit);
        final TextView mThank = view.findViewById(R.id.thank);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = mRating.getRating();
                // Store the rating in Firebase Realtime Database
                DatabaseReference ratingsRef = db.getReference("ratings");
                ratingsRef.push().setValue(rating)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Firebase", "Rating added successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Firebase", "Error adding rating: " + e.getMessage());
                            }
                        });

                mThank.setVisibility(View.VISIBLE);
                mSubmit.setVisibility(View.INVISIBLE);

                if (rating == 5) {
                    mThank.setText(R.string.thank_you);
                } else if (rating == 0) {
                    mThank.setText(R.string.very_disappointing);
                } else {
                    mThank.setText(R.string.thank_you_for_your_feedback);
                }
            }
        });

        return view;
    }



    private void calculateAndDisplayAverageRating() {
        ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalRating = 0;
                int ratingCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double rating = snapshot.getValue(Double.class);
                    if (rating != null) {
                        totalRating += rating;
                        ratingCount++;
                    }
                }
                double averageRating = ratingCount > 0 ? totalRating / ratingCount : 0.0;
                averageRatingTextView.setText("Average Rating: " + String.format("%.1f", averageRating));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event if needed
            }
        });
    }


    //লিমিট দেওয়া কয়টা করে কমেন্ট নিয়ে আসবে তা
    private void listenForCommentUpdates(int limit, int startPosition, Consumer<ArrayList<Comments>> callback) {

        commentsRef.orderByKey()
                .startAt(String.valueOf(startPosition))
                .limitToFirst(limit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<Comments> updatedCommentList = new ArrayList<>();
                        for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                            Comments comment = commentSnapshot.getValue(Comments.class);
                            if (comment != null) {
                                updatedCommentList.add(comment);
                            }
                        }
                        callback.accept(updatedCommentList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle onCancelled

                    }
                });
    }



    @SuppressLint("NotifyDataSetChanged")
    private void postComment() {
        String commentText = comment.getText().toString();

        if (!commentText.isEmpty()) {
            long currentTime = new Date().getTime();
            Toast.makeText(getContext(), commentText, Toast.LENGTH_SHORT).show();


            String profileUrl = sharedPreferences.getString("userProfileUrl", "");

            // Retrieve profile name from SharedPreferences
            String userName = sharedPreferences.getString("userName", "");


            Comments instanceOfComment = new Comments(commentText, userName, currentTime, profileUrl);
            mCommentList.add(instanceOfComment);
            comment.setText("");
            commentAdapter.notifyDataSetChanged();
            updateCommentListInDatabase(mCommentList);
        } else {
            Toast.makeText(getContext(), "Enter a comment.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCommentListInDatabase(ArrayList<Comments> commentList) {
        commentsRef.setValue(commentList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Comment list updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Error updating comment list: " + e.getMessage());
                    }
                });
    }





    @Override
    public void onLikeClick(int position, TextView likeCountTextView, ImageButton likeButton) {


        Comments comment = mCommentList.get(position);
        if (comment == null) {
            return;
        }

        String userId = getCurrentUserID();
        if (userId == null) {
            return;
        }

        ArrayList<String> likedBy = comment.getLikedBy();
        ArrayList<String> dislikeBy=comment.getDislikedBy();

        if(!dislikeBy.contains(userId)) {
            if (likedBy.contains(userId)) {
                likedBy.remove(userId);
                likeButton.setImageResource(R.drawable.baseline_thumb_up_24);
            } else {
                likedBy.add(userId);
                likeButton.setImageResource(R.drawable.thum_up_after_liked);
            }
        }

        int likeCount = likedBy.size();
        likeCountTextView.setText(String.valueOf(likeCount));
        likeCountTextView.setVisibility(likeCount > 0 ? View.VISIBLE : View.INVISIBLE);

        updateCommentListInDatabase(mCommentList);
        commentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDisLikeClick(int position, TextView disLikeCountTextView, ImageButton dislikeButton) {


        Comments comment = mCommentList.get(position);
        if (comment == null) {
            return;
        }

        String userId = getCurrentUserID();
        if (userId == null) {
            return;
        }

        ArrayList<String> likedBy = comment.getLikedBy();
        ArrayList<String> dislikedBy = comment.getDislikedBy();


        if (!likedBy.contains(userId)) {
            if (dislikedBy.contains(userId)) {
                dislikedBy.remove(userId);
                dislikeButton.setImageResource(R.drawable.baseline_thumb_down_24);
            } else {
                dislikedBy.add(userId);
                dislikeButton.setImageResource(R.drawable.baseline_thumb_down_after_dislike_24);
            }

            int dislikeCount = dislikedBy.size();
            disLikeCountTextView.setText(String.valueOf(dislikeCount));
            disLikeCountTextView.setVisibility(dislikeCount > 0 ? View.VISIBLE : View.INVISIBLE);

            updateCommentListInDatabase(mCommentList);
            commentAdapter.notifyDataSetChanged();
        }
    }


    private String getCurrentUserID() {
        return FirebaseAuth.getInstance().getCurrentUser().toString() ;
    }

    private void updateCommentListUI(ArrayList<Comments> updatedCommentList) {
        mCommentList.clear();
        mCommentList.addAll(updatedCommentList);
        commentAdapter.notifyDataSetChanged();
    }



    /* এখানেই pagination এর কাজ হচ্ছে । currentPage এর মান বাড়াচ্ছি প্রতিবার শুধু আর তা পুরনো কমেন্ট এর সাথে যোগ করে স্ক্রিনে দেখাচ্ছি
     */

    private void loadComments() {
        startPosition = (currentPage - 1) * commentsPerPage;
        listenForCommentUpdates(commentsPerPage, startPosition, newComments -> {

            if (!newComments.isEmpty()) {
                mCommentList.addAll(newComments);
                commentAdapter.notifyDataSetChanged();
                currentPage++;
            } else {
                Toast.makeText(getContext(), "No more comments to load", Toast.LENGTH_SHORT).show();
            }
        });
    }









}