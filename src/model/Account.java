package model;

public class Account {
    private int accountId;
    private int typeId;
    private int departmentId;
    private int collegeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String username;
    private String password;
    private String status;

    public Account() {
    }

    public Account(int accountId, int typeId, int departmentId, int collegeId,
                   String firstName, String lastName, String email,
                   String username, String password, String status, String phone) {
        this.accountId = accountId;
        this.typeId = typeId;
        this.departmentId = departmentId;
        this.collegeId = collegeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.status = status;
    }


    public int getAccountId() { return accountId; }
    public int getTypeId() { return typeId; }
    public int getDepartmentId() { return departmentId; }
    public int getCollegeId() { return collegeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getStatus() { return status; }
    public String getPhone() { return phone; }


    public void setAccountId(int accountId) { this.accountId = accountId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public void setCollegeId(int collegeId) { this.collegeId = collegeId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setStatus(String status) { this.status = status; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", role=" + getRole() +
                ", name=" + firstName + " " + lastName +
                '}';
    }

    public String getRole() {
        switch (typeId) {
            case 0:
                return "admin";
            case 1:
                return "user";
            default:
                return "unknown";
        }
    }

}
