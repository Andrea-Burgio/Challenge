package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidTransferAmountException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Before
  public void resetAccounts() {
   accountsService.clearAccounts();
  }

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  public void moneyTransfer_positiveTransfer() throws Exception{
    Account accountFrom = new Account("1");
    Account accountTo = new Account("2");
    accountFrom.setBalance(BigDecimal.valueOf(150));

    accountsService.createAccount(accountFrom);
    accountsService.createAccount(accountTo);

    accountsService.moneyTransfer("1", "2", BigDecimal.valueOf(100));
    assertThat(accountFrom.getBalance()).isEqualTo((BigDecimal.valueOf(50)));
    assertThat(accountTo.getBalance()).isEqualTo((BigDecimal.valueOf(100)));
  }

  @Test
  public void moneyTransfer_negativeTransfer() throws Exception{
    Account accountFrom = new Account("1");
    Account accountTo = new Account("2");

    try {
      accountFrom.setBalance(BigDecimal.valueOf(150));
      accountsService.createAccount(accountFrom);
      accountsService.createAccount(accountTo);
      accountsService.moneyTransfer("1", "2", BigDecimal.valueOf(-5)); //invalid amount
    } catch (InvalidTransferAmountException e){
      assertThat(e.getMessage()).isEqualTo("Transfer amount must be positive");
    }
  }

  @Test
  public void moneyTransfer_negativeBalance() throws Exception{
    Account accountFrom = new Account("1");
    Account accountTo = new Account("2");
    try {
      accountFrom.setBalance(BigDecimal.valueOf(150));  //insufficient balance
      accountsService.createAccount(accountFrom);
      accountsService.createAccount(accountTo);
      accountsService.moneyTransfer("1", "2", BigDecimal.valueOf(200)); //invalid result: 150-200=-50
    } catch (InsufficientBalanceException e){
      assertThat(e.getMessage()).isEqualTo("Insufficient balance in account " + accountFrom.getAccountId());
    }
  }
}
