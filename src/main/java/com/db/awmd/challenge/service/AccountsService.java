package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidTransferAmountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  private final EmailNotificationService emailNotificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, EmailNotificationService emailNotificationService) {
    this.accountsRepository = accountsRepository;
    this.emailNotificationService = emailNotificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }


  public void clearAccounts(){
    accountsRepository.clearAccounts();
  }


  public synchronized void moneyTransfer(String accountFromId, String accountToId, BigDecimal amount) throws RuntimeException {
    Account accountFrom = accountsRepository.getAccount(accountFromId);
    Account accountTo = accountsRepository.getAccount(accountToId);

    BigDecimal totAccountFromBalance;
    BigDecimal totAccountToBalance;

    if (amount.compareTo(BigDecimal.ZERO)>0) {
      totAccountFromBalance = accountFrom.getBalance().subtract(amount);
      totAccountToBalance = accountTo.getBalance().add(amount);
    }
    else{
      throw new InvalidTransferAmountException("Transfer amount must be positive");
    }

    if (totAccountFromBalance.compareTo(BigDecimal.ZERO)>=0) {
      accountFrom.setBalance(totAccountFromBalance);
      accountTo.setBalance(totAccountToBalance);
      accountsRepository.updateAccountBalance(accountFromId, totAccountFromBalance);
      accountsRepository.updateAccountBalance(accountToId, totAccountToBalance);
      emailNotificationService.notifyAboutTransfer(getAccount(accountFromId), "Transfer successful. Your new balance is" + getAccount(accountFromId).getBalance());
      emailNotificationService.notifyAboutTransfer(getAccount(accountToId), "Transfer successful. Your new balance is" + getAccount(accountToId).getBalance());
    }
    else {
      throw new InsufficientBalanceException("Insufficient balance in account " + accountFromId);
    }
  }
}