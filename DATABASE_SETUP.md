# EduPath Database Setup

EduPath uses a shared **MySQL database hosted on Aiven Cloud**. All team members can connect to the same database.

### Connection Details

- **Database:** `EduPath`
- **Host:** `mysql-2e9b99dc-edupathmit.g.aivencloud.com`
- **Port:** `17254`
- **Username:** Shared privately by database administrator
- **Password:** Shared privately by the database administrator
- **SSL Mode:** Require

### How to Connect

1. Install MySQL Workbench.
2. Create a new connection using the details above.
3. Enter the database password shared privately with you.
4. Set SSL mode to `Require`.
5. Click **Test Connection**, then connect.
6. Run the following query to select the database:

```sql
USE EduPath;
SHOW TABLES;
```

### Important

- Do not share database credentials publicly or commit them to GitHub.
- Do not drop, delete, or modify shared tables without informing the team.
- Use the shared `EduPath` database for project development.
