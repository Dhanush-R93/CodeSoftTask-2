import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.sql.*;

class AtmUser {
    static int balance;
    static int pinNo;
    static int atmNumber;
    static int takeAmount = 0;
    static int addAmount = 0;
    static final int minimumBalance = 500;
    static String name;

    AtmUser(int pinNo, int balance, String name, int atmNumber) {
        AtmUser.atmNumber = atmNumber;
        AtmUser.name = name;
        AtmUser.pinNo = pinNo;
        AtmUser.balance = balance;
    }
}

public class ATM {
    public static void main(String[] args) {
        try {
            Scanner sc=new Scanner(System.in);
            System.out.println("Enter 1 for Create new Account");
            System.out.println("Enter 2 for Transactions");
            int choice=sc.nextInt();
            if(choice==1){
                insertData();
            }
            else if (choice==2){
               program();
            }
            else {
                System.out.println("Invalid input");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    static void insertData() throws SQLException {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "Dhanush");
             Statement st = con.createStatement()) {
            Scanner input = new Scanner(System.in);
            System.out.println("Create Your ATM Number");
            int acc_no = input.nextInt();
            if(acc_no<10000){
                System.out.println("Create  5 digits Number");
                System.out.println("Do you want to create again? yes or no");
                String choice=input.next();
                if (choice.equalsIgnoreCase("yes")) {
                    insertData();
                }
                else if (choice.equalsIgnoreCase("no")) {
                    System.out.println("Thank you !");
                    return;
                } else {
                    System.out.println("Invalid input.");
                    return;
                }
            }
            System.out.println("Create Strong Password");
            int password = input.nextInt();
            if(password<1000){
                System.out.println("Create  4 digits Number Password");
                return;
            }
            PreparedStatement checkStmt = con.prepareStatement("SELECT * FROM customers WHERE atm_no = ?");
            checkStmt.setInt(1, acc_no);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("ATM Number already exists. Create using a different number.");
                System.out.println("Do you want to try again? yes or no");
                String choice = input.next();
                if (choice.equalsIgnoreCase("yes")) {
                    insertData();
                } else if (choice.equalsIgnoreCase("no")) {
                    System.out.println("Thank you !");
                    return;
                } else {
                    System.out.println("Invalid input.");
                    return;
                }
            } else {
                System.out.println("Enter your Name");
                String name = input.next();

                PreparedStatement pst = con.prepareStatement("INSERT INTO customers (atm_no,acc_name, balance, pw) VALUES (?, ?, ?, ?)");
                pst.setInt(1, acc_no);
                pst.setString(2, name);
                pst.setInt(3, 500);  // default balance
                pst.setInt(4, password);

                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Successfully Created an Account in DBS ATM.");
                } else {
                    System.out.println("Failed to create account. Please try again.");
                }
            }
        } catch (InputMismatchException e) {
            System.err.println("Invalid input. Please enter the correct details.");
        }
    }


    static void program() throws SQLException {
        Scanner in = new Scanner(System.in);

        System.out.println("Enter your ATM Number:");
        int atmNumber = in.nextInt();

        System.out.println("Enter your Secret PIN:");
        int secretPin = in.nextInt();

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "Dhanush");
             PreparedStatement pst = con.prepareStatement("SELECT * FROM customers WHERE atm_no = ? AND pw = ?")) {

            pst.setInt(1, atmNumber);
            pst.setInt(2, secretPin);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int sp = rs.getInt(4);
                    int bal = rs.getInt(3);
                    String accName = rs.getString(2);
                    int an = rs.getInt(1);

                    AtmUser user = new AtmUser(secretPin, bal, accName, atmNumber);
                    display(atmNumber, secretPin);
                } else {
                    promptRetry();
                }
            }
        } catch (InputMismatchException e) {
            System.err.println("WRONG INPUT. PLEASE ENTER YOUR SECRET NUMBER ONLY");
        }
    }



    static void promptRetry() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("WRONG ATM NUMBER OR PIN");
        System.out.println("Do you want to try again? Type 'yes' or 'no'");
        String userInput = input.next();

        if (userInput.equalsIgnoreCase("yes")) {
            program();
        } else if (userInput.equalsIgnoreCase("no")) {
            System.out.println("THANK YOU FOR USING IOB BANK ATM.");
        } else {
            System.out.println("Invalid input");
        }
    }

    static void display(int atmNumber, int secretPin) throws SQLException {
        if (AtmUser.pinNo == secretPin) {
            Scanner input = new Scanner(System.in);
            Date date = new Date();
            int count = 0;

            System.out.println("\nLOGIN SUCCESSFUL.");
            while (true) {
                System.out.println();
                System.out.println("Enter 1 to check your Bank Balance");
                System.out.println("Enter 2 to add amount to your account");
                System.out.println("Enter 3 to withdraw amount from your account");
                System.out.println("Enter 4 to print receipt");
                System.out.println("Enter 5 to Change the Current password");
                System.out.println("Enter 6 to Past Transaction list");
                System.out.println("Enter 7 for Money Transfer to another Account in DBS ATM");
                System.out.println("Enter 8 to Exit");

                int option = input.nextInt();

                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "Dhanush");
                     Statement st = con.createStatement()) {

                    switch (option) {
                        case 1:
                            System.out.println("YOUR CURRENT BANK BALANCE IS " + AtmUser.balance+" Rs");
                            break;
                        case 2:
                            System.out.println("How much amount do you want to add to your account?");
                            AtmUser.addAmount = input.nextInt();
                            AtmUser.balance += AtmUser.addAmount;
                            String updateBalanceQuery = "UPDATE customers SET balance = ? WHERE atm_no = ?";
                            try (PreparedStatement updateStmt = con.prepareStatement(updateBalanceQuery)) {
                                updateStmt.setInt(1, AtmUser.balance);
                                updateStmt.setInt(2, atmNumber);
                                int rowsUpdated = updateStmt.executeUpdate();
                                if (rowsUpdated > 0) {
                                    System.out.println("Successfully added " + AtmUser.addAmount + " rupees to your bank account.");
                                } else {
                                    System.out.println("Failed to update the balance. Please try again.");
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            break;

                        case 3:
                            System.out.println("Enter the amount to withdraw:");
                            AtmUser.takeAmount = input.nextInt();

                            if (AtmUser.takeAmount > AtmUser.balance) {
                                System.out.println("Not enough amount. Your current balance is " + AtmUser.balance);
                                AtmUser.takeAmount=0;
                            } else if (AtmUser.balance - AtmUser.takeAmount < AtmUser.minimumBalance) {
                                System.out.println("Cannot withdrawl amount. Minimum balance is" + AtmUser.minimumBalance + " must be maintained.");
                                AtmUser.takeAmount=0;
                            } else {
                                AtmUser.balance -= AtmUser.takeAmount;
                                st.executeUpdate("UPDATE customers SET balance = " + AtmUser.balance + " WHERE atm_no = " + atmNumber);
                                System.out.println("Successfully withdrew " + AtmUser.takeAmount + " rupees from your bank account.");
                            }
                            break;
                        case 4:
                            printReceipt(atmNumber, date, count);
                            count++;
                            break;
                        case 5:
                            System.out.println("Enter Strong password");
                            int newPassWord=input.nextInt();
                            if(newPassWord>1000){
                                st.executeUpdate("UPDATE customers SET pw = " + newPassWord + " WHERE atm_no = " + atmNumber + ";");
                                System.out.println("Successfully Updated New Password ");
                            }
                            else {
                                System.out.println("New Password Cannot update ");
                                System.out.println("Create Strong 4 digit Password");
                            }
                            break;
                        case 6:
                            System.out.println();
                            System.out.println("Transactions list");
                            System.out.println("_________________");
                            ResultSet rs=st.executeQuery("Select * from CustomerAmountDetails where Atm_no="+atmNumber+";");
                            while(rs.next()){
                                System.out.println("ATM Number          : "+rs.getInt(1));
                                System.out.println("Before Transactions : "+rs.getInt(2)+" Rs");
                                System.out.println("After Transactions  : "+rs.getInt(3)+" Rs");
                                System.out.println("Time                : "+rs.getString(4));
                                System.out.println("Date                : "+rs.getString(5));
                                System.out.println();
                                System.out.println();
                            }
                            break;
                        case 7:
                            System.out.println("Amount transfer to another account");
                            System.out.println("Enter Atm Number");
                            int atmno = input.nextInt();
                            ResultSet rs1 = st.executeQuery("select * from customers;");
                            boolean matchFound = false;
                            while (rs1.next()) {
                                int Num = rs1.getInt(1);
                                int HisAmount = rs1.getInt(3);
                                if (atmno == Num && atmno!=AtmUser.atmNumber) {
                                    matchFound = true;
                                    System.out.println("How much amount will be want to transfer?");
                                    int Amount = input.nextInt();
                                    if (Amount > AtmUser.balance) {
                                        System.out.println("Not enough amount. Your current balance is " + AtmUser.balance);
                                        AtmUser.takeAmount = 0;
                                    } else if (AtmUser.balance - Amount < AtmUser.minimumBalance) {
                                        System.out.println("Cannot transfer amount. Minimum balance is " + AtmUser.minimumBalance + " must be maintained.");
                                        AtmUser.takeAmount = 0;
                                    } else {
                                        String query = "UPDATE customers SET balance = ? WHERE atm_no = ?";
                                        try (PreparedStatement pstmt = con.prepareStatement(query)) {
                                            int amt = Amount + HisAmount;
                                            pstmt.setInt(1, amt);
                                            pstmt.setInt(2, Num);
                                            int rows = pstmt.executeUpdate();
                                            System.out.println("Successfully transfered "+Amount);
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }

                                        AtmUser.balance -= Amount;
                                        String query1 = "UPDATE customers SET balance = " + AtmUser.balance + " WHERE atm_no = " + atmNumber + ";";
                                        try (Statement st2 = con.createStatement()) {
                                            int rows = st2.executeUpdate(query1);
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            if (!matchFound) {
                                if(atmno==AtmUser.atmNumber){
                                    System.out.println("Entered Number is your Number,So cannot transfer ");
                                }
                                else {
                                    System.out.println("This ATM Number Not available in DBS ATM");
                                }
                            }
                            rs1.close();
                            break;
                        case 8:
                            System.out.println("THANK YOU ");
                            return;
                        default:
                            System.out.println("Invalid option. Please try again.");
                    }
                }
            }
        } else {
            System.out.println("Your ATM Number and PIN Number is incorrect. Please enter correctly.");
        }
    }

    static void printReceipt(int atmNumber, Date date, int count) {
        System.out.println("_______________________");
        System.out.println("|      DBS ATM ");
        System.out.println("| DATE     : " + date.getDate() + "/" + (date.getMonth() + 1) + "/" + (date.getYear() + 1900));
        System.out.println("| TIME     : " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
        System.out.println("| ATM ID   : 07510843");
        System.out.println("| TRANX NO : " + (count + 1));
        System.out.println("| CARD NO  : XXXXXXXXXXXX" + atmNumber);
        System.out.println("| SAVING   : A/C XXXXXXXX ");
        System.out.println("| NAME     : " + AtmUser.name);
        System.out.println("| YOUR BANK BALANCE IS      : " + AtmUser.balance);
        System.out.println("| NOW ADDED AMOUNT IS       : " + AtmUser.addAmount);
        System.out.println("| NOW WITHDRAWAL AMOUNT IS  : " + AtmUser.takeAmount);
        System.out.println("|           THANK YOU");
        System.out.println("|   FOR USING DHANUSH BANK ATM");
        System.out.println("|________________________");
    }
}
