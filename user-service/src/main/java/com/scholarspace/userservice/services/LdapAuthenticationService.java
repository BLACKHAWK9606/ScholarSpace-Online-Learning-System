package com.scholarspace.userservice.services;

import com.scholarspace.userservice.config.LdapConfig;
import com.scholarspace.userservice.models.Role;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.*;

@Service
public class LdapAuthenticationService {

    private final LdapTemplate ldapTemplate;
    private final LdapConfig ldapConfig;

    public LdapAuthenticationService(LdapTemplate ldapTemplate, LdapConfig ldapConfig) {
        this.ldapTemplate = ldapTemplate;
        this.ldapConfig = ldapConfig;
    }

    public boolean authenticateUser(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            System.out.println("LDAP Auth: Empty credentials provided");
            return false;
        }
        
        try {
            System.out.println("LDAP Auth: Attempting authentication for: " + email);
            
            // Step 1: Find user DN using service account
            String userDn = findUserDn(email);
            if (userDn == null) {
                System.out.println("LDAP Auth: User DN not found for: " + email);
                return false;
            }
            
            System.out.println("LDAP Auth: Found user DN: " + userDn);
            
            // Step 2: Authenticate user with their DN and password
            boolean result = bindWithUserCredentials(userDn, password);
            System.out.println("LDAP Auth: Authentication result: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("LDAP Auth: Exception during authentication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String findUserDn(String identifier) {
        try {
            String filter = String.format("(|(userPrincipalName=%s)(sAMAccountName=%s))", identifier, identifier);
            System.out.println("LDAP Search: Using filter: " + filter);
            
            String searchBase = ldapConfig.getUserSearchBase();
            if (searchBase == null || searchBase.trim().isEmpty()) {
                searchBase = "";  // Search from root
            }
            
            List<String> dns = ldapTemplate.search(
                searchBase,
                filter,
                (Attributes attrs) -> getAttributeValue(attrs, "distinguishedName")
            );
            
            System.out.println("LDAP Search: Found " + dns.size() + " results");
            return dns.isEmpty() ? null : dns.get(0);
        } catch (Exception e) {
            System.out.println("LDAP Search: Exception during user search: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean bindWithUserCredentials(String userDn, String password) {
        LdapContext ctx = null;
        try {
            System.out.println("LDAP Bind: Attempting bind with DN: " + userDn);
            
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapConfig.getUrl());
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, userDn);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put("com.sun.jndi.ldap.connect.timeout", "5000");
            env.put("com.sun.jndi.ldap.read.timeout", "5000");

            ctx = new InitialLdapContext(env, null);
            System.out.println("LDAP Bind: Successful bind for: " + userDn);
            return true;
        } catch (Exception e) {
            System.out.println("LDAP Bind: Failed to bind with DN: " + userDn + ", Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (ctx != null) {
                try { ctx.close(); } catch (Exception e) {}
            }
        }
    }

    public Map<String, Object> getUserDetails(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        try {
            String filter = String.format("(|(userPrincipalName=%s)(sAMAccountName=%s))", email, email);

            String searchBase = ldapConfig.getUserSearchBase();
            if (searchBase == null || searchBase.trim().isEmpty()) {
                searchBase = "";  // Search from root
            }
            
            List<Map<String, Object>> users = ldapTemplate.search(
                searchBase,
                filter,
                (Attributes attrs) -> {
                    Map<String, Object> userDetails = new HashMap<>();
                    
                    // Extract user attributes with fallbacks
                    String displayName = getAttributeValue(attrs, "displayName");
                    String givenName = getAttributeValue(attrs, "givenName");
                    String surname = getAttributeValue(attrs, "sn");
                    String samAccountName = getAttributeValue(attrs, "sAMAccountName");
                    String distinguishedName = getAttributeValue(attrs, "distinguishedName");
                    
                    userDetails.put("email", email);
                    userDetails.put("name", displayName != null ? displayName : (givenName + " " + surname).trim());
                    userDetails.put("firstName", givenName);
                    userDetails.put("lastName", surname);
                    userDetails.put("samAccountName", samAccountName);
                    userDetails.put("distinguishedName", distinguishedName);
                    
                    return userDetails;
                }
            );

            if (!users.isEmpty()) {
                Map<String, Object> userDetails = users.get(0);
                String distinguishedName = (String) userDetails.get("distinguishedName");
                if (distinguishedName != null) {
                    Role role = getUserRole(distinguishedName);
                    userDetails.put("role", role);
                    return userDetails;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user details from LDAP: " + e.getMessage());
        }
        
        return null;
    }

    private Role getUserRole(String userDn) {
        if (userDn == null || userDn.trim().isEmpty()) {
            return Role.STUDENT;
        }
        
        try {
            // Search for groups where this user is a member
            String filter = String.format("(member=%s)", userDn);
            
            String groupSearchBase = ldapConfig.getGroupSearchBase();
            if (groupSearchBase == null || groupSearchBase.trim().isEmpty()) {
                groupSearchBase = "";  // Search from root
            }
            
            List<String> groups = ldapTemplate.search(
                groupSearchBase,
                filter,
                (Attributes attrs) -> getAttributeValue(attrs, "cn")
            );

            // Check groups in priority order (Admin > Instructor > Student)
            for (String group : groups) {
                if ("ScholarSpace-Admins".equalsIgnoreCase(group)) {
                    return Role.ADMIN;
                }
            }
            for (String group : groups) {
                if ("ScholarSpace-Instructors".equalsIgnoreCase(group)) {
                    return Role.INSTRUCTOR;
                }
            }
            for (String group : groups) {
                if ("ScholarSpace-Students".equalsIgnoreCase(group)) {
                    return Role.STUDENT;
                }
            }
            
            return Role.STUDENT;
        } catch (Exception e) {
            return Role.STUDENT;
        }
    }

    private String getAttributeValue(Attributes attrs, String attributeName) {
        try {
            Attribute attr = attrs.get(attributeName);
            return attr != null ? (String) attr.get() : null;
        } catch (Exception e) {
            return null;
        }
    }
}