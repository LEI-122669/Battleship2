public class LargeClassExample {

    private String name;
    private String address;
    private String email;
    private String phone;
    private int age;
    private double salary;
    private String department;
    private String position;

    public LargeClassExample() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public double getSalary() {
        return salary;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

    public void printPersonalInformation() {
        System.out.println(name);
        System.out.println(address);
        System.out.println(email);
        System.out.println(phone);
        System.out.println(age);
    }

    public void printProfessionalInformation() {
        System.out.println(salary);
        System.out.println(department);
        System.out.println(position);
    }

    public double calculateAnnualSalary() {
        return salary * 12;
    }

    public double calculateBonus() {
        return salary * 0.15;
    }

    public boolean isEligibleForPromotion() {
        return age > 25 && salary > 2000;
    }

    public void updateContactInformation(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public void increaseSalary(double percentage) {
        salary += salary * percentage / 100;
    }

    public void changeDepartment(String department) {
        this.department = department;
    }

    public void changePosition(String position) {
        this.position = position;
    }

    public void displayAllInformation() {
        printPersonalInformation();
        printProfessionalInformation();
    }
}
