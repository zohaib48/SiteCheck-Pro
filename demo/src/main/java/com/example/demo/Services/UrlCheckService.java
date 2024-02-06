package com.example.demo.Services;

import org.apache.commons.validator.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.example.demo.Repository.UserRepository;
import com.example.demo.models.StatusCodeResult;
import com.example.demo.models.User;

@Service
public class UrlCheckService {

    @Autowired
    private UserRepository userLimitRepository;

    public String checkUrls(String sitemapUrl, int numThreads, HttpSession session, Model model)
            throws IOException, InterruptedException {

        boolean isValidUrl = validateUrl(sitemapUrl);
        if (!isValidUrl) {
            System.out.println("Invalid URL");
            session.setAttribute("error", "URL is invalid. Please enter a valid URL.");
            return "statusCode";
        }

        session.setAttribute("executorService", Executors.newFixedThreadPool(numThreads));
        session.setAttribute("results", new CopyOnWriteArrayList<>());
        session.setAttribute("statusCounts", new ConcurrentHashMap<>());
        session.setAttribute("error", null);

        ExecutorService executorService = (ExecutorService) session.getAttribute("executorService");
        List<StatusCodeResult> results = (List<StatusCodeResult>) session.getAttribute("results");
        Map<Integer, Integer> statusCounts = (Map<Integer, Integer>) session.getAttribute("statusCounts");
        int websitesToCheck = (int) session.getAttribute("urlToCheck");
        System.out.println("URL to check " + websitesToCheck);

         

        int websitesToCheckNow = processSitemapUrls(sitemapUrl, numThreads, executorService, results, statusCounts,
                session, websitesToCheck);

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        session.setAttribute("InProcess", false);
        System.out.println("Program ended");
        System.out.println("Websites to check inside: " + websitesToCheckNow);

        int count = results.size();
        System.out.println("Result list size: " + count);

        User userInfo = (User) session.getAttribute("userInfo");
        int remainingLimit = userInfo.getRemainingLimit() - count;
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
    private int processSitemapUrls(String sitemapUrl, int numThreads, ExecutorService executorService,
            List<StatusCodeResult> results, Map<Integer, Integer> statusCounts, HttpSession session,
            int websitesToCheck) throws IOException {

       
      
        if (containsUrlLocTags(sitemapUrl)) {
            Document doc = Jsoup.connect(sitemapUrl).get();
            int totalWebsitesToCheck = Math.min(websitesToCheck, doc.select("urlset > url > loc").size());
            System.out.println("Websites to check: " + totalWebsitesToCheck);

            // Check user limit before processing each URL

            for (Element urlElement : doc.select("urlset > url > loc").subList(0, totalWebsitesToCheck)) {
                String url = urlElement.text();
                session.setAttribute("InProcess", true);

                // Execute the status checking task in a separate thread
                executorService.execute(() -> {
                    try {
                        System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Processing URL: " + url);
                        int statusCode = getStatusCode(url);
                        results.add(new StatusCodeResult(url, statusCode));
                        statusCounts.put(statusCode, statusCounts.getOrDefault(statusCode, 0) + 1);

                        // Update user limit in the database

                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle the error as needed
                    }
                });
            }

            websitesToCheck += totalWebsitesToCheck;
            session.setAttribute("urlToCheck", websitesToCheck);
        } else {
            Document doc = Jsoup.connect(sitemapUrl).get();
            for (Element sitemapElement : doc.select("sitemap > loc")) {
                String nestedSitemapUrl = sitemapElement.text();

                processSitemapUrls(nestedSitemapUrl, numThreads, executorService, results, statusCounts,
                        session, websitesToCheck);

                int checkStatus = (int) session.getAttribute("urlToCheck");
                if (checkStatus >= User.MAX_WEBSITES_TO_CHECK) {
                    System.out.println("Time to break");
                    break;
                }
            }
        }

        return websitesToCheck;
    }

    private User updateUserLimit(String email, int remainingLimit) {
        User userLimit = userLimitRepository.findByEmail(email);

        if (userLimit != null) {
            // Update the existing user limit
            userLimit.setRemainingLimit(remainingLimit);
        } 

        // Save the user limit (either existing or new) in the repository
        User Updateduser = userLimitRepository.save(userLimit);
        return Updateduser;
    }

    public static boolean containsUrlLocTags(String sitemapUrl) {
        try {
            Document doc = Jsoup.connect(sitemapUrl).get();
            Elements urlLocElements = doc.select("url:has(loc)");
            return !urlLocElements.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getStatusCode(String url) throws IOException {
        URL websiteURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) websiteURL.openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        connection.setRequestMethod("GET");
        return connection.getResponseCode();
    }
}
