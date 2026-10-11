# Fleet Rental Management System (FRMS) — ITS1033 OOP Coursework











Console application in Java (JDK 25+), single package `lk.ijse.fleet`.

## 

## 

## Build \& Run





```
javac -d bin $(find src -name "\\\\\\\*.java")
java -cp bin lk.ijse.fleet.Main
```

## 

## 

## Quick tour





1. Register a user, then log in.
2. Add a vehicle and a customer (menu 1, 2).
3. Create a rental (4) — the `\\\\\\\[EVENT]` line comes from the lambda `RentalEventListener`.
4. Return a vehicle (5); view reports (6, 7, 8).
5. Two daemon threads print fleet status / overdue alerts every 5 seconds.

## 

## 

## Notes





* Passwords are stored in plain text (per assignment); no persistence or roles.
* `CorporateCustomer.outstandingBalance` is not auto-updated on rental, as the brief doesn't require it.

## 

## 

AI-assistance disclosure

\---



AI assistance was used to help understand some programming concepts and code, clarify how certain parts of the system work, and guide the development of some code. I reviewed and adapted the suggestions to suit the requirements of this project.



