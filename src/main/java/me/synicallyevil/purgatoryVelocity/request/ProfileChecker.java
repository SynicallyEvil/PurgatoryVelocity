package me.synicallyevil.purgatoryVelocity.request;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

public class ProfileChecker {

    private UUID uuid;
    private String currentName, newName;
    private boolean isRealAccount;

    public ProfileChecker(String username){
        this.currentName = username;

        try{
            runCheck(username);
        }catch (Exception ex){
            //System.out.println("Error: " + ex.getMessage());
        }
    }

    private void runCheck(String username) throws Exception {
        String link = "https://api.mojang.com/users/profiles/minecraft/" + username;
        URL url = new URL(link);

        Scanner scan = new Scanner(url.openStream());
        StringBuilder str = new StringBuilder();
        while (scan.hasNext())
            str.append(scan.nextLine());
        scan.close();

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject)parser.parse(str.toString());

        if(obj.get("id") == null){
            isRealAccount = false;
            return;
        }

        this.uuid = UUID.fromString(getFormattedUUID((String)obj.get("id")));
        this.newName = (String)obj.get("username");

        isRealAccount = true;

        //System.out.println("uuid: " + uuid);
        //System.out.println("name: " + newName);
        //System.out.println("isreal: " + isRealAccount);
    }

    public String getCurrentName() {
        return currentName;
    }

    public String getNewName() {
        return newName;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isRealAccount() {
        return isRealAccount;
    }

    private String getFormattedUUID(String uuid){
        // Ensure the input length is exactly 32 characters
        if (uuid == null || uuid.length() != 32) {
            throw new IllegalArgumentException("Input must be a 32-character string.");
        }

        // Use a StringBuilder for efficient string manipulation
        StringBuilder formatted = new StringBuilder();

        // Add the hyphens at the specified positions
        formatted.append(uuid, 0, 8).append("-")
                .append(uuid, 8, 12).append("-")
                .append(uuid, 12, 16).append("-")
                .append(uuid, 16, 20).append("-")
                .append(uuid.substring(20));

        return formatted.toString();
    }
}