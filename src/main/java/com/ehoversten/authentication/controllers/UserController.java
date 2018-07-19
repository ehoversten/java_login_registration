package com.ehoversten.authentication.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ehoversten.authentication.models.User;
import com.ehoversten.authentication.services.UserService;

@Controller
public class UserController {
	
	// --- DEPENDENCY INJECTION --- //
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @RequestMapping("/registration")
    public String registerForm(@ModelAttribute("user") User user) {
        return "registration";
    }
    @RequestMapping("/login")
    public String login() {
        return "login";
    }
    
    @RequestMapping(value="/registration", method=RequestMethod.POST)
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, HttpSession session) {
        // if result has errors, return the registration page (don't worry about validations just now)

    	if(result.hasErrors()) {
    		return "registration";
    	}
        // else, save the user in the database, save the user id in session ...
    	User newUser = userService.registerUser(user);
    	session.setAttribute("user", newUser.getId());
    	// and redirect them to the /home route
    	return "redirect:/home";
    }
    
    @RequestMapping(value="/login", method=RequestMethod.POST)
    public String loginUser(@RequestParam("email") String email, @RequestParam("password") String password, Model model, HttpSession session) {
        // if the user is authenticated, save their user id in session
    	if(userService.authenticateUser(email, password)) {
    		User user = userService.findByEmail(email);
    		Long id = user.getId();
    		session.setAttribute("user", id);
    		return "redirect:/home";
    	}
        // else, add error messages and return the login page
    	else {
    		model.addAttribute("error", "Could not log you in");
    		return "login";
    	}
    }
    
    @RequestMapping("/home")
    public String home(HttpSession session, Model model) {
        // get user from session, save them in the model and return the home page
    	Long id = (Long) session.getAttribute("user");
    	if(id == null) {
    		return "redirect:/login";
    	}
    	
    	User user = userService.findUserById(id);
    	model.addAttribute("user", user);
    	return "home";
    }
    
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        // invalidate session
    	session.setAttribute("user", null);
        // redirect to login page
    	return "login";
    }
}
