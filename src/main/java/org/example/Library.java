package org.example;

import io.ebean.*;
import org.example.model.Books;
import org.example.model.Person;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// Database for Library
// Requests
// All transaction with books will be held here
public class Library {
    final List<Books> books;
    final Database database = DB.getDefault();
    Supplier<Character> doYouStillWantToContinue = () -> {
        Scanner scanner = new Scanner(System.in);
        char option;
        while (true) {
            System.out.print("Do you still want to continue (Y/N)?: ");
            option = scanner.nextLine().toUpperCase().charAt(0);

            if (!(option == 'Y' || option == 'N')) {
                System.out.println("Please enter Y/N only");
            } else {
                break;
            }
        }
        return option;
    };

    int input;

    // Initialize
    {
        // All hold request/borrowed book past deadline will be mark as incomplete/deadline
        Transaction transaction = database.beginTransaction();
        try {
            // Collect all hold request
            String sql = "SELECT * FROM HoldRequest WHERE holdRequestStatus = 'Pending'";
            List<SqlRow> holdRequestRows = database.sqlQuery(sql).findList();
            LocalDate now = LocalDate.now();
            for (SqlRow row : holdRequestRows) {
                int bookID = row.getInteger("bookID");
                int holdRequestID = row.getInteger("holdRequestID");
                String deadline = row.getString("holdRequestDeadline");

                if (now.isAfter(LocalDate.parse(deadline))) {
                    // Update HoldRequest (set Status = Incomplete)
                    Books book = DB.find(Books.class)
                            .where().eq("bookID", bookID).findOne();
                    sql = "UPDATE HoldRequest SET holdRequestStatus = 'Incomplete' WHERE holdRequestID = " + holdRequestID;
                    database.sqlUpdate(sql).execute();
                    book.setBookAvailability("Available");
                    database.save(book);
                }
            }

            // Collect all borrowed books
            sql = "SELECT * FROM BorrowedBooks WHERE borrowedBookStatus = 'Ongoing'";
            List<SqlRow> borrowedBookRows = database.sqlQuery(sql).findList();
            for (SqlRow row : borrowedBookRows) {
                int borrowedBookID = row.getInteger("borrowedBookID");
                int borrowerID = row.getInteger("borrowerID");
                String deadline = row.getString("borrowedBookDeadline");

                if (now.isAfter(LocalDate.parse(deadline))) {
                    // Update BorrowedBook (set Status = Overdue)
                    sql = "UPDATE BorrowedBooks SET borrowedBookStatus = 'Overdue' WHERE borrowedBookID = " + borrowedBookID;
                    database.sqlUpdate(sql).execute();
                    // Insert new data to loanPenalty
                    sql = "INSERT INTO loanPenalty (borrowerID, loanPenaltyFine, loanPenaltyStatus, loanPenaltyDateStarted)" +
                            "VALUES (" + borrowerID + ", 20, 'Unpaid', '" + LocalDate.now() + "')";
                    database.sqlUpdate(sql).execute();
                }
            }

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            System.out.println("Database error: " + e);
        } finally {
            transaction.close();
        }
    }

    public Library() {
        this.books = DB.find(Books.class).findList();
    }

    public void addNewBook(String bookTitle, String bookAuthor, String bookSubject) {
        Books book = new Books(bookTitle, bookAuthor, bookSubject, "Available");
        DB.save(book);
    }

