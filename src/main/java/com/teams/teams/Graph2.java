package com.teams.teams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import okhttp3.Request;

import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.Attendee;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.OnlineMeetingProviderType;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.AttendeeType;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.OnlineMeetingCollectionPage;
import com.microsoft.graph.requests.EventCollectionPage;
import com.microsoft.graph.requests.EventCollectionRequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
public class Graph2 {

    private static GraphServiceClient<Request> graphClient = null;
//    private static TokenCredentialAuthProvider authProvider = null;
    private static TokenCredentialAuthProvider authProvider = null;
    
    public static void initializeGraphAuth(String applicationId, List<String> scopes) {
        // Create the auth provider
        final DeviceCodeCredential credential = new DeviceCodeCredentialBuilder()
            .clientId(applicationId)
            .challengeConsumer(challenge -> System.out.println(challenge.getMessage()))
            .build();

        authProvider = new TokenCredentialAuthProvider(scopes, credential);

        // Create default logger to only log errors
        DefaultLogger logger = new DefaultLogger();
        logger.setLoggingLevel(LoggerLevel.ERROR);

        // Build a Graph client
        graphClient = GraphServiceClient.builder()
            .authenticationProvider(authProvider)
            .logger(logger)
            .buildClient();
    }
    public static void initializeGraphAuth2(String applicationId, List<String> scopes) {
        // Create the auth provider
    	final UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
    	        .clientId(applicationId)
    	        .username("juan.castillorincon@endava.com")
    	        .password("Pipe961204!")
    	        .build();

    	authProvider = new TokenCredentialAuthProvider(scopes, usernamePasswordCredential);
    	 try {
             URL meUrl = new URL("https://graph.microsoft.com/v1.0/me");
             System.out.println("token "+authProvider.getAuthorizationTokenAsync(meUrl).get());
              
         } catch(Exception ex) {
        	 System.out.println("error getting token "+ex);
         }
    	 
    	 
    	 
//    	graphClient =
//    	  GraphServiceClient
//    	    .builder()
//    	    .authenticationProvider(authProvider)
//    	    .buildClient();

//    	final User me = graphClient.me().buildRequest().get();
    }

    public static String getUserAccessToken()
    {
        try {
            URL meUrl = new URL("https://graph.microsoft.com/v1.0/me");
            return authProvider.getAuthorizationTokenAsync(meUrl).get();
        } catch(Exception ex) {
            return null;
        }
    }
    
    public static void createEvent(
    	    String timeZone,
    	    String subject,
    	    LocalDateTime start,
    	    LocalDateTime end,
    	    Set<String> attendees,
    	    String body)
    	{
    	    if (graphClient == null) throw new NullPointerException(
    	        "Graph client has not been initialized. Call initializeGraphAuth before calling this method");

    	    Event newEvent = new Event();

    	    newEvent.subject = subject;

    	    newEvent.start = new DateTimeTimeZone();
    	    newEvent.start.dateTime = start.toString();
    	    newEvent.start.timeZone = timeZone;

    	    newEvent.end = new DateTimeTimeZone();
    	    newEvent.end.dateTime = end.toString();
    	    newEvent.end.timeZone = timeZone;
    	    newEvent.isOnlineMeeting = true;
    	    newEvent.onlineMeetingProvider = OnlineMeetingProviderType.TEAMS_FOR_BUSINESS;


    	    if (attendees != null && !attendees.isEmpty()) {
    	        newEvent.attendees = new LinkedList<Attendee>();

    	        attendees.forEach((email) -> {
    	            Attendee attendee = new Attendee();
    	            // Set each attendee as required
    	            attendee.type = AttendeeType.REQUIRED;
    	            attendee.emailAddress = new EmailAddress();
    	            attendee.emailAddress.address = email;
    	            newEvent.attendees.add(attendee);
    	        });
    	    }

    	    if (body != null) {
    	        newEvent.body = new ItemBody();
    	        newEvent.body.content = body;
    	        // Treat body as plain text
    	        newEvent.body.contentType = BodyType.TEXT;
    	    }

    	    // POST /me/events
    	    graphClient
    	        .me()
    	        .events()
    	        .buildRequest()
    	        .post(newEvent);
    	}
    
    
    public static User getUser() {
        if (graphClient == null) throw new NullPointerException(
            "Graph client has not been initialized. Call initializeGraphAuth before calling this method");

        // GET /me to get authenticated user
        User me = graphClient
            .me()
            .buildRequest()
            .select("displayName,mailboxSettings")
            .get();
//        User user = graphClient.users()
        return me;
    }
    
