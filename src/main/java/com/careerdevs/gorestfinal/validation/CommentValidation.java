package com.careerdevs.gorestfinal.validation;

import com.careerdevs.gorestfinal.models.Comment;
import com.careerdevs.gorestfinal.models.User;
import com.careerdevs.gorestfinal.repositories.CommentRepository;
import com.careerdevs.gorestfinal.repositories.UserRepository;

import java.util.Optional;

public class CommentValidation { // CANNOT CREATE NEW COMMENTS BECAUSE USERREPO IS NULL

    public static ValidationError validateComment(Comment comment, CommentRepository commentRepo, UserRepository userRepo, boolean isUpdate) throws IllegalAccessException {

        ValidationError errors = new ValidationError();

        if (isUpdate) {
            if (comment.getId() == 0) {
                errors.addError("id", "ID cannot be left blank");
            } else {
                Optional<Comment> foundUser = commentRepo.findById(comment.getId());
                if (foundUser.isEmpty()) {
                    errors.addError("id", "No comment found with the ID: " + comment.getId());
                }
            }
        }

        String commentName = comment.getName();
        String commentBody = comment.getBody();
        String commentEmail = comment.getEmail();
        long commentUserId = comment.getPost_id();

        if (commentName == null || commentName.trim().equals("")) {
            errors.addError("name", "Name cannot be left blank.");
        }

        if (commentBody == null || commentBody.trim().equals("")) {
            errors.addError("body", "Body cannot be left blank.");
        }

        if (commentEmail == null || commentEmail.trim().equals("")) {
            errors.addError("email", "Email cannot be left blank.");
        }

        if (commentUserId == 0) {
            errors.addError("post_id", "Post_ID cannot be left blank.");
        } else {
            // is the commentUserId connected to an existing user.
            Optional<User> foundUser = userRepo.findById(commentUserId);

            errors.addError("post_id", "Post_ID is invalid because there is no user found with the ID: " + commentUserId);
        }

        return errors;

    }

}
