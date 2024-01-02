package SpringChat.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Users") // because a table with this class name may exist (reserved names)
public class UserModel extends BaseModel implements Comparable<UserModel> {

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    public void setUsername(String username) {
        this.username = username.toLowerCase().trim();
    }

    @Column(length = 100)
    private String password;

    @Column(length = 100)
    private String firstname;

    @Column(length = 100)
    private String lastname;

    @Transient
    private Boolean rememberMe;

    public static String validateFirstname(String s) {
        int lb = 1, ub = 90;
        if (s.length() < lb || s.length() > ub) {
            return "Firstname must be at least " + lb + " at most " + ub + " characters";
        } else {
            if (hasIllegalCharacters(s)) {
                return "Firstname includes illegal characters";
            }
            return null;
        }
    }

    public static String validateLastname(String s) {
        int lb = 1, ub = 90;
        if (s.length() < lb || s.length() > ub) {
            return "Lastname must be at least " + lb + " at most " + ub + " characters";
        } else {
            if (hasIllegalCharacters(s)) {
                return "Lastname includes illegal characters";
            }
            return null;
        }
    }

    public static String validateUsername(String s) {
        int lb = 1, ub = 90;
        if (s.length() < lb || s.length() > ub) {
            return "Username must be at least " + lb + " at most " + ub + " characters";
        } else {
            if (hasIllegalCharacters(s)) {
                return "Username includes illegal characters";
            }
            return null;
        }
    }

    public static String validatePassword(String s) {
        int lb = 6, ub = 90;
        if (s.length() < lb || s.length() > ub) {
            return "Passwords must be at least " + lb + " at most " + ub + " characters";
        } else {
            if (hasIllegalCharacters(s)) {
                return "Password includes illegal characters";
            }
            return null;
        }
    }

    public static boolean hasIllegalCharacters(String s) {
        boolean ok = true;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || (ch >= 32 && ch <= 46) || (ch == 64))) {
                ok = false;
                break;
            }
        }
        return !ok;
    }

    public static String validateAll(UserModel userModel) {
        String problem = UserModel.validateFirstname(userModel.getFirstname());
        if (problem == null) {
            problem = UserModel.validateLastname(userModel.getLastname());
        }
        if (problem == null) {
            problem = UserModel.validateUsername(userModel.getUsername());
        }
        if (problem == null) {
            problem = UserModel.validatePassword(userModel.getPassword());
        }
        return problem;
    }

    @Override
    public int compareTo(UserModel userModel) {
        return getPresentation().compareTo(userModel.getPresentation());
    }

    public String getPresentation() {
        return firstname + " " + lastname;
    }
}
