import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {

        String adminPass = "admin123";
        String recPass = "recep123";
        String dentPass = "dentis123";

        String adminHash = BCrypt.hashpw(adminPass, BCrypt.gensalt());
        String recHash = BCrypt.hashpw(recPass, BCrypt.gensalt());
        String dentHash = BCrypt.hashpw(dentPass, BCrypt.gensalt());

        System.out.println("========== COPY THESE ==========");
        System.out.println("Admin: " + adminHash);
        System.out.println("Receptionist: " + recHash);
        System.out.println("Dentist: " + dentHash);
        System.out.println("=================================");
        System.out.println("Passwords:");
        System.out.println("Admin: " + adminPass);
        System.out.println("Receptionist: " + recPass);
        System.out.println("Dentist: " + dentPass);
    }
}