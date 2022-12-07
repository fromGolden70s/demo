package banking;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import org.sqlite.SQLiteDataSource;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


class Main {
    static Scanner scanner = new Scanner(System.in);
    static int[] cardNo = new int[16];
    static int[] pin = new int[4];
    static String cardNoString;
    static String pinString;
    static int balance;
    static String file;
    static String url;
    static SQLiteDataSource dataSource = new SQLiteDataSource();


    public static void main(String[] args) {
        file = args[1];
        url = "jdbc:sqlite:C:\\Users\\1\\IdeaProjects\\Simple Banking System\\Simple Banking System\\task\\" + file;

        menu();
    }

    public static void menu() {
        System.out.println("1. Create an account\n2. Log into account\n0. Exit");
        int input = scanner.nextInt();
        switch(input) {
            case 1:
                createAccount();
                break;
            case 2:
                logIn();
                break;
            case 0:
                System.out.println("Bye!");
                System.exit(0);
                break;
            default:
                System.out.println("Wrong input");
                menu();
                break;
        }
    }

    public static void createAccount() {

        Random ran = new Random();

        cardNo[0] = 4;
        cardNo[1] = 0;
        cardNo[2] = 0;
        cardNo[3] = 0;
        cardNo[4] = 0;
        cardNo[5] = 0;
        for(int i = 6; i <= 14; i++) {
            cardNo[i] = ran.nextInt(10);
        }
        luhn();

        for(int i = 0; i < 4; i++) {
            pin[i] = ran.nextInt(10);
        }
        cardNoString = Arrays.toString(cardNo).replace(", ", "").replace("[", "").replace("]", "");
        pinString = Arrays.toString(pin).replace(", ", "").replace("[", "").replace("]", "");

        System.out.println("\nYour card has been created\nYour card number:");
        for (int j : cardNo) {
            System.out.print(j);
        }
        System.out.println("\nYour card PIN:");
        for (int j: pin) {
            System.out.print(j);
        }
        System.out.println("\n");

        int tableId = 1;

        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {

            try (Statement statement = con.createStatement()) {
                try (ResultSet tableTemp = statement.executeQuery("SELECT * FROM CARD;")) {
                    while (tableTemp.next()) {
                        tableId++;
                    }
                }
                String text = "INSERT INTO card (id, number, pin) VALUES (" + tableId + ", '" +
                        cardNoString + "', '" + pinString + "');";
                statement.executeUpdate(text);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        menu();
    }

    public static void loggedMenu() {
        System.out.println("\n\n1. Balance\n2. Add income\n3. Do transfer\n4. Close account\n5. Log out\n0. Exit");
        int input = scanner.nextInt();
        switch(input) {
            case 1:
                System.out.print("Balance: " + balance);
                loggedMenu();
                break;
            case 2:
                enterIncome();
                break;
            case 3:
                transfer();
                break;
            case 4:
                closeAcc();
                break;
            case 5:
                System.out.println("You have successfully logged out!");
                menu();
                break;
            case 0:
                System.out.println("Bye!");
                System.exit(0);
                break;
            default:
                System.out.println("Wrong input");
                menu();
                break;
        }
    }

    public static void enterIncome() {
        System.out.println("Enter income:");
        int addIncome = scanner.nextInt();
        balance += addIncome;

        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate ("UPDATE CARD SET balance = " + balance +
                        " WHERE number = " + cardNoString + ";");
                System.out.println("Income was added!\n");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loggedMenu();
    }

    public static void transfer() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Transfer\nEnter card number:");
        String transferTo = scanner.nextLine();
        //checking last digit
        try {
            for (int i = 0; i < 16; i++) {
                cardNo[i] = Integer.parseInt(String.valueOf(transferTo.charAt(i)));
            }
        } catch (Exception e) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            loggedMenu();
        }
        luhn();
        if (cardNo[15] != Integer.parseInt(String.valueOf(transferTo.charAt(15)))) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            loggedMenu();
        }

        String checkCard = "SELECT * FROM CARD WHERE number = " + transferTo + ";";
        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                try (ResultSet result = statement.executeQuery(checkCard)) {
                    result.getString("pin");
                } catch (Exception e) {
                    System.out.println("Such a card does not exist.");
                    con.close();
                    statement.close();
                    loggedMenu();
                }
                System.out.println("Enter how much money you want to transfer:");
                int sum = scanner.nextInt();
                if (sum > balance) {
                    System.out.println("Not enough money");
                    con.close();
                    statement.close();
                    loggedMenu();
                } else {
                    balance -= sum;
                    statement.executeUpdate("UPDATE CARD SET balance = " + balance +
                            " WHERE number = '" + cardNoString + "';");
                    statement.executeUpdate("UPDATE CARD SET balance = (balance + " + sum +
                            ") WHERE number = '" + transferTo + "';");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Success!");
        loggedMenu();
    }

    public static void closeAcc() {
        String text = "UPDATE CARD SET number = 'closed', pin = 'none' WHERE number = " + cardNoString + ";";
        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate(text);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("\nThe account has been closed!");
        menu();
    }

    public static void logIn() {
        System.out.println("Enter your card number:");
        String inputCard = scanner.nextLine();
        inputCard = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String inputPin = scanner.nextLine();

        try {
            for (int i = 0; i < 16; i++) {
                Integer.parseInt(String.valueOf(inputCard.charAt(i)));
            }
            for (int i = 0; i < 4; i++) {
                Integer.parseInt(String.valueOf(inputPin.charAt(i)));
            }
        } catch (Exception e) {
            System.out.println("Wrong card number or PIN!");
            menu();
        }

        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                try (ResultSet search = statement.executeQuery("SELECT pin, balance FROM CARD WHERE number = " + inputCard + ";")) {

                    if (inputPin.equals(search.getString("pin"))) {
                        cardNoString = inputCard;
                        pinString = inputPin;
                        balance = search.getInt("balance");
                        System.out.println("\nYou have successfully logged in!");
                        con.close();
                        statement.close();
                        search.close();
                        loggedMenu();
                    } else {
                        System.out.println("Wrong card number or PIN!");
                        con.close();
                        statement.close();
                        search.close();
                        menu();
                    }
                } catch (SQLException e) {
                    System.out.println("Wrong card number or PIN!");
                    con.close();
                    statement.close();

                    menu();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void luhn() {
        int[] arr = Arrays.copyOfRange(cardNo, 0, 15);
        for (int i = 0; i < 15; i++) {
            arr[i] *= 2;
            i++;
        }
        for (int i = 0; i < 15; i++) {
            if (arr[i] > 9) {
                arr[i] -= 9;
            }
        }
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            sum += arr[i];
        }
        if (sum % 10 == 0) {
            cardNo[15] = 0;
        } else {
            cardNo[15] = 10 - (sum % 10);
        }
    }
}
