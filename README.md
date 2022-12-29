# EmployeeManager
Employee Manager

Employee manager contains reactive base implementation using spring WebFlux.

Java Version : 11
Maven Verison : 3.8.1

Employee Manager runs on port 8080
Employee Director runs on port 8081

Use Case Diagram:

![HRManagementUseCaseDiagram](https://user-images.githubusercontent.com/21036082/209912460-15786c7a-31ff-42e3-8cd0-76d96f99cbbd.jpg)


Employee Manager holds employee data with embeded version of Mongo DB and does basic operations like Create, Patch, Delete, Get, GetByCriteria (Dynamic Search), and getAll.
Employee Director manages employee vacation, CRUD operations related to user Role.
There are user role restrictions while doing an operation which described in LLD document.
Approve vacation has been handled.


These functionalities will be added later. 
1. Adding between the dates operation while doing dynamic search. 
2. Reject Vacation
3. Creating a job to renew vacation date.

Patch operation is not working when I sent a request from Director to Manager via WebClient. 
Patch operation is working fine with RestTemplate. 
