package com.loansystem.service;

import com.loansystem.exception.ApplicationNotFoundException;
import com.loansystem.exception.LoanNotEligibleException;
import com.loansystem.model.Applicant;
import com.loansystem.model.ApplicationStatus;
import com.loansystem.model.Loan;
import com.loansystem.model.LoanApplication;
import com.loansystem.util.AppLogger;
import com.loansystem.util.StringUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoanService {

    public interface ProcessingListener {
        void onStatusChanged(LoanApplication application);
    }

    private final Logger log = AppLogger.get();
    private final Map<String, LoanApplication> applications = new LinkedHashMap<>();
    private final PriorityBlockingQueue<LoanApplication> queue = new PriorityBlockingQueue<>();
    private final List<ProcessingListener> listeners = new CopyOnWriteArrayList<>();
    private final List<Thread> workers = new ArrayList<>();
    private final FileManager fileManager;
    private final Random random = new Random();
    private int idCounter = 1000;
    private volatile boolean running = true;

    public LoanService(FileManager fileManager) {
        this.fileManager = fileManager;
        loadSavedData();
        startWorkers(2);
    }

    private void loadSavedData() {
        try {
            for (LoanApplication app : fileManager.load()) {
                applications.put(app.getAppId(), app);
                int number = Integer.parseInt(app.getAppId().substring(2));
                idCounter = Math.max(idCounter, number);
                if (app.getStatus() == ApplicationStatus.SUBMITTED
                        || app.getStatus() == ApplicationStatus.UNDER_REVIEW) {
                    queue.add(app);                     
                }
            }
        } catch (IOException | NumberFormatException e) {
            log.log(Level.SEVERE, "Could not load saved applications", e);
        }
    }

    private void startWorkers(int count) {
        for (int i = 1; i <= count; i++) {
            Thread worker = new Thread(new Worker(), "LoanWorker-" + i);
            worker.setDaemon(true);
            workers.add(worker);
            worker.start();
        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            while (running) {
                try {
                    LoanApplication app = queue.take();
                    process(app);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void process(LoanApplication app) throws InterruptedException {
        log.info("Processing " + app.getAppId() + " (" + app.getApplicant().getName() + ")");
        app.setStatus(ApplicationStatus.UNDER_REVIEW, "Verifying documents and credit history...");
        fire(app);
        Thread.sleep(2500 + random.nextInt(1500));           

        try {
            app.getLoan().checkEligibility(app.getApplicant());
            Loan loan = app.getLoan();
            Applicant person = app.getApplicant();
            app.setStatus(ApplicationStatus.APPROVED, String.format("Approved at %.2f%% p.a. | EMI %s | Total payable %s",
                    loan.getInterestRate(person), StringUtil.money(loan.calculateEMI(person)),
                    StringUtil.money(loan.totalPayable(person))));
            log.info(app.getAppId() + " APPROVED");
        } catch (LoanNotEligibleException e) {
            app.setStatus(ApplicationStatus.REJECTED, String.join("; ", e.getReasons()));
            log.warning(app.getAppId() + " REJECTED: " + e.getMessage());
        } catch (RuntimeException e) {
            app.setStatus(ApplicationStatus.REJECTED, "Unexpected error during processing");
            log.log(Level.SEVERE, "Unexpected error for " + app.getAppId(), e);
        }
        save();
        fire(app);
    }

    public LoanApplication submit(Applicant applicant, Loan loan) {
        LoanApplication app;
        synchronized (this) {
            String id;
            do {                                             
                idCounter++;
                id = "LN" + idCounter;
            } while (applications.containsKey(id));
            app = new LoanApplication(id, applicant, loan);
            applications.put(id, app);
        }
        queue.add(app);
        log.info("Application " + app.getAppId() + " submitted by " + applicant.getName());
        save();
        fire(app);
        return app;
    }

    public synchronized LoanApplication find(String appId) throws ApplicationNotFoundException {
        LoanApplication app = applications.get(appId);
        if (app == null) {
            throw new ApplicationNotFoundException(appId);
        }
        return app;
    }

    public synchronized List<LoanApplication> getAll() {
        return new ArrayList<>(applications.values());
    }

    public synchronized Map<ApplicationStatus, Integer> countByStatus() {
        Map<ApplicationStatus, Integer> counts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus s : ApplicationStatus.values()) {
            counts.put(s, 0);
        }
        for (LoanApplication app : applications.values()) {
            counts.merge(app.getStatus(), 1, Integer::sum);
        }
        return counts;
    }

    public synchronized double totalApprovedAmount() {
        double total = 0;
        for (LoanApplication app : applications.values()) {
            if (app.getStatus() == ApplicationStatus.APPROVED) {
                total += app.getLoan().getPrincipal();
            }
        }
        return total;
    }

    public static List<double[]> buildSchedule(double principal, double annualRate, int months) {
        List<double[]> rows = new ArrayList<>();
        double emi = Loan.calculateEMI(principal, annualRate, months);
        double monthlyRate = annualRate / 12 / 100;
        double balance = principal;
        for (int month = 1; month <= months; month++) {
            double interest = balance * monthlyRate;
            double principalPart = emi - interest;
            balance = Math.max(0, balance - principalPart);
            rows.add(new double[]{month, emi, interest, principalPart, balance});
        }
        return rows;
    }

    public void addListener(ProcessingListener listener) {
        listeners.add(listener);
    }

    private void fire(LoanApplication app) {
        for (ProcessingListener l : listeners) {
            l.onStatusChanged(app);
        }
    }

    public synchronized void save() {
        try {
            fileManager.save(applications.values());
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not save applications", e);
        }
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void shutdown() {
        running = false;
        for (Thread t : workers) {
            t.interrupt();
        }
        save();
        log.info("Service shut down");
    }
}