    public void updateBook(int bookID) {
        Scanner scanner = new Scanner(System.in);
        String update = null;

        Books book = DB.find(Books.class)
                .where()
                .eq("bookID", bookID)
                .findOne();

        while (true) {
            System.out.println("\nBook ID: " + bookID);
            System.out.println("\t1. Book Title: " + book.getBookTitle() +
                    "\n\t2. Book Author: " + book.getBookAuthor() +
                    "\n\t3. Book Subject: " + book.getBookSubject() +
                    "\n\t4. Exit");

            System.out.print("Enter option [1-4]: ");
            input = scanner.nextInt();
            scanner.nextLine();

            if (input > 4) { // Warning if input is not 1-4
                System.out.println("Please enter 1-4 only!");
            } else if (input == 4) { // Break the loop (exit)
                break;
            } else {
                while (true) {
                    if (input == 1) { // Update book title
                        System.out.print("Enter new Book Title: ");
                        update = scanner.nextLine();
                        book.setBookTitle(update);
                    } else if (input == 2) { // Update book author
                        System.out.print("Enter new Book Author: ");
                        update = scanner.nextLine();
                        book.setBookAuthor(update);
                    } else if (input == 3) { // Update book subject
                        System.out.print("Enter new Book Subject: ");
                        update = scanner.nextLine();
                        book.setBookSubject(update);
                    }

                    // Saving the new update
                    if (update != null) {
                        DB.save(book);
                        break;
                    } else { // Warning if the update is empty
                        System.out.println("Please enter a valid input");
                    }
                }

            }
        }


    }

    public void deleteBook(int bookID) {
        Scanner scanner = new Scanner(System.in);
        char option;

        Books book = DB.find(Books.class)
                .where()
                .eq("bookID", bookID)
                .findOne();

        while (true) {
            System.out.println("\nDelete Book ID: " + book.getId());
            System.out.println("\tBook Title: " + book.getBookTitle() +
                    "\n\tBook Author: " + book.getBookAuthor() +
                    "\n\tBook Subject: " + book.getBookSubject());

            System.out.print("Confirm deletion (Y/N): ");
            option = scanner.nextLine().toUpperCase().charAt(0);

            if (option == 'Y') {
                DB.delete(book);
                System.out.println("Deleting Book ID: " + bookID + " [Success]");
                break;
            } else if (option == 'N') {
                System.out.println("Deleting Book ID: " + bookID + " [Failed]");
                break;
            } else {
                System.out.println("Please enter Y or N only");
            }
        }
    }

    public List<Books> getAllBooks() {
        List<Books> books = DB.find(Books.class).findList();
        books.sort(Comparator.comparing(Books::getBookTitle));

        return books;
    }

    public void displayBooks(List<Books> booksToDisplay) {
        Scanner scanner = new Scanner(System.in);
        int pageNumber = 1;
        int totalPages = (int) Math.ceil((double) booksToDisplay.size() / 10);

        displayBookByPage(pageNumber, booksToDisplay);

        while (true) {
            if (totalPages == 1) {
                System.out.print("Enter any key to exit: ");
                scanner.nextLine();
                break;
            } else {
                while (true) {
                    if (pageNumber >= totalPages) {
                        System.out.print("""
                                1. Previous Page
                                2. Search Page
                                3. Exit
                                Enter your choice:\s""");
                    } else if (pageNumber == 1) {
                        System.out.print("""
                                1. Next Page
                                2. Search Page
                                3. Exit
                                Enter your choice:\s""");
                    } else {
                        System.out.print("""
                                1. Next Page
                                2. Previous Page
                                3. Search Page
                                4. Exit
                                Enter your choice:\s""");
                    }
                    input = scanner.nextInt();

                    if (input > 4) {
                        System.out.println("Please enter 1-4 only\n");
                    } else if (input > 3) {
                        if (pageNumber == 1 || pageNumber == totalPages) {
                            System.out.println("Please enter 1-3 only\n");
                        } else {
                            break;
                        }

                    } else {
                        break;
                    }
                }
                if (input == 1) {
                    if (pageNumber == totalPages) {
                        pageNumber--;
                    } else {
                        pageNumber++;
                    }
                } else if (input == 2) {
                    if (pageNumber == 1 || pageNumber == totalPages) {
                        //search page
                        while (true) {
                            System.out.print("Enter page (1-" + totalPages + "): ");
                            int page = scanner.nextInt();

                            if (page > totalPages) {
                                System.out.println("Please enter 1-" + totalPages + " only");
                            } else {
                                pageNumber = page;
                                break;
                            }
                        }
                    } else {
                        pageNumber--;
                    }
                } else if (input == 3) {
                    if (pageNumber == 1 || pageNumber == totalPages) {
                        break;
                    } else {
                        //search page
                        while (true) {
                            System.out.print("Enter page (1-" + totalPages + "): ");
                            int page = scanner.nextInt();

                            if (page > totalPages) {
                                System.out.println("Please enter 1-" + totalPages + " only");
                            } else {
                                pageNumber = page;
                                break;
                            }
                        }
                    }
                } else if (input == 4) {
                    break;
                }
                displayBookByPage(pageNumber, booksToDisplay);
            }
        }
    }

