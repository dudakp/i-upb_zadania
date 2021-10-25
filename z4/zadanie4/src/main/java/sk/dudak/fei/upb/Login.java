//////////////////////////////////////////////////////////////////////////
// TODO:                                                                //
// Uloha2: Upravte funkciu na prihlasovanie tak, aby porovnavala        //
//         heslo ulozene v databaze s heslom od uzivatela po            //
//         potrebnych upravach.                                         //
// Uloha3: Vlozte do prihlasovania nejaku formu oneskorenia.            //
//////////////////////////////////////////////////////////////////////////
package sk.dudak.fei.upb;

import sk.dudak.fei.upb.model.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class Login {
    protected static Database.MyResult prihlasovanie(String meno, String heslo) throws IOException, Exception {
        /*
         *   Delay je vhodne vytvorit este pred kontolou prihlasovacieho mena.
         */
        Database.MyResult account = Database.find(meno);
        User user = Database.findByName(meno);
        if (!account.getFirst()) {
            return new Database.MyResult(false, "Nespravne meno.");
        } else {
            LocalDateTime loginLockedTo = user.getLoginLockedTo();
            if (loginLockedTo != null && LocalDateTime.now().isBefore(loginLockedTo)) {
                return new Database.MyResult(false, "Dalsie prihlasenie bude mozne az o: " + loginLockedTo.format(DateTimeFormatter.RFC_1123_DATE_TIME));
            }
            byte[] hash = Security.hash(heslo, Base64.getDecoder().decode(user.getSalt()));
            String calculatedPwdHash = Base64.getEncoder().encodeToString(hash);
            boolean rightPassword = user.getPwdHash().equals(calculatedPwdHash);
            if (!rightPassword) {
                Integer failedAttempts = user.getFailedAttempts();
                if (failedAttempts != null) {
                    user.setFailedAttempts(failedAttempts + 1);
                } else {
                    user.setFailedAttempts(1);
                }
                if (failedAttempts != null && failedAttempts >= 3) {
                    user.setLoginLockedTo(LocalDateTime.now().plusMinutes(3));
                }
                Database.saveUser(user);
                return new Database.MyResult(false, "Nespravne heslo.");
            }
        }
        user.setLoginLockedTo(null);
        user.setFailedAttempts(null);
        Database.saveUser(user);
        return new Database.MyResult(true, "Uspesne prihlasenie.");
    }
}
