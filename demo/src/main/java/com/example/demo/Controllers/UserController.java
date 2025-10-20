package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.UUID;
import javax.servlet.http.Cookie;
import com.example.demo.Services.EmailService;
import com.example.demo.Services.UserService;
import com.example.demo.models.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

     @GetMapping("/home")
     public String home(Model model, HttpSession session) {
     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
       
           
            String email = auth.getName(); // Get logged-in user's email
            User user = userService.getUserByEmail(email);
            session.setAttribute("userInfo", user);
            model.addAttribute("user", session.getAttribute("userInfo"));
        }

   
     return "index";
}

@Autowired
private EmailService emailService;

@PostMapping("/register")
public String registerUser(@ModelAttribute User user, Model model, HttpServletRequest request, HttpSession session) {
    try {
        // Check if the email already exists in the database
            if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("registrationError", "Email is already registered. Please use a different email address.");
            return "register";
             }

        // Generate a random verification token
        String verificationToken = UUID.randomUUID().toString();
        session.setAttribute("verification_Token", verificationToken);
        session.setAttribute("tempUser", user);

      

        // Send verification email
        String verificationLink = getVerificationLink(request, verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);

        model.addAttribute("verification", "Verification link has been sent to your email. Please verify and then log in to the system.");

        // Redirect to a page informing the user to check their email for verification
        return "register";
    } catch (Exception e) {
        e.printStackTrace();  // Log other exceptions

        // Add an attribute to the model for generic error handling
        model.addAttribute("registrationError", "Registration failed. Please try again.");

        // Redirect back to the registration page
        return "register";
    }
}
private String getVerificationLink(HttpServletRequest request, String verificationToken) {
    String appUrl = request.getScheme() + "://" + request.getServerName();
    return appUrl + ":8080/verify?token=" + verificationToken;
}
    @GetMapping("/login")
    public String showLoginForm(HttpServletRequest request, Model model,HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            
        return "redirect:/home";
        }

        // Check for remember-me cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("remember-me") && cookie.getValue().equals("true")) {
                    return "redirect:/home";
                }
            }
        }

      
        return  "login";
    }

     @GetMapping("/logout")
    public String logout() {
        

        return "redirect:/login?logout"; // Redirect to the login page with a logout parameter
    }



    @GetMapping("/SitemapChecker")
    public String SitemapChecker(Model model, HttpSession session) {
      

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
       
           
        String email = auth.getName(); // Get logged-in user's email

     
    
        User user = userService.getUserByEmail(email);
        session.setAttribute("userInfo", user);
            
        model.addAttribute("user", session.getAttribute("userInfo"));

        }


  

        return "SitemapChecker";
    }

     @GetMapping("/WebsiteChecker")
    public String showStatusCodePage(Model model, HttpSession session) {
        
        
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
       
           
        String email = auth.getName(); // Get logged-in user's email

     
    
        User user = userService.getUserByEmail(email);
        session.setAttribute("userInfo", user);
            
        model.addAttribute("user", session.getAttribute("userInfo"));
        
        }


        return "WebsiteChecker";
    }
}
