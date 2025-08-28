package com.microservicio.account.transaction.account_transaction.services;

import com.microservicio.account.transaction.account_transaction.entities.Account;
import com.microservicio.account.transaction.account_transaction.entities.Transaction;
import com.microservicio.account.transaction.account_transaction.exceptions.AccountNotFoundException;
import com.microservicio.account.transaction.account_transaction.exceptions.InsufficientFundsException;
import com.microservicio.account.transaction.account_transaction.exceptions.InternalServerException;
import com.microservicio.account.transaction.account_transaction.exceptions.NoTransactionsFoundException;
import com.microservicio.account.transaction.account_transaction.models.TransactionDTO;
import com.microservicio.account.transaction.account_transaction.repositories.AccountRepository;
import com.microservicio.account.transaction.account_transaction.repositories.TransactionsRepository;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TransactionServiceImp implements TransactionService {

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions() {
        return Optional.of(transactionsRepository.findAll())
                .filter(transactions -> !transactions.isEmpty())
                .map(transactions -> transactions.stream()
                        .map(TransactionMapper.INSTANCE::transactionToDto)
                        .toList())
                .orElseThrow(() -> new NoTransactionsFoundException("No se encontraron transacciones"));
    }

    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(@NonNull Long id) {
        return transactionsRepository.findById(id)
                .map(TransactionMapper.INSTANCE::transactionToDto)
                .orElseThrow(() -> new NoTransactionsFoundException("Transacción no encontrada con ID: " + id));
    }

    @Transactional
    public TransactionDTO saveTransaction(TransactionDTO transactionDto) {
        try {
            log.info("Procesando transacción para cuenta: {}", transactionDto.getAccountNumber());

            Account account = findAccountById(transactionDto.getAccountNumber());

            BigDecimal currentBalance = BigDecimal.valueOf(account.getAvailableBalance());
            BigDecimal transactionValue = BigDecimal.valueOf(transactionDto.getValue());

            // Validar fondos suficientes para débitos
            if (transactionValue.compareTo(BigDecimal.ZERO) < 0) {
                if (currentBalance.compareTo(transactionValue.abs()) < 0) {
                    throw new InsufficientFundsException("Saldo insuficiente en la cuenta");
                }
            }

            // Calcular nuevo balance
            BigDecimal newBalance = currentBalance.add(transactionValue);

            // Crear y guardar transacción
            Transaction transaction = TransactionMapper.INSTANCE.dtoToTransaction(transactionDto);
            transaction.setBalance(newBalance.doubleValue());
            Transaction savedTransaction = transactionsRepository.save(transaction);

            // Actualizar balance de cuenta
            account.setAvailableBalance(newBalance.doubleValue());
            accountRepository.save(account);

            return TransactionMapper.INSTANCE.transactionToDto(savedTransaction);
        } catch (AccountNotFoundException | InsufficientFundsException e) {
            throw e;
        } catch (ConstraintViolationException e) {
            log.error("Constraint violation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error procesando transacción: {}", e.getMessage(), e);
            throw new InternalServerException("Error interno procesando la transacción");
        }
    }

    @Transactional
    public TransactionDTO deleteTransactionById(Long id) {
        try {
            log.info("Eliminando transacción con ID: {}", id);

            Transaction transaction = findTransactionById(id);
            Account account = findAccountById(transaction.getAccountNumber());

            // Revertir transacción
            BigDecimal currentBalance = BigDecimal.valueOf(account.getAvailableBalance());
            BigDecimal transactionValue = BigDecimal.valueOf(transaction.getValue());
            BigDecimal revertedBalance = currentBalance.subtract(transactionValue);

            // Actualizar cuenta
            account.setAvailableBalance(revertedBalance.doubleValue());
            accountRepository.save(account);

            // Eliminar transacción
            transactionsRepository.deleteById(id);

            return TransactionMapper.INSTANCE.transactionToDto(transaction);
        } catch (AccountNotFoundException | NoTransactionsFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error eliminando transacción: {}", e.getMessage(), e);
            throw new InternalServerException(e.getMessage());
        }
    }

    private Account findAccountById(Long accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada: " + accountNumber));
    }

    private Transaction findTransactionById(Long id) {
        return transactionsRepository.findById(id)
                .orElseThrow(() -> new NoTransactionsFoundException("Transacción no encontrada: " + id));
    }
    private void validateAccountExists(Long accountNumber) {
        if (!accountRepository.existsById(accountNumber)) {
            throw new AccountNotFoundException("Cuenta no encontrada: " + accountNumber);
        }
    }
}