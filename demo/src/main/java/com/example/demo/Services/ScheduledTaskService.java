package com.example.demo.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.Repository.UserRepository;
import com.example.demo.models.User;

@Service
public class ScheduledTaskService {

    @Autowired
    private UserRepository userRepository;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetRemainingLimits() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            // Check if the remaining limit is not already at the maximum
            if (user.getRemainingLimit() < User.MAX_WEBSITES_TO_CHECK) {
                user.setRemainingLimit(User.MAX_WEBSITES_TO_CHECK);
            }

            // Reset WebsiteRemainingLimit if it's a field in the User class
            // Assuming WebsiteRemainingLimit is a field in User
            if (user.getWebsiteRemainingLimit() < User.MAX_WEBSITES_TO_CHECK) {
                user.setWebsiteRemainingLimit(User.MAX_WEBSITES_TO_CHECK);
            }

            userRepository.save(user);
        }
    }
}
