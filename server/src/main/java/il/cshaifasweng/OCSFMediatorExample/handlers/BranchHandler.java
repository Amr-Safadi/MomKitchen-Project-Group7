package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class BranchHandler {
    public static Branch getBranchByName(String branchName, SessionFactory sessionFactory) {
        Branch branch = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            branch = session.createQuery("FROM Branch WHERE name = :branchName", Branch.class)
                    .setParameter("branchName", branchName)
                    .uniqueResult();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return branch;
    }
}
