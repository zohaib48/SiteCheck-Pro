package com.example.demo.Controllers;



// Import statements...
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.example.demo.Services.UrlCheckService;
import com.example.demo.Services.UrlCheckService2;
import com.example.demo.models.StatusCodeResult;
import com.example.demo.models.User;

import org.springframework.http.MediaType;

@Controller
public class SitemapCheckerController {

  
    @Autowired
    private UrlCheckService urlCheckService;

    @Autowired
    private TaskExecute taskExecutor;



    
    @PostMapping("/check-status")
    public String checkStatus(@RequestParam("sitemapUrl") String sitemapUrl,
            @RequestParam(value = "numThreads", defaultValue = "5") String numThreads,
            Model model, HttpSession session) {
        // Reset session attributes before starting a new task

        stopProcess(session);

        ClearSession(session);

        // Get user limit information and initialize user record if not present
        User userInfo = (User) session.getAttribute("userInfo");
        model.addAttribute("user", userInfo);
        System.out.println(userInfo.getEmail());
        int remainingLimit = userInfo.getRemainingLimit();
        System.out.println(remainingLimit);

        session.setAttribute("urlToCheck", remainingLimit);

          if(remainingLimit<=0){
        
                session.setAttribute("limitReached", true);
                     return "SitemapChecker";
             }



        taskExecutor.submitTask(() -> {
            try {
                // Parse numThreads to int before passing it to checkUrls
                int threads = Integer.parseInt(numThreads);
                urlCheckService.checkUrls(sitemapUrl, threads, session, model);
               
            } catch (Exception e) {
                // Handle the error as needed
                session.setAttribute("error", "An error occurred: " + e.getMessage());
            }
        }, session);

        return "SitemapChecker";
    }

 
  
    @Autowired
    private UrlCheckService2 urlCheckService2;

    @PostMapping("/fetch-html-and-check-status")
    public String fetchHtmlAndCheckStatus(@RequestParam("websiteUrl") String websiteUrl,
            @RequestParam(value = "numThreads", defaultValue = "5") int numThreads,
            @RequestParam("depthLevel") int depthLevel,
            Model model, HttpSession session) {
    
            stopProcess(session);

            ClearSession(session);

        User userInfo = (User) session.getAttribute("userInfo");
        model.addAttribute("user", userInfo);
        System.out.println(userInfo.getEmail());
        int remainingLimit = userInfo.getWebsiteRemainingLimit();
        System.out.println(remainingLimit);

        session.setAttribute("urlToCheck", remainingLimit);

          if(remainingLimit<=0){
        
                session.setAttribute("limitReached", true);
                     return "WebSiteChecker";
             }



          taskExecutor.submitTask(() -> {
            try {
                
              urlCheckService2.checkUrls(websiteUrl, numThreads, session, depthLevel);
                
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                // Handle the error as needed
                session.setAttribute("error", "An error occurred: " + e.getMessage());
            }
        }, session);

        return "WebSiteChecker";
    }

    public void ClearSession( HttpSession session){

        session.setAttribute("executorService", null);
        session.setAttribute("results", null);
        session.setAttribute("statusCounts", null);

        // Reset other session attributes as needed
        session.setAttribute("InProcess", false);
        session.setAttribute("urlToCheck", 0);
        session.setAttribute("limitReached", false);
        session.setAttribute("error", null);
        session.setAttribute("Idle", false);


    }

  

    @PostMapping("/stop-process")
    @ResponseBody
    public Map<String, Object> stopProcess(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        ExecutorService executorService = (ExecutorService) session.getAttribute("executorService");
        if (executorService != null) {
            executorService.shutdownNow();
        }
        session.setAttribute("InProcess", false);
        session.setAttribute("stopProcess", true);

        response.put("stopProcess", true);
        return response;
    }

    @GetMapping("/score")
    public ResponseEntity<Map<String, Object>> getscore(HttpSession session) {
        Map<Integer, Integer> statusCounts = (Map<Integer, Integer>) session.getAttribute("statusCounts");

        Boolean limit = (Boolean) session.getAttribute("limitReached");

         Boolean Idle = (Boolean) session.getAttribute("Idle");

        boolean inProcess = session.getAttribute("InProcess") != null && (boolean) session.getAttribute("InProcess");
        String error = (String) session.getAttribute("error");

        Map<String, Object> result = new HashMap<>();
        result.put("statusCounts", statusCounts);
        System.out.println("limit value is " + limit);
        result.put("limitReached", limit);
        result.put("InProcess", inProcess);
        result.put("error", error);
        result.put("Idle", Idle);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/urls/{SitemapChecker}")
    public ResponseEntity<List<String>> getUrlsByStatusCode(@PathVariable int SitemapChecker, HttpSession session) {
        List<String> urls = Collections.synchronizedList(new ArrayList<>());
        List<StatusCodeResult> results = (List<StatusCodeResult>) session.getAttribute("results");

        // Synchronize access to shared resource
        synchronized (results) {
            for (StatusCodeResult result : results) {
                if (result.getStatusCode() == SitemapChecker) {
                    urls.add(result.getUrl());
                }
            }
        }

        return new ResponseEntity<>(urls, HttpStatus.OK);
    }

   @GetMapping("/downloadCsv")
public ResponseEntity<byte[]> downloadCsv(HttpSession session, Model model) {
    List<StatusCodeResult> results = (List<StatusCodeResult>) session.getAttribute("results");

    if (results != null) {
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("URL,Status Code\n");
        for (StatusCodeResult result : results) {
            csvContent.append(result.getUrl()).append(",").append(result.getStatusCode()).append("\n");
        }

        // Set CSV file headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "urls.csv");

        // Convert CSV content to byte array
        byte[] csvBytes = csvContent.toString().getBytes();

        // Return ResponseEntity with CSV content and headers
        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    } else {
        model.addAttribute("errorInExcel", true);
        // Assuming "SitemapChecker" is the name of your error view
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // You can change the status code as needed
    }
}

}