    public static List<Event> getCalendarView(
    	    ZonedDateTime viewStart, ZonedDateTime viewEnd, String timeZone) {
    	    if (graphClient == null) throw new NullPointerException(
    	        "Graph client has not been initialized. Call initializeGraphAuth before calling this method");

    	    List<Option> options = new LinkedList<Option>();
    	    options.add(new QueryOption("startDateTime", viewStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
    	    options.add(new QueryOption("endDateTime", viewEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
    	    // Sort results by start time
    	    options.add(new QueryOption("$orderby", "start/dateTime"));

    	    // Start and end times adjusted to user's time zone
    	    options.add(new HeaderOption("Prefer", "outlook.timezone=\"" + timeZone + "\""));

    	    // GET /me/events
    	    EventCollectionPage eventPage = graphClient
    	        .me()
    	        .calendarView()
    	        .buildRequest(options)
    	        .select("subject,organizer,start,end")
    	        .top(25)
    	        .get();

    	    List<Event> allEvents = new LinkedList<Event>();

    	    // Create a separate list of options for the paging requests
    	    // paging request should not include the query parameters from the initial
    	    // request, but should include the headers.
    	    List<Option> pagingOptions = new LinkedList<Option>();
    	    pagingOptions.add(new HeaderOption("Prefer", "outlook.timezone=\"" + timeZone + "\""));

    	    while (eventPage != null) {
    	        allEvents.addAll(eventPage.getCurrentPage());

    	        EventCollectionRequestBuilder nextPage =
    	            eventPage.getNextPage();

    	        if (nextPage == null) {
    	            break;
    	        } else {
    	            eventPage = nextPage
    	                .buildRequest(pagingOptions)
    	                .get();
    	        }
    	    }

    	    return allEvents;
    	}
    
    public static void printAtendance() {

    	Event event = graphClient.me().events("744612265&conf=1270366874")
    		.buildRequest()
    		.select("isOnlineMeeting,onlineMeetingProvider,onlineMeeting")
    		.get();
    	System.out.println(event);
    }
    
    public static void testVideoConference() {
    	OnlineMeetingCollectionPage onlineMeetings = graphClient.communications().onlineMeetings()
    			.buildRequest()
    			.filter("VideoTeleconferenceId eq ' 744612265'")
    			.get();
    	System.out.println(onlineMeetings);
    	
    	}
    
//    private RestTemplate getRestTemplate()
//            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
//        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
//        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
//                .loadTrustMaterial(null, acceptingTrustStrategy)
//                .build();
//        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setSSLSocketFactory(csf)
//                .build();
//        HttpComponentsClientHttpRequestFactory requestFactory =
//                new HttpComponentsClientHttpRequestFactory();
//        requestFactory.setHttpClient(httpClient);
//        RestTemplate restTemplate = new RestTemplate(requestFactory);
//        return restTemplate;
//    }
    public static void getAtendanceReport() {
    	try {
    		HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String token = "eyJ0eXAiOiJKV1QiLCJub25jZSI6Il9lZ052LXhNQ3FpQ2gwSlk0d29hdEJjTnZTQktwaGVJNl92allQeHRLWnMiLCJhbGciOiJSUzI1NiIsIng1dCI6Imwzc1EtNTBjQ0g0eEJWWkxIVEd3blNSNzY4MCIsImtpZCI6Imwzc1EtNTBjQ0g0eEJWWkxIVEd3blNSNzY4MCJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8wYjNmYzE3OC1iNzMwLTRlOGItOTg0My1lODEyNTkyMzdiNzcvIiwiaWF0IjoxNjM1OTc3MjM2LCJuYmYiOjE2MzU5NzcyMzYsImV4cCI6MTYzNTk4MTEzNiwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFYUUFpLzhUQUFBQWF4QkRVQzhmeklDdHJBanQyUHFFcm14emk4WVZDTTVHUkQ1OTJySkxSLzNxbFJsTm9NSVh0d1Q5UnF3ZGpyOCt6Z3hrb3Y0TlovSnphZjh2SFhoem5zL0I3QmIvbXRRalVFLzJ4c0NZb0FJZ2JIN0xUS2ZoSGludVNWbFNNelllbjRtK3F5V2REa2Y2Q1hkczdwaEw0Zz09IiwiYW1yIjpbInB3ZCIsInJzYSIsIndpYSIsIm1mYSJdLCJhcHBfZGlzcGxheW5hbWUiOiJyZWNvZ25pdGlvbl9wcm9ncmFtIiwiYXBwaWQiOiI2NmNjM2NjYS0wMDYwLTQ3YWMtYjQ3Ni00Yzg1NTQ0MTZkZjUiLCJhcHBpZGFjciI6IjAiLCJkZXZpY2VpZCI6IjhjOTlhMTE2LWQ2YzUtNGU0Ni1iODZjLWRjNmY5NWE4YWYwMCIsImZhbWlseV9uYW1lIjoiQ2FzdGlsbG8gUmluY29uIiwiZ2l2ZW5fbmFtZSI6Ikp1YW4gRmVsaXBlIiwiaWR0eXAiOiJ1c2VyIiwiaXBhZGRyIjoiMTkwLjE1OS4xMi4yMDMiLCJuYW1lIjoiSnVhbiBGZWxpcGUgQ2FzdGlsbG8gUmluY29uIiwib2lkIjoiNjI4N2RkODgtODc1YS00ZDMzLWFkYjYtOTcxNWIyNjM0MjFiIiwib25wcmVtX3NpZCI6IlMtMS01LTIxLTM5OTg3MTI5Mi0xMDY3NTQxNTk4LTcwMjg0MzgzNC0xNDE4NjUiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzIwMDE1REZCQ0U3OSIsInJoIjoiMC5BVjBBZU1FX0N6QzNpMDZZUS1nU1dTTjdkOG84ekdaZ0FLeEh0SFpNaFZSQmJmVmRBTXcuIiwic2NwIjoiRmlsZXMuUmVhZC5BbGwgb3BlbmlkIHByb2ZpbGUgVXNlci5SZWFkIGVtYWlsIE1haWxib3hTZXR0aW5ncy5SZWFkIENhbGVuZGFycy5SZWFkV3JpdGUgT25saW5lTWVldGluZ3MuUmVhZCIsInNpZ25pbl9zdGF0ZSI6WyJkdmNfbW5nZCIsImR2Y19kbWpkIiwia21zaSJdLCJzdWIiOiI0alVId2ZyR0V4bmVON1dJX3ItaWpfR3piZFpsa25iUVVRX3NjcGRKVkV3IiwidGVuYW50X3JlZ2lvbl9zY29wZSI6IkVVIiwidGlkIjoiMGIzZmMxNzgtYjczMC00ZThiLTk4NDMtZTgxMjU5MjM3Yjc3IiwidW5pcXVlX25hbWUiOiJqdWFuLmNhc3RpbGxvcmluY29uQGVuZGF2YS5jb20iLCJ1cG4iOiJKdWFuLkNhc3RpbGxvUmluY29uQGVuZGF2YS5jb20iLCJ1dGkiOiI0X1Uyd05XbjJrQy1RdUxqeEpxS0FBIiwidmVyIjoiMS4wIiwid2lkcyI6WyJiNzlmYmY0ZC0zZWY5LTQ2ODktODE0My03NmIxOTRlODU1MDkiXSwieG1zX3N0Ijp7InN1YiI6ImFhT1dYcWFYZG95R0tFRUZxMUw1ck1NRmNxR3hsbzN5RHRfUjNZWm04N1EifSwieG1zX3RjZHQiOjEzNzIyMjg5MzV9.FGBt9NbXn5FJOfsPEH9SZs8DL4w2vAE7knLA2Ym7YRYy10ip9wq4W3Y8FpYl5BYbRs06MfTz3PTZ3eyJHrWLIFNks10kFs8vR4dagND2aLWsdQWMt9HGjHF6XeS0wdkmlp_9IgcpaOB3gH4ZFb4xNnXDG9TeztJXwJ1yqdD-cxx9UTsDTDJe1rdE1rTg4e7tbv6_O78RRS4bN1HkImpu8I-C4-LSmM1x_UB9fmzx5kUl0sqyosITNHSY-5GYFi6ilVTpQgkUH5HDa9fRfQnVnLnU6vKvFxzQzqsbn0pcKSjeeckqPFbB96TJ-eO-6ZEs4qMrjrSlUMc4clrMEyeOPg";
            headers.set("Authorization", token);
            HttpEntity<String> requestEntity = new HttpEntity<>("body", headers);
            ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<String>() {};
            String url = "https://graph.microsoft.com/beta/users/6287dd88-875a-4d33-adb6-9715b263421b/onlineMeetings/MSo2Mjg3ZGQ4OC04NzVhLTRkMzMtYWRiNi05NzE1YjI2MzQyMWIqMCoqMTk6bWVldGluZ19ObUV3TURBeE5UZ3ROR05rWVMwMFlqZGhMVGxqTmpndE9HSTJNVE16TjJZNE5UbGpAdGhyZWFkLnYy/meetingAttendanceReport";
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange( url, HttpMethod.GET, requestEntity, responseType);
            System.out.println(response.getBody());
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	
    	
    	
//    	String token = "eyJ0eXAiOiJKV1QiLCJub25jZSI6Il9lZ052LXhNQ3FpQ2gwSlk0d29hdEJjTnZTQktwaGVJNl92allQeHRLWnMiLCJhbGciOiJSUzI1NiIsIng1dCI6Imwzc1EtNTBjQ0g0eEJWWkxIVEd3blNSNzY4MCIsImtpZCI6Imwzc1EtNTBjQ0g0eEJWWkxIVEd3blNSNzY4MCJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8wYjNmYzE3OC1iNzMwLTRlOGItOTg0My1lODEyNTkyMzdiNzcvIiwiaWF0IjoxNjM1OTc3MjM2LCJuYmYiOjE2MzU5NzcyMzYsImV4cCI6MTYzNTk4MTEzNiwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFYUUFpLzhUQUFBQWF4QkRVQzhmeklDdHJBanQyUHFFcm14emk4WVZDTTVHUkQ1OTJySkxSLzNxbFJsTm9NSVh0d1Q5UnF3ZGpyOCt6Z3hrb3Y0TlovSnphZjh2SFhoem5zL0I3QmIvbXRRalVFLzJ4c0NZb0FJZ2JIN0xUS2ZoSGludVNWbFNNelllbjRtK3F5V2REa2Y2Q1hkczdwaEw0Zz09IiwiYW1yIjpbInB3ZCIsInJzYSIsIndpYSIsIm1mYSJdLCJhcHBfZGlzcGxheW5hbWUiOiJyZWNvZ25pdGlvbl9wcm9ncmFtIiwiYXBwaWQiOiI2NmNjM2NjYS0wMDYwLTQ3YWMtYjQ3Ni00Yzg1NTQ0MTZkZjUiLCJhcHBpZGFjciI6IjAiLCJkZXZpY2VpZCI6IjhjOTlhMTE2LWQ2YzUtNGU0Ni1iODZjLWRjNmY5NWE4YWYwMCIsImZhbWlseV9uYW1lIjoiQ2FzdGlsbG8gUmluY29uIiwiZ2l2ZW5fbmFtZSI6Ikp1YW4gRmVsaXBlIiwiaWR0eXAiOiJ1c2VyIiwiaXBhZGRyIjoiMTkwLjE1OS4xMi4yMDMiLCJuYW1lIjoiSnVhbiBGZWxpcGUgQ2FzdGlsbG8gUmluY29uIiwib2lkIjoiNjI4N2RkODgtODc1YS00ZDMzLWFkYjYtOTcxNWIyNjM0MjFiIiwib25wcmVtX3NpZCI6IlMtMS01LTIxLTM5OTg3MTI5Mi0xMDY3NTQxNTk4LTcwMjg0MzgzNC0xNDE4NjUiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzIwMDE1REZCQ0U3OSIsInJoIjoiMC5BVjBBZU1FX0N6QzNpMDZZUS1nU1dTTjdkOG84ekdaZ0FLeEh0SFpNaFZSQmJmVmRBTXcuIiwic2NwIjoiRmlsZXMuUmVhZC5BbGwgb3BlbmlkIHByb2ZpbGUgVXNlci5SZWFkIGVtYWlsIE1haWxib3hTZXR0aW5ncy5SZWFkIENhbGVuZGFycy5SZWFkV3JpdGUgT25saW5lTWVldGluZ3MuUmVhZCIsInNpZ25pbl9zdGF0ZSI6WyJkdmNfbW5nZCIsImR2Y19kbWpkIiwia21zaSJdLCJzdWIiOiI0alVId2ZyR0V4bmVON1dJX3ItaWpfR3piZFpsa25iUVVRX3NjcGRKVkV3IiwidGVuYW50X3JlZ2lvbl9zY29wZSI6IkVVIiwidGlkIjoiMGIzZmMxNzgtYjczMC00ZThiLTk4NDMtZTgxMjU5MjM3Yjc3IiwidW5pcXVlX25hbWUiOiJqdWFuLmNhc3RpbGxvcmluY29uQGVuZGF2YS5jb20iLCJ1cG4iOiJKdWFuLkNhc3RpbGxvUmluY29uQGVuZGF2YS5jb20iLCJ1dGkiOiI0X1Uyd05XbjJrQy1RdUxqeEpxS0FBIiwidmVyIjoiMS4wIiwid2lkcyI6WyJiNzlmYmY0ZC0zZWY5LTQ2ODktODE0My03NmIxOTRlODU1MDkiXSwieG1zX3N0Ijp7InN1YiI6ImFhT1dYcWFYZG95R0tFRUZxMUw1ck1NRmNxR3hsbzN5RHRfUjNZWm04N1EifSwieG1zX3RjZHQiOjEzNzIyMjg5MzV9.FGBt9NbXn5FJOfsPEH9SZs8DL4w2vAE7knLA2Ym7YRYy10ip9wq4W3Y8FpYl5BYbRs06MfTz3PTZ3eyJHrWLIFNks10kFs8vR4dagND2aLWsdQWMt9HGjHF6XeS0wdkmlp_9IgcpaOB3gH4ZFb4xNnXDG9TeztJXwJ1yqdD-cxx9UTsDTDJe1rdE1rTg4e7tbv6_O78RRS4bN1HkImpu8I-C4-LSmM1x_UB9fmzx5kUl0sqyosITNHSY-5GYFi6ilVTpQgkUH5HDa9fRfQnVnLnU6vKvFxzQzqsbn0pcKSjeeckqPFbB96TJ-eO-6ZEs4qMrjrSlUMc4clrMEyeOPg";
//    	RestTemplate restTemplate = new RestTemplate();
//    	String fooResourceUrl
//    	  = "https://graph.microsoft.com/beta/users/6287dd88-875a-4d33-adb6-9715b263421b/onlineMeetings/MSo2Mjg3ZGQ4OC04NzVhLTRkMzMtYWRiNi05NzE1YjI2MzQyMWIqMCoqMTk6bWVldGluZ19ObUV3TURBeE5UZ3ROR05rWVMwMFlqZGhMVGxqTmpndE9HSTJNVE16TjJZNE5UbGpAdGhyZWFkLnYy/meetingAttendanceReport";
//    	HttpHeaders headers = new HttpHeaders();
//    	headers.setBearerAuth(token);
//    	HttpEntity<String> entity = new HttpEntity<>("body", headers);
//    	ResponseEntity<String> response
//    	  = restTemplate.getForEntity(fooResourceUrl , String.class);
//    	InputStream stream = graphClient.customRequest("/users/6287dd88-875a-4d33-adb6-9715b263421b/onlineMeetings/MSo2Mjg3ZGQ4OC04NzVhLTRkMzMtYWRiNi05NzE1YjI2MzQyMWIqMCoqMTk6bWVldGluZ19ObUV3TURBeE5UZ3ROR05rWVMwMFlqZGhMVGxqTmpndE9HSTJNVE16TjJZNE5UbGpAdGhyZWFkLnYy/meetingAttendanceReport", InputStream.class)
//    			
//    			.buildRequest()
//    			.get();
//    	graphClient.users(getUserAccessToken())
//    	String result = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
//    	
//		JSONObject json = new JSONObject(result);
//		System.out.println(json);
//		System.out.println(json.get("attendanceRecords"));
    }
}