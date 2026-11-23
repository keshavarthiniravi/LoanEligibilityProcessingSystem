LOAN ELIGIBILITY AND PROCESSING SYSTEM  (22IT321 - Object Oriented Programming, Project 3)
=========================================================================================

REQUIREMENTS : JDK 17 or newer (JDK 21 recommended). Check with:  java -version  and  javac -version

HOW TO RUN
  Windows : double-click run.bat            (or run it in a terminal)
  Mac/Linux: chmod +x run.sh && ./run.sh
  Manual  : javac -d out src/com/loansystem/*.java src/com/loansystem/*/*.java
            java -cp out com.loansystem.Main
  VS Code : open this folder, run src/com/loansystem/Main.java
  JAR     : java -jar LoanSystem.jar   (prebuilt, same code)

SCREENS (use these for your report screenshots)
  1. Dashboard        - stat tiles, bar chart, recent list, "Load Sample Data" button
  2. New Application  - form + live EMI preview, "Check Eligibility", "Submit Application"
  3. Applications     - table, search, status filter, details panel, EMI schedule button
  4. EMI Calculator   - amortisation schedule (month-by-month)
  5. Reports          - Generate Report / Export to File (saved in reports/)
  Also capture: validation error dialog, not-eligible dialog, custom exception messages.

TIP: click "Load Sample Data" on the Dashboard, then watch statuses change
     Submitted -> Under Review -> Approved/Rejected (background threads, ~3-5 sec each).

FILES CREATED AT RUN TIME
  data/applications.csv   - saved applications (reloaded on next start)
  logs/loan_system.log    - log file
  reports/*.txt           - exported reports

WEEK-WISE TOPIC MAP (where each topic is used)
  1  Looping constructs   LoanService.buildSchedule (for), DashboardPanel.refresh (while + for-each),
                          LoanService.submit (do-while), BarChart.paintComponent (indexed for)
  2  Encapsulation        Applicant, Loan (private fields + validated getters/setters)
  3  Inheritance          Loan -> EducationLoan / HomeLoan / PersonalLoan ; MainFrame extends JFrame
  4  Polymorphism         Overriding: getInterestRate, maxEligibleAmount, collectReasons ;
                          Overloading: Loan.calculateEMI(Applicant) / calculateEMI(double,double,int) ;
                          LoanFactory returns Loan references
  5  Interfaces           Eligible, Payable (default method), Reportable (default method),
                          Refreshable, LoanService.ProcessingListener
  6  Exceptions           Custom: InvalidInputException, LoanNotEligibleException,
                          ApplicationNotFoundException ; Built-in: NumberFormatException, IOException,
                          InterruptedException, RuntimeException (multi-catch, try-with-resources)
  7  Threading            LoanService.Worker (2 worker threads + PriorityBlockingQueue),
                          MainFrame.startClock (clock thread), SwingUtilities.invokeLater
  8  User-defined package com.loansystem.{model, exception, service, ui, util}
  9  I/O + Strings        FileManager (BufferedReader/Writer, split, join, format, export report),
                          StringUtil (StringBuilder, regex, replace, substring, title-case)
  10 Collections          LinkedHashMap (applications), PriorityBlockingQueue, EnumMap (statistics),
                          ArrayList, CopyOnWriteArrayList, Comparator / Comparable
  11 Logging              util.AppLogger (java.util.logging, console + file handler)
  12 Event handling       ActionListener, KeyListener (digits-only, live preview, Enter key),
                          MouseListener (hover), ItemListener, ChangeListener (sliders),
                          ListSelectionListener, WindowListener
  13 Application          Complete Swing application tying everything together

LOAN RULES
  Type       Base rate  Min income  Min credit  Max tenure  Age      Max amount
  Education  8.5 %      15,000      600         15 yrs      18-35    40 x income (cap 75 lakh)
  Home       9.0 %      30,000      700         30 yrs      21-60    80 x income, <= 80 % of property
  Personal   12.5 %     20,000      650         5 yrs       21-58    20 x income (cap 25 lakh)
  Rate is reduced for credit score >= 750 / 800 and increased for < 650.  EMI must be <= 50 % of income.
