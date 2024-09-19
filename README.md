# Domain Registration App

This project combines a Firebase RTDB, the eNom API, and the WHMCS/Oznorts API to simulate a domain registration system. A user can sign up/login and quickly view an overview of their account, including their active, expiring, and expired domains. They can easily renew unexpired domains for an additional year.

Users can also search for new domains. If a domain is unregistered (within the app and the wider world wide web) and eNom supports the registration of that domain/TLD, they will be able to view the price of the registration and register the domain to their account. (The eNom API is not used to register domains as that process is seriously complicated and costs money.) If the domain is marked as registered with eNom's test API, a user can attempt to search the WHOIS records for their searched domain using the WHMCS API.

Users can also see a detailed history of their previous domain searches and quickly search for those domains again and register them, if available.

## Special Instructions/Limitations
- The app does not actually register any domains with the live or sandbox eNom API. Rather, it "registers" the domain locally (in a Firebase database). Future searches for the domain search that local database first before querying the eNom API to determine if the domain is taken or available.
- Since domains are registered for a year, the “expiring” and “expired” domain lists will usually be empty (unless you go back to test in a year :) ). To demo this functionality, the dates stored in Firebase were manually modified to simulate what happens when the domain is expiring/expired. This can be seen with the demo user listed below.
- Prices are retrieved from the eNom API and marked up 35%, and then rounded up to the nearest $**.95.
- The WHMCS API only returns WHOIS records for a few TLDs (.com, .net, .edu, perhaps a few others).
- WHOIS results will only be possible to search if the domain is registered externally and not locally (try a domain like google.com or gwu.edu)

## Demo User
Email: ozzy@ozzysimpson.com

Password: password

## More information
For more information, including screenshots, view this doc: https://docs.google.com/document/d/1UAaFe6R6wr1Z_MKTSmJMTh0Ee2xgZ8YbaTpQKPrBi4A/edit?usp=sharing
