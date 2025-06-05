package org.example;

import io.ebean.DB;
import org.example.model.Books;
import org.example.model.Person;

import java.util.Scanner;

import java.util.function.Function;

public class Main {
    static Person person = null;
    static Library library = new Library();
    static Function<String, Character> continueOption = (s) -> {
        Scanner scanner = new Scanner(System.in);
        Character option;

        while (true) {
            System.out.print("Do you still want to continue changing your " + s + "? [Y/N]: ");
            option = scanner.nextLine().toUpperCase().charAt(0);

            if (!(option.equals('N') || option.equals('Y'))) {
                System.out.println("Please enter Y and N only");
            } else {
                break;
            }
        }

        return option;
    };

    static int input;

    public static void main(String[] args) {
        DB.getDefault();

        mainPage();
    }

    public static void mainPage() {
        // Main Page == Opening the Library Management System

        Scanner scanner = new Scanner(System.in);
        int category;

        // This loop will continue until the user inputs the correct options (1, 2, 3)
        while (true) {
            // Option for Students Portal or Admins Portal
            System.out.println("=================================================");
            System.out.println("\t\tLibrary Management System");
            System.out.println("=================================================");
            System.out.println("1. Student Portal\n2. Admins Portal\n3. Exit");
            System.out.print("Enter Option: ");
            category = scanner.nextInt();

            if (category < 4) {
                if (category == 3) { // End the system
                    System.out.println("Thanks for visiting!!");
                    System.exit(0);
                } else { // Continue to Log/Register option
                    while (true) {
                        System.out.println("=================================================");
                        System.out.println("1. Log-in\n2. Register\n3. Exit");
                        System.out.print("Enter Option: ");
                        input = scanner.nextInt();

                        if (input == 1) {
                            login((category == 1) ? "Student" : "Staff");
                        } else if (input == 2) {
                            register((category == 1) ? "Student" : "Staff");
                        } else if (input == 3) {
                            break;
                        } else {
                            System.out.println("Please enter (1, 2 or 3) only");
                        }
                    }
                }
            } else {
                System.out.println("Please enter (1, 2 or 3) only");
            }
        }
    }

    public static void register(String category) {
        // Register page

        Scanner scanner = new Scanner(System.in);
        String username, password, name, address;
        int staff;

        System.out.println("=================================================");
        System.out.println("\t\tRegister");

        // Loop again until all data are correct
        while (true) {
            // Get all the user details
            System.out.println("=================================================");
            System.out.println("""
                    Notes:\s
                    * Username should be minimum of 7 characters
                    * Password should be at least 12 characters
                    """);
            System.out.print("Username: ");
            username = scanner.nextLine();
            System.out.print("Password: ");
            password = scanner.nextLine();
            System.out.print("Name: ");
            name = scanner.nextLine();
            System.out.print("Address: ");
            address = scanner.nextLine();

            // For Staff (clarifying if they're Librarian or Clerk)
            if (category.equals("Staff")) {
                while (true) {
                    try {
                        System.out.println("1. Librarian\n2. Clerk");
                        System.out.print("Enter option: ");
                        staff = scanner.nextInt();
                        scanner.nextLine();

                        if (staff > 2) {
                            System.out.println("Enter 1 or 2 only");
                        } else {
                            switch (staff) {
                                case 1 -> category = "Librarian";
                                case 2 -> category = "Clerk";
                            }
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("Enter numeric characters only");
                        scanner.nextLine();
                    }
                }
            }

            // Validating details
            if (username.length() > 6 && password.length() > 11
                    && name.length() > 0 && address.length() > 0) {

                // Check if username exists
                Person found = DB.find(Person.class)
                        .where()
                        .eq("personUsername", username)
                        .findOne();

                if (found != null) { // Username Exists
                    System.out.println("Username Exists! Try again");
                } else { // Username doesn't exist (Saves the info to the database)
                    System.out.println("Details: "
                            + "\n\tUsername: " + username
                            + "\n\tName: " + name
                            + "\n\tAddress: " + address);
                    DB.save(new Person(username, password, name, address, category));
                    login("Staff");
                    break;
                }

            } else {
                System.out.println("Your username or password is short");
            }

        }

    }

    public static void login(String category) {
        // Login Page
        String username, password;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Get the username and password
            System.out.println("=================================================");
            System.out.println("\t\tLogin");
            System.out.println("=================================================");
            System.out.print("Username: ");
            username = scanner.nextLine();
            System.out.print("Password: ");
            password = scanner.nextLine();

            // Check if username and password is correct
            person = DB.find(Person.class)
                    .where()
                    .eq("personUsername", username)
                    .eq("personPassword", password)
                    .findOne();

            if (person != null) {
                if (category.equals("Staff")) { // Category == Staff
                    if (person.getCategory().equals("Student")) {
                        // If the account is for student and the student picks the Admins Portal they will go back to the main page
                        System.out.println("Please select the Student Portal");
                        person = null;
                        mainPage();
                    } else {
                        adminsPortal();
                    }
                } else { // Category == Student
                    if (!person.getCategory().equals("Student")) {
                        // If the account is for staff and the user picks the Student Portal they will go back to the main page
                        System.out.println("Please select the Admins Portal");
                        person = null;
                        mainPage();
                    } else {
                        studentsPortal();
                    }
                }
            } else {
                System.out.println("Wrong ID or Password");
                System.out.print("Do you want to continue? (Y/N): ");
                char continueOption = scanner.nextLine().toUpperCase().charAt(0);
                if (continueOption == 'N') {
                    System.out.println("Bye!");
                    break;
                }
            }
        }
    }

