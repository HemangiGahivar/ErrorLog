package com.wipro.demo.controller;

import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.wipro.demo.bean.User;
import com.wipro.demo.service.UserService;

import java.util.Collections;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping(value={"/user"})
@Timed
public class UserController 
{
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	static String logName = "error-log";
	


@Autowired
        @GetMapping("/")
        public String greet() {
        logger.info("printing hello!!!");
                return "Hello!";
        }



	@Autowired
	UserService userService;
    

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
     @Timed(value = "users.ById",percentiles = {0.5, 0.95, 0.999}, histogram = true)
    public ResponseEntity<User> getUserById(@PathVariable("id") int id) {
        System.out.println("Fetching User with id " + id);
        User user = userService.findById(id);
        if (user == null) {
        	
        	  ResponseEntity<User> responseEntity=new ResponseEntity<User>(HttpStatus.NOT_FOUND);
            //return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
            logger.error("HttpStatus.Not_found");
            return responseEntity;
        }
             logger.info("userfound by id");
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }
    
	 @PostMapping(value="/create",headers="Accept=application/json")
     @Timed(value = "add.users",percentiles = {0.5, 0.95, 0.999}, histogram = true)
	 public ResponseEntity<Void> createUser(@RequestBody User user, UriComponentsBuilder ucBuilder){
	     System.out.println("Creating User "+user.getName());
	     userService.createUser(user);
	     HttpHeaders headers = new HttpHeaders();
	     headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(user.getId()).toUri());
	     logger.info("user created");
	     return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	 }

	 @GetMapping(value="/get", headers="Accept=application/json")
     @Timed(value = "users.list",percentiles = {0.5, 0.95, 0.999}, histogram = true)
	 public List<User> getAllUser() {	 
	  List<User> tasks=userService.getUser();
	  return tasks;
	 }

	@PutMapping(value="/update", headers="Accept=application/json")
	public ResponseEntity<String> updateUser(@RequestBody User currentUser)
	{
	User user = userService.findById(currentUser.getId());
	if (user==null) {
		logger.error("user not found");
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		
	}
	user.setId(currentUser.getId());
	user.setName(currentUser.getName());
	user.setCountry(currentUser.getCountry());
	userService.update(user);
	return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	@DeleteMapping(value="/{id}", headers ="Accept=application/json")
    @Timed(value = "delete.users",percentiles = {0.5, 0.95, 0.999}, histogram = true)
	public ResponseEntity<User> deleteUser(@PathVariable("id") int id){
		User user = userService.findById(id);
		if (user == null) {
			logger.error("user not found");
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		userService.deleteUserById(id);
		return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
	}
	
	@PatchMapping(value="/{id}", headers="Accept=application/json")
	public ResponseEntity<User> updateUserPartial(@PathVariable("id") int id, @RequestBody User currentUser){
		User user = userService.findById(id);
		if(user ==null){
			logger.error("user not found");
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		
		userService.updatePartially(currentUser, id);
	
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
}
