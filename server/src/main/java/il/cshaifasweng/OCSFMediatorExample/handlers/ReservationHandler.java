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
import java.time.format.DateTimeFormatter;
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
            // Fetch all tables for the branch
            List<RestaurantTable> allTables = session.createQuery(
                            "FROM RestaurantTable t WHERE t.branch.id = :branchId", RestaurantTable.class)
                    .setParameter("branchId", branch.getId())
                    .getResultList();

            // Loop over every table—do not skip any based solely on the reserved flag.
            for (RestaurantTable table : allTables) {
                boolean conflict = false;
                // Get all reservations for today that include this table.
                List<Reservation> reservations = session.createQuery(
                                "SELECT r FROM Reservation r JOIN r.tables t WHERE t.id = :tableId AND r.date = :date",
                                Reservation.class)
                        .setParameter("tableId", table.getId())
                        .setParameter("date", reservation.getDate())
                        .getResultList();
                // Define the client's requested time slot (assuming a 90-minute window).
                LocalDateTime reqStart = LocalDateTime.of(reservation.getDate(), reservation.getTime());
                LocalDateTime reqEnd = reqStart.plusMinutes(90);
                for (Reservation r : reservations) {
                    LocalDateTime existingStart = LocalDateTime.of(r.getDate(), r.getTime());
                    LocalDateTime existingEnd = existingStart.plusMinutes(90);
                    // Check for time overlap
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

        // Try to find the best combination of tables among matching ones;
        // if none, combine with other tables.
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
        if (branch == null) return "Branch not specified";

        LocalTime openTime = branch.getOpenHour().plusMinutes(15);
        if (reservation.getDate().equals(LocalDate.now()) && LocalTime.now().isAfter(openTime)) {
            openTime = LocalTime.now().plusMinutes(15 - (LocalTime.now().getMinute() % 15));
        }
        LocalTime closeTime = branch.getCloseHour().minusMinutes(15);
        LocalDate reservationDate = reservation.getDate();
        LocalDateTime requestedTime = LocalDateTime.of(reservationDate, reservation.getTime());

        List<LocalDateTime> availableTimes = new ArrayList<>();
        LocalDateTime slot = LocalDateTime.of(reservationDate, openTime);
        LocalDateTime end = LocalDateTime.of(reservationDate, closeTime);

        while (!slot.isAfter(end)) {
            Reservation testReservation = new Reservation(
                    branch, reservationDate, slot.toLocalTime(), reservation.getGuests(),
                    reservation.getSeatingArea(), reservation.getFullName(), reservation.getPhone(),
                    reservation.getEmail(), reservation.getCreditCard(), null
            );

            List<RestaurantTable> allocated = allocateTablesForReservation(testReservation, sessionFactory);

            if (allocated != null && !allocated.isEmpty()) {
                availableTimes.add(slot);
            }
            slot = slot.plusMinutes(15);
        }

        availableTimes.sort(Comparator.comparingLong(t -> Math.abs(Duration.between(requestedTime, t).toMinutes())));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return availableTimes.stream()
                .limit(4)
                .map(dt -> dt.toLocalTime().format(formatter))
                .collect(Collectors.joining(", "));
    }

    // Helper function that rounds a LocalTime up to the next quarter-hour.
// If the time is already exactly on a quarter (e.g. 13:00, 13:15, 13:30, 13:45),
// it returns the same time.
    private static LocalTime roundUpToNextQuarterHour(LocalTime time) {
        int minute = time.getMinute();
        int remainder = minute % 15;
        if (remainder == 0) {
            return time; // Already on a quarter-hour boundary.
        }
        int minutesToAdd = 15 - remainder;
        return time.plusMinutes(minutesToAdd).withSecond(0).withNano(0);
    }


}