    public static void studentsPortal() {
        /*
        Student's view:
        - View Profile
            - Edit Profile (change all details)
        - View Books
            - View all books
                - Search books by: Name, Author or Subject
                - Request Book to loan
        - View Transaction
            - Pending borrowed books
            - View Penalty
            - View returned books
        - Logout
        */

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=================================================");
            System.out.println("\t\tStudent's Portal");
            System.out.println("Account: " + person.getName());
            library.showLoanDetails(person);
            System.out.println("=================================================");
            System.out.println("1. Profile"); // User can view and edit profile details
            System.out.println("2. View Books"); // User can view books and search a book and request to loan
            System.out.println("3. Transaction"); // User can view the pending and completed transaction, plus penalty if he has one
            System.out.println("4. Log out");
            System.out.print("Enter Choice: ");
            input = scanner.nextInt();

            if (input == 1) { // View Profile
                viewProfile();
            } else if (input == 2) { // View Books
                viewBooks();
            } else if (input == 3) { // View Borrowed Books
                viewTransaction();
            } else {
                System.out.println("Goodbye!");
                System.exit(0);
            }
        }
    }

    public static void viewProfile() {
        /*
        Profile page:
        - Can view details (View Profile)
        - Can edit details (Edit Profile)
        - Go back
         */
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=================================================");
            System.out.println("\t\tProfile");
            System.out.println("=================================================");
            System.out.println("1. Edit profile");
            System.out.println("2. Exit");
            System.out.print("Enter Choice: ");
            input = scanner.nextInt();

            if (input == 1) { // Edit profile
                editProfile();
            } else if (input == 2) { // Exit (go back to the student portal)
                break;
            } else {
                System.out.println("Enter 1 and 2 only");
            }
        }
    }

    public static void editProfile() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("=================================================");
            System.out.println("\t\tEdit Profile");
            System.out.println("1. Username: " + person.getUsername());
            System.out.println("2. Password: " + person.getPassword().replaceAll(".", "*")); // Show password but with *
            System.out.println("3. Name: " + person.getName());
            System.out.println("4. Address: " + person.getAddress());
            System.out.println("5. Exit");
            System.out.print("Enter Choice: ");
            input = scanner.nextInt();
            scanner.nextLine();

            if (input == 1) { // Change Username
                while (true) {
                    System.out.print("Enter new username: ");
                    String newUsername = scanner.nextLine();
                    if (newUsername.length() > 6) { // Check if username is greater than 6
                        if (DB.find(Person.class)
                                .where()
                                .eq("personUsername", newUsername)
                                .findOne() != null) { // Check if username exists
                            System.out.println("Username exists!");
                            if (continueOption.apply("username") == 'N') { // Go back to the profile
                                break;
                            }
                        } else {
                            person.setUsername(newUsername);
                            DB.save(person);
                            System.out.println("Username successfully changed");
                            break;
                        }
                    } else {
                        System.out.println("Username is short!");
                        if (continueOption.apply("username") == 'N') { // Go back to the profile
                            break;
                        }
                    }
                }
            } else if (input == 2) { // Change Password
                while (true) {
                    // Old password for confirmation
                    System.out.print("Enter old password: ");
                    String oldPassword = scanner.nextLine();

                    if (person.getPassword().equals(oldPassword)) {
                        // New Password
                        while (true) {
                            System.out.print("Enter new password: ");
                            String newPassword = scanner.nextLine();

                            if (newPassword.length() > 11) { // If new password is greater than 11
                                person.setPassword(newPassword);
                                DB.save(person);
                                System.out.println("Password successfully changed");
                                break;
                            } else { // If password is too short
                                System.out.println("Password is too short!");
                                if (continueOption.apply("password") == 'N') { // Go back to the profile
                                    break;
                                }
                            }
                        }
                        break;
                    } else {
                        System.out.println("Wrong password");
                        if (continueOption.apply("password") == 'N') { // Go back to the profile
                            break;
                        }
                    }
                }
            } else if (input == 3) { // Change Name
                System.out.print("Enter new name: ");
                String newName = scanner.nextLine();
                if (newName.length() > 5) {
                    person.setName(newName);
                    DB.save(person);
                    System.out.println("Name successfully changed");
                } else {
                    System.out.println("Too short!");
                    if (continueOption.apply("name") == 'N') { // Go back to the profile
                        break;
                    }
                }

            } else if (input == 4) { // Change Address
                System.out.print("Enter new address: ");
                String newAddress = scanner.nextLine();
                if (newAddress.length() > 5) {
                    person.setAddress(newAddress);
                    DB.save(person);
                    System.out.println("Address successfully changed");
                    break;
                } else {
                    System.out.println("Too short!");
                    if (continueOption.apply("address") == 'N') { // Go back to the profile
                        break;
                    }
                }
            } else if (input == 5) { // Exit
                break;
            } else { // Wrong input
                System.out.println("Enter 1-5 only");
            }
        }
    }

    public static void viewBooks() {
        /*
        - View Books
            - View all books
                - Search books by: Name, Author or Subject
                - Request Book to loan
         */
        Scanner scanner = new Scanner(System.in);
        String toSearch; // for searching a book by name/author/subject

        while (true) {
            System.out.println("=================================================");
            System.out.println("\t\tBook Section");
            System.out.println("=================================================");
            System.out.println("1. View all books");
            System.out.println("2. Search by book's title");
            System.out.println("3. Search by book's author");
            System.out.println("4. Search by book's subject");
            if (person.getCategory().equals("Student")) {
                System.out.println("5. Loan a book");
            } else if (person.getCategory().equals("Librarian")) {
                System.out.println("5. Search by book's ID");
            }
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");
            input = scanner.nextInt();
            scanner.nextLine();

            if (input == 1) {
                library.displayBooks(library.getAllBooks());
            } else if (input == 2) {
                System.out.print("Enter book's title: ");
                toSearch = scanner.nextLine();
                library.displayBookBy("title", toSearch);
            } else if (input == 3) {
                System.out.print("Enter book's author: ");
                toSearch = scanner.nextLine();
                library.displayBookBy("author", toSearch);
            } else if (input == 4) {
                System.out.print("Enter book's subject: ");
                toSearch = scanner.nextLine();
                library.displayBookBy("subject", toSearch);
            } else if (input == 5) {
                if (person.getCategory().equals("Student")) {
                    System.out.print("Enter Book ID to borrow: ");
                    int bookID = scanner.nextInt();
                    scanner.nextLine();
                    library.requestAHold(bookID, person);
                } else if (person.getCategory().equals("Librarian")) {
                    System.out.print("Enter book's ID: ");
                    int bookID = scanner.nextInt();
                    library.displayBookBy("bookID", String.valueOf(bookID));
                }

            } else if (input == 6) {
                break;
            } else {
                System.out.println("Please enter 1-5 only");
            }
        }
    }

    public static void viewTransaction() {
        /*
        Transaction page
            - Show ongoing book request/borrowed books/penalty fee
            - View history
                - View Borrowed Books (Status)
         */
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=================================================");
            System.out.println("\t\tTransaction");
            System.out.println("=================================================");
            System.out.println("1. View Ongoing Transaction");
            System.out.println("2. View History");
            System.out.println("3. Exit");
            System.out.print("Enter Choice: ");
            input = scanner.nextInt();

            if (input == 1) { // View Ongoing Transaction
                library.viewOngoingTransaction(person);
            } else if (input == 2) { // View History
                library.viewTransactionHistory(person);
            } else if (input == 3) { // Exit
                break;
            } else {
                System.out.println("Enter 1-3 only");
            }

        }
    }

    private static void adminsPortal() {
        /*
        Staff's view:
        - Clerk
            - View Profile
                - Edit Profile
            - View Books
                - View all books
                    - Search books by: Name, Author or Subject
            - Check in/out books
                - Check in
                - Check out
                - Check borrower ID
            - View Transaction
                - Ongoing
                - Past Deadline
            - Logout

        - Librarian
            - View Profile
                - Edit Profile
            - View Books
                - View all books
                    - Search books by: Name, Author or Subject
            - Add/Edit/Delete Books
                - Add
                - Edit
                - Delete
            - Logout
        */

        Scanner scanner = new Scanner(System.in);
        char option;

        while (true) {
            System.out.println("=================================================");
            if (person.getCategory().equals("Clerk")) {
                System.out.println("\t\tClerk's Portal");
                System.out.println("Account: " + person.getName());
                System.out.println("=================================================");
                System.out.println("1. Profile"); // User can view and edit profile details
                System.out.println("2. Check In/Out Book"); // Clerk can check in, check out or record that a fine was paid
                System.out.println("3. Settle Penalty Fine"); // Pay penalty fine
                System.out.println("4. Transaction"); // User can view the pending and completed transaction, plus penalty if he has one
                System.out.println("5. Log out");
                System.out.print("Enter Choice: ");
                input = scanner.nextInt();

                if (input == 1) { // View Profile
                    viewProfile();
                } else if (input == 2) { // Check in/out books
                    library.checkInAndOutBook(person.getId());
                } else if (input == 3) { // Settle penalty fine
                    library.penaltyFine();
                } else if (input == 4) { // View Transaction
                    viewTransaction();
                } else {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
            } else if (person.getCategory().equals("Librarian")) {
                System.out.println("\t\tLibrarian's Portal");
                System.out.println("Account: " + person.getName());
                System.out.println("=================================================");
                System.out.println("1. Profile"); // User can view and edit profile details
                System.out.println("2. View Books"); // User can view books and search a book and request to loan
                System.out.println("3. Add/Edit/Delete Books"); // Librarian can add, edit, or delete a book
                System.out.println("4. Log out");
                System.out.print("Enter Choice: ");
                input = scanner.nextInt();

                if (input == 1) { // View Profile
                    viewProfile();
                } else if (input == 2) { // View Books
                    viewBooks();
                } else if (input == 3) { // Add, edit or delete a book
                    while (true) {
                        System.out.println("=================================================");
                        System.out.println("\t\tAdd\\Edit\\Delete a Book");
                        System.out.println("=================================================");
                        System.out.println("1. Add");
                        System.out.println("2. Edit");
                        System.out.println("3. Delete");
                        System.out.println("4. Exit");
                        System.out.print("Enter Choice: ");
                        input = scanner.nextInt();
                        scanner.nextLine();

                        if (input > 4) {
                            System.out.println("Enter 1-4 only");
                        } else {
                            if (input == 1) { // Adding new book
                                String bookTitle, bookAuthor, bookSubject;
                                System.out.println("=================================================");
                                System.out.println("\t\tAdd a new book");
                                System.out.println("=================================================");
                                System.out.println("Enter the following details: \nBook's Title, Book's Author, Book's Subject\n");
                                System.out.print("Book Title: ");
                                bookTitle = scanner.nextLine();
                                System.out.print("Book Author: ");
                                bookAuthor = scanner.nextLine();
                                System.out.print("Book Subject: ");
                                bookSubject = scanner.nextLine();
                                System.out.println("Adding: " +
                                        "\n\tBook Title: " + bookTitle +
                                        "\n\tBook Author: " + bookAuthor +
                                        "\n\tBook Subject: " + bookSubject);

                                while (true) { // Confirming to add a new book
                                    System.out.print("Proceed to add? [Y/N]: ");
                                    option = scanner.nextLine().toUpperCase().charAt(0);
                                    if (!(option == 'Y' || option == 'N')) {
                                        System.out.println("Please enter Y or N only");
                                    } else {
                                        break;
                                    }
                                }

                                if (option == 'Y') {
                                    library.addNewBook(bookTitle, bookAuthor, bookSubject);
                                    System.out.println("Adding: " + bookTitle + " [Success]");
                                } else {
                                    System.out.println("Adding: " + bookTitle + " [Failed]");
                                }
                            } else if (input == 2) { // Edit a book
                                while (true) {
                                    System.out.println("=================================================");
                                    System.out.println("\t\tEdit book");
                                    System.out.println("=================================================");
                                    System.out.print("Enter Book ID: ");
                                    int bookID = scanner.nextInt();

                                    if (DB.find(Books.class).where().eq("bookID", bookID).findOne() != null) {
                                        // Book ID exists
                                        library.updateBook(bookID);
                                        break;
                                    } else {
                                        // Book ID does not exist
                                        scanner.nextLine();
                                        System.out.println("\nBook ID does not exists");
                                        while (true) {
                                            System.out.print("Do you want to try again? Y/N: ");
                                            option = scanner.nextLine().toUpperCase().charAt(0);

                                            if (!(option == 'Y' || option == 'N')) {
                                                System.out.println("Please enter Y or N only");
                                            } else {
                                                break;
                                            }
                                        }

                                        if (option == 'N') {
                                            break;
                                        }
                                    }
                                }
                            } else if (input == 3) { // Delete a book
                                while (true) {
                                    System.out.println("=================================================");
                                    System.out.println("\t\tDelete book");
                                    System.out.println("=================================================");
                                    System.out.print("Enter Book ID: ");
                                    int bookID = scanner.nextInt();

                                    if (DB.find(Books.class).where().eq("bookID", bookID).findOne() != null) {
                                        // Book ID exists
                                        library.deleteBook(bookID);
                                        break;
                                    } else {
                                        // Book ID does not exist
                                        scanner.nextLine();
                                        System.out.println("\nBook ID does not exists");
                                        while (true) {
                                            System.out.print("Do you want to try again? Y/N: ");
                                            option = scanner.nextLine().toUpperCase().charAt(0);

                                            if (!(option == 'Y' || option == 'N')) {
                                                System.out.println("Please enter Y or N only");
                                            } else {
                                                break;
                                            }
                                        }

                                        if (option == 'N') {
                                            break;
                                        }
                                    }
                                }

                            } else {
                                break;
                            }
                        }
                    }

                } else {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
            }

        }
    }
}