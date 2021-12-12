//////////////////////////////////////////////////////////////////////////
// TODO:                                                                //
// Uloha1: Do suboru s heslami ulozit aj sal.                           //
// Uloha2: Pouzit vytvorenu funkciu na hashovanie a ulozit heslo        //
//         v zahashovanom tvare.                                        //
//////////////////////////////////////////////////////////////////////////
package sk.dudak.fei.upb;

import org.passay.RuleResult;
import org.passay.RuleResultDetail;
import sk.dudak.fei.upb.model.User;

import java.util.Base64;
import java.util.stream.Collectors;


public class Registration {
    protected static Database.MyResult registracia(String meno, String heslo) throws Exception {
        if (Database.exist(meno)) {
            System.out.println("Meno je uz zabrate.");
            return new Database.MyResult(false, "Meno je uz zabrate.");
        } else {
            RuleResult validation = Security.validatePassword(heslo);
            if (!validation.isValid()) {
                String c = validation.getDetails().stream().map(RuleResultDetail::getErrorCode).collect(Collectors.joining("\n"));
                return new Database.MyResult(false, c);
            }
            byte[] salt = Security.getSalt();
            byte[] hash = Security.hash(heslo, salt);

            User user = new User();
            user.setName(meno);
            user.setSalt(Base64.getEncoder().encodeToString(salt));
            user.setPwdHash(Base64.getEncoder().encodeToString(hash));
            Database.add(user);
        }
        return new Database.MyResult(true, "");
    }

}
