//////////////////////////////////////////////////////////////////////////
// TODO:                                                                //
// Uloha1: Vytvorit funkciu na bezpecne generovanie saltu.              //
// Uloha2: Vytvorit funkciu na hashovanie.                              //
// Je vhodne vytvorit aj dalsie pomocne funkcie napr. na porovnavanie   //
// hesla ulozeneho v databaze so zadanym heslom.                        //
//////////////////////////////////////////////////////////////////////////
package sk.dudak.fei.upb;


import org.passay.CharacterCharacteristicsRule;
import org.passay.CharacterRule;
import org.passay.DictionaryRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.dictionary.WordListDictionary;
import org.passay.dictionary.WordLists;
import org.passay.dictionary.sort.ArraysSort;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class Security {

    public static byte[] hash(String password, byte[] salt) throws Exception {
        /*
         *   Pred samotnym hashovanim si najskor musite ulozit instanciu hashovacieho algoritmu.
         *   Hash sa uklada ako bitovy retazec, takze ho nasledne treba skonvertovat na String (napr. cez BigInteger);
         */
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }

    protected static byte[] getSalt() {
        /*
         *   Salt treba generovat cez secure funkciu.
         */
        SecureRandom random = new SecureRandom();
        byte[] res = new byte[16];
        random.nextBytes(res);
        return res;
    }

    public static RuleResult validatePassword(String password) {
        PasswordData passwordData = new PasswordData(password);
        CharacterRule[] characterRules = {
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1)
        };
        WordListDictionary wordList = null;
        try {
            wordList = new WordListDictionary(WordLists.createFromReader(
                    new FileReader[]{new FileReader("daj tu svoj path ku wordlistu")},
                    false,
                    new ArraysSort()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PasswordValidator validator = new PasswordValidator(
                new CharacterCharacteristicsRule(
                        characterRules.length,
                        characterRules
                ),
                new DictionaryRule(wordList));
        RuleResult res = validator.validate(passwordData);
        return res;
    }
}