    private void displayBookByPage(int pageNumber, List<Books> booksToDisplay) {
        int pageSize = 10;
        int totalBooks = booksToDisplay.size();
        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);

        int startIndex = (pageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalBooks);

        System.out.println("=================================================");
        System.out.println("\t\tBooks Section");
        System.out.println("\t\tPage " + pageNumber + " of " + totalPages);
        System.out.println("=================================================");

        for (int i = startIndex; i < endIndex; i++) {
            System.out.println("Book Title: " + booksToDisplay.get(i).getBookTitle()
                    + "\n\tBook ID: \t\t\t " + booksToDisplay.get(i).getId()
                    + "\n\tBook Author: \t\t " + booksToDisplay.get(i).getBookAuthor()
                    + "\n\tBook Subject: \t\t " + booksToDisplay.get(i).getBookSubject()
                    + "\n\tBook Availability:\t " + booksToDisplay.get(i).getBookAvailability() + "\n");
        }

        System.out.println("Page " + pageNumber + " of " + totalPages);

    }

    public void displayBookBy(String searchBy, String toSearch) {
        List<Books> booksToDisplay = new ArrayList<>();
        final int bookID;
        if (searchBy.equals("bookID")) {
            bookID = Integer.parseInt(toSearch);
        } else {
            bookID = 0;
        }

        switch (searchBy) {
            case "title" -> booksToDisplay = books.stream()
                    .filter(book -> book.getBookTitle().toLowerCase().contains(toSearch.toLowerCase()))
                    .collect(Collectors.toList());
            case "author" -> booksToDisplay = books.stream()
                    .filter(book -> book.getBookAuthor().toLowerCase().contains(toSearch.toLowerCase()))
                    .collect(Collectors.toList());
            case "subject" -> booksToDisplay = books.stream()
                    .filter(book -> book.getBookSubject().toLowerCase().contains(toSearch.toLowerCase()))
                    .collect(Collectors.toList());
            case "bookID" -> booksToDisplay = books.stream()
                    .filter(book -> book.getId() == bookID)
                    .collect(Collectors.toList());
        }

        if (booksToDisplay.size() == 0) {
            System.out.println("\nNo " + searchBy + " found\n");
        } else {
            displayBooks(booksToDisplay);
        }

    }

    public void requestAHold(int bookID, Person person) {
        List<Integer> holdRequestBooks = getUserHoldRequestBooks(person);
        List<Integer> borrowedBooks = getUserBorrowedBooks(person);
        Integer penaltyFee = getPenaltyFee(person);

        String sql;

        // Check if user have 3 ongoing loan or 3 hold request
        if (holdRequestBooks.size() == 3) {
            System.out.println("You can only request up to 3 books");
        } else if (borrowedBooks.size() == 3) {
            System.out.println("You can borrow up to 3 books only, return the borrowed books so you can request again");
        } else if (penaltyFee > 0) {
            System.out.println("Please pay the penalty fee so you can borrow books from our library again");
        } else {
            // Get book details
            Books book = DB.find(Books.class)
                    .where().eq("bookID", bookID).findOne();

            if (book != null) { // Check if book exists
                if (book.getBookAvailability().equals("Available")) { // Check if book is Available
                    Transaction transaction = database.beginTransaction();
                    try {
                        // Create a pending hold request
                        sql = "INSERT INTO HoldRequest (bookID, userID, holdRequestDate, holdRequestDeadline, holdRequestStatus) " +
                                "VALUES (:bookID, :userID, :holdRequestDate, :holdRequestDeadline, :holdRequestStatus)";
                        SqlUpdate insert = database.sqlUpdate(sql);
                        insert.setParameter("bookID", bookID)
                                .setParameter("userID", person.getId())
                                .setParameter("holdRequestDate", String.valueOf(LocalDate.now()))
                                .setParameter("holdRequestDeadline", String.valueOf(LocalDate.now().plusDays(2)))
                                .setParameter("holdRequestStatus", "Pending");
                        insert.execute();
                        book.setBookAvailability("Not Available");
                        database.save(book);
                        sql = "SELECT seq from SQLITE_SEQUENCE WHERE name = 'HoldRequest'";
                        SqlRow sqlRow = database.sqlQuery(sql).findOne();
                        int holdRequestID = sqlRow.getInteger("seq");
                        transaction.commit();
                        System.out.println("\nHold Request Successful");
                        System.out.println("Hold RequestID: " + holdRequestID + " [IMPORTANT]");
                        System.out.println("Book: [" + book.getBookTitle() + "] is now on hold. \nPlease go to the clerk to get the book." +
                                "\nGet the book before [" + LocalDate.now().plusDays(2) + "] or it will become available for other students to borrow\n");
                    } catch (Exception e) {
                        transaction.rollback();
                        System.out.println("Database error");
                    } finally {
                        transaction.close();
                    }
                } else {
                    System.out.println("Book is not available as of this moment");
                }
            } else {
                System.out.println("No book found");
            }
        }
    }

    private List<Integer> getUserHoldRequestBooks(Person person) {
        // Get bookIDs from HoldRequest Table
        String sqlHoldRequest = "SELECT bookID FROM HoldRequest WHERE userID = " + person.getId() + " AND holdRequestStatus = 'Pending'";
        List<SqlRow> rowHoldRequest = database.sqlQuery(sqlHoldRequest).findList();

        return rowHoldRequest.stream()
                .map(row -> row.getInteger("bookID")).toList();
    }

    private List<Integer> getUserBorrowedBooks(Person person) {
        // Get book IDs
        String sql = "SELECT bookID FROM BorrowedBooks WHERE borrowerID = " + person.getId() + " AND borrowedBookStatus = 'Ongoing'";
        List<SqlRow> rows = database.sqlQuery(sql).findList();

        return rows.stream()
                .map(row -> row.getInteger("bookID")).toList();
    }

    private Integer getPenaltyFee(Person person) {
        Integer penaltyFee;

        String sqlPenalty = "SELECT loanPenaltyFine FROM loanPenalty WHERE borrowerID = " + person.getId() + " AND loanPenaltyStatus = 'Unpaid'";
        SqlRow rowPenalty = database.sqlQuery(sqlPenalty).findOne();
        penaltyFee = Optional.ofNullable(rowPenalty != null ? rowPenalty.getInteger("loanPenaltyFine") : null).orElse(0);

        return penaltyFee;
    }

    public void showLoanDetails(Person person) {
        // Display Loan Details

        List<Integer> holdRequestBooks = getUserHoldRequestBooks(person);
        List<Integer> borrowedBooks = getUserBorrowedBooks(person);
        Integer penaltyFee = getPenaltyFee(person);

        if (holdRequestBooks.size() > 0) { // Display book requested to be loan (should be greater than 1 to be displayed)
            System.out.println("Book requested: " + holdRequestBooks.size());
        }

        if (borrowedBooks.size() > 0) { // Display book loaned (greater than 0 and less than or equal to 3)
            System.out.println("Book borrowed: " + borrowedBooks.size());
        }

        if (penaltyFee > 0) { // Display penalty fine
            System.out.println("Penalty Fee: " + penaltyFee);
        }

    }

    public void viewOngoingTransaction(Person person) {
        // Display ongoing transaction

        List<Integer> holdRequestBooks = getUserHoldRequestBooks(person);
        List<Integer> borrowedBooks = getUserBorrowedBooks(person);
        Integer penaltyFee = getPenaltyFee(person);
        Scanner scanner = new Scanner(System.in);
        String sql;

        System.out.println("=================================================");
        System.out.println("\t\tOngoing Transaction");
        System.out.println("=================================================");

        if (!(holdRequestBooks.size() > 0 || borrowedBooks.size() > 0 || penaltyFee > 0)) {
            System.out.println("You have no ongoing transaction right now");
        } else {
            if (holdRequestBooks.size() > 0) {
                System.out.println("Hold Request: " + holdRequestBooks.size());
                for (Integer bookID : holdRequestBooks) {
                    sql = "SELECT bookTitle FROM Books WHERE bookID = " + bookID;
                    System.out.println("\tBook Title: " + database.sqlQuery(sql).findOne().getString("bookTitle"));
                    sql = "SELECT holdRequestDeadline FROM HoldRequest WHERE userID = " + person.getId()
                            + " AND bookID = " + bookID + " AND holdRequestStatus = 'Pending'";
                    System.out.println("\tHold Request Deadline: " + database.sqlQuery(sql).findOne().getString("holdRequestDeadline"));
                    System.out.println("\n");
                }
            }
            if (borrowedBooks.size() > 0) {
                System.out.println("Borrowed Books: " + borrowedBooks.size());
                for (Integer bookID : borrowedBooks) {
                    sql = "SELECT bookTitle FROM Books WHERE bookID = " + bookID;
                    System.out.println("\tBook Title: " + database.sqlQuery(sql).findOne().getString("bookTitle"));
                    sql = "SELECT borrowedBookDeadline FROM BorrowedBooks WHERE borrowerID = " + person.getId()
                            + " AND bookID = " + bookID + " AND borrowedBookStatus = 'Ongoing'";
                    System.out.println("\tBorrowed Book Deadline: " + database.sqlQuery(sql).findOne().getString("borrowedBookDeadline"));
                    System.out.println("\n");
                }
            }
            if (penaltyFee > 0) {
                System.out.println("Penalty Fee: " + penaltyFee);
            }
        }

        System.out.print("\nEnter any keys to exit: ");
        scanner.nextLine();
        System.out.println("\n\n\n");
    }

    public void viewTransactionHistory(Person person) {
        // Display transaction history
        Scanner scanner = new Scanner(System.in);

        // Get book IDs
        String sql = "SELECT * FROM BorrowedBooks WHERE borrowerID = " + person.getId() + " AND borrowedBookStatus = 'Complete'";
        List<SqlRow> rows = database.sqlQuery(sql).findList();

        System.out.println("""
                =================================================
                \t\tTransaction History
                =================================================
                """);

        for (SqlRow row : rows) {
            sql = "SELECT bookTitle FROM Books WHERE bookID = " + row.getString("bookID");
            SqlRow newRow = database.sqlQuery(sql).findOne();
            System.out.println("Title: " + newRow.getString("bookTitle"));
            System.out.println("\tDate Borrowed: " + row.getString("borrowedBookStartDate"));
            System.out.println("\tDate Returned: " + row.getString("borrowedBookReturnDate"));
        }

        System.out.print("\nEnter any keys to exit: ");
        scanner.nextLine();
        System.out.println("\n\n");
    }

    public void checkInAndOutBook(Long clerkID) {
        Scanner scanner = new Scanner(System.in);
        int requestID;
        String sql;

        while (true) {
            System.out.println("=================================================");
            System.out.println("1. Check In \n2. Check Out\n3. Exit");
            System.out.print("Enter your choice (1-3): ");
            input = scanner.nextInt();

            if (input >= 4) {
                System.out.println("Please enter 1-3 only");
            } else {
                if (input == 1) { // Check in a book
                    while (true) {
                        System.out.println("=================================================");
                        System.out.print("Enter Hold Request ID: ");
                        requestID = scanner.nextInt();

                        sql = "SELECT * FROM HoldRequest WHERE holdRequestID = " + requestID + " AND holdRequestStatus = 'Pending'";
                        SqlRow sqlRow = database.sqlQuery(sql).findOne();

                        if (sqlRow != null) {
                            checkInBook(sqlRow, clerkID);
                            break;
                        } else {
                            System.out.println("No Hold Request ID Found");

                            if (doYouStillWantToContinue.get().equals('N')) {
                                break;
                            }
                        }
                    }
                } else if (input == 2) { // Check out a book
                    while (true) {
                        System.out.println("=================================================");
                        System.out.print("Enter Book Borrowed ID: ");
                        requestID = scanner.nextInt();

                        sql = "SELECT * FROM BorrowedBooks WHERE borrowedBookID = " + requestID + " AND borrowedBookStatus = 'Ongoing' OR borrowedBookStatus = 'Overdue'";
                        SqlRow sqlRow = database.sqlQuery(sql).findOne();

                        if (sqlRow != null) {
                            checkOutBook(sqlRow);
                            break;
                        } else {
                            System.out.println("No Book Borrowed ID Found");

                            if (doYouStillWantToContinue.get().equals('N')) {
                                break;
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }

    private void checkInBook(SqlRow sqlRow, Long clerkID) {
        Scanner scanner = new Scanner(System.in);
        char option;
        String sql;

        int holdRequestID = sqlRow.getInteger("holdRequestID");
        int bookID = sqlRow.getInteger("bookID");
        int userID = sqlRow.getInteger("userID");

        System.out.println("Hold Request ID: " + holdRequestID);
        System.out.println("\tBook ID: " + bookID);
        System.out.println("\tBorrower ID: " + userID);

        while (true) {
            System.out.print("Confirm check in (Y/N): ");
            option = scanner.nextLine().toUpperCase().charAt(0);

            if (option == 'Y') {
                Transaction transaction = database.beginTransaction();

                try {
                    // Update hold request
                    sql = "UPDATE HoldRequest SET holdRequestStatus = 'Complete' WHERE holdRequestID = " + holdRequestID;
                    database.sqlUpdate(sql).execute();

                    // Insert borrowed book
                    sql = "INSERT INTO BorrowedBooks (bookID, borrowerID, clerkID, borrowedBookStatus, borrowedBookStartDate, borrowedBookDeadline)" +
                            "VALUES (:bookID, :borrowerID, :clerkID, :borrowedBookStatus, :borrowedBookStartDate, :borrowedBookDeadline)";
                    SqlUpdate insert = database.sqlUpdate(sql);
                    insert.setParameter("bookID", bookID)
                            .setParameter("borrowerID", userID)
                            .setParameter("clerkID", clerkID)
                            .setParameter("borrowedBookStatus", "Ongoing")
                            .setParameter("borrowedBookStartDate", String.valueOf(LocalDate.now()))
                            .setParameter("borrowedBookDeadline", String.valueOf(LocalDate.now().plusDays(7)));
                    insert.execute();
                    transaction.commit();
                    System.out.println("Checking-In Hold Request ID: " + holdRequestID + " [Success]");
                } catch (Exception e) {
                    transaction.rollback();
                    System.out.println("Database error: " + e);
                    System.out.println("Checking-In Hold Request ID: " + holdRequestID + " [Failed]");
                } finally {
                    transaction.close();
                }

                break;
            } else if (input == 'N') {
                System.out.println("Checking-In Hold Request ID: " + sqlRow.getInteger("holdRequestID") + " [Failed]");
                break;
            } else {
                System.out.println("Please enter Y or N only");
            }
        }
    }

    private void checkOutBook(SqlRow sqlRow) {
        Scanner scanner = new Scanner(System.in);
        char option;
        String sql;

        int borrowedBookID = sqlRow.getInteger("borrowedBookID");
        int bookID = sqlRow.getInteger("bookID");
        int userID = sqlRow.getInteger("borrowerID");

        System.out.println("Book Borrowed ID: " + borrowedBookID);
        System.out.println("\tBook ID: " + bookID);
        System.out.println("\tBorrower ID: " + userID);

        while (true) {
            System.out.print("Confirm check out (Y/N): ");
            option = scanner.nextLine().toUpperCase().charAt(0);

            if (option == 'Y') {
                Transaction transaction = database.beginTransaction();

                try {
                    // Update borrowed book (set status to complete, set return date to today date)
                    sql = "UPDATE BorrowedBooks SET borrowedBookStatus = 'Complete', borrowedBookReturnDate = '" + LocalDate.now()
                            + "' WHERE borrowedBookID = " + borrowedBookID;
                    database.sqlUpdate(sql).execute();
                    transaction.commit();
                    System.out.println("Checking-Out Borrowed Book ID: " + borrowedBookID + " [Success]");
                } catch (Exception e) {
                    transaction.rollback();
                    System.out.println("Database error: " + e);
                    System.out.println("Checking-Out Borrowed Book ID: " + borrowedBookID + " [Failed]");
                } finally {
                    transaction.close();
                }

                break;
            } else if (input == 'N') {
                System.out.println("Checking-In Hold Request ID: " + sqlRow.getInteger("holdRequestID") + " [Failed]");
                break;
            } else {
                System.out.println("Please enter Y or N only");
            }
        }
    }

    public void penaltyFine() {
        Scanner scanner = new Scanner(System.in);
        char option;
        String sql;
        int borrowerID;
        int totalFine = 0;

        while (true) {
            System.out.println("\n=================================================");
            System.out.println("\t\tSettle Penalty Fine");
            System.out.println("=================================================");
            System.out.print("Enter Borrower ID: ");
            borrowerID = scanner.nextInt();
            scanner.nextLine();

            sql = "SELECT * FROM loanPenalty WHERE borrowerID = " + borrowerID + " AND loanPenaltyStatus = 'Unpaid'";
            List<SqlRow> sqlRow = database.sqlQuery(sql).findList();

            if (sqlRow != null) {
                // Check if a book is still not returned
                sql = "SELECT * FROM BorrowedBooks WHERE borrowerID = " + borrowerID + " AND borrowedBookStatus = 'Overdue'";
                SqlRow sqlRowCheckOverdue = database.sqlQuery(sql).findOne();
                if (sqlRowCheckOverdue!= null) {
                    System.out.println("Borrower still has some overdue books that hasn't been return");
                    break;
                } else {
                    // Compute total fine
                    for (SqlRow row : sqlRow) {
                        totalFine += row.getInteger("loanPenaltyFine");
                    }

                    System.out.println("\nAmount to settle: " + totalFine);
                    System.out.print("Confirm payment (Y/N): ");
                    option = scanner.nextLine().toUpperCase().charAt(0);

                    if (option == 'Y') {
                        Transaction transaction = database.beginTransaction();

                        try {
                            // Update borrowed book (set status to complete, set return date to today date)
                            for (SqlRow row : sqlRow) {
                                sql = "UPDATE loanPenalty SET loanPenaltyStatus = 'Paid', loanPenaltyDatePaid = '" + LocalDate.now()
                                        + "' WHERE loanPenaltyID = " + row.getInteger("loanPenaltyID");
                                database.sqlUpdate(sql).execute();
                            }

                            transaction.commit();
                            System.out.println("Payment Successful");
                            System.out.print("Enter any key to exit: ");
                            scanner.nextLine();
                            break;
                        } catch (Exception e) {
                            transaction.rollback();
                            System.out.println("Database error: " + e);
                            System.out.println("Payment Failed");
                        } finally {
                            transaction.close();
                        }

                    } else if (option == 'N') {
                        System.out.println("Payment Failed");

                        if (doYouStillWantToContinue.get().equals('N')) {
                            break;
                        }
                    }
                }

            } else {
                System.out.println("No Borrower ID Found");
                if (doYouStillWantToContinue.get().equals('N')) {
                    break;
                }
            }
        }
    }
}
