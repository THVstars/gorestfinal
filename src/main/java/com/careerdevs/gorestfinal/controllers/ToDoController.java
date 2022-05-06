package com.careerdevs.gorestfinal.controllers;

import com.careerdevs.gorestfinal.models.ToDo;

import com.careerdevs.gorestfinal.repositories.ToDoRepository;
import com.careerdevs.gorestfinal.utils.ApiErrorHandling;
import com.careerdevs.gorestfinal.validation.ToDoValidation;
import com.careerdevs.gorestfinal.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/todos")
public class ToDoController {

    @Autowired
    ToDoRepository toDoRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getToDoById (@PathVariable("id") String id) {

        try {

            if (ApiErrorHandling.isStrNaN(id)) {
                System.out.println("NOT AN ID.");
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + " ID is not a valid ID.");
            }

            long uID = Long.parseLong(id);

            Optional<ToDo> foundToDo = toDoRepository.findById(uID);

            if (foundToDo.isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, " ToDo Not Found With ID: " + id);
            }

            return new ResponseEntity<>(foundToDo, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e) {
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllToDos() {

        try {

            Iterable<ToDo> allToDos = toDoRepository.findAll();

            return new ResponseEntity<>(allToDos, HttpStatus.OK);

        } catch (Exception e) {
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteToDoById(@PathVariable("id") String id) {

        try {
            if (ApiErrorHandling.isStrNaN(id)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + " id not a valid ID");
            }

            long uID = Long.parseLong(id);

            Optional<ToDo> foundToDo = toDoRepository.findById(uID);

            if (foundToDo.isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, " ToDo not found with ID:" + id);
            }

            toDoRepository.deleteById(uID);

            return new ResponseEntity<>(foundToDo, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e) {
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @DeleteMapping("/deleteall")
    public ResponseEntity<?> deleteAllToDos() {

        try {

            long totalToDos = toDoRepository.count();
            toDoRepository.deleteAll();

            return new ResponseEntity<>("ToDos Deleted: " + totalToDos, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e) {
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @PostMapping("/")
    public ResponseEntity<?> createToDo(@RequestBody ToDo newToDo) {

        try {

            ValidationError errors = ToDoValidation.validateToDo(newToDo, toDoRepository, false);
            if (errors.hasError()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, errors.toJSONString());
            }

            ToDo createdToDo = toDoRepository.save(newToDo);

            return new ResponseEntity<>(createdToDo, HttpStatus.CREATED);

        } catch(HttpClientErrorException e) {
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e) {
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<?> uploadToDoById(@PathVariable("id") String toDoId, RestTemplate restTemplate) {

        try {

            if (ApiErrorHandling.isStrNaN(toDoId)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, toDoId + " is not a valid ID.");
            }

            long uID = Long.parseLong(toDoId);

            String url = "https://gorest.co.in/public/v2/todos/" + uID;

            ToDo foundToDo = restTemplate.getForObject(url, ToDo.class);

            System.out.println(foundToDo);

            assert foundToDo != null;

            if (foundToDo == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "ToDo with ID: " + uID + " not found.");
            }

            ToDo savedToDo = toDoRepository.save(foundToDo);

            return new ResponseEntity<>(savedToDo, HttpStatus.CREATED);

        } catch (HttpClientErrorException e) {
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e) {
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @PostMapping("/uploadall")
    public ResponseEntity<?> uploadAll(RestTemplate restTemplate) {

        try {

            String url = "https://gorest.co.in/public/v2/todos";

            ResponseEntity<ToDo[]> response = restTemplate.getForEntity(url, ToDo[].class);

            ToDo[] firstPageToDos = response.getBody();

            if (firstPageToDos == null) {
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to GET first page of todos from GoREST.");
            }

            ArrayList<ToDo> allToDos = new ArrayList<>(Arrays.asList(firstPageToDos));

            HttpHeaders responseHeaders = response.getHeaders();

            String totalPages = Objects.requireNonNull(responseHeaders.get("X-Pagination-Pages").get(0));
            int totalPgNum = Integer.parseInt(totalPages);

            for (int i = 2; i <= totalPgNum; i++) {

                String pageUrl = url + "?page=" + i;
                ToDo[] pageToDos = restTemplate.getForObject(pageUrl, ToDo[].class);

                if (firstPageToDos == null) {
                    throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to GET page " + i + " of todos from GoREST");
                }

                allToDos.addAll(Arrays.asList(firstPageToDos));

            }

            toDoRepository.saveAll(allToDos);

            return new ResponseEntity<>("ToDos Created: " + allToDos.size(), HttpStatus.OK);

        } catch (Exception e) {
            return ApiErrorHandling.genericApiError(e);
        }

    }

    @PutMapping("/")
    public ResponseEntity<?> updateToDo(@RequestBody ToDo updateToDo) {

        try {

            ValidationError errors = ToDoValidation.validateToDo(updateToDo, toDoRepository, true);
            if (errors.hasError()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, errors.toJSONString());
            }

            ToDo savedToDo = toDoRepository.save(updateToDo);

            return new ResponseEntity<>(savedToDo, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());

        } catch (Exception e) {
            return ApiErrorHandling.genericApiError(e);
        }

    }

}