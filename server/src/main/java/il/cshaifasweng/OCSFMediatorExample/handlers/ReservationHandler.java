package il.cshaifasweng.OCSFMediatorExample.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.Reservation;
import il.cshaifasweng.OCSFMediatorExample.entities.Branch;
import il.cshaifasweng.OCSFMediatorExample.entities.RestaurantTable;
import il.cshaifasweng.OCSFMediatorExample.util.EmailSender;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
                    long minutesUntilReservation = Duration.between(LocalDateTime.now(), reservationDateTime).toMinutes();
                    if (minutesUntilReservation <= 60) {
                        feeApplied = true;
                    }
                }
                String feestr="" ;
                if (feeApplied ==true)
                {
                    feestr =  "Fee = 10$ \n\n";
                }
                else
                    feestr = "There is no fee \n\n" ;
                // Send email notification
                String emailSubject = "❌ Table Reservation Canceled - Mom's Kitchen";
                String emailBody = "Dear " + reservation.getFullName()+ ",\n\n"
                        + "Your table reservation at Mom's Kitchen has been successfully canceled.\n"
                        + "Reservation Details:\n"
                        + "📅 Date: " + reservation.getDate() + "\n"
                        + "⏰ Time: " + reservation.getTime() + "\n"
                        + "👥 Guests: " + reservation.getGuests() + "\n"
                        + feestr
                        + "We hope to serve you in the future. Let us know if you’d like to book again!\n\n"
                        + "Best regards,\n"
                        + "Mom's Kitchen Team";

                EmailSender.sendEmail(reservation.getEmail(), emailSubject, emailBody);
                System.out.println("📧 Reservation cancellation email sent to " + reservation.getEmail());


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
        List<RestaurantTable> matchingTables = new ArrayList<>();
        List<RestaurantTable> otherTables = new ArrayList<>();
        Branch branch = reservation.getBranch();
        String requestedSeating = reservation.getSeatingArea();

        try (Session session = sessionFactory.openSession()) {
            List<RestaurantTable> allTables = session.createQuery(
                            "FROM RestaurantTable t WHERE t.branch.id = :branchId", RestaurantTable.class)
                    .setParameter("branchId", branch.getId())
                    .getResultList();

            for (RestaurantTable table : allTables) {
                boolean conflict = false;
                List<Reservation> reservations = session.createQuery(
                                "SELECT r FROM Reservation r JOIN r.tables t WHERE t.id = :tableId AND r.date = :date", Reservation.class)
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
                    if (table.getSeatingArea().equalsIgnoreCase(requestedSeating)) {
                        matchingTables.add(table);
                    } else {
                        otherTables.add(table);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        List<RestaurantTable> allocated = findTableCombination(matchingTables, reservation.getGuests());
        if (allocated == null) {
            List<RestaurantTable> combined = new ArrayList<>();
            combined.addAll(matchingTables);
            combined.addAll(otherTables);
            allocated = findTableCombination(combined, reservation.getGuests());

        }
        return allocated;
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

/*
    public static List<RestaurantTable> saveReservation(Reservation reservation, SessionFactory sessionFactory) {
        List<RestaurantTable> allocatedTables = allocateTablesForReservation(reservation, sessionFactory);
        if (allocatedTables == null || allocatedTables.isEmpty()) {
            return null;
        }
        System.out.println("here");
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

            // Send email notification
            String emailSubject = "🍽️ Table Reservation Confirmation - Mom's Kitchen";
            String emailBody = "Dear " + reservation.getFullName()+ ",\n\n"
                    + "Your table reservation at Mom's Kitchen has been successfully booked.\n"
                    + "Reservation Details:\n"
                    + "📅 Date: " + reservation.getDate()+ "\n"
                    + "⏰ Time: " + reservation.getTime() + "\n"
                    + "👥 Guests: " + reservation.getGuests() + "\n"
                    + "📍 Location: Mom's Kitchen," + reservation.getBranch().getName() + "\n\n"
                    + "We look forward to serving you!\n\n"
                    + "Best regards,\n"
                    + "Mom's Kitchen Team";

            EmailSender.sendEmail(reservation.getEmail(), emailSubject, emailBody);

            System.out.println("📧 Reservation confirmation email sent to " + reservation.getEmail());

            reservation.setTables(allocatedTables);
            session.save(reservation);
            tx.commit();
            return allocatedTables;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
*/

    public static List<RestaurantTable> saveReservation(Reservation reservation, SessionFactory sessionFactory) {
        List<RestaurantTable> allocatedTables = allocateTablesForReservation(reservation, sessionFactory);
        if (allocatedTables == null || allocatedTables.isEmpty()) {
            return null;
        }
        System.out.println("here");
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            LocalDateTime reqDateTime = LocalDateTime.of(reservation.getDate(), reservation.getTime());
            LocalDateTime now = LocalDateTime.now();
            long delayMillis = Duration.between(now, reqDateTime).toMillis();

            for (RestaurantTable table : allocatedTables) {
                if (!reqDateTime.isAfter(now)) {
                    // Reserve the table right away if the reservation time is now or in the past
                    table.setReserved(true);
                    session.update(table);
                } else {
                    // Schedule the table reservation to be activated at the requested time
                    ReservationScheduler.scheduleReservationActivation(table.getId(), delayMillis, sessionFactory);
                }
            }

            // Send email notification
            String emailSubject = "🍽️ Table Reservation Confirmation - Mom's Kitchen";
            String emailBody = "Dear " + reservation.getFullName() + ",\n\n"
                    + "Your table reservation at Mom's Kitchen has been successfully booked.\n"
                    + "Reservation Details:\n"
                    + "📅 Date: " + reservation.getDate() + "\n"
                    + "⏰ Time: " + reservation.getTime() + "\n"
                    + "👥 Guests: " + reservation.getGuests() + "\n"
                    + "📍 Location: Mom's Kitchen," + reservation.getBranch().getName() + "\n\n"
                    + "We look forward to serving you!\n\n"
                    + "Best regards,\n"
                    + "Mom's Kitchen Team";

            EmailSender.sendEmail(reservation.getEmail(), emailSubject, emailBody);

            System.out.println("📧 Reservation confirmation email sent to " + reservation.getEmail());

            reservation.setTables(allocatedTables);
            session.save(reservation);
            tx.commit();
            return allocatedTables;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

public static String computeAlternativeTimes(Reservation reservation, SessionFactory sessionFactory) {
    Branch branch = reservation.getBranch();
    if (branch == null) {
        return "Branch not specified";
    }

    int maxCapacity = 25;
    int newGuests = reservation.getGuests();
    LocalTime openTime ;
    openTime = branch.getOpenHour().plusMinutes(15).isAfter( LocalTime.now()) ? branch.getOpenHour().plusMinutes(15) : LocalTime.now();
    LocalTime closeTime = branch.getCloseHour().minusMinutes(60); // Close 1 hour early

    LocalDate reservationDate = reservation.getDate();
    LocalDateTime requestedTime = LocalDateTime.of(reservationDate, reservation.getTime());

    List<LocalDateTime> timeSlots = new ArrayList<>();
    LocalDateTime slot = LocalDateTime.of(reservationDate, openTime);
    LocalDateTime end = LocalDateTime.of(reservationDate, closeTime);

    while (!slot.isAfter(end)) {
        timeSlots.add(slot);
        slot = slot.plusMinutes(15);
    }

    List<LocalDateTime> availableTimes = new ArrayList<>();

    try (Session session = sessionFactory.openSession()) {
        // Load all reservations for that day in one query (for performance)
        List<Reservation> sameDayReservations = session.createQuery(
                        "FROM Reservation r WHERE r.branch.id = :branchId AND r.date = :date", Reservation.class)
                .setParameter("branchId", branch.getId())
                .setParameter("date", reservationDate)
                .getResultList();

        for (LocalDateTime time : timeSlots) {
            LocalDateTime slotStart = time;
            LocalDateTime slotEnd = time.plusMinutes(90);

            int totalGuests = 0;

            for (Reservation r : sameDayReservations) {
                LocalDateTime resStart = LocalDateTime.of(r.getDate(), r.getTime());
                LocalDateTime resEnd = resStart.plusMinutes(90);

                if (slotStart.isBefore(resEnd) && resStart.isBefore(slotEnd)) {
                    totalGuests += r.getGuests();
                }
            }

            if (totalGuests + newGuests <= maxCapacity) {
                availableTimes.add(time);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "Error retrieving available times";
    }

    // Sort available times by closeness to requested time
    availableTimes.sort(Comparator.comparingLong(t -> Math.abs(Duration.between(requestedTime, t).toMinutes())));

    return availableTimes.stream()
            .limit(4)
            .map(dt -> dt.toLocalTime().toString())
            .collect(Collectors.joining(", "));
}


}
