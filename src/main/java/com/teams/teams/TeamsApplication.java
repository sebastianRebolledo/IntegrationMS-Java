package com.teams.teams;

//import org.springframework.boot.SpringApplication;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.User;
//import org.springframework.boot.autoconfigure.SpringBootApplication;





//@SpringBootApplication
public class TeamsApplication {

	public static void main(String[] args) {
		//SpringApplication.run(TeamsApplication.class, args);

		System.out.println("Java Graph Tutorial");
		System.out.println();

		// Load OAuth settings
		final Properties oAuthProperties = new Properties();
		try {
			oAuthProperties.load(TeamsApplication.class.getResourceAsStream("oAuth.properties"));
		} catch (IOException e) {
			System.out.println(
					"Unable to read OAuth configuration. Make sure you have a properly formatted oAuth.properties file. See README for details.");
			return;
		}

		final String appId = oAuthProperties.getProperty("app.id");
		System.out.println(appId);
		final List<String> appScopes = Arrays.asList(oAuthProperties.getProperty("app.scopes").split(","));
		System.out.println(appScopes);

		// Initialize Graph with auth settings
		
//		Graph2.initializeGraphAuth(appId, appScopes);
//		Graph2.initializeGraphAuth2(appId, appScopes);
	    final String accessToken = Graph2.getUserAccessToken();

		// Display access token

		// Greet the user
//		User user = Graph2.getUser();
//		System.out.println("Welcome " + user.displayName);
//		System.out.println("Time zone: " + user.mailboxSettings.timeZone);
//		System.out.println();
		
		Scanner input = new Scanner(System.in);

		int choice = -1;

		while (choice != 0) {
			System.out.println("Please choose one of the following options:");
			System.out.println("0. Exit");
			System.out.println("1. Display access token");
			System.out.println("2. View this week's calendar");
			System.out.println("3. Add an event");

			try {
				choice = input.nextInt();
			} catch (InputMismatchException ex) {
				// Skip over non-integer input
			}

			input.nextLine();

			// Process user choice
			switch (choice) {
			case 0:
				// Exit the program
				System.out.println("Goodbye...");
				break;
			case 1:
				// Display access token
				System.out.println("Access token: " + accessToken);

				break;
			case 2:
				// List the calendar
//				listCalendarEvents(user.mailboxSettings.timeZone);
				break;
			case 3:
				// Create a new event
//				createEvent(user.mailboxSettings.timeZone, input);
				break;
			case 4:
				// Get Attendance
				Graph2.getAtendanceReport();
				break;
			default:
				System.out.println("Invalid choice");
			}
		}

		input.close();

	}
	
	private static String formatDateTimeTimeZone(DateTimeTimeZone date) {
	    LocalDateTime dateTime = LocalDateTime.parse(date.dateTime);

	    return dateTime.format(
	        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)) +
	        " (" + date.timeZone + ")";
	}
	
	private static void listCalendarEvents(String timeZone) {
	    ZoneId tzId = GraphToIana.getZoneIdFromWindows("Pacific Standard Time");

	    // Get midnight of the first day of the week (assumed Sunday)
	    // in the user's timezone, then convert to UTC
	    ZonedDateTime startOfWeek = ZonedDateTime.now(tzId)
	        .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
	        .truncatedTo(ChronoUnit.DAYS)
	        .withZoneSameInstant(ZoneId.of("UTC"));

	    // Add 7 days to get the end of the week
	    ZonedDateTime endOfWeek = startOfWeek.plusDays(7);

	    // Get the user's events
	    List<Event> events = Graph2.getCalendarView(
	        startOfWeek, endOfWeek, timeZone);

	    System.out.println("Events:");

	    for (Event event : events) {
	        System.out.println("Subject: " + event.subject);
	        System.out.println("  Organizer: " + event.organizer.emailAddress.name);
	        System.out.println("  Start: " + formatDateTimeTimeZone(event.start));
	        System.out.println("  End: " + formatDateTimeTimeZone(event.end));
	    }

	    System.out.println();
	}
	
	
	private static void createEvent(String timeZone, Scanner input) {
	    DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");

	    // Prompt for subject
	    String subject = "";
	    while (subject.isBlank()) {
	        System.out.print("Subject (required): ");
	        subject = input.nextLine();
	    }

	    // Prompt for start date/time
	    LocalDateTime start = null;
	    while (start == null) {
	        System.out.print("Start (mm/dd/yyyy hh:mm AM/PM): ");
	        //Date date = new Date();
	        String date = input.nextLine();
	        System.out.println(date);

	        try {
	            start = LocalDateTime.now();
	        } catch (DateTimeParseException dtp) {
	            System.out.println("DATE: "+ date +"Inputformat: " + inputFormat);

	            System.out.println("Invalid input, try again.");
	        }
	    }

	    // Prompt for end date/time
	    LocalDateTime end = null;
	    while (end == null) {
	        System.out.print("End (mm/dd/yyyy hh:mm AM/PM): ");
	        String date = input.nextLine();

	        try {
	            end = LocalDateTime.now();
	        } catch (DateTimeParseException dtp) {
	            System.out.println("Invalid input, try again.");
	        }

	        if (end.isBefore(start)) {
	            System.out.println("End time must be after start time.");
	            end = null;
	        }
	    }

	    // Prompt for attendees
	    HashSet<String> attendees = new HashSet<String>();
	    System.out.print("Would you like to add attendees? (y/n): ");
	    if (input.nextLine().trim().toLowerCase().startsWith("y")) {
	        String attendee = "";
	        do {
	            System.out.print("Enter an email address (leave blank to finalize the list): ");
	            attendee = input.nextLine();

	            if (!attendee.isBlank()) {
	                attendees.add(attendee);
	            }
	        } while (!attendee.isBlank());
	    }

	    // Prompt for body
	    String body = null;
	    System.out.print("Would you like to add a body? (y/n): ");
	    if (input.nextLine().trim().toLowerCase().startsWith("y")) {
	        System.out.print("Enter a body: ");
	        body = input.nextLine();
	    }

	    // Confirm input
	    System.out.println();
	    System.out.println("New event:");
	    System.out.println("Subject: " + subject);
	    System.out.println("Start: " + start.format(inputFormat));
	    System.out.println("End: " + end.format(inputFormat));
	    System.out.println("Attendees: " + (attendees.size() > 0 ? attendees.toString() : "NONE"));
	    System.out.println("Body: " + (body == null ? "NONE" : body));

	    System.out.print("Is this correct? (y/n): ");
	    if (input.nextLine().trim().toLowerCase().startsWith("y")) {
	        Graph2.createEvent(timeZone, subject, start, end, attendees, body);
	        System.out.println("Event created.");
	        //Graph2.printAtendance();
	    } else {
	        System.out.println("Canceling.");
	    }

	    System.out.println();
	}

}
