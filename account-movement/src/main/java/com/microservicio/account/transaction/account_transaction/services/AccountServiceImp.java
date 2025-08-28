package com.microservicio.account.transaction.account_transaction.services;

import com.microservicio.account.transaction.account_transaction.configuracion.RabbitMQConfig;
import com.microservicio.account.transaction.account_transaction.entities.Account;
import com.microservicio.account.transaction.account_transaction.entities.Transaction;
import com.microservicio.account.transaction.account_transaction.exceptions.*;
import com.microservicio.account.transaction.account_transaction.models.AccountDTO;
import com.microservicio.account.transaction.account_transaction.models.AccountDtoRed;
import com.microservicio.account.transaction.account_transaction.models.ReportDTO;
import com.microservicio.account.transaction.account_transaction.repositories.AccountRepository;
import com.microservicio.account.transaction.account_transaction.repositories.TransactionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Service
public class AccountServiceImp implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private ClientApiService clientApiService;

    @Autowired
    private AccountNumberGeneratorService accountNumberGeneratorService;

    private static final String DEFAULT_ACCOUNT_TYPE = "Ahorros";
    private static final String DEFAULT_STATE = "true";
    private static final int DEFAULT_BALANCE = 0;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/M/yyyy");

    @Transactional(readOnly = true)
    public List<AccountDTO> getAllAccounts() {
        return Optional.of(accountRepository.findAll())
                .filter(accounts -> !accounts.isEmpty())
                .map(this::mapAccountsToDto)
                .orElseThrow(() -> new NoAccountsFoundException("No accounts were found in the database."));
    }

    @Transactional
    public AccountDTO saveAccount(AccountDTO accountDto) {
        return executeWithExceptionHandling(() -> {
            validateClientDoesNotExits(accountDto.getClientId());
            validateAccountDoesNotExist(accountDto.getAccountNumber());
            accountDto.setAvailableBalance(accountDto.getInitialBalance());
            Account account = AccountMapper.INSTANCE.dtoToAccount(accountDto);
            return AccountMapper.INSTANCE.AccountToDto(accountRepository.save(account));
        });
    }

    @Transactional
    public AccountDTO updateAccount(AccountDTO accountDto, Long id) {
        return executeWithExceptionHandling(() -> {
            Account accountDb = findAccountById(id);
            updateAccountFields(accountDb, accountDto);
            return AccountMapper.INSTANCE.AccountToDto(accountRepository.save(accountDb));
        });
    }

    @Transactional
    public AccountDtoRed deleteAccountById(Long id) {
        return executeWithExceptionHandling(() -> {
            Account accountDb = findAccountById(id);
            accountRepository.deleteById(id);
            return AccountMapper.INSTANCE.AccountToDtoRed(accountDb);
        });
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> generateReportByClientDateRange(Long clientId, Date startDate, Date endDate) {
        return accountRepository.findClientByid(clientId)
                .stream()
                .sorted(Comparator.comparing(Account::getAccountNumber))
                .flatMap(account -> generateReportForAccount(account, clientId, startDate, endDate).stream())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> generateReportDateRange(Date startDate, Date endDate) {
        return clientApiService.getAllIdClients()
                .stream()
                .map(Integer::longValue)
                .flatMap(clientId -> generateReportByClientDateRange(clientId, startDate, endDate).stream())
                .toList();
    }


    @RabbitListener(queues = RabbitMQConfig.QUEUE_CLIENT)
    public void receiveMessage(Long clientId) {
        log.info("Received Message from Cliente Queue: {}", clientId);

        try {
            AccountDTO accountDTO = createDefaultAccount(clientId);
            AccountDTO savedAccount = saveAccount(accountDTO);
            log.info("Cuenta creada:: Cliente->{}, Cuenta->{}", clientId, savedAccount.getAccountNumber());
        } catch (Exception e) {
            log.error("Error creating account for client {}: {}", clientId, e.getMessage(), e);
        }
    }

    private List<AccountDTO> mapAccountsToDto(List<Account> accounts) {
        return accounts.stream()
                .map(AccountMapper.INSTANCE::AccountToDto)
                .toList();
    }

    private void validateAccountDoesNotExist(Long accountNumber) {
        if (accountRepository.existsAccountByAccountNumber(accountNumber)) {
            throw new AccountAlreadyExistsException("The Account already exists");
        }
    }

    private void validateClientDoesNotExits(Long clientId){
        clientApiService.getNameClientById(clientId);
    }

    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account no encontrado con ID: " + id));
    }

    private void updateAccountFields(Account accountDb, AccountDTO accountDto) {
        accountDb.setAccountType(accountDto.getAccountType());
        accountDb.setInitialBalance(accountDto.getInitialBalance());
        accountDb.setState(accountDto.getState());
    }

    private List<ReportDTO> generateReportForAccount(Account account, Long clientId, Date startDate, Date endDate) {
        return transactionsRepository.findByCuentaIdAndFechaBetween(account.getAccountNumber(), startDate, endDate)
                .stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .map(transaction -> createReportDTO(account, transaction, clientId))
                .toList();
    }

    private ReportDTO createReportDTO(Account account, Transaction transaction, Long clientId) {
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setNameClient(clientApiService.getNameClientById(clientId));
        reportDTO.setTransactionDate(formatDate(transaction.getTransactionDate()));
        reportDTO.setAccountNumber(account.getAccountNumber().toString());
        reportDTO.setType(account.getAccountType());
        reportDTO.setInitialBalance(account.getInitialBalance());
        reportDTO.setTransactionValue(transaction.getValue());
        reportDTO.setAvailableBalance(transaction.getBalance());
        reportDTO.setState(account.getState());
        return reportDTO;
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        Instant instant;
        if (date instanceof java.sql.Date) {
            instant = Instant.ofEpochMilli(date.getTime());
        } else {
            instant = date.toInstant();
        }
        LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private AccountDTO createDefaultAccount(Long clientId) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setClientId(clientId);
        accountDTO.setAccountType(DEFAULT_ACCOUNT_TYPE);
        accountDTO.setState(DEFAULT_STATE);
        accountDTO.setInitialBalance(DEFAULT_BALANCE);
        accountDTO.setAvailableBalance(DEFAULT_BALANCE);
        accountDTO.setAccountNumber(Long.parseLong(accountNumberGeneratorService.generateAccountNumber()));
        return accountDTO;
    }

    private <T> T executeWithExceptionHandling(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (AccountNotFoundException | AccountAlreadyExistsException e) {
            throw e; // Re-lanzar excepciones específicas sin envolver
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation occurred", e);
            throw new DuplicateEntryException("The Account already exists");
        } catch (RuntimeException e) {
            log.error("Runtime exception occurred", e);
            throw new InternalServerException(e.getMessage());
        }
    }
}
