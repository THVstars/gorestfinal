package com.careerdevs.gorestfinal.validation;

import com.careerdevs.gorestfinal.models.Post;
import com.careerdevs.gorestfinal.models.User;
import com.careerdevs.gorestfinal.repositories.PostRepository;
import com.careerdevs.gorestfinal.repositories.UserRepository;

import java.util.Optional;

public class PostValidation { // CANNOT CREATE NEW POSTS BECAUSE USERREPO IS NULL

    public static ValidationError validatePost(Post post, PostRepository postRepo, UserRepository userRepo, boolean isUpdate) throws IllegalAccessException {

        ValidationError errors = new ValidationError();

        if (isUpdate) {
            if (post.getId() == 0) {
                errors.addError("id", "ID cannot be left blank");
            } else {
                Optional<Post> foundUser = postRepo.findById(post.getId());
                if (foundUser.isEmpty()) {
                    errors.addError("id", "No post found with the ID: " + post.getId());
                }
            }
        }

        String postTitle = post.getTitle();
        String postBody = post.getBody();
        long postUserId = post.getUser_id();

        if (postTitle == null || postTitle.trim().equals("")) {
            errors.addError("title", "Title cannot be left blank.");
        }

        if (postBody == null || postBody.trim().equals("")) {
            errors.addError("body", "Body cannot be left blank.");
        }

        if (postUserId == 0) {
            errors.addError("user_id", "User_ID cannot be left blank.");
        } else {
            // is the postUserId connected to an existing user.
            Optional<User> foundUser = userRepo.findById(postUserId);

            errors.addError("user_id", "User_ID is invalid because there is no user found with the ID: " + postUserId);
        }

        return errors;
    }

}
