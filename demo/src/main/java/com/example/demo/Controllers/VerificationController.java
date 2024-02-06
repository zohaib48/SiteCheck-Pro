package com.example.demo.Controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Services.UserService;
import com.example.demo.models.User;

@Controller
public class VerificationController {

    @Autowired
    private UserService userService;

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token, Model model, HttpSession session) {
        // Find the user by verification token
        User user = (User) session.getAttribute("tempUser");

        String verification_Token = (String) session.getAttribute("verification_Token");

        if (user != null && verification_Token.equals(token)) {
            try {
                // Mark the user as verified
             
              
                userService.saveUser(user);  // Save the user to the repository after verification
                model.addAttribute("verificationSuccess", "Email verification successful. You can now log in.");
                session.removeAttribute("tempUser");
                return "login";
            }  catch (Exception e) {
                // Log other exceptions
                e.printStackTrace();
                model.addAttribute("registrationError", "Registration failed. Please try again.");
            }
        } else {
            model.addAttribute("registrationError", "Invalid or expired verification token.");
        }

        return "register";  // Redirect back to the registration page with an error message
    }
}
