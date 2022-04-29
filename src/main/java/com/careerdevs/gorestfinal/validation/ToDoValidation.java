package com.careerdevs.gorestfinal.validation;

import com.careerdevs.gorestfinal.models.ToDo;
import com.careerdevs.gorestfinal.models.User;
import com.careerdevs.gorestfinal.repositories.ToDoRepository;
import com.careerdevs.gorestfinal.repositories.UserRepository;

import java.util.Optional;

public class ToDoValidation { // CANNOT CREATE NEW TODOS BECAUSE USERREPO IS NULL

    public static ValidationError validateToDo(ToDo toDo, ToDoRepository toDoRepo, UserRepository userRepo, boolean isUpdate) throws IllegalAccessException {

        ValidationError errors = new ValidationError();

        if (isUpdate) {
            if (toDo.getId() == 0) {
                errors.addError("id", "ID cannot be left blank");
            } else {
                Optional<ToDo> foundUser = toDoRepo.findById(toDo.getId());
                if (foundUser.isEmpty()) {
                    errors.addError("id", "No todo found with the ID: " + toDo.getId());
                }
            }
        }

        String toDoTitle = toDo.getTitle(); // user_id, title, due_on, status
        String toDoDueOn = toDo.getDue_on();
        String toDoStatus = toDo.getStatus();
        long toDoUserId = toDo.getUser_id();

        if (toDoTitle == null || toDoTitle.trim().equals("")) {
            errors.addError("title", "Title cannot be left blank.");
        }

        if (toDoDueOn == null || toDoDueOn.trim().equals("")) {
            errors.addError("due_on", "Due_On cannot be left blank.");
        }

        if (toDoStatus == null || toDoStatus.trim().equals("")) {
            errors.addError("status", "Status cannot be left blank.");
        }

        if (toDoUserId == 0) {
            errors.addError("user_id", "User_ID cannot be left blank.");
        } else {
            // is the toDoUserId connected to an existing user.
            Optional<User> foundUser = userRepo.findById(toDoUserId);

            errors.addError("user_id", "User_ID is invalid because there is no user found with the ID: " + toDoUserId);
        }

        return errors;
    }

}