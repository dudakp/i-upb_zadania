/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.dudak.fei.upb;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import sk.dudak.fei.upb.model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.JOptionPane;

public class Database {


    final static class MyResult {
        private final boolean first;
        private final String second;

        public MyResult(boolean first, String second) {
            this.first = first;
            this.second = second;
        }

        public boolean getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }
    }

    protected static MyResult add(User u) throws IOException {
        if (exist(u.getName())) {
            return new MyResult(false, "Meno uz existuje");
        }
        saveUser(u);
        return new MyResult(true, "");
    }

    protected static MyResult find(String text) throws IOException {
        User user = findByName(text);
        if (user == null) {
            return new MyResult(false, "Meno sa nenaslo");
        }
        return new MyResult(true, "");
    }

    protected static boolean exist(String name) {
        return findByName(name) != null;
    }

    // moje pridane

    public static User findByName(String name) {
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.openSession();
        return ((User) executeInTransaction(session, () -> session.createQuery("FROM User u WHERE u.name = ?1").setParameter(1, name).uniqueResult()));
    }

    public static void saveUser(User u) {
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.openSession();
        executeInTransaction(session, session::saveOrUpdate, u);
    }

    private static <T> T executeInTransaction(Session session, Supplier<T> f) {
        Transaction tx = null;
        T res = null;
        try (session) {
            tx = session.beginTransaction();
            res = f.get();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return res;
    }

    private static <T> void executeInTransaction(Session session, Consumer<T> f, T entity) {
        Transaction tx = null;
        try (session) {
            tx = session.beginTransaction();
            f.accept(entity);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}
