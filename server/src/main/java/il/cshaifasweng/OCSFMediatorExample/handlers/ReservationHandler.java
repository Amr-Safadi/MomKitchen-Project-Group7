package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class ReservationHandler {

    public static List<Reservation> getReservationsByUser(String fullName, String phone, SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Reservation r WHERE r.fullName = :fullName AND r.phone = :phone", Reservation.class)
                    .setParameter("fullName", fullName)
                    .setParameter("phone", phone)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean cancelReservation(Reservation reservation, SessionFactory sessionFactory) {
        boolean feeApplied = false;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Reservation res = session.get(Reservation.class, reservation.getId());
            if (res != null) {
                LocalDateTime reservationDateTime = LocalDateTime.of(res.getDate(), res.getTime());
                if (!LocalDateTime.now().isAfter(reservationDateTime)) {
                    long minutesUntilReservation = java.time.Duration.between(LocalDateTime.now(), reservationDateTime).toMinutes();
                    if (minutesUntilReservation <= 60) {
                        feeApplied = true;
                    }
                }
                session.delete(res);
                tx.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return feeApplied;
    }

    public static List<RestaurantTable> allocateTablesForReservation(Reservation reservation, SessionFactory sessionFactory) {
        List<RestaurantTable> availableTables = new ArrayList<>();
        Branch branch = reservation.getBranch();

        try (Session session = sessionFactory.openSession()) {
            List<RestaurantTable> allTables = session.createQuery(
                            "FROM RestaurantTable t WHERE t.branch.id = :branchId", RestaurantTable.class)
                    .setParameter("branchId", branch.getId())
                    .getResultList();

            for (RestaurantTable table : allTables) {
                boolean conflict = false;
                List<Reservation> reservations = session.createQuery(
                                "FROM Reservation r WHERE r.table.id = :tableId AND r.date = :date", Reservation.class)
                        .setParameter("tableId", table.getId())
                        .setParameter("date", reservation.getDate())
                        .getResultList();
                LocalDateTime reqStart = LocalDateTime.of(reservation.getDate(), reservation.getTime());
                LocalDateTime reqEnd = reqStart.plusMinutes(90);
                for (Reservation r : reservations) {
                    LocalDateTime existingStart = LocalDateTime.of(r.getDate(), r.getTime());
                    LocalDateTime existingEnd = existingStart.plusMinutes(90);
                    if (reqStart.isBefore(existingEnd) && existingStart.isBefore(reqEnd)) {
                        conflict = true;
                        break;
                    }
                }
                if (!conflict) {
                    availableTables.add(table);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (availableTables.isEmpty()) {
            return null;
        }

        int guestCount = reservation.getGuests();
        int maxCapacity = availableTables.stream().mapToInt(RestaurantTable::getCapacity).max().orElse(0);

        if (guestCount > maxCapacity) {
            return findTableCombination(availableTables, guestCount);
        } else {
            RestaurantTable bestSingle = availableTables.stream()
                    .filter(t -> t.getCapacity() >= guestCount)
                    .min(Comparator.comparingInt(RestaurantTable::getCapacity))
                    .orElse(null);

            List<RestaurantTable> merged = findTableCombination(availableTables, guestCount);

            if (merged != null && merged.size() > 1) {
                return merged;
            } else {
                return Collections.singletonList(bestSingle);
            }
        }
    }

    private static List<RestaurantTable> findTableCombination(List<RestaurantTable> tables, int guests) {
        List<RestaurantTable> bestCombination = null;
        int bestCount = Integer.MAX_VALUE;
        int bestTotalCapacity = Integer.MAX_VALUE;
        int n = tables.size();
        for (int mask = 1; mask < (1 << n); mask++) {
            List<RestaurantTable> combination = new ArrayList<>();
            int totalCapacity = 0;
            int count = 0;
            for (int j = 0; j < n; j++) {
                if ((mask & (1 << j)) != 0) {
                    RestaurantTable table = tables.get(j);
                    combination.add(table);
                    totalCapacity += table.getCapacity();
                    count++;
                }
            }
            if (totalCapacity >= guests) {
                if (count < bestCount || (count == bestCount && totalCapacity < bestTotalCapacity)) {
                    bestCombination = combination;
                    bestCount = count;
                    bestTotalCapacity = totalCapacity;
                }
            }
        }
        return bestCombination;
    }

    public static List<RestaurantTable> saveReservation(Reservation reservation, SessionFactory sessionFactory) {
        List<RestaurantTable> allocatedTables = allocateTablesForReservation(reservation, sessionFactory);
        if (allocatedTables == null || allocatedTables.isEmpty()) {
            return null;
        }

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            LocalDateTime reqDateTime = LocalDateTime.of(reservation.getDate(), reservation.getTime());
            LocalDateTime now = LocalDateTime.now();
            for (RestaurantTable table : allocatedTables) {
                if (!reqDateTime.isAfter(now)) {
                    table.setReserved(true);
                    session.update(table);
                } else {
                    long delayMillis = Duration.between(now, reqDateTime).toMillis();
                    ReservationScheduler.scheduleReservationActivation(table.getId(), delayMillis, sessionFactory);
                }
            }

            reservation.setTable(allocatedTables.get(0));
            session.save(reservation);
            tx.commit();
            return allocatedTables;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String computeAlternativeTimes(Reservation reservation) {
        Branch branch = reservation.getBranch();
        if (branch == null) {
            return "Branch not specified";
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime branchOpenDT = LocalDateTime.of(now.toLocalDate(), branch.getOpenHour()).plusMinutes(15);
        LocalDateTime branchCloseDT = LocalDateTime.of(now.toLocalDate(), branch.getCloseHour()).minusMinutes(60);
        List<String> alternatives = new ArrayList<>();
        LocalDateTime slot = branchOpenDT;
        while (!slot.isAfter(branchCloseDT)) {
            alternatives.add(slot.toLocalTime().toString());
            slot = slot.plusMinutes(15);
        }
        return String.join(", ", alternatives);
    }
}
