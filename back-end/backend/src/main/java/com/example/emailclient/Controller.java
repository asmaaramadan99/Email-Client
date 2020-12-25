package com.example.emailclient;

import Misc.FileSaver;
import Models.*;
import Models.Immutables.ContactImmutable;
import Models.Immutables.EmailHeaderImmutable;
import Models.Immutables.EmailImmutable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class Controller {


    @RequestMapping(value = "/login", method = RequestMethod.POST,produces = "application/json")
    public Map<String, Object> authenticate(@RequestHeader(value="Authorization") String userInfo){
        String authenticatedUserID=SecurityFilter.getInstance().authenticateLogin(userInfo);
        if(authenticatedUserID!="null")
            return SecurityFilter.getInstance().generateBasicInfo(authenticatedUserID);
        Map <String,Object> errorMap=new HashMap<>();
        errorMap.put("authenticated","false");
        return errorMap;
    }
    @RequestMapping(value = "/signUp", method = RequestMethod.POST)
    public String authenticateNewUser(@RequestBody Map<String ,Object> userSignUpInfo) throws ParseException {
        SecurityFilter.getInstance().createNewUser(userSignUpInfo);

        return "success";
    }

    @RequestMapping(value = "/getMail", method = RequestMethod.GET, produces = "application/json")
    public EmailImmutable getMail(int id,String key) {
        UserSession userSession = SecurityFilter.getInstance().getUserSession(key);
        if(userSession!=null) {
            System.err.println(id);
            EmailImmutable emI = new EmailImmutable(userSession.getMail(id));
            return emI;
        }
        return null;
    }

    @RequestMapping(value = "/sendMail", method = RequestMethod.POST)
    public String sendMail( @RequestPart(name ="attachments",required = false) MultipartFile[] attachments,
                            @RequestParam(name ="email") String emailJson,
                                @RequestParam(name="key" )String key,
                            @RequestPart(name ="receivers") String receiversStr) {
        Map<String, Object> emailMap = null;
        try {
            emailMap = new ObjectMapper().readValue(emailJson, Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }

        String[] receivers = receiversStr.split(" ");
        String[] paths = null;
        if (attachments != null) {
            paths = new String[attachments.length];
            try {
                int i = 0;
                for (MultipartFile at : attachments) {
                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
                    String fileName = sdf.format(new Date()) + at.getOriginalFilename();
                    String path = FileSaver.saveFile(at, fileName);
                    if (path == null) {
                        return null;
                    }
                    paths[i] = path;
                    i++;
                }
            } catch (Exception e) {
                return null;
            }
        }
        UserSession userSession = SecurityFilter.getInstance().getUserSession(key);
        if(userSession!=null) {
            userSession.sendEmail(emailMap, receivers, paths);
            return "Succeeded";
        }
        return "Failed";
    }

    @RequestMapping(value = "/drafts",method = RequestMethod.POST)
    public String drafts(@RequestParam(name ="email") String emailJson, @RequestParam(name="key" )String key){
        Map<String, Object> emailMap = null;
        try {
            emailMap = new ObjectMapper().readValue(emailJson, Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }
        UserSession userSession = SecurityFilter.getInstance().getUserSession(key);
        if(userSession!=null) {
            userSession.draft(emailMap);
            return "Succeeded";
        }
        return "Succeeded";
    }


    @RequestMapping(value = "/moveMail",method = RequestMethod.PUT)
    public void moveMail(@RequestParam String headersId, @RequestParam int currentFolder, @RequestParam int destinationFolder,@RequestParam String key){
        UserSession userSession = SecurityFilter.getInstance().getUserSession(key);
        if(userSession!=null) {
            List<Integer> headersIdList = new ArrayList<Integer>();
            for (String numStr : headersId.split(",")) {
                headersIdList.add(Integer.parseInt(numStr));
            }
            userSession.moveEmail(headersIdList, currentFolder, destinationFolder);
        }

    }
    @RequestMapping(value = "/copyMail",method = RequestMethod.PUT)
    public void copyMail(@RequestParam String headersId, @RequestParam int currentFolder, @RequestParam int destinationFolder,@RequestParam String key){
        UserSession userSession = SecurityFilter.getInstance().getUserSession(key);
        if(userSession!=null) {
            List<Integer> headersIdList = new ArrayList<Integer>();
            userSession.copyEmail(headersIdList, currentFolder, destinationFolder);
        }
    }
    @RequestMapping(value = "/deleteMail",method = RequestMethod.DELETE)
    public void deleteMail(@RequestParam String headersId, @RequestParam int currentFolder,@RequestParam String key){
        UserSession userSession = SecurityFilter.getInstance().getUserSession(key);
        if(userSession!=null) {
            List<Integer> headersIdList = new ArrayList<Integer>();
            for (String numStr : headersId.split(",")) {
                headersIdList.add(Integer.parseInt(numStr));
            }
            userSession.deleteEmail(headersIdList, currentFolder);
        }
    }
    @RequestMapping(value = "/addFolder",method = RequestMethod.POST)
    public void addFolder(@RequestBody Map<String,Object> folderMap){
        UserSession userSession = new UserSession(30);
        userSession.addFolder(folderMap);
    }

    @RequestMapping(value = "/deleteFolder",method = RequestMethod.DELETE)
    public void deleteFolder(int folderId){
        UserSession userSession = new UserSession(1);
        userSession.removeFolder(folderId);
    }
    @RequestMapping(value = "/editFolder",method = RequestMethod.PUT)
    public void editFolder(@RequestBody Map<String,Object> folderMap){
        UserSession userSession = new UserSession(1);
        userSession.editFolder(folderMap);
    }
    @RequestMapping(value = "/addContact",method = RequestMethod.POST)
    public void addContact(@RequestBody Map<String,Object> contactMap){
        UserSession userSession = new UserSession(7);
        userSession.addContact(contactMap);
    }

    @RequestMapping(value = "/deleteContact",method = RequestMethod.DELETE)
    public void deleteContact(@RequestParam Integer contactId){
        UserSession userSession = new UserSession(7);
        userSession.removeContact(contactId);
    }
    @RequestMapping(value = "/editContact",method = RequestMethod.PUT)
    public void editContact(@RequestBody Map<String,Object> contactMap){
        UserSession userSession = new UserSession(7);
        userSession.editFolder(contactMap);
    }



    @RequestMapping(value = "/loadMailHeaders",method = RequestMethod.GET)
    public List<EmailHeaderImmutable> loadMailHeaders(int folderIndex,int page,String criteria,Boolean order,String key){
        UserSession userSession = SecurityFilter.getInstance().getUserSession(key);
        if(userSession!=null) {
            return userSession.loadEmailHeaders(folderIndex, page, criteria, order);
        }
        return null;
    }

    @RequestMapping(value = "/filterMailHeaders",method = RequestMethod.GET)
    public List<EmailHeaderImmutable> filterMailHeaders(int folderIndex,int page,String criteria,String filterKey,String key){
        UserSession userSession = SecurityFilter.getInstance().getUserSession(key);
        if(userSession!=null) {
            return userSession.filterEmailHeaders(folderIndex, page, criteria, filterKey);
        }
        return null;
    }

    @RequestMapping(value = "/loadContacts",method = RequestMethod.GET)
    public List<ContactImmutable> loadContacts(){
        UserSession userSession = new UserSession(7);
        return userSession.loadContacts();
    }
}
