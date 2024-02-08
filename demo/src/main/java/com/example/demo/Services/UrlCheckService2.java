package com.example.demo.Services;

import org.apache.commons.validator.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpSession;

import com.example.demo.Repository.UserRepository;
import com.example.demo.models.StatusCodeResult;
import com.example.demo.models.User;

@Service
public class UrlCheckService2 {
  
        @Autowired
        private UserRepository userLimitRepository;

    @Async
    public String checkUrls(String websiteUrl, int numThreads, HttpSession session, int depthLevel)
            throws IOException, InterruptedException {

                boolean isValidUrl = validateUrl(websiteUrl);
                if (!isValidUrl) {
                    System.out.println("invalid url");
                       session.setAttribute("error", "URL IS INVALID PLEASE ENTER AGAIN");
                    return "statusCode";
                }


        session.setAttribute("executorService", Executors.newFixedThreadPool(numThreads));
        session.setAttribute("results", new CopyOnWriteArrayList<>());
        session.setAttribute("statusCounts", new ConcurrentHashMap<>());
        session.setAttribute("error", null);
        

        ExecutorService executorService = (ExecutorService) session.getAttribute("executorService");
       
        List<StatusCodeResult> results = (List<StatusCodeResult>) session.getAttribute("results");
 
        Map<Integer, Integer> statusCounts = (Map<Integer, Integer>) session.getAttribute("statusCounts");
        
        int websitesToCheck =(int) session.getAttribute("urlToCheck");

   

        int websitesToChecks = processSitemapUrls(websiteUrl, numThreads, executorService, results, statusCounts, session,
                depthLevel,websitesToCheck);

        // Wait for all tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        session.setAttribute("InProcess", false);
        System.out.println("program ended");
        System.out.println("web to check inside" + websitesToChecks);
        
      
        int count = results.size();
        System.out.println("Result list size: " + count);

        User userInfo = (User) session.getAttribute("userInfo");
        int remainingLimit = userInfo.getWebsiteRemainingLimit() - count;
        User Updated_user = updateUserLimit(userInfo.getEmail(), remainingLimit);
        session.setAttribute("userInfo", Updated_user);

        if (remainingLimit == 0) {
            System.out.print("Limit reached");
            session.setAttribute("limitReached", true);
        }
        
        session.setAttribute("Idle", true);
    
        return "statusCode";
    }

       private boolean validateUrl(String url) {
        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid(url);
    }

    private int processSitemapUrls(String websiteUrl, int numThreads, ExecutorService executorService,
            List<StatusCodeResult> results, Map<Integer, Integer> statusCounts, HttpSession session, int depthLevel,int websitesToCheck)
            throws IOException, InterruptedException {
          
           

        if (depthLevel == 0) {
            int statusCode = getStatusCode(websiteUrl);
            System.out.print("url to check=" + websiteUrl);

            results.add(new StatusCodeResult(websiteUrl, statusCode));
            statusCounts.put(statusCode, statusCounts.getOrDefault(statusCode, 0) + 1);

        }
        if (depthLevel == 1) {
            Document doc = Jsoup.connect(websiteUrl).get();

            // Extract all anchor tags
            List<String> urlsToCheck = new ArrayList<>();

            for (Element anchorElement : doc.select("a[href]")) {
                String url = anchorElement.attr("abs:href");
                urlsToCheck.add(url);
                
                  if (urlsToCheck.size() >= websitesToCheck) {
                        break; // Exit the loop if the desired number of iterations is reached
                  }

                

            }
            System.out.println("check =" + urlsToCheck.size());

           
            session.setAttribute("InProcess", true);
            for (String url : urlsToCheck) {
                // Execute the status checking task in a separate thread
                executorService.execute(() -> {
                    try {
                        System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Processing URL: " + url);
                      
                        int statusCode = getStatusCode(url);
                        results.add(new StatusCodeResult(url, statusCode));
                        statusCounts.put(statusCode, statusCounts.getOrDefault(statusCode, 0) + 1);
                     
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle the error as needed
                    } 
                });
            }

         
        }
         System.out.println("web to check returning  =" + websitesToCheck);
        return websitesToCheck;
    }

    private int getStatusCode(String url) throws IOException {
        URL websiteURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) websiteURL.openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        connection.setRequestMethod("GET");
        return connection.getResponseCode();
    }

     private User updateUserLimit(String email, int remainingLimit) {
        User userLimit = userLimitRepository.findByEmail(email);

        if (userLimit != null) {
            // Update the existing user limit
            userLimit.setWebsiteRemainingLimit(remainingLimit);
        } 

        // Save the user limit (either existing or new) in the repository
        User Updateduser = userLimitRepository.save(userLimit);
        return Updateduser;
    }

}
