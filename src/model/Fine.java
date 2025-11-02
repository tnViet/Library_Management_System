package model;

import java.time.LocalDate;

public class Fine {
    private int id;
    private int loanId;
    private double amount;
    private boolean paid;
    private LocalDate fineDate;

    public Fine() {}
    public Fine(int id, int loanId, double amount, boolean paid, LocalDate fineDate) {
        this.id = id;
        this.loanId = loanId;
        this.amount = amount;
        this.paid = paid;
        this.fineDate = fineDate;
    }
    public Fine(int loanId, double amount, boolean paid, LocalDate fineDate) {
        this.loanId = loanId;
        this.amount = amount;
        this.paid = paid;
        this.fineDate = fineDate;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getLoanId() {
        return loanId;
    }
    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public boolean isPaid() {
        return paid;
    }
    public void setPaid(boolean paid) {
        this.paid = paid;
    }
    public LocalDate getFineDate() {
        return fineDate;
    }
    public void setFineDate(LocalDate fineDate) {
        this.fineDate = fineDate;
    }
    @Override
    public String toString() {
        return "Fine{" + "id=" + id + ", loanId=" + loanId +
                ", amount=" + amount + ", paid=" + paid +
                ", fineDate=" + fineDate + '}';
    }
}